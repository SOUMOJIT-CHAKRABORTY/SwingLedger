package com.swingledger.app.trade;

/** Allowed states for a manually recorded, single-buy/single-sell trade. */
public enum TradeStatus {

	PLANNED,
	OPEN,
	CLOSED;

	/** Returns the next state, rejecting skipped, repeated, or reversed transitions. */
	public TradeStatus transitionTo(TradeStatus next) {
		if (next == null) {
			throw new IllegalArgumentException("Next trade status is required");
		}
		if ((this == PLANNED && next == OPEN) || (this == OPEN && next == CLOSED)) {
			return next;
		}
		throw new IllegalStateException("Cannot transition trade from " + this + " to " + next);
	}
}
