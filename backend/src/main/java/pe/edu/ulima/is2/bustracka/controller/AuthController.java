package pe.edu.ulima.is2.bustracka.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.ulima.is2.bustracka.dto.*;
import pe.edu.ulima.is2.bustracka.service.AuthService;

import java.util.Map;

/**
 * Expone los endpoints REST de autenticacion. Es una capa delgada: solo
 * traduce HTTP <-> DTOs y delega toda la logica en AuthService (SRP).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** RF1: Registro de usuario (valida email y usuario duplicados dentro del service). */
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        AuthResponseDTO respuesta = authService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    /** RF2: Inicio de sesion (valida credenciales y retorna un JWT). */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO respuesta = authService.iniciarSesion(request);
        return ResponseEntity.ok(respuesta);
    }

    /** RF3 (paso 1): solicita la recuperacion; "envia" (simulado) el enlace con el token. */
    @PostMapping("/recover")
    public ResponseEntity<Map<String, String>> solicitarRecuperacion(@Valid @RequestBody RecoverPasswordRequestDTO request) {
        String enlace = authService.solicitarRecuperacion(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "mensaje", "Si el correo existe, se envio un enlace de recuperacion",
                // Se expone el enlace solo para poder probar el flujo en el
                // Sprint 1 sin un servidor de correo real; en produccion NO
                // se devolveria en la respuesta HTTP.
                "enlaceDemo", enlace
        ));
    }

    /** RF3 (paso 2): confirma la nueva contrasena usando el token recibido en el enlace. */
    @PutMapping("/recover")
    public ResponseEntity<Map<String, String>> confirmarRecuperacion(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.confirmarNuevaContrasena(request.getToken(), request.getNuevaContrasena());
        return ResponseEntity.ok(Map.of("mensaje", "Contrasena actualizada correctamente"));
    }

    /**
     * RF4 de soporte: valida que el JWT siga siendo valido y devuelve los
     * datos del usuario autenticado. El "cierre de sesion" en si es
     * responsabilidad del cliente (borrar el token); al ser JWT sin estado,
     * no hay nada que invalidar en el servidor en este sprint.
     */
    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> usuarioActual(HttpServletRequest httpRequest) {
        Long idUsuario = ((Integer) httpRequest.getAttribute("idUsuario")).longValue();
        return ResponseEntity.ok(authService.obtenerUsuarioActual(idUsuario));
    }
}
