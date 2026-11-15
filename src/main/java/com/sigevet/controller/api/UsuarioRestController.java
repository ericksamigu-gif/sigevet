package com.sigevet.controller.api;

import com.sigevet.dto.UsuarioDTO;
import com.sigevet.entity.Usuario;
import com.sigevet.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioRestController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Valida que el usuario en sesión tenga rol ADMINISTRADOR
     */
    private boolean esAdministrador(HttpServletRequest request) {
        Usuario u = (Usuario) request.getAttribute("usuario");
        return u != null && "ADMINISTRADOR".equalsIgnoreCase(u.getRol());
    }

    @GetMapping
    public ResponseEntity<?> listarUsuarios(HttpServletRequest request) {
        if (!esAdministrador(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Se requiere rol ADMINISTRADOR."));
        }

        List<UsuarioDTO> dtos = usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas(HttpServletRequest request) {
        if (!esAdministrador(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Se requiere rol ADMINISTRADOR."));
        }

        List<Usuario> todos = usuarioRepository.findAll();
        long total = todos.size();
        long admins = todos.stream().filter(u -> "ADMINISTRADOR".equalsIgnoreCase(u.getRol())).count();
        long veterinarios = todos.stream().filter(u -> "VETERINARIO".equalsIgnoreCase(u.getRol())).count();
        long recepcionistas = todos.stream().filter(u -> "RECEPCIONISTA".equalsIgnoreCase(u.getRol())).count();
        long activos = todos.stream().filter(u -> u.getEstado() == null || "ACTIVO".equalsIgnoreCase(u.getEstado())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("administradores", admins);
        stats.put("veterinarios", veterinarios);
        stats.put("recepcionistas", recepcionistas);
        stats.put("activos", activos);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id, HttpServletRequest request) {
        if (!esAdministrador(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado."));
        }

        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok((Object) convertirADTO(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Usuario no encontrado.")));
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        if (!esAdministrador(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Solo administradores pueden crear usuarios."));
        }

        String username = payload.get("username");
        String password = payload.get("password");
        String nombre = payload.get("nombre");
        String apellido = payload.get("apellido");
        String rol = payload.get("rol");
        String correo = payload.get("correo");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario y contraseña son obligatorios."));
        }

        if (usuarioRepository.findByUsername(username.trim()) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya está registrado en el sistema."));
        }

        Usuario nuevo = new Usuario();
        nuevo.setUsername(username.trim());
        nuevo.setPassword(passwordEncoder.encode(password.trim()));
        nuevo.setNombre(nombre != null ? nombre.trim() : "");
        nuevo.setApellido(apellido != null ? apellido.trim() : "");
        nuevo.setRol(rol != null ? rol.trim().toUpperCase() : "RECEPCIONISTA");
        nuevo.setCorreo(correo != null ? correo.trim() : "");
        nuevo.setEstado("ACTIVO");

        Usuario guardado = usuarioRepository.save(nuevo);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarUsuario(@PathVariable Integer id, @RequestBody Map<String, String> payload, HttpServletRequest request) {
        if (!esAdministrador(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acceso denegado."));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado."));
        }

        Usuario u = usuarioOpt.get();

        if (payload.containsKey("nombre")) u.setNombre(payload.get("nombre").trim());
        if (payload.containsKey("apellido")) u.setApellido(payload.get("apellido").trim());
        if (payload.containsKey("rol")) u.setRol(payload.get("rol").trim().toUpperCase());
        if (payload.containsKey("correo")) u.setCorreo(payload.get("correo").trim());
        if (payload.containsKey("estado")) u.setEstado(payload.get("estado").trim().toUpperCase());

        // Actualizar contraseña solo si fue enviada
        String nuevaClave = payload.get("password");
        if (nuevaClave != null && !nuevaClave.trim().isEmpty()) {
            u.setPassword(passwordEncoder.encode(nuevaClave.trim()));
        }

        Usuario actualizado = usuarioRepository.save(u);
        return ResponseEntity.ok(convertirADTO(actualizado));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> alternarEstado(@PathVariable Integer id, @RequestBody Map<String, String> payload, HttpServletRequest request) {
        if (!esAdministrador(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Acceso denegado."));
        }

        Optional<Usuario> opt = usuarioRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuario no encontrado"));

        Usuario u = opt.get();
        String nuevoEstado = payload.getOrDefault("estado", "ACTIVO".equalsIgnoreCase(u.getEstado()) ? "INACTIVO" : "ACTIVO");
        u.setEstado(nuevoEstado);
        usuarioRepository.save(u);

        return ResponseEntity.ok(Map.of("id", u.getId(), "estado", u.getEstado()));
    }

    private UsuarioDTO convertirADTO(Usuario u) {
        return new UsuarioDTO(
                u.getId(),
                u.getUsername(),
                u.getNombre(),
                u.getApellido(),
                u.getRol(),
                u.getCorreo(),
                u.getEstado() != null ? u.getEstado() : "ACTIVO"
        );
    }
}
