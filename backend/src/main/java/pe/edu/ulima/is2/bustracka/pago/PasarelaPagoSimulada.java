package pe.edu.ulima.is2.bustracka.pago;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Pasarela de pruebas (sandbox propio). Regla para poder demostrar ambos
 * caminos: las tarjetas que terminan en 0000 son rechazadas, el resto se aprueba.
 */
@Component
public class PasarelaPagoSimulada implements PasarelaPago {

    private static final Logger log = LoggerFactory.getLogger(PasarelaPagoSimulada.class);

    @Override
    public ResultadoPago cobrar(SolicitudPago solicitud) {
        if (solicitud.numeroTarjeta().endsWith("0000")) {
            return new ResultadoPago(false, null, "Tarjeta rechazada por el emisor");
        }
        String codigo = "SIM-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        log.info("[PASARELA SIMULADA] Cobro aprobado {} por S/ {}", codigo, solicitud.monto());
        return new ResultadoPago(true, codigo, "Pago aprobado");
    }

    @Override
    public void anular(String codigoTransaccion) {
        log.info("[PASARELA SIMULADA] Cobro {} anulado", codigoTransaccion);
    }
}
