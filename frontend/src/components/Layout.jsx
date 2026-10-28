import { Link, NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

/** Barra de navegacion + contenedor comun de todas las pantallas privadas. */
export default function Layout() {
  const { usuario, cerrarSesion } = useAuth();
  const claseLink = ({ isActive }) => "nav-link" + (isActive ? " active fw-semibold" : "");

  return (
    <>
      <nav className="navbar navbar-expand-md navbar-dark bg-primary">
        <div className="container">
          <Link className="navbar-brand" to="/">BusTracka</Link>
          <div className="navbar-nav me-auto">
            <NavLink className={claseLink} to="/" end>Buscar viajes</NavLink>
            <NavLink className={claseLink} to="/seguimiento">Seguimiento</NavLink>
            <NavLink className={claseLink} to="/compras">Mis compras</NavLink>
          </div>
          <span className="navbar-text me-3">{usuario?.nombreUsuario}</span>
          <button className="btn btn-outline-light btn-sm" onClick={cerrarSesion}>Cerrar sesion</button>
        </div>
      </nav>
      <main className="container py-4">
        <Outlet />
      </main>
    </>
  );
}
