package pe.edu.ulima.is2.bustracka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ViajeResponseDTO(Long idViaje, String placa, LocalDateTime fechaSalida, BigDecimal precio,
                               int duracionMin, Long idTerminalOrigen, String ciudadOrigen,
                               Long idTerminalDestino, String ciudadDestino,
                               int capacidad, int asientosDisponibles) {
}
