package pe.edu.ulima.is2.bustracka.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.ulima.is2.bustracka.exception.TokenInvalidoException;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Encapsula la generacion/validacion de JWT (HS256). Se usa tanto para el
 * token de sesion (login) como -con un "purpose" distinto y vida mas corta-
 * para el token de recuperacion de contrasena, evitando que un token de
 * un tipo se pueda reutilizar como si fuera del otro.
 */
@Component
public class JwtUtil {

    private final SecretKey clave;
    private final long expiracionSesionMs;

    public JwtUtil(@Value("${jwt.secret}") String secretoBase64,
                    @Value("${jwt.expiration-ms}") long expiracionSesionMs) {
        this.clave = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secretoBase64));
        this.expiracionSesionMs = expiracionSesionMs;
    }

    public long getExpiracionSesionMs() {
        return expiracionSesionMs;
    }

    public String generarTokenSesion(Long idUsuario, String email) {
        return generarToken(idUsuario, email, "SESION", expiracionSesionMs);
    }

    /** Token de recuperacion de contrasena: vida corta (15 min) y "purpose" distinto al de sesion. */
    public String generarTokenRecuperacion(Long idUsuario, String email) {
        return generarToken(idUsuario, email, "RECUPERACION", 15 * 60 * 1000L);
    }

    private String generarToken(Long idUsuario, String email, String proposito, long vigenciaMs) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + vigenciaMs);
        return Jwts.builder()
                .setSubject(email)
                .claim("idUsuario", idUsuario)
                .claim("proposito", proposito)
                .setIssuedAt(ahora)
                .setExpiration(expiracion)
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
