import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listarTerminales } from "../api/viajeService";
import Alerta from "../components/Alerta";
import MapaTerminales from "../components/MapaTerminales";

/** Terminales disponibles: ubicacion, direccion y acceso directo a buscar viajes desde/hacia cada una. */
export default function Terminales() {
  const [terminales, setTerminales] = useState([]);
  const [seleccionada, setSeleccionada] = useState(null);
  const [busqueda, setBusqueda] = useState("");
  const [ciudad, setCiudad] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    listarTerminales().then(setTerminales).catch((e) => setError(e.message));
  }, []);

  const texto = busqueda.trim().toLowerCase();
  const lista = terminales.filter((t) =>
    (!ciudad || t.ciudad === ciudad) && (!texto || `${t.nombre} ${t.direccion || ""}`.toLowerCase().includes(texto)));
  const ciudades = [...new Set(terminales.map((t) => t.ciudad))];

  return (
    <div className="container py-4 py-md-5">
      <div className="bt-section-head">
        <div>
          <p className="bt-eyebrow mb-0">Red de terminales</p>
          <h1>Terminales disponibles</h1>
        </div>
        <span className="bt-tag">
          <i className="bi bi-geo" aria-hidden="true"></i>
          {lista.length} {lista.length === 1 ? "terminal" : "terminales"}
        </span>
      </div>

      <Alerta tipo="error">{error}</Alerta>

      <div className="row g-2 g-md-3 mb-4">
        <div className="col-12 col-md-8">
          <div className="bt-field">
            <i className="bi bi-search" aria-hidden="true"></i>
            <input type="search" className="form-control" placeholder="Buscar por nombre o dirección"
                   aria-label="Buscar terminal" value={busqueda} onChange={(e) => setBusqueda(e.target.value)} />
          </div>
        </div>
        <div className="col-12 col-md-4">
          <div className="bt-field">
            <i className="bi bi-building" aria-hidden="true"></i>
            <select className="form-select" value={ciudad} onChange={(e) => setCiudad(e.target.value)} aria-label="Filtrar por ciudad">
              <option value="">Todas las ciudades</option>
              {ciudades.map((c) => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
        </div>
      </div>

      <div className="row g-4">
        <div className="col-12 col-lg-6 order-2 order-lg-1">
          <ul className="list-unstyled d-grid gap-3 mb-0">
            {lista.map((t) => (
              <li key={t.idTerminal}>
                <article className="bt-ficha" style={seleccionada === t.idTerminal ? { boxShadow: "var(--bt-shadow)" } : undefined}>
                  <div className="bt-ficha-body d-grid gap-2">
                    <div className="d-flex justify-content-between align-items-start gap-2">
                      <div>
                        <h2 className="h3 mb-1">{t.nombre}</h2>
                        <p className="mb-0 bt-muted">{t.direccion || "Dirección no registrada"} · {t.ciudad}</p>
                      </div>
                      <button type="button" className="bt-btn bt-btn-ghost bt-btn-sm" onClick={() => setSeleccionada(t.idTerminal)}>
                        <i className="bi bi-pin-map" aria-hidden="true"></i> Ver en mapa
                      </button>
                    </div>
                    <div className="d-flex flex-wrap gap-2">
                      <Link to={`/?origen=${t.idTerminal}`} className="bt-btn bt-btn-primary bt-btn-sm">Salir de aquí</Link>
                      <Link to={`/?destino=${t.idTerminal}`} className="bt-btn bt-btn-ghost bt-btn-sm">Llegar aquí</Link>
                    </div>
                  </div>
                </article>
              </li>
            ))}
          </ul>
        </div>
        <div className="col-12 col-lg-6 order-1 order-lg-2">
          <div className="bt-map" aria-label="Mapa de terminales">
            <MapaTerminales terminales={lista} seleccionada={seleccionada} onSeleccionar={setSeleccionada} />
          </div>
        </div>
      </div>
    </div>
  );
}
