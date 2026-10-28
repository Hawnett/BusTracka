import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

/**
 * Mapa Leaflet + OpenStreetMap (no requiere API key). Se crea una sola vez y
 * en cada actualizacion solo se mueve el marcador del bus, sin redibujar todo.
 */
export default function MapaSeguimiento({ ubicacion }) {
  const contenedor = useRef(null);
  const mapa = useRef(null);
  const marcadorBus = useRef(null);
  const capaRuta = useRef(null);
  const idViajeDibujado = useRef(null);

  useEffect(() => {
    mapa.current = L.map(contenedor.current).setView([-12.05, -77.04], 6);
    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "&copy; OpenStreetMap",
      maxZoom: 18,
    }).addTo(mapa.current);
    return () => mapa.current.remove();
  }, []);

  useEffect(() => {
    if (!ubicacion || !mapa.current) return;
    const posicion = [ubicacion.latitud, ubicacion.longitud];

    // La ruta y las terminales solo se redibujan cuando cambia de viaje.
    if (idViajeDibujado.current !== ubicacion.idViaje) {
      idViajeDibujado.current = ubicacion.idViaje;
      if (capaRuta.current) capaRuta.current.remove();
      const puntos = ubicacion.ruta.map((p) => [p.latitud, p.longitud]);
      const linea = L.polyline(puntos, { color: "#14110f", weight: 4, opacity: 0.8, dashArray: "8 6" });
      const origen = L.circleMarker(puntos[0], { radius: 8, color: "#1f7a4d", fillOpacity: 1 })
        .bindTooltip(`Origen: ${ubicacion.ciudadOrigen}`);
      const destino = L.circleMarker(puntos[puntos.length - 1], { radius: 8, color: "#14110f", fillColor: "#ffc400", fillOpacity: 1 })
        .bindTooltip(`Destino: ${ubicacion.ciudadDestino}`);
      capaRuta.current = L.layerGroup([linea, origen, destino]).addTo(mapa.current);
      mapa.current.fitBounds(linea.getBounds(), { padding: [30, 30] });
    }

    if (!marcadorBus.current) {
      const icono = L.divIcon({ html: "🚌", className: "bus-marker", iconSize: [30, 30] });
      marcadorBus.current = L.marker(posicion, { icon: icono }).addTo(mapa.current);
    } else {
      marcadorBus.current.setLatLng(posicion);
    }
  }, [ubicacion]);

  return <div ref={contenedor} className="mapa-seguimiento" />;
}
