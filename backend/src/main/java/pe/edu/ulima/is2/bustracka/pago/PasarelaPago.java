package pe.edu.ulima.is2.bustracka.pago;

/**
 * Puerto hacia la pasarela de pagos (DIP). VentaServiceImpl solo conoce esta
 * interfaz; para usar una pasarela real basta con crear otra implementacion.
 */
public interface PasarelaPago {
    ResultadoPago cobrar(SolicitudPago solicitud);

    /** Reversa un cobro aprobado (por ejemplo si despues falla el registro de la venta). */
    void anular(String codigoTransaccion);
}
