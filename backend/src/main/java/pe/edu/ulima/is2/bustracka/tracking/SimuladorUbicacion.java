package pe.edu.ulima.is2.bustracka.tracking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.ulima.is2.bustracka.model.PuntoRuta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.time.Duration;
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
    public UbicacionBus obtenerUbicacion(Viaje viaje, List<PuntoRuta> ruta) {
        long segundos = Duration.between(viaje.fechaSalida(), ZonaApp.ahora()).getSeconds();
        if (segundos < 0) {
            PuntoRuta origen = ruta.get(0);
            return new UbicacionBus(origen.latitud(), origen.longitud(), 0, "PROGRAMADO");
        }
        double duracionSeg = viaje.duracionMin() * 60.0;
        double progreso = ((segundos * velocidadSimulada) % duracionSeg) / duracionSeg;
        return interpolar(ruta, progreso);
    }

    /**
     * Ubica el punto a "progreso" del recorrido total recorriendo los tramos de
     * la polilinea (distancia euclidiana en grados: suficiente para una simulacion).
     */
    private UbicacionBus interpolar(List<PuntoRuta> ruta, double progreso) {
        double[] largos = new double[ruta.size() - 1];
        double total = 0;
        for (int i = 0; i < largos.length; i++) {
            largos[i] = distancia(ruta.get(i), ruta.get(i + 1));
            total += largos[i];
        }
        double objetivo = progreso * total;
        for (int i = 0; i < largos.length; i++) {
            if (objetivo <= largos[i] || i == largos.length - 1) {
                double t = largos[i] == 0 ? 0 : Math.min(1, objetivo / largos[i]);
                PuntoRuta a = ruta.get(i);
                PuntoRuta b = ruta.get(i + 1);
                return new UbicacionBus(a.latitud() + (b.latitud() - a.latitud()) * t,
                        a.longitud() + (b.longitud() - a.longitud()) * t, progreso, "EN_RUTA");
            }
            objetivo -= largos[i];
        }
        PuntoRuta ultimo = ruta.get(ruta.size() - 1);
        return new UbicacionBus(ultimo.latitud(), ultimo.longitud(), 1, "EN_RUTA");
    }

    private double distancia(PuntoRuta a, PuntoRuta b) {
        return Math.hypot(b.latitud() - a.latitud(), b.longitud() - a.longitud());
    }
}
