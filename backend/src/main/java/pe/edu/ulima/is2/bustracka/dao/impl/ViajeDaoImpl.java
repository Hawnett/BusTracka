package pe.edu.ulima.is2.bustracka.dao.impl;

import pe.edu.ulima.is2.bustracka.config.ConexionDB;
import pe.edu.ulima.is2.bustracka.dao.ViajeDao;
import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;
import pe.edu.ulima.is2.bustracka.model.Viaje;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ViajeDaoImpl implements ViajeDao {

    /** Las fechas de viaje se guardan en hora de Lima; comparamos contra la hora actual de Lima. */
    private static final String AHORA = "(now() AT TIME ZONE 'America/Lima')";

    private static final String SQL_BASE =
            "SELECT v.id_viaje, v.id_ruta, v.id_bus, b.placa, v.fecha_salida, v.precio, r.duracion_min, " +
            "r.id_terminal_origen, t1.ciudad AS ciudad_origen, " +
            "r.id_terminal_destino, t2.ciudad AS ciudad_destino, b.capacidad, " +
            "b.capacidad - (SELECT COUNT(*) FROM detalle_venta dv WHERE dv.id_viaje = v.id_viaje) AS disponibles " +
            "FROM viaje v " +
            "JOIN ruta r ON r.id_ruta = v.id_ruta " +
            "JOIN bus b ON b.id_bus = v.id_bus " +
            "JOIN terminal t1 ON t1.id_terminal = r.id_terminal_origen " +
            "JOIN terminal t2 ON t2.id_terminal = r.id_terminal_destino ";

    @Override
    public List<Viaje> buscar(Long idTerminalOrigen, Long idTerminalDestino, LocalDate fecha) {
        // SQL dinamico: solo se agregan los filtros que el usuario envio.
        StringBuilder sql = new StringBuilder(SQL_BASE).append("WHERE v.fecha_salida > " + AHORA + " ");
        List<Object> params = new ArrayList<>();
        if (idTerminalOrigen != null) {
            sql.append("AND r.id_terminal_origen = ? ");
            params.add(idTerminalOrigen);
        }
        if (idTerminalDestino != null) {
            sql.append("AND r.id_terminal_destino = ? ");
            params.add(idTerminalDestino);
        }
        if (fecha != null) {
            sql.append("AND v.fecha_salida >= ? AND v.fecha_salida < ? ");
            params.add(Timestamp.valueOf(fecha.atStartOfDay()));
            params.add(Timestamp.valueOf(fecha.plusDays(1).atStartOfDay()));
        }
        sql.append("ORDER BY v.fecha_salida");
        return consultar(sql.toString(), params);
    }

    @Override
    public List<Viaje> listarEnCurso() {
        return consultar(SQL_BASE + "WHERE v.fecha_salida <= " + AHORA + " AND v.fecha_salida > " + AHORA + " - interval '1 day' " +
                "ORDER BY v.fecha_salida", List.of());
    }

    @Override
    public Optional<Viaje> buscarPorId(Long idViaje) {
        List<Viaje> res = consultar(SQL_BASE + "WHERE v.id_viaje = ?", List.of(idViaje));
        return res.isEmpty() ? Optional.empty() : Optional.of(res.get(0));
    }

    private List<Viaje> consultar(String sql, List<Object> params) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Viaje> viajes = new ArrayList<>();
                while (rs.next()) {
                    viajes.add(mapearFila(rs));
                }
                return viajes;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al consultar viajes", e);
        }
    }

    private Viaje mapearFila(ResultSet rs) throws SQLException {
        return new Viaje(rs.getLong("id_viaje"), rs.getLong("id_ruta"), rs.getLong("id_bus"),
                rs.getString("placa"), rs.getTimestamp("fecha_salida").toLocalDateTime(),
                rs.getBigDecimal("precio"), rs.getInt("duracion_min"),
                rs.getLong("id_terminal_origen"), rs.getString("ciudad_origen"),
                rs.getLong("id_terminal_destino"), rs.getString("ciudad_destino"),
                rs.getInt("capacidad"), rs.getInt("disponibles"));
    }
}
