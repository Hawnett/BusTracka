import { useEffect, useState } from "react";
import MapaSeguimiento from "./MapaSeguimiento";
import PanelSeguimiento from "./PanelSeguimiento";
import Alerta from "./Alerta";

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

  const viaje = ubicacion && {
    origen: ubicacion.ciudadOrigen,
    destino: ubicacion.ciudadDestino,
    estado: ubicacion.estado === "EN_RUTA" ? "En ruta" : "Aún no sale",
    progreso: Math.round(ubicacion.progreso * 100),
  };

  return (
    <>
      <Alerta tipo="error">{error}</Alerta>
      <div className="row g-3 g-lg-4">
        <div className="col-12 col-lg-8 order-2 order-lg-1">
          <div className="bt-map" aria-label="Mapa del recorrido">
            <MapaSeguimiento ubicacion={ubicacion} />
          </div>
        </div>
        <div className="col-12 col-lg-4 order-1 order-lg-2">
          <PanelSeguimiento viaje={viaje || {}} />
        </div>
      </div>
    </>
  );
}
