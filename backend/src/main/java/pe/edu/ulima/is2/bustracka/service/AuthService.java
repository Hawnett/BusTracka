package pe.edu.ulima.is2.bustracka.service;

import pe.edu.ulima.is2.bustracka.dto.AuthResponseDTO;
import pe.edu.ulima.is2.bustracka.dto.LoginRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.RegisterRequestDTO;
import pe.edu.ulima.is2.bustracka.dto.UsuarioResponseDTO;

/**
 * Logica de negocio de autenticacion (SRP: el controller solo enruta HTTP,
 * este servicio decide las reglas: duplicados, hashing, emision de JWT, etc.).
 */
public interface AuthService {

    AuthResponseDTO registrar(RegisterRequestDTO request);

    AuthResponseDTO iniciarSesion(LoginRequestDTO request);

    /** Genera y "envia" (simulado) el enlace de recuperacion. Devuelve el link solo para fines de demo/pruebas. */
    String solicitarRecuperacion(String email);

    void confirmarNuevaContrasena(String token, String nuevaContrasena);

    UsuarioResponseDTO obtenerUsuarioActual(Long idUsuario);
}
