package com.swingledger.app.risk;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/risk")
public class RiskController {
    private final RiskCalculator calculator;

    public RiskController(RiskCalculator calculator) {
        this.calculator = calculator;
    }

    @PostMapping("/calculate")
    public RiskResult calculate(@RequestBody RiskRequest request) {
        return calculator.calculate(request);
    }
}
