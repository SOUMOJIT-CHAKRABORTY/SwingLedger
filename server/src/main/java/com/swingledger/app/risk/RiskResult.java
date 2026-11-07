package com.swingledger.app.risk;

import java.math.BigDecimal;
import java.math.BigInteger;

public record RiskResult(BigDecimal riskBudget, BigInteger quantity, BigDecimal positionValue,
        BigDecimal plannedLoss, BigDecimal rewardToRiskRatio, BigDecimal potentialProfit) {
}
