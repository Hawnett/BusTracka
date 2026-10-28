import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { buscarViajes, listarTerminales } from "../api/viajeService";

export const formatearFecha = (iso) =>
  new Date(iso).toLocaleString("es-PE", { dateStyle: "medium", timeStyle: "short" });

/** Sprint 2: consulta de rutas y horarios disponibles. */
export default function BuscarViajes() {
  const navigate = useNavigate();
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

  return (
    <>
      <h1 className="h4 mb-3">Buscar viajes</h1>
      <form className="row g-2 mb-4" onSubmit={enviar}>
        <div className="col-md-3">
          <select className="form-select" name="origen" value={filtros.origen} onChange={cambiar}>
            <option value="">Cualquier origen</option>
            {terminales.map((t) => <option key={t.idTerminal} value={t.idTerminal}>{t.ciudad}</option>)}
          </select>
        </div>
        <div className="col-md-3">
          <select className="form-select" name="destino" value={filtros.destino} onChange={cambiar}>
            <option value="">Cualquier destino</option>
            {terminales.map((t) => <option key={t.idTerminal} value={t.idTerminal}>{t.ciudad}</option>)}
          </select>
        </div>
        <div className="col-md-3">
          <input type="date" className="form-control" name="fecha" value={filtros.fecha} onChange={cambiar} />
        </div>
        <div className="col-md-3 d-grid">
          <button className="btn btn-primary" disabled={cargando}>Buscar</button>
        </div>
      </form>

      {error && <div className="alert alert-danger">{error}</div>}
      {!cargando && viajes.length === 0 && !error && (
        <div className="alert alert-info">No hay viajes disponibles con esos filtros.</div>
      )}

      <div className="row g-3">
        {viajes.map((v) => (
          <div className="col-md-6" key={v.idViaje}>
            <div className="card h-100">
              <div className="card-body">
                <h2 className="h5">{v.ciudadOrigen} → {v.ciudadDestino}</h2>
                <p className="mb-1">Sale: {formatearFecha(v.fechaSalida)}</p>
                <p className="mb-1 text-muted">
                  Duracion {Math.floor(v.duracionMin / 60)} h {v.duracionMin % 60} min · Bus {v.placa}
                </p>
                <p className="mb-3">
                  <strong>S/ {Number(v.precio).toFixed(2)}</strong>{" "}
                  <span className={"badge " + (v.asientosDisponibles > 0 ? "bg-success" : "bg-danger")}>
                    {v.asientosDisponibles} asientos libres
                  </span>
                </p>
                <button className="btn btn-outline-primary btn-sm" disabled={v.asientosDisponibles === 0}
                        onClick={() => navigate(`/viajes/${v.idViaje}`)}>
                  Elegir asientos
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}
