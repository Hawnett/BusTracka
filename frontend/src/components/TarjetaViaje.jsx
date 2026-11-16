import { Link } from "react-router-dom";
import HoraPanel from "./HoraPanel";
import { diasDespues, duracionTexto, fechaCorta, horaCorta } from "../utils/formato";

/** Boleto de un viaje (datos tal como los entrega la API de viajes). */
export default function TarjetaViaje({ viaje, onComprar }) {
  const libres = viaje.asientosDisponibles;
  const sinAsientos = libres === 0;
  const diasLlegada = diasDespues(viaje.salidaEstimada, viaje.llegadaEstimada);

  return (
    <article
      className={`bt-boleto ${sinAsientos ? "is-agotado" : ""}`}
      aria-label={`${viaje.ciudadOrigen} a ${viaje.ciudadDestino}, ${horaCorta(viaje.salidaEstimada)}`}
    >
      <div className="bt-boleto-cuerpo">
        <div className="bt-boleto-top">
          <span className="bt-label mb-0 bt-muted">Boleto · Interprovincial</span>
          {viaje.minutosRetraso > 0 && (
            <span className="bt-retraso">
              <i className="bi bi-clock-history" aria-hidden="true"></i>
              Retraso {viaje.minutosRetraso} min
            </span>
          )}
          {sinAsientos && <span className="bt-stamp">Agotado</span>}
        </div>

        <div className="bt-ruta">
          <div>
            <span className="bt-label mb-1 bt-muted">Origen</span>
            <span className="bt-ciudad">{viaje.ciudadOrigen}</span>
            <span className="d-block small bt-muted">{viaje.terminalOrigen}</span>
          </div>
          <div className="bt-ruta-linea" aria-hidden="true">
            <i className="bi bi-bus-front-fill"></i>
          </div>
          <div className="text-end">
            <span className="bt-label mb-1 bt-muted">Destino</span>
            <span className="bt-ciudad">{viaje.ciudadDestino}</span>
            <span className="d-block small bt-muted">{viaje.terminalDestino}</span>
          </div>
        </div>

        <dl className="bt-meta">
          <div>
            <dt>Fecha</dt>
            <dd>{fechaCorta(viaje.salidaEstimada)}</dd>
          </div>
          <div>
            <dt>Llega</dt>
            <dd>{horaCorta(viaje.llegadaEstimada)}{diasLlegada > 0 && ` (+${diasLlegada})`}</dd>
          </div>
          <div>
            <dt>Duración</dt>
            <dd>{duracionTexto(viaje.duracionMin)}</dd>
          </div>
          <div>
            <dt>Placa</dt>
            <dd><span className="bt-plate">{viaje.placa}</span></dd>
          </div>
          <div>
            <dt>Disponibilidad</dt>
            <dd>
              <span className={`bt-libres ${sinAsientos ? "bt-libres--none" : ""}`}>
                {sinAsientos ? "Sin asientos libres" : `${libres} asientos libres`}
              </span>
            </dd>
          </div>
        </dl>
      </div>

      <div className="bt-boleto-talon bt-perfo">
        <div>
          <span className="bt-label mb-1 d-block">Sale</span>
          <HoraPanel hora={horaCorta(viaje.salidaEstimada)} />
        </div>
        <p className="bt-precio mb-0">
          <small>S/</small>
          {Number(viaje.precio).toFixed(2)}
        </p>
        <div className="d-grid gap-2">
          <button type="button" className="bt-btn bt-btn-primary" disabled={sinAsientos} onClick={() => onComprar(viaje)}>
            {sinAsientos ? "Agotado" : "Elegir asientos"}
            {!sinAsientos && <i className="bi bi-arrow-right" aria-hidden="true"></i>}
          </button>
          <Link to={`/viajes/${viaje.idViaje}/detalle`} className="bt-btn bt-btn-ghost bt-btn-sm">
            Ver detalle y mapa
          </Link>
        </div>
      </div>
    </article>
  );
}
