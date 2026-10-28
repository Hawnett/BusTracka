package pe.edu.ulima.is2.bustracka.service;

import pe.edu.ulima.is2.bustracka.dto.TerminalResponseDTO;
import java.util.List;

public interface TerminalService {
    List<TerminalResponseDTO> listarTerminales();
}
