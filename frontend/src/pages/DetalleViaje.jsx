import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { obtenerViaje } from "../api/viajeService";
import { obtenerUbicacion } from "../api/seguimientoService";
import Alerta from "../components/Alerta";
import MapaSeguimiento from "../components/MapaSeguimiento";
import ParadasViaje from "../components/ParadasViaje";
import { ESTADOS_VIAJE, duracionTexto, formatearFecha, soles } from "../utils/formato";

/** Detalle de un viaje de la busqueda: recorrido en el mapa, itinerario y datos del bus. */
export default function DetalleViaje() {
  const { id } = useParams();
  const [viaje, setViaje] = useState(null);
  const [recorrido, setRecorrido] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([obtenerViaje(id), obtenerUbicacion(id)])
      .then(([v, r]) => {
        setViaje(v);
        setRecorrido(r);
      })
      .catch((e) => setError(e.message));
  }, [id]);

  if (!viaje || !recorrido) {
    return <div className="container py-5">{error ? <Alerta tipo="error">{error}</Alerta> : <p className="bt-muted">Cargando…</p>}</div>;
  }

  const programado = viaje.estado === "PROGRAMADO";

  return (
    <div className="container py-4 py-md-5">
      <Link to="/" className="bt-link small d-inline-flex align-items-center gap-1 mb-3">
        <i className="bi bi-arrow-left" aria-hidden="true"></i> Volver a resultados
      </Link>
      <div className="bt-section-head">
        <div>
          <p className="bt-eyebrow mb-0">Detalle del viaje · {formatearFecha(viaje.salidaEstimada)}</p>
          <h1>{viaje.ciudadOrigen} → {viaje.ciudadDestino}</h1>
        </div>
        <div className="d-flex flex-wrap gap-2">
          {viaje.minutosRetraso > 0 && <span className="bt-retraso">Retraso {viaje.minutosRetraso} min</span>}
          <span className="bt-tag">{ESTADOS_VIAJE[viaje.estado]}</span>
        </div>
      </div>

      <div className="row g-4">
        <div className="col-12 col-lg-7">
          <div className="bt-map" aria-label="Recorrido del viaje en el mapa">
            <MapaSeguimiento ubicacion={recorrido} />
          </div>
        </div>

        <div className="col-12 col-lg-5 d-grid gap-4 align-content-start">
          <section aria-labelledby="titulo-itinerario">
            <h2 id="titulo-itinerario" className="bt-label mb-2">Itinerario</h2>
            <ParadasViaje paradas={recorrido.paradas} mostrarEstado={!programado} />
          </section>

          <section className="bt-ficha" aria-labelledby="titulo-datos">
            <h2 id="titulo-datos" className="bt-ficha-head">Datos del viaje</h2>
            <dl className="bt-filas">
              <div className="bt-fila"><dt>Bus</dt><dd><span className="bt-plate">{viaje.placa}</span></dd></div>
              <div className="bt-fila"><dt>Sale de</dt><dd>{viaje.terminalOrigen}</dd></div>
              <div className="bt-fila"><dt>Llega a</dt><dd>{viaje.terminalDestino}</dd></div>
              <div className="bt-fila"><dt>Llegada estimada</dt><dd>{formatearFecha(viaje.llegadaEstimada)}</dd></div>
              <div className="bt-fila"><dt>Duración</dt><dd>{duracionTexto(viaje.duracionMin)}</dd></div>
              <div className="bt-fila"><dt>Distancia</dt><dd>{recorrido.distanciaTotalKm} km</dd></div>
              <div className="bt-fila"><dt>Asientos disponibles</dt><dd>{viaje.asientosDisponibles} de {viaje.capacidad}</dd></div>
              <div className="bt-fila"><dt>Precio</dt><dd className="fs-5">{soles(viaje.precio)}</dd></div>
            </dl>
          </section>

          <div className="d-grid gap-2">
            {programado && viaje.asientosDisponibles > 0 && (
              <Link to={`/viajes/${viaje.idViaje}`} className="bt-btn bt-btn-primary">
                Elegir asientos <i className="bi bi-arrow-right" aria-hidden="true"></i>
              </Link>
            )}
            {viaje.estado === "EN_RUTA" && (
              <Link to={`/seguimiento?viaje=${viaje.idViaje}`} className="bt-btn bt-btn-primary">
                <i className="bi bi-geo-alt" aria-hidden="true"></i> Seguir bus en vivo
              </Link>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
