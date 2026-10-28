import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import RutaPrivada from "./routes/RutaPrivada";
import Layout from "./components/Layout";
import Login from "./pages/Login";
import Register from "./pages/Register";
import RecoverPassword from "./pages/RecoverPassword";
import BuscarViajes from "./pages/BuscarViajes";
import Compra from "./pages/Compra";
import Comprobante from "./pages/Comprobante";
import MisCompras from "./pages/MisCompras";
import Seguimiento from "./pages/Seguimiento";
import SeguimientoPublico from "./pages/SeguimientoPublico";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Publicas */}
          <Route path="/login" element={<Login />} />
          <Route path="/registro" element={<Register />} />
          <Route path="/recuperar" element={<RecoverPassword />} />
          <Route path="/seguimiento/:token" element={<SeguimientoPublico />} />

          {/* Privadas: exigen sesion, comparten la barra de navegacion */}
          <Route element={<RutaPrivada><Layout /></RutaPrivada>}>
            <Route path="/" element={<BuscarViajes />} />
            <Route path="/viajes/:id" element={<Compra />} />
            <Route path="/comprobante/:id" element={<Comprobante />} />
            <Route path="/compras" element={<MisCompras />} />
            <Route path="/seguimiento" element={<Seguimiento />} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
