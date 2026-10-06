package pe.edu.ulima.is2.bustracka.dao;

import pe.edu.ulima.is2.bustracka.model.Usuario;

import java.util.Optional;

/**
 * Contrato de acceso a datos para Usuario (DIP: la capa de servicio depende
 * de esta interfaz, nunca de la implementacion JDBC concreta).
 */
public interface UsuarioDao {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(Long idUsuario);

    boolean existePorEmail(String email);

    void actualizarContrasena(Long idUsuario, String nuevaContrasenaHasheada);
}
