package pe.edu.ulima.is2.bustracka.dto;

import java.util.List;

public record UbicacionResponseDTO(Long idViaje, String ciudadOrigen, String ciudadDestino,
                                   double latitud, double longitud, double progreso, String estado,
                                   List<PuntoRutaDTO> ruta) {
}
