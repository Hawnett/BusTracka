package pe.edu.ulima.is2.bustracka.exception;

/** Se lanza cuando la solicitud viola una regla de negocio (400). */
public class ReglaNegocioException extends RuntimeException {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
