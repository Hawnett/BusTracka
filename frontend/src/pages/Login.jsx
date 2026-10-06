import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { login } from "../api/authService";
import { useAuth } from "../context/AuthContext";

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
    <div className="d-flex justify-content-center align-items-center min-vh-100 bg-light">
      <div className="card shadow-sm" style={{ width: "100%", maxWidth: 420 }}>
        <div className="card-body p-4">
          <h1 className="h4 mb-1 text-center">BusTracka</h1>
          <p className="text-muted text-center mb-4">Inicia sesion para continuar</p>

          {error && (
            <div className="alert alert-danger py-2" role="alert">
              {error}
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
                name="email"
                placeholder="nombre@correo.com"
                value={form.email}
                onChange={handleChange}
                required
                autoComplete="email"
              />
            </div>

            <div className="mb-2">
              <label htmlFor="contrasena" className="form-label">
                Contrasena
              </label>
              <input
                type="password"
                className="form-control"
                id="contrasena"
                name="contrasena"
                placeholder="********"
                value={form.contrasena}
                onChange={handleChange}
                required
                autoComplete="current-password"
              />
            </div>

            <div className="d-flex justify-content-end mb-3">
              <Link to="/recuperar" className="small">
                Olvidaste tu contrasena?
              </Link>
            </div>

            <button type="submit" className="btn btn-primary w-100" disabled={cargando}>
              {cargando ? "Ingresando..." : "Iniciar sesion"}
            </button>
          </form>

          <p className="text-center mt-3 mb-0 small">
            No tienes cuenta? <Link to="/registro">Registrate</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
