package pe.edu.ulima.is2.bustracka.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record CompraRequestDTO(
        @NotNull Long idViaje,
        @NotEmpty List<Long> idAsientos,
        @NotBlank String nombrePasajero,
        @NotBlank String titularTarjeta,
        @NotBlank @Pattern(regexp = "\\d{13,19}", message = "debe tener entre 13 y 19 digitos") String numeroTarjeta) {
}
