package pe.edu.ulima.is2.bustracka.exception;

/** Se lanza cuando la pasarela rechaza el cobro (402). */
public class PagoRechazadoException extends RuntimeException {
    public PagoRechazadoException(String mensaje) {
        super(mensaje);
    }
}
