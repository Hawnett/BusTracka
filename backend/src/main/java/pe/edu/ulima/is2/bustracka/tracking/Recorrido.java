package pe.edu.ulima.is2.bustracka.tracking;

import pe.edu.ulima.is2.bustracka.model.PuntoRuta;

import java.util.List;

/** Kilometros de una ruta (Haversine) y posicion a un porcentaje del recorrido. */
public final class Recorrido {

    private static final double RADIO_TIERRA_KM = 6371.0;

    private final List<PuntoRuta> puntos;
    private final double[] acumuladoKm;

    public Recorrido(List<PuntoRuta> puntos) {
        if (puntos.size() < 2) {
            throw new IllegalArgumentException("Una ruta necesita al menos origen y destino");
        }
        this.puntos = puntos;
        this.acumuladoKm = new double[puntos.size()];
        for (int i = 1; i < puntos.size(); i++) {
            acumuladoKm[i] = acumuladoKm[i - 1] + distanciaKm(puntos.get(i - 1), puntos.get(i));
        }
    }

    public double totalKm() {
        return acumuladoKm[acumuladoKm.length - 1];
    }

    public double fraccionDelPunto(int indice) {
        return totalKm() == 0 ? 0 : acumuladoKm[indice] / totalKm();
    }

    public double[] posicionEn(double progreso) {
        double objetivo = Math.max(0, Math.min(1, progreso)) * totalKm();
        for (int i = 1; i < puntos.size(); i++) {
            if (objetivo <= acumuladoKm[i] || i == puntos.size() - 1) {
                double largo = acumuladoKm[i] - acumuladoKm[i - 1];
                double t = largo == 0 ? 0 : Math.min(1, (objetivo - acumuladoKm[i - 1]) / largo);
                PuntoRuta a = puntos.get(i - 1);
                PuntoRuta b = puntos.get(i);
                return new double[]{a.latitud() + (b.latitud() - a.latitud()) * t,
                        a.longitud() + (b.longitud() - a.longitud()) * t};
            }
        }
        PuntoRuta ultimo = puntos.get(puntos.size() - 1);
        return new double[]{ultimo.latitud(), ultimo.longitud()};
    }

    static double distanciaKm(PuntoRuta a, PuntoRuta b) {
        double dLat = Math.toRadians(b.latitud() - a.latitud());
        double dLon = Math.toRadians(b.longitud() - a.longitud());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a.latitud())) * Math.cos(Math.toRadians(b.latitud()))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * RADIO_TIERRA_KM * Math.asin(Math.sqrt(h));
    }
}
