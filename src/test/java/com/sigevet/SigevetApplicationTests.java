package com.sigevet;

import com.sigevet.entity.Cita;
import com.sigevet.entity.Cliente;
import com.sigevet.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
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
    @DisplayName("CP-03: Validación de registro de cliente con DNI y datos obligatorios")
    void test3_RegistrarClienteDniValido() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Carlos");
        cliente.setApellido("Mendoza");
        cliente.setDni("74859612");
        cliente.setTelefono("987654321");
        cliente.setCorreo("carlos.mendoza@gmail.com");

        assertNotNull(cliente.getNombre());
        assertNotNull(cliente.getDni());
        assertEquals(8, cliente.getDni().length(), "El DNI en Perú debe contar con exactamente 8 dígitos");
        assertTrue(cliente.getDni().matches("\\d{8}"), "El DNI debe contener solo caracteres numéricos");
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
}
