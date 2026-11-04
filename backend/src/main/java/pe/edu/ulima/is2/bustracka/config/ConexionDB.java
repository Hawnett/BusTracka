package pe.edu.ulima.is2.bustracka.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import pe.edu.ulima.is2.bustracka.exception.PersistenciaException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Patron Singleton: garantiza una unica instancia responsable de entregar
 * conexiones JDBC a PostgreSQL a partir de application.properties.
 *
 * Decision de diseno: en vez de guardar una unica Connection compartida
 * (poco seguro en un servidor multi-hilo como Tomcat, ya que java.sql.Connection
 * no es thread-safe), el Singleton guarda un pool de conexiones (HikariCP) y
 * expone getConexion() para que cada hilo/DAO obtenga su propia Connection
 * de corta vida, la use en un try-with-resources y la cierre (vuelve al pool).
 * Asi no se abre una conexion nueva a PostgreSQL en cada consulta.
 *
 * DB_URL, DB_USUARIO y DB_PASSWORD (variables de entorno) tienen prioridad
 * sobre application.properties.
 */
public final class ConexionDB {

    private static volatile ConexionDB instancia;

    private final HikariDataSource pool;

    private ConexionDB() {
        Properties props = cargarPropiedades();
        HikariConfig config = new HikariConfig();
        config.setPoolName("bustracka");
        config.setJdbcUrl(valor(props, "db.url", "DB_URL"));
        config.setUsername(valor(props, "db.usuario", "DB_USUARIO"));
        config.setPassword(valor(props, "db.password", "DB_PASSWORD"));
        config.setDriverClassName(props.getProperty("db.driver"));
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(5000);
        // Arranca aunque la BD aun no este lista; el error aparece al pedir una conexion.
        config.setInitializationFailTimeout(-1);
        this.pool = new HikariDataSource(config);
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

    /** Conexion del pool; el llamador debe cerrarla (try-with-resources) para devolverla. */
    public Connection getConexion() {
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo conectar a la base de datos", e);
        }
    }

    private static String valor(Properties props, String clave, String variableEntorno) {
        String deEntorno = System.getenv(variableEntorno);
        return deEntorno != null && !deEntorno.isBlank() ? deEntorno : props.getProperty(clave);
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
