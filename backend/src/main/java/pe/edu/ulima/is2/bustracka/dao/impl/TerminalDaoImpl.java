package pe.edu.ulima.is2.bustracka.dao.impl;

import pe.edu.ulima.is2.bustracka.config.ConexionDB;
import pe.edu.ulima.is2.bustracka.dao.TerminalDao;
import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;
import pe.edu.ulima.is2.bustracka.model.Terminal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TerminalDaoImpl implements TerminalDao {

    private static final String SQL_LISTAR =
            "SELECT id_terminal, nombre, ciudad, latitud, longitud FROM terminal ORDER BY ciudad";

    @Override
    public List<Terminal> listarTodos() {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            List<Terminal> lista = new ArrayList<>();
            while (rs.next()) {
                lista.add(new Terminal(rs.getLong("id_terminal"), rs.getString("nombre"),
                        rs.getString("ciudad"), rs.getDouble("latitud"), rs.getDouble("longitud")));
            }
            return lista;
        } catch (SQLException e) {
            throw new PersistenciaException("Error al listar terminales", e);
        }
    }
}
