package pe.edu.ulima.is2.bustracka.notificacion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Registra el correo en el log y, si se configura notificacion.webhook-url,
 * tambien lo envia por POST (JSON) a un webhook, que es como se integraria n8n.
 */
@Component
public class NotificacionServiceSimulada implements NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionServiceSimulada.class);

    private final String webhookUrl;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    public NotificacionServiceSimulada(@Value("${notificacion.webhook-url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void enviarComprobante(String emailDestino, String asunto, String cuerpo) {
        log.info("[SIMULACION DE EMAIL] Para: {} | Asunto: {}\n{}", emailDestino, asunto, cuerpo);
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        try {
            String json = "{\"para\":\"" + escapar(emailDestino) + "\",\"asunto\":\"" + escapar(asunto)
                    + "\",\"cuerpo\":\"" + escapar(cuerpo) + "\"}";
            HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            http.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            log.warn("No se pudo notificar al webhook (la compra no se ve afectada): {}", e.getMessage());
        }
    }

    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }
}
