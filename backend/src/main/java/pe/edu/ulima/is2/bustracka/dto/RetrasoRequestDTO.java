package pe.edu.ulima.is2.bustracka.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RetrasoRequestDTO(
        @NotNull @Min(value = 0, message = "no puede ser negativo")
        @Max(value = 720, message = "no puede superar 12 horas") Integer minutosRetraso) {
}
