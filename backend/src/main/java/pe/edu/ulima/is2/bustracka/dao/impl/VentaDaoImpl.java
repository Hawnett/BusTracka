package pe.edu.ulima.is2.bustracka.dao.impl;

import pe.edu.ulima.is2.bustracka.config.ConexionDB;
import pe.edu.ulima.is2.bustracka.dao.VentaDao;
import pe.edu.ulima.is2.bustracka.exception.AsientoNoDisponibleException;
import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;
import pe.edu.ulima.is2.bustracka.model.DetalleVenta;
import pe.edu.ulima.is2.bustracka.model.Venta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VentaDaoImpl implements VentaDao {

    private static final String SQL_INSERT_VENTA =
            "INSERT INTO venta (id_usuario, total, estado, codigo_pago, codigo_comprobante, fecha) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_DETALLE =
            "INSERT INTO detalle_venta (id_venta, id_viaje, id_asiento, nombre_pasajero, precio) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT =
            "SELECT id_venta, id_usuario, total, estado, codigo_pago, codigo_comprobante, fecha FROM venta ";
    private static final String SQL_DETALLES =
            "SELECT d.id_viaje, d.id_asiento, a.numero, d.nombre_pasajero, d.precio " +
            "FROM detalle_venta d JOIN asiento a ON a.id_asiento = d.id_asiento " +
            "WHERE d.id_venta = ? ORDER BY a.numero";
    private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

    @Override
    public Venta registrar(Venta venta, List<DetalleVenta> detalles) {
        try (Connection con = ConexionDB.getInstancia().getConexion()) {
            con.setAutoCommit(false); // transaccion: o se guarda todo o no se guarda nada
            try {
                long idVenta;
                try (PreparedStatement ps = con.prepareStatement(SQL_INSERT_VENTA, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setLong(1, venta.idUsuario());
                    ps.setBigDecimal(2, venta.total());
                    ps.setString(3, venta.estado());
                    ps.setString(4, venta.codigoPago());
                    ps.setString(5, venta.codigoComprobante());
                    ps.setTimestamp(6, Timestamp.valueOf(venta.fecha()));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        idVenta = keys.getLong(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(SQL_INSERT_DETALLE)) {
                    for (DetalleVenta d : detalles) {
                        ps.setLong(1, idVenta);
                        ps.setLong(2, d.idViaje());
                        ps.setLong(3, d.idAsiento());
                        ps.setString(4, d.nombrePasajero());
                        ps.setBigDecimal(5, d.precio());
                        ps.executeUpdate();
                    }
                }
                con.commit();
                return new Venta(idVenta, venta.idUsuario(), venta.total(), venta.estado(),
                        venta.codigoPago(), venta.codigoComprobante(), venta.fecha());
            } catch (SQLException e) {
                con.rollback();
                // UNIQUE (id_viaje, id_asiento): otro usuario compro el asiento primero
                if (SQLSTATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
                    throw new AsientoNoDisponibleException("Uno de los asientos ya fue vendido, elige otro");
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al registrar la venta", e);
        }
    }

    @Override
    public Optional<Venta> buscarPorId(Long idVenta) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_SELECT + "WHERE id_venta = ?")) {
            ps.setLong(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapearVenta(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al buscar la venta", e);
        }
    }

    @Override
    public List<Venta> listarPorUsuario(Long idUsuario) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_SELECT + "WHERE id_usuario = ? ORDER BY fecha DESC")) {
            ps.setLong(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                List<Venta> ventas = new ArrayList<>();
                while (rs.next()) {
                    ventas.add(mapearVenta(rs));
                }
                return ventas;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar las ventas", e);
        }
    }

    @Override
    public List<DetalleVenta> listarDetalles(Long idVenta) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_DETALLES)) {
            ps.setLong(1, idVenta);
            try (ResultSet rs = ps.executeQuery()) {
                List<DetalleVenta> detalles = new ArrayList<>();
                while (rs.next()) {
                    detalles.add(new DetalleVenta(rs.getLong("id_viaje"), rs.getLong("id_asiento"),
                            rs.getInt("numero"), rs.getString("nombre_pasajero"), rs.getBigDecimal("precio")));
                }
                return detalles;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener el detalle de la venta", e);
        }
    }

    private Venta mapearVenta(ResultSet rs) throws SQLException {
        return new Venta(rs.getLong("id_venta"), rs.getLong("id_usuario"), rs.getBigDecimal("total"),
                rs.getString("estado"), rs.getString("codigo_pago"), rs.getString("codigo_comprobante"),
                rs.getTimestamp("fecha").toLocalDateTime());
    }
}
