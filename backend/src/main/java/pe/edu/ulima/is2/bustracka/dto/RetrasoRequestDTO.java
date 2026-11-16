package pe.edu.ulima.is2.bustracka.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pe.edu.ulima.is2.bustracka.model.MotivoRetraso;

/**
 * Datos que envía el administrador al registrar un retraso (PUT /api/admin/viajes/{id}/retraso).
 * Junto con el id del viaje de la URL, son las cinco entradas de la funcionalidad (HU 5.2).
 */
public record RetrasoRequestDTO(
        @NotNull(message = "es obligatorio") @Min(value = 0, message = "no puede ser negativo")
        @Max(value = 720, message = "no puede superar 12 horas") Integer minutosRetraso,
        @NotNull(message = "es obligatorio") MotivoRetraso motivo,
        @NotBlank(message = "es obligatorio")
        @Size(min = 10, max = 200, message = "debe tener entre 10 y 200 caracteres") String detalle,
        @NotNull(message = "es obligatorio") Boolean avisarPasajeros) {
}