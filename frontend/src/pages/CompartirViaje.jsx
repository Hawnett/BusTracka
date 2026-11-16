import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { generarEnlaceSeguimiento, obtenerComprobante } from "../api/ventaService";
import Alerta from "../components/Alerta";
import EstadoPasaje from "../components/EstadoPasaje";
import { formatearFecha } from "../utils/formato";

/** Compartir el viaje con un enlace temporal que se abre sin iniciar sesion (Release 2, Sprint 3). */
export default function CompartirViaje() {
  const { id } = useParams();
  const [compra, setCompra] = useState(null);
  const [enlace, setEnlace] = useState(null);
  const [error, setError] = useState("");
  const [generando, setGenerando] = useState(false);
  const [copiado, setCopiado] = useState(false);

  useEffect(() => {
    obtenerComprobante(id).then(setCompra).catch((e) => setError(e.message));
  }, [id]);

  const generar = async () => {
    setGenerando(true);
    setError("");
    try {
      setEnlace(await generarEnlaceSeguimiento(id));
    } catch (e) {
      setError(e.message);
    } finally {
      setGenerando(false);
    }
  };

  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(enlace.url);
      setCopiado(true);
    } catch {
      window.prompt("Copia el enlace:", enlace.url);
    }
  };

  if (!compra) {
    return <div className="container py-5">{error ? <Alerta tipo="error">{error}</Alerta> : <p className="bt-muted">Cargando…</p>}</div>;
  }

  const activo = compra.estadoPasaje === "PROXIMO" || compra.estadoPasaje === "EN_CURSO";
  const mensaje = enlace ? `Sigue mi viaje ${compra.ciudadOrigen} → ${compra.ciudadDestino} en vivo: ${enlace.url}` : "";
  const compartirNativo = () => navigator.share({ title: "Mi viaje en BusTracka", text: mensaje }).catch(() => {});

  return (
    <div className="container py-4 py-md-5">
      <Link to={`/compras/${compra.idVenta}`} className="bt-link small d-inline-flex align-items-center gap-1 mb-3">
        <i className="bi bi-arrow-left" aria-hidden="true"></i> Volver al detalle
      </Link>
      <div className="bt-section-head">
        <div>
          <p className="bt-eyebrow mb-0">Seguimiento compartido</p>
          <h1>Compartir mi viaje</h1>
        </div>
      </div>
      <p className="bt-muted mb-4">Genera un enlace temporal para que tu familia vea por dónde va tu bus, sin crear una cuenta.</p>

      <Alerta tipo="error">{error}</Alerta>

      <div className="row g-4">
        <div className="col-12 col-lg-7 d-grid gap-4 align-content-start">
          <section className="bt-ficha" aria-labelledby="titulo-viaje-activo">
            <h2 id="titulo-viaje-activo" className="bt-ficha-head">Tu viaje</h2>
            <dl className="bt-filas">
              <div className="bt-fila"><dt>Ruta</dt><dd>{compra.ciudadOrigen} → {compra.ciudadDestino}</dd></div>
              <div className="bt-fila"><dt>Fecha y hora</dt><dd>{formatearFecha(compra.salidaEstimada)}</dd></div>
              <div className="bt-fila"><dt>Código</dt><dd>{compra.codigoComprobante}</dd></div>
              <div className="bt-fila"><dt>Estado</dt><dd><EstadoPasaje estado={compra.estadoPasaje} /></dd></div>
            </dl>
          </section>

          {enlace && (
            <section className="bt-ficha" aria-labelledby="titulo-enlace">
              <h2 id="titulo-enlace" className="bt-ficha-head">
                <i className="bi bi-check2-square" aria-hidden="true"></i> Enlace generado
              </h2>
              <div className="bt-ficha-body d-grid gap-3">
                <div className="bt-enlace">
                  <input type="text" readOnly value={enlace.url} aria-label="Enlace de seguimiento" onFocus={(e) => e.target.select()} />
                  <button type="button" className="bt-btn bt-btn-primary" onClick={copiar}>
                    <i className={`bi ${copiado ? "bi-check2" : "bi-clipboard"}`} aria-hidden="true"></i>
                    {copiado ? "Copiado" : "Copiar"}
                  </button>
                </div>
                <p className="small mb-0">
                  <span className="bt-tag me-2">Temporal</span>
                  Válido durante el viaje; vence el <strong>{formatearFecha(enlace.expiraEn)}</strong>.
                </p>

                <p className="bt-label mb-0">Compartir por</p>
                <div className="bt-compartir">
                  <a className="bt-btn bt-btn-whatsapp" href={`https://wa.me/?text=${encodeURIComponent(mensaje)}`} target="_blank" rel="noreferrer">
                    <i className="bi bi-whatsapp" aria-hidden="true"></i> WhatsApp
                  </a>
                  <a className="bt-btn bt-btn-ghost" href={`mailto:?subject=${encodeURIComponent("Sigue mi viaje en BusTracka")}&body=${encodeURIComponent(mensaje)}`}>
                    <i className="bi bi-envelope" aria-hidden="true"></i> Correo
                  </a>
                  <a className="bt-btn bt-btn-ghost" href={`sms:?&body=${encodeURIComponent(mensaje)}`}>
                    <i className="bi bi-chat-dots" aria-hidden="true"></i> Mensaje
                  </a>
                  {navigator.share && (
                    <button type="button" className="bt-btn bt-btn-ghost" onClick={compartirNativo}>
                      <i className="bi bi-three-dots" aria-hidden="true"></i> Más…
                    </button>
                  )}
                </div>
                <p className="small bt-muted mb-0">
                  <i className="bi bi-info-circle me-1" aria-hidden="true"></i>
                  Quien reciba el enlace podrá ver el recorrido sin iniciar sesión.
                </p>
              </div>
            </section>
          )}
        </div>

        <div className="col-12 col-lg-5">
          <section className="bt-ficha text-center" aria-labelledby="titulo-generar">
            <h2 id="titulo-generar" className="bt-ficha-head justify-content-center">Generar enlace</h2>
            <div className="bt-ficha-body d-grid gap-3">
              {activo ? (
                <button type="button" className="bt-btn bt-btn-ink" onClick={generar} disabled={generando}>
                  <i className="bi bi-link-45deg" aria-hidden="true"></i>
                  {generando ? "Generando…" : enlace ? "Volver a mostrar enlace" : "Generar enlace de seguimiento"}
                </button>
              ) : (
                <p className="bt-muted mb-0">Solo se puede compartir un viaje que aún no termina.</p>
              )}
              {enlace?.qrBase64 && (
                <div className="mx-auto">
                  <div className="bt-qr bt-qr--lg mx-auto">
                    <img src={`data:image/png;base64,${enlace.qrBase64}`} alt="Código QR del enlace de seguimiento" />
                  </div>
                  <p className="small bt-muted mt-2 mb-0">También puedes mostrar este QR para abrir el enlace.</p>
                </div>
              )}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
