import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/authService";
import { useAuth } from "../context/AuthContext";
import AuthLayout from "../components/AuthLayout";
import Alerta from "../components/Alerta";
import Campo from "../components/Campo";

/**
 * Vista de inicio de sesion (RF2). Componente funcional con Hooks
 * (useState para el formulario, useAuth para persistir la sesion global).
 */
export default function Login() {
  const [form, setForm] = useState({ email: "", contrasena: "" });
  const [error, setError] = useState("");
  const [cargando, setCargando] = useState(false);
  const { iniciarSesion } = useAuth();
  const navigate = useNavigate();

  function handleChange(e) {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setCargando(true);
    try {
      const respuesta = await login(form);
      iniciarSesion(respuesta);
      navigate("/", { replace: true });
    } catch (err) {
      setError(err.message);
    } finally {
      setCargando(false);
    }
  }

  return (
    <AuthLayout
      anden="07"
      eyebrow="Iniciar sesión"
      titulo="Bienvenido de vuelta"
      subtitulo="Ingresa para buscar viajes, comprar pasajes y seguir tu bus."
      pie={
        <>
          ¿Aún no tienes cuenta? <Link to="/registro" className="bt-link">Crear cuenta</Link>
        </>
      }
    >
      <Alerta tipo="error">{error}</Alerta>

      <form onSubmit={handleSubmit} noValidate className="d-grid gap-3">
        <Campo id="email" name="email" type="email" etiqueta="Correo electrónico" icono="bi-envelope"
               placeholder="nombre@correo.com" autoComplete="email" value={form.email} onChange={handleChange} required />
        <Campo id="contrasena" name="contrasena" type="password" etiqueta="Contraseña" icono="bi-lock"
               placeholder="••••••••" autoComplete="current-password" value={form.contrasena} onChange={handleChange} required
               accionEtiqueta={<Link to="/recuperar" className="bt-link small">¿La olvidaste?</Link>} />
        <button type="submit" className="bt-btn bt-btn-primary w-100 mt-2" disabled={cargando}>
          {cargando ? "Ingresando…" : "Ingresar"}
          <i className="bi bi-arrow-right" aria-hidden="true"></i>
        </button>
      </form>
    </AuthLayout>
  );
}
