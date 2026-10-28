package pe.edu.ulima.is2.bustracka.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.TerminalDao;
import pe.edu.ulima.is2.bustracka.dto.TerminalResponseDTO;
import pe.edu.ulima.is2.bustracka.service.TerminalService;

import java.util.List;

@Service
public class TerminalServiceImpl implements TerminalService {

    private final TerminalDao terminalDao = DaoFactory.crearTerminalDao();

    @Override
    public List<TerminalResponseDTO> listarTerminales() {
        return terminalDao.listarTodos().stream()
                .map(t -> new TerminalResponseDTO(t.idTerminal(), t.nombre(), t.ciudad(), t.latitud(), t.longitud()))
                .toList();
    }
}
