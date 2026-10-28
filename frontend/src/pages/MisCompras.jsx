import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listarCompras } from "../api/ventaService";
import { formatearFecha } from "./BuscarViajes";
import Alerta from "../components/Alerta";

export default function MisCompras() {
  const [compras, setCompras] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    listarCompras().then(setCompras).catch((e) => setError(e.message));
  }, []);

  const lista = compras || [];

  return (
    <div className="container py-4 py-md-5">
      <div className="bt-section-head">
        <div>
          <p className="bt-eyebrow mb-0">Historial</p>
          <h1>Mis compras</h1>
        </div>
        <span className="bt-tag">
          <i className="bi bi-ticket-perforated" aria-hidden="true"></i>
          {lista.length} {lista.length === 1 ? "compra" : "compras"}
        </span>
      </div>

      <Alerta tipo="error">{error}</Alerta>

      {compras && lista.length === 0 && (
        <div className="bt-empty">
          <div className="bt-pictograma" aria-hidden="true">
            <i className="bi bi-ticket-perforated"></i>
          </div>
          <h2 className="mb-2">Aún no tienes compras</h2>
          <p className="bt-muted mb-3">Cuando compres un pasaje, aparecerá aquí.</p>
          <Link to="/" className="bt-btn bt-btn-primary">Buscar viajes</Link>
        </div>
      )}

      {lista.length > 0 && (
        <>
          <div className="bt-libreta-head" aria-hidden="true">
            <span>Código</span>
            <span>Ruta</span>
            <span>Salida</span>
            <span>Total</span>
            <span></span>
          </div>
          <ul className="bt-libreta">
            {lista.map((c) => (
              <li key={c.idVenta} className="bt-libreta-row">
                <span className="bt-code">{c.codigoComprobante}</span>

                <p className="bt-libreta-ruta mb-0">
                  {c.ciudadOrigen}
                  <i className="bi bi-arrow-right" aria-label="a"></i>
                  {c.ciudadDestino}
                </p>

                <span className="bt-libreta-fecha">
                  <span className="visually-hidden">Salida: </span>
                  {formatearFecha(c.fechaSalida)} · Asientos {c.asientos.map((a) => a.numero).join(", ")}
                </span>

                <span className="bt-libreta-total">
                  <span className="visually-hidden">Total: </span>
                  S/ {Number(c.total).toFixed(2)}
                </span>

                <div className="bt-libreta-accion">
                  <Link to={`/comprobante/${c.idVenta}`} className="bt-btn bt-btn-ghost bt-btn-sm">
                    Ver boleto <i className="bi bi-chevron-right" aria-hidden="true"></i>
                  </Link>
                </div>
              </li>
            ))}
          </ul>
        </>
      )}
    </div>
  );
}
