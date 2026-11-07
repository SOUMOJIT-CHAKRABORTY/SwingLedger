import React from "react";

export default function Stat({ label, value, icon: Icon, foot, positive }) {
  return (
    <article className="card stat">
      <div>
        <span>{label}</span>
        <Icon size={18} />
      </div>
      <strong
        className={
          positive === true ? "gain" : positive === false ? "loss" : ""
        }
      >
        {value}
      </strong>
      <small>{foot}</small>
    </article>
  );
}
