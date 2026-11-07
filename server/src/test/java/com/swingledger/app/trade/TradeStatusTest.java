package com.swingledger.app.trade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradeStatusTest {

	@Test
	void plannedTradeCanBeOpenedAndThenClosed() {
		TradeStatus status = TradeStatus.PLANNED;

		status = status.transitionTo(TradeStatus.OPEN);
		assertEquals(TradeStatus.OPEN, status);
		status = status.transitionTo(TradeStatus.CLOSED);
		assertEquals(TradeStatus.CLOSED, status);
	}

	@ParameterizedTest
	@CsvSource({"PLANNED, PLANNED", "PLANNED, CLOSED", "OPEN, PLANNED", "OPEN, OPEN",
			"CLOSED, PLANNED", "CLOSED, OPEN", "CLOSED, CLOSED"})
	void rejectsSkippedRepeatedAndReversedTransitions(TradeStatus current, TradeStatus next) {
		assertThrows(IllegalStateException.class, () -> current.transitionTo(next));
	}

	@ParameterizedTest
	@EnumSource(TradeStatus.class)
	void rejectsMissingNextStatus(TradeStatus current) {
		assertThrows(IllegalArgumentException.class, () -> current.transitionTo(null));
	}
}
