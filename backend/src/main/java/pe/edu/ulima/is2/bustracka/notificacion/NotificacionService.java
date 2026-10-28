package pe.edu.ulima.is2.bustracka.notificacion;

/** Envio de correos (en el informe lo hace n8n). Los errores nunca deben tumbar la compra. */
public interface NotificacionService {
    void enviarComprobante(String emailDestino, String asunto, String cuerpo);
}
