package pe.edu.ulima.is2.bustracka.exception;

/** Se lanza al intentar registrar un email que ya existe en la tabla usuario. */
public class CorreoDuplicadoException extends RuntimeException {
    public CorreoDuplicadoException(String email) {
        super("Ya existe una cuenta registrada con el email: " + email);
    }
}
