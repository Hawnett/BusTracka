/** Panel lateral del seguimiento: origen, destino, estado y progreso del viaje. */
export default function PanelSeguimiento({ viaje = {}, children }) {
  const progreso = viaje.progreso ?? 0;

  return (
    <section className="bt-panel" aria-labelledby="titulo-seguimiento">
      <header className="bt-panel-head">
        <h2 id="titulo-seguimiento">Tu viaje</h2>
        <span className="bt-status" role="status">
          <span className="bt-status-dot" aria-hidden="true"></span>
          {viaje.estado}
        </span>
      </header>

      <div className="bt-panel-body">
        <ol className="bt-stops">
          <li>
            <span className="bt-stop-dot" aria-hidden="true"></span>
            <div>
              <span className="bt-stop-label">Origen</span>
              <span className="bt-stop-city">{viaje.origen}</span>
            </div>
          </li>
          <li>
            <span className="bt-stop-dot is-destino" aria-hidden="true">
              <i className="bi bi-geo-alt-fill"></i>
            </span>
            <div>
              <span className="bt-stop-label">Destino</span>
              <span className="bt-stop-city">{viaje.destino}</span>
            </div>
          </li>
        </ol>

        <div className="d-flex justify-content-between align-items-end mb-2">
          <span className="bt-label mb-1">Progreso del viaje</span>
          <span className="bt-progress-num">{progreso}%</span>
        </div>
        <div className="progress bt-progress" role="progressbar" aria-label="Progreso del viaje"
             aria-valuenow={progreso} aria-valuemin={0} aria-valuemax={100}>
          <div className="progress-bar" style={{ width: `${progreso}%` }}></div>
        </div>
        <div className="bt-progress-scale" aria-hidden="true">
          <span>Salida</span>
          <span>Llegada</span>
        </div>

        {children && <div className="mt-4">{children}</div>}
      </div>
    </section>
  );
}
