import { useCallback, useEffect, useState } from "react";
import { listarViajesEnCurso } from "../api/viajeService";
import { obtenerUbicacion } from "../api/seguimientoService";
import VistaSeguimiento from "../components/VistaSeguimiento";

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
    <>
      <h1 className="h4 mb-3">Seguimiento de buses</h1>
      {error && <div className="alert alert-danger">{error}</div>}
      {viajes.length === 0 && !error && <div className="alert alert-info">No hay buses en ruta en este momento.</div>}
      {viajes.length > 0 && (
        <>
          <select className="form-select mb-3" value={idViaje} onChange={(e) => setIdViaje(e.target.value)}>
            {viajes.map((v) => (
              <option key={v.idViaje} value={v.idViaje}>
                {v.ciudadOrigen} → {v.ciudadDestino} · bus {v.placa}
              </option>
            ))}
          </select>
          {idViaje && <VistaSeguimiento key={idViaje} cargarUbicacion={cargar} />}
        </>
      )}
    </>
  );
}
