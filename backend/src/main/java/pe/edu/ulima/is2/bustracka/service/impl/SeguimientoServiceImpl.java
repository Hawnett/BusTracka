package pe.edu.ulima.is2.bustracka.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.RutaDao;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.ParadaDTO;
import pe.edu.ulima.is2.bustracka.dto.PuntoRutaDTO;
import pe.edu.ulima.is2.bustracka.dto.UbicacionResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.model.EstadoViaje;
import pe.edu.ulima.is2.bustracka.model.PuntoRuta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.service.SeguimientoService;
import pe.edu.ulima.is2.bustracka.tracking.CalculadoraItinerario;
import pe.edu.ulima.is2.bustracka.tracking.ParadaItinerario;
import pe.edu.ulima.is2.bustracka.tracking.ProveedorUbicacion;
import pe.edu.ulima.is2.bustracka.tracking.Recorrido;
import pe.edu.ulima.is2.bustracka.tracking.UbicacionBus;

import java.util.List;

@Service
public class SeguimientoServiceImpl implements SeguimientoService {

    private final ViajeDao viajeDao = DaoFactory.crearViajeDao();
    private final RutaDao rutaDao = DaoFactory.crearRutaDao();
    private final ProveedorUbicacion proveedorUbicacion;

    public SeguimientoServiceImpl(ProveedorUbicacion proveedorUbicacion) {
        this.proveedorUbicacion = proveedorUbicacion;
    }

    @Override
    public UbicacionResponseDTO obtenerUbicacion(Long idViaje) {
        Viaje viaje = viajeDao.buscarPorId(idViaje)
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));
        List<PuntoRuta> ruta = rutaDao.listarPuntos(viaje.idRuta());
        UbicacionBus ubicacion = proveedorUbicacion.obtenerUbicacion(viaje, ruta);
        EstadoViaje estado = EstadoViaje.valueOf(ubicacion.estado());

        List<ParadaDTO> paradas = CalculadoraItinerario.calcular(viaje, ruta, ubicacion.progreso(), estado).stream()
                .map(p -> new ParadaDTO(p.nombre(), p.hora(), p.estado(), p.latitud(), p.longitud()))
                .toList();
        String proximaParada = paradas.stream()
                .filter(p -> ParadaItinerario.PROXIMA.equals(p.estado()))
                .map(ParadaDTO::nombre)
                .findFirst().orElse(null);

        // Velocidad promedio del itinerario: el GPS simulado no mide velocidad real.
        Recorrido recorrido = new Recorrido(ruta);
        double velocidad = estado == EstadoViaje.EN_RUTA ? recorrido.totalKm() / (viaje.duracionMin() / 60.0) : 0;

        return new UbicacionResponseDTO(viaje.idViaje(), viaje.ciudadOrigen(), viaje.ciudadDestino(),
                ubicacion.latitud(), ubicacion.longitud(), ubicacion.progreso(), ubicacion.estado(),
                ruta.stream().map(p -> new PuntoRutaDTO(p.latitud(), p.longitud())).toList(),
                viaje.placa(), viaje.terminalOrigen(), viaje.terminalDestino(), viaje.fechaSalida(),
                viaje.minutosRetraso(), viaje.salidaEstimada(), viaje.llegadaEstimada(),
                Math.round(velocidad), Math.round(recorrido.totalKm()),
                Math.round(recorrido.totalKm() * ubicacion.progreso()), proximaParada, paradas,
                proveedorUbicacion.aceleracion());
    }
}
