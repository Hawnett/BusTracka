import { useEffect, useState } from "react";
import { listarViajesOperables, registrarRetraso } from "../api/adminService";
import Alerta from "../components/Alerta";
import { ESTADOS_VIAJE, formatearFecha } from "../utils/formato";

const MOTIVOS = [
  ["TRAFICO", "Tráfico en la ruta"],
  ["CLIMA", "Condiciones climáticas"],
  ["FALLA_MECANICA", "Falla mecánica del bus"],
  ["ACCIDENTE", "Accidente en la vía"],
  ["OTRO", "Otro motivo"],
];

const FORMULARIO_VACIO = { minutosRetraso: "", motivo: "", detalle: "", avisarPasajeros: true };

/** Administrador: registra el retraso de un viaje con su motivo; n8n avisa por correo a los pasajeros (HU 5.2). */
export default function Operaciones() {
  const [viajes, setViajes] = useState([]);
  const [elegido, setElegido] = useState(null);
  const [form, setForm] = useState(FORMULARIO_VACIO);
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");
  const [guardando, setGuardando] = useState(false);

  const cargar = () => listarViajesOperables().then(setViajes).catch((e) => setError(e.message));

  useEffect(() => {
    cargar();
  }, []);

  const abrir = (viaje) => {
    setElegido(viaje);
    setForm({ ...FORMULARIO_VACIO, minutosRetraso: String(viaje.minutosRetraso) });
    setMensaje("");
    setError("");
  };

  const cambiar = (campo) => (e) =>
      setForm({ ...form, [campo]: e.target.type === "checkbox" ? e.target.checked : e.target.value });

  const guardar = async (e) => {
    e.preventDefault();
    setGuardando(true);
    setError("");
    setMensaje("");
    try {
      const actualizado = await registrarRetraso(elegido.idViaje, {
        minutosRetraso: form.minutosRetraso === "" ? null : Number(form.minutosRetraso),
        motivo: form.motivo || null,
        detalle: form.detalle,
        avisarPasajeros: form.avisarPasajeros,
      });
      const aviso = form.avisarPasajeros && Number(form.minutosRetraso) > 0
          ? "Los pasajeros recibirán un correo con el cambio y el motivo."
          : "No se enviará correo a los pasajeros.";
      setMensaje(`${actualizado.ciudadOrigen} → ${actualizado.ciudadDestino}: nueva salida ${formatearFecha(actualizado.salidaEstimada)}. ${aviso}`);
      setElegido(null);
      await cargar();
    } catch (err) {
      setError(err.message);
    } finally {
      setGuardando(false);
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
        <p className="bt-muted mb-4">Registra el retraso de un viaje que aún no llega a su destino, con su motivo. Con 0 minutos vuelve a su hora programada.</p>

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
              <th scope="col">Retraso</th>
              <th scope="col"><span className="visually-hidden">Acción</span></th>
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
                  <td>{v.minutosRetraso} min</td>
                  <td>
                    <button type="button" className="bt-btn bt-btn-primary bt-btn-sm" onClick={() => abrir(v)}>
                      Registrar retraso
                    </button>
                  </td>
                </tr>
            ))}
            </tbody>
          </table>
        </div>

        {elegido && (
            <form className="bt-panel mt-4" onSubmit={guardar} noValidate>
              <div className="bt-panel-head">
                <h2>Retraso de {elegido.ciudadOrigen} → {elegido.ciudadDestino}</h2>
                <span className="bt-plate">{elegido.placa}</span>
              </div>
              <div className="bt-panel-body">
                <div className="row g-3">
                  <div className="col-sm-4">
                    <label className="form-label" htmlFor="minutos">Minutos de retraso (0 a 720)</label>
                    <input id="minutos" type="number" min="0" max="720" step="5" className="form-control"
                           value={form.minutosRetraso} onChange={cambiar("minutosRetraso")} />
                  </div>
                  <div className="col-sm-8">
                    <label className="form-label" htmlFor="motivo">Motivo</label>
                    <select id="motivo" className="form-select" value={form.motivo} onChange={cambiar("motivo")}>
                      <option value="">Elige un motivo</option>
                      {MOTIVOS.map(([valor, texto]) => <option key={valor} value={valor}>{texto}</option>)}
                    </select>
                  </div>
                  <div className="col-12">
                    <label className="form-label" htmlFor="detalle">Detalle para el pasajero (10 a 200 caracteres)</label>
                    <textarea id="detalle" className="form-control" rows="2" maxLength={200}
                              value={form.detalle} onChange={cambiar("detalle")} />
                  </div>
                  <div className="col-12 form-check ms-2">
                    <input id="avisar" type="checkbox" className="form-check-input"
                           checked={form.avisarPasajeros} onChange={cambiar("avisarPasajeros")} />
                    <label className="form-check-label" htmlFor="avisar">Avisar a los pasajeros por correo</label>
                  </div>
                </div>
                <div className="d-flex gap-2 mt-3">
                  <button type="submit" className="bt-btn bt-btn-primary" disabled={guardando}>
                    {guardando ? "Guardando…" : "Guardar retraso"}
                  </button>
                  <button type="button" className="bt-btn" onClick={() => setElegido(null)}>Cancelar</button>
                </div>
              </div>
            </form>
        )}
      </div>
  );
}