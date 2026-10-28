package pe.edu.ulima.is2.bustracka.service;

import pe.edu.ulima.is2.bustracka.dto.UbicacionResponseDTO;

public interface SeguimientoService {
    UbicacionResponseDTO obtenerUbicacion(Long idViaje);

    /** Seguimiento a partir del enlace compartido (token firmado, sin sesion). */
    UbicacionResponseDTO obtenerUbicacionPorToken(String tokenSeguimiento);
}
