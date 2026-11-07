import React from "react";
import Modal from "../common/Modal.jsx";
import PortfolioForm from "../portfolio/PortfolioForm.jsx";
import Planner from "./TradePlanner.jsx";
import ExecutionForm from "./ExecutionForm.jsx";
import TradeDetail from "./TradeDetail.jsx";
import { api } from "../../api/client.js";

export default function TradeDialogs({
  modal,
  busy,
  setModal,
  setError,
  error,
  action,
  portfolio,
  summary,
  show,
}) {
  return (
    <Modal
      title={
        {
          portfolio: modal.trade ? "Edit portfolio" : "Create a portfolio",
          plan: "Plan your next trade",
          detail: modal.trade?.symbol,
          buy: "Record your purchase",
          sell: "Close your position",
        }[modal.type]
      }
      onClose={() => {
        if (!busy) {
          setModal(null);
          setError("");
        }
      }}
    >
      {error && (
        <div className="error" role="alert">
          {error}
        </div>
      )}
      {modal.type === "portfolio" && (
        <PortfolioForm
          value={modal.trade}
          busy={busy}
          onSubmit={(body) =>
            action(
              () =>
                api(
                  "/portfolios" + (modal.trade ? "/" + modal.trade.id : ""),
                  modal.trade ? "PATCH" : "POST",
                  body,
                ),
              "Portfolio saved",
            )
          }
        />
      )}{" "}
      {modal.type === "plan" && (
        <Planner
          portfolio={portfolio}
          summary={summary}
          busy={busy}
          onSave={(body) =>
            action(() => api("/trade-plans", "POST", body), "Trade plan saved")
          }
        />
      )}{" "}
      {modal.type === "detail" && (
        <TradeDetail
          trade={modal.trade}
          onBuy={() => show("buy", modal.trade)}
          onSell={() => show("sell", modal.trade)}
          onError={setError}
        />
      )}{" "}
      {(modal.type === "buy" || modal.type === "sell") && (
        <ExecutionForm
          trade={modal.trade}
          selling={modal.type === "sell"}
          busy={busy}
          onSubmit={(body) =>
            action(
              () =>
                modal.type === "buy"
                  ? api("/trades", "POST", {
                      ...body,
                      planId: modal.trade.id,
                    })
                  : api(`/trades/${modal.trade.id}/close`, "PATCH", body),
              modal.type === "buy" ? "Purchase recorded" : "Trade closed",
            )
          }
        />
      )}
    </Modal>
  );
}
