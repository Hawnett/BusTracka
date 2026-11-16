import ParadasViaje from "./ParadasViaje";
import { ESTADOS_VIAJE, horaCorta } from "../utils/formato";

/** Panel lateral del seguimiento: estado, progreso, datos del recorrido y paradas. */
export default function PanelSeguimiento({ ubicacion, children }) {
  if (!ubicacion) {
    return (
      <section className="bt-panel" aria-label="Tu viaje">
        <div className="bt-panel-body bt-muted">Cargando el recorrido…</div>
      </section>
    );
  }

  const progreso = Math.round(ubicacion.progreso * 100);
  const enRuta = ubicacion.estado === "EN_RUTA";

  return (
    <section className="bt-panel" aria-labelledby="titulo-seguimiento">
      <header className="bt-panel-head">
        <h2 id="titulo-seguimiento">{ubicacion.ciudadOrigen} → {ubicacion.ciudadDestino}</h2>
        <span className="bt-status" role="status">
          {enRuta && <span className="bt-status-dot" aria-hidden="true"></span>}
          {ESTADOS_VIAJE[ubicacion.estado]}
        </span>
      </header>

      <div className="bt-panel-body">
        <div className="d-flex flex-wrap gap-2 mb-3">
          <span className="bt-plate">{ubicacion.placa}</span>
          {ubicacion.minutosRetraso > 0 && (
            <span className="bt-retraso">
              <i className="bi bi-clock-history" aria-hidden="true"></i>
              Retraso de {ubicacion.minutosRetraso} min
            </span>
          )}
          {ubicacion.aceleracion > 1 && (
            <span className="bt-tag" title="El bus de la demo avanza más rápido que el horario real">
              Simulación ×{ubicacion.aceleracion}
            </span>
          )}
        </div>

        <div className="d-flex justify-content-between align-items-end mb-2">
          <span className="bt-label mb-1">Progreso del recorrido</span>
          <span className="bt-progress-num">{progreso}%</span>
        </div>
        <div className="progress bt-progress" role="progressbar" aria-label="Progreso del viaje"
             aria-valuenow={progreso} aria-valuemin={0} aria-valuemax={100}>
          <div className="progress-bar" style={{ width: `${progreso}%` }}></div>
        </div>
        <div className="bt-progress-scale mb-3" aria-hidden="true">
          <span>{ubicacion.ciudadOrigen} · {horaCorta(ubicacion.salidaEstimada)}</span>
          <span>{ubicacion.ciudadDestino} · {horaCorta(ubicacion.llegadaEstimada)}</span>
        </div>

        <dl className="bt-datos">
          <div className="bt-dato">
            <dt>Próxima parada</dt>
            <dd>{ubicacion.proximaParada || "—"}</dd>
          </div>
          <div className="bt-dato">
            <dt>Llegada estimada</dt>
            <dd>{horaCorta(ubicacion.llegadaEstimada)}</dd>
          </div>
          <div className="bt-dato">
            <dt>Velocidad</dt>
            <dd>{enRuta ? `${ubicacion.velocidadKmh} km/h` : "—"}</dd>
          </div>
          <div className="bt-dato">
            <dt>Recorrido</dt>
            <dd>{ubicacion.recorridoKm} / {ubicacion.distanciaTotalKm} km</dd>
          </div>
        </dl>

        <p className="bt-label mb-2">Estado de paradas</p>
        <ParadasViaje paradas={ubicacion.paradas} />

        {children && <div className="mt-4">{children}</div>}
      </div>
    </section>
  );
}
