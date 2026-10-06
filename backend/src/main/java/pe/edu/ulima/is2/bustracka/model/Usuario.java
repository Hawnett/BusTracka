package pe.edu.ulima.is2.bustracka.model;

import java.time.LocalDateTime;

/**
 * Entidad de dominio Usuario. Se mapea a mano (sin ORM) desde el ResultSet
 * en UsuarioDaoImpl, siguiendo el enfoque JDBC + DAO del curso.
 */
public class Usuario {

    private Long idUsuario;
    private String nombreUsuario;
    private String contrasena; // se guarda SIEMPRE como hash BCrypt, nunca en texto plano
    private String email;
    private String estado;     // "ACTIVO" | "INACTIVO"
    private Integer idRol;
    private LocalDateTime fechaCreacion;

    public Usuario() {
    }

    public Usuario(Long idUsuario, String nombreUsuario, String contrasena, String email,
                   String estado, Integer idRol, LocalDateTime fechaCreacion) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.email = email;
        this.estado = estado;
        this.idRol = idRol;
        this.fechaCreacion = fechaCreacion;
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

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isActivo() {
        return "ACTIVO".equalsIgnoreCase(estado);
    }
}
