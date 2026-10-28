import api, { extraerMensajeError } from "./authService";

async function llamar(promesa) {
  try {
    const { data } = await promesa;
    return data;
  } catch (error) {
    throw new Error(extraerMensajeError(error));
  }
}

/** Con sesion iniciada. */
export const obtenerUbicacion = (idViaje) => llamar(api.get(`/seguimiento/${idViaje}`));

/** Enlace compartido: no requiere sesion, el backend valida el token. */
export const obtenerUbicacionPublica = (token) =>
  llamar(api.get("/publico/seguimiento", { params: { token } }));
