package pe.edu.ulima.is2.bustracka.service.impl;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.RutaDao;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.dto.PuntoRutaDTO;
import pe.edu.ulima.is2.bustracka.dto.UbicacionResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.model.PuntoRuta;
import pe.edu.ulima.is2.bustracka.model.Viaje;
import pe.edu.ulima.is2.bustracka.security.JwtUtil;
import pe.edu.ulima.is2.bustracka.service.SeguimientoService;
import pe.edu.ulima.is2.bustracka.tracking.ProveedorUbicacion;
import pe.edu.ulima.is2.bustracka.tracking.UbicacionBus;

import java.util.List;

@Service
public class SeguimientoServiceImpl implements SeguimientoService {

    private final ViajeDao viajeDao = DaoFactory.crearViajeDao();
    private final RutaDao rutaDao = DaoFactory.crearRutaDao();
    private final ProveedorUbicacion proveedorUbicacion;
    private final JwtUtil jwtUtil;

    public SeguimientoServiceImpl(ProveedorUbicacion proveedorUbicacion, JwtUtil jwtUtil) {
        this.proveedorUbicacion = proveedorUbicacion;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UbicacionResponseDTO obtenerUbicacion(Long idViaje) {
        Viaje viaje = viajeDao.buscarPorId(idViaje)
                .orElseThrow(() -> new RecursoNoEncontradoException("El viaje no existe"));
        List<PuntoRuta> ruta = rutaDao.listarPuntos(viaje.idRuta());
        UbicacionBus ubicacion = proveedorUbicacion.obtenerUbicacion(viaje, ruta);
        return new UbicacionResponseDTO(viaje.idViaje(), viaje.ciudadOrigen(), viaje.ciudadDestino(),
                ubicacion.latitud(), ubicacion.longitud(), ubicacion.progreso(), ubicacion.estado(),
                ruta.stream().map(p -> new PuntoRutaDTO(p.latitud(), p.longitud())).toList());
    }

    @Override
    public UbicacionResponseDTO obtenerUbicacionPorToken(String tokenSeguimiento) {
        Claims claims = jwtUtil.validarYObtenerClaims(tokenSeguimiento, "SEGUIMIENTO");
        Long idViaje = claims.get("idViaje", Integer.class).longValue();
        return obtenerUbicacion(idViaje);
    }
}
