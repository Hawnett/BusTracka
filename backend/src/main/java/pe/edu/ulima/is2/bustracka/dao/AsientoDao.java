package pe.edu.ulima.is2.bustracka.dao;

import pe.edu.ulima.is2.bustracka.model.Asiento;
import java.util.List;

public interface AsientoDao {
    /** Todos los asientos del bus del viaje, marcando cuales ya estan vendidos. */
    List<Asiento> listarPorViaje(Long idViaje);
}
