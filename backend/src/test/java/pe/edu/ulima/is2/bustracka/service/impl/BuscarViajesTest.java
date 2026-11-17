package pe.edu.ulima.is2.bustracka.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.ViajeResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;
import pe.edu.ulima.is2.bustracka.model.FranjaHoraria;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de ViajeServiceImpl.buscarViajes (HU 2.1): origen, destino, fecha,
 * pasajeros y franja horaria. El DAO se reemplaza por un doble (Mockito).
 * Autor: Alvaro Diego Mera Incio.
 */
class BuscarViajesTest {

    private ViajeDao viajeDao;
    private ViajeServiceImpl servicio;
    private final LocalDate manana = ZonaApp.ahora().toLocalDate().plusDays(1);

    @BeforeEach
    void preparar() {
        viajeDao = mock(ViajeDao.class);
        try (MockedStatic<DaoFactory> fabrica = mockStatic(DaoFactory.class)) {
            fabrica.when(DaoFactory::crearViajeDao).thenReturn(viajeDao);
            fabrica.when(DaoFactory::crearAsientoDao).thenReturn(mock(AsientoDao.class));
            servicio = new ViajeServiceImpl();
        }
    }

    private Viaje viajeDeManana() {
        return new Viaje(3L, 1L, 1L, "ABC-123", manana.atTime(8, 0), 0, new BigDecimal("45.00"),
                240, 1L, "Terminal Plaza Norte", "Lima", 2L, "Terminal Ica", "Ica", 20, 19);
    }

    @Test
    void buscaConLosCincoFiltrosValidos() {
        when(viajeDao.buscar(1L, 2L, manana, 2, FranjaHoraria.MANANA)).thenReturn(List.of(viajeDeManana()));

        List<ViajeResponseDTO> viajes = servicio.buscarViajes(1L, 2L, manana, 2, FranjaHoraria.MANANA);

        assertEquals(1, viajes.size());
        assertEquals(3L, viajes.get(0).idViaje());
    }

    @Test
    void sinCantidadDePasajerosBuscaParaUnPasajero() {
        when(viajeDao.buscar(null, null, null, 1, null)).thenReturn(List.of());

        servicio.buscarViajes(null, null, null, null, null);

        verify(viajeDao).buscar(null, null, null, 1, null);
    }

    @Test
    void rechazaCeroOMasDeSeisPasajeros() {
        assertThrows(ReglaNegocioException.class, () -> servicio.buscarViajes(1L, 2L, manana, 0, null));
        assertThrows(ReglaNegocioException.class, () -> servicio.buscarViajes(1L, 2L, manana, 7, null));
        verify(viajeDao, never()).buscar(any(), any(), any(), anyInt(), any());
    }

    @Test
    void rechazaOrigenIgualAlDestino() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> servicio.buscarViajes(1L, 1L, manana, 2, null));
        assertEquals("El origen y el destino deben ser distintos", error.getMessage());
    }

    @Test
    void rechazaUnaFechaPasada() {
        LocalDate ayer = ZonaApp.ahora().toLocalDate().minusDays(1);
        assertThrows(ReglaNegocioException.class, () -> servicio.buscarViajes(1L, 2L, ayer, 2, null));
        verify(viajeDao, never()).buscar(any(), any(), any(), anyInt(), any());
    }
}
