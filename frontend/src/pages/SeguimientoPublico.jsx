import { useCallback, useState } from "react";
import { useParams } from "react-router-dom";
import { obtenerSeguimientoCompartido } from "../api/seguimientoService";
import VistaSeguimiento from "../components/VistaSeguimiento";
import Logo from "../components/Logo";
import { formatearFecha } from "../utils/formato";

/** Enlace compartido: el familiar ve el viaje sin iniciar sesion (Release 2, Sprint 3). */
export default function SeguimientoPublico() {
  const { token } = useParams();
  const [compartido, setCompartido] = useState(null);
  const [invalido, setInvalido] = useState("");

  const cargar = useCallback(async () => {
    try {
      const datos = await obtenerSeguimientoCompartido(token);
      setCompartido(datos);
      return datos.ubicacion;
    } catch (e) {
      if (e.status === 410) setInvalido(e.message);
      throw e;
    }
  }, [token]);

  return (
    <div className="d-flex flex-column min-vh-100">
      <header className="bt-public-head">
        <div className="container d-flex justify-content-between align-items-center gap-3 py-2">
          <Logo to="/" />
          <span className="bt-tag bt-tag--ink">
            <i className="bi bi-people" aria-hidden="true"></i>
            Seguimiento compartido
          </span>
        </div>
      </header>

      <main className="container py-4 py-md-5 flex-grow-1">
        {invalido ? (
          <div className="bt-empty">
            <div className="bt-pictograma" aria-hidden="true"><i className="bi bi-link-45deg"></i></div>
            <h1 className="h2 mb-2">Enlace inválido</h1>
            <p className="bt-muted mb-0">Este enlace de seguimiento no existe o el viaje ya terminó. Pide uno nuevo a quien te lo compartió.</p>
          </div>
        ) : (
          <>
            <div className="bt-section-head">
              <div>
                <p className="bt-eyebrow mb-0">En vivo</p>
                <h1>{compartido ? `Viaje de ${compartido.nombrePasajero}` : "Sigue este viaje en vivo"}</h1>
              </div>
            </div>
            {compartido && (
              <p className="bt-muted mb-4">
                Pasaje <span className="bt-code">{compartido.codigoComprobante}</span> · Asientos {compartido.asientos.join(", ")}.
                {" "}No necesitas iniciar sesión. El enlace vence el {formatearFecha(compartido.expiraEn)}.
              </p>
            )}
            <VistaSeguimiento cargarUbicacion={cargar} />
            <p className="small bt-muted text-center mt-3 mb-0">Esta página se actualiza automáticamente.</p>
          </>
        )}
      </main>

      <footer className="bt-public-foot">
        Con tecnología de <strong>BusTracka</strong>
      </footer>
    </div>
  );
}
