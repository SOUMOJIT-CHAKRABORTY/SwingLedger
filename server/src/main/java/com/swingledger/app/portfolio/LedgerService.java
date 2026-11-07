package com.swingledger.app.portfolio;

import java.math.*;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.swingledger.app.risk.*;
import com.swingledger.app.trade.TradeStatus;

/** Transaction boundaries and ownership checks for the manual ledger. */
@Service
public class LedgerService {
	private final JdbcTemplate db;
	private final RiskCalculator calculator;

	public LedgerService(JdbcTemplate db, RiskCalculator calculator) {
		this.db = db;
		this.calculator = calculator;
	}

	public record PortfolioInput(String name, String type, BigDecimal capital, BigDecimal riskPercent) {
	}

	public record PlanInput(String portfolioId, String symbol, BigDecimal entry, BigDecimal stopLoss, BigDecimal target,
			BigDecimal riskPercent, String rationale) {
	}

	public record BuyInput(String planId, BigDecimal price, Long quantity, LocalDate date, BigDecimal fees) {
	}

	public record SellInput(BigDecimal price, LocalDate date, BigDecimal fees) {
	}

	public List<Map<String, Object>> portfolios(String owner) {
		return db.queryForList("SELECT * FROM portfolios WHERE owner_id=? ORDER BY name,id", owner);
	}

	public Map<String, Object> portfolio(String owner, String id) {
		return ownedPortfolio(owner, id, false);
	}

	private Map<String, Object> ownedPortfolio(String owner, String id, boolean lock) {
		return one(db.queryForList("SELECT * FROM portfolios WHERE id=? AND owner_id=?" + (lock ? " FOR UPDATE" : ""),
				id, owner));
	}

	@Transactional
	public Map<String, Object> savePortfolio(String owner, String id, PortfolioInput input) {
		text(input.name(), 80, "Portfolio name");
		if (!Set.of("SWING", "LONG_TERM").contains(Objects.toString(input.type(), "")))
			bad("Choose SWING or LONG_TERM");
		money(input.capital(), false);
		risk(input.riskPercent());
		if (id == null) {
			id = UUID.randomUUID().toString();
			db.update("INSERT INTO portfolios VALUES (?,?,?,?,?,?)", id, owner, input.name().trim(), input.type(),
					input.capital(), input.riskPercent());
		} else {
			ownedPortfolio(owner, id, true);
			if (input.capital().compareTo(committed(id)) < 0)
				bad("Budget cannot be below committed purchase capital");
			db.update("UPDATE portfolios SET name=?,type=?,capital=?,risk_percent=? WHERE id=?", input.name().trim(),
					input.type(), input.capital(), input.riskPercent(), id);
		}
		return portfolio(owner, id);
	}

	public Map<String, Object> summary(String owner, String id) {
		var p = portfolio(owner, id);
		var result = new LinkedHashMap<String, Object>();
		BigDecimal budget = (BigDecimal) p.get("capital"), committed = committed(id);
		var trades = db.queryForList("SELECT net_pnl FROM trade_plans WHERE portfolio_id=? AND status='CLOSED'", id);
		BigDecimal pnl = BigDecimal.ZERO;
		int wins = 0, losses = 0;
		for (var row : trades) {
			BigDecimal value = (BigDecimal) row.get("net_pnl");
			pnl = pnl.add(value);
			if (value.signum() > 0)
				wins++;
			if (value.signum() < 0)
				losses++;
		}
		result.put("budget", budget);
		result.put("committed", committed);
		result.put("available", budget.subtract(committed));
		result.put("realisedPnl", pnl);
		result.put("closedCount", trades.size());
		result.put("wins", wins);
		result.put("losses", losses);
		result.put("valuationNote",
				"Open positions are shown at purchase cost. No live market valuation. Realised P&L does not automatically change the allocated budget.");
		return result;
	}

	private BigDecimal committed(String id) {
		return db.queryForObject(
				"SELECT COALESCE(SUM(buy_price*buy_quantity+buy_fees),0) FROM trade_plans WHERE portfolio_id=? AND status='OPEN'",
				BigDecimal.class, id);
	}

	public List<Map<String, Object>> plans(String owner, String portfolioId) {
		portfolio(owner, portfolioId);
		return db
				.queryForList("SELECT * FROM trade_plans WHERE portfolio_id=? ORDER BY created_at DESC,id", portfolioId)
				.stream().map(this::dates).toList();
	}

	public Map<String, Object> plan(String owner, String id) {
		return dates(one(db.queryForList(
				"SELECT t.* FROM trade_plans t JOIN portfolios p ON p.id=t.portfolio_id WHERE t.id=? AND p.owner_id=?",
				id, owner)));
	}

	@Transactional
	public Map<String, Object> createPlan(String owner, PlanInput input) {
		var p = ownedPortfolio(owner, input.portfolioId(), true);
		if (input.symbol() == null || !input.symbol().matches("[A-Za-z0-9][A-Za-z0-9.:-]{0,29}"))
			bad("Enter a valid exchange symbol (letters, numbers, dot, colon or hyphen)");
		String rationale = input.rationale() == null ? "" : input.rationale().trim();
		if (rationale.length() > 4000)
			bad("Rationale must be at most 4000 characters");
		money(input.entry(), false);
		money(input.stopLoss(), false);
		money(input.target(), false);
		risk(input.riskPercent());
		BigDecimal available = ((BigDecimal) p.get("capital")).subtract(committed(input.portfolioId()));
		if (available.signum() <= 0)
			bad("No available capital in this portfolio");
		var result = calculator.calculate(
				new RiskRequest(available, input.riskPercent(), input.entry(), input.stopLoss(), input.target()));
		if (result.quantity().signum() == 0)
			bad("Risk or capital is insufficient for one share");
		long quantity;
		try {
			quantity = result.quantity().longValueExact();
		} catch (ArithmeticException e) {
			throw new IllegalArgumentException("Quantity is too large");
		}
		String id = UUID.randomUUID().toString();
		db.update(
				"INSERT INTO trade_plans (id,portfolio_id,symbol,entry_price,stop_loss,target_price,risk_percent,quantity,status,created_at,rationale) VALUES (?,?,?,?,?,?,?,?,'PLANNED',?,?)",
				id, input.portfolioId(), input.symbol().toUpperCase(Locale.ROOT), input.entry(), input.stopLoss(),
				input.target(), input.riskPercent(), quantity, LocalDateTime.now(Clock.systemUTC()), rationale);
		return plan(owner, id);
	}

