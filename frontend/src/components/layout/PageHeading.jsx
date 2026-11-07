import React from "react";
import { Plus } from "lucide-react";

export default function PageHeading({ page, portfolio, show }) {
  return (
    <div className="page-heading">
      <div>
        <div className="eyebrow">YOUR TRADING WORKSPACE</div>
        <h1>
          {page === "Overview"
            ? "A clearer view of your capital."
            : page === "Trade journal"
              ? "Every trade tells a story."
              : page === "Portfolios"
                ? "Give your capital a purpose."
                : "Define the risk. Then the trade."}
        </h1>
        <p>
          {page === "Overview"
            ? "Plan thoughtfully. Track honestly. Build better trading habits."
            : page === "Trade journal"
              ? "Keep your plans, executions, and lessons in one place."
              : page === "Portfolios"
                ? "Separate your strategies with intentional capital allocation."
                : "Position sizing that respects your capital and your boundaries."}
        </p>
      </div>
      <button
        className="primary"
        onClick={() =>
          show(page === "Portfolios" || !portfolio ? "portfolio" : "plan")
        }
      >
        <Plus size={17} />
        {page === "Portfolios" || !portfolio ? "New portfolio" : "Plan a trade"}
      </button>
    </div>
  );
}
