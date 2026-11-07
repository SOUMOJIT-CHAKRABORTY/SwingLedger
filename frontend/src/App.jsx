import React from "react";
import TradeDialogs from "./components/trades/TradeDialogs.jsx";
import RiskCalculatorPage from "./pages/RiskCalculatorPage.jsx";
import PortfolioSelector from "./components/portfolio/PortfolioSelector.jsx";
import PortfolioPage from "./pages/PortfolioPage.jsx";
import PageHeading from "./components/layout/PageHeading.jsx";
import { useState, useEffect } from "react";
import { Wallet, X, Check, ShieldCheck } from "lucide-react";
import Logo from "./components/layout/Logo.jsx";
import Empty from "./components/common/Empty.jsx";
import Auth from "./pages/AuthPage.jsx";
import Sidebar from "./components/layout/Sidebar.jsx";
import Topbar from "./components/layout/Topbar.jsx";
import TradeTable from "./components/trades/TradeTable.jsx";
import Dashboard from "./components/dashboard/Dashboard.jsx";
import { api, resetCsrfToken } from "./api/client.js";

export default function App() {
  const [user, setUser] = useState();
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState("Overview");
  const [portfolios, setPortfolios] = useState([]);
  const [selected, setSelected] = useState("");
  const [summary, setSummary] = useState(null);
  const [trades, setTrades] = useState([]);
  const [modal, setModal] = useState(null);
  const [error, setError] = useState("");
  const [toast, setToast] = useState("");
  const [busy, setBusy] = useState(false);
  const [filter, setFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const portfolio = portfolios.find((p) => p.id === selected);
  useEffect(() => {
    api("/auth/me")
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
    const expired = () => {
      setUser(null);
      resetCsrfToken();
    };
    window.addEventListener("session-expired", expired);
    return () => window.removeEventListener("session-expired", expired);
  }, []);
  useEffect(() => {
    if (user) reload();
    else {
      setPortfolios([]);
      setTrades([]);
      setSelected("");
      setSummary(null);
    }
  }, [user]);
  useEffect(() => {
    if (selected) loadPortfolio(selected);
  }, [selected]);
  useEffect(() => {
    if (toast) {
      const t = setTimeout(() => setToast(""), 4000);
      return () => clearTimeout(t);
    }
  }, [toast]);
  async function reload() {
    try {
      const list = await api("/portfolios");
      setPortfolios(list);
      const id = list.some((p) => p.id === selected)
        ? selected
        : list[0]?.id || "";
      setSelected(id);
      if (id) await loadPortfolio(id);
    } catch (e) {
      setError(e.message);
    }
  }
  async function loadPortfolio(id) {
    try {
      const [s, t] = await Promise.all([
        api(`/portfolios/${id}/summary`),
        api(`/trade-plans?portfolioId=${id}`),
      ]);
      setSummary(s);
      setTrades(t);
    } catch (e) {
      setError(e.message);
    }
  }
  async function action(fn, message) {
    setBusy(true);
    setError("");
    try {
      await fn();
      await reload();
      setModal(null);
      setToast(message);
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  function show(type, trade) {
    setError("");
    setModal({ type, trade });
  }
  if (loading)
    return (
      <div className="loading">
        <Logo />
        <p>Getting your workspace ready…</p>
      </div>
    );
  if (!user)
    return (
      <Auth
        onLogin={(u) => {
          setUser(u);
          resetCsrfToken();
        }}
      />
    );
  const visible = trades.filter(
    (t) =>
      (filter === "ALL" || t.status === filter) &&
      t.symbol.toLowerCase().includes(search.toLowerCase()),
  );
  const openCount = trades.filter((t) => t.status === "OPEN").length;
  return (
    <div className="app">
      <Sidebar
        user={user}
        page={page}
        setPage={setPage}
        setError={setError}
        setUser={setUser}
      />
      <main>
        <Topbar page={page} />
        <div className="content">
          <PageHeading page={page} portfolio={portfolio} show={show} />
          {error && !modal && (
            <div className="error" role="alert">
              {error}
              <button onClick={() => setError("")} aria-label="Dismiss error">
                <X size={16} />
              </button>
            </div>
          )}
          {page === "Portfolios" ? (
            <PortfolioPage
              portfolios={portfolios}
              show={show}
              setSelected={setSelected}
              setPage={setPage}
            />
          ) : (
            <>
              <PortfolioSelector
                portfolio={portfolio}
                portfolios={portfolios}
                selected={selected}
                setSelected={setSelected}
              />
              {page === "Risk calculator" ? (
                <RiskCalculatorPage
                  portfolio={portfolio}
                  summary={summary}
                  action={action}
                  busy={busy}
                />
              ) : !portfolio ? (
                <Empty
                  icon={Wallet}
                  title="Your first portfolio starts here"
                  text="Set aside a capital budget, choose your risk, and make room for your first plan."
                  action={() => show("portfolio")}
                  label="Create a portfolio"
                />
              ) : (
                <>
                  {page === "Overview" && (
                    <>
                      <Dashboard
                        summary={summary}
                        openCount={openCount}
                        show={show}
                      />
                    </>
                  )}
                  <TradeTable
                    page={page}
                    trades={trades}
                    visible={visible}
                    filter={filter}
                    setFilter={setFilter}
                    search={search}
                    setSearch={setSearch}
                    show={show}
                  />
                  <div className="footer-note">
                    <ShieldCheck size={14} /> Manual records. Clear boundaries.
                    No live prices or broker execution.
                  </div>
                </>
              )}
            </>
          )}
        </div>
      </main>
      {toast && (
        <div className="toast" role="status">
          <Check size={18} />
          {toast}
        </div>
      )}
      {modal && (
        <TradeDialogs
          modal={modal}
          busy={busy}
          setModal={setModal}
          setError={setError}
          error={error}
          action={action}
          portfolio={portfolio}
          summary={summary}
          show={show}
        />
      )}
    </div>
  );
}
