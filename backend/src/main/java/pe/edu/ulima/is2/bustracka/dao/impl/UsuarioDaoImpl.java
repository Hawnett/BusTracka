package pe.edu.ulima.is2.bustracka.dao.impl;

import pe.edu.ulima.is2.bustracka.config.ConexionDB;
import pe.edu.ulima.is2.bustracka.dao.UsuarioDao;
import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;
import pe.edu.ulima.is2.bustracka.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

/**
 * Implementacion JDBC "pura" del DAO de Usuario: cada metodo abre su propia
 * Connection (obtenida del Singleton ConexionDB) dentro de un
 * try-with-resources, para no dejar conexiones abiertas ante una excepcion.
 */
public class UsuarioDaoImpl implements UsuarioDao {

    private static final String SQL_INSERT =
            "INSERT INTO usuario (nombre_usuario, contrasena, email, estado, id_rol) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_SELECT_POR_EMAIL =
            "SELECT id_usuario, nombre_usuario, contrasena, email, estado, id_rol, fecha_creacion " +
            "FROM usuario WHERE email = ?";

    private static final String SQL_SELECT_POR_ID =
            "SELECT id_usuario, nombre_usuario, contrasena, email, estado, id_rol, fecha_creacion " +
            "FROM usuario WHERE id_usuario = ?";

    private static final String SQL_EXISTE_POR_EMAIL =
            "SELECT 1 FROM usuario WHERE email = ?";

    private static final String SQL_UPDATE_CONTRASENA =
            "UPDATE usuario SET contrasena = ? WHERE id_usuario = ?";

    @Override
    public Usuario guardar(Usuario usuario) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getContrasena());
            ps.setString(3, usuario.getEmail());
            ps.setString(4, usuario.getEstado());
            ps.setInt(5, usuario.getIdRol());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    usuario.setIdUsuario(keys.getLong(1));
                }
            }
            return usuario;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al guardar el usuario en la base de datos", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorEmail(String email) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_SELECT_POR_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearFila(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el usuario por email", e);
        }
    }

    @Override
    public Optional<Usuario> buscarPorId(Long idUsuario) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_SELECT_POR_ID)) {

            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearFila(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el usuario por id", e);
        }
    }

    @Override
    public boolean existePorEmail(String email) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_EXISTE_POR_EMAIL)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al verificar existencia del email", e);
        }
    }

    @Override
    public void actualizarContrasena(Long idUsuario, String nuevaContrasenaHasheada) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_UPDATE_CONTRASENA)) {

            ps.setString(1, nuevaContrasenaHasheada);
            ps.setLong(2, idUsuario);
            int filas = ps.executeUpdate();
            if (filas == 0) {
                throw new PersistenciaException("No se encontro el usuario a actualizar (id=" + idUsuario + ")", null);
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al actualizar la contrasena", e);
        }
    }

    /** Mapeo manual ResultSet -> Usuario (sin ORM), tal como se ensena en el curso. */
    private Usuario mapearFila(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getLong("id_usuario"));
        usuario.setNombreUsuario(rs.getString("nombre_usuario"));
        usuario.setContrasena(rs.getString("contrasena"));
        usuario.setEmail(rs.getString("email"));
        usuario.setEstado(rs.getString("estado"));
        usuario.setIdRol(rs.getInt("id_rol"));
        Timestamp fecha = rs.getTimestamp("fecha_creacion");
        if (fecha != null) {
            usuario.setFechaCreacion(fecha.toLocalDateTime());
        }
        return usuario;
    }
}
