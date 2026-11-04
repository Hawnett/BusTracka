package pe.edu.ulima.is2.bustracka.model;

import java.time.LocalDateTime;

/** Enlace temporal para seguir un viaje sin iniciar sesion. */
public record EnlaceSeguimiento(Long idSeguimiento, Long idVenta, Long idViaje, String token,
                                LocalDateTime fechaCreacion, LocalDateTime fechaExpiracion, String estado) {

    public static final String ACTIVO = "ACTIVO";

    public boolean estaActivo() {
        return ACTIVO.equals(estado);
    }
}
