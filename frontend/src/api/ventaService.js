import api, { extraerMensajeError } from "./authService";

async function llamar(promesa) {
  try {
    const { data } = await promesa;
    return data;
  } catch (error) {
    throw new Error(extraerMensajeError(error));
  }
}

export const comprar = (solicitud) => llamar(api.post("/ventas", solicitud));
export const listarCompras = () => llamar(api.get("/ventas"));
export const obtenerComprobante = (id) => llamar(api.get(`/ventas/${id}`));
