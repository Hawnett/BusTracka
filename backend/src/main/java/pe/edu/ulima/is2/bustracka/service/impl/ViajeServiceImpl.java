package pe.edu.ulima.is2.bustracka.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.AsientoResponseDTO;
import pe.edu.ulima.is2.bustracka.dto.RetrasoRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.ViajeResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.exception.ReglaNegocioException;
import pe.edu.ulima.is2.bustracka.model.EstadoViaje;
import pe.edu.ulima.is2.bustracka.model.FranjaHoraria;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.service.ViajeService;
import pe.edu.ulima.is2.bustracka.util.ZonaApp;

import java.time.LocalDate;
import java.util.List;

@Service
public class ViajeServiceImpl implements ViajeService {

    private static final int MAX_PASAJEROS_POR_BUSQUEDA = 6;

    private final ViajeDao viajeDao = DaoFactory.crearViajeDao();
    private final AsientoDao asientoDao = DaoFactory.crearAsientoDao();

    @Override
    public List<ViajeResponseDTO> buscarViajes(Long idTerminalOrigen, Long idTerminalDestino, LocalDate fecha,
                                               Integer pasajeros, FranjaHoraria franja) {
        int cantidad = pasajeros == null ? 1 : pasajeros;
        if (cantidad < 1 || cantidad > MAX_PASAJEROS_POR_BUSQUEDA) {
            throw new ReglaNegocioException("La cantidad de pasajeros debe estar entre 1 y " + MAX_PASAJEROS_POR_BUSQUEDA);
        }
        if (idTerminalOrigen != null && idTerminalOrigen.equals(idTerminalDestino)) {
            throw new ReglaNegocioException("El origen y el destino deben ser distintos");
        }
        if (fecha != null && fecha.isBefore(ZonaApp.ahora().toLocalDate())) {
            throw new ReglaNegocioException("La fecha de viaje no puede ser anterior a hoy");
        }
        return viajeDao.buscar(idTerminalOrigen, idTerminalDestino, fecha, cantidad, franja).stream().map(this::aDto).toList();
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
    public ViajeResponseDTO registrarRetraso(Long idViaje, RetrasoRequestDTO datos) {
        Viaje viaje = buscar(idViaje);
        if (viaje.estadoEn(ZonaApp.ahora()) == EstadoViaje.FINALIZADO) {
            throw new ReglaNegocioException("El viaje ya llego a su destino, no se puede reprogramar");
        }
        // Volver a la hora programada (0 minutos) no es un retraso: no se avisa a nadie.
        boolean avisar = datos.avisarPasajeros() && datos.minutosRetraso() > 0;
        viajeDao.actualizarRetraso(idViaje, datos.minutosRetraso(), datos.motivo().name(), datos.detalle().trim(), avisar);
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