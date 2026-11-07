import React from "react";
import { ArrowUpRight, NotebookPen, Search } from "lucide-react";
import Empty from "../common/Empty.jsx";
import { money, number } from "../../utils/format.js";

export default function TradeTable({
  page,
  trades,
  visible,
  filter,
  setFilter,
  search,
  setSearch,
  show,
}) {
  return (
    <section className="card trades-card">
      <div className="section-heading table-heading">
        <div>
          <h2>
            {page === "Overview" ? "Your trade activity" : "Trade journal"}{" "}
            <span className="count">{trades.length}</span>
          </h2>
          <p>From first thesis to final reflection.</p>
        </div>
        <div className="search">
          <Search size={16} />
          <input
            aria-label="Search symbol"
            placeholder="Search symbol…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
      </div>
      <div className="tabs">
        {["ALL", "PLANNED", "OPEN", "CLOSED"].map((f) => (
          <button
            className={f === filter ? "selected" : ""}
            onClick={() => setFilter(f)}
            key={f}
          >
            {f === "ALL" ? "All trades" : f[0] + f.slice(1).toLowerCase()}{" "}
            <span>
              {f === "ALL"
                ? trades.length
                : trades.filter((t) => t.status === f).length}
            </span>
          </button>
        ))}
      </div>
      {visible.length ? (
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>SYMBOL / STRATEGY</th>
                <th>STATUS</th>
                <th>QUANTITY</th>
                <th>ENTRY / BUY</th>
                <th>TARGET / EXIT</th>
                <th>NET P&L</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {visible.map((t) => (
                <tr key={t.id}>
                  <td>
                    <div className="symbol">
                      <span>{t.symbol.slice(0, 2)}</span>
                      <div>
                        <strong>{t.symbol}</strong>
                        <small>Long · Cash equity</small>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className={"status " + t.status.toLowerCase()}>
                      <i />
                      {t.status[0] + t.status.slice(1).toLowerCase()}
                    </span>
                  </td>
                  <td>{number(t.buy_quantity || t.quantity)}</td>
                  <td>{money(t.buy_price || t.entry_price)}</td>
                  <td>{money(t.sell_price || t.target_price)}</td>
                  <td className={Number(t.net_pnl) >= 0 ? "gain" : "loss"}>
                    {t.status === "CLOSED" ? (
                      money(t.net_pnl)
                    ) : (
                      <span className="subtle">—</span>
                    )}
                  </td>
                  <td>
                    <button
                      className="row-action"
                      onClick={() => show("detail", t)}
                    >
                      View <ArrowUpRight size={15} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <Empty
          icon={NotebookPen}
          title={
            trades.length
              ? "No matching trades"
              : "Your next decision belongs here"
          }
          text={
            trades.length
              ? "Try another symbol or status."
              : "Start with a plan. Record the why, not just the price."
          }
          action={trades.length ? null : () => show("plan")}
          label="Plan your first trade"
        />
      )}
    </section>
  );
}
