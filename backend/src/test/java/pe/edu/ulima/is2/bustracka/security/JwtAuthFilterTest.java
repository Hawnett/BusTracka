package pe.edu.ulima.is2.bustracka.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtAuthFilterTest {

    private final JwtUtil jwtUtil = new JwtUtil(Base64.getEncoder()
            .encodeToString("clave-de-prueba-de-mas-de-32-bytes-para-hs256".getBytes()), 3_600_000);
    private final SesionesCerradas sesionesCerradas = new SesionesCerradas();
    private final JwtAuthFilter filtro = new JwtAuthFilter(jwtUtil, sesionesCerradas);

    private int estadoDe(String metodo, String uri, String token) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(metodo, uri);
        if (token != null) {
            request.addHeader("Authorization", "Bearer " + token);
        }
        MockHttpServletResponse response = new MockHttpServletResponse();
        filtro.doFilter(request, response, new MockFilterChain());
        return response.getStatus();
    }

    @Test
    void sinTokenNoSeEntraAUnaRutaPrivada() throws Exception {
        assertEquals(401, estadoDe("GET", "/api/ventas", null));
    }

    @Test
    void elEnlaceCompartidoYElBoletoNoPidenSesion() throws Exception {
        assertEquals(200, estadoDe("GET", "/api/publico/seguimiento", null));
        assertEquals(200, estadoDe("GET", "/api/publico/boletos/BT-ABC", null));
    }

    @Test
    void unPasajeroNoPuedeUsarLasRutasDeAdministrador() throws Exception {
        assertEquals(403, estadoDe("PUT", "/api/admin/viajes/1/retraso", jwtUtil.generarTokenSesion(2L, "ana@test.pe", 2)));
        assertEquals(200, estadoDe("PUT", "/api/admin/viajes/1/retraso", jwtUtil.generarTokenSesion(1L, "admin@test.pe", 1)));
    }

    @Test
    void despuesDelLogoutElTokenYaNoSirve() throws Exception {
        String token = jwtUtil.generarTokenSesion(2L, "ana@test.pe", 2);
        assertEquals(200, estadoDe("GET", "/api/ventas", token));

        Claims claims = jwtUtil.validarYObtenerClaims(token, "SESION");
        sesionesCerradas.cerrar(claims.getId(), claims.getExpiration().toInstant());

        assertEquals(401, estadoDe("GET", "/api/ventas", token));
    }

    @Test
    void elLogoutExigeSesion() throws Exception {
        assertEquals(401, estadoDe("POST", "/api/auth/logout", null));
        assertEquals(200, estadoDe("POST", "/api/auth/login", null));
    }
}
