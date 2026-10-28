import api, { extraerMensajeError } from "./authService";

/** Envuelve una llamada axios para que los componentes reciban siempre un Error con mensaje legible. */
async function llamar(promesa) {
  try {
    const { data } = await promesa;
    return data;
  } catch (error) {
    throw new Error(extraerMensajeError(error));
  }
}

export const listarTerminales = () => llamar(api.get("/terminales"));

export const buscarViajes = ({ origen, destino, fecha }) =>
  llamar(api.get("/viajes", { params: { origen: origen || undefined, destino: destino || undefined, fecha: fecha || undefined } }));

export const listarViajesEnCurso = () => llamar(api.get("/viajes/en-curso"));
export const obtenerViaje = (id) => llamar(api.get(`/viajes/${id}`));
export const listarAsientos = (id) => llamar(api.get(`/viajes/${id}/asientos`));
