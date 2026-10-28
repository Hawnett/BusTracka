package pe.edu.ulima.is2.bustracka.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.AsientoResponseDTO;
import pe.edu.ulima.is2.bustracka.dto.ViajeResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.service.ViajeService;

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
        return aDto(viajeDao.buscarPorId(idViaje)
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe")));
    }

    @Override
    public List<AsientoResponseDTO> listarAsientos(Long idViaje) {
        viajeDao.buscarPorId(idViaje).orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));
        return asientoDao.listarPorViaje(idViaje).stream()
                .map(a -> new AsientoResponseDTO(a.idAsiento(), a.numero(), a.ocupado()))
                .toList();
    }

    private ViajeResponseDTO aDto(Viaje v) {
        return new ViajeResponseDTO(v.idViaje(), v.placa(), v.fechaSalida(), v.precio(), v.duracionMin(),
                v.idTerminalOrigen(), v.ciudadOrigen(), v.idTerminalDestino(), v.ciudadDestino(),
                v.capacidad(), v.asientosDisponibles());
    }
}
