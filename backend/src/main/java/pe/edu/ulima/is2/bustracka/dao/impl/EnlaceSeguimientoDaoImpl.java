package pe.edu.ulima.is2.bustracka.dao.impl;

import pe.edu.ulima.is2.bustracka.config.ConexionDB;
import pe.edu.ulima.is2.bustracka.dao.EnlaceSeguimientoDao;
import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;
import pe.edu.ulima.is2.bustracka.model.EnlaceSeguimiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

public class EnlaceSeguimientoDaoImpl implements EnlaceSeguimientoDao {

    private static final String SQL_INSERT =
            "INSERT INTO seguimiento (id_venta, id_viaje, token, fecha_creacion, fecha_expiracion, estado) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT =
            "SELECT id_seguimiento, id_venta, id_viaje, token, fecha_creacion, fecha_expiracion, estado FROM seguimiento ";

    @Override
    public EnlaceSeguimiento registrar(EnlaceSeguimiento enlace) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, enlace.idVenta());
            ps.setLong(2, enlace.idViaje());
            ps.setString(3, enlace.token());
            ps.setTimestamp(4, Timestamp.valueOf(enlace.fechaCreacion()));
            ps.setTimestamp(5, Timestamp.valueOf(enlace.fechaExpiracion()));
            ps.setString(6, enlace.estado());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return new EnlaceSeguimiento(keys.getLong(1), enlace.idVenta(), enlace.idViaje(), enlace.token(),
                        enlace.fechaCreacion(), enlace.fechaExpiracion(), enlace.estado());
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar el enlace de seguimiento", e);
        }
    }

    @Override
    public Optional<EnlaceSeguimiento> buscarPorToken(String token) {
        return buscarUno("WHERE token = ?", token);
    }

    @Override
    public Optional<EnlaceSeguimiento> buscarActivoPorVenta(Long idVenta) {
        return buscarUno("WHERE id_venta = ? AND estado = 'ACTIVO' ORDER BY id_seguimiento DESC LIMIT 1", idVenta);
    }

    private Optional<EnlaceSeguimiento> buscarUno(String filtro, Object valor) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_SELECT + filtro)) {
            ps.setObject(1, valor);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new EnlaceSeguimiento(rs.getLong("id_seguimiento"), rs.getLong("id_venta"),
                        rs.getLong("id_viaje"), rs.getString("token"),
                        rs.getTimestamp("fecha_creacion").toLocalDateTime(),
                        rs.getTimestamp("fecha_expiracion").toLocalDateTime(), rs.getString("estado")));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar el enlace de seguimiento", e);
        }
    }
}
