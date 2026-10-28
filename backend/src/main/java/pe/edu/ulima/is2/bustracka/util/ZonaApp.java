package pe.edu.ulima.is2.bustracka.util;

import java.time.LocalDateTime;
import java.time.ZoneId;

/** Zona horaria unica de la aplicacion (las fechas de viaje se guardan en hora de Lima). */
public final class ZonaApp {
    public static final ZoneId ZONA = ZoneId.of("America/Lima");

    private ZonaApp() {
    }

    public static LocalDateTime ahora() {
        return LocalDateTime.now(ZONA);
    }
}
