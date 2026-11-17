import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { buscarViajes, listarTerminales } from "../api/viajeService";
import { useAuth } from "../context/AuthContext";
import TarjetaViaje from "../components/TarjetaViaje";

/** Sprint 2: consulta de rutas y horarios disponibles. */
export default function BuscarViajes() {
  const navigate = useNavigate();
  const { usuario } = useAuth();
  const [params] = useSearchParams();
  const [terminales, setTerminales] = useState([]);
  // Desde la pantalla de terminales se llega con ?origen= o ?destino= ya elegidos.
  const [filtros, setFiltros] = useState({ origen: params.get("origen") || "", destino: params.get("destino") || "", fecha: "", pasajeros: "1", franja: "" });
  const [viajes, setViajes] = useState([]);
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  const buscar = async (f) => {
    setCargando(true);
    setError("");
    try {
      setViajes(await buscarViajes(f));
    } catch (e) {
      setError(e.message);
    } finally {
      setCargando(false);
    }
  };

  useEffect(() => {
    listarTerminales().then(setTerminales).catch((e) => setError(e.message));
    buscar(filtros);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const cambiar = (e) => setFiltros({ ...filtros, [e.target.name]: e.target.value });
  const enviar = (e) => {
    e.preventDefault();
    buscar(filtros);
  };
  const intercambiar = () => setFiltros((f) => ({ ...f, origen: f.destino, destino: f.origen }));

  return (
      <>
        <section className="bt-salidas">
          <div className="container">
            <div className="bt-salidas-strip">
              <span>Terminal BusTracka · Panel de salidas</span>
              <span>Hola{usuario?.nombreUsuario ? `, ${usuario.nombreUsuario}` : ""}</span>
            </div>
            <h1 className="mb-3 text-balance">
              ¿A dónde viajas
              <br />
              esta vez?
            </h1>
            <p className="mb-0">Compara horarios, elige tu asiento y sigue tu bus en vivo.</p>
          </div>
        </section>

        <div className="container pb-5">
          <form className="bt-search" onSubmit={enviar} role="search" aria-label="Buscar viajes">
            <div className="row g-2 g-md-3 align-items-end">
              <div className="col-12 col-md">
                <label htmlFor="filtro-origen" className="bt-label">Origen</label>
                <div className="bt-field">
                  <i className="bi bi-record-circle" aria-hidden="true"></i>
                  <select id="filtro-origen" name="origen" className="form-select" value={filtros.origen} onChange={cambiar}>
                    <option value="">Cualquier origen</option>
                    {terminales.map((t) => <option key={t.idTerminal} value={t.idTerminal}>{t.ciudad}</option>)}
                  </select>
                </div>
              </div>

              <div className="col-12 col-md-auto d-flex justify-content-center">
                <button type="button" className="bt-swap" onClick={intercambiar} aria-label="Intercambiar origen y destino">
                  <i className="bi bi-arrow-left-right" aria-hidden="true"></i>
                </button>
              </div>

              <div className="col-12 col-md">
                <label htmlFor="filtro-destino" className="bt-label">Destino</label>
                <div className="bt-field">
                  <i className="bi bi-geo-alt" aria-hidden="true"></i>
                  <select id="filtro-destino" name="destino" className="form-select" value={filtros.destino} onChange={cambiar}>
                    <option value="">Cualquier destino</option>
                    {terminales.map((t) => <option key={t.idTerminal} value={t.idTerminal}>{t.ciudad}</option>)}
                  </select>
                </div>
              </div>

              <div className="col-12 col-md-3">
                <label htmlFor="filtro-fecha" className="bt-label">Fecha de viaje</label>
                <div className="bt-field">
                  <i className="bi bi-calendar3" aria-hidden="true"></i>
                  <input id="filtro-fecha" name="fecha" type="date" className="form-control" value={filtros.fecha} onChange={cambiar} />
                </div>
              </div>

            </div>

            <div className="row g-2 g-md-3 align-items-end mt-1">
              <div className="col-6 col-md-3">
                <label htmlFor="filtro-pasajeros" className="bt-label">Pasajeros</label>
                <div className="bt-field">
                  <i className="bi bi-people" aria-hidden="true"></i>
                  <select id="filtro-pasajeros" name="pasajeros" className="form-select" value={filtros.pasajeros} onChange={cambiar}>
                    {[1, 2, 3, 4, 5, 6].map((n) => <option key={n} value={n}>{n} {n === 1 ? "pasajero" : "pasajeros"}</option>)}
                  </select>
                </div>
              </div>

              <div className="col-6 col-md-3">
                <label htmlFor="filtro-franja" className="bt-label">Horario de salida</label>
                <div className="bt-field">
                  <i className="bi bi-clock" aria-hidden="true"></i>
                  <select id="filtro-franja" name="franja" className="form-select" value={filtros.franja} onChange={cambiar}>
                    <option value="">Cualquier hora</option>
                    <option value="MANANA">Mañana (antes de 12:00)</option>
                    <option value="TARDE">Tarde (12:00 a 17:59)</option>
                    <option value="NOCHE">Noche (desde 18:00)</option>
                  </select>
                </div>
              </div>

              <div className="col-12 col-md-auto ms-md-auto">
                <button type="submit" className="bt-btn bt-btn-primary w-100" style={{ minHeight: 50 }} disabled={cargando}>
                  <i className="bi bi-search" aria-hidden="true"></i>
                  Buscar
                </button>
              </div>
            </div>
          </form>

          <section className="mt-5" aria-labelledby="titulo-resultados">
            <div className="bt-section-head">
              <div>
                <p className="bt-eyebrow mb-0">Resultados</p>
                <h2 id="titulo-resultados">Viajes disponibles</h2>
              </div>
              <span className="bt-tag">
              <i className="bi bi-bus-front" aria-hidden="true"></i>
                {viajes.length} {viajes.length === 1 ? "viaje" : "viajes"}
            </span>
            </div>

            {error && (
                <div className="bt-alert bt-alert--error mb-3" role="alert">
                  <i className="bi bi-exclamation-octagon-fill" aria-hidden="true"></i>
                  <span>{error}</span>
                </div>
            )}

            {!cargando && !error && viajes.length === 0 ? (
                <div className="bt-empty">
                  <div className="bt-pictograma" aria-hidden="true">
                    <i className="bi bi-signpost-split"></i>
                  </div>
                  <h3 className="mb-2">No hay viajes disponibles con esos filtros</h3>
                  <p className="bt-muted mb-0">Prueba con otra fecha o cambia el origen o destino.</p>
                </div>
            ) : (
                <div className="d-grid gap-4">
                  {viajes.map((v) => (
                      <TarjetaViaje key={v.idViaje} viaje={v} onComprar={(viaje) => navigate(`/viajes/${viaje.idViaje}`)} />
                  ))}
                </div>
            )}
          </section>
        </div>
      </>
  );
}