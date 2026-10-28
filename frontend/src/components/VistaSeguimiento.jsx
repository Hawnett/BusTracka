import { useEffect, useState } from "react";
import MapaSeguimiento from "./MapaSeguimiento";

const INTERVALO_MS = 3000;

/**
 * Consulta la ubicacion cada 3 s (polling) y la pinta en el mapa.
 * Recibe la funcion de carga para servir tanto al seguimiento privado
 * (por id de viaje) como al enlace publico (por token).
 */
export default function VistaSeguimiento({ cargarUbicacion }) {
  const [ubicacion, setUbicacion] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let activo = true;
    const consultar = async () => {
      try {
        const datos = await cargarUbicacion();
        if (activo) {
          setUbicacion(datos);
          setError("");
        }
      } catch (e) {
        if (activo) setError(e.message);
      }
    };
    consultar();
    const timer = setInterval(consultar, INTERVALO_MS);
    return () => {
      activo = false;
      clearInterval(timer);
    };
  }, [cargarUbicacion]);

  return (
    <>
      {error && <div className="alert alert-danger">{error}</div>}
      {ubicacion && (
        <p className="mb-2">
          <strong>{ubicacion.ciudadOrigen} → {ubicacion.ciudadDestino}</strong>{" "}
          <span className={"badge " + (ubicacion.estado === "EN_RUTA" ? "bg-success" : "bg-secondary")}>
            {ubicacion.estado === "EN_RUTA" ? "En ruta" : "Aun no sale"}
          </span>{" "}
          <span className="text-muted">Avance: {Math.round(ubicacion.progreso * 100)}%</span>
        </p>
      )}
      <MapaSeguimiento ubicacion={ubicacion} />
    </>
  );
}
