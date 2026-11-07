import React from "react";
import {
  LayoutDashboard,
  Wallet,
  NotebookPen,
  Calculator,
  LogOut,
  ShieldCheck,
} from "lucide-react";
import Logo from "./Logo.jsx";
import { api, resetCsrfToken } from "../../api/client.js";

export default function Sidebar({ user, page, setPage, setError, setUser }) {
  return (
    <aside className="sidebar">
      <Logo />
      <div className="workspace">
        <span className="avatar">{user.name[0].toUpperCase()}</span>
        <div>
          <strong>Personal workspace</strong>
          <small>Your trading, in perspective</small>
        </div>
      </div>
      <div className="nav-label">WORKSPACE</div>
      <nav>
        {[
          ["Overview", LayoutDashboard],
          ["Portfolios", Wallet],
          ["Trade journal", NotebookPen],
          ["Risk calculator", Calculator],
        ].map(([label, Icon]) => (
          <button
            key={label}
            className={page === label ? "active" : ""}
            onClick={() => {
              setPage(label);
              setError("");
            }}
          >
            <Icon size={19} />
            {label}
            {page === label && <span className="nav-dot" />}
          </button>
        ))}
      </nav>
      <div className="sidebar-bottom">
        <div className="discipline">
          <ShieldCheck size={21} />
          <strong>
            A little discipline.
            <br />A clearer picture.
          </strong>
          <p>Plan your risk. Record your decisions. Learn from every trade.</p>
        </div>
        <button
          className="account"
          onClick={async () => {
            try {
              await api("/auth/logout", "POST");
              setUser(null);
              resetCsrfToken();
            } catch (e) {
              setError(e.message);
            }
          }}
        >
          <span className="avatar small">{user.name[0].toUpperCase()}</span>
          <div>
            <strong>{user.name}</strong>
            <small>Sign out</small>
          </div>
          <LogOut size={17} />
        </button>
      </div>
    </aside>
  );
}
