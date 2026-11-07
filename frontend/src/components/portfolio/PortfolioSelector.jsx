import React from "react";
import { Wallet } from "lucide-react";

export default function PortfolioSelector({
  portfolio,
  portfolios,
  selected,
  setSelected,
}) {
  return (
    <div className="portfolio-switch">
      <div className="select-wrap">
        <Wallet size={18} />
        <select
          aria-label="Active portfolio"
          value={selected}
          onChange={(e) => setSelected(e.target.value)}
        >
          {portfolios.length === 0 && (
            <option value="">No portfolios yet</option>
          )}
          {portfolios.map((p) => (
            <option key={p.id} value={p.id}>
              {p.name}
            </option>
          ))}
        </select>
      </div>
      <span className="subtle">
        {portfolio?.type === "LONG_TERM"
          ? "Long-term investing"
          : "Swing trading"}{" "}
        <span>•</span> INR
      </span>
    </div>
  );
}
