package pe.edu.ulima.is2.bustracka.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.EnlaceSeguimientoDao;
import pe.edu.ulima.is2.bustracka.dao.VentaDao;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.EnlaceSeguimientoDTO;
import pe.edu.ulima.is2.bustracka.dto.SeguimientoCompartidoDTO;
import pe.edu.ulima.is2.bustracka.exception.EnlaceInvalidoException;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;
import pe.edu.ulima.is2.bustracka.model.DetalleVenta;
import pe.edu.ulima.is2.bustracka.model.EnlaceSeguimiento;
import pe.edu.ulima.is2.bustracka.model.Venta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.qr.GeneradorQr;
import pe.edu.ulima.is2.bustracka.service.SeguimientoService;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EnlaceSeguimientoServiceImplTest {

    private EnlaceSeguimientoDao enlaceDao;
    private VentaDao ventaDao;
    private ViajeDao viajeDao;
    private SeguimientoService seguimientoService;
    private GeneradorQr generadorQr;
    private EnlaceSeguimientoServiceImpl servicio;

    @BeforeEach
    void preparar() {
        enlaceDao = mock(EnlaceSeguimientoDao.class);
        ventaDao = mock(VentaDao.class);
        viajeDao = mock(ViajeDao.class);
        seguimientoService = mock(SeguimientoService.class);
        generadorQr = mock(GeneradorQr.class);
        try (MockedStatic<DaoFactory> fabrica = mockStatic(DaoFactory.class)) {
            fabrica.when(DaoFactory::crearEnlaceSeguimientoDao).thenReturn(enlaceDao);
            fabrica.when(DaoFactory::crearVentaDao).thenReturn(ventaDao);
            fabrica.when(DaoFactory::crearViajeDao).thenReturn(viajeDao);
            servicio = new EnlaceSeguimientoServiceImpl(seguimientoService, generadorQr, "http://localhost:5173");
        }
    }

    /** Viaje de 4 horas que salio (o sale) a la hora indicada respecto de ahora. */
    private Viaje viajeQueSaleEn(long minutos) {
        return new Viaje(9L, 1L, 1L, "ABC-123", ZonaApp.ahora().plusMinutes(minutos), 0, BigDecimal.TEN, 240,
                1L, "Terminal A", "A", 2L, "Terminal B", "B", 20, 18);
    }

    private Venta venta(Long idUsuario, String estado) {
        return new Venta(3L, idUsuario, BigDecimal.TEN, estado, "SIM-1", "BT-ABC", "1111", ZonaApp.ahora().minusDays(1));
    }

    private void prepararCompra(Venta venta, Viaje viaje) {
        when(ventaDao.buscarPorId(3L)).thenReturn(Optional.of(venta));
        when(ventaDao.listarDetalles(3L)).thenReturn(List.of(
                new DetalleVenta(9L, 11L, 11, "Ana Torres", BigDecimal.TEN),
                new DetalleVenta(9L, 12L, 12, "Ana Torres", BigDecimal.TEN)));
        when(viajeDao.buscarPorId(9L)).thenReturn(Optional.of(viaje));
    }

    @Test
    void reutilizaElEnlaceActivoDeLaCompra() {
        EnlaceSeguimiento existente = new EnlaceSeguimiento(1L, 3L, 9L, "tok123", ZonaApp.ahora(),
                ZonaApp.ahora().plusDays(1), EnlaceSeguimiento.ACTIVO);
        when(enlaceDao.buscarActivoPorVenta(3L)).thenReturn(Optional.of(existente));

        EnlaceSeguimientoDTO enlace = servicio.obtenerOCrear(venta(1L, "PAGADA"), viajeQueSaleEn(60));

        assertEquals("http://localhost:5173/seguimiento/tok123", enlace.url());
        verify(enlaceDao, never()).registrar(any());
    }

    @Test
    void creaUnTokenAleatorioQueVenceAlFinalDelDiaDeLlegada() {
        Viaje viaje = viajeQueSaleEn(60);
        when(enlaceDao.buscarActivoPorVenta(3L)).thenReturn(Optional.empty());
        when(enlaceDao.registrar(any())).thenAnswer(inv -> inv.getArgument(0));

        servicio.obtenerOCrear(venta(1L, "PAGADA"), viaje);

        ArgumentCaptor<EnlaceSeguimiento> creado = ArgumentCaptor.forClass(EnlaceSeguimiento.class);
        verify(enlaceDao).registrar(creado.capture());
        assertTrue(creado.getValue().token().length() >= 32);
        assertEquals(viaje.llegadaEstimada().toLocalDate().atTime(23, 59, 59), creado.getValue().fechaExpiracion());
        assertEquals(EnlaceSeguimiento.ACTIVO, creado.getValue().estado());
    }

    @Test
    void elFamiliarVeElViajeConElNombreDelPasajero() {
        prepararCompra(venta(1L, "PAGADA"), viajeQueSaleEn(-60));
        when(enlaceDao.buscarPorToken("tok123")).thenReturn(Optional.of(new EnlaceSeguimiento(1L, 3L, 9L, "tok123",
                ZonaApp.ahora().minusHours(2), ZonaApp.ahora().plusHours(10), EnlaceSeguimiento.ACTIVO)));

        SeguimientoCompartidoDTO compartido = servicio.verCompartido("tok123");

        assertEquals("Ana Torres", compartido.nombrePasajero());
        assertEquals(List.of(11, 12), compartido.asientos());
        verify(seguimientoService).obtenerUbicacion(9L);
    }

    @Test
    void rechazaUnTokenQueNoExiste() {
        when(enlaceDao.buscarPorToken("inventado")).thenReturn(Optional.empty());

        assertThrows(EnlaceInvalidoException.class, () -> servicio.verCompartido("inventado"));
    }

    @Test
    void rechazaElEnlaceCuandoElViajeYaTermino() {
        prepararCompra(venta(1L, "PAGADA"), viajeQueSaleEn(-3 * 24 * 60));
        LocalDateTime creado = ZonaApp.ahora().minusDays(3);
        when(enlaceDao.buscarPorToken("tok123")).thenReturn(Optional.of(new EnlaceSeguimiento(1L, 3L, 9L, "tok123",
                creado, creado.toLocalDate().atTime(23, 59, 59), EnlaceSeguimiento.ACTIVO)));

        assertThrows(EnlaceInvalidoException.class, () -> servicio.verCompartido("tok123"));
    }

    @Test
    void noComparteLaCompraDeOtroUsuario() {
        prepararCompra(venta(1L, "PAGADA"), viajeQueSaleEn(60));

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.generarParaCompra(2L, 3L));
    }

    @Test
    void noComparteUnViajeQueYaTermino() {
        prepararCompra(venta(1L, "PAGADA"), viajeQueSaleEn(-6 * 60));

        assertThrows(ReglaNegocioException.class, () -> servicio.generarParaCompra(1L, 3L));
    }

    @Test
    void alGenerarElEnlaceTambienDevuelveSuQr() {
        prepararCompra(venta(1L, "PAGADA"), viajeQueSaleEn(60));
        when(enlaceDao.buscarActivoPorVenta(3L)).thenReturn(Optional.empty());
        when(enlaceDao.registrar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(generadorQr.generarPngBase64(any())).thenReturn("QR");

        EnlaceSeguimientoDTO enlace = servicio.generarParaCompra(1L, 3L);

        assertEquals("QR", enlace.qrBase64());
        assertNotNull(enlace.expiraEn());
    }
}
