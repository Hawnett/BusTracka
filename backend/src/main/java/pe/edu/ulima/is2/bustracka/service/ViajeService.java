package pe.edu.ulima.is2.bustracka.service;

import pe.edu.ulima.is2.bustracka.dto.AsientoResponseDTO;
import pe.edu.ulima.is2.bustracka.dto.RetrasoRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.ViajeResponseDTO;
import pe.edu.ulima.is2.bustracka.model.FranjaHoraria;

import java.time.LocalDate;
import java.util.List;

public interface ViajeService {
    List<ViajeResponseDTO> buscarViajes(Long idTerminalOrigen, Long idTerminalDestino, LocalDate fecha,
                                        Integer pasajeros, FranjaHoraria franja);

    List<ViajeResponseDTO> listarViajesEnCurso();

    ViajeResponseDTO obtenerViaje(Long idViaje);

    List<AsientoResponseDTO> listarAsientos(Long idViaje);

    /** Para el administrador: viajes que aun no llegan a su destino. */
    List<ViajeResponseDTO> listarViajesNoFinalizados();

    /** El correo de alerta lo envia el flujo de notificaciones de n8n al detectar el cambio. */
    ViajeResponseDTO registrarRetraso(Long idViaje, RetrasoRequestDTO datos);
}