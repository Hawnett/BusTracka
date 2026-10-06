import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { confirmarRecuperacion, solicitarRecuperacion } from "../api/authService";

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

  return (
    <div className="d-flex justify-content-center align-items-center min-vh-100 bg-light">
      <div className="card shadow-sm" style={{ width: "100%", maxWidth: 420 }}>
        <div className="card-body p-4">
          {token ? <FormularioNuevaContrasena token={token} /> : <FormularioSolicitarEnlace />}
          <p className="text-center mt-3 mb-0 small">
            <Link to="/login">Volver a iniciar sesion</Link>
          </p>
        </div>
      </div>
    </div>
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
      // El backend simula el envio de correo; en Sprint 1 exponemos el
      // enlace en pantalla solo para poder probar el flujo de punta a
      // punta sin un servidor SMTP real.
      setEnlaceDemo(data.enlaceDemo);
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <>
      <h1 className="h4 mb-1 text-center">Recuperar contrasena</h1>
      <p className="text-muted text-center mb-4">
        Te enviaremos un enlace para restablecerla
      </p>

      {error && <div className="alert alert-danger py-2">{error}</div>}
      {mensaje && <div className="alert alert-success py-2">{mensaje}</div>}
      {enlaceDemo && (
        <div className="alert alert-info py-2 small">
          Enlace de prueba (simula el correo enviado):{" "}
          <a href={enlaceDemo}>{enlaceDemo}</a>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <div className="mb-3">
          <label htmlFor="email" className="form-label">
            Correo electronico
          </label>
          <input
            type="email"
            className="form-control"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="btn btn-primary w-100" disabled={cargando}>
          {cargando ? "Enviando..." : "Enviar enlace"}
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
      <h1 className="h4 mb-1 text-center">Nueva contrasena</h1>
      <p className="text-muted text-center mb-4">Define tu nueva contrasena</p>

      {error && <div className="alert alert-danger py-2">{error}</div>}
      {mensaje && <div className="alert alert-success py-2">{mensaje}</div>}

      <form onSubmit={handleSubmit} noValidate>
        <div className="mb-3">
          <label htmlFor="nuevaContrasena" className="form-label">
            Nueva contrasena
          </label>
          <input
            type="password"
            className="form-control"
            id="nuevaContrasena"
            value={nuevaContrasena}
            onChange={(e) => setNuevaContrasena(e.target.value)}
            minLength={8}
            required
          />
        </div>
        <div className="mb-3">
          <label htmlFor="confirmar" className="form-label">
            Confirmar contrasena
          </label>
          <input
            type="password"
            className="form-control"
            id="confirmar"
            value={confirmar}
            onChange={(e) => setConfirmar(e.target.value)}
            minLength={8}
            required
          />
        </div>
        <button type="submit" className="btn btn-primary w-100" disabled={cargando}>
          {cargando ? "Guardando..." : "Actualizar contrasena"}
        </button>
      </form>
    </>
  );
}
