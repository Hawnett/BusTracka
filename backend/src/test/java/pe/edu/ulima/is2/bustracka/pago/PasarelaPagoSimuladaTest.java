package pe.edu.ulima.is2.bustracka.pago;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasarelaPagoSimuladaTest {

    private final PasarelaPago pasarela = new PasarelaPagoSimulada();

    @Test
    void apruebaTarjetaNormal() {
        ResultadoPago r = pasarela.cobrar(new SolicitudPago(BigDecimal.TEN, "4111111111111111", "Ana"));
        assertTrue(r.aprobado());
        assertNotNull(r.codigoTransaccion());
    }

    @Test
    void rechazaTarjetaQueTerminaEnCeros() {
        ResultadoPago r = pasarela.cobrar(new SolicitudPago(BigDecimal.TEN, "4111111111110000", "Ana"));
        assertFalse(r.aprobado());
    }
}
