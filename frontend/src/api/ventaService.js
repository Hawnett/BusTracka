import api, { llamar } from "./authService";

export const comprar = (solicitud) => llamar(api.post("/ventas", solicitud));
export const listarCompras = () => llamar(api.get("/ventas"));
export const obtenerComprobante = (id) => llamar(api.get(`/ventas/${id}`));
export const generarEnlaceSeguimiento = (id) => llamar(api.post(`/ventas/${id}/enlace-seguimiento`));

/** Pagina publica a la que lleva el QR del boleto (no requiere sesion). */
export const validarBoleto = (codigo) => llamar(api.get(`/publico/boletos/${encodeURIComponent(codigo)}`));

/** Descarga el PDF del comprobante (va con el token, por eso no basta un enlace normal). */
export async function descargarComprobantePdf(id, codigo) {
  const pdf = await llamar(api.get(`/ventas/${id}/pdf`, { responseType: "blob" }));
  const url = URL.createObjectURL(pdf);
  const enlace = document.createElement("a");
  enlace.href = url;
  enlace.download = `boleto-${codigo || id}.pdf`;
  document.body.appendChild(enlace);
  enlace.click();
  enlace.remove();
  URL.revokeObjectURL(url);
}
