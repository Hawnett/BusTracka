package pe.edu.ulima.is2.bustracka.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.RetrasoRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.ViajeResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;
import pe.edu.ulima.is2.bustracka.model.MotivoRetraso;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrarRetrasoTest {

    private ViajeDao viajeDao;
    private ViajeServiceImpl servicio;

    private static Viaje viajeQueSale(LocalDateTime salida, int minutosRetraso) {
        return new Viaje(3L, 1L, 1L, "ABC-123", salida, minutosRetraso, new BigDecimal("45.00"),
                240, 1L, "Terminal Plaza Norte", "Lima", 2L, "Terminal Trujillo", "Trujillo", 40, 38);
    }

    @BeforeEach
    void preparar() {
        viajeDao = mock(ViajeDao.class);
        try (MockedStatic<DaoFactory> fabrica = mockStatic(DaoFactory.class)) {
            fabrica.when(DaoFactory::crearViajeDao).thenReturn(viajeDao);
            fabrica.when(DaoFactory::crearAsientoDao).thenReturn(mock(AsientoDao.class));
            servicio = new ViajeServiceImpl();
        }
    }

    private static RetrasoRequestDTO retraso(int minutos, boolean avisar) {
        return new RetrasoRequestDTO(minutos, MotivoRetraso.TRAFICO, "Congestion en la Panamericana Sur", avisar);
    }

    @Test
    void guardaUnRetrasoValidoYAvisaALosPasajeros() {
        LocalDateTime manana = ZonaApp.ahora().plusDays(1);
        when(viajeDao.buscarPorId(3L)).thenReturn(Optional.of(viajeQueSale(manana, 0)), Optional.of(viajeQueSale(manana, 30)));

        ViajeResponseDTO respuesta = servicio.registrarRetraso(3L, retraso(30, true));

        verify(viajeDao).actualizarRetraso(3L, 30, "TRAFICO", "Congestion en la Panamericana Sur", true);
        assertEquals(30, respuesta.minutosRetraso());
    }

    @Test
    void guardaElRetrasoSinAvisarSiElAdministradorLoDesmarca() {
        LocalDateTime manana = ZonaApp.ahora().plusDays(1);
        when(viajeDao.buscarPorId(3L)).thenReturn(Optional.of(viajeQueSale(manana, 0)), Optional.of(viajeQueSale(manana, 45)));

        servicio.registrarRetraso(3L, retraso(45, false));

        verify(viajeDao).actualizarRetraso(3L, 45, "TRAFICO", "Congestion en la Panamericana Sur", false);
    }

    @Test
    void conCeroMinutosVuelveASuHoraYNoAvisa() {
        LocalDateTime manana = ZonaApp.ahora().plusDays(1);
        when(viajeDao.buscarPorId(3L)).thenReturn(Optional.of(viajeQueSale(manana, 15)), Optional.of(viajeQueSale(manana, 0)));

        ViajeResponseDTO respuesta = servicio.registrarRetraso(3L, retraso(0, true));

        verify(viajeDao).actualizarRetraso(3L, 0, "TRAFICO", "Congestion en la Panamericana Sur", false);
        assertEquals(0, respuesta.minutosRetraso());
    }

    @Test
    void rechazaViajeQueNoExiste() {
        when(viajeDao.buscarPorId(999L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> servicio.registrarRetraso(999L, retraso(30, true)));
        verify(viajeDao, never()).actualizarRetraso(anyLong(), anyInt(), anyString(), anyString(), anyBoolean());
    }

    @Test
    void rechazaViajeQueYaTermino() {
        LocalDateTime haceTresDias = ZonaApp.ahora().minusDays(3);
        when(viajeDao.buscarPorId(10L)).thenReturn(Optional.of(viajeQueSale(haceTresDias, 0)));

        assertThrows(ReglaNegocioException.class, () -> servicio.registrarRetraso(10L, retraso(30, true)));
        verify(viajeDao, never()).actualizarRetraso(anyLong(), anyInt(), anyString(), anyString(), anyBoolean());
    }
}