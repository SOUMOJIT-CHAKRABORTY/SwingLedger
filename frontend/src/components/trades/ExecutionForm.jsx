import React from "react";
import { Check } from "lucide-react";
import Field from "../common/Field.jsx";
import { number, today } from "../../utils/format.js";

export default function ExecutionForm({ trade, selling, busy, onSubmit }) {
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        const d = Object.fromEntries(new FormData(e.currentTarget));
        onSubmit({
          price: Number(d.price),
          date: d.date,
          fees: Number(d.fees),
          ...(!selling ? { quantity: Number(d.quantity) } : {}),
        });
      }}
    >
      <div className="note">
        {selling
          ? `Record one complete sale of all ${trade.buy_quantity} shares of ${trade.symbol}.`
          : `Record an actual purchase of ${trade.symbol}. This does not place a broker order.`}
      </div>
      <div className="form-grid">
        <Field
          label={selling ? "Sell price (₹)" : "Buy price (₹)"}
          name="price"
          type="number"
          min="0.01"
          step="0.01"
          defaultValue={selling ? trade.target_price : trade.entry_price}
          required
          autoFocus
        />
        {!selling && (
          <Field
            label="Quantity"
            name="quantity"
            type="number"
            min="1"
            max={trade.quantity}
            step="1"
            defaultValue={trade.quantity}
            required
          />
        )}
        <Field
          label="Execution date"
          name="date"
          type="date"
          min={selling ? String(trade.buy_date).slice(0, 10) : "1900-01-01"}
          max={today()}
          defaultValue={today()}
          required
        />
        <Field
          label="Total charges (₹)"
          name="fees"
          type="number"
          min="0"
          step="0.01"
          defaultValue="0"
          required
        />
      </div>
      <p className="form-intro">
        Check the details carefully. Execution records cannot be edited or
        repeated.
      </p>
      <button className="primary full" disabled={busy}>
        {busy
          ? "Recording…"
          : selling
            ? "Confirm full close"
            : "Confirm purchase"}
        <Check size={17} />
      </button>
    </form>
  );
}
