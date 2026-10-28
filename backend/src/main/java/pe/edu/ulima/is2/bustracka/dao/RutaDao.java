package pe.edu.ulima.is2.bustracka.dao;

import pe.edu.ulima.is2.bustracka.model.PuntoRuta;
import java.util.List;

public interface RutaDao {
    /** Puntos de la ruta ordenados de origen a destino. */
    List<PuntoRuta> listarPuntos(Long idRuta);
}
