# Frontend code guide

This project uses JavaScript and JSX. The entry point is `src/main.jsx`.

## Where to find code

| Change or bug | Start here |
| --- | --- |
| Mounting React / global stylesheet | `src/main.jsx` |
| Current page, selected portfolio, shared state, refreshing data | `src/App.jsx` |
| Login and registration forms | `src/pages/AuthPage.jsx` |
| Portfolio listing | `src/pages/PortfolioPage.jsx` |
| Standalone risk calculator screen | `src/pages/RiskCalculatorPage.jsx` |
| Navigation and page heading | `src/components/layout/` |
| Dashboard totals and capital chart | `src/components/dashboard/` |
| Portfolio form and selector | `src/components/portfolio/` |
| Sizing form | `src/components/trades/TradePlanner.jsx` |
| Purchase and sale forms | `src/components/trades/ExecutionForm.jsx` |
| Trade details and journal notes | `src/components/trades/TradeDetail.jsx` |
| Trade list, search and status filters | `src/components/trades/TradeTable.jsx` |
| Selecting and connecting trade dialogs | `src/components/trades/TradeDialogs.jsx` |
| Shared fields, empty states, modal shell | `src/components/common/` |
| HTTP requests, CSRF and request errors | `src/api/client.js` |
| Currency, number and date formatting | `src/utils/format.js` |
| Appearance and responsive rules | `src/style.css` |

## How data flows

`App` owns workspace state and passes values and callbacks to components.
Forms collect user input and call a callback when the parent owns the mutation.
Components with their own isolated interactions (authentication, journal notes,
calculator preview) call the shared API client directly.

For a wrong displayed value, inspect the component's props first, then the API
response in your browser's Network panel, then the backend calculation. For a
failed save, inspect the request payload and response before changing UI state.
CSRF token state is private to the API client; use `resetCsrfToken()` when the
account/session changes.

## Commands

- `pnpm dev`: start the UI development server (Java must run on port 8080).
- `pnpm build`: compile UI assets into Spring's static resource directory.
- `pnpm format`: format source files with Prettier.
- `pnpm format:check`: check formatting without changing files.

After rebuilding the UI, rebuild the Java JAR to include the updated assets.
Run `build-local.ps1` from the project root to build both parts together.
