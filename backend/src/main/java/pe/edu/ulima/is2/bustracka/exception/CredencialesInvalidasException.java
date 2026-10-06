package pe.edu.ulima.is2.bustracka.exception;

/**
 * Se lanza cuando el email no existe o la contrasena no coincide.
 * A proposito NO distinguimos el motivo en el mensaje (buena practica de
 * seguridad: no revelar si el email existe o no).
 */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Email o contrasena incorrectos");
    }
}
