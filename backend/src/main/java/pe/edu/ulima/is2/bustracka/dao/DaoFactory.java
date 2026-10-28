package pe.edu.ulima.is2.bustracka.dao;

import pe.edu.ulima.is2.bustracka.dao.impl.AsientoDaoImpl;
import pe.edu.ulima.is2.bustracka.dao.impl.RutaDaoImpl;
import pe.edu.ulima.is2.bustracka.dao.impl.TerminalDaoImpl;
import pe.edu.ulima.is2.bustracka.dao.impl.UsuarioDaoImpl;
import pe.edu.ulima.is2.bustracka.dao.impl.VentaDaoImpl;
import pe.edu.ulima.is2.bustracka.dao.impl.ViajeDaoImpl;

/**
 * Patron Factory: centraliza la creacion de los DAOs de la aplicacion.
 * Los services piden la instancia aqui y solo conocen la interfaz (DIP).
 */
public final class DaoFactory {

    private DaoFactory() {
        // clase utilitaria, no se instancia
    }

    public static UsuarioDao crearUsuarioDao() {
        return new UsuarioDaoImpl();
    }

    public static TerminalDao crearTerminalDao() {
        return new TerminalDaoImpl();
    }

    public static RutaDao crearRutaDao() {
        return new RutaDaoImpl();
    }

    public static ViajeDao crearViajeDao() {
        return new ViajeDaoImpl();
    }

    public static AsientoDao crearAsientoDao() {
        return new AsientoDaoImpl();
    }

    public static VentaDao crearVentaDao() {
        return new VentaDaoImpl();
    }
}
