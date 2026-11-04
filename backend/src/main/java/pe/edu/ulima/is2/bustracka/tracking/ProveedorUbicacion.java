package pe.edu.ulima.is2.bustracka.tracking;

import pe.edu.ulima.is2.bustracka.model.PuntoRuta;
import pe.edu.ulima.is2.bustracka.model.Viaje;

import java.util.List;

/**
 * Abstraccion de "de donde sale la posicion del bus" (DIP). Hoy la implementa
 * SimuladorUbicacion; mañana podria ser una implementacion que lea el GPS real
 * sin tocar SeguimientoService.
 */
public interface ProveedorUbicacion {
    UbicacionBus obtenerUbicacion(Viaje viaje, List<PuntoRuta> ruta);

    /** Cuantas veces mas rapido que el horario se mueve el bus (1 = tiempo real). */
    default double aceleracion() {
        return 1;
    }
}
