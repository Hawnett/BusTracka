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

/**
 * Normaliza los errores de axios a un mensaje de texto legible, ya que el
 * backend siempre responde con { status, error, message } (ver
 * ApiErrorResponseDTO / GlobalExceptionHandler en el backend).
 */
function extraerMensajeError(error) {
  if (error.response?.data?.message) {
    return error.response.data.message;
  }
  if (error.request) {
    return "No se pudo conectar con el servidor. Verifica que el backend este corriendo.";
  }
  return "Ocurrio un error inesperado.";
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

export async function obtenerUsuarioActual() {
  try {
    const { data } = await api.get("/auth/me");
    return data;
  } catch (error) {
    throw new Error(extraerMensajeError(error));
  }
}

export default api;
