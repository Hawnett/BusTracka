import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { confirmarRecuperacion, solicitarRecuperacion } from "../api/authService";
import AuthLayout from "../components/AuthLayout";
import Alerta from "../components/Alerta";
import Campo from "../components/Campo";

/**
 * Vista de recuperacion de contrasena (RF3), con dos pasos en un solo
 * componente:
 *  1) Sin "token" en la URL: formulario para pedir el enlace (email).
 *  2) Con "?token=..." en la URL (el link "enviado" por el backend):
 *     formulario para definir la nueva contrasena.
 */
export default function RecoverPassword() {
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const pie = (
    <Link to="/login" className="bt-link">
      <i className="bi bi-arrow-left me-1" aria-hidden="true"></i>Volver a iniciar sesión
    </Link>
  );

  return token ? (
    <AuthLayout anden="03" eyebrow="Nueva contraseña" titulo="Define tu nueva contraseña"
                subtitulo="Elige una contraseña de al menos 8 caracteres." pie={pie}>
      <FormularioNuevaContrasena token={token} />
    </AuthLayout>
  ) : (
    <AuthLayout anden="03" eyebrow="Recuperar contraseña" titulo="¿Olvidaste tu contraseña?"
                subtitulo="Escribe tu correo y te enviaremos un enlace para crear una nueva." pie={pie}>
      <FormularioSolicitarEnlace />
    </AuthLayout>
  );
}

function FormularioSolicitarEnlace() {
  const [email, setEmail] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);
  const [enlaceDemo, setEnlaceDemo] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setMensaje("");
    setCargando(true);
    try {
      const data = await solicitarRecuperacion(email);
      setMensaje(data.mensaje);
      // El backend simula el envio de correo; exponemos el enlace en pantalla
      // solo para poder probar el flujo de punta a punta sin servidor SMTP.
      setEnlaceDemo(data.enlaceDemo);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <>
      <Alerta tipo="error">{error}</Alerta>
      <Alerta tipo="exito">{mensaje}</Alerta>
      {enlaceDemo && (
        <p className="small bt-muted">
          Enlace de prueba (simula el correo enviado): <a className="bt-link" href={enlaceDemo}>{enlaceDemo}</a>
        </p>
      )}

      <form onSubmit={handleSubmit} noValidate className="d-grid gap-3">
        <Campo id="email" name="email" type="email" etiqueta="Correo electrónico" icono="bi-envelope"
               placeholder="nombre@correo.com" autoComplete="email" value={email}
               onChange={(e) => setEmail(e.target.value)} required />
        <button type="submit" className="bt-btn bt-btn-primary w-100 mt-2" disabled={cargando}>
          {cargando ? "Enviando…" : "Enviar enlace"}
          <i className="bi bi-send" aria-hidden="true"></i>
        </button>
      </form>
    </>
  );
}

function FormularioNuevaContrasena({ token }) {
  const [nuevaContrasena, setNuevaContrasena] = useState("");
  const [confirmar, setConfirmar] = useState("");
  const [mensaje, setMensaje] = useState("");
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setMensaje("");

    if (nuevaContrasena !== confirmar) {
      setError("Las contrasenas no coinciden");
      return;
    }

    setCargando(true);
    try {
      const data = await confirmarRecuperacion({ token, nuevaContrasena });
      setMensaje(data.mensaje);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <>
      <Alerta tipo="error">{error}</Alerta>
      <Alerta tipo="exito">{mensaje}</Alerta>

      <form onSubmit={handleSubmit} noValidate className="d-grid gap-3">
        <Campo id="nuevaContrasena" name="nuevaContrasena" type="password" etiqueta="Nueva contraseña" icono="bi-lock"
               placeholder="Mínimo 8 caracteres" autoComplete="new-password" value={nuevaContrasena}
               onChange={(e) => setNuevaContrasena(e.target.value)} required />
        <Campo id="confirmar" name="confirmar" type="password" etiqueta="Confirmar contraseña" icono="bi-lock-fill"
               placeholder="Repite tu contraseña" autoComplete="new-password" value={confirmar}
               onChange={(e) => setConfirmar(e.target.value)} required />
        <button type="submit" className="bt-btn bt-btn-primary w-100 mt-2" disabled={cargando}>
          {cargando ? "Guardando…" : "Actualizar contraseña"}
          <i className="bi bi-check2" aria-hidden="true"></i>
        </button>
      </form>
    </>
  );
}
