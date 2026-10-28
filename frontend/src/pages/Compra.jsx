import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { listarAsientos, obtenerViaje } from "../api/viajeService";
import { comprar } from "../api/ventaService";
import { formatearFecha } from "./BuscarViajes";

/** Sprint 2 (mapa de asientos) + Sprint 4 (pago): una sola pantalla en dos pasos visuales. */
export default function Compra() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [viaje, setViaje] = useState(null);
  const [asientos, setAsientos] = useState([]);
  const [seleccion, setSeleccion] = useState([]);
  const [form, setForm] = useState({ nombrePasajero: "", titularTarjeta: "", numeroTarjeta: "" });
  const [error, setError] = useState("");
  const [procesando, setProcesando] = useState(false);

  const cargar = useCallback(async () => {
    try {
      const [v, a] = await Promise.all([obtenerViaje(id), listarAsientos(id)]);
      setViaje(v);
      setAsientos(a);
      // Si alguien compro un asiento que yo tenia marcado, lo quito de mi seleccion.
      setSeleccion((sel) => sel.filter((s) => !a.find((x) => x.idAsiento === s)?.ocupado));
    } catch (e) {
      setError(e.message);
    }
  }, [id]);

  useEffect(() => {
    cargar();
  }, [cargar]);

  const alternar = (a) => {
    if (a.ocupado) return;
    setSeleccion((sel) => (sel.includes(a.idAsiento) ? sel.filter((s) => s !== a.idAsiento) : [...sel, a.idAsiento]));
  };

  const cambiar = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const pagar = async (e) => {
    e.preventDefault();
    setError("");
    setProcesando(true);
    try {
      const comprobante = await comprar({
        idViaje: Number(id),
        idAsientos: seleccion,
        ...form,
        numeroTarjeta: form.numeroTarjeta.replace(/\s/g, ""),
      });
      navigate(`/comprobante/${comprobante.idVenta}`);
    } catch (err) {
      setError(err.message);
      cargar(); // refresca por si el error fue de asiento ocupado
    } finally {
      setProcesando(false);
    }
  };

  if (!viaje) return <div className="text-center">{error ? <div className="alert alert-danger">{error}</div> : "Cargando..."}</div>;

  const total = seleccion.length * Number(viaje.precio);
  // 2 columnas, pasillo, 2 columnas
  const filas = [];
  for (let i = 0; i < asientos.length; i += 4) filas.push(asientos.slice(i, i + 4));
  const boton = (a) => (
    <button key={a.idAsiento} type="button"
            className={"btn asiento " + (a.ocupado ? "btn-secondary" : seleccion.includes(a.idAsiento) ? "btn-primary" : "btn-outline-primary")}
            disabled={a.ocupado} onClick={() => alternar(a)}>
      {a.numero}
    </button>
  );

  return (
    <>
      <h1 className="h4">{viaje.ciudadOrigen} → {viaje.ciudadDestino}</h1>
      <p className="text-muted">Sale: {formatearFecha(viaje.fechaSalida)} · Bus {viaje.placa} · S/ {Number(viaje.precio).toFixed(2)} por asiento</p>
      {error && <div className="alert alert-danger">{error}</div>}

      <div className="row g-4">
        <div className="col-md-6">
          <h2 className="h6">1. Elige tus asientos (max. 6)</h2>
          <div className="border rounded p-3 d-inline-block">
            {filas.map((fila, i) => (
              <div className="d-flex gap-2 mb-2" key={i}>
                {fila.slice(0, 2).map(boton)}
                <div className="pasillo" />
                {fila.slice(2).map(boton)}
              </div>
            ))}
          </div>
          <p className="small text-muted mt-2">Gris = ocupado · Azul = tu seleccion</p>
        </div>

        <div className="col-md-6">
          <h2 className="h6">2. Datos y pago</h2>
          <form onSubmit={pagar}>
            <input className="form-control mb-2" name="nombrePasajero" placeholder="Nombre del pasajero" required
                   value={form.nombrePasajero} onChange={cambiar} />
            <input className="form-control mb-2" name="titularTarjeta" placeholder="Titular de la tarjeta" required
                   value={form.titularTarjeta} onChange={cambiar} />
            <input className="form-control mb-2" name="numeroTarjeta" placeholder="Numero de tarjeta (13-19 digitos)" required
                   inputMode="numeric" value={form.numeroTarjeta} onChange={cambiar} />
            <p className="small text-muted">Pasarela simulada: cualquier tarjeta se aprueba, salvo las que terminan en 0000.</p>
            <p className="fs-5">Total: <strong>S/ {total.toFixed(2)}</strong> ({seleccion.length} asiento/s)</p>
            <button className="btn btn-success w-100" disabled={seleccion.length === 0 || procesando}>
              {procesando ? "Procesando pago..." : "Pagar y confirmar"}
            </button>
          </form>
        </div>
      </div>
    </>
  );
}
