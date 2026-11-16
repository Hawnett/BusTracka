package pe.edu.ulima.is2.bustracka.notificacion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/**
 * Registra el correo en el log y, si se configura notificacion.webhook-url,
 * tambien lo envia por POST (JSON) a un webhook, que es como se integra n8n.
 * El envio es asincrono: la compra no espera a n8n.
 */
@Component
public class NotificacionServiceSimulada implements NotificacionService {

    private static final Logger log = LoggerFactory.getLogger(NotificacionServiceSimulada.class);

    private final String webhookUrl;
    private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(3)).build();
    private final ObjectMapper json = new ObjectMapper();

    public NotificacionServiceSimulada(@Value("${notificacion.webhook-url:}") String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    @Override
    public void enviarCorreo(String emailDestino, String asunto, String texto, String html) {
        log.info("[SIMULACION DE EMAIL] Para: {} | Asunto: {}\n{}", emailDestino, asunto, texto);
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }
        try {
            String cuerpo = json.writeValueAsString(Map.of("para", emailDestino, "asunto", asunto,
                    "cuerpo", texto, "html", html == null ? "" : html));
            HttpRequest request = HttpRequest.newBuilder(URI.create(webhookUrl))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
                    .build();
            http.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                    .exceptionally(e -> {
                        log.warn("No se pudo notificar al webhook (la compra no se ve afectada): {}", e.getMessage());
                        return null;
                    });
        } catch (JsonProcessingException | IllegalArgumentException e) {
            log.warn("No se pudo armar la notificacion para el webhook: {}", e.getMessage());
        }
    }
}
