import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/authService";
import { useAuth } from "../context/AuthContext";

/**
 * Vista de registro (RF1). Replica la misma estructura de Login.jsx:
 * useState para el formulario + authService + useAuth para loguear
 * automaticamente al usuario apenas se crea la cuenta.
 */
export default function Register() {
  const [form, setForm] = useState({
    nombreUsuario: "",
    email: "",
    contrasena: "",
    confirmarContrasena: "",
  });
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

    // Validacion de confirmacion de contrasena en el cliente; las reglas de
    // negocio "de verdad" (email/usuario duplicado, longitud minima) las
    // valida el backend y llegan como mensaje de error si fallan.
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
    <div className="d-flex justify-content-center align-items-center min-vh-100 bg-light">
      <div className="card shadow-sm" style={{ width: "100%", maxWidth: 420 }}>
        <div className="card-body p-4">
          <h1 className="h4 mb-1 text-center">Crear cuenta</h1>
          <p className="text-muted text-center mb-4">Registrate en BusTracka</p>

          {error && (
            <div className="alert alert-danger py-2" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            <div className="mb-3">
              <label htmlFor="nombreUsuario" className="form-label">
                Nombre de usuario
              </label>
              <input
                type="text"
                className="form-control"
                id="nombreUsuario"
                name="nombreUsuario"
                placeholder="Juan Perez"
                value={form.nombreUsuario}
                onChange={handleChange}
                required
                minLength={3}
              />
            </div>

            <div className="mb-3">
              <label htmlFor="email" className="form-label">
                Correo electronico
              </label>
              <input
                type="email"
                className="form-control"
                id="email"
                name="email"
                placeholder="nombre@correo.com"
                value={form.email}
                onChange={handleChange}
                required
                autoComplete="email"
              />
            </div>

            <div className="mb-3">
              <label htmlFor="contrasena" className="form-label">
                Contrasena
              </label>
              <input
                type="password"
                className="form-control"
                id="contrasena"
                name="contrasena"
                placeholder="Minimo 8 caracteres"
                value={form.contrasena}
                onChange={handleChange}
                required
                minLength={8}
                autoComplete="new-password"
              />
            </div>

            <div className="mb-3">
              <label htmlFor="confirmarContrasena" className="form-label">
                Confirmar contrasena
              </label>
              <input
                type="password"
                className="form-control"
                id="confirmarContrasena"
                name="confirmarContrasena"
                value={form.confirmarContrasena}
                onChange={handleChange}
                required
                minLength={8}
                autoComplete="new-password"
              />
            </div>

            <button type="submit" className="btn btn-primary w-100" disabled={cargando}>
              {cargando ? "Creando cuenta..." : "Registrarme"}
            </button>
          </form>

          <p className="text-center mt-3 mb-0 small">
            Ya tienes cuenta? <Link to="/login">Inicia sesion</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
