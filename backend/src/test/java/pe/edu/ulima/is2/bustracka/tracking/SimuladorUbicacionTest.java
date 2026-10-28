package pe.edu.ulima.is2.bustracka.tracking;

import org.junit.jupiter.api.Test;
import pe.edu.ulima.is2.bustracka.model.PuntoRuta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimuladorUbicacionTest {

    private final List<PuntoRuta> ruta = List.of(new PuntoRuta(1, 0, 0), new PuntoRuta(2, 10, 10));

    private Viaje viajeQueSalioHace(long minutos) {
        LocalDateTime salida = ZonaApp.ahora().minusMinutes(minutos);
        return new Viaje(1L, 1L, 1L, "ABC-123", salida, BigDecimal.TEN, 60, 1L, "A", 2L, "B", 20, 20);
    }

    @Test
    void viajeFuturoSeQuedaEnElOrigen() {
        UbicacionBus u = new SimuladorUbicacion(1).obtenerUbicacion(viajeQueSalioHace(-120), ruta);
        assertEquals("PROGRAMADO", u.estado());
        assertEquals(0, u.latitud(), 0.0001);
    }

    @Test
    void aMitadDeViajeEstaEnElPuntoMedio() {
        UbicacionBus u = new SimuladorUbicacion(1).obtenerUbicacion(viajeQueSalioHace(30), ruta);
        assertEquals("EN_RUTA", u.estado());
        assertEquals(5.0, u.latitud(), 0.1);
        assertEquals(5.0, u.longitud(), 0.1);
    }
}
