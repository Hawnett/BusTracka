package pe.edu.ulima.is2.bustracka.qr;

import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorQrZxingTest {

    @Test
    void generaUnPngValido() {
        byte[] bytes = Base64.getDecoder().decode(new GeneradorQrZxing().generarPngBase64("BUSTRACKA:BT-TEST"));
        // Firma de un archivo PNG: 0x89 'P' 'N' 'G'
        assertTrue(bytes.length > 100 && bytes[0] == (byte) 0x89 && bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G');
    }
}
