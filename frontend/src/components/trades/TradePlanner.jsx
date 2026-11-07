import React, { useState, useEffect } from "react";
import { Calculator, ArrowRight } from "lucide-react";
import Field from "../common/Field.jsx";
import { api } from "../../api/client.js";
import { money, number } from "../../utils/format.js";

export default function Planner({
  portfolio,
  summary,
  calculateOnly = false,
  onSave,
  busy,
}) {
  const [result, setResult] = useState(null),
    [err, setErr] = useState(""),
    [calculating, setCalculating] = useState(false);
  useEffect(() => {
    setResult(null);
  }, [portfolio?.id]);
  async function submit(e) {
    e.preventDefault();
    setErr("");
    setCalculating(true);
    const data = Object.fromEntries(new FormData(e.currentTarget));
    const input = {
      capital: Number(data.capital),
      riskPercent: Number(data.riskPercent),
      entry: Number(data.entry),
      stopLoss: Number(data.stopLoss),
      target: Number(data.target),
    };
    try {
      const r = await api("/risk/calculate", "POST", input);
      setResult(r);
      if (e.nativeEvent.submitter?.value === "save") {
        if (!portfolio)
          throw new Error("Create a portfolio before saving a plan.");
        await onSave({
          ...input,
          portfolioId: portfolio.id,
          symbol: data.symbol,
          rationale: data.rationale,
        });
      }
    } catch (e) {
      setErr(e.message);
    } finally {
      setCalculating(false);
    }
  }
  return (
    <form
      onSubmit={submit}
      onChange={() => setResult(null)}
      key={portfolio?.id || "standalone"}
    >
      <p className="form-intro">
        {calculateOnly
          ? "Try a scenario with manually entered prices."
          : "Size the position before committing your capital."}
      </p>
      {err && (
        <div className="error" role="alert">
          {err}
        </div>
      )}
      {!calculateOnly && (
        <Field
          label="Exchange symbol"
          name="symbol"
          placeholder="NSE:RELIANCE"
          maxLength={30}
          pattern="[A-Za-z0-9][A-Za-z0-9.:\-]{0,29}"
          required
          autoFocus
        />
      )}
      <div className="form-grid">
        <Field
          label="Available capital (₹)"
          name="capital"
          type="number"
          min="0.01"
          step="0.01"
          defaultValue={summary?.available ?? 15000}
          readOnly={!calculateOnly}
          required
        />
        <Field
          label="Risk per trade (%)"
          name="riskPercent"
          type="number"
          min="0.01"
          max="100"
          step="0.01"
          defaultValue={portfolio?.risk_percent || 1}
          required
        />
        <Field
          label="Entry price (₹)"
          name="entry"
          type="number"
          min="0.01"
          step="0.01"
          defaultValue="200"
          required
        />
        <Field
          label="Stop-loss (₹)"
          name="stopLoss"
          type="number"
          min="0.01"
          step="0.01"
          defaultValue="194"
          required
        />
        <Field
          label="Target price (₹)"
          name="target"
          type="number"
          min="0.01"
          step="0.01"
          defaultValue="224"
          required
        />
      </div>
      {!calculateOnly && (
        <Field label="Your thesis (optional)">
          <textarea
            name="rationale"
            rows={3}
            maxLength={4000}
            placeholder="Why this trade? What would invalidate your idea?"
          />
        </Field>
      )}
      {result && (
        <div className="calculation-result" aria-live="polite">
          <div>
            <small>MAX. QUANTITY</small>
            <strong>
              {number(result.quantity)} <span>shares</span>
            </strong>
          </div>
          <div>
            <small>PLANNED LOSS</small>
            <strong>{money(result.plannedLoss)}</strong>
          </div>
          <div>
            <small>POSITION VALUE</small>
            <strong>{money(result.positionValue)}</strong>
          </div>
          <div>
            <small>REWARD / RISK</small>
            <strong>{result.rewardToRiskRatio}:1</strong>
          </div>
          <div>
            <small>POTENTIAL PROFIT AT TARGET</small>
            <strong>{money(result.potentialProfit)}</strong>
          </div>
          <p>
            Estimated profit if all shares sell at the target price, before fees
            and slippage.
          </p>
          {Number(result.quantity) === 0 && (
            <p>
              Insufficient capital or risk budget for one share. Adjust your
              inputs.
            </p>
          )}
        </div>
      )}
      <div className="form-actions">
        <button
          className={calculateOnly ? "primary" : "secondary"}
          disabled={calculating || busy}
          type="submit"
          value="calculate"
        >
          <Calculator size={16} />
          {calculating ? "Calculating…" : "Calculate position"}
        </button>
        {!calculateOnly && (
          <button
            className="primary"
            type="submit"
            value="save"
            disabled={busy || calculating || !portfolio}
          >
            {busy ? "Saving…" : "Save trade plan"}
            <ArrowRight size={16} />
          </button>
        )}
      </div>
    </form>
  );
}
