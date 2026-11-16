package pe.edu.ulima.is2.bustracka.service;

import pe.edu.ulima.is2.bustracka.dto.EnlaceSeguimientoDTO;
import pe.edu.ulima.is2.bustracka.dto.SeguimientoCompartidoDTO;
import pe.edu.ulima.is2.bustracka.model.Venta;
import pe.edu.ulima.is2.bustracka.model.Viaje;

import java.util.Optional;

/** Compartir el viaje con un enlace temporal (Release 2, Sprint 3). */
public interface EnlaceSeguimientoService {

    /** Devuelve el enlace activo de la compra o crea uno si aun no tiene. */
    EnlaceSeguimientoDTO obtenerOCrear(Venta venta, Viaje viaje);

    /** URL del enlace activo, solo si la compra esta pagada y el viaje no termino. */
    Optional<String> urlVigente(Venta venta, Viaje viaje);

    EnlaceSeguimientoDTO generarParaCompra(Long idUsuario, Long idVenta);

    SeguimientoCompartidoDTO verCompartido(String token);
}
