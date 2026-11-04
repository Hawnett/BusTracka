import { Navigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

const ID_ROL_ADMINISTRADOR = 1;

/** Solo el administrador entra (el backend igual valida el rol en /api/admin). */
export default function RutaAdmin({ children }) {
  const { usuario } = useAuth();
  return usuario?.idRol === ID_ROL_ADMINISTRADOR ? children : <Navigate to="/" replace />;
}
