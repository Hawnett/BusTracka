package pe.edu.ulima.is2.bustracka.exception;

/** Se lanza cuando se busca un usuario u otro recurso que no existe. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
