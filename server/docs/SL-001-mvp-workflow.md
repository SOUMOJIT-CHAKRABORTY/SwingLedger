# SL-001: MVP workflow and scope

Source: https://app.notion.com/p/408344297d6a810ca17ad91cb68af060

## User journey

The following illustrative prices are manually entered sample data, not market quotes.
Risk percentage and prices are example inputs, not application defaults.

1. Create a portfolio with INR 15,000 of swing-trading capital.
2. Choose a cash equity and plan a long trade: entry INR 100, stop INR 95,
   target INR 110, risk budget 1% of capital (INR 150).
3. Calculate risk per share: 100 - 95 = INR 5. Risk permits 30 shares
   (150 / 5); available cash permits 150 shares (15,000 / 100).
   Choose the smaller whole-share quantity: 30. Planned investment is
   INR 3,000, planned risk INR 150, and reward-to-risk ratio 2:1.
   Risk is an estimate: execution can differ from a stop price.
4. Save the plan and its rationale. A plan is not an executed order and does
   not consume cash. If sizing produces zero shares, revise the plan.
5. Buy externally and manually record one completed buy of 30 shares at
   INR 100. The trade becomes OPEN. Available cash is INR 12,000.
6. Sell externally and manually record one complete sell of all 30 shares at
   INR 110. The trade becomes CLOSED. Gross proceeds are INR 3,300,
   gross realized P&L is INR 300, and cash is INR 15,300 before charges.
7. Review entry, exit, quantity, original rationale, gross P&L, and journal
   notes. Compare the outcome to the plan; retain the original plan for review.

## Scope decisions

- Long-only cash equities; whole shares; no short positions.
- Exactly one completed buy and one complete sell per trade. No scale-in,
  scale-out, partial fills, or partial closes.
- Manual prices and execution records. Saving a plan never places an order.
- Excluded: live-market data, brokerage execution/integration, AI features,
  tax calculation/reporting, partial fills, and derivatives.
- Sample calculations are gross of charges; fee treatment, API contracts,
  persistence, authentication, and detailed sizing validation belong to later tickets.

## Lifecycle contract and implementation boundary

PLANNED -> OPEN -> CLOSED. CLOSED is terminal. Skipping, reversing, or repeating
a transition is rejected by the domain policy. Request-level retry/idempotency
handling belongs to the API/service layer in later tickets.

`TradeStatus` encodes only this transition policy. It does not prove a buy or
sell occurred, validate quantity, persist trades, or enforce account ownership.
Those checks must be implemented by the later trade services.

## MVP success and acceptance evidence

MVP success means one live end-to-end workflow in the running application:
create the INR 15,000 portfolio, calculate risk, save a plan, manually record
the buy, manually record the complete sell, and review the saved result after
reloading. No automated brokerage execution is required. This is a future
release acceptance check, not a claim that the current starter app supports it.

SL-001 acceptance mapping:

- INR 15,000 journey: steps 1–7 above.
- Long-only and one buy/complete sell: scope decisions above.
- All six exclusions: scope decisions above.
- One live end-to-end success definition: preceding paragraph.

## Coding standards illustrated

- Put business rules in a focused domain type, independent of Spring or a database.
- Use an enum for a finite set of states instead of arbitrary strings.
- Keep fields immutable and expose behavior through a small public method.
- Reject invalid transitions explicitly; do not silently accept inconsistent state.
- Test observable behavior, including every forbidden state pair and null input.
- Use descriptive test names and Arrange–Act–Assert structure. Unit tests need
  no Spring context; the existing context-load test remains an integration smoke test.
- Keep scope honest: a passing transition test is not an end-to-end trade test.

From `server`, run `./mvnw.cmd test` on Windows (or `./mvnw test` on Unix).
Run only the new unit tests with `./mvnw.cmd -Dtest=TradeStatusTest test`.

Validation on 2026-11-02: installed Maven 3.9.10 with Java 25 passed all 12
tests (11 lifecycle cases and the existing Spring context smoke test).
The wrapper failed to start in this environment, and the default JAVA_HOME
selected Java 21 despite Java 25 being on PATH. The successful PowerShell command was:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.5.0.2'
mvn test
```

This changes JAVA_HOME only for the current shell session.
