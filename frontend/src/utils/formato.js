// Las fechas llegan del backend sin zona (hora de Lima), el navegador las toma como locales.

export const formatearFecha = (iso) =>
  new Date(iso).toLocaleString("es-PE", { dateStyle: "medium", timeStyle: "short" });

export const fechaCorta = (iso) =>
  new Date(iso).toLocaleDateString("es-PE", { day: "2-digit", month: "short", year: "numeric" });

export const horaCorta = (iso) =>
  new Date(iso).toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit", hour12: false });

export const duracionTexto = (minutos) => `${Math.floor(minutos / 60)} h ${minutos % 60} min`;

/** Dias de diferencia entre dos fechas: sirve para mostrar "+1" cuando se llega al dia siguiente. */
export function diasDespues(desde, hasta) {
  const a = new Date(desde);
  const b = new Date(hasta);
  const inicioA = new Date(a.getFullYear(), a.getMonth(), a.getDate());
  const inicioB = new Date(b.getFullYear(), b.getMonth(), b.getDate());
  return Math.round((inicioB - inicioA) / 86400000);
}

export const soles = (monto) => `S/ ${Number(monto).toFixed(2)}`;

export const ESTADOS_PASAJE = {
  PROXIMO: { texto: "Próximo", clase: "is-proximo" },
  EN_CURSO: { texto: "En curso", clase: "is-curso" },
  COMPLETADO: { texto: "Completado", clase: "is-completado" },
  CANCELADO: { texto: "Cancelado", clase: "is-cancelado" },
};

export const ESTADOS_VIAJE = {
  PROGRAMADO: "Aún no sale",
  EN_RUTA: "En ruta",
  FINALIZADO: "Llegó a destino",
};
