package pe.edu.ulima.is2.bustracka.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** estado es el de la venta (PAGADA / ANULADA); estadoPasaje, el que se muestra en el historial. */
public record ComprobanteResponseDTO(Long idVenta, String codigoComprobante, String estado, String estadoPasaje,
                                     BigDecimal total, LocalDateTime fechaCompra, Long idViaje,
                                     String ciudadOrigen, String ciudadDestino,
                                     String terminalOrigen, String terminalDestino,
                                     LocalDateTime fechaSalida, int minutosRetraso,
                                     LocalDateTime salidaEstimada, LocalDateTime llegadaEstimada, String placa,
                                     List<AsientoVendidoDTO> asientos, String codigoPago, String metodoPago,
                                     String comprador, String qrBase64, String enlaceSeguimiento) {
}
