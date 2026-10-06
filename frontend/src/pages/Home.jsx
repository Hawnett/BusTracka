import { useAuth } from "../context/AuthContext";

/**
 * Pantalla minima post-login, solo para verificar visualmente que la sesion
 * quedo activa (usuario + boton de cerrar sesion). Los sprints siguientes
 * reemplazaran esto por el dashboard real de BusTracka.
 */
export default function Home() {
  const { usuario, cerrarSesion } = useAuth();

  return (
    <div className="container py-5">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1 className="h4 mb-0">Bienvenido a BusTracka</h1>
        <button className="btn btn-outline-danger btn-sm" onClick={cerrarSesion}>
          Cerrar sesion
        </button>
      </div>
      <div className="card">
        <div className="card-body">
          <p className="mb-1">
            <strong>Usuario:</strong> {usuario?.nombreUsuario}
          </p>
          <p className="mb-0">
            <strong>Email:</strong> {usuario?.email}
          </p>
        </div>
      </div>
    </div>
  );
}
