package pe.edu.ulima.is2.bustracka.dao;

import pe.edu.ulima.is2.bustracka.model.Viaje;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ViajeDao {
    /** Viajes futuros; cualquier filtro en null se ignora. */
    List<Viaje> buscar(Long idTerminalOrigen, Long idTerminalDestino, LocalDate fecha);

    /** Viajes que ya salieron (para el seguimiento en el mapa). */
    List<Viaje> listarEnCurso();

    Optional<Viaje> buscarPorId(Long idViaje);
}
