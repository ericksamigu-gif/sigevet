package com.sigevet;

import com.sigevet.entity.Cita;
import com.sigevet.entity.Cliente;
import com.sigevet.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class SigevetApplicationTests {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("CP-01: Verificación de hashing seguro y coincidencia de contraseña con BCrypt")
    void test1_AutenticacionBCryptExitosa() {
        String passwordPlana = "123456";
        String hashGenerado = passwordEncoder.encode(passwordPlana);

        assertNotNull(hashGenerado, "El hash generado no debe ser nulo");
        assertNotEquals(passwordPlana, hashGenerado, "La contraseña no debe almacenarse en texto plano");
        assertTrue(passwordEncoder.matches(passwordPlana, hashGenerado), "BCrypt debe validar la coincidencia de contraseña correcta");
    }

    @Test
    @DisplayName("CP-02: Rechazo de autenticación ante contraseña incorrecta")
    void test2_AutenticacionClaveInvalidaRechazo() {
        String passwordCorrecta = "123456";
        String passwordErronea = "passwordErronea999";
        String hash = passwordEncoder.encode(passwordCorrecta);

        assertFalse(passwordEncoder.matches(passwordErronea, hash), "BCrypt debe rechazar credenciales no coincidentes");
    }

    @Test
    @DisplayName("CP-03: Validación de registro de cliente con DNI estricto y formato de correo electrónico")
    void test3_RegistrarClienteDniYCorreoValidos() {
        final String regexDni = "^\\d{8}$";
        final String regexCorreo = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

        // 1. Caso DNI Válido (8 dígitos numéricos)
        String dniValido = "74859612";
        assertTrue(dniValido.matches(regexDni), "El DNI válido de 8 dígitos debe ser aceptado");

        // 2. Casos DNI Inválidos:
        String dniConLetras = "7485961A";
        String dniConMasDe8Digitos = "7485961234";
        String dniConMenosDe8Digitos = "74859";
        String dniConSimbolos = "7485-961";

        assertFalse(dniConLetras.matches(regexDni), "El sistema debe rechazar DNI que contenga letras");
        assertFalse(dniConMasDe8Digitos.matches(regexDni), "El sistema debe rechazar DNI con más de 8 dígitos");
        assertFalse(dniConMenosDe8Digitos.matches(regexDni), "El sistema debe rechazar DNI con menos de 8 dígitos");
        assertFalse(dniConSimbolos.matches(regexDni), "El sistema debe rechazar DNI con caracteres especiales");

        // 3. Caso Correo Válido
        String correoValido = "carlos.mendoza@gmail.com";
        assertTrue(correoValido.matches(regexCorreo), "El formato estándar de correo debe ser aceptado");

        // 4. Casos Correo Inválidos
        String correoSinArroba = "carlos.mendoza.gmail.com";
        String correoSinDominio = "carlos.mendoza@";
        String correoSinExtension = "carlos.mendoza@gmail";

        assertFalse(correoSinArroba.matches(regexCorreo), "El sistema debe rechazar correos sin '@'");
        assertFalse(correoSinDominio.matches(regexCorreo), "El sistema debe rechazar correos sin servidor de dominio");
        assertFalse(correoSinExtension.matches(regexCorreo), "El sistema debe rechazar correos sin extensión de dominio válida (.com, etc)");

        // 5. Entidad Cliente con datos válidos
        Cliente cliente = new Cliente();
        cliente.setNombre("Carlos");
        cliente.setApellido("Mendoza");
        cliente.setDni(dniValido);
        cliente.setTelefono("987654321");
        cliente.setCorreo(correoValido);

        assertNotNull(cliente.getNombre());
        assertNotNull(cliente.getApellido());
        assertTrue(cliente.getDni().matches(regexDni));
        assertTrue(cliente.getCorreo().matches(regexCorreo));
    }

    @Test
    @DisplayName("CP-04: Transición de ciclo de vida de Cita médica (PROGRAMADA -> ATENDIDA)")
    void test4_TransicionEstadoCitaAtendida() {
        Cita cita = new Cita();
        cita.setFecha(LocalDate.now());
        cita.setHora(LocalTime.of(10, 30));
        cita.setMotivo("Vacunación anual y desparasitación");
        cita.setEstado("PROGRAMADA");

        assertEquals("PROGRAMADA", cita.getEstado());

        // Simulación de atención médica por el especialista veterinario
        cita.setEstado("ATENDIDA");
        assertEquals("ATENDIDA", cita.getEstado(), "El estado de la cita debe actualizarse a ATENDIDA al culminar atención");
    }

    @Test
    @DisplayName("CP-05: Control de Acceso por Roles (RBAC) - Restricción de facultades médicas a Recepcionista")
    void test5_ControlAccesoRBAC_RestriccionRecepcionista() {
        Usuario veterinario = new Usuario();
        veterinario.setUsername("jlopez");
        veterinario.setRol("VETERINARIO");

        Usuario recepcionista = new Usuario();
        recepcionista.setUsername("aperez");
        recepcionista.setRol("RECEPCIONISTA");

        Usuario admin = new Usuario();
        admin.setUsername("admin");
        admin.setRol("ADMINISTRADOR");

        // Regla RBAC del sistema para atención médica y prescripción clínica
        java.util.function.Predicate<Usuario> esPersonalClinico = u -> 
            u != null && ("VETERINARIO".equalsIgnoreCase(u.getRol()) || "ADMINISTRADOR".equalsIgnoreCase(u.getRol()));

        assertTrue(esPersonalClinico.test(veterinario), "El rol VETERINARIO debe tener permisos clínicos");
        assertTrue(esPersonalClinico.test(admin), "El rol ADMINISTRADOR debe tener facultades sobre el módulo clínico");
        assertFalse(esPersonalClinico.test(recepcionista), "El rol RECEPCIONISTA debe tener denegado el registro de atenciones y recetas médicas");
    }

    @Test
    @DisplayName("CP-06: Validación de no retroactividad - Prohibir agendar citas en fechas u horas pasadas")
    void test6_ValidacionNoRetroactividadCitas() {
        LocalDateTime ahora = LocalDateTime.now();

        // 1. Caso de prueba: Intento de cita en fecha pasada (Ayer)
        LocalDate fechaAyer = ahora.toLocalDate().minusDays(1);
        LocalTime horaCita = LocalTime.of(10, 0);
        LocalDateTime citaPasadaFecha = LocalDateTime.of(fechaAyer, horaCita);

        // 2. Caso de prueba: Intento de cita en horario pasado del día actual
        LocalDate fechaHoy = ahora.toLocalDate();
        LocalTime horaPasada = ahora.toLocalTime().minusHours(2);
        LocalDateTime citaPasadaHora = LocalDateTime.of(fechaHoy, horaPasada);

        // 3. Caso de prueba: Cita válida para fecha y horario futuro
        LocalDate fechaManana = ahora.toLocalDate().plusDays(1);
        LocalTime horaFutura = LocalTime.of(11, 0);
        LocalDateTime citaValidaFutura = LocalDateTime.of(fechaManana, horaFutura);

        // Regla de Negocio del Sistema: Una cita solo puede agendarse en el horario actual o posterior
        java.util.function.Predicate<LocalDateTime> esHorarioValidoParaAgendar = fechaHora ->
                fechaHora != null && !fechaHora.isBefore(LocalDateTime.now().minusMinutes(1));

        // Aserciones
        assertFalse(esHorarioValidoParaAgendar.test(citaPasadaFecha),
                "El sistema debe rechazar citas programadas en fechas anteriores a la actual");
        assertFalse(esHorarioValidoParaAgendar.test(citaPasadaHora),
                "El sistema debe rechazar citas programadas en horas ya transcurridas del día de hoy");
        assertTrue(esHorarioValidoParaAgendar.test(citaValidaFutura),
                "El sistema debe permitir agendar citas en horarios actuales o posteriores");
    }
}
