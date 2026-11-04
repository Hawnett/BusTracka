package pe.edu.ulima.is2.bustracka.dto;

import java.time.LocalDateTime;
import java.util.List;

public record BoletoPublicoDTO(String codigoComprobante, boolean valido, String estadoPasaje,
                               String nombrePasajero, String ciudadOrigen, String ciudadDestino,
                               String terminalOrigen, LocalDateTime salidaEstimada, String placa,
                               List<Integer> asientos) {
}
