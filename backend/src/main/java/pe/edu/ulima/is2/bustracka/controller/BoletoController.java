package pe.edu.ulima.is2.bustracka.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ulima.is2.bustracka.dto.BoletoPublicoDTO;
import pe.edu.ulima.is2.bustracka.service.VentaService;

/** Destino del QR del boleto: lo abre quien lo escanea, sin iniciar sesion. */
@RestController
public class BoletoController {

    private final VentaService ventaService;

    public BoletoController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping("/api/publico/boletos/{codigo}")
    public BoletoPublicoDTO validar(@PathVariable String codigo) {
        return ventaService.validarBoleto(codigo);
    }
}
