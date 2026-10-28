package pe.edu.ulima.is2.bustracka.model;

import java.math.BigDecimal;

public record DetalleVenta(Long idViaje, Long idAsiento, int numeroAsiento, String nombrePasajero, BigDecimal precio) {
}
