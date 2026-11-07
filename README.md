# SwingLedger — local trading workspace

## Run

Double-click `start-local.cmd`, then open http://127.0.0.1:8080.
Keep the command window open; Ctrl+C stops it. Java 21+ is required.
The compiled React UI is included in the Java application. Docker and Node are
not needed to run. If the JAR is missing, the launcher uses Maven 3.9+ to build it.

Create your own account, or explore the illustrative browser-test account:
- Email: demo@swingledger.local
- Password: Local-demo-2026!

This is sample local data, not a broker account. Do not reuse the demo password.
Records persist in `server/data/`. Run from `server` (the launcher does this).
Stop the app before copying that directory for a backup. Sessions expire after
one hour of inactivity and do not survive restarts. The server binds to loopback
only; this build is for local testing, not public hosting.

## Test the workflow

1. Register and create a SWING portfolio: INR 15,000 budget, 1% default risk.
2. Plan a test symbol: entry 200, stop 194, target 224. Calculation returns 25
   shares, INR 5,000 position, INR 150 planned loss and reward/risk 4:1.
3. Save the plan and thesis. Record a purchase of 25 shares at 200 with INR 10 fees.
   Committed capital becomes INR 5,010; available capital is INR 9,990.
4. Add a journal note. Record a complete sale at 224 with INR 15 fees.
   Net realised P&L is INR 575; committed capital returns to zero.
5. Refresh or restart, sign in, and verify the saved record and notes.
6. Create another account to check its records are isolated.

Budget is an allocation, not a broker cash balance. Realised P&L is shown
separately and does not automatically change the budget. Open positions use
purchase cost plus charges, never a live valuation. Plans do not reserve capital;
purchases do. One purchase (up to planned quantity) and one complete sale are
allowed per plan. Execution records cannot be edited or repeated.

## Build and develop

Install Java 21+, Maven 3.9+, Node 22+ and pnpm 11; point JAVA_HOME at your JDK.
Run `./build-local.ps1`, or run these commands manually:

```powershell
cd frontend
pnpm install --frozen-lockfile
node node_modules/vite/bin/vite.js build
cd ../server
mvn clean verify
java -jar target/app-0.0.1-SNAPSHOT.jar
```

For frontend development, start Java and run `pnpm dev` in frontend; Vite proxies
API requests to port 8080. System fonts are used if web fonts are unavailable.
Installed Maven is the verified path; the bundled Maven wrapper has an existing
startup problem on this machine.

## Optional PostgreSQL

Start Docker Desktop. Set a locally chosen POSTGRES_PASSWORD environment variable,
then run `docker compose up -d` from the root. Before starting Java, export:
- DB_URL = jdbc:postgresql://localhost:5432/swingledger
- DB_USER = swingledger
- DB_PASSWORD = your chosen password

Flyway creates/version-controls the schema. This is a separate database; existing
H2 records are not transferred. Spring does not load .env files automatically.
Never commit actual passwords. Run tests against a disposable PostgreSQL database
using TEST_DB_URL, TEST_DB_USER and TEST_DB_PASSWORD, then `mvn test`.
A PostgreSQL service job is included in CI; local execution was not verified
because Docker's engine was unavailable.

## Architecture and coding standards

React forms -> HTTP controllers -> transactional service -> JDBC -> H2/PostgreSQL.

- auth: BCrypt passwords, server-side sessions, CSRF and session-ID rotation.
- risk: immutable request/result records and BigDecimal sizing.
- portfolio: ownership-scoped queries, validation and transactional ledger operations.
- trade: PLANNED -> OPEN -> CLOSED state policy.
- common: consistent API errors and health endpoint.
- Flyway: foreign keys, indexes, decimal money and schema version history.

Controllers handle HTTP; services own business rules. Server-side calculations
and authenticated ownership are authoritative. Portfolio row locks protect
capital-changing operations against concurrent requests. Calendar dates are
serialized without timezone conversion. Tests cover arithmetic, authentication,
ownership, duplicate actions, concurrent opening, fees, losing trades and rollback.

53 backend tests pass on Java 21/H2. React production build passes. Browser QA
covers the manual workflow. PostgreSQL and hosted CI still need environment runs.
No password reset, email verification, partial fills, live prices, broker orders,
tax, or AI features are included. See server/docs/delivery-status.md for ticket coverage.
