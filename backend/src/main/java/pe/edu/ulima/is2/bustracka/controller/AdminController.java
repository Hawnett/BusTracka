package pe.edu.ulima.is2.bustracka.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pe.edu.ulima.is2.bustracka.dto.RetrasoRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.ViajeResponseDTO;
import pe.edu.ulima.is2.bustracka.service.ViajeService;

import java.util.List;

/** Operaciones del administrador (el filtro JWT exige el rol ADMINISTRADOR en /api/admin). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ViajeService viajeService;

    public AdminController(ViajeService viajeService) {
        this.viajeService = viajeService;
    }

    @GetMapping("/viajes")
    public List<ViajeResponseDTO> viajesNoFinalizados() {
        return viajeService.listarViajesNoFinalizados();
    }

    @PutMapping("/viajes/{id}/retraso")
    public ViajeResponseDTO registrarRetraso(@PathVariable Long id, @Valid @RequestBody RetrasoRequestDTO request) {
        return viajeService.registrarRetraso(id, request.minutosRetraso());
    }
}
