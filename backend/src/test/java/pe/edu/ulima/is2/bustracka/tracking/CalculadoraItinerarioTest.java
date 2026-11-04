package pe.edu.ulima.is2.bustracka.tracking;

import org.junit.jupiter.api.Test;
import pe.edu.ulima.is2.bustracka.model.EstadoViaje;
import pe.edu.ulima.is2.bustracka.model.PuntoRuta;
import pe.edu.ulima.is2.bustracka.model.Viaje;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadoraItinerarioTest {

    // Cuatro puntos equidistantes sobre el ecuador; el tercero no tiene nombre (solo da forma a la ruta).
    private final List<PuntoRuta> ruta = List.of(new PuntoRuta(1, "A", 0, 0), new PuntoRuta(2, "Medio", 0, 1),
            new PuntoRuta(3, null, 0, 2), new PuntoRuta(4, "B", 0, 3));
    private final LocalDateTime salida = LocalDateTime.of(2026, 11, 5, 10, 0);

    private Viaje viaje(int minutosRetraso) {
        return new Viaje(1L, 1L, 1L, "ABC-123", salida, minutosRetraso, BigDecimal.TEN, 90,
                1L, "Terminal A", "A", 2L, "Terminal B", "B", 20, 20);
    }

    @Test
    void enRutaMarcaLasParadasPasadasLaActualYLasProximas() {
        List<ParadaItinerario> paradas = CalculadoraItinerario.calcular(viaje(0), ruta, 0.5, EstadoViaje.EN_RUTA);

        assertEquals(3, paradas.size());
        assertEquals(List.of("Terminal A", "Medio", "Terminal B"), paradas.stream().map(ParadaItinerario::nombre).toList());
        assertEquals(List.of(ParadaItinerario.PASO, ParadaItinerario.ACTUAL, ParadaItinerario.PROXIMA),
                paradas.stream().map(ParadaItinerario::estado).toList());
    }

    @Test
    void laHoraDeCadaParadaRepartiendoLaDuracionYSumandoElRetraso() {
        List<ParadaItinerario> paradas = CalculadoraItinerario.calcular(viaje(15), ruta, 0, EstadoViaje.PROGRAMADO);

        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 15), paradas.get(0).hora());
        assertEquals(LocalDateTime.of(2026, 11, 5, 10, 45), paradas.get(1).hora());
        assertEquals(LocalDateTime.of(2026, 11, 5, 11, 45), paradas.get(2).hora());
    }

    @Test
    void antesDeSalirElBusEstaEnElOrigen() {
        List<ParadaItinerario> paradas = CalculadoraItinerario.calcular(viaje(0), ruta, 0, EstadoViaje.PROGRAMADO);

        assertEquals(List.of(ParadaItinerario.ACTUAL, ParadaItinerario.PROXIMA, ParadaItinerario.PROXIMA),
                paradas.stream().map(ParadaItinerario::estado).toList());
    }

    @Test
    void alLlegarTodasLasParadasQuedanPasadas() {
        List<ParadaItinerario> paradas = CalculadoraItinerario.calcular(viaje(0), ruta, 1, EstadoViaje.FINALIZADO);

        assertEquals(List.of(ParadaItinerario.PASO, ParadaItinerario.PASO, ParadaItinerario.PASO),
                paradas.stream().map(ParadaItinerario::estado).toList());
    }
}
