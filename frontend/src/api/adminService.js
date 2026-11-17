import api, { llamar } from "./authService";

export const listarViajesOperables = () => llamar(api.get("/admin/viajes"));

/** datos = { minutosRetraso, motivo, detalle, avisarPasajeros } (HU 5.2). */
export const registrarRetraso = (idViaje, datos) =>
    llamar(api.put(`/admin/viajes/${idViaje}/retraso`, datos));