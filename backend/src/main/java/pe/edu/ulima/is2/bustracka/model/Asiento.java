package pe.edu.ulima.is2.bustracka.model;

/** Asiento de un bus, con su estado para un viaje concreto. */
public record Asiento(Long idAsiento, int numero, boolean ocupado) {
}
