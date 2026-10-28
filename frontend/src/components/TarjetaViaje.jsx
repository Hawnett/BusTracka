import HoraPanel from "./HoraPanel";

const fechaCorta = (iso) =>
  new Date(iso).toLocaleDateString("es-PE", { day: "2-digit", month: "short", year: "numeric" });
const horaCorta = (iso) =>
  new Date(iso).toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit", hour12: false });

/** Boleto de un viaje (datos tal como los entrega la API de viajes). */
export default function TarjetaViaje({ viaje, onComprar }) {
  const libres = viaje.asientosDisponibles;
  const sinAsientos = libres === 0;

  return (
    <article
      className={`bt-boleto ${sinAsientos ? "is-agotado" : ""}`}
      aria-label={`${viaje.ciudadOrigen} a ${viaje.ciudadDestino}, ${horaCorta(viaje.fechaSalida)}`}
    >
      <div className="bt-boleto-cuerpo">
        <div className="bt-boleto-top">
          <span className="bt-label mb-0 bt-muted">Boleto · Interprovincial</span>
          {sinAsientos && <span className="bt-stamp">Agotado</span>}
        </div>

        <div className="bt-ruta">
          <div>
            <span className="bt-label mb-1 bt-muted">Origen</span>
            <span className="bt-ciudad">{viaje.ciudadOrigen}</span>
          </div>
          <div className="bt-ruta-linea" aria-hidden="true">
            <i className="bi bi-bus-front-fill"></i>
          </div>
          <div className="text-end">
            <span className="bt-label mb-1 bt-muted">Destino</span>
            <span className="bt-ciudad">{viaje.ciudadDestino}</span>
          </div>
        </div>

        <dl className="bt-meta">
          <div>
            <dt>Fecha</dt>
            <dd>{fechaCorta(viaje.fechaSalida)}</dd>
          </div>
          <div>
            <dt>Duración</dt>
            <dd>{Math.floor(viaje.duracionMin / 60)} h {viaje.duracionMin % 60} min</dd>
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
          <HoraPanel hora={horaCorta(viaje.fechaSalida)} />
        </div>
        <p className="bt-precio mb-0">
          <small>S/</small>
          {Number(viaje.precio).toFixed(2)}
        </p>
        <button type="button" className="bt-btn bt-btn-primary" disabled={sinAsientos} onClick={() => onComprar(viaje)}>
          {sinAsientos ? "Agotado" : "Elegir asientos"}
          {!sinAsientos && <i className="bi bi-arrow-right" aria-hidden="true"></i>}
        </button>
      </div>
    </article>
  );
}
