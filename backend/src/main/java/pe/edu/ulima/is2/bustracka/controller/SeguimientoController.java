package pe.edu.ulima.is2.bustracka.controller;

import org.springframework.web.bind.annotation.*;
import pe.edu.ulima.is2.bustracka.dto.SeguimientoCompartidoDTO;
import pe.edu.ulima.is2.bustracka.dto.UbicacionResponseDTO;
import pe.edu.ulima.is2.bustracka.service.EnlaceSeguimientoService;
import pe.edu.ulima.is2.bustracka.service.SeguimientoService;

/** Seguimiento del bus en tiempo real (Sprint 3) y enlace compartido. */
@RestController
public class SeguimientoController {

    private final SeguimientoService seguimientoService;
    private final EnlaceSeguimientoService enlaceSeguimientoService;

    public SeguimientoController(SeguimientoService seguimientoService, EnlaceSeguimientoService enlaceSeguimientoService) {
        this.seguimientoService = seguimientoService;
        this.enlaceSeguimientoService = enlaceSeguimientoService;
    }

    /** Con sesion iniciada. */
    @GetMapping("/api/seguimiento/{idViaje}")
    public UbicacionResponseDTO ubicacion(@PathVariable Long idViaje) {
        return seguimientoService.obtenerUbicacion(idViaje);
    }

    /** Sin sesion: se valida el token del enlace (ver JwtAuthFilter, ruta /api/publico). */
    @GetMapping("/api/publico/seguimiento")
    public SeguimientoCompartidoDTO seguimientoCompartido(@RequestParam String token) {
        return enlaceSeguimientoService.verCompartido(token);
    }
}