	@Transactional
	public Map<String, Object> open(String owner, BuyInput input) {
		var initial = plan(owner, input.planId());
		var p = ownedPortfolio(owner, (String) initial.get("portfolio_id"), true);
		var plan = plan(owner, input.planId());
		transition(plan, "OPEN");
		money(input.price(), false);
		money(input.fees(), true);
		date(input.date());
		if (input.quantity() == null || input.quantity() <= 0
				|| input.quantity() > ((Number) plan.get("quantity")).longValue())
			bad("Buy quantity must be positive and no greater than the planned quantity");
		BigDecimal cost = input.price().multiply(BigDecimal.valueOf(input.quantity())).add(input.fees());
		BigDecimal available = ((BigDecimal) p.get("capital")).subtract(committed((String) p.get("id")));
		if (cost.compareTo(available) > 0)
			bad("Purchase including charges exceeds available capital");
		db.update("UPDATE trade_plans SET status='OPEN',buy_price=?,buy_quantity=?,buy_date=?,buy_fees=? WHERE id=?",
				input.price(), input.quantity(), input.date(), input.fees(), input.planId());
		return plan(owner, input.planId());
	}

	@Transactional
	public Map<String, Object> close(String owner, String id, SellInput input) {
		var initial = plan(owner, id);
		ownedPortfolio(owner, (String) initial.get("portfolio_id"), true);
		var plan = plan(owner, id);
		transition(plan, "CLOSED");
		money(input.price(), false);
		money(input.fees(), true);
		date(input.date());
		LocalDate bought = LocalDate.parse((String) plan.get("buy_date"));
		if (input.date().isBefore(bought))
			bad("Sell date cannot precede buy date");
		BigDecimal pnl = input.price().subtract((BigDecimal) plan.get("buy_price"))
				.multiply(BigDecimal.valueOf(((Number) plan.get("buy_quantity")).longValue()))
				.subtract((BigDecimal) plan.get("buy_fees")).subtract(input.fees());
		db.update("UPDATE trade_plans SET status='CLOSED',sell_price=?,sell_date=?,sell_fees=?,net_pnl=? WHERE id=?",
				input.price(), input.date(), input.fees(), pnl, id);
		return plan(owner, id);
	}

	public List<Map<String, Object>> notes(String owner, String id) {
		plan(owner, id);
		return db.queryForList(
				"SELECT id,trade_id,author_id,note,created_at FROM journal_entries WHERE trade_id=? ORDER BY created_at,id",
				id).stream().map(this::dates).toList();
	}

	@Transactional
	public Map<String, Object> note(String owner, String id, String note) {
		plan(owner, id);
		text(note, 4000, "Journal note");
		String noteId = UUID.randomUUID().toString();
		db.update("INSERT INTO journal_entries VALUES (?,?,?,?,?)", noteId, id, owner, note.trim(),
				LocalDateTime.now(Clock.systemUTC()));
		return dates(db.queryForMap("SELECT * FROM journal_entries WHERE id=?", noteId));
	}

	private Map<String, Object> dates(Map<String, Object> row) {
		row.replaceAll((key, value) -> value instanceof java.sql.Date date ? date.toLocalDate().toString()
				: value instanceof java.sql.Timestamp time ? time.toLocalDateTime().toString() + "Z" : value);
		return row;
	}

	private void transition(Map<String, Object> plan, String next) {
		try {
			TradeStatus.valueOf((String) plan.get("status")).transitionTo(TradeStatus.valueOf(next));
		} catch (IllegalStateException e) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
		}
	}

	private static Map<String, Object> one(List<Map<String, Object>> rows) {
		if (rows.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Record not found");
		return rows.getFirst();
	}

	private static void text(String s, int max, String field) {
		if (s == null || s.isBlank() || s.length() > max)
			bad(field + " is required and must be at most " + max + " characters");
	}

	private static void money(BigDecimal v, boolean zero) {
		if (v == null || v.signum() < 0 || (!zero && v.signum() == 0) || v.precision() > 16 || v.scale() > 8
				|| v.scale() < -8 || v.compareTo(new BigDecimal("1000000000000")) > 0)
			bad("Money must be " + (zero ? "nonnegative" : "positive")
					+ ", at most 1 trillion, with up to 8 decimal places");
	}

	private static void risk(BigDecimal v) {
		money(v, false);
		if (v.compareTo(new BigDecimal("100")) > 0)
			bad("Risk percentage must not exceed 100");
	}

	private static void date(LocalDate date) {
		if (date == null || date.isAfter(LocalDate.now()) || date.isBefore(LocalDate.of(1900, 1, 1)))
			bad("Enter a valid execution date between 1900 and today");
	}

	private static void bad(String message) {
		throw new IllegalArgumentException(message);
	}
}
