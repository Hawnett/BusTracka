package pe.edu.ulima.is2.bustracka.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.ulima.is2.bustracka.dto.CompraRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.ComprobanteResponseDTO;
import pe.edu.ulima.is2.bustracka.dto.EnlaceSeguimientoDTO;
import pe.edu.ulima.is2.bustracka.service.EnlaceSeguimientoService;
import pe.edu.ulima.is2.bustracka.service.VentaService;

import java.util.List;

/** Compra de pasajes, historial y comprobantes. El usuario sale del JWT, nunca del body. */
@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;
    private final EnlaceSeguimientoService enlaceSeguimientoService;

    public VentaController(VentaService ventaService, EnlaceSeguimientoService enlaceSeguimientoService) {
        this.ventaService = ventaService;
        this.enlaceSeguimientoService = enlaceSeguimientoService;
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

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> comprobantePdf(@PathVariable Long id, HttpServletRequest http) {
        byte[] pdf = ventaService.generarPdf(idUsuario(http), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("boleto-bustracka-" + id + ".pdf").build().toString())
                .body(pdf);
    }

    @PostMapping("/{id}/enlace-seguimiento")
    public EnlaceSeguimientoDTO compartir(@PathVariable Long id, HttpServletRequest http) {
        return enlaceSeguimientoService.generarParaCompra(idUsuario(http), id);
    }

    private Long idUsuario(HttpServletRequest http) {
        return ((Integer) http.getAttribute("idUsuario")).longValue();
    }
}
