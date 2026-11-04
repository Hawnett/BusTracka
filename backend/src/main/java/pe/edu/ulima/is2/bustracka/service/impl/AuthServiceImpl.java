package pe.edu.ulima.is2.bustracka.service.impl;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pe.edu.ulima.is2.bustracka.dao.DaoFactory;
import pe.edu.ulima.is2.bustracka.dao.UsuarioDao;
import pe.edu.ulima.is2.bustracka.dto.AuthResponseDTO;
import pe.edu.ulima.is2.bustracka.dto.LoginRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.RegisterRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.UsuarioResponseDTO;
import pe.edu.ulima.is2.bustracka.exception.CorreoDuplicadoException;
import pe.edu.ulima.is2.bustracka.exception.CredencialesInvalidasException;
import pe.edu.ulima.is2.bustracka.exception.RecursoNoEncontradoException;
import pe.edu.ulima.is2.bustracka.model.Usuario;
import pe.edu.ulima.is2.bustracka.notificacion.NotificacionService;
import pe.edu.ulima.is2.bustracka.notificacion.PlantillaCorreo;
import pe.edu.ulima.is2.bustracka.security.JwtUtil;
import pe.edu.ulima.is2.bustracka.security.SesionesCerradas;
import pe.edu.ulima.is2.bustracka.service.AuthService;

import java.time.Instant;

/**
 * Implementacion de AuthService.
 *
 * Nota de diseno (DIP): dependemos de la interfaz UsuarioDao, no de
 * UsuarioDaoImpl. La instancia concreta la entrega DaoFactory (patron
 * Factory), en vez de instanciarla aqui con "new UsuarioDaoImpl()" o de
 * inyectarla por Spring, para practicar el patron tal como lo pide el
 * enunciado del Sprint 1.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final int ID_ROL_PASAJERO_POR_DEFECTO = 2;

    private final UsuarioDao usuarioDao;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final NotificacionService notificacionService;
    private final SesionesCerradas sesionesCerradas;
    private final String urlFrontend;

    public AuthServiceImpl(JwtUtil jwtUtil, NotificacionService notificacionService, SesionesCerradas sesionesCerradas,
                           @Value("${app.frontend-url:http://localhost:5173}") String urlFrontend) {
        this.usuarioDao = DaoFactory.crearUsuarioDao();
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.notificacionService = notificacionService;
        this.sesionesCerradas = sesionesCerradas;
        this.urlFrontend = urlFrontend;
    }

    @Override
    public AuthResponseDTO registrar(RegisterRequestDTO request) {
        if (usuarioDao.existePorEmail(request.getEmail())) {
            throw new CorreoDuplicadoException(request.getEmail());
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreUsuario(request.getNombreUsuario());
        nuevoUsuario.setEmail(request.getEmail());
        nuevoUsuario.setContrasena(passwordEncoder.encode(request.getContrasena())); // nunca texto plano
        nuevoUsuario.setEstado("ACTIVO");
        nuevoUsuario.setIdRol(ID_ROL_PASAJERO_POR_DEFECTO);

        Usuario usuarioGuardado = usuarioDao.guardar(nuevoUsuario);
        return construirRespuesta(usuarioGuardado);
    }

    @Override
    public AuthResponseDTO iniciarSesion(LoginRequestDTO request) {
        Usuario usuario = usuarioDao.buscarPorEmail(request.getEmail())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo() || !passwordEncoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new CredencialesInvalidasException();
        }
        return construirRespuesta(usuario);
    }

    @Override
    public String solicitarRecuperacion(String email) {
        // La HU 1.3 pide avisar cuando el correo no esta registrado.
        Usuario usuario = usuarioDao.buscarPorEmail(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una cuenta con ese email"));

        String tokenRecuperacion = jwtUtil.generarTokenRecuperacion(usuario.getIdUsuario(), usuario.getEmail());
        String enlace = urlFrontend + "/recuperar?token=" + tokenRecuperacion;

        PlantillaCorreo correo = new PlantillaCorreo(usuario.getNombreUsuario())
                .parrafo("Recibimos una solicitud para cambiar la contraseña de tu cuenta BusTracka. "
                        + "El enlace vence en 15 minutos.")
                .boton("Crear una nueva contraseña", enlace, true)
                .parrafo("Si no fuiste tú, ignora este correo: tu contraseña no cambiará.");
        notificacionService.enviarCorreo(email, "Recupera tu contraseña de BusTracka", correo.texto(), correo.html());
        log.info("Enlace de recuperacion enviado a {}", email);

        return enlace;
    }

    @Override
    public void confirmarNuevaContrasena(String token, String nuevaContrasena) {
        Claims claims = jwtUtil.validarYObtenerClaims(token, "RECUPERACION");
        Long idUsuario = claims.get("idUsuario", Integer.class).longValue();

        usuarioDao.buscarPorId(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario del token ya no existe"));

        usuarioDao.actualizarContrasena(idUsuario, passwordEncoder.encode(nuevaContrasena));
    }

    @Override
    public void cerrarSesion(String idToken, Instant expiraToken) {
        sesionesCerradas.cerrar(idToken, expiraToken);
    }

    @Override
    public UsuarioResponseDTO obtenerUsuarioActual(Long idUsuario) {
        Usuario usuario = usuarioDao.buscarPorId(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        return new UsuarioResponseDTO(usuario.getIdUsuario(), usuario.getNombreUsuario(),
                usuario.getEmail(), usuario.getIdRol());
    }

    private AuthResponseDTO construirRespuesta(Usuario usuario) {
        String token = jwtUtil.generarTokenSesion(usuario.getIdUsuario(), usuario.getEmail(), usuario.getIdRol());
        return new AuthResponseDTO(usuario.getIdUsuario(), usuario.getNombreUsuario(),
                usuario.getEmail(), usuario.getIdRol(), token, jwtUtil.getExpiracionSesionMs());
    }
}
