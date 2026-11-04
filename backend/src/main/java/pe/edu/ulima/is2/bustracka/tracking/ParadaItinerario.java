package pe.edu.ulima.is2.bustracka.tracking;

import java.time.LocalDateTime;

/** Parada con su hora estimada. ACTUAL = ultima parada por la que paso el bus. */
public record ParadaItinerario(String nombre, LocalDateTime hora, String estado, double latitud, double longitud) {

    public static final String PASO = "PASO";
    public static final String ACTUAL = "ACTUAL";
    public static final String PROXIMA = "PROXIMA";
}
