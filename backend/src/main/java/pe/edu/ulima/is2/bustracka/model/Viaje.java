package pe.edu.ulima.is2.bustracka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Viaje programado con los datos de ruta y bus ya resueltos (lectura). */
public record Viaje(Long idViaje, Long idRuta, Long idBus, String placa,
                    LocalDateTime fechaSalida, BigDecimal precio, int duracionMin,
                    Long idTerminalOrigen, String ciudadOrigen,
                    Long idTerminalDestino, String ciudadDestino,
                    int capacidad, int asientosDisponibles) {
}
