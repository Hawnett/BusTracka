package pe.edu.ulima.is2.bustracka.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EstadoPasajeTest {

    private final LocalDateTime salida = LocalDateTime.of(2026, 11, 5, 10, 0);
    private final Viaje viaje = new Viaje(1L, 1L, 1L, "ABC-123", salida, 30, BigDecimal.TEN, 240,
            1L, "Terminal A", "A", 2L, "Terminal B", "B", 20, 20);

    private Venta venta(String estado) {
        return new Venta(1L, 1L, BigDecimal.TEN, estado, "SIM-1", "BT-1", "1111", salida.minusDays(1));
    }

    @Test
    void conRetrasoSigueProximoHastaLaNuevaHoraDeSalida() {
        assertEquals(EstadoPasaje.PROXIMO, EstadoPasaje.de(venta("PAGADA"), viaje, salida.plusMinutes(15)));
    }

    @Test
    void enCursoMientrasElBusVaEnCamino() {
        assertEquals(EstadoPasaje.EN_CURSO, EstadoPasaje.de(venta("PAGADA"), viaje, salida.plusHours(2)));
    }

    @Test
    void completadoDespuesDeLaLlegadaEstimada() {
        assertEquals(EstadoPasaje.COMPLETADO, EstadoPasaje.de(venta("PAGADA"), viaje, salida.plusHours(5)));
    }

    @Test
    void unaVentaAnuladaSiempreEsCancelada() {
        assertEquals(EstadoPasaje.CANCELADO, EstadoPasaje.de(venta("ANULADA"), viaje, salida.minusHours(1)));
    }
}
