import React, { useState } from "react";
import { ArrowUpRight, ArrowRight, ShieldCheck } from "lucide-react";
import Field from "../components/common/Field.jsx";
import Logo from "../components/layout/Logo.jsx";
import { api } from "../api/client.js";

export default function Auth({ onLogin }) {
  const [register, setRegister] = useState(false),
    [error, setError] = useState(""),
    [busy, setBusy] = useState(false);
  async function submit(e) {
    e.preventDefault();
    setBusy(true);
    setError("");
    const data = Object.fromEntries(new FormData(e.currentTarget));
    try {
      onLogin(
        await api("/auth/" + (register ? "register" : "login"), "POST", data),
      );
    } catch (e) {
      setError(e.message);
    } finally {
      setBusy(false);
    }
  }
  return (
    <div className="auth">
      <div className="auth-story">
        <Logo />
        <div>
          <span className="eyebrow">LESS NOISE. MORE INTENTION.</span>
          <h1>
            Your capital.
            <br />
            Your decisions.
            <br />
            <em>A clearer picture.</em>
          </h1>
          <p>
            A thoughtful home for your trading plans,
            <br />
            your progress, and everything you learn along the way.
          </p>
          <div className="auth-illustration">
            <div className="illustration-label">
              <span>THE POWER OF A PLAN</span>
              <ArrowUpRight />
            </div>
            <div className="chart-bars">
              {[30, 45, 36, 59, 53, 76, 68, 90, 82, 110, 120].map((h, i) => (
                <i style={{ height: h }} key={i} />
              ))}
            </div>
            <div className="illustration-bottom">
              <ShieldCheck size={16} /> Plan risk. Record decisions. Reflect.
            </div>
          </div>
        </div>
        <small>BUILT FOR DISCIPLINED, INDEPENDENT TRADERS.</small>
      </div>
      <div className="auth-form">
        <div>
          <span className="pill">YOUR PERSONAL TRADING JOURNAL</span>
          <h2>{register ? "Start with intention." : "Welcome back."}</h2>
          <p>
            {register
              ? "Create your private local workspace."
              : "Sign in to pick up where you left off."}
          </p>
          {error && (
            <div className="error" role="alert">
              {error}
            </div>
          )}
          <form onSubmit={submit}>
            {register && (
              <Field
                label="Your name"
                name="name"
                autoComplete="name"
                maxLength={80}
                required
              />
            )}
            <Field
              label="Email address"
              name="email"
              type="email"
              autoComplete="email"
              placeholder="you@example.com"
              required
            />
            <Field
              label="Password"
              name="password"
              type="password"
              autoComplete={register ? "new-password" : "current-password"}
              minLength={register ? 10 : 1}
              maxLength={72}
              placeholder={
                register ? "At least 10 characters" : "Enter your password"
              }
              required
            />
            <button className="primary full" disabled={busy}>
              {busy
                ? "Please wait…"
                : register
                  ? "Create workspace"
                  : "Sign in"}
              <ArrowRight size={18} />
            </button>
          </form>
          <p className="auth-toggle">
            {register ? "Already have an account?" : "New to SwingLedger?"}{" "}
            <button
              onClick={() => {
                setRegister(!register);
                setError("");
              }}
            >
              {register ? "Sign in" : "Create an account"}
            </button>
          </p>
          <div className="auth-privacy">
            <ShieldCheck size={16} /> Saved on this machine. No broker
            connection.
          </div>
        </div>
      </div>
    </div>
  );
}
