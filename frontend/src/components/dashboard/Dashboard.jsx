import React from "react";
import {
  ArrowUpRight,
  Wallet,
  Check,
  TrendingUp,
  ShieldCheck,
  Layers,
} from "lucide-react";
import Stat from "./Stat.jsx";
import { money, number } from "../../utils/format.js";

export default function Dashboard({ summary, openCount, show }) {
  return (
    <>
      <div className="stats">
        <Stat
          label="Allocated capital"
          value={money(summary?.budget)}
          icon={Wallet}
          foot="Your portfolio budget"
        />
        <Stat
          label="Available to deploy"
          value={money(summary?.available)}
          icon={Layers}
          foot={`${money(summary?.committed)} committed to open trades`}
        />
        <Stat
          label="Realised net P&L"
          value={money(summary?.realisedPnl)}
          icon={TrendingUp}
          positive={Number(summary?.realisedPnl) >= 0}
          foot="After recorded buy & sell charges"
        />
        <Stat
          label="Closed trades"
          value={number(summary?.closedCount)}
          icon={Check}
          foot={`${summary?.wins || 0} wins · ${summary?.losses || 0} losses`}
        />
      </div>
      <div className="overview-grid">
        <div className="card capital-card">
          <div className="section-heading">
            <h2>Capital at work</h2>
            <span className="pill">At purchase cost</span>
          </div>
          <div className="capital-inner">
            <div
              className="donut"
              style={{
                "--used": `${Math.min(100, (Number(summary?.committed) / Number(summary?.budget || 1)) * 100)}%`,
              }}
            >
              <div>
                <small>AVAILABLE</small>
                <strong>
                  {Math.round(
                    (Number(summary?.available || 0) /
                      Number(summary?.budget || 1)) *
                      100,
                  )}
                  <span>%</span>
                </strong>
              </div>
            </div>
            <div className="legend">
              <div>
                <span className="dot green" />
                <span>Available capital</span>
                <strong>{money(summary?.available)}</strong>
              </div>
              <div>
                <span className="dot dark" />
                <span>Committed capital</span>
                <strong>{money(summary?.committed)}</strong>
              </div>
              <p>
                {openCount} open {openCount === 1 ? "position" : "positions"} ·
                No live market valuation
              </p>
            </div>
          </div>
        </div>
        <div className="focus-card">
          <span className="focus-label">
            <ShieldCheck size={16} /> THE DISCIPLINE EDGE
          </span>
          <h2>
            A good trade starts
            <br />
            before you buy.
          </h2>
          <p>
            Know your downside. Write your thesis.
            <br />
            Let your plan do the heavy lifting.
          </p>
          <button onClick={() => show("plan")}>
            Build your next plan <ArrowUpRight size={18} />
          </button>
          <div className="focus-decoration" aria-hidden="true">
            ↗
          </div>
        </div>
      </div>
    </>
  );
}
