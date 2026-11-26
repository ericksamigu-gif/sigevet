package com.sigevet.config;

import com.sigevet.entity.Alergia;
import com.sigevet.entity.Usuario;
import com.sigevet.entity.Vacuna;
import com.sigevet.repository.AlergiaRepository;
import com.sigevet.repository.UsuarioRepository;
import com.sigevet.repository.VacunaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository,
                                     PasswordEncoder passwordEncoder,
                                     VacunaRepository vacunaRepository,
                                     AlergiaRepository alergiaRepository,
                                     DataSource dataSource) {
        return args -> {
            // Asegurar que las columnas activo y auditoría estén actualizadas en las filas existentes
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("UPDATE usuario SET activo = TRUE WHERE activo = FALSE OR activo IS NULL");
                stmt.executeUpdate("UPDATE cliente SET activo = TRUE WHERE activo = FALSE OR activo IS NULL");
                stmt.executeUpdate("UPDATE mascota SET activo = TRUE WHERE activo = FALSE OR activo IS NULL");
                stmt.executeUpdate("UPDATE cita SET activo = TRUE WHERE activo = FALSE OR activo IS NULL");
                stmt.executeUpdate("UPDATE atencion_medica SET activo = TRUE WHERE activo = FALSE OR activo IS NULL");
                stmt.executeUpdate("UPDATE reporte SET activo = TRUE WHERE activo = FALSE OR activo IS NULL");
            } catch (Exception ignored) {}

            // Usuarios con BCrypt y Roles RBAC
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, "admin", "123456", "Administrador", "General", "ADMINISTRADOR", "admin@sigevet.com");
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, "jlopez", "123456", "Juan", "Lopez", "VETERINARIO", "jlopez@sigevet.com");
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, "mgarcia", "123456", "Maria", "Garcia", "VETERINARIO", "mgarcia@sigevet.com");
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, "aperez", "123456", "Ana", "Perez", "RECEPCIONISTA", "aperez@sigevet.com");

            // Semilla de vacunas estándar de veterinaria si la tabla está vacía
            if (vacunaRepository.count() == 0) {
                crearVacuna(vacunaRepository, "Antirrábica", "Canino/Felino", "Vacuna obligatoria anual contra el virus de la rabia.");
                crearVacuna(vacunaRepository, "Séxtuple Canina (DHPPi+L)", "Canino", "Protege contra Moquillo, Parvovirus, Hepatitis, Parainfluenza y Leptospirosis.");
                crearVacuna(vacunaRepository, "Parvovirus Canino", "Canino", "Inmunización temprana contra parvovirosis.");
                crearVacuna(vacunaRepository, "Triple Felina (FVRCP)", "Felino", "Protege contra Rinotraqueítis, Calicivirus y Panleucopenia felina.");
                crearVacuna(vacunaRepository, "Leucemia Felina (FeLV)", "Felino", "Protege contra el virus de la leucemia felina.");
                crearVacuna(vacunaRepository, "KC Tos de las Perreras (Bordetella)", "Canino", "Prevención de la traqueobronquitis infecciosa canina.");
            }

            // Semilla de alergias comunes si la tabla está vacía
            if (alergiaRepository.count() == 0) {
                crearAlergia(alergiaRepository, "Penicilinas / Betalactámicos", "Medicamentosa");
                crearAlergia(alergiaRepository, "Picadura de Pulgas (DAPP)", "Parásitos");
                crearAlergia(alergiaRepository, "Proteína de Pollo / Aves", "Alimentaria");
                crearAlergia(alergiaRepository, "Ácaros del Polvo / Polen", "Ambiental");
                crearAlergia(alergiaRepository, "Antiinflamatorios AINEs", "Medicamentosa");
                crearAlergia(alergiaRepository, "Lácteos y Derivados", "Alimentaria");
            }
        };
    }

    private void crearOActualizarUsuario(UsuarioRepository repo, PasswordEncoder encoder,
                                         String username, String passwordPlana,
                                         String nombre, String apellido, String rol, String correo) {
        Usuario usuario = repo.findByUsernameAnyStatus(username);
        if (usuario == null) {
            usuario = repo.findByUsername(username);
        }
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(encoder.encode(passwordPlana));
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setRol(rol);
            usuario.setCorreo(correo);
            usuario.setEstado("ACTIVO");
            usuario.setActivo(true);
            repo.save(usuario);
        } else {
            usuario.setActivo(true);
            usuario.setPassword(encoder.encode(passwordPlana));
            usuario.setRol(rol);
            if (usuario.getEstado() == null || usuario.getEstado().isBlank()) {
                usuario.setEstado("ACTIVO");
            }
            repo.save(usuario);
        }
    }

    private void crearVacuna(VacunaRepository repo, String nombre, String especie, String descripcion) {
        Vacuna v = new Vacuna();
        v.setNombre(nombre);
        v.setEspecieDestino(especie);
        v.setDescripcion(descripcion);
        repo.save(v);
    }

    private void crearAlergia(AlergiaRepository repo, String nombre, String categoria) {
        Alergia a = new Alergia();
        a.setNombre(nombre);
        a.setCategoria(categoria);
        repo.save(a);
    }
}
