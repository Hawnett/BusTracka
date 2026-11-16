package pe.edu.ulima.is2.bustracka.pago;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasarelaPagoSimuladaCasosTest {

    private final PasarelaPago pasarela = new PasarelaPagoSimulada();

    private ResultadoPago cobrar(String tarjeta) {
        return pasarela.cobrar(new SolicitudPago(new BigDecimal("45.00"), tarjeta, "Ana Perez"));
    }

    @Test
    void apruebaTarjetaValida() {
        ResultadoPago r = cobrar("4111111111111111");
        assertTrue(r.aprobado());
        assertTrue(r.codigoTransaccion().startsWith("SIM-"));
    }

    @Test
    void rechazaTarjetaQueTerminaEnCeros() {
        ResultadoPago r = cobrar("4111111111110000");
        assertFalse(r.aprobado());
        assertNull(r.codigoTransaccion());
    }

    @Test
    void apruebaSiLosCerosEstanEnMedio() {
        ResultadoPago r = cobrar("4111000011111111");
        assertTrue(r.aprobado());
    }

    @Test
    void generaCodigosDistintos() {
        ResultadoPago r1 = cobrar("4111111111111111");
        ResultadoPago r2 = cobrar("4111111111111111");
        assertNotEquals(r1.codigoTransaccion(), r2.codigoTransaccion());
    }
}