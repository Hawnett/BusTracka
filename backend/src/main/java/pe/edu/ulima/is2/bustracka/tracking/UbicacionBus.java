package pe.edu.ulima.is2.bustracka.tracking;

/** Posicion calculada de un bus. progreso va de 0 (origen) a 1 (destino). */
public record UbicacionBus(double latitud, double longitud, double progreso, String estado) {
}
