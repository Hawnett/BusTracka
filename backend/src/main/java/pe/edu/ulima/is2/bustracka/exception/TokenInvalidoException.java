package pe.edu.ulima.is2.bustracka.exception;

/** Se lanza cuando un JWT (de sesion o de recuperacion de contrasena) es invalido o expiro. */
public class TokenInvalidoException extends RuntimeException {
    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
