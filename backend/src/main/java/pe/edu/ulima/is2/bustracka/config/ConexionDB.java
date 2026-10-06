package pe.edu.ulima.is2.bustracka.config;

import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Patron Singleton: garantiza una unica instancia responsable de crear
 * conexiones JDBC a PostgreSQL a partir de application.properties.
 *
 * Decision de diseno: en vez de guardar una unica Connection compartida
 * (poco seguro en un servidor multi-hilo como Tomcat, ya que java.sql.Connection
 * no es thread-safe), el Singleton guarda la CONFIGURACION una sola vez y
 * expone getConexion() para que cada hilo/DAO obtenga su propia Connection
 * de corta vida, la use en un try-with-resources y la cierre. Esto respeta
 * el espiritu academico del patron Singleton (una sola fuente de verdad
 * para los datos de conexion) sin introducir condiciones de carrera.
 */
public final class ConexionDB {

    private static volatile ConexionDB instancia;

    private final String url;
    private final String usuario;
    private final String password;

    private ConexionDB() {
        Properties props = cargarPropiedades();
        this.url = props.getProperty("db.url");
        this.usuario = props.getProperty("db.usuario");
        this.password = props.getProperty("db.password");
        try {
            Class.forName(props.getProperty("db.driver"));
        } catch (ClassNotFoundException e) {
            throw new PersistenciaException("No se encontro el driver JDBC de PostgreSQL", e);
        }
    }

    /** Double-checked locking: evita sincronizar en cada llamada una vez inicializado. */
    public static ConexionDB getInstancia() {
        if (instancia == null) {
            synchronized (ConexionDB.class) {
                if (instancia == null) {
                    instancia = new ConexionDB();
                }
            }
        }
        return instancia;
    }

    /** Cada llamada abre una conexion nueva; el llamador es responsable de cerrarla (try-with-resources). */
    public Connection getConexion() {
        try {
            return DriverManager.getConnection(url, usuario, password);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a la base de datos", e);
        }
    }

    private Properties cargarPropiedades() {
        Properties props = new Properties();
        try (InputStream is = ConexionDB.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is == null) {
                throw new IOException("No se encontro application.properties en el classpath");
            }
            props.load(is);
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer la configuracion de base de datos", e);
        }
        return props;
    }
}
