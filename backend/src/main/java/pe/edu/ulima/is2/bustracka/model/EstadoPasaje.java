package pe.edu.ulima.is2.bustracka.model;

import java.time.LocalDateTime;

/** Estado de la compra en el historial; se calcula, no se guarda. */
public enum EstadoPasaje {
    PROXIMO,
    EN_CURSO,
    COMPLETADO,
    CANCELADO;

    public static EstadoPasaje de(Venta venta, Viaje viaje, LocalDateTime ahora) {
        if (!venta.estaPagada()) {
            return CANCELADO;
        }
        return switch (viaje.estadoEn(ahora)) {
            case PROGRAMADO -> PROXIMO;
            case EN_RUTA -> EN_CURSO;
            case FINALIZADO -> COMPLETADO;
        };
    }
}
