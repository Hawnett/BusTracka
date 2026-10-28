import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { obtenerComprobante } from "../api/ventaService";
import { formatearFecha } from "./BuscarViajes";
import Alerta from "../components/Alerta";
import HoraPanel from "../components/HoraPanel";
import Logo from "../components/Logo";

/** Sprint 4: comprobante con codigo QR y enlace de seguimiento. */
export default function Comprobante() {
  const { id } = useParams();
  const [c, setC] = useState(null);
  const [error, setError] = useState("");
  const [copiado, setCopiado] = useState(false);

  useEffect(() => {
    obtenerComprobante(id).then(setC).catch((e) => setError(e.message));
  }, [id]);

  if (error) return <div className="container py-5"><Alerta tipo="error">{error}</Alerta></div>;
  if (!c) return <div className="container py-5 bt-muted">Cargando…</div>;

  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(c.enlaceSeguimiento);
      setCopiado(true);
    } catch {
      window.prompt("Copia el enlace:", c.enlaceSeguimiento);
    }
  };

  const salida = new Date(c.fechaSalida);
  const fecha = salida.toLocaleDateString("es-PE", { day: "2-digit", month: "short", year: "numeric" });
  const hora = salida.toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit", hour12: false });
  const pasajeros = [...new Set(c.asientos.map((a) => a.nombrePasajero))].join(", ");

  return (
    <div className="container py-4 py-md-5">
      <div className="bt-comprobante-wrap">
        <div className="bt-confirmado">
          <div className="bt-pictograma" aria-hidden="true">
            <i className="bi bi-check-lg"></i>
          </div>
          <div>
            <h1>¡Compra confirmada!</h1>
            <p className="bt-muted mb-0">Presenta este boleto digital al abordar.</p>
          </div>
        </div>

        <article className="bt-comprobante" aria-label={`Boleto ${c.codigoComprobante}`}>
          <header className="bt-comprobante-head">
            <Logo to="/" />
            <span className="bt-code">{c.codigoComprobante}</span>
          </header>

          <div className="bt-comprobante-cuerpo">
            <p className="bt-comprobante-tipo">Boleto de viaje · Interprovincial</p>

            <div className="bt-ruta">
              <div>
                <span className="bt-label mb-1 bt-muted">Origen</span>
                <span className="bt-ciudad">{c.ciudadOrigen}</span>
              </div>
              <div className="bt-ruta-linea" aria-hidden="true">
                <i className="bi bi-bus-front-fill"></i>
              </div>
              <div className="text-end">
                <span className="bt-label mb-1 bt-muted">Destino</span>
                <span className="bt-ciudad">{c.ciudadDestino}</span>
              </div>
            </div>

            <dl className="bt-campos">
              <div><dt>Fecha</dt><dd>{fecha}</dd></div>
              <div><dt>Salida</dt><dd><HoraPanel hora={hora} tamano="sm" /></dd></div>
              <div><dt>Placa</dt><dd><span className="bt-plate">{c.placa}</span></dd></div>
              <div>
                <dt>Asientos</dt>
                <dd className="d-flex flex-wrap gap-1">
                  {c.asientos.map((a) => <span key={a.numero} className="bt-seat-tag">{a.numero}</span>)}
                </dd>
              </div>
              <div className="is-ancho">
                <dt>Pasajero(s)</dt>
                <dd>
                  {pasajeros}
                  <span className="d-block bt-muted fw-normal">
                    Pago {c.codigoPago} · {formatearFecha(c.fechaCompra)} · {c.estado}
                  </span>
                </dd>
              </div>
            </dl>

            <span className="bt-sello">
              Sellado
              <small>BusTracka</small>
            </span>
          </div>

          <div className="bt-perfo bt-talonera">
            <div>
              <span className="bt-label mb-1 d-block">Talonera · Total pagado</span>
              <span className="bt-precio d-block mb-2"><small>S/</small>{Number(c.total).toFixed(2)}</span>
              <span className="small bt-muted">Escanea el código al subir al bus.</span>
            </div>
            <div className="bt-qr" aria-label="Código QR del boleto">
              <img src={`data:image/png;base64,${c.qrBase64}`} alt="Código QR del comprobante" width="150" height="150" />
            </div>
          </div>
        </article>

        <div className="d-flex flex-column flex-sm-row flex-wrap gap-3 mt-4">
          <a className="bt-btn bt-btn-primary flex-fill" href={c.enlaceSeguimiento} target="_blank" rel="noreferrer">
            <i className="bi bi-geo-alt" aria-hidden="true"></i> Seguir mi bus
          </a>
          <button type="button" className="bt-btn bt-btn-ghost flex-fill" onClick={copiar}>
            <i className={`bi ${copiado ? "bi-check2" : "bi-link-45deg"}`} aria-hidden="true"></i>
            {copiado ? "¡Enlace copiado!" : "Copiar enlace de seguimiento"}
          </button>
          <button type="button" className="bt-btn bt-btn-ghost flex-fill" onClick={() => window.print()}>
            <i className="bi bi-printer" aria-hidden="true"></i> Imprimir
          </button>
          <Link to="/compras" className="bt-btn bt-btn-ghost flex-fill">
            <i className="bi bi-ticket-perforated" aria-hidden="true"></i> Mis compras
          </Link>
        </div>
      </div>
    </div>
  );
}
