package pe.edu.ulima.is2.bustracka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Viaje con los datos de ruta y bus ya resueltos. La salida estimada suma el retraso. */
public record Viaje(Long idViaje, Long idRuta, Long idBus, String placa,
                    LocalDateTime fechaSalida, int minutosRetraso, BigDecimal precio, int duracionMin,
                    Long idTerminalOrigen, String terminalOrigen, String ciudadOrigen,
                    Long idTerminalDestino, String terminalDestino, String ciudadDestino,
                    int capacidad, int asientosDisponibles) {

    public LocalDateTime salidaEstimada() {
        return fechaSalida.plusMinutes(minutosRetraso);
    }

    public LocalDateTime llegadaEstimada() {
        return salidaEstimada().plusMinutes(duracionMin);
    }

    public EstadoViaje estadoEn(LocalDateTime instante) {
        if (instante.isBefore(salidaEstimada())) {
            return EstadoViaje.PROGRAMADO;
        }
        if (instante.isBefore(llegadaEstimada())) {
            return EstadoViaje.EN_RUTA;
        }
        return EstadoViaje.FINALIZADO;
    }
}
