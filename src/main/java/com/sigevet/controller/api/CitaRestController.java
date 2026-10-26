package com.sigevet.controller.api;

import com.sigevet.dto.CitaDTO;
import com.sigevet.entity.Cita;
import com.sigevet.entity.Mascota;
import com.sigevet.entity.Usuario;
import com.sigevet.repository.CitaRepository;
import com.sigevet.repository.MascotaRepository;
import com.sigevet.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/citas")
public class CitaRestController {

    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaRestController(CitaRepository citaRepository, MascotaRepository mascotaRepository, UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Listar citas con filtros opcionales (búsqueda, estado, fecha)
     */
    @GetMapping
    public ResponseEntity<List<CitaDTO>> listarCitas(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String estado) {

        List<Cita> citas;
        if (buscar != null && !buscar.trim().isEmpty()) {
            citas = citaRepository.buscarPorFiltro(buscar.trim());
        } else if (estado != null && !estado.trim().isEmpty() && !estado.equalsIgnoreCase("TODOS")) {
            citas = citaRepository.findByEstadoIgnoreCaseOrderByFechaDescHoraDesc(estado.trim());
        } else {
            citas = citaRepository.findAllByOrderByFechaDescHoraDesc();
        }

        List<CitaDTO> dtos = citas.stream().map(this::convertirADTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtener una cita por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        return citaRepository.findById(id)
                .map(c -> ResponseEntity.ok(convertirADTO(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Programar una nueva cita
     */
    @PostMapping
    public ResponseEntity<?> crearCita(@RequestBody Map<String, Object> body) {
        String fechaStr = (String) body.get("fecha");
        String horaStr = (String) body.get("hora");
        String motivo = (String) body.get("motivo");
        String estado = (String) body.get("estado");
        Object mascotaIdObj = body.get("mascotaId");
        Object veterinarioIdObj = body.get("veterinarioId");

        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La fecha de la cita es obligatoria"));
        }
        if (horaStr == null || horaStr.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La hora de la cita es obligatoria"));
        }
        if (mascotaIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe seleccionar un paciente (mascota)"));
        }
        if (veterinarioIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe asignar un veterinario"));
        }

        Integer mascotaId = Integer.parseInt(mascotaIdObj.toString());
        Integer veterinarioId = Integer.parseInt(veterinarioIdObj.toString());

        Mascota mascota = mascotaRepository.findById(mascotaId).orElse(null);
        if (mascota == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "La mascota seleccionada no existe"));
        }

        Usuario veterinario = usuarioRepository.findById(veterinarioId).orElse(null);
        if (veterinario == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El veterinario seleccionado no existe"));
        }

        Cita cita = new Cita();
        cita.setFecha(LocalDate.parse(fechaStr.trim()));
        cita.setHora(LocalTime.parse(horaStr.trim()));
        cita.setMotivo(motivo != null && !motivo.trim().isEmpty() ? motivo.trim() : "Consulta General");
        cita.setEstado(estado != null && !estado.trim().isEmpty() ? estado.trim().toUpperCase() : "PROGRAMADA");
        cita.setMascota(mascota);
        cita.setVeterinario(veterinario);

        Cita guardada = citaRepository.save(cita);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(guardada));
    }

    /**
     * Actualizar una cita
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCita(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return citaRepository.findById(id).map(cita -> {
            String fechaStr = (String) body.get("fecha");
            String horaStr = (String) body.get("hora");
            String motivo = (String) body.get("motivo");
            String estado = (String) body.get("estado");
            Object mascotaIdObj = body.get("mascotaId");
            Object veterinarioIdObj = body.get("veterinarioId");

            if (fechaStr != null && !fechaStr.trim().isEmpty()) {
                cita.setFecha(LocalDate.parse(fechaStr.trim()));
            }
            if (horaStr != null && !horaStr.trim().isEmpty()) {
                cita.setHora(LocalTime.parse(horaStr.trim()));
            }
            if (motivo != null) {
                cita.setMotivo(motivo.trim());
            }
            if (estado != null && !estado.trim().isEmpty()) {
                cita.setEstado(estado.trim().toUpperCase());
            }
            if (mascotaIdObj != null) {
                Integer mascotaId = Integer.parseInt(mascotaIdObj.toString());
                mascotaRepository.findById(mascotaId).ifPresent(cita::setMascota);
            }
            if (veterinarioIdObj != null) {
                Integer vetId = Integer.parseInt(veterinarioIdObj.toString());
                usuarioRepository.findById(vetId).ifPresent(cita::setVeterinario);
            }

            Cita actualizada = citaRepository.save(cita);
            return ResponseEntity.ok(convertirADTO(actualizada));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualizar rápidamente el estado de una cita (ej: ATENDIDA, CANCELADA, PROGRAMADA)
     */
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        String nuevoEstado = body.get("estado");
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe indicar el nuevo estado"));
        }

        return citaRepository.findById(id).map(cita -> {
            cita.setEstado(nuevoEstado.trim().toUpperCase());
            Cita actualizada = citaRepository.save(cita);
            return ResponseEntity.ok(convertirADTO(actualizada));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Eliminar una cita
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCita(@PathVariable Integer id) {
        return citaRepository.findById(id).map(cita -> {
            citaRepository.delete(cita);
            return ResponseEntity.ok(Map.of("mensaje", "Cita cancelada y eliminada con éxito", "id", id));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Estadísticas para las tarjetas KPI de citas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        List<Cita> todas = citaRepository.findAll();
        long totalCitas = todas.size();

        long pendientesProgramadas = todas.stream()
                .filter(c -> c.getEstado() != null &&
                        (c.getEstado().equalsIgnoreCase("PROGRAMADA") || c.getEstado().equalsIgnoreCase("PENDIENTE")))
                .count();

        long atendidas = todas.stream()
                .filter(c -> c.getEstado() != null && c.getEstado().equalsIgnoreCase("ATENDIDA"))
                .count();

        long canceladas = todas.stream()
                .filter(c -> c.getEstado() != null && c.getEstado().equalsIgnoreCase("CANCELADA"))
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCitas", totalCitas);
        stats.put("pendientes", pendientesProgramadas);
        stats.put("atendidas", atendidas);
        stats.put("canceladas", canceladas);

        return ResponseEntity.ok(stats);
    }

    /**
     * Lista de veterinarios para el selector del formulario
     */
    @GetMapping("/veterinarios")
    public ResponseEntity<?> obtenerVeterinarios() {
        List<Usuario> vets = usuarioRepository.findByRolIgnoreCase("VETERINARIO");
        List<Map<String, Object>> lista = vets.stream().map(v -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", v.getId());
            map.put("nombre", v.getNombre() + " " + v.getApellido());
            map.put("correo", v.getCorreo());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(lista);
    }

    private CitaDTO convertirADTO(Cita c) {
        String mascotaNombre = c.getMascota() != null ? c.getMascota().getNombre() : "N/D";
        String mascotaEspecie = c.getMascota() != null ? c.getMascota().getEspecie() : "N/D";
        String mascotaRaza = c.getMascota() != null ? c.getMascota().getRaza() : "";
        Integer mascotaId = c.getMascota() != null ? c.getMascota().getId() : null;

        String clienteNombre = (c.getMascota() != null && c.getMascota().getCliente() != null)
                ? c.getMascota().getCliente().getNombre() + " " + c.getMascota().getCliente().getApellido()
                : "Sin propietario";

        String clienteTelefono = (c.getMascota() != null && c.getMascota().getCliente() != null)
                ? c.getMascota().getCliente().getTelefono()
                : "";

        String vetNombre = c.getVeterinario() != null
                ? "Dr. " + c.getVeterinario().getNombre() + " " + c.getVeterinario().getApellido()
                : "No asignado";
        Integer vetId = c.getVeterinario() != null ? c.getVeterinario().getId() : null;

        return new CitaDTO(
                c.getId(),
                c.getFecha(),
                c.getHora(),
                c.getMotivo(),
                c.getEstado(),
                mascotaId,
                mascotaNombre,
                mascotaEspecie,
                mascotaRaza,
                clienteNombre,
                clienteTelefono,
                vetId,
                vetNombre
        );
    }
}
