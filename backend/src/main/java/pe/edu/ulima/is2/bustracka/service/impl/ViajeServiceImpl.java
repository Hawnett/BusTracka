package pe.edu.ulima.is2.bustracka.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.AsientoResponseDTO;
import pe.edu.ulima.is2.bustracka.dto.ViajeResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;
import pe.edu.ulima.is2.bustracka.model.EstadoViaje;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.service.ViajeService;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.time.LocalDate;
import java.util.List;

@Service
public class ViajeServiceImpl implements ViajeService {

    private final ViajeDao viajeDao = DaoFactory.crearViajeDao();
    private final AsientoDao asientoDao = DaoFactory.crearAsientoDao();

    @Override
    public List<ViajeResponseDTO> buscarViajes(Long idTerminalOrigen, Long idTerminalDestino, LocalDate fecha) {
        return viajeDao.buscar(idTerminalOrigen, idTerminalDestino, fecha).stream().map(this::aDto).toList();
    }

    @Override
    public List<ViajeResponseDTO> listarViajesEnCurso() {
        return viajeDao.listarEnCurso().stream().map(this::aDto).toList();
    }

    @Override
    public ViajeResponseDTO obtenerViaje(Long idViaje) {
        return aDto(buscar(idViaje));
    }

    @Override
    public List<AsientoResponseDTO> listarAsientos(Long idViaje) {
        buscar(idViaje);
        return asientoDao.listarPorViaje(idViaje).stream()
                .map(a -> new AsientoResponseDTO(a.idAsiento(), a.numero(), a.ocupado()))
                .toList();
    }

    @Override
    public List<ViajeResponseDTO> listarViajesNoFinalizados() {
        return viajeDao.listarNoFinalizados().stream().map(this::aDto).toList();
    }

    @Override
    public ViajeResponseDTO registrarRetraso(Long idViaje, int minutosRetraso) {
        Viaje viaje = buscar(idViaje);
        if (viaje.estadoEn(ZonaApp.ahora()) == EstadoViaje.FINALIZADO) {
            throw new ReglaNegocioException("El viaje ya llego a su destino, no se puede reprogramar");
        }
        viajeDao.actualizarRetraso(idViaje, minutosRetraso);
        return obtenerViaje(idViaje);
    }

    private Viaje buscar(Long idViaje) {
        return viajeDao.buscarPorId(idViaje)
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));
    }

    private ViajeResponseDTO aDto(Viaje v) {
        return new ViajeResponseDTO(v.idViaje(), v.placa(), v.fechaSalida(), v.precio(), v.duracionMin(),
                v.idTerminalOrigen(), v.terminalOrigen(), v.ciudadOrigen(),
                v.idTerminalDestino(), v.terminalDestino(), v.ciudadDestino(),
                v.capacidad(), v.asientosDisponibles(), v.minutosRetraso(),
                v.salidaEstimada(), v.llegadaEstimada(), v.estadoEn(ZonaApp.ahora()).name());
    }
}
