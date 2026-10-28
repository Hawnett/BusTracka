import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/authService";
import { useAuth } from "../context/AuthContext";
import AuthLayout from "../components/AuthLayout";
import Alerta from "../components/Alerta";
import Campo from "../components/Campo";

/**
 * Vista de registro (RF1). Replica la misma estructura de Login.jsx:
 * useState para el formulario + authService + useAuth para loguear
 * automaticamente al usuario apenas se crea la cuenta.
 */
export default function Register() {
  const [form, setForm] = useState({ nombreUsuario: "", email: "", contrasena: "", confirmarContrasena: "" });
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

    // Confirmacion de contrasena en el cliente; las reglas de negocio "de verdad"
    // (email duplicado, longitud minima) las valida el backend.
    if (form.contrasena !== form.confirmarContrasena) {
      setError("Las contrasenas no coinciden");
      return;
    }

    setCargando(true);
    try {
      const respuesta = await register({
        nombreUsuario: form.nombreUsuario,
        email: form.email,
        contrasena: form.contrasena,
      });
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
      anden="12"
      eyebrow="Crear cuenta"
      titulo="Tu próximo viaje empieza aquí"
      subtitulo="Regístrate en segundos y guarda tus boletos en un solo lugar."
      pie={
        <>
          ¿Ya tienes cuenta? <Link to="/login" className="bt-link">Inicia sesión</Link>
        </>
      }
    >
      <Alerta tipo="error">{error}</Alerta>

      <form onSubmit={handleSubmit} noValidate className="d-grid gap-3">
        <Campo id="nombreUsuario" name="nombreUsuario" etiqueta="Nombre de usuario" icono="bi-person"
               placeholder="Ej. mariaquispe" autoComplete="username" value={form.nombreUsuario} onChange={handleChange} required />
        <Campo id="email" name="email" type="email" etiqueta="Correo electrónico" icono="bi-envelope"
               placeholder="nombre@correo.com" autoComplete="email" value={form.email} onChange={handleChange} required />
        <Campo id="contrasena" name="contrasena" type="password" etiqueta="Contraseña" icono="bi-lock"
               placeholder="Mínimo 8 caracteres" autoComplete="new-password" value={form.contrasena} onChange={handleChange} required />
        <Campo id="confirmarContrasena" name="confirmarContrasena" type="password" etiqueta="Confirmar contraseña" icono="bi-lock-fill"
               placeholder="Repite tu contraseña" autoComplete="new-password" value={form.confirmarContrasena} onChange={handleChange} required />
        <button type="submit" className="bt-btn bt-btn-primary w-100 mt-2" disabled={cargando}>
          {cargando ? "Creando cuenta…" : "Crear cuenta"}
          <i className="bi bi-arrow-right" aria-hidden="true"></i>
        </button>
      </form>
    </AuthLayout>
  );
}
