package pe.edu.ulima.is2.bustracka.service;

import pe.edu.ulima.is2.bustracka.dto.UbicacionResponseDTO;

public interface SeguimientoService {
    UbicacionResponseDTO obtenerUbicacion(Long idViaje);
}
