import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listarCompras } from "../api/ventaService";
import Alerta from "../components/Alerta";
import EstadoPasaje from "../components/EstadoPasaje";
import { formatearFecha, soles } from "../utils/formato";

const FILTROS = [
  { valor: "TODOS", texto: "Todos" },
  { valor: "PROXIMO", texto: "Próximos" },
  { valor: "EN_CURSO", texto: "En curso" },
  { valor: "COMPLETADO", texto: "Completados" },
  { valor: "CANCELADO", texto: "Cancelados" },
];

export default function MisCompras() {
  const [compras, setCompras] = useState(null);
  const [error, setError] = useState("");
  const [filtro, setFiltro] = useState("TODOS");
  const [busqueda, setBusqueda] = useState("");

  useEffect(() => {
    listarCompras().then(setCompras).catch((e) => setError(e.message));
  }, []);

  const todas = compras || [];
  const texto = busqueda.trim().toLowerCase();
  const lista = todas.filter((c) =>
    (filtro === "TODOS" || c.estadoPasaje === filtro)
    && (!texto || `${c.codigoComprobante} ${c.ciudadOrigen} ${c.ciudadDestino}`.toLowerCase().includes(texto)));
  const totalGastado = todas.filter((c) => c.estadoPasaje !== "CANCELADO").reduce((suma, c) => suma + Number(c.total), 0);

  return (
    <div className="container py-4 py-md-5">
      <div className="bt-section-head">
        <div>
          <p className="bt-eyebrow mb-0">Historial</p>
          <h1>Mis compras</h1>
        </div>
        <span className="bt-tag">
          <i className="bi bi-ticket-perforated" aria-hidden="true"></i>
          {todas.length} {todas.length === 1 ? "compra" : "compras"} · {soles(totalGastado)}
        </span>
      </div>

      <Alerta tipo="error">{error}</Alerta>

      {compras && todas.length === 0 && (
        <div className="bt-empty">
          <div className="bt-pictograma" aria-hidden="true">
            <i className="bi bi-ticket-perforated"></i>
          </div>
          <h2 className="mb-2">Aún no tienes compras</h2>
          <p className="bt-muted mb-3">Cuando compres un pasaje, aparecerá aquí.</p>
          <Link to="/" className="bt-btn bt-btn-primary">Buscar viajes</Link>
        </div>
      )}

      {todas.length > 0 && (
        <>
          <div className="d-flex flex-column flex-lg-row justify-content-between gap-3 mb-3">
            <div className="bt-tabs" role="group" aria-label="Filtrar por estado del pasaje">
              {FILTROS.map((f) => (
                <button key={f.valor} type="button" className={`bt-tab ${filtro === f.valor ? "is-active" : ""}`}
                        aria-pressed={filtro === f.valor} onClick={() => setFiltro(f.valor)}>
                  {f.texto}
                </button>
              ))}
            </div>
            <div className="bt-field" style={{ minWidth: 260 }}>
              <i className="bi bi-search" aria-hidden="true"></i>
              <input type="search" className="form-control" placeholder="Buscar por código o ruta"
                     aria-label="Buscar por código o ruta" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
            </div>
          </div>

          {lista.length === 0 ? (
            <div className="bt-empty">
              <h2 className="mb-2">No hay compras con ese filtro</h2>
              <p className="bt-muted mb-0">Prueba con otro estado o borra la búsqueda.</p>
            </div>
          ) : (
            <div className="bt-historial">
              <div className="bt-libreta-head" aria-hidden="true">
                <span>Código</span>
                <span>Ruta</span>
                <span>Salida</span>
                <span>Estado</span>
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
                      {formatearFecha(c.salidaEstimada)} · Asientos {c.asientos.map((a) => a.numero).join(", ")}
                    </span>

                    <EstadoPasaje estado={c.estadoPasaje} />

                    <span className="bt-libreta-total">
                      <span className="visually-hidden">Total: </span>
                      {c.estadoPasaje === "CANCELADO" ? <s>{soles(c.total)}</s> : soles(c.total)}
                    </span>

                    <div className="bt-libreta-accion">
                      <Link to={`/compras/${c.idVenta}`} className="bt-btn bt-btn-ghost bt-btn-sm flex-fill">Detalle</Link>
                      {c.estadoPasaje !== "CANCELADO" && (
                        <Link to={`/comprobante/${c.idVenta}`} className="bt-btn bt-btn-primary bt-btn-sm flex-fill">Boleto</Link>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </>
      )}
    </div>
  );
}
