package pe.edu.ulima.is2.bustracka.dto;

public record TerminalResponseDTO(Long idTerminal, String nombre, String ciudad, String direccion,
                                  double latitud, double longitud) {
}
