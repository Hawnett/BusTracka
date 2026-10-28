package pe.edu.ulima.is2.bustracka.dao.impl;

import pe.edu.ulima.is2.bustracka.config.ConexionDB;
import pe.edu.ulima.is2.bustracka.dao.RutaDao;
import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;
import pe.edu.ulima.is2.bustracka.model.PuntoRuta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RutaDaoImpl implements RutaDao {

    private static final String SQL_PUNTOS =
            "SELECT orden, latitud, longitud FROM ruta_punto WHERE id_ruta = ? ORDER BY orden";

    @Override
    public List<PuntoRuta> listarPuntos(Long idRuta) {
        try (Connection con = ConexionDB.getInstancia().getConexion();
             PreparedStatement ps = con.prepareStatement(SQL_PUNTOS)) {
            ps.setLong(1, idRuta);
            try (ResultSet rs = ps.executeQuery()) {
                List<PuntoRuta> puntos = new ArrayList<>();
                while (rs.next()) {
                    puntos.add(new PuntoRuta(rs.getInt("orden"), rs.getDouble("latitud"), rs.getDouble("longitud")));
                }
                return puntos;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("Error al obtener los puntos de la ruta", e);
        }
    }
}
