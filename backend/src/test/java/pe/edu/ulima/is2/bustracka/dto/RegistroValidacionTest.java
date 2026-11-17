package pe.edu.ulima.is2.bustracka.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de las reglas de RegisterRequestDTO (HU 1.1): nombreUsuario, email,
 * contrasena, confirmarContrasena y celular. Se usa el mismo validador que aplica @Valid
 * en el controlador, sin levantar Spring ni la base de datos.
 * Autor: Reaid Yusef Barreto Ibrahim.
 */
class RegistroValidacionTest {

    private static Validator validador;

    @BeforeAll
    static void crearValidador() {
        validador = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private static String campoConError(RegisterRequestDTO registro) {
        Set<ConstraintViolation<RegisterRequestDTO>> errores = validador.validate(registro);
        assertEquals(1, errores.size(), "se esperaba exactamente un error");
        return errores.iterator().next().getPropertyPath().toString();
    }

    @Test
    void aceptaUnRegistroValido() {
        RegisterRequestDTO registro = new RegisterRequestDTO("mariaq", "maria.q@test.pe", "Secreta123", "Secreta123", "987654321");
        assertTrue(validador.validate(registro).isEmpty());
    }

    @Test
    void rechazaContrasenasQueNoCoinciden() {
        RegisterRequestDTO registro = new RegisterRequestDTO("mariaq", "maria.q@test.pe", "Secreta123", "Secreta124", "987654321");
        assertEquals("confirmacionIgual", campoConError(registro));
    }

    @Test
    void rechazaCelularDeOchoDigitos() {
        RegisterRequestDTO registro = new RegisterRequestDTO("mariaq", "maria.q@test.pe", "Secreta123", "Secreta123", "98765432");
        assertEquals("celular", campoConError(registro));
    }

    @Test
    void rechazaCelularQueNoEmpiezaCon9() {
        RegisterRequestDTO registro = new RegisterRequestDTO("mariaq", "maria.q@test.pe", "Secreta123", "Secreta123", "887654321");
        assertEquals("celular", campoConError(registro));
    }

    @Test
    void rechazaEmailSinArroba() {
        RegisterRequestDTO registro = new RegisterRequestDTO("mariaq", "maria.test.pe", "Secreta123", "Secreta123", "987654321");
        assertEquals("email", campoConError(registro));
    }
}
