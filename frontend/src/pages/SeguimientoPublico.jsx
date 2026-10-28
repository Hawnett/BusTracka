import { useCallback } from "react";
import { useParams } from "react-router-dom";
import { obtenerUbicacionPublica } from "../api/seguimientoService";
import VistaSeguimiento from "../components/VistaSeguimiento";
import Logo from "../components/Logo";

/** Enlace compartido del comprobante: funciona sin iniciar sesion. */
export default function SeguimientoPublico() {
  const { token } = useParams();
  const cargar = useCallback(() => obtenerUbicacionPublica(token), [token]);

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
        <div className="bt-section-head">
          <div>
            <p className="bt-eyebrow mb-0">En vivo</p>
            <h1>Sigue este viaje en vivo</h1>
          </div>
        </div>
        <p className="bt-muted mb-4">Alguien compartió contigo la ubicación de su bus.</p>
        <VistaSeguimiento cargarUbicacion={cargar} />
      </main>

      <footer className="bt-public-foot">
        Con tecnología de <strong>BusTracka</strong>
      </footer>
    </div>
  );
}
