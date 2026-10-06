package pe.edu.ulima.is2.bustracka.dto;

/**
 * DTO de salida para login/register: nunca devolvemos la entidad Usuario
 * completa (evita filtrar el hash de la contrasena, entre otros campos).
 */
public class AuthResponseDTO {

    private Long idUsuario;
    private String nombreUsuario;
    private String email;
    private String token;
    private String tokenType = "Bearer";
    private long expiresInMs;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(Long idUsuario, String nombreUsuario, String email, String token, long expiresInMs) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.email = email;
        this.token = token;
        this.expiresInMs = expiresInMs;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInMs() {
        return expiresInMs;
    }

    public void setExpiresInMs(long expiresInMs) {
        this.expiresInMs = expiresInMs;
    }
}
