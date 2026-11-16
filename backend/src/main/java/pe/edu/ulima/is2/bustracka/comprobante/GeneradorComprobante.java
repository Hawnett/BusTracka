package pe.edu.ulima.is2.bustracka.comprobante;

import pe.edu.ulima.is2.bustracka.dto.ComprobanteResponseDTO;

public interface GeneradorComprobante {
    byte[] generarPdf(ComprobanteResponseDTO comprobante);
}
