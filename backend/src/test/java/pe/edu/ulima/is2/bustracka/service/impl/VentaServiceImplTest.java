package pe.edu.ulima.is2.bustracka.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import pe.edu.ulima.is2.bustracka.comprobante.GeneradorComprobante;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.UsuarioDao;
import pe.edu.ulima.is2.bustracka.dao.VentaDao;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.BoletoPublicoDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class VentaServiceImplTest {

    private VentaDao ventaDao;
    private ViajeDao viajeDao;
    private AsientoDao asientoDao;
    private UsuarioDao usuarioDao;
    private PasarelaPago pasarela;
    private NotificacionService notificacionService;
    private EnlaceSeguimientoService enlaceService;
    private VentaServiceImpl servicio;

    private final Viaje viaje = new Viaje(9L, 1L, 1L, "ABC-123", ZonaApp.ahora().plusDays(1), 0, new BigDecimal("45.00"),
            240, 1L, "Terminal A", "Lima", 2L, "Terminal B", "Ica", 20, 18);

    @BeforeEach
    void preparar() {
        ventaDao = mock(VentaDao.class);
        viajeDao = mock(ViajeDao.class);
        asientoDao = mock(AsientoDao.class);
        usuarioDao = mock(UsuarioDao.class);
        pasarela = mock(PasarelaPago.class);
        notificacionService = mock(NotificacionService.class);
        enlaceService = mock(EnlaceSeguimientoService.class);
        GeneradorQr generadorQr = mock(GeneradorQr.class);
        when(generadorQr.generarPngBase64(any())).thenReturn("QR");
        try (MockedStatic<DaoFactory> fabrica = mockStatic(DaoFactory.class)) {
            fabrica.when(DaoFactory::crearVentaDao).thenReturn(ventaDao);
            fabrica.when(DaoFactory::crearViajeDao).thenReturn(viajeDao);
            fabrica.when(DaoFactory::crearAsientoDao).thenReturn(asientoDao);
            fabrica.when(DaoFactory::crearUsuarioDao).thenReturn(usuarioDao);
            servicio = new VentaServiceImpl(pasarela, notificacionService, generadorQr, mock(GeneradorComprobante.class),
                    enlaceService, "http://localhost:5173");
        }
        when(viajeDao.buscarPorId(9L)).thenReturn(Optional.of(viaje));
        when(asientoDao.listarPorViaje(9L)).thenReturn(List.of(new Asiento(11L, 11, false), new Asiento(12L, 12, true)));
        when(usuarioDao.buscarPorId(1L)).thenReturn(Optional.of(
                new Usuario(1L, "ana", "hash", "ana@test.pe", "ACTIVO", 2, ZonaApp.ahora())));
    }

    private CompraRequestDTO compra(List<Long> asientos) {
        return new CompraRequestDTO(9L, asientos, "Ana Torres", "ANA TORRES", "4111111111111111");
    }

    @Test
    void pagoRechazadoNoRegistraLaVenta() {
        when(pasarela.cobrar(any())).thenReturn(new ResultadoPago(false, null, "Tarjeta rechazada"));

        assertThrows(PagoRechazadoException.class, () -> servicio.comprar(1L, compra(List.of(11L))));
        verify(ventaDao, never()).registrar(any(), anyList());
    }

    @Test
    void siOtroCompraElAsientoDuranteElPagoSeAnulaElCobro() {
        when(pasarela.cobrar(any())).thenReturn(new ResultadoPago(true, "SIM-1", "ok"));
        when(ventaDao.registrar(any(), anyList())).thenThrow(new AsientoNoDisponibleException("ocupado"));

        assertThrows(AsientoNoDisponibleException.class, () -> servicio.comprar(1L, compra(List.of(11L))));
        verify(pasarela).anular("SIM-1");
    }

    @Test
    void noVendeUnAsientoOcupado() {
        assertThrows(AsientoNoDisponibleException.class, () -> servicio.comprar(1L, compra(List.of(12L))));
        verify(pasarela, never()).cobrar(any());
    }

    @Test
    void noMasDeSeisAsientosPorCompra() {
        assertThrows(ReglaNegocioException.class,
                () -> servicio.comprar(1L, compra(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L))));
    }

    @Test
    void compraGuardaSoloLosUltimosDigitosYEnviaElCorreoConQrYEnlace() {
        when(pasarela.cobrar(any())).thenReturn(new ResultadoPago(true, "SIM-1", "ok"));
        when(ventaDao.registrar(any(), anyList())).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            return new Venta(3L, v.idUsuario(), v.total(), v.estado(), v.codigoPago(), v.codigoComprobante(),
                    v.tarjetaUltimos4(), v.fecha());
        });
        when(enlaceService.obtenerOCrear(any(), any())).thenReturn(
                new EnlaceSeguimientoDTO(3L, "http://localhost:5173/seguimiento/tok", ZonaApp.ahora().plusDays(1), null));

        servicio.comprar(1L, compra(List.of(11L)));

        ArgumentCaptor<Venta> guardada = ArgumentCaptor.forClass(Venta.class);
        verify(ventaDao).registrar(guardada.capture(), anyList());
        assertEquals("1111", guardada.getValue().tarjetaUltimos4());
        assertEquals(new BigDecimal("45.00"), guardada.getValue().total());
        verify(notificacionService).enviarCorreo(eq("ana@test.pe"), anyString(),
                contains("http://localhost:5173/seguimiento/tok"), contains("data:image/png;base64,QR"));
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
}
