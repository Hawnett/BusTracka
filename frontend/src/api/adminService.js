import api, { llamar } from "./authService";

export const listarViajesOperables = () => llamar(api.get("/admin/viajes"));

export const registrarRetraso = (idViaje, minutosRetraso) =>
  llamar(api.put(`/admin/viajes/${idViaje}/retraso`, { minutosRetraso }));
