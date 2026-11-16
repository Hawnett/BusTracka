package pe.edu.ulima.is2.bustracka.comprobante;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import pe.edu.ulima.is2.bustracka.dto.AsientoVendidoDTO;
import pe.edu.ulima.is2.bustracka.dto.ComprobanteResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** Boleto en PDF (OpenPDF) con los datos del comprobante y su QR. */
@Component
public class GeneradorComprobantePdf implements GeneradorComprobante {

    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Color TINTA = new Color(0x14, 0x11, 0x0f);
    private static final Color GRIS = new Color(0x5a, 0x52, 0x4a);
    private static final Color AMARILLO = new Color(0xff, 0xc4, 0x00);
    private static final Map<String, String> ESTADOS = Map.of("PROXIMO", "Próximo", "EN_CURSO", "En curso",
            "COMPLETADO", "Completado", "CANCELADO", "Cancelado");

    @Override
    public byte[] generarPdf(ComprobanteResponseDTO c) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A5, 32, 32, 32, 32);
        try {
            PdfWriter.getInstance(documento, salida);
            documento.open();
            documento.add(cabecera(c));

            Paragraph ruta = new Paragraph(c.ciudadOrigen() + " – " + c.ciudadDestino(),
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, TINTA));
            ruta.setSpacingBefore(14);
            ruta.setSpacingAfter(8);
            documento.add(ruta);
            documento.add(tablaDatos(c));

            Image qr = Image.getInstance(Base64.getDecoder().decode(c.qrBase64()));
            qr.scaleToFit(130, 130);
            qr.setAlignment(Element.ALIGN_CENTER);
            qr.setSpacingBefore(12);
            documento.add(qr);

            Paragraph pie = new Paragraph("Presenta este código QR al abordar. Llega 30 minutos antes de la salida.",
                    FontFactory.getFont(FontFactory.HELVETICA, 9, GRIS));
            pie.setAlignment(Element.ALIGN_CENTER);
            documento.add(pie);
            documento.close();
        } catch (DocumentException | IOException e) {
            throw new ReglaNegocioException("No se pudo generar el PDF del comprobante");
        }
        return salida.toByteArray();
    }

    private PdfPTable cabecera(ComprobanteResponseDTO c) {
        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.addCell(celdaCabecera("BUSTRACKA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, AMARILLO), Element.ALIGN_LEFT));
        tabla.addCell(celdaCabecera("Boleto " + c.codigoComprobante(),
                FontFactory.getFont(FontFactory.COURIER_BOLD, 11, Color.WHITE), Element.ALIGN_RIGHT));
        return tabla;
    }

    private PdfPCell celdaCabecera(String texto, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBackgroundColor(TINTA);
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setPadding(10);
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return celda;
    }

    private PdfPTable tablaDatos(ComprobanteResponseDTO c) throws DocumentException {
        Map<String, String> filas = new LinkedHashMap<>();
        filas.put("Estado", ESTADOS.getOrDefault(c.estadoPasaje(), c.estadoPasaje()));
        filas.put("Pasajero", c.asientos().stream().map(AsientoVendidoDTO::nombrePasajero).distinct().collect(Collectors.joining(", ")));
        filas.put("Salida", c.salidaEstimada().format(FECHA_HORA) + (c.minutosRetraso() > 0 ? " (retraso de " + c.minutosRetraso() + " min)" : ""));
        filas.put("Llegada estimada", c.llegadaEstimada().format(FECHA_HORA));
        filas.put("Terminal de salida", c.terminalOrigen());
        filas.put("Terminal de llegada", c.terminalDestino());
        filas.put("Bus", c.placa());
        filas.put("Asientos", c.asientos().stream().map(a -> String.valueOf(a.numero())).collect(Collectors.joining(", ")));
        filas.put("Método de pago", c.metodoPago());
        filas.put("Fecha de compra", c.fechaCompra().format(FECHA_HORA));
        filas.put("Total pagado", "S/ " + c.total().setScale(2, RoundingMode.HALF_UP));

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{2, 3});
        Font etiqueta = FontFactory.getFont(FontFactory.HELVETICA, 10, GRIS);
        Font valor = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, TINTA);
        filas.forEach((clave, dato) -> {
            tabla.addCell(celdaDato(clave, etiqueta, Element.ALIGN_LEFT));
            tabla.addCell(celdaDato(dato, valor, Element.ALIGN_RIGHT));
        });
        return tabla;
    }

    private PdfPCell celdaDato(String texto, Font fuente, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto == null ? "-" : texto, fuente));
        celda.setBorder(Rectangle.BOTTOM);
        celda.setBorderColor(GRIS);
        celda.setPaddingTop(5);
        celda.setPaddingBottom(5);
        celda.setHorizontalAlignment(alineacion);
        return celda;
    }
}
