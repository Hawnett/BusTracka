import api, { llamar } from "./authService";

/** Con sesion iniciada. */
export const obtenerUbicacion = (idViaje) => llamar(api.get(`/seguimiento/${idViaje}`));

/** Enlace compartido: no requiere sesion, el backend valida el token. */
export const obtenerSeguimientoCompartido = (token) =>
  llamar(api.get("/publico/seguimiento", { params: { token } }));
