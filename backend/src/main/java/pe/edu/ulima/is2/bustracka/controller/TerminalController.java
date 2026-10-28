package pe.edu.ulima.is2.bustracka.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.ulima.is2.bustracka.dto.TerminalResponseDTO;
import pe.edu.ulima.is2.bustracka.service.TerminalService;

import java.util.List;

@RestController
@RequestMapping("/api/terminales")
public class TerminalController {

    private final TerminalService terminalService;

    public TerminalController(TerminalService terminalService) {
        this.terminalService = terminalService;
    }

    @GetMapping
    public List<TerminalResponseDTO> listar() {
        return terminalService.listarTerminales();
    }
}
