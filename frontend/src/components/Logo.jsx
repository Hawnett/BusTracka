import { Link } from "react-router-dom";

export default function Logo({ to = "/", className = "" }) {
  return (
    <Link to={to} className={`bt-logo ${className}`} aria-label="BusTracka, inicio">
      <span className="bt-logo-mark" aria-hidden="true">
        <i className="bi bi-bus-front-fill"></i>
      </span>
      <span aria-hidden="true">BusTracka</span>
    </Link>
  );
}
