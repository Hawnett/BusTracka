package pe.edu.ulima.is2.bustracka.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SeguimientoCompartidoDTO(String nombrePasajero, String codigoComprobante, List<Integer> asientos,
                                       LocalDateTime expiraEn, UbicacionResponseDTO ubicacion) {
}
