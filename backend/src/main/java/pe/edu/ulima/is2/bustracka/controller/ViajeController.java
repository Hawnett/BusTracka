package pe.edu.ulima.is2.bustracka.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pe.edu.ulima.is2.bustracka.dto.AsientoResponseDTO;
import pe.edu.ulima.is2.bustracka.dto.ViajeResponseDTO;
import pe.edu.ulima.is2.bustracka.service.ViajeService;

import java.time.LocalDate;
import java.util.List;

/** Consulta de viajes, horarios y asientos (Sprint 2). Capa delgada: delega en ViajeService. */
@RestController
@RequestMapping("/api/viajes")
public class ViajeController {

    private final ViajeService viajeService;

    public ViajeController(ViajeService viajeService) {
        this.viajeService = viajeService;
    }

    @GetMapping
    public List<ViajeResponseDTO> buscar(
            @RequestParam(required = false) Long origen,
            @RequestParam(required = false) Long destino,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return viajeService.buscarViajes(origen, destino, fecha);
    }

    @GetMapping("/en-curso")
    public List<ViajeResponseDTO> enCurso() {
        return viajeService.listarViajesEnCurso();
    }

    @GetMapping("/{id}")
    public ViajeResponseDTO obtener(@PathVariable Long id) {
        return viajeService.obtenerViaje(id);
    }

    @GetMapping("/{id}/asientos")
    public List<AsientoResponseDTO> asientos(@PathVariable Long id) {
        return viajeService.listarAsientos(id);
    }
}
