package pe.edu.ulima.is2.bustracka.tracking;

import pe.edu.ulima.is2.bustracka.model.EstadoViaje;
import pe.edu.ulima.is2.bustracka.model.PuntoRuta;
import pe.edu.ulima.is2.bustracka.model.Viaje;

import java.util.ArrayList;
import java.util.List;

/** Paradas del viaje con su hora estimada (repartida segun los kilometros) y su estado. */
public final class CalculadoraItinerario {

    private static final double TOLERANCIA = 1e-6;

    private CalculadoraItinerario() {
    }

    public static List<ParadaItinerario> calcular(Viaje viaje, List<PuntoRuta> ruta, double progreso, EstadoViaje estado) {
        Recorrido recorrido = new Recorrido(ruta);
        List<Integer> indicesParada = indicesDeParadas(ruta);

        int actual = 0;
        if (estado == EstadoViaje.FINALIZADO) {
            actual = indicesParada.size() - 1;
        } else if (estado == EstadoViaje.EN_RUTA) {
            for (int i = 0; i < indicesParada.size(); i++) {
                if (recorrido.fraccionDelPunto(indicesParada.get(i)) <= progreso + TOLERANCIA) {
                    actual = i;
                }
            }
        }

        List<ParadaItinerario> paradas = new ArrayList<>();
        for (int i = 0; i < indicesParada.size(); i++) {
            int indice = indicesParada.get(i);
            PuntoRuta punto = ruta.get(indice);
            long minutos = Math.round(viaje.duracionMin() * recorrido.fraccionDelPunto(indice));
            String estadoParada;
            if (estado == EstadoViaje.FINALIZADO || i < actual) {
                estadoParada = ParadaItinerario.PASO;
            } else if (i == actual) {
                estadoParada = ParadaItinerario.ACTUAL;
            } else {
                estadoParada = ParadaItinerario.PROXIMA;
            }
            paradas.add(new ParadaItinerario(nombreDeParada(viaje, ruta, indice), viaje.salidaEstimada().plusMinutes(minutos),
                    estadoParada, punto.latitud(), punto.longitud()));
        }
        return paradas;
    }

    /** Origen, destino y los puntos intermedios con nombre. */
    private static List<Integer> indicesDeParadas(List<PuntoRuta> ruta) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < ruta.size(); i++) {
            boolean extremo = i == 0 || i == ruta.size() - 1;
            if (extremo || ruta.get(i).nombre() != null) {
                indices.add(i);
            }
        }
        return indices;
    }

    private static String nombreDeParada(Viaje viaje, List<PuntoRuta> ruta, int indice) {
        if (indice == 0) {
            return viaje.terminalOrigen();
        }
        if (indice == ruta.size() - 1) {
            return viaje.terminalDestino();
        }
        return ruta.get(indice).nombre();
    }
}
