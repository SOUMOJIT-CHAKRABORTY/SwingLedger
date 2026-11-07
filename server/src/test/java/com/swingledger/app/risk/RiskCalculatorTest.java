package com.swingledger.app.risk;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class RiskCalculatorTest {
    private final RiskCalculator calculator = new RiskCalculator();

    static RiskRequest request(String capital, String risk, String entry, String stop, String target) {
        return new RiskRequest(new BigDecimal(capital), new BigDecimal(risk), new BigDecimal(entry),
                new BigDecimal(stop), new BigDecimal(target));
    }

    @Test
    void matchesTicketExample() {
        var result = calculator.calculate(request("15000", "1", "200", "194", "224"));
        assertEquals("25", result.quantity().toString());
        assertDecimal("150", result.riskBudget());
        assertDecimal("5000", result.positionValue());
        assertDecimal("150", result.plannedLoss());
        assertDecimal("4", result.rewardToRiskRatio());
        assertDecimal("600", result.potentialProfit());
    }

    @ParameterizedTest
    @CsvSource({"100,100,200,194,224,0", "15000,0.001,200,194,224,0",
            "1000,100,300,299,302,3", "1000,1,100,97,110,3",
            "15000,1,200,194,224,25", "0.30,100,0.10,0.09,0.12,3"})
    void floorsQuantityAndRespectsBothBudgets(String capital, String risk, String entry,
            String stop, String target, String quantity) {
        var input = request(capital, risk, entry, stop, target);
        var result = calculator.calculate(input);
        assertEquals(quantity, result.quantity().toString());
        assertTrue(result.positionValue().compareTo(input.capital()) <= 0);
        assertTrue(result.plannedLoss().compareTo(result.riskBudget()) <= 0);
        if (result.quantity().signum() == 0) assertDecimal("0", result.potentialProfit());
    }

    @Test
    void roundsRatioOnlyAfterExactSizing() {
        var result = calculator.calculate(request("1000", "1", "100", "97", "110"));
        assertDecimal("3.3333", result.rewardToRiskRatio());
        assertDecimal("9", result.plannedLoss());
        assertDecimal("30", result.potentialProfit());
    }

    @ParameterizedTest
    @CsvSource({"0,1,200,194,224", "-1,1,200,194,224", "100,0,200,194,224",
            "100,-1,200,194,224", "100,101,200,194,224", "100,1,0,194,224",
            "100,1,-1,194,224", "100,1,200,200,224", "100,1,200,201,224",
            "100,1,200,194,200", "100,1,200,194,199", "100,1,200,0,224",
            "100,1,200,-1,224", "100,1,200,194,0", "100,1,200,194,-1",
            "100,0.000000001,200,194,224", "1E30,1,200,194,224"})
    void rejectsInvalidInputs(String capital, String risk, String entry, String stop, String target) {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(request(capital, risk, entry, stop, target)));
    }

    @Test
    void rejectsMissingRequestAndEachMissingField() {
        assertThrows(IllegalArgumentException.class, () -> calculator.calculate(null));
        BigDecimal[] values = {new BigDecimal("15000"), BigDecimal.ONE, new BigDecimal("200"),
                new BigDecimal("194"), new BigDecimal("224")};
        for (int index = 0; index < values.length; index++) {
            BigDecimal[] missing = values.clone();
            missing[index] = null;
            assertThrows(IllegalArgumentException.class, () -> calculator.calculate(
                    new RiskRequest(missing[0], missing[1], missing[2], missing[3], missing[4])));
        }
    }

    private static void assertDecimal(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
