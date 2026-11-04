package pe.edu.ulima.is2.bustracka.tracking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.ulima.is2.bustracka.model.EstadoViaje;
import pe.edu.ulima.is2.bustracka.model.PuntoRuta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Simula el GPS del bus a partir del tiempo transcurrido desde la salida.
 * Sin estado ni hilos: la posicion se calcula en cada consulta, por lo que
 * es segura para varios usuarios concurrentes. La velocidad simulada acelera
 * el viaje para poder verlo moverse en la demo y, al llegar, vuelve a empezar.
 */
@Component
public class SimuladorUbicacion implements ProveedorUbicacion {

    private final double velocidadSimulada;

    public SimuladorUbicacion(@Value("${tracking.velocidad-simulada:60}") double velocidadSimulada) {
        this.velocidadSimulada = velocidadSimulada;
    }

    @Override
    public double aceleracion() {
        return velocidadSimulada;
    }

    @Override
    public UbicacionBus obtenerUbicacion(Viaje viaje, List<PuntoRuta> ruta) {
        LocalDateTime ahora = ZonaApp.ahora();
        EstadoViaje estado = viaje.estadoEn(ahora);
        if (estado == EstadoViaje.PROGRAMADO) {
            PuntoRuta origen = ruta.get(0);
            return new UbicacionBus(origen.latitud(), origen.longitud(), 0, estado.name());
        }
        if (estado == EstadoViaje.FINALIZADO) {
            PuntoRuta destino = ruta.get(ruta.size() - 1);
            return new UbicacionBus(destino.latitud(), destino.longitud(), 1, estado.name());
        }
        long segundos = Duration.between(viaje.salidaEstimada(), ahora).getSeconds();
        double duracionSeg = viaje.duracionMin() * 60.0;
        double progreso = ((segundos * velocidadSimulada) % duracionSeg) / duracionSeg;
        double[] posicion = new Recorrido(ruta).posicionEn(progreso);
        return new UbicacionBus(posicion[0], posicion[1], progreso, estado.name());
    }
}
