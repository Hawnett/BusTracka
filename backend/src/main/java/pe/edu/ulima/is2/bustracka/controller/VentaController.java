package pe.edu.ulima.is2.bustracka.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.ulima.is2.bustracka.dto.CompraRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.ComprobanteResponseDTO;
import pe.edu.ulima.is2.bustracka.service.VentaService;

import java.util.List;

/** Compra de pasajes y comprobantes (Sprint 4). El usuario sale del JWT, nunca del body. */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<ComprobanteResponseDTO> comprar(@Valid @RequestBody CompraRequestDTO request,
                                                          HttpServletRequest http) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.comprar(idUsuario(http), request));
    }

    @GetMapping
    public List<ComprobanteResponseDTO> misCompras(HttpServletRequest http) {
        return ventaService.listarCompras(idUsuario(http));
    }

    @GetMapping("/{id}")
    public ComprobanteResponseDTO comprobante(@PathVariable Long id, HttpServletRequest http) {
        return ventaService.obtenerComprobante(idUsuario(http), id);
    }

    private Long idUsuario(HttpServletRequest http) {
        return ((Integer) http.getAttribute("idUsuario")).longValue();
    }
}
