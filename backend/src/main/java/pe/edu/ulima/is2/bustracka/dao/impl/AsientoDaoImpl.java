package pe.edu.ulima.is2.bustracka.dao.impl;

import pe.edu.ulima.is2.bustracka.config.ConexionDB;
import pe.edu.ulima.is2.bustracka.dao.AsientoDao;
import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;
import pe.edu.ulima.is2.bustracka.model.Asiento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsientoDaoImpl implements AsientoDao {

    private static final String SQL_POR_VIAJE =
            "SELECT a.id_asiento, a.numero, " +
            "EXISTS (SELECT 1 FROM detalle_venta dv WHERE dv.id_viaje = v.id_viaje AND dv.id_asiento = a.id_asiento) AS ocupado " +
            "FROM viaje v JOIN asiento a ON a.id_bus = v.id_bus " +
            "WHERE v.id_viaje = ? ORDER BY a.numero";

    @Override
    public List<Asiento> listarPorViaje(Long idViaje) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_POR_VIAJE)) {
            ps.setLong(1, idViaje);
            try (ResultSet rs = ps.executeQuery()) {
                List<Asiento> asientos = new ArrayList<>();
                while (rs.next()) {
                    asientos.add(new Asiento(rs.getLong("id_asiento"), rs.getInt("numero"), rs.getBoolean("ocupado")));
                }
                return asientos;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar los asientos del viaje", e);
        }
    }
}
