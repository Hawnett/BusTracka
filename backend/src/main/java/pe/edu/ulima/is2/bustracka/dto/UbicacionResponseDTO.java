package pe.edu.ulima.is2.bustracka.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UbicacionResponseDTO(Long idViaje, String ciudadOrigen, String ciudadDestino,
                                   double latitud, double longitud, double progreso, String estado,
                                   List<PuntoRutaDTO> ruta, String placa, String terminalOrigen, String terminalDestino,
                                   LocalDateTime fechaSalida, int minutosRetraso, LocalDateTime salidaEstimada,
                                   LocalDateTime llegadaEstimada, double velocidadKmh, double distanciaTotalKm,
                                   double recorridoKm, String proximaParada, List<ParadaDTO> paradas,
                                   double aceleracion) {
}
