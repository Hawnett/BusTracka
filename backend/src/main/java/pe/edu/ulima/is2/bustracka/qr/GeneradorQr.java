package pe.edu.ulima.is2.bustracka.qr;

public interface GeneradorQr {
    /** Devuelve el QR como PNG en base64 (sin prefijo data:). */
    String generarPngBase64(String contenido);
}
