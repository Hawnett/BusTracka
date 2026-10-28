package pe.edu.ulima.is2.bustracka.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Component
public class GeneradorQrZxing implements GeneradorQr {

    private static final int TAMANO = 240;

    @Override
    public String generarPngBase64(String contenido) {
        try {
            BitMatrix matriz = new QRCodeWriter().encode(contenido, BarcodeFormat.QR_CODE, TAMANO, TAMANO);
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
            return Base64.getEncoder().encodeToString(salida.toByteArray());
        } catch (WriterException | IOException e) {
            throw new ReglaNegocioException("No se pudo generar el codigo QR del comprobante");
        }
    }
}
