import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { buscarViajes, listarTerminales } from "../api/viajeService";
import { useAuth } from "../context/AuthContext";
import TarjetaViaje from "../components/TarjetaViaje";

export const formatearFecha = (iso) =>
  new Date(iso).toLocaleString("es-PE", { dateStyle: "medium", timeStyle: "short" });

/** Sprint 2: consulta de rutas y horarios disponibles. */
export default function BuscarViajes() {
  const navigate = useNavigate();
  const { usuario } = useAuth();
  const [terminales, setTerminales] = useState([]);
  const [filtros, setFiltros] = useState({ origen: "", destino: "", fecha: "" });
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

            <div className="col-12 col-md-auto">
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
