package pe.edu.ulima.is2.bustracka.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.ulima.is2.bustracka.dto.UbicacionResponseDTO;
import pe.edu.ulima.is2.bustracka.service.SeguimientoService;

/** Seguimiento del bus en tiempo real (Sprint 3). */
@RestController
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService) {
        this.seguimientoService = seguimientoService;
    }

    /** Con sesion iniciada. */
    @GetMapping("/api/seguimiento/{idViaje}")
    public UbicacionResponseDTO ubicacion(@PathVariable Long idViaje) {
        return seguimientoService.obtenerUbicacion(idViaje);
    }

    /** Sin sesion: se valida el token del enlace (ver JwtAuthFilter, ruta /api/publico). */
    @GetMapping("/api/publico/seguimiento")
    public UbicacionResponseDTO ubicacionPublica(@RequestParam String token) {
        return seguimientoService.obtenerUbicacionPorToken(token);
    }
}
