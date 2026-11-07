import React from "react";
import { Calculator, ShieldCheck } from "lucide-react";
import Planner from "../components/trades/TradePlanner.jsx";
import { api } from "../api/client.js";

export default function RiskCalculatorPage({
  portfolio,
  summary,
  action,
  busy,
}) {
  return (
    <div className="calculator-layout">
      <div className="card padded">
        <div className="section-heading">
          <h2>Position size calculator</h2>
          <Calculator size={20} />
        </div>
        <Planner
          portfolio={portfolio}
          summary={summary}
          calculateOnly
          onSave={(body) =>
            action(() => api("/trade-plans", "POST", body), "Trade plan saved")
          }
          busy={busy}
        />
      </div>
      <div className="card explain">
        <span className="icon-box">
          <ShieldCheck />
        </span>
        <h2>
          Risk first.
          <br />
          Confidence follows.
        </h2>
        <p>
          Your position is limited by both the amount you are willing to risk
          and the capital available.
        </p>
        <ol>
          <li>Set your entry, stop-loss, and target.</li>
          <li>Choose the percentage you can risk.</li>
          <li>Review your maximum whole-share quantity.</li>
        </ol>
        <div className="note">
          This is a planning tool, not a stock recommendation. Actual losses can
          exceed planned risk due to price gaps, fees, or slippage.
        </div>
      </div>
    </div>
  );
}
