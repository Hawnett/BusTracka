import { useEffect, useRef } from "react";
import L from "leaflet";
import "leaflet/dist/leaflet.css";

/** Terminales en el mapa; al elegir una en la lista, el mapa se centra en ella. */
export default function MapaTerminales({ terminales, seleccionada, onSeleccionar }) {
  const contenedor = useRef(null);
  const mapa = useRef(null);
  const capa = useRef(null);

  useEffect(() => {
    mapa.current = L.map(contenedor.current).setView([-12.05, -77.04], 5);
    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      attribution: "&copy; OpenStreetMap",
      maxZoom: 18,
    }).addTo(mapa.current);
    return () => mapa.current.remove();
  }, []);

  useEffect(() => {
    if (!mapa.current || terminales.length === 0) return;
    if (capa.current) capa.current.remove();
    const marcadores = terminales.map((t) =>
      L.circleMarker([t.latitud, t.longitud], {
        radius: t.idTerminal === seleccionada ? 11 : 8,
        color: "#14110f",
        weight: 2,
        fillColor: t.idTerminal === seleccionada ? "#ffc400" : "#fbf8f2",
        fillOpacity: 1,
      })
        .bindTooltip(`${t.nombre} · ${t.ciudad}`)
        .on("click", () => onSeleccionar(t.idTerminal))
    );
    capa.current = L.featureGroup(marcadores).addTo(mapa.current);
    const elegida = terminales.find((t) => t.idTerminal === seleccionada);
    if (elegida) {
      mapa.current.setView([elegida.latitud, elegida.longitud], 9);
    } else {
      mapa.current.fitBounds(capa.current.getBounds(), { padding: [30, 30] });
    }
  }, [terminales, seleccionada, onSeleccionar]);

  return <div ref={contenedor} className="mapa-seguimiento" />;
}
