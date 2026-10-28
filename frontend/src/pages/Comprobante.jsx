import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { obtenerComprobante } from "../api/ventaService";
import { formatearFecha } from "./BuscarViajes";

/** Sprint 4: comprobante con codigo QR y enlace de seguimiento. */
export default function Comprobante() {
  const { id } = useParams();
  const [c, setC] = useState(null);
  const [error, setError] = useState("");
  const [copiado, setCopiado] = useState(false);

  useEffect(() => {
    obtenerComprobante(id).then(setC).catch((e) => setError(e.message));
  }, [id]);

  if (error) return <div className="alert alert-danger">{error}</div>;
  if (!c) return <div className="text-center">Cargando...</div>;

  const copiar = async () => {
    try {
      await navigator.clipboard.writeText(c.enlaceSeguimiento);
      setCopiado(true);
    } catch {
      window.prompt("Copia el enlace:", c.enlaceSeguimiento);
    }
  };

  return (
    <div className="card mx-auto" style={{ maxWidth: 560 }}>
      <div className="card-body">
        <div className="alert alert-success py-2">Compra confirmada. Te enviamos el comprobante por correo.</div>
        <h1 className="h5">Comprobante {c.codigoComprobante}</h1>
        <p className="mb-1"><strong>{c.ciudadOrigen} → {c.ciudadDestino}</strong></p>
        <p className="mb-1">Sale: {formatearFecha(c.fechaSalida)} · Bus {c.placa}</p>
        <ul className="mb-2">
          {c.asientos.map((a) => (
            <li key={a.numero}>Asiento {a.numero} · {a.nombrePasajero} · S/ {Number(a.precio).toFixed(2)}</li>
          ))}
        </ul>
        <p className="mb-1"><strong>Total: S/ {Number(c.total).toFixed(2)}</strong> ({c.estado})</p>
        <p className="small text-muted">Pago {c.codigoPago} · {formatearFecha(c.fechaCompra)}</p>
        <div className="text-center my-3">
          <img src={`data:image/png;base64,${c.qrBase64}`} alt="Codigo QR del comprobante" width="180" height="180" />
        </div>
        <div className="d-flex flex-wrap gap-2 justify-content-center">
          <a className="btn btn-primary btn-sm" href={c.enlaceSeguimiento} target="_blank" rel="noreferrer">Seguir mi bus</a>
          <button className="btn btn-outline-secondary btn-sm" onClick={copiar}>
            {copiado ? "Enlace copiado" : "Copiar enlace de seguimiento"}
          </button>
          <button className="btn btn-outline-secondary btn-sm" onClick={() => window.print()}>Imprimir</button>
          <Link className="btn btn-link btn-sm" to="/compras">Mis compras</Link>
        </div>
      </div>
    </div>
  );
}
