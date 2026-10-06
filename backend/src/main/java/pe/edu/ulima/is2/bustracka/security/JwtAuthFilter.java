package pe.edu.ulima.is2.bustracka.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.ulima.is2.bustracka.exception.TokenInvalidoException;

import java.io.IOException;

/**
 * Filtro propio de autenticacion (no usamos Spring Security completo a
 * proposito, ver pom.xml). Lee el header "Authorization: Bearer <token>",
 * valida el JWT y, si es correcto, deja los datos del usuario disponibles
 * en request attributes para que el controller los use (por ejemplo en
 * GET /api/auth/me).
 *
 * En Sprint 1 solo protegemos /api/auth/me; los demas endpoints de
 * autenticacion (login, register, recover) son publicos por definicion.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String RUTA_PROTEGIDA = "/api/auth/me";

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (!request.getRequestURI().equals(RUTA_PROTEGIDA)) {
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Falta el token de sesion");
            return;
        }

        try {
            String token = header.substring(7);
            Claims claims = jwtUtil.validarYObtenerClaims(token, "SESION");
            request.setAttribute("idUsuario", claims.get("idUsuario", Integer.class));
            request.setAttribute("email", claims.getSubject());
            chain.doFilter(request, response);
        } catch (TokenInvalidoException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }
}
