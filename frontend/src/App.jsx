import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import RutaPrivada from "./routes/RutaPrivada";
import RutaAdmin from "./routes/RutaAdmin";
import Layout from "./components/Layout";
import Login from "./pages/Login";
import Register from "./pages/Register";
import RecoverPassword from "./pages/RecoverPassword";
import BuscarViajes from "./pages/BuscarViajes";
import DetalleViaje from "./pages/DetalleViaje";
import Compra from "./pages/Compra";
import Comprobante from "./pages/Comprobante";
import MisCompras from "./pages/MisCompras";
import DetalleCompra from "./pages/DetalleCompra";
import CompartirViaje from "./pages/CompartirViaje";
import Seguimiento from "./pages/Seguimiento";
import SeguimientoPublico from "./pages/SeguimientoPublico";
import BoletoPublico from "./pages/BoletoPublico";
import Terminales from "./pages/Terminales";
import Operaciones from "./pages/Operaciones";

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
          <Route path="/boleto/:codigo" element={<BoletoPublico />} />

          {/* Privadas: exigen sesion, comparten la barra de navegacion */}
          <Route element={<RutaPrivada><Layout /></RutaPrivada>}>
            <Route path="/" element={<BuscarViajes />} />
            <Route path="/viajes/:id" element={<Compra />} />
            <Route path="/viajes/:id/detalle" element={<DetalleViaje />} />
            <Route path="/comprobante/:id" element={<Comprobante />} />
            <Route path="/compras" element={<MisCompras />} />
            <Route path="/compras/:id" element={<DetalleCompra />} />
            <Route path="/compras/:id/compartir" element={<CompartirViaje />} />
            <Route path="/seguimiento" element={<Seguimiento />} />
            <Route path="/terminales" element={<Terminales />} />
            <Route path="/operaciones" element={<RutaAdmin><Operaciones /></RutaAdmin>} />
          </Route>

          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}
