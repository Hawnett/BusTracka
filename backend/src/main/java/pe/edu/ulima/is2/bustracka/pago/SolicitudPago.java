package pe.edu.ulima.is2.bustracka.pago;

import java.math.BigDecimal;

public record SolicitudPago(BigDecimal monto, String numeroTarjeta, String titular) {
}
