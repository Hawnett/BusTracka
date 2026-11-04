package pe.edu.ulima.is2.bustracka.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.ulima.is2.bustracka.exception.TokenInvalidoException;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Encapsula la generacion/validacion de JWT (HS256). Se usa tanto para el
 * token de sesion (login) como -con un "purpose" distinto y vida mas corta-
 * para el token de recuperacion de contrasena, evitando que un token de
 * un tipo se pueda reutilizar como si fuera del otro.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey clave;
    private final long expiracionSesionMs;

    public JwtUtil(@Value("${jwt.secret:}") String secretoBase64,
                   @Value("${jwt.expiration-ms}") long expiracionSesionMs) {
        this.clave = crearClave(secretoBase64);
        this.expiracionSesionMs = expiracionSesionMs;
    }

    /** Sin JWT_SECRET se usa una clave aleatoria: funciona, pero las sesiones se pierden al reiniciar. */
    private static SecretKey crearClave(String secretoBase64) {
        if (secretoBase64 == null || secretoBase64.isBlank()) {
            log.warn("JWT_SECRET no esta definido: se usa una clave aleatoria para esta ejecucion");
            return Keys.secretKeyFor(SignatureAlgorithm.HS256);
        }
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretoBase64));
    }

    public long getExpiracionSesionMs() {
        return expiracionSesionMs;
    }

    public String generarTokenSesion(Long idUsuario, String email, Integer idRol) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString()) // permite invalidar este token al cerrar sesion
                .setSubject(email)
                .claim("idUsuario", idUsuario)
                .claim("rol", idRol)
                .claim("proposito", "SESION")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiracionSesionMs))
                .signWith(clave, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Token de recuperacion de contrasena: vida corta (15 min) y "purpose" distinto al de sesion. */
    public String generarTokenRecuperacion(Long idUsuario, String email) {
        return Jwts.builder()
                .setSubject(email)
                .claim("idUsuario", idUsuario)
                .claim("proposito", "RECUPERACION")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000L))
                .signWith(clave, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Lanza TokenInvalidoException si el token esta mal formado, expirado o el proposito no calza. */
    public Claims validarYObtenerClaims(String token, String propositoEsperado) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(clave)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String proposito = claims.get("proposito", String.class);
            if (propositoEsperado != null && !propositoEsperado.equals(proposito)) {
                throw new TokenInvalidoException("El token no corresponde a la operacion solicitada");
            }
            return claims;
        } catch (ExpiredJwtException e) {
            throw new TokenInvalidoException("El token ha expirado");
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenInvalidoException("El token es invalido");
        }
    }
}
