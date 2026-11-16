package pe.edu.ulima.is2.bustracka.dao;

import pe.edu.ulima.is2.bustracka.model.Viaje;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ViajeDao {
    /** Viajes futuros; cualquier filtro en null se ignora. */
    List<Viaje> buscar(Long idTerminalOrigen, Long idTerminalDestino, LocalDate fecha);

    /** Viajes que ya salieron y aun no llegan (para el seguimiento en el mapa). */
    List<Viaje> listarEnCurso();

    List<Viaje> listarNoFinalizados();

    Optional<Viaje> buscarPorId(Long idViaje);

    void actualizarRetraso(Long idViaje, int minutosRetraso);
}
