package pe.edu.ulima.is2.bustracka.exception;

/**
 * Envuelve un SQLException para no filtrar detalles de infraestructura
 * (JDBC) hacia la capa de servicio/controller (principio de encapsulamiento
 * entre capas).
 */
public class PersistenciaException extends RuntimeException {
    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
