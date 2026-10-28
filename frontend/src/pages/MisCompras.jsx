import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listarCompras } from "../api/ventaService";
import { formatearFecha } from "./BuscarViajes";

export default function MisCompras() {
  const [compras, setCompras] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    listarCompras().then(setCompras).catch((e) => setError(e.message));
  }, []);

  return (
    <>
      <h1 className="h4 mb-3">Mis compras</h1>
      {error && <div className="alert alert-danger">{error}</div>}
      {compras && compras.length === 0 && <div className="alert alert-info">Aun no has comprado pasajes.</div>}
      <div className="list-group">
        {(compras || []).map((c) => (
          <Link key={c.idVenta} to={`/comprobante/${c.idVenta}`} className="list-group-item list-group-item-action">
            <div className="d-flex justify-content-between">
              <strong>{c.ciudadOrigen} → {c.ciudadDestino}</strong>
              <span>S/ {Number(c.total).toFixed(2)}</span>
            </div>
            <small className="text-muted">
              {c.codigoComprobante} · Sale {formatearFecha(c.fechaSalida)} · Asientos {c.asientos.map((a) => a.numero).join(", ")}
            </small>
          </Link>
        ))}
      </div>
    </>
  );
}
