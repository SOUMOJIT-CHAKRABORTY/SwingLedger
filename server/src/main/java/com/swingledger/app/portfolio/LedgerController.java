package com.swingledger.app.portfolio;

import java.security.Principal;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import com.swingledger.app.portfolio.LedgerService.*;

@RestController
@RequestMapping("/api/v1")
public class LedgerController {
    private final LedgerService service;
    public LedgerController(LedgerService service) { this.service=service; }
    @GetMapping("/portfolios") public Object portfolios(Principal p) { return service.portfolios(p.getName()); }
    @PostMapping("/portfolios") public Object createPortfolio(Principal p,@RequestBody PortfolioInput input) { return service.savePortfolio(p.getName(),null,input); }
    @GetMapping("/portfolios/{id}") public Object portfolio(Principal p,@PathVariable String id) { return service.portfolio(p.getName(),id); }
    @PatchMapping("/portfolios/{id}") public Object updatePortfolio(Principal p,@PathVariable String id,@RequestBody PortfolioInput input) { return service.savePortfolio(p.getName(),id,input); }
    @GetMapping("/portfolios/{id}/summary") public Object summary(Principal p,@PathVariable String id) { return service.summary(p.getName(),id); }
    @GetMapping("/trade-plans") public Object plans(Principal p,@RequestParam String portfolioId) { return service.plans(p.getName(),portfolioId); }
    @GetMapping("/trade-plans/{id}") public Object plan(Principal p,@PathVariable String id) { return service.plan(p.getName(),id); }
    @PostMapping("/trade-plans") public Object createPlan(Principal p,@RequestBody PlanInput input) { return service.createPlan(p.getName(),input); }
    @PostMapping("/trades") public Object open(Principal p,@RequestBody BuyInput input) { return service.open(p.getName(),input); }
    @PatchMapping("/trades/{id}/close") public Object close(Principal p,@PathVariable String id,@RequestBody SellInput input) { return service.close(p.getName(),id,input); }
    @GetMapping("/trades/{id}/journal") public Object notes(Principal p,@PathVariable String id) { return service.notes(p.getName(),id); }
    public record NoteInput(String note) {}
    @PostMapping("/trades/{id}/journal") public Object note(Principal p,@PathVariable String id,@RequestBody NoteInput input) { return service.note(p.getName(),id,input.note()); }
}
