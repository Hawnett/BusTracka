import { Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import Navbar from "./Navbar";

/** Barra de navegacion + contenedor comun de todas las pantallas privadas. */
export default function Layout() {
  const { usuario, cerrarSesion } = useAuth();

  return (
    <div className="d-flex flex-column min-vh-100">
      <Navbar nombreUsuario={usuario?.nombreUsuario} onCerrarSesion={cerrarSesion} />
      <main className="bt-main flex-grow-1">
        <Outlet />
      </main>
      <footer className="bt-footer d-none d-md-block">
        <div className="container d-flex justify-content-between gap-3 py-3">
          <span>BusTracka · Terminal de viajes interprovinciales</span>
          <span>Pagos en S/ (pasarela simulada)</span>
        </div>
      </footer>
    </div>
  );
}
