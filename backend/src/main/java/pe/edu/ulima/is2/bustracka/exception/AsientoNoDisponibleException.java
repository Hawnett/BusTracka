package pe.edu.ulima.is2.bustracka.exception;

/** Se lanza cuando un asiento ya fue vendido (409). */
public class AsientoNoDisponibleException extends RuntimeException {
    public AsientoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
