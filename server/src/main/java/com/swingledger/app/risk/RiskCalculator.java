package com.swingledger.app.risk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class RiskCalculator {
	public RiskResult calculate(RiskRequest request) {
		if (request == null)
			throw new IllegalArgumentException("Request is required");
		positive(request.capital(), "capital");
		positive(request.riskPercent(), "riskPercent");
		positive(request.entry(), "entry");
		positive(request.stopLoss(), "stopLoss");
		positive(request.target(), "target");
		if (request.riskPercent().compareTo(new BigDecimal("100")) > 0)
			throw new IllegalArgumentException("riskPercent must not exceed 100");
		if (request.stopLoss().compareTo(request.entry()) >= 0)
			throw new IllegalArgumentException("stopLoss must be below entry");
		if (request.target().compareTo(request.entry()) <= 0)
			throw new IllegalArgumentException("target must be above entry");

		BigDecimal budget = request.capital().multiply(request.riskPercent()).movePointLeft(2);
		BigDecimal lossPerShare = request.entry().subtract(request.stopLoss());
		BigDecimal quantity = budget.divide(lossPerShare, 0, RoundingMode.FLOOR)
				.min(request.capital().divide(request.entry(), 0, RoundingMode.FLOOR));
		return new RiskResult(budget, quantity.toBigIntegerExact(), quantity.multiply(request.entry()),
				quantity.multiply(lossPerShare),
				request.target().subtract(request.entry()).divide(lossPerShare, 4, RoundingMode.HALF_UP),
				quantity.multiply(request.target().subtract(request.entry())));
	}

	private void positive(BigDecimal value, String field) {
		if (value == null || value.signum() <= 0)
			throw new IllegalArgumentException(field + " must be greater than zero");
		// Bound decimal work and prevent impractically large inputs at the API
		// boundary.
		if (value.precision() > 18 || value.scale() > 8 || value.scale() < -8)
			throw new IllegalArgumentException(field + " supports at most 18 digits and 8 decimal places");
	}
}
