package pe.edu.ulima.is2.bustracka.service.impl;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.UsuarioDao;
import pe.edu.ulima.is2.bustracka.dto.AuthResponseDTO;
import pe.edu.ulima.is2.bustracka.dto.LoginRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.RegisterRequestDTO;
import pe.edu.ulima.is2.bustracka.exception.CorreoDuplicadoException;
import pe.edu.ulima.is2.bustracka.exception.CredencialesInvalidasException;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.exception.TokenInvalidoException;
import pe.edu.ulima.is2.bustracka.model.Usuario;
import pe.edu.ulima.is2.bustracka.notificacion.NotificacionService;
import pe.edu.ulima.is2.bustracka.security.JwtUtil;
import pe.edu.ulima.is2.bustracka.security.SesionesCerradas;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Pruebas del servicio de autenticacion (Sprint 1). El DAO se reemplaza por un doble. */
class AuthServiceImplTest {

    private static final String SECRETO = Base64.getEncoder()
            .encodeToString("clave-de-prueba-de-mas-de-32-bytes-para-hs256".getBytes());
    private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder();

    private UsuarioDao usuarioDao;
    private NotificacionService notificacionService;
    private JwtUtil jwtUtil;
    private SesionesCerradas sesionesCerradas;
    private AuthServiceImpl authService;

    @BeforeEach
    void preparar() {
        usuarioDao = mock(UsuarioDao.class);
        notificacionService = mock(NotificacionService.class);
        jwtUtil = new JwtUtil(SECRETO, 3_600_000);
        sesionesCerradas = new SesionesCerradas();
        try (MockedStatic<DaoFactory> fabrica = mockStatic(DaoFactory.class)) {
            fabrica.when(DaoFactory::crearUsuarioDao).thenReturn(usuarioDao);
            authService = new AuthServiceImpl(jwtUtil, notificacionService, sesionesCerradas, "http://localhost:5173");
        }
    }

    private Usuario usuario(String estado) {
        return new Usuario(7L, "ana", BCRYPT.encode("Secreta123"), "ana@test.pe", estado, 2, LocalDateTime.now());
    }

    @Test
    void registraConContrasenaCifradaYRolPasajero() {
        when(usuarioDao.existePorEmail("ana@test.pe")).thenReturn(false);
        when(usuarioDao.guardar(any())).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(5L);
            return u;
        });

        AuthResponseDTO respuesta = authService.registrar(new RegisterRequestDTO("ana", "ana@test.pe", "Secreta123"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioDao).guardar(guardado.capture());
        assertNotEquals("Secreta123", guardado.getValue().getContrasena());
        assertTrue(BCRYPT.matches("Secreta123", guardado.getValue().getContrasena()));
        assertEquals(2, guardado.getValue().getIdRol());
        Claims claims = jwtUtil.validarYObtenerClaims(respuesta.getToken(), "SESION");
        assertEquals(5, claims.get("idUsuario", Integer.class));
    }

    @Test
    void rechazaCorreoYaRegistrado() {
        when(usuarioDao.existePorEmail("ana@test.pe")).thenReturn(true);

        assertThrows(CorreoDuplicadoException.class,
                () -> authService.registrar(new RegisterRequestDTO("ana", "ana@test.pe", "Secreta123")));
        verify(usuarioDao, never()).guardar(any());
    }

    @Test
    void iniciaSesionYElTokenLlevaElRol() {
        when(usuarioDao.buscarPorEmail("ana@test.pe")).thenReturn(Optional.of(usuario("ACTIVO")));

        AuthResponseDTO respuesta = authService.iniciarSesion(new LoginRequestDTO("ana@test.pe", "Secreta123"));

        assertEquals(2, respuesta.getIdRol());
        assertEquals(2, jwtUtil.validarYObtenerClaims(respuesta.getToken(), "SESION").get("rol", Integer.class));
    }

    @Test
    void rechazaContrasenaIncorrecta() {
        when(usuarioDao.buscarPorEmail("ana@test.pe")).thenReturn(Optional.of(usuario("ACTIVO")));

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(new LoginRequestDTO("ana@test.pe", "otraClave1")));
    }

    @Test
    void rechazaCorreoNoRegistradoAlIniciarSesion() {
        when(usuarioDao.buscarPorEmail("nadie@test.pe")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(new LoginRequestDTO("nadie@test.pe", "Secreta123")));
    }

    @Test
    void rechazaCuentaInactiva() {
        when(usuarioDao.buscarPorEmail("ana@test.pe")).thenReturn(Optional.of(usuario("INACTIVO")));

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(new LoginRequestDTO("ana@test.pe", "Secreta123")));
    }

    @Test
    void recuperacionAvisaSiElCorreoNoExiste() {
        when(usuarioDao.buscarPorEmail("nadie@test.pe")).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> authService.solicitarRecuperacion("nadie@test.pe"));
        verify(notificacionService, never()).enviarCorreo(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void recuperacionEnviaUnEnlaceDelFrontendPorCorreo() {
        when(usuarioDao.buscarPorEmail("ana@test.pe")).thenReturn(Optional.of(usuario("ACTIVO")));

        String enlace = authService.solicitarRecuperacion("ana@test.pe");

        assertTrue(enlace.startsWith("http://localhost:5173/recuperar?token="));
        verify(notificacionService).enviarCorreo(eq("ana@test.pe"), anyString(), contains(enlace), contains(enlace));
    }

    @Test
    void confirmaLaNuevaContrasenaCifrada() {
        when(usuarioDao.buscarPorId(7L)).thenReturn(Optional.of(usuario("ACTIVO")));
        String token = jwtUtil.generarTokenRecuperacion(7L, "ana@test.pe");

        authService.confirmarNuevaContrasena(token, "NuevaClave1");

        verify(usuarioDao).actualizarContrasena(eq(7L), argThat(hash -> BCRYPT.matches("NuevaClave1", hash)));
    }

    @Test
    void unTokenDeSesionNoSirveParaCambiarLaContrasena() {
        String tokenSesion = jwtUtil.generarTokenSesion(7L, "ana@test.pe", 2);

        assertThrows(TokenInvalidoException.class, () -> authService.confirmarNuevaContrasena(tokenSesion, "NuevaClave1"));
        verify(usuarioDao, never()).actualizarContrasena(any(), any());
    }

    @Test
    void cerrarSesionInvalidaElToken() {
        Claims claims = jwtUtil.validarYObtenerClaims(jwtUtil.generarTokenSesion(7L, "ana@test.pe", 2), "SESION");

        authService.cerrarSesion(claims.getId(), claims.getExpiration().toInstant());

        assertTrue(sesionesCerradas.estaCerrada(claims.getId()));
    }
}
