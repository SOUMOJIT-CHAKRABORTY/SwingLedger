import React from "react";
import { Wallet, Plus, ArrowRight } from "lucide-react";
import { money } from "../utils/format.js";

export default function PortfolioPage({
  portfolios,
  show,
  setSelected,
  setPage,
}) {
  return (
    <>
      <div className="section-heading">
        <h2>
          Your portfolios <span className="count">{portfolios.length}</span>
        </h2>
        <span>Built around your strategy</span>
      </div>
      <div className="portfolio-grid">
        {portfolios.map((p) => (
          <article className="card portfolio-card" key={p.id}>
            <span className="icon-box">
              <Wallet size={23} />
            </span>
            <span className="pill">
              {p.type === "SWING" ? "Swing trading" : "Long term"}
            </span>
            <h2>{p.name}</h2>
            <small>Allocated capital</small>
            <h3>{money(p.capital)}</h3>
            <div className="portfolio-bottom">
              <span>{p.risk_percent}% default risk</span>
              <button onClick={() => show("portfolio", p)}>
                Edit portfolio
              </button>
            </div>
            <button
              className="text-button"
              onClick={() => {
                setSelected(p.id);
                setPage("Overview");
              }}
            >
              View workspace <ArrowRight size={16} />
            </button>
          </article>
        ))}
        <button className="new-portfolio" onClick={() => show("portfolio")}>
          <Plus size={28} />
          <strong>Create a portfolio</strong>
          <span>A dedicated space for your next strategy</span>
        </button>
      </div>
    </>
  );
}
