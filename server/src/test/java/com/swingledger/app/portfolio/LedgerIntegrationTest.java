package com.swingledger.app.portfolio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import com.swingledger.app.portfolio.LedgerService.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LedgerIntegrationTest {
    @Autowired LedgerService ledger;
    @Autowired JdbcTemplate db;
    String owner, other, portfolio;
    BigDecimal n(String value) { return new BigDecimal(value); }
    @BeforeEach void setup() {
        owner=UUID.randomUUID().toString();other=UUID.randomUUID().toString();
        for(String id:List.of(owner,other)) db.update("INSERT INTO app_users VALUES (?,?,?,?)",id,id+"@test.local","Test","not-a-login");
        portfolio=(String)ledger.savePortfolio(owner,null,new PortfolioInput("Swing","SWING",n("15000"),n("1"))).get("id");
    }
    String plan() { return (String)ledger.createPlan(owner,new PlanInput(portfolio,"NSE:TEST",n("200"),n("194"),n("224"),n("1"),"Breakout thesis")).get("id"); }
    BuyInput buy(String id) { return new BuyInput(id,n("200"),25L,LocalDate.now().minusDays(1),n("10")); }
    @Test void fullLifecycleAccountsForFeesAndReleasesCapital() {
        String id=plan(); ledger.open(owner,buy(id));
        assertEquals(LocalDate.now().minusDays(1).toString(),ledger.plan(owner,id).get("buy_date"));
        assertEquals(0,n("5010").compareTo((BigDecimal)ledger.summary(owner,portfolio).get("committed")));
        assertThrows(ResponseStatusException.class,()->ledger.open(owner,buy(id)));
        ledger.note(owner,id,"Bought after confirmation");ledger.note(owner,id,"Closed at target");
        assertEquals(2,ledger.notes(owner,id).size());
        var closed=ledger.close(owner,id,new SellInput(n("224"),LocalDate.now(),n("15")));
        assertEquals(0,n("575").compareTo((BigDecimal)closed.get("net_pnl")));
        var summary=ledger.summary(owner,portfolio);
        assertEquals(1,summary.get("wins"));assertEquals(1,summary.get("closedCount"));
        assertEquals(0,((BigDecimal)summary.get("committed")).signum());
        assertThrows(ResponseStatusException.class,()->ledger.close(owner,id,new SellInput(n("224"),LocalDate.now(),n("0"))));
    }
    @Test void losingTradeIncludesBothCharges() {
        String id=plan();ledger.open(owner,buy(id));
        var result=ledger.close(owner,id,new SellInput(n("190"),LocalDate.now(),n("5")));
        assertEquals(0,n("-265").compareTo((BigDecimal)result.get("net_pnl")));
        assertEquals(1,ledger.summary(owner,portfolio).get("losses"));
    }
    @Test void invalidOperationsRollbackWithoutChangingBalanceOrStatus() {
        String id=plan();
        assertThrows(IllegalArgumentException.class,()->ledger.open(owner,new BuyInput(id,n("1000"),25L,LocalDate.now(),n("0"))));
        assertEquals("PLANNED",ledger.plan(owner,id).get("status"));
        assertEquals(0,((BigDecimal)ledger.summary(owner,portfolio).get("committed")).signum());
        assertThrows(ResponseStatusException.class,()->ledger.close(owner,id,new SellInput(n("220"),LocalDate.now(),n("0"))));
        ledger.open(owner,buy(id));
        assertThrows(IllegalArgumentException.class,()->ledger.close(owner,id,new SellInput(n("220"),LocalDate.now().minusDays(2),n("0"))));
        assertThrows(IllegalArgumentException.class,()->ledger.close(owner,id,new SellInput(n("220"),LocalDate.now().plusDays(1),n("0"))));
        assertEquals("OPEN",ledger.plan(owner,id).get("status"));
        assertThrows(IllegalArgumentException.class,()->ledger.savePortfolio(owner,portfolio,new PortfolioInput("Swing","SWING",n("100"),n("1"))));
        assertEquals(0,n("15000").compareTo((BigDecimal)ledger.portfolio(owner,portfolio).get("capital")));
    }
    @Test void isolatesEveryOwnedResource() {
        String id=plan();
        assertTrue(ledger.portfolios(other).isEmpty());
        assertThrows(ResponseStatusException.class,()->ledger.portfolio(other,portfolio));
        assertThrows(ResponseStatusException.class,()->ledger.summary(other,portfolio));
        assertThrows(ResponseStatusException.class,()->ledger.plan(other,id));
        assertThrows(ResponseStatusException.class,()->ledger.plans(other,portfolio));
        assertThrows(ResponseStatusException.class,()->ledger.note(other,id,"intrusion"));
        assertThrows(ResponseStatusException.class,()->ledger.notes(other,id));
        assertThrows(ResponseStatusException.class,()->ledger.open(other,buy(id)));
        assertThrows(ResponseStatusException.class,()->ledger.close(other,id,new SellInput(n("220"),LocalDate.now(),n("0"))));
        assertThrows(ResponseStatusException.class,()->ledger.savePortfolio(other,portfolio,new PortfolioInput("X","SWING",n("10"),n("1"))));
    }
    @Test void validatesPlansAndPortfolioBoundaries() {
        for(var input:List.of(new PortfolioInput("A","UNKNOWN",n("100"),n("1")),new PortfolioInput("A","SWING",n("0"),n("1")),new PortfolioInput("A","SWING",n("100"),n("101"))))
            assertThrows(IllegalArgumentException.class,()->ledger.savePortfolio(owner,null,input));
        assertThrows(IllegalArgumentException.class,()->ledger.createPlan(owner,new PlanInput(portfolio,"bad symbol",n("200"),n("194"),n("224"),n("1"),"")));
        assertThrows(IllegalArgumentException.class,()->ledger.createPlan(owner,new PlanInput(portfolio,"TEST",n("200"),n("201"),n("224"),n("1"),"")));
        assertThrows(IllegalArgumentException.class,()->ledger.createPlan(owner,new PlanInput(portfolio,"TEST",n("20000"),n("19000"),n("22000"),n("1"),"")));
        assertThrows(ResponseStatusException.class,()->ledger.plan(owner,"missing"));
        assertEquals(0,ledger.summary(owner,portfolio).get("closedCount"));
        ledger.savePortfolio(owner,portfolio,new PortfolioInput("Updated","LONG_TERM",n("20000"),n("2")));
        assertEquals("Updated",ledger.portfolio(owner,portfolio).get("name"));
    }
    @Test void concurrentOpenCannotOverspendOrOpenTwice() throws Exception {
        String id=plan();
        try(var executor=java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var start=new java.util.concurrent.CountDownLatch(1);
            java.util.concurrent.Callable<Boolean> work=()->{start.await();try{ledger.open(owner,buy(id));return true;}catch(ResponseStatusException e){return false;}};
            var a=executor.submit(work);var b=executor.submit(work);start.countDown();
            assertNotEquals(a.get(),b.get());
            assertEquals(0,n("5010").compareTo((BigDecimal)ledger.summary(owner,portfolio).get("committed")));
        }
    }
}
