package pe.edu.ulima.is2.bustracka.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.edu.ulima.is2.bustracka.comprobante.GeneradorComprobante;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.dao.UsuarioDao;
import pe.edu.ulima.is2.bustracka.dao.VentaDao;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.BoletoPublicoDTO;
import pe.edu.ulima.is2.bustracka.dto.ComprobanteResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.pago.SolicitudPago;
import pe.edu.ulima.is2.bustracka.dto.CompraRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.EnlaceSeguimientoDTO;
import pe.edu.ulima.is2.bustracka.exception.AsientoNoDisponibleException;
import pe.edu.ulima.is2.bustracka.exception.PagoRechazadoException;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;
import pe.edu.ulima.is2.bustracka.model.Asiento;
import pe.edu.ulima.is2.bustracka.model.DetalleVenta;
import pe.edu.ulima.is2.bustracka.model.Usuario;
import pe.edu.ulima.is2.bustracka.model.Venta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.notificacion.NotificacionService;
import pe.edu.ulima.is2.bustracka.pago.PasarelaPago;
import pe.edu.ulima.is2.bustracka.pago.ResultadoPago;
import pe.edu.ulima.is2.bustracka.qr.GeneradorQr;
import pe.edu.ulima.is2.bustracka.service.EnlaceSeguimientoService;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentaServiceImplTest {
    private VentaDao ventaDao;
    private ViajeDao viajeDao;
    private AsientoDao asientoDao;
    private UsuarioDao usuarioDao;
    private PasarelaPago pasarela;
    private NotificacionService notificacionService;
    private EnlaceSeguimientoService enlaceService;
    private GeneradorQr generadorQr;
    private VentaServiceImpl servicio;
    private Viaje viaje;

    @BeforeEach
    void preparar() {
        ventaDao = spy(new VentaDaoDePrueba());
        viajeDao = mock(ViajeDao.class);
        asientoDao = mock(AsientoDao.class);
        usuarioDao = mock(UsuarioDao.class);
        pasarela = spy(new PasarelaDePrueba());
        notificacionService = spy(new CorreoDePrueba());
        enlaceService = new EnlaceSeguimientoDePrueba();
        generadorQr = spy(new GeneradorQrDePrueba());
        servicio = new VentaServiceImpl(pasarela, notificacionService, generadorQr,
                mock(GeneradorComprobante.class), enlaceService, "http://localhost:5173",
                ventaDao, viajeDao, asientoDao, usuarioDao);
        viaje = viajeConSalida(7);
        when(viajeDao.buscarPorId(9L)).thenReturn(Optional.of(viaje));
        when(asientoDao.listarPorViaje(9L)).thenReturn(List.of(
                new Asiento(11L, 11, false), new Asiento(12L, 12, true)));
        when(usuarioDao.buscarPorId(1L)).thenReturn(Optional.of(
                new Usuario(1L, "ana", "hash", "ana@test.pe", "ACTIVO", 2, ZonaApp.ahora())));
    }

    private Viaje viajeConSalida(int dias) {
        return new Viaje(9L, 1L, 1L, "ABC-123", ZonaApp.ahora().plusDays(dias), 0,
                new BigDecimal("45.50"), 240, 1L, "Terminal A", "Lima", 2L,
                "Terminal B", "Ica", 20, 18);
    }

    private CompraRequestDTO compra(List<Long> asientos) {
        return new CompraRequestDTO(9L, asientos, "Ana Torres", "ANA TORRES", "4111111111111111");
    }

    private SolicitudPago pago(String monto) {
        return new SolicitudPago(new BigDecimal(monto), "4111111111111111", "ANA TORRES");
    }

    private void aprobar() {
        ((PasarelaDePrueba) pasarela).respuesta = new ResultadoPago(true, "SIM-1", "ok");
    }

    private void verificarSinCobroNiRegistro() {
        assertEquals(0, ((VentaDaoDePrueba) ventaDao).registros);
        assertEquals(0, ((PasarelaDePrueba) pasarela).cobros);
    }

    // 1. Total exacto, detalle del asiento y ultimos digitos conservados.
    @Test
    void testCompraValidaDeUnAsiento() {
        aprobar();
        ComprobanteResponseDTO resultado = servicio.comprar(1L, compra(List.of(11L)));
        VentaDaoDePrueba dao = (VentaDaoDePrueba) ventaDao;
        assertEquals(new BigDecimal("45.50"), resultado.total());
        assertEquals(new BigDecimal("45.50"), dao.guardada.total());
        assertEquals("1111", dao.guardada.tarjetaUltimos4());
        assertEquals(List.of(new DetalleVenta(9L, 11L, 11, "Ana Torres", new BigDecimal("45.50"))), dao.detalles);
        verify(ventaDao).registrar(dao.guardada, dao.detalles);
        verify(pasarela).cobrar(pago("45.50"));
    }

    // 2. Dos asientos libres; no se reutiliza el ocupado del escenario base.
    @Test
    void testCompraValidaDeVariosAsientos() {
        when(asientoDao.listarPorViaje(9L)).thenReturn(List.of(new Asiento(11L, 11, false), new Asiento(13L, 13, false)));
        aprobar();
        ComprobanteResponseDTO resultado = servicio.comprar(1L, compra(List.of(11L, 13L)));
        VentaDaoDePrueba dao = (VentaDaoDePrueba) ventaDao;
        assertEquals(new BigDecimal("91.00"), resultado.total());
        assertEquals(new BigDecimal("91.00"), dao.guardada.total());
        assertEquals(List.of(new DetalleVenta(9L, 11L, 11, "Ana Torres", new BigDecimal("45.50")),
                new DetalleVenta(9L, 13L, 13, "Ana Torres", new BigDecimal("45.50"))), dao.detalles);
        verify(pasarela).cobrar(pago("91.00"));
        assertEquals(1, dao.registros);
    }

    // 3. Limite permitido con seis identificadores distintos.
    @Test
    void testCompraValidaDeSeisAsientos() {
        when(asientoDao.listarPorViaje(9L)).thenReturn(List.of(new Asiento(1L, 1, false), new Asiento(2L, 2, false),
                new Asiento(3L, 3, false), new Asiento(4L, 4, false), new Asiento(5L, 5, false), new Asiento(6L, 6, false)));
        aprobar();
        ComprobanteResponseDTO resultado = servicio.comprar(1L, compra(List.of(1L, 2L, 3L, 4L, 5L, 6L)));
        VentaDaoDePrueba dao = (VentaDaoDePrueba) ventaDao;
        assertEquals(new BigDecimal("273.00"), resultado.total());
        assertEquals(new BigDecimal("273.00"), dao.guardada.total());
        assertEquals(List.of(1L, 2L, 3L, 4L, 5L, 6L), dao.detalles.stream().map(DetalleVenta::idAsiento).toList());
        assertEquals(6, resultado.asientos().size());
        verify(pasarela).cobrar(pago("273.00"));
        assertEquals(1, dao.registros);
    }

    // 4. Existente, ampliado: verifica ausencia de efectos externos.
    @Test
    void noMasDeSeisAsientosPorCompra() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.comprar(1L, compra(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))));
        assertEquals("Maximo 6 asientos por compra", error.getMessage());
        verificarSinCobroNiRegistro();
    }

    // 5. La busqueda no encuentra el viaje.
    @Test
    void testViajeInexistente() {
        when(viajeDao.buscarPorId(9L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.comprar(1L, compra(List.of(11L))));
        verificarSinCobroNiRegistro();
    }

    // 6. Fecha pasada sin depender de un instante de frontera.
    @Test
    void testViajeQueYaSalio() {
        when(viajeDao.buscarPorId(9L)).thenReturn(Optional.of(viajeConSalida(-7)));
        assertThrows(ReglaNegocioException.class, () -> servicio.comprar(1L, compra(List.of(11L))));
        verificarSinCobroNiRegistro();
    }

    // 7. El asiento no esta en el catalogo del viaje.
    @Test
    void testAsientoAjenoAlViaje() {
        assertThrows(ReglaNegocioException.class, () -> servicio.comprar(1L, compra(List.of(99L))));
        verificarSinCobroNiRegistro();
    }

    // 8. Existente, ampliado con ausencia de registro.
    @Test
    void noVendeUnAsientoOcupado() {
        assertThrows(AsientoNoDisponibleException.class, () -> servicio.comprar(1L, compra(List.of(12L))));
        verificarSinCobroNiRegistro();
    }

    // 9. Libre primero: tampoco se registra parcialmente la seleccion.
    @Test
    void testAsientoLibreSeguidoDeOcupado() {
        assertThrows(AsientoNoDisponibleException.class, () -> servicio.comprar(1L, compra(List.of(11L, 12L))));
        verificarSinCobroNiRegistro();
    }

    // 10. Existente: el rechazo de pasarela impide la persistencia.
    @Test
    void pagoRechazadoNoRegistraLaVenta() {
        ((PasarelaDePrueba) pasarela).respuesta = new ResultadoPago(false, null, "Tarjeta rechazada");
        PagoRechazadoException error = assertThrows(PagoRechazadoException.class,
                () -> servicio.comprar(1L, compra(List.of(11L))));
        assertEquals("Tarjeta rechazada", error.getMessage());
        assertEquals(0, ((VentaDaoDePrueba) ventaDao).registros);
        verify(pasarela).cobrar(pago("45.50"));
    }

    // 11. Existente: simula conflicto, no concurrencia real.
    @Test
    void siOtroCompraElAsientoDuranteElPagoSeAnulaElCobro() {
        aprobar();
        AsientoNoDisponibleException conflicto = new AsientoNoDisponibleException("ocupado");
        ((VentaDaoDePrueba) ventaDao).error = conflicto;
        AsientoNoDisponibleException resultado = assertThrows(AsientoNoDisponibleException.class,
                () -> servicio.comprar(1L, compra(List.of(11L))));
        assertEquals(conflicto, resultado);
        assertEquals(1, ((VentaDaoDePrueba) ventaDao).registros);
        verify(pasarela).cobrar(pago("45.50"));
        verify(pasarela).anular("SIM-1");
    }

    // 12. Error general de persistencia, no una colision de asiento.
    @Test
    void testErrorDePersistenciaAnulaElCobro() {
        aprobar();
        PersistenciaException fallo = new PersistenciaException("Fallo de almacenamiento simulado", new RuntimeException("causa ficticia"));
        ((VentaDaoDePrueba) ventaDao).error = fallo;
        PersistenciaException resultado = assertThrows(PersistenciaException.class,
                () -> servicio.comprar(1L, compra(List.of(11L))));
        assertEquals(fallo, resultado);
        assertEquals(1, ((VentaDaoDePrueba) ventaDao).registros);
        verify(pasarela).cobrar(pago("45.50"));
        verify(pasarela).anular("SIM-1");
    }

    // 13. El stub de QR recibe la URL generada a partir del codigo aleatorio real.
    @Test
    void testQrDelBoletoEnElComprobante() {
        aprobar();
        ComprobanteResponseDTO resultado = servicio.comprar(1L, compra(List.of(11L)));
        String contenido = "http://localhost:5173/boleto/" + resultado.codigoComprobante();
        verify(generadorQr).generarPngBase64(contenido);
        assertEquals("QR", resultado.qrBase64());
        assertEquals(((VentaDaoDePrueba) ventaDao).guardada.codigoComprobante(), resultado.codigoComprobante());
    }

    // 14. Conserva el proposito del caso existente de correo, ampliando los datos observados.
    @Test
    void compraGuardaSoloLosUltimosDigitosYEnviaElCorreoConQrYEnlace() {
        aprobar();
        ComprobanteResponseDTO resultado = servicio.comprar(1L, compra(List.of(11L)));
        CorreoDePrueba enviado = (CorreoDePrueba) notificacionService;
        assertEquals(1, enviado.envios);
        assertEquals("ana@test.pe", enviado.destino);
        assertEquals("Tu comprobante BusTracka " + resultado.codigoComprobante(), enviado.asunto);
        assertTrue(enviado.texto.contains(resultado.codigoComprobante()));
        assertTrue(enviado.texto.contains("S/ 45.50"));
        assertTrue(enviado.texto.contains("http://localhost:5173/comprobante/3"));
        assertTrue(enviado.texto.contains("http://localhost:5173/seguimiento/tok"));
        assertTrue(enviado.html.contains(resultado.codigoComprobante()));
        assertTrue(enviado.html.contains("45.50"));
        assertTrue(enviado.html.contains("http://localhost:5173/seguimiento/tok"));
        assertTrue(enviado.html.contains("data:image/png;base64,QR"));
        assertEquals("1111", ((VentaDaoDePrueba) ventaDao).guardada.tarjetaUltimos4());
        verify(notificacionService).enviarCorreo(enviado.destino, enviado.asunto, enviado.texto, enviado.html);
    }

    // 15. Documenta comportamiento actual, no un requisito de negocio aprobado.
    @Test
    void testCompradorNoEncontradoNoEnviaCorreo() {
        aprobar();
        when(usuarioDao.buscarPorId(1L)).thenReturn(Optional.empty());
        ComprobanteResponseDTO resultado = servicio.comprar(1L, compra(List.of(11L)));
        assertEquals(3L, resultado.idVenta());
        assertEquals("PAGADA", resultado.estado());
        assertEquals(new BigDecimal("45.50"), resultado.total());
        assertEquals(1, ((VentaDaoDePrueba) ventaDao).registros);
        assertEquals(0, ((CorreoDePrueba) notificacionService).envios);
    }

    @Test
    void elQrDeUnaCompraAnuladaNoEsValido() {
        Venta anulada = new Venta(3L, 1L, BigDecimal.TEN, "ANULADA", "SIM-1", "BT-ABC", "1111", ZonaApp.ahora());
        when(ventaDao.buscarPorCodigo("BT-ABC")).thenReturn(Optional.of(anulada));
        when(ventaDao.listarDetalles(3L)).thenReturn(List.of(new DetalleVenta(9L, 11L, 11, "Ana Torres", BigDecimal.TEN)));

        BoletoPublicoDTO boleto = servicio.validarBoleto("BT-ABC");

        assertFalse(boleto.valido());
        assertEquals("CANCELADO", boleto.estadoPasaje());
    }

    private static class PasarelaDePrueba implements PasarelaPago {
        int cobros;
        ResultadoPago respuesta;
        public ResultadoPago cobrar(SolicitudPago solicitud) {
            cobros++;
            return respuesta;
        }
        public void anular(String codigoTransaccion) { }
    }

    private static class GeneradorQrDePrueba implements GeneradorQr {
        public String generarPngBase64(String contenido) { return "QR"; }
    }

    private static class EnlaceSeguimientoDePrueba implements EnlaceSeguimientoService {
        public EnlaceSeguimientoDTO obtenerOCrear(Venta venta, Viaje viaje) {
            return new EnlaceSeguimientoDTO(venta.idVenta(), "http://localhost:5173/seguimiento/tok",
                    ZonaApp.ahora().plusDays(7), null);
        }
        public Optional<String> urlVigente(Venta venta, Viaje viaje) { return Optional.empty(); }
        public EnlaceSeguimientoDTO generarParaCompra(Long usuario, Long venta) { return null; }
        public pe.edu.ulima.is2.bustracka.dto.SeguimientoCompartidoDTO verCompartido(String token) { return null; }
    }

    /** Doble local de persistencia: observa argumentos sin BD ni captura Mockito. */
    private static class VentaDaoDePrueba implements VentaDao {
        Venta guardada;
        List<DetalleVenta> detalles;
        int registros;
        RuntimeException error;
        public Venta registrar(Venta venta, List<DetalleVenta> filas) {
            registros++;
            guardada = venta;
            detalles = List.copyOf(filas);
            if (error != null) throw error;
            return new Venta(3L, venta.idUsuario(), venta.total(), venta.estado(), venta.codigoPago(),
                    venta.codigoComprobante(), venta.tarjetaUltimos4(), venta.fecha());
        }
        public Optional<Venta> buscarPorId(Long id) { return Optional.empty(); }
        public Optional<Venta> buscarPorCodigo(String codigo) { return Optional.empty(); }
        public List<Venta> listarPorUsuario(Long id) { return List.of(); }
        public List<DetalleVenta> listarDetalles(Long id) { return List.of(); }
    }

    /** Doble de correo: conserva lo enviado en memoria y nunca comunica con una red. */
    private static class CorreoDePrueba implements NotificacionService {
        String destino, asunto, texto, html;
        int envios;
        public void enviarCorreo(String destino, String asunto, String texto, String html) {
            envios++;
            this.destino = destino;
            this.asunto = asunto;
            this.texto = texto;
            this.html = html;
        }
    }
}
