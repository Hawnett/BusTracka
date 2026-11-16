import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

const RADIO_TIERRA_KM = 6371;

function distanciaKm([lat1, lon1], [lat2, lon2]) {
  const rad = Math.PI / 180;
  const h = Math.sin(((lat2 - lat1) * rad) / 2) ** 2
    + Math.cos(lat1 * rad) * Math.cos(lat2 * rad) * Math.sin(((lon2 - lon1) * rad) / 2) ** 2;
  return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(h));
}

/** Tramo ya recorrido: los puntos de la ruta que quedaron atras del bus, mas su posicion actual. */
function tramoRecorrido(puntos, progreso, posicion) {
  const acumulado = [0];
  for (let i = 1; i < puntos.length; i++) {
    acumulado.push(acumulado[i - 1] + distanciaKm(puntos[i - 1], puntos[i]));
  }
  const objetivo = progreso * acumulado[acumulado.length - 1];
  return [...puntos.filter((_, i) => acumulado[i] <= objetivo), posicion];
}

/**
 * Mapa Leaflet + OpenStreetMap (no requiere API key). Se crea una sola vez y
 * en cada actualizacion solo se mueve el bus y el tramo recorrido, sin redibujar todo.
 */
export default function MapaSeguimiento({ ubicacion }) {
  const contenedor = useRef(null);
  const mapa = useRef(null);
  const marcadorBus = useRef(null);
  const capaRuta = useRef(null);
  const lineaRecorrida = useRef(null);
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
    const puntos = ubicacion.ruta.map((p) => [p.latitud, p.longitud]);

    // La ruta, las paradas y las terminales solo se redibujan cuando cambia de viaje.
    if (idViajeDibujado.current !== ubicacion.idViaje) {
      idViajeDibujado.current = ubicacion.idViaje;
      if (capaRuta.current) capaRuta.current.remove();
      const linea = L.polyline(puntos, { color: "#14110f", weight: 4, opacity: 0.7, dashArray: "8 6" });
      const paradas = (ubicacion.paradas || []).slice(1, -1).map((p) =>
        L.circleMarker([p.latitud, p.longitud], { radius: 6, color: "#14110f", weight: 2, fillColor: "#fbf8f2", fillOpacity: 1 })
          .bindTooltip(p.nombre)
      );
      const origen = L.circleMarker(puntos[0], { radius: 8, color: "#1f7a4d", fillOpacity: 1 })
        .bindTooltip(`Origen: ${ubicacion.terminalOrigen || ubicacion.ciudadOrigen}`);
      const destino = L.circleMarker(puntos[puntos.length - 1], { radius: 8, color: "#14110f", fillColor: "#ffc400", fillOpacity: 1 })
        .bindTooltip(`Destino: ${ubicacion.terminalDestino || ubicacion.ciudadDestino}`);
      capaRuta.current = L.layerGroup([linea, ...paradas, origen, destino]).addTo(mapa.current);
      mapa.current.fitBounds(linea.getBounds(), { padding: [30, 30] });
    }

    const recorrido = tramoRecorrido(puntos, ubicacion.progreso, posicion);
    if (!lineaRecorrida.current) {
      lineaRecorrida.current = L.polyline(recorrido, { color: "#1f7a4d", weight: 5 }).addTo(mapa.current);
    } else {
      lineaRecorrida.current.setLatLngs(recorrido);
    }

    if (!marcadorBus.current) {
      const icono = L.divIcon({ html: "🚌", className: "bus-marker", iconSize: [30, 30] });
      marcadorBus.current = L.marker(posicion, { icon: icono, zIndexOffset: 1000 }).addTo(mapa.current);
    } else {
      marcadorBus.current.setLatLng(posicion);
    }
  }, [ubicacion]);

  return <div ref={contenedor} className="mapa-seguimiento" />;
}
