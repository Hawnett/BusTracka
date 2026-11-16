package pe.edu.ulima.is2.bustracka.comprobante;

import org.junit.jupiter.api.Test;
import pe.edu.ulima.is2.bustracka.dto.AsientoVendidoDTO;
import pe.edu.ulima.is2.bustracka.dto.ComprobanteResponseDTO;
import pe.edu.ulima.is2.bustracka.qr.GeneradorQrZxing;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeneradorComprobantePdfTest {

    @Test
    void generaUnPdfConElQrDelBoleto() {
        LocalDateTime salida = LocalDateTime.of(2026, 11, 5, 8, 0);
        ComprobanteResponseDTO comprobante = new ComprobanteResponseDTO(3L, "BT-ABC", "PAGADA", "PROXIMO",
                new BigDecimal("90.00"), salida.minusDays(1), 9L, "Lima", "Ica", "Terminal Plaza Norte", "Terminal Ica",
                salida, 15, salida.plusMinutes(15), salida.plusMinutes(255), "ABC-123",
                List.of(new AsientoVendidoDTO(11, "Ana Torres", new BigDecimal("45.00")),
                        new AsientoVendidoDTO(12, "Ana Torres", new BigDecimal("45.00"))),
                "SIM-1", "Tarjeta terminada en 1111", "ana",
                new GeneradorQrZxing().generarPngBase64("http://localhost:5173/boleto/BT-ABC"), null);

        byte[] pdf = new GeneradorComprobantePdf().generarPdf(comprobante);

        assertTrue(pdf.length > 1000);
        assertEquals("%PDF", new String(pdf, 0, 4, StandardCharsets.US_ASCII));
    }
}
