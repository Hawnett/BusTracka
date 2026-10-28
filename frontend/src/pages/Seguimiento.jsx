import { useCallback, useEffect, useState } from "react";
import { listarViajesEnCurso } from "../api/viajeService";
import { obtenerUbicacion } from "../api/seguimientoService";
import VistaSeguimiento from "../components/VistaSeguimiento";
import Alerta from "../components/Alerta";

/** Sprint 3: seguimiento (con sesion) de los buses en ruta. */
export default function Seguimiento() {
  const [viajes, setViajes] = useState([]);
  const [idViaje, setIdViaje] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    listarViajesEnCurso()
      .then((lista) => {
        setViajes(lista);
        if (lista.length > 0) setIdViaje(String(lista[0].idViaje));
      })
      .catch((e) => setError(e.message));
  }, []);

  // useCallback: VistaSeguimiento reinicia su polling solo si cambia el viaje elegido.
  const cargar = useCallback(() => obtenerUbicacion(idViaje), [idViaje]);

  return (
    <div className="container py-4 py-md-5">
      <div className="bt-section-head">
        <div>
          <p className="bt-eyebrow mb-0">En vivo</p>
          <h1>Seguimiento del bus</h1>
        </div>
      </div>

      <Alerta tipo="error">{error}</Alerta>

      {viajes.length === 0 && !error && (
        <div className="bt-empty">
          <div className="bt-pictograma" aria-hidden="true"><i className="bi bi-map"></i></div>
          <h2 className="mb-2">No hay buses en ruta en este momento</h2>
          <p className="bt-muted mb-0">Cuando un viaje salga, podrás verlo aquí.</p>
        </div>
      )}

      {viajes.length > 0 && (
        <>
          <div className="bt-field mb-3" style={{ maxWidth: 520 }}>
            <i className="bi bi-bus-front" aria-hidden="true"></i>
            <select className="form-select" value={idViaje} onChange={(e) => setIdViaje(e.target.value)}
                    aria-label="Elegir viaje en ruta">
              {viajes.map((v) => (
                <option key={v.idViaje} value={v.idViaje}>
                  {v.ciudadOrigen} → {v.ciudadDestino} · bus {v.placa}
                </option>
              ))}
            </select>
          </div>
          {idViaje && <VistaSeguimiento key={idViaje} cargarUbicacion={cargar} />}
        </>
      )}
    </div>
  );
}
