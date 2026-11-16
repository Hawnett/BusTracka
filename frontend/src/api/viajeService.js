import api, { llamar } from "./authService";

export const listarTerminales = () => llamar(api.get("/terminales"));

export const buscarViajes = ({ origen, destino, fecha }) =>
  llamar(api.get("/viajes", { params: { origen: origen || undefined, destino: destino || undefined, fecha: fecha || undefined } }));

export const listarViajesEnCurso = () => llamar(api.get("/viajes/en-curso"));
export const obtenerViaje = (id) => llamar(api.get(`/viajes/${id}`));
export const listarAsientos = (id) => llamar(api.get(`/viajes/${id}/asientos`));
