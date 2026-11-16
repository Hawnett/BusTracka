package pe.edu.ulima.is2.bustracka.dto;

import java.time.LocalDateTime;

public record EnlaceSeguimientoDTO(Long idVenta, String url, LocalDateTime expiraEn, String qrBase64) {
}
