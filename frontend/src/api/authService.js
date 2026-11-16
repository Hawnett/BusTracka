import axios from "axios";

/**
 * Capa de servicios HTTP: centraliza TODAS las peticiones al backend de
 * autenticacion en un solo lugar. Los componentes (Login, Register,
 * RecoverPassword) nunca llaman a axios directamente, solo a estas
 * funciones (facilita testear y mantener la app si cambia la URL o
 * la libreria HTTP usada).
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  headers: { "Content-Type": "application/json" },
});

// Interceptor: agrega automaticamente el JWT guardado (si existe) a toda
// peticion saliente, para que las rutas protegidas (ej. /auth/me) funcionen
// sin tener que repetir el header manualmente en cada llamada.
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("bustracka_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Si la sesion vencio o se cerro en el servidor, se vuelve al login en vez de mostrar errores sueltos.
api.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    // Las rutas /auth/* manejan su propio 401 (credenciales invalidas, sesion restaurada en AuthContext).
    const esAuth = error.config?.url?.startsWith("/auth/");
    if (error.response?.status === 401 && !esAuth && localStorage.getItem("bustracka_token")) {
      localStorage.removeItem("bustracka_token");
      window.location.assign("/login");
    }
    return Promise.reject(error);
  }
);

/**
 * Normaliza los errores de axios a un mensaje de texto legible, ya que el
 * backend siempre responde con { status, error, message } (ver
 * ApiErrorResponseDTO / GlobalExceptionHandler en el backend).
 */
export function extraerMensajeError(error) {
  if (error.response?.data?.message) {
    return error.response.data.message;
  }
  if (error.request) {
    return "No se pudo conectar con el servidor. Verifica que el backend este corriendo.";
  }
  return "Ocurrio un error inesperado.";
}

/** Envuelve una llamada axios: devuelve los datos o lanza un Error con mensaje legible y el codigo HTTP. */
export async function llamar(promesa) {
  try {
    const { data } = await promesa;
    return data;
  } catch (error) {
    const e = new Error(extraerMensajeError(error));
    e.status = error.response?.status;
    throw e;
  }
}

export async function login({ email, contrasena }) {
  try {
    const { data } = await api.post("/auth/login", { email, contrasena });
    return data;
  } catch (error) {
    throw new Error(extraerMensajeError(error));
  }
}

export async function register({ nombreUsuario, email, contrasena }) {
  try {
    const { data } = await api.post("/auth/register", { nombreUsuario, email, contrasena });
    return data;
  } catch (error) {
    throw new Error(extraerMensajeError(error));
  }
}

/** Paso 1 de recuperacion: solicita el enlace (el backend lo "envia" simulando un email). */
export async function solicitarRecuperacion(email) {
  try {
    const { data } = await api.post("/auth/recover", { email });
    return data;
  } catch (error) {
    throw new Error(extraerMensajeError(error));
  }
}

/** Paso 2 de recuperacion: confirma la nueva contrasena con el token recibido en el enlace. */
export async function confirmarRecuperacion({ token, nuevaContrasena }) {
  try {
    const { data } = await api.put("/auth/recover", { token, nuevaContrasena });
    return data;
  } catch (error) {
    throw new Error(extraerMensajeError(error));
  }
}

/**
 * Cierra la sesion tambien en el servidor: el token deja de ser aceptado aunque no haya vencido.
 * El token va explicito porque el cliente lo borra de localStorage en el mismo momento.
 */
export const cerrarSesionServidor = (token) =>
  llamar(api.post("/auth/logout", null, { headers: { Authorization: `Bearer ${token}` } }));

export async function obtenerUsuarioActual() {
  try {
    const { data } = await api.get("/auth/me");
    return data;
  } catch (error) {
    throw new Error(extraerMensajeError(error));
  }
}

export default api;
