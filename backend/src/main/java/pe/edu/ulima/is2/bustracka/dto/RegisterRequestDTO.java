package pe.edu.ulima.is2.bustracka.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para POST /api/auth/register.
 */
public class RegisterRequestDTO {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre de usuario debe tener entre 3 y 100 caracteres")
    private String nombreUsuario;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es valido")
    private String email;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
    private String contrasena;

    @NotBlank(message = "Confirma tu contrasena")
    private String confirmarContrasena;

    @NotBlank(message = "El celular es obligatorio")
    @Pattern(regexp = "9\\d{8}", message = "El celular debe tener 9 digitos y empezar con 9")
    private String celular;

    public RegisterRequestDTO() {
    }

    public RegisterRequestDTO(String nombreUsuario, String email, String contrasena) {
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.contrasena = contrasena;
    }

    public RegisterRequestDTO(String nombreUsuario, String email, String contrasena,
                              String confirmarContrasena, String celular) {
        this(nombreUsuario, email, contrasena);
        this.confirmarContrasena = confirmarContrasena;
        this.celular = celular;
    }

    @JsonIgnore
    @AssertTrue(message = "Las contrasenas no coinciden")
    public boolean isConfirmacionIgual() {
        return contrasena == null || confirmarContrasena == null || contrasena.equals(confirmarContrasena);
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getConfirmarContrasena() {
        return confirmarContrasena;
    }

    public void setConfirmarContrasena(String confirmarContrasena) {
        this.confirmarContrasena = confirmarContrasena;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }
}