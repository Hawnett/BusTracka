import { useEffect, useState } from "react";
import { listarViajesOperables, registrarRetraso } from "../api/adminService";
import Alerta from "../components/Alerta";
import { ESTADOS_VIAJE, formatearFecha } from "../utils/formato";

/** Administrador: reprograma la salida de un viaje; n8n avisa por correo a los pasajeros (HU 5.2). */
export default function Operaciones() {
  const [viajes, setViajes] = useState([]);
  const [minutos, setMinutos] = useState({});
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(null);

  const cargar = () =>
    listarViajesOperables()
      .then((lista) => {
        setViajes(lista);
        setMinutos(Object.fromEntries(lista.map((v) => [v.idViaje, String(v.minutosRetraso)])));
      })
      .catch((e) => setError(e.message));

  useEffect(() => {
    cargar();
  }, []);

  const guardar = async (viaje) => {
    setGuardando(viaje.idViaje);
    setError("");
    setMensaje("");
    try {
      const actualizado = await registrarRetraso(viaje.idViaje, Number(minutos[viaje.idViaje]));
      setMensaje(`${actualizado.ciudadOrigen} → ${actualizado.ciudadDestino}: nueva salida ${formatearFecha(actualizado.salidaEstimada)}. `
        + "Los pasajeros recibirán un correo con el cambio.");
      await cargar();
    } catch (e) {
      setError(e.message);
    } finally {
      setGuardando(null);
    }
  };

  return (
    <div className="container py-4 py-md-5">
      <div className="bt-section-head">
        <div>
          <p className="bt-eyebrow mb-0">Administrador</p>
          <h1>Operaciones de viajes</h1>
        </div>
      </div>
      <p className="bt-muted mb-4">Registra el retraso de un viaje que aún no llega a su destino. Con 0 minutos vuelve a su hora programada.</p>

      <Alerta tipo="error">{error}</Alerta>
      <Alerta tipo="exito">{mensaje}</Alerta>

      <div className="table-responsive">
        <table className="bt-tabla">
          <thead>
            <tr>
              <th scope="col">Viaje</th>
              <th scope="col">Bus</th>
              <th scope="col">Salida programada</th>
              <th scope="col">Salida estimada</th>
              <th scope="col">Estado</th>
              <th scope="col">Retraso (min)</th>
            </tr>
          </thead>
          <tbody>
            {viajes.map((v) => (
              <tr key={v.idViaje}>
                <td><strong>{v.ciudadOrigen} → {v.ciudadDestino}</strong></td>
                <td><span className="bt-plate">{v.placa}</span></td>
                <td>{formatearFecha(v.fechaSalida)}</td>
                <td>{formatearFecha(v.salidaEstimada)}</td>
                <td>{ESTADOS_VIAJE[v.estado]}</td>
                <td>
                  <div className="d-flex gap-2 align-items-center">
                    <input type="number" min="0" max="720" step="5" className="bt-input-min"
                           aria-label={`Minutos de retraso de ${v.ciudadOrigen} a ${v.ciudadDestino}`}
                           value={minutos[v.idViaje] ?? ""} onChange={(e) => setMinutos({ ...minutos, [v.idViaje]: e.target.value })} />
                    <button type="button" className="bt-btn bt-btn-primary bt-btn-sm" onClick={() => guardar(v)}
                            disabled={guardando === v.idViaje || minutos[v.idViaje] === String(v.minutosRetraso)}>
                      {guardando === v.idViaje ? "Guardando…" : "Guardar"}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
