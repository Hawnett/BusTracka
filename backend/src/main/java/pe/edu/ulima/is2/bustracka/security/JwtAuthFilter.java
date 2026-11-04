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
 * Desde el Release 1 TODO lo que cuelga de /api/ exige sesion, salvo
 * login/register/recover (/api/auth/*) y /api/publico/* (enlace de
 * seguimiento compartido y validacion del boleto). /api/admin/* ademas
 * exige el rol ADMINISTRADOR.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String RUTA_AUTH = "/api/auth/";
    private static final String RUTA_ME = "/api/auth/me";
    private static final String RUTA_LOGOUT = "/api/auth/logout";
    private static final String RUTA_PUBLICA = "/api/publico/";
    private static final String RUTA_ADMIN = "/api/admin/";
    private static final int ID_ROL_ADMINISTRADOR = 1;

    private final JwtUtil jwtUtil;
    private final SesionesCerradas sesionesCerradas;

    public JwtAuthFilter(JwtUtil jwtUtil, SesionesCerradas sesionesCerradas) {
        this.jwtUtil = jwtUtil;
        this.sesionesCerradas = sesionesCerradas;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (esPublica(request)) {
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
            if (sesionesCerradas.estaCerrada(claims.getId())) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "La sesion fue cerrada");
                return;
            }
            Integer idRol = claims.get("rol", Integer.class);
            if (request.getRequestURI().startsWith(RUTA_ADMIN) && (idRol == null || idRol != ID_ROL_ADMINISTRADOR)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo un administrador puede hacer esta operacion");
                return;
            }
            request.setAttribute("idUsuario", claims.get("idUsuario", Integer.class));
            request.setAttribute("email", claims.getSubject());
            request.setAttribute("idToken", claims.getId());
            request.setAttribute("expiraToken", claims.getExpiration().toInstant());
            chain.doFilter(request, response);
        } catch (TokenInvalidoException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        }
    }

    private boolean esPublica(HttpServletRequest request) {
        String uri = request.getRequestURI();
        // Las peticiones preflight de CORS (OPTIONS) nunca llevan token.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (uri.startsWith(RUTA_PUBLICA)) {
            return true;
        }
        return uri.startsWith(RUTA_AUTH) && !uri.equals(RUTA_ME) && !uri.equals(RUTA_LOGOUT);
    }
}
