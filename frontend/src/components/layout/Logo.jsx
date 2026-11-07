import React from "react";
import { Activity } from "lucide-react";

export default function Logo() {
  return (
    <div className="brand">
      <span className="brand-icon">
        <Activity size={23} />
      </span>
      Swing<span>Ledger</span>
    </div>
  );
}
