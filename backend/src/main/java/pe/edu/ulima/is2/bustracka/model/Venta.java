package pe.edu.ulima.is2.bustracka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Venta(Long idVenta, Long idUsuario, BigDecimal total, String estado,
                    String codigoPago, String codigoComprobante, String tarjetaUltimos4, LocalDateTime fecha) {

    public static final String PAGADA = "PAGADA";

    public boolean estaPagada() {
        return PAGADA.equals(estado);
    }
}
