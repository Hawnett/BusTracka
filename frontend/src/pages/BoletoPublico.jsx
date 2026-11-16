import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { validarBoleto } from "../api/ventaService";
import EstadoPasaje from "../components/EstadoPasaje";
import Logo from "../components/Logo";
import { formatearFecha } from "../utils/formato";

/** Pagina a la que lleva el QR del boleto: muestra si es valido para abordar (HU 4.3). */
export default function BoletoPublico() {
  const { codigo } = useParams();
  const [boleto, setBoleto] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    validarBoleto(codigo).then(setBoleto).catch((e) => setError(e.message));
  }, [codigo]);

  return (
    <div className="d-flex flex-column min-vh-100">
      <header className="bt-public-head">
        <div className="container d-flex justify-content-between align-items-center gap-3 py-2">
          <Logo to="/" />
          <span className="bt-tag bt-tag--ink">
            <i className="bi bi-qr-code-scan" aria-hidden="true"></i>
            Validación de boleto
          </span>
        </div>
      </header>

      <main className="container py-4 py-md-5 flex-grow-1">
        <div className="bt-comprobante-wrap">
          {error && (
            <div className="bt-empty">
              <div className="bt-pictograma" aria-hidden="true"><i className="bi bi-x-lg"></i></div>
              <h1 className="h2 mb-2">Boleto no encontrado</h1>
              <p className="bt-muted mb-0">El código {codigo} no corresponde a ningún pasaje.</p>
            </div>
          )}

          {boleto && (
            <article className="bt-comprobante" aria-label={`Boleto ${boleto.codigoComprobante}`}>
              <header className="bt-comprobante-head">
                <span className="bt-code">{boleto.codigoComprobante}</span>
                <EstadoPasaje estado={boleto.estadoPasaje} />
              </header>
              <div className="bt-comprobante-cuerpo text-center">
                <p className={`bt-validez ${boleto.valido ? "is-valido" : "is-invalido"} my-3`} role="status">
                  <i className={`bi ${boleto.valido ? "bi-check-circle" : "bi-x-circle"}`} aria-hidden="true"></i>
                  {boleto.valido ? "Válido" : "No válido"}
                </p>
                <dl className="bt-filas text-start">
                  <div className="bt-fila"><dt>Pasajero</dt><dd>{boleto.nombrePasajero}</dd></div>
                  <div className="bt-fila"><dt>Ruta</dt><dd>{boleto.ciudadOrigen} → {boleto.ciudadDestino}</dd></div>
                  <div className="bt-fila"><dt>Salida</dt><dd>{formatearFecha(boleto.salidaEstimada)}</dd></div>
                  <div className="bt-fila"><dt>Terminal</dt><dd>{boleto.terminalOrigen}</dd></div>
                  <div className="bt-fila"><dt>Bus</dt><dd><span className="bt-plate">{boleto.placa}</span></dd></div>
                  <div className="bt-fila"><dt>Asientos</dt><dd>{boleto.asientos.join(", ")}</dd></div>
                </dl>
              </div>
            </article>
          )}
        </div>
      </main>

      <footer className="bt-public-foot">
        Con tecnología de <strong>BusTracka</strong>
      </footer>
    </div>
  );
}
