import { useCallback } from "react";
import { useParams } from "react-router-dom";
import { obtenerUbicacionPublica } from "../api/seguimientoService";
import VistaSeguimiento from "../components/VistaSeguimiento";

/** Enlace compartido del comprobante: funciona sin iniciar sesion. */
export default function SeguimientoPublico() {
  const { token } = useParams();
  const cargar = useCallback(() => obtenerUbicacionPublica(token), [token]);

  return (
    <div className="container py-4">
      <h1 className="h4 mb-3">BusTracka · Sigue tu bus</h1>
      <VistaSeguimiento cargarUbicacion={cargar} />
    </div>
  );
}
