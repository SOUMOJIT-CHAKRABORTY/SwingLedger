import React from "react";

export default function Topbar({ page }) {
  return (
    <header className="topbar">
      <span>
        Workspace <span className="slash">/</span> <strong>{page}</strong>
      </span>
      <div className="local">
        <span /> Local workspace
      </div>
    </header>
  );
}
