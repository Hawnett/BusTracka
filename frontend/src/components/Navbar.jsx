import { NavLink } from "react-router-dom";
import Logo from "./Logo";

const ENLACES = [
  { numero: "01", etiqueta: "Buscar viajes", icono: "bi-search", to: "/", end: true },
  { numero: "02", etiqueta: "Mis compras", icono: "bi-ticket-perforated", to: "/compras" },
  { numero: "03", etiqueta: "Seguimiento", icono: "bi-geo-alt", to: "/seguimiento" },
];

export default function Navbar({ nombreUsuario, onCerrarSesion }) {
  const inicial = nombreUsuario ? nombreUsuario.charAt(0).toUpperCase() : "?";
  const clase = ({ isActive }) => "bt-nav-link" + (isActive ? " is-active" : "");

  return (
    <>
      <header className="bt-navbar sticky-top">
        <nav className="container d-flex align-items-center justify-content-between gap-3" aria-label="Navegación principal">
          <Logo className="py-2" />

          <ul className="bt-nav-links list-unstyled mb-0">
            {ENLACES.map((e) => (
              <li key={e.to}>
                <NavLink to={e.to} end={e.end} className={clase}>
                  <span className="bt-nav-num" aria-hidden="true">{e.numero}</span>
                  {e.etiqueta}
                </NavLink>
              </li>
            ))}
          </ul>

          <div className="d-flex align-items-center gap-2 py-2">
            <span className="bt-user">
              <span className="bt-avatar" aria-hidden="true">{inicial}</span>
              <span className="d-none d-sm-inline">{nombreUsuario}</span>
            </span>
            <button type="button" className="bt-logout" onClick={onCerrarSesion} title="Cerrar sesión">
              <i className="bi bi-box-arrow-right" aria-hidden="true"></i>
              <span className="visually-hidden">Cerrar sesión</span>
            </button>
          </div>
        </nav>
      </header>

      <nav className="bt-tabbar" aria-label="Navegación móvil">
        {ENLACES.map((e) => (
          <NavLink key={e.to} to={e.to} end={e.end} className={({ isActive }) => (isActive ? "is-active" : "")}>
            <i className={`bi ${e.icono}`} aria-hidden="true"></i>
            {e.etiqueta}
          </NavLink>
        ))}
      </nav>
    </>
  );
}
