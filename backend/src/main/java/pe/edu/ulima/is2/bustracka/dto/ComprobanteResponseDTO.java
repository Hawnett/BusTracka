package pe.edu.ulima.is2.bustracka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComprobanteResponseDTO(Long idVenta, String codigoComprobante, String estado, BigDecimal total,
                                     LocalDateTime fechaCompra, Long idViaje, String ciudadOrigen,
                                     String ciudadDestino, LocalDateTime fechaSalida, String placa,
                                     List<AsientoVendidoDTO> asientos, String codigoPago,
                                     String qrBase64, String enlaceSeguimiento) {
}
