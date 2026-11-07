import React, { useState, useEffect } from "react";
import { Plus, ArrowRight } from "lucide-react";
import Field from "../common/Field.jsx";
import { api } from "../../api/client.js";
import { money, number } from "../../utils/format.js";

export default function TradeDetail({ trade, onBuy, onSell, onError }) {
  const [notes, setNotes] = useState([]),
    [busy, setBusy] = useState(false);
  useEffect(() => {
    api(`/trades/${trade.id}/journal`)
      .then(setNotes)
      .catch((e) => onError(e.message));
  }, [trade.id]);
  return (
    <>
      <div className="detail-status">
        <span className={"status " + trade.status.toLowerCase()}>
          <i />
          {trade.status}
        </span>
        <span>Long-only · Cash equity</span>
      </div>
      <div className="detail-grid">
        {[
          ["Planned entry", money(trade.entry_price)],
          ["Stop-loss", money(trade.stop_loss)],
          ["Target", money(trade.target_price)],
          ["Planned shares", number(trade.quantity)],
          ...(trade.status !== "PLANNED"
            ? [
                ["Buy price", money(trade.buy_price)],
                ["Shares purchased", number(trade.buy_quantity)],
                ["Buy date", String(trade.buy_date).slice(0, 10)],
                ["Buy charges", money(trade.buy_fees)],
              ]
            : []),
          ...(trade.status === "CLOSED"
            ? [
                ["Sell price", money(trade.sell_price)],
                ["Sell date", String(trade.sell_date).slice(0, 10)],
                ["Sell charges", money(trade.sell_fees)],
                ["Realised net P&L", money(trade.net_pnl)],
              ]
            : []),
        ].map(([label, val]) => (
          <div key={label}>
            <small>{label}</small>
            <strong>{val}</strong>
          </div>
        ))}
      </div>
      {trade.rationale && (
        <div className="thesis">
          <small>ORIGINAL THESIS</small>
          <p>{trade.rationale}</p>
        </div>
      )}
      {trade.status === "PLANNED" && (
        <button className="primary full" onClick={onBuy}>
          Record purchase <ArrowRight size={17} />
        </button>
      )}
      {trade.status === "OPEN" && (
        <button className="primary full" onClick={onSell}>
          Record full sale <ArrowRight size={17} />
        </button>
      )}
      <div className="journal">
        <h3>Journal & reflections</h3>
        {notes.length ? (
          notes.map((n) => (
            <article key={n.id}>
              <small>{new Date(n.created_at).toLocaleString()}</small>
              <p>{n.note}</p>
            </article>
          ))
        ) : (
          <p className="subtle">No notes yet. Capture what you are seeing.</p>
        )}
        <form
          onSubmit={async (e) => {
            e.preventDefault();
            const form = e.currentTarget;
            setBusy(true);
            try {
              await api(`/trades/${trade.id}/journal`, "POST", {
                note: new FormData(form).get("note"),
              });
              setNotes(await api(`/trades/${trade.id}/journal`));
              form.reset();
            } catch (e) {
              onError(e.message);
            } finally {
              setBusy(false);
            }
          }}
        >
          <Field label="Add a reflection">
            <textarea
              name="note"
              placeholder="What worked? What would you do differently?"
              rows={3}
              maxLength={4000}
              required
            />
          </Field>
          <button className="secondary" disabled={busy}>
            {busy ? "Saving…" : "Add note"}
            <Plus size={15} />
          </button>
        </form>
      </div>
    </>
  );
}
