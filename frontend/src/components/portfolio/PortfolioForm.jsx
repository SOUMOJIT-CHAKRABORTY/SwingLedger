import React from "react";
import { ArrowRight } from "lucide-react";
import Field from "../common/Field.jsx";
import { number } from "../../utils/format.js";

export default function PortfolioForm({ value, busy, onSubmit }) {
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        const d = Object.fromEntries(new FormData(e.currentTarget));
        onSubmit({
          ...d,
          capital: Number(d.capital),
          riskPercent: Number(d.riskPercent),
        });
      }}
    >
      <p className="form-intro">
        A budget for a specific strategy. You can adjust it as your plans
        evolve.
      </p>
      <Field
        label="Portfolio name"
        name="name"
        defaultValue={value?.name || ""}
        placeholder="My swing portfolio"
        maxLength={80}
        required
        autoFocus
      />
      <Field label="Strategy">
        <select name="type" defaultValue={value?.type || "SWING"}>
          <option value="SWING">Swing trading</option>
          <option value="LONG_TERM">Long-term investing</option>
        </select>
      </Field>
      <div className="form-grid">
        <Field
          label="Capital budget (₹)"
          name="capital"
          type="number"
          min="0.01"
          max="1000000000000"
          step="0.01"
          defaultValue={value?.capital || 15000}
          required
        />
        <Field
          label="Default risk per trade (%)"
          name="riskPercent"
          type="number"
          min="0.01"
          max="100"
          step="0.01"
          defaultValue={value?.risk_percent || 1}
          required
        />
      </div>
      <div className="note">
        Allocated budget is separate from realised P&L. Open purchases and their
        charges reduce available capital.
      </div>
      <button className="primary full" disabled={busy}>
        {busy ? "Saving…" : "Save portfolio"}
        <ArrowRight size={16} />
      </button>
    </form>
  );
}
