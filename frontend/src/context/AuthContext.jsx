import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { obtenerUsuarioActual } from "../api/authService";

const TOKEN_KEY = "bustracka_token";
const AuthContext = createContext(null);

/**
 * Maneja el estado global de sesion con Context API. Se eligio Context +
 * un hook (useAuth) en vez de una libreria externa (Redux/Zustand) porque
 * el estado de auth del Sprint 1 es simple (usuario + token) y no justifica
 * esa dependencia adicional a nivel academico.
 */
export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [cargando, setCargando] = useState(true);

  // Al montar la app, si hay un token guardado, se valida contra el backend
  // (GET /api/auth/me) para restaurar la sesion tras refrescar la pagina.
  useEffect(() => {
    const token = localStorage.getItem(TOKEN_KEY);
    if (!token) {
      setCargando(false);
      return;
    }
    obtenerUsuarioActual()
      .then((datosUsuario) => setUsuario(datosUsuario))
      .catch(() => {
        localStorage.removeItem(TOKEN_KEY);
        setUsuario(null);
      })
      .finally(() => setCargando(false));
  }, []);

  function iniciarSesion(authResponse) {
    // authResponse viene de POST /auth/login o /auth/register (AuthResponseDTO)
    localStorage.setItem(TOKEN_KEY, authResponse.token);
    setUsuario({
      idUsuario: authResponse.idUsuario,
      nombreUsuario: authResponse.nombreUsuario,
      email: authResponse.email,
    });
  }

  /** RF4: Cierre de sesion = invalidar el token en el cliente (JWT sin estado del lado del servidor). */
  function cerrarSesion() {
    localStorage.removeItem(TOKEN_KEY);
    setUsuario(null);
  }

  const valor = useMemo(
    () => ({
      usuario,
      cargando,
      estaAutenticado: !!usuario,
      iniciarSesion,
      cerrarSesion,
    }),
    [usuario, cargando]
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const contexto = useContext(AuthContext);
  if (!contexto) {
    throw new Error("useAuth debe usarse dentro de un <AuthProvider>");
  }
  return contexto;
}
