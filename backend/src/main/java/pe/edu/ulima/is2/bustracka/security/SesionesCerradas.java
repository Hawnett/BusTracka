package pe.edu.ulima.is2.bustracka.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tokens de sesion cerrados con logout antes de vencer. Se guardan solo hasta
 * su expiracion (despues el JWT ya no es valido de todos modos).
 */
@Component
public class SesionesCerradas {

    private final Map<String, Instant> cerradas = new ConcurrentHashMap<>();

    public void cerrar(String idToken, Instant expiracion) {
        Instant ahora = Instant.now();
        cerradas.values().removeIf(vence -> vence.isBefore(ahora));
        cerradas.put(idToken, expiracion);
    }

    public boolean estaCerrada(String idToken) {
        return idToken != null && cerradas.containsKey(idToken);
    }
}
