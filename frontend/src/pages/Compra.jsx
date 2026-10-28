import { useCallback, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { listarAsientos, obtenerViaje } from "../api/viajeService";
import { comprar } from "../api/ventaService";
import { formatearFecha } from "./BuscarViajes";
import Alerta from "../components/Alerta";
import Campo from "../components/Campo";
import MapaAsientos from "../components/MapaAsientos";

const MAX_ASIENTOS = 6;

/** Sprint 2 (mapa de asientos) + Sprint 4 (pago): una sola pantalla en pasos. */
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
    setSeleccion((sel) => {
      if (sel.includes(a.idAsiento)) return sel.filter((s) => s !== a.idAsiento);
      return sel.length >= MAX_ASIENTOS ? sel : [...sel, a.idAsiento];
    });
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

  if (!viaje) {
    return (
      <div className="container py-5">
        {error ? <Alerta tipo="error">{error}</Alerta> : <p className="bt-muted">Cargando…</p>}
      </div>
    );
  }

  const total = (seleccion.length * Number(viaje.precio)).toFixed(2);
  const numerosElegidos = asientos.filter((a) => seleccion.includes(a.idAsiento)).map((a) => a.numero);
  const fechaSalida = new Date(viaje.fechaSalida);
  const horaSalida = fechaSalida.toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit", hour12: false });
  const sinSeleccion = seleccion.length === 0;

  return (
    <div className="container py-4 py-md-5">
      <Link to="/" className="bt-link small d-inline-flex align-items-center gap-1 mb-3">
        <i className="bi bi-arrow-left" aria-hidden="true"></i> Volver a resultados
      </Link>
      <div className="bt-section-head">
        <div>
          <p className="bt-eyebrow mb-0">Compra de pasaje</p>
          <h1>Completa tu compra</h1>
        </div>
      </div>
      <p className="bt-muted mb-4">Elige tus asientos (máx. {MAX_ASIENTOS}), ingresa los datos del pasajero y paga.</p>

      <form onSubmit={pagar} noValidate>
        <div className="row g-4">
          <div className="col-12 col-lg-7 d-grid gap-4">
            <section className="bt-ficha" aria-labelledby="titulo-asientos">
              <h2 id="titulo-asientos" className="bt-ficha-head">
                <span className="bt-step">1</span> Elige tus asientos
              </h2>
              <div className="bt-ficha-body">
                <MapaAsientos asientos={asientos} seleccion={seleccion} onAlternar={alternar} />
              </div>
            </section>

            <section className="bt-ficha" aria-labelledby="titulo-pasajero">
              <h2 id="titulo-pasajero" className="bt-ficha-head">
                <span className="bt-step">2</span> Datos del pasajero
              </h2>
              <div className="bt-ficha-body">
                <div className="row g-3">
                  <Campo className="col-12" id="nombrePasajero" name="nombrePasajero" etiqueta="Nombre completo"
                         icono="bi-person" placeholder="Nombre del pasajero" autoComplete="name"
                         value={form.nombrePasajero} onChange={cambiar} required />
                </div>
              </div>
            </section>

            <section className="bt-ficha" aria-labelledby="titulo-pago">
              <h2 id="titulo-pago" className="bt-ficha-head">
                <span className="bt-step">3</span> Pago con tarjeta
                <i className="bi bi-credit-card-2-front ms-auto fs-4" aria-hidden="true"></i>
              </h2>
              <div className="bt-ficha-body">
                <div className="row g-3">
                  <Campo className="col-12" id="titularTarjeta" name="titularTarjeta" etiqueta="Titular de la tarjeta"
                         icono="bi-person-badge" placeholder="Nombre impreso en la tarjeta" autoComplete="cc-name"
                         value={form.titularTarjeta} onChange={cambiar} required />
                  <Campo className="col-12" id="numeroTarjeta" name="numeroTarjeta" etiqueta="Número de tarjeta (13-19 dígitos)"
                         icono="bi-credit-card" placeholder="0000 0000 0000 0000" autoComplete="cc-number"
                         inputMode="numeric" maxLength={23} value={form.numeroTarjeta} onChange={cambiar} required />
                </div>
                <p className="small bt-muted mt-3 mb-0">
                  <i className="bi bi-info-circle me-1" aria-hidden="true"></i>
                  Pasarela simulada: cualquier tarjeta se aprueba, salvo las que terminan en 0000.
                </p>
              </div>
            </section>
          </div>

          <div className="col-12 col-lg-5">
            <aside className="bt-resumen" aria-labelledby="titulo-resumen">
              <div className="bt-resumen-head">
                <p className="bt-eyebrow mb-0">Resumen del viaje</p>
                <h2 id="titulo-resumen">
                  {viaje.ciudadOrigen}
                  <i className="bi bi-arrow-right" aria-label="a"></i>
                  {viaje.ciudadDestino}
                </h2>
              </div>

              <dl className="bt-filas">
                <div className="bt-fila"><dt>Salida</dt><dd>{formatearFecha(viaje.fechaSalida)}</dd></div>
                <div className="bt-fila"><dt>Hora</dt><dd>{horaSalida}</dd></div>
                <div className="bt-fila"><dt>Placa del bus</dt><dd><span className="bt-plate">{viaje.placa}</span></dd></div>
                <div className="bt-fila"><dt>Precio por asiento</dt><dd>S/ {Number(viaje.precio).toFixed(2)}</dd></div>
                <div className="bt-fila">
                  <dt>Asientos</dt>
                  <dd className="d-flex flex-wrap justify-content-end gap-1">
                    {numerosElegidos.length === 0 ? (
                      <span className="bt-muted fw-normal">Ninguno aún</span>
                    ) : (
                      numerosElegidos.map((n) => <span key={n} className="bt-seat-tag">{n}</span>)
                    )}
                  </dd>
                </div>
              </dl>

              <div className="bt-perfo bt-resumen-pago">
                <div className="bt-total">
                  <span className="bt-label mb-0">Total a pagar</span>
                  <span className="bt-precio"><small>S/</small>{total}</span>
                </div>

                <div className="mt-3">
                  <Alerta tipo="error">{error}</Alerta>
                </div>

                <button type="submit" className="bt-btn bt-btn-ink w-100 py-3" disabled={sinSeleccion || procesando}>
                  <i className="bi bi-lock-fill" aria-hidden="true"></i>
                  {procesando ? "Procesando pago…" : `Pagar S/ ${total}`}
                </button>
              </div>
            </aside>
          </div>
        </div>
      </form>
    </div>
  );
}
