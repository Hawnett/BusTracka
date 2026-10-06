package pe.edu.ulima.is2.bustracka.dao;

import pe.edu.ulima.is2.bustracka.dao.impl.UsuarioDaoImpl;

/**
 * Patron Factory: centraliza la creacion de los DAOs de la aplicacion.
 * Si en un sprint futuro se agrega, por ejemplo, RolDao o BusDao, el
 * resto del codigo (servicios) sigue pidiendo la instancia aqui, sin
 * conocer la clase concreta que la implementa (DIP).
 */
public final class DaoFactory {

    private DaoFactory() {
        // clase utilitaria, no se instancia
    }

    public static UsuarioDao crearUsuarioDao() {
        return new UsuarioDaoImpl();
    }
}
