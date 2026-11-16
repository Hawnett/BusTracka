import { horaCorta } from "../utils/formato";

const ETIQUETAS = {
  PASO: { texto: "Pasó", clase: "is-paso" },
  ACTUAL: { texto: "Ahora", clase: "is-actual" },
  PROXIMA: { texto: "Próxima", clase: "is-proxima" },
};

/** Itinerario: hora estimada de cada parada y si el bus ya pasó, está en ese tramo o aún no llega. */
export default function ParadasViaje({ paradas = [], mostrarEstado = true }) {
  return (
    <ol className="bt-paradas" aria-label="Paradas del viaje">
      {paradas.map((p) => {
        const etiqueta = ETIQUETAS[p.estado] || ETIQUETAS.PROXIMA;
        return (
          <li key={p.nombre}>
            <time dateTime={p.hora}>{horaCorta(p.hora)}</time>
            {mostrarEstado ? <span className={`bt-parada-tag ${etiqueta.clase}`}>{etiqueta.texto}</span> : <span />}
            <span>{p.nombre}</span>
          </li>
        );
      })}
    </ol>
  );
}
