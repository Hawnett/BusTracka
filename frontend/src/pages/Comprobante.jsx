import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { descargarComprobantePdf, obtenerComprobante } from "../api/ventaService";
import Alerta from "../components/Alerta";
import EstadoPasaje from "../components/EstadoPasaje";
import HoraPanel from "../components/HoraPanel";
import Logo from "../components/Logo";
import { fechaCorta, formatearFecha, horaCorta } from "../utils/formato";

/** Comprobante con codigo QR (el QR lleva a la pagina publica que valida el boleto). */
export default function Comprobante() {
  const { id } = useParams();
  const { state } = useLocation();
  const [c, setC] = useState(null);
  const [error, setError] = useState("");
  const [descargando, setDescargando] = useState(false);

  useEffect(() => {
    obtenerComprobante(id).then(setC).catch((e) => setError(e.message));
  }, [id]);

  if (error && !c) return <div className="container py-5"><Alerta tipo="error">{error}</Alerta></div>;
  if (!c) return <div className="container py-5 bt-muted">Cargando…</div>;

  const descargar = async () => {
    setDescargando(true);
    setError("");
    try {
      await descargarComprobantePdf(c.idVenta, c.codigoComprobante);
    } catch (e) {
      setError(e.message);
    } finally {
      setDescargando(false);
    }
  };

  const pasajeros = [...new Set(c.asientos.map((a) => a.nombrePasajero))].join(", ");
  const recienComprado = state?.recienComprado;

  return (
    <div className="container py-4 py-md-5">
      <div className="bt-comprobante-wrap">
        {recienComprado ? (
          <div className="bt-confirmado">
            <div className="bt-pictograma" aria-hidden="true">
              <i className="bi bi-check-lg"></i>
            </div>
            <div>
              <h1>¡Compra confirmada!</h1>
              <p className="bt-muted mb-0">Te enviamos el boleto a tu correo. Preséntalo al abordar.</p>
            </div>
          </div>
        ) : (
          <div className="bt-section-head">
            <div>
              <p className="bt-eyebrow mb-0">Comprobante digital</p>
              <h1>Tu boleto</h1>
            </div>
            <EstadoPasaje estado={c.estadoPasaje} />
          </div>
        )}

        <Alerta tipo="error">{error}</Alerta>

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
              <div><dt>Fecha</dt><dd>{fechaCorta(c.salidaEstimada)}</dd></div>
              <div>
                <dt>Salida</dt>
                <dd>
                  <HoraPanel hora={horaCorta(c.salidaEstimada)} tamano="sm" />
                  {c.minutosRetraso > 0 && (
                    <span className="d-block small bt-muted fw-normal mt-1">Antes {horaCorta(c.fechaSalida)} · retraso {c.minutosRetraso} min</span>
                  )}
                </dd>
              </div>
              <div><dt>Terminal</dt><dd>{c.terminalOrigen}</dd></div>
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
                    {c.metodoPago} · {formatearFecha(c.fechaCompra)}
                  </span>
                </dd>
              </div>
            </dl>

            {c.estado === "PAGADA" && (
              <span className="bt-sello">
                Sellado
                <small>BusTracka</small>
              </span>
            )}
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
          {c.enlaceSeguimiento && (
            <>
              <a className="bt-btn bt-btn-primary flex-fill" href={c.enlaceSeguimiento} target="_blank" rel="noreferrer">
                <i className="bi bi-geo-alt" aria-hidden="true"></i> Seguir mi bus
              </a>
              <Link to={`/compras/${c.idVenta}/compartir`} className="bt-btn bt-btn-ghost flex-fill">
                <i className="bi bi-share" aria-hidden="true"></i> Compartir viaje
              </Link>
            </>
          )}
          <button type="button" className="bt-btn bt-btn-ghost flex-fill" onClick={descargar} disabled={descargando}>
            <i className="bi bi-file-earmark-pdf" aria-hidden="true"></i> {descargando ? "Descargando…" : "Descargar PDF"}
          </button>
          <button type="button" className="bt-btn bt-btn-ghost flex-fill" onClick={() => window.print()}>
            <i className="bi bi-printer" aria-hidden="true"></i> Imprimir
          </button>
          <Link to={`/compras/${c.idVenta}`} className="bt-btn bt-btn-ghost flex-fill">
            <i className="bi bi-card-list" aria-hidden="true"></i> Detalle del viaje
          </Link>
        </div>
      </div>
    </div>
  );
}
