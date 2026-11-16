package pe.edu.ulima.is2.bustracka.exception;

/** Enlace de seguimiento inexistente, revocado o vencido (410). */
public class EnlaceInvalidoException extends RuntimeException {
    public EnlaceInvalidoException(String mensaje) {
        super(mensaje);
    }
}
