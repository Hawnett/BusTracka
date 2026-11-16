package pe.edu.ulima.is2.bustracka.dto;

import java.time.LocalDateTime;

public record ParadaDTO(String nombre, LocalDateTime hora, String estado, double latitud, double longitud) {
}
