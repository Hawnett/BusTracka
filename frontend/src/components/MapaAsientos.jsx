/**
 * Plano del bus. Recibe los asientos de la API ({idAsiento, numero, ocupado})
 * y los ids seleccionados; el estado visual se calcula aqui.
 */
export default function MapaAsientos({ asientos = [], seleccion = [], onAlternar }) {
  const estadoDe = (a) => (a.ocupado ? "ocupado" : seleccion.includes(a.idAsiento) ? "seleccionado" : "libre");

  return (
    <div className="d-grid gap-3">
      <div className="bt-legend" aria-hidden="true">
        <span><span className="bt-legend-swatch"></span> Libre</span>
        <span><span className="bt-legend-swatch is-seleccionado"></span> Seleccionado</span>
        <span><span className="bt-legend-swatch is-ocupado"></span> Ocupado</span>
      </div>

      <div className="bt-bus">
        <div className="bt-bus-front">
          <span><i className="bi bi-door-open" aria-hidden="true"></i> Puerta</span>
          <span><i className="bi bi-person-square" aria-hidden="true"></i> Conductor</span>
        </div>

        <div className="bt-seat-grid" role="group" aria-label={`Asientos del bus (${asientos.length})`}>
          {asientos.map((a) => {
            const estado = estadoDe(a);
            return (
              <button
                key={a.idAsiento}
                type="button"
                className={`bt-seat is-${estado}`}
                disabled={a.ocupado}
                aria-pressed={estado === "seleccionado"}
                aria-label={`Asiento ${a.numero}, ${estado}`}
                onClick={() => onAlternar(a)}
              >
                {String(a.numero).padStart(2, "0")}
              </button>
            );
          })}
        </div>
      </div>
    </div>
  );
}
