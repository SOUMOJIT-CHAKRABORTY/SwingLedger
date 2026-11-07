CREATE TABLE app_users (
 id VARCHAR(36) PRIMARY KEY, email VARCHAR(254) NOT NULL UNIQUE,
 name VARCHAR(80) NOT NULL, password_hash VARCHAR(100) NOT NULL
);
CREATE TABLE portfolios (
 id VARCHAR(36) PRIMARY KEY, owner_id VARCHAR(36) NOT NULL REFERENCES app_users(id),
 name VARCHAR(80) NOT NULL, type VARCHAR(20) NOT NULL CHECK (type IN ('SWING','LONG_TERM')),
 capital DECIMAL(24,8) NOT NULL CHECK (capital > 0), risk_percent DECIMAL(12,8) NOT NULL CHECK (risk_percent > 0 AND risk_percent <= 100)
);
CREATE INDEX portfolios_owner ON portfolios(owner_id);
CREATE TABLE trade_plans (
 id VARCHAR(36) PRIMARY KEY, portfolio_id VARCHAR(36) NOT NULL REFERENCES portfolios(id),
 symbol VARCHAR(30) NOT NULL, entry_price DECIMAL(24,8) NOT NULL,
 stop_loss DECIMAL(24,8) NOT NULL, target_price DECIMAL(24,8) NOT NULL,
 risk_percent DECIMAL(12,8) NOT NULL, quantity BIGINT NOT NULL CHECK (quantity > 0),
 status VARCHAR(12) NOT NULL CHECK (status IN ('PLANNED','OPEN','CLOSED')),
 created_at TIMESTAMP NOT NULL, rationale VARCHAR(4000) NOT NULL,
 buy_price DECIMAL(24,8), buy_quantity BIGINT, buy_date DATE, buy_fees DECIMAL(24,8),
 sell_price DECIMAL(24,8), sell_date DATE, sell_fees DECIMAL(24,8), net_pnl DECIMAL(24,8)
);
CREATE INDEX plans_portfolio ON trade_plans(portfolio_id);
CREATE TABLE journal_entries (
 id VARCHAR(36) PRIMARY KEY, trade_id VARCHAR(36) NOT NULL REFERENCES trade_plans(id),
 author_id VARCHAR(36) NOT NULL REFERENCES app_users(id), note VARCHAR(4000) NOT NULL,
 created_at TIMESTAMP NOT NULL
);
CREATE INDEX journal_trade ON journal_entries(trade_id);
