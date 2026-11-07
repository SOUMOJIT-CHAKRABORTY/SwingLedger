# Delivery status — 2026-11-02

Local MVP delivered. Notion statuses unchanged. No remote push or public deployment.

| Tickets | Outcome |
| --- | --- |
| SL-001–007 | Scope, Java foundation, CI, risk API and tests implemented |
| SL-008 | Persistent local H2 and Flyway verified; PostgreSQL Compose supplied, actual PostgreSQL run pending |
| SL-009–010 | Registration, BCrypt, sessions, CSRF, ownership isolation |
| SL-011–013 | Portfolio schema, create/list/read/update and boundary/isolation tests |
| SL-014–017 | Persisted plans, server sizing, capital checks, transitions and tests |
| SL-018–021 | Manual purchase/full sale, charges, net P&L, journal and transaction tests |
| SL-022 | Budget, committed/available capital, realised P&L, closed count, wins/losses |
| SL-023–026 | React accounts, portfolios, calculator, planning, journal and dashboard |
| SL-027 | Representative tests and PostgreSQL CI job supplied; PostgreSQL and hosted CI execution unverified |
| SL-028 | Local runnable JAR/launcher supplied; public deployment outside local-only request |
| SL-029 | Setup, architecture and demo walkthrough documented |
| SL-030 | Agent browser testing done; independent beta-user walkthrough outstanding |
| SL-031–034 | Post-MVP integrations, imports, AI and licensed data excluded |

53 tests pass on Java 21/H2. React build passes. Browser testing found a calendar
date serialization bug, now fixed with a regression assertion. Account and
portfolio records persist across application restarts; sessions require sign-in again.

Known limits: no password reset/email verification, execution corrections, partial
fills, live valuation, broker integration or tax. See root README for local setup.
