import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/** Bloquea el acceso a rutas que requieren sesion activa (ej. "/"). */
export default function RutaPrivada({ children }) {
  const { estaAutenticado, cargando } = useAuth();

  if (cargando) {
    return (
      <div className="d-flex justify-content-center align-items-center min-vh-100">
        <div className="spinner-border text-primary" role="status" />
      </div>
    );
  }

  return estaAutenticado ? children : <Navigate to="/login" replace />;
}
