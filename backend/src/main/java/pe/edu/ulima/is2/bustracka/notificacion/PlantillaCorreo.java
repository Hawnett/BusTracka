package pe.edu.ulima.is2.bustracka.notificacion;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Arma el correo en texto y en HTML con el mismo contenido. El HTML usa
 * estilos en linea porque los clientes de correo ignoran las hojas de estilo.
 */
public final class PlantillaCorreo {

    private final String saludo;
    private final StringBuilder cuerpoTexto = new StringBuilder();
    private final StringBuilder cuerpoHtml = new StringBuilder();

    public PlantillaCorreo(String nombre) {
        this.saludo = "Hola, " + nombre;
    }

    public PlantillaCorreo parrafo(String contenido) {
        cuerpoTexto.append(contenido).append("\n\n");
        cuerpoHtml.append("<p style=\"margin:0 0 16px;font-size:15px;line-height:1.5\">").append(escapar(contenido)).append("</p>");
        return this;
    }

    public PlantillaCorreo datos(Map<String, String> filas) {
        cuerpoHtml.append("<table style=\"width:100%;border-collapse:collapse;margin:0 0 20px;font-size:14px\">");
        filas.forEach((etiqueta, valor) -> {
            cuerpoTexto.append(etiqueta).append(": ").append(valor).append("\n");
            cuerpoHtml.append("<tr><td style=\"padding:6px 0;color:#5a524a;border-bottom:1px dotted #5a524a\">")
                    .append(escapar(etiqueta)).append("</td><td style=\"padding:6px 0;text-align:right;font-weight:bold;border-bottom:1px dotted #5a524a\">")
                    .append(escapar(valor)).append("</td></tr>");
        });
        cuerpoHtml.append("</table>");
        cuerpoTexto.append("\n");
        return this;
    }

    public PlantillaCorreo imagenQr(String pngBase64, String leyenda) {
        cuerpoHtml.append("<div style=\"text-align:center;margin:0 0 20px\"><img alt=\"Código QR\" width=\"180\" height=\"180\" ")
                .append("style=\"border:2px solid #14110f;padding:6px;background:#ffffff\" src=\"data:image/png;base64,")
                .append(pngBase64).append("\"><div style=\"font-size:12px;color:#5a524a;margin-top:6px\">")
                .append(escapar(leyenda)).append("</div></div>");
        return this;
    }

    public PlantillaCorreo boton(String etiqueta, String url, boolean principal) {
        cuerpoTexto.append(etiqueta).append(": ").append(url).append("\n");
        String fondo = principal ? "#ffc400" : "#fbf8f2";
        cuerpoHtml.append("<p style=\"margin:0 0 12px\"><a href=\"").append(escapar(url)).append("\" style=\"display:block;padding:12px 16px;")
                .append("background:").append(fondo).append(";color:#14110f;border:2px solid #14110f;text-align:center;")
                .append("font-weight:bold;text-decoration:none;text-transform:uppercase;letter-spacing:1px\">")
                .append(escapar(etiqueta)).append("</a></p>");
        return this;
    }

    public String texto() {
        return saludo + "\n\n" + cuerpoTexto + "Correo automático, no responder.";
    }

    public String html() {
        return "<div style=\"background:#f4efe6;padding:24px;font-family:Arial,Helvetica,sans-serif;color:#14110f\">"
                + "<div style=\"max-width:560px;margin:0 auto;background:#fbf8f2;border:2px solid #14110f\">"
                + "<div style=\"background:#14110f;color:#ffc400;padding:16px 24px;font-size:24px;font-weight:bold;letter-spacing:2px\">BUSTRACKA</div>"
                + "<div style=\"padding:24px\"><p style=\"margin:0 0 16px;font-size:20px\">" + escapar(saludo) + "</p>"
                + cuerpoHtml + "</div>"
                + "<div style=\"padding:12px 24px;border-top:2px dashed #14110f;font-size:12px;color:#5a524a\">"
                + "Correo automático, no responder. &copy; 2026 BusTracka</div></div></div>";
    }

    public static Map<String, String> filas() {
        return new LinkedHashMap<>();
    }

    private static String escapar(String valor) {
        return valor == null ? "" : valor.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
