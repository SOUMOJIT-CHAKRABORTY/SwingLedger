package com.swingledger.app.risk;

import java.math.BigDecimal;

public record RiskRequest(BigDecimal capital, BigDecimal riskPercent, BigDecimal entry,
        BigDecimal stopLoss, BigDecimal target) {
}
