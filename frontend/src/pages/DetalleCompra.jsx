import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { descargarComprobantePdf, obtenerComprobante } from "../api/ventaService";
import Alerta from "../components/Alerta";
import EstadoPasaje from "../components/EstadoPasaje";
import { formatearFecha, soles } from "../utils/formato";

/** Detalle de un viaje comprado: datos del recorrido y de la compra (Release 2, Sprint 2). */
export default function DetalleCompra() {
  const { id } = useParams();
  const [c, setC] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    obtenerComprobante(id).then(setC).catch((e) => setError(e.message));
  }, [id]);

  if (error && !c) return <div className="container py-5"><Alerta tipo="error">{error}</Alerta></div>;
  if (!c) return <div className="container py-5 bt-muted">Cargando…</div>;

  const descargar = () => descargarComprobantePdf(c.idVenta, c.codigoComprobante).catch((e) => setError(e.message));
  const pasajeros = [...new Set(c.asientos.map((a) => a.nombrePasajero))].join(", ");

  return (
    <div className="container py-4 py-md-5">
      <Link to="/compras" className="bt-link small d-inline-flex align-items-center gap-1 mb-3">
        <i className="bi bi-arrow-left" aria-hidden="true"></i> Volver al historial
      </Link>
      <div className="bt-section-head">
        <div>
          <p className="bt-eyebrow mb-0">Compra {c.codigoComprobante}</p>
          <h1>Detalle de mi viaje</h1>
        </div>
        <EstadoPasaje estado={c.estadoPasaje} />
      </div>

      <Alerta tipo="error">{error}</Alerta>

      <div className="row g-4">
        <div className="col-12 col-lg-6">
          <section className="bt-ficha h-100" aria-labelledby="titulo-datos-viaje">
            <h2 id="titulo-datos-viaje" className="bt-ficha-head">Datos del viaje</h2>
            <dl className="bt-filas">
              <div className="bt-fila"><dt>Ruta</dt><dd>{c.ciudadOrigen} → {c.ciudadDestino}</dd></div>
              <div className="bt-fila"><dt>Salida</dt><dd>{formatearFecha(c.salidaEstimada)}</dd></div>
              {c.minutosRetraso > 0 && (
                <div className="bt-fila">
                  <dt>Horario original</dt>
                  <dd><s>{formatearFecha(c.fechaSalida)}</s> · retraso {c.minutosRetraso} min</dd>
                </div>
              )}
              <div className="bt-fila"><dt>Llegada estimada</dt><dd>{formatearFecha(c.llegadaEstimada)}</dd></div>
              <div className="bt-fila"><dt>Terminal de salida</dt><dd>{c.terminalOrigen}</dd></div>
              <div className="bt-fila"><dt>Terminal de llegada</dt><dd>{c.terminalDestino}</dd></div>
              <div className="bt-fila"><dt>Bus</dt><dd><span className="bt-plate">{c.placa}</span></dd></div>
              <div className="bt-fila">
                <dt>Asientos</dt>
                <dd>{c.asientos.map((a) => a.numero).join(", ")} ({c.asientos.length} {c.asientos.length === 1 ? "pasajero" : "pasajeros"})</dd>
              </div>
            </dl>
          </section>
        </div>

        <div className="col-12 col-lg-6">
          <section className="bt-ficha h-100" aria-labelledby="titulo-datos-compra">
            <h2 id="titulo-datos-compra" className="bt-ficha-head">Datos de la compra</h2>
            <dl className="bt-filas">
              <div className="bt-fila"><dt>Código de operación</dt><dd>{c.codigoComprobante}</dd></div>
              <div className="bt-fila"><dt>Fecha de compra</dt><dd>{formatearFecha(c.fechaCompra)}</dd></div>
              <div className="bt-fila"><dt>Método de pago</dt><dd>{c.metodoPago}</dd></div>
              <div className="bt-fila"><dt>Comprador</dt><dd>{c.comprador}</dd></div>
              <div className="bt-fila"><dt>Pasajero(s)</dt><dd>{pasajeros}</dd></div>
              <div className="bt-fila">
                <dt>Total pagado</dt>
                <dd className="fs-5">{c.estadoPasaje === "CANCELADO" ? <s>{soles(c.total)}</s> : soles(c.total)}</dd>
              </div>
            </dl>
          </section>
        </div>
      </div>

      <div className="d-flex flex-column flex-sm-row flex-wrap gap-3 mt-4">
        {c.estadoPasaje !== "CANCELADO" && (
          <>
            <Link to={`/comprobante/${c.idVenta}`} className="bt-btn bt-btn-primary">
              <i className="bi bi-qr-code" aria-hidden="true"></i> Ver comprobante
            </Link>
            <button type="button" className="bt-btn bt-btn-ghost" onClick={descargar}>
              <i className="bi bi-file-earmark-pdf" aria-hidden="true"></i> Descargar PDF
            </button>
          </>
        )}
        {c.estadoPasaje === "EN_CURSO" && (
          <Link to={`/seguimiento?viaje=${c.idViaje}`} className="bt-btn bt-btn-ghost">
            <i className="bi bi-geo-alt" aria-hidden="true"></i> Seguir bus
          </Link>
        )}
        {(c.estadoPasaje === "PROXIMO" || c.estadoPasaje === "EN_CURSO") && (
          <Link to={`/compras/${c.idVenta}/compartir`} className="bt-btn bt-btn-ghost">
            <i className="bi bi-share" aria-hidden="true"></i> Compartir viaje
          </Link>
        )}
      </div>
    </div>
  );
}
