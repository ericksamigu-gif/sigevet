package com.sigevet.controller.api;

import com.sigevet.dto.AtencionMedicaDTO;
import com.sigevet.entity.AtencionMedica;
import com.sigevet.entity.Cita;
import com.sigevet.entity.Mascota;
import com.sigevet.entity.Usuario;
import com.sigevet.repository.AtencionMedicaRepository;
import com.sigevet.repository.CitaRepository;
import com.sigevet.repository.MascotaRepository;
import com.sigevet.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/atenciones")
public class AtencionMedicaRestController {

    private final AtencionMedicaRepository atencionMedicaRepository;
    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public AtencionMedicaRestController(AtencionMedicaRepository atencionMedicaRepository,
                                        CitaRepository citaRepository,
                                        MascotaRepository mascotaRepository,
                                        UsuarioRepository usuarioRepository) {
        this.atencionMedicaRepository = atencionMedicaRepository;
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Valida si el usuario en sesión tiene perfil clínico (VETERINARIO o ADMINISTRADOR)
     */
    private boolean esPersonalClinico(HttpServletRequest request) {
        if (request == null) return false;
        Usuario u = (Usuario) request.getAttribute("usuario");
        if (u == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                u = (Usuario) session.getAttribute("usuario");
            }
        }
        if (u == null) return false;
        String rol = u.getRol();
        return "ADMINISTRADOR".equalsIgnoreCase(rol) || "VETERINARIO".equalsIgnoreCase(rol);
    }

    private Usuario obtenerUsuarioActual(HttpServletRequest request) {
        if (request == null) return null;
        Usuario u = (Usuario) request.getAttribute("usuario");
        if (u == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                u = (Usuario) session.getAttribute("usuario");
            }
        }
        return u;
    }

    /**
     * Obtener listado de atenciones médicas con búsqueda y filtrado por paciente
     */
    @GetMapping
    public ResponseEntity<List<AtencionMedicaDTO>> listarAtenciones(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Integer mascotaId) {

        List<AtencionMedica> atenciones;

        if (mascotaId != null) {
            atenciones = atencionMedicaRepository.findByMascotaIdOrderByFechaDesc(mascotaId);
        } else if (buscar != null && !buscar.trim().isEmpty()) {
            atenciones = atencionMedicaRepository.buscarPorFiltro(buscar.trim());
        } else {
            atenciones = atencionMedicaRepository.findAllByOrderByFechaDescIdDesc();
        }

        List<AtencionMedicaDTO> dtos = atenciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtener el detalle de una atención médica específica
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        Optional<AtencionMedica> atencionOpt = atencionMedicaRepository.findById(id);
        if (atencionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "La atención médica con ID " + id + " no existe."));
        }
        return ResponseEntity.ok(convertirADTO(atencionOpt.get()));
    }

    /**
     * Obtener la atención médica vinculada a una cita (si existe)
     */
    @GetMapping("/por-cita/{citaId}")
    public ResponseEntity<?> obtenerPorCita(@PathVariable Integer citaId) {
        Optional<AtencionMedica> atencionOpt = atencionMedicaRepository.findByCitaId(citaId);
        if (atencionOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("existe", false));
        }
        AtencionMedicaDTO dto = convertirADTO(atencionOpt.get());
        Map<String, Object> resp = new HashMap<>();
        resp.put("existe", true);
        resp.put("atencion", dto);
        return ResponseEntity.ok(resp);
    }

    /**
     * Obtener estadísticas del módulo de atenciones para KPIs
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        List<AtencionMedica> todas = atencionMedicaRepository.findAll();

        long totalAtenciones = todas.size();
        long atencionesHoy = todas.stream()
                .filter(a -> a.getFecha() != null && a.getFecha().equals(LocalDate.now()))
                .count();

        long totalPacientes = todas.stream()
                .map(a -> a.getMascota() != null ? a.getMascota().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long conTratamiento = todas.stream()
                .filter(a -> a.getTratamiento() != null && !a.getTratamiento().trim().isEmpty())
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalAtenciones", totalAtenciones);
        stats.put("atencionesHoy", atencionesHoy);
        stats.put("totalPacientes", totalPacientes);
        stats.put("conTratamiento", conTratamiento);

        return ResponseEntity.ok(stats);
    }

    /**
     * Citas disponibles para registrar atención médica (pendientes o programadas)
     */
    @GetMapping("/citas-disponibles")
    public ResponseEntity<?> listarCitasDisponibles() {
        List<Cita> citas = citaRepository.findAllByOrderByFechaDescHoraDesc();
        List<Map<String, Object>> result = citas.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("fecha", c.getFecha());
            map.put("hora", c.getHora());
            map.put("motivo", c.getMotivo());
            map.put("estado", c.getEstado());
            map.put("mascotaId", c.getMascota() != null ? c.getMascota().getId() : null);
            map.put("mascotaNombre", c.getMascota() != null ? c.getMascota().getNombre() : "N/D");
            map.put("clienteNombre", (c.getMascota() != null && c.getMascota().getCliente() != null)
                    ? c.getMascota().getCliente().getNombre() + " " + c.getMascota().getCliente().getApellido()
                    : "N/D");
            map.put("veterinarioId", c.getVeterinario() != null ? c.getVeterinario().getId() : null);
            map.put("veterinarioNombre", c.getVeterinario() != null
                    ? "Dr. " + c.getVeterinario().getNombre() + " " + c.getVeterinario().getApellido()
                    : "No asignado");
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Registrar una nueva atención médica
     */
    @PostMapping
    public ResponseEntity<?> registrarAtencion(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        if (!esPersonalClinico(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Se requiere rol VETERINARIO o ADMINISTRADOR para registrar atenciones médicas."));
        }

        try {
            Integer mascotaId = payload.get("mascotaId") != null ? Integer.valueOf(payload.get("mascotaId").toString()) : null;
            Integer citaId = payload.get("citaId") != null && !payload.get("citaId").toString().isEmpty()
                    ? Integer.valueOf(payload.get("citaId").toString()) : null;
            Integer veterinarioId = payload.get("veterinarioId") != null && !payload.get("veterinarioId").toString().isEmpty()
                    ? Integer.valueOf(payload.get("veterinarioId").toString()) : null;

            String diagnostico = payload.get("diagnostico") != null ? payload.get("diagnostico").toString().trim() : "";
            String tratamiento = payload.get("tratamiento") != null ? payload.get("tratamiento").toString().trim() : "";
            String vacunas = payload.get("vacunas") != null ? payload.get("vacunas").toString().trim() : "";
            String alergias = payload.get("alergias") != null ? payload.get("alergias").toString().trim() : "";
            String fechaStr = payload.get("fecha") != null ? payload.get("fecha").toString().trim() : "";

            // Validaciones
            if (diagnostico.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El diagnóstico clínico es obligatorio."));
            }
            if (tratamiento.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El tratamiento o indicaciones médicas son obligatorios."));
            }

            LocalDate fecha = (!fechaStr.isEmpty()) ? LocalDate.parse(fechaStr) : LocalDate.now();

            // Resolver Cita y Mascota
            Cita cita = null;
            Mascota mascota = null;

            if (citaId != null) {
                Optional<Cita> citaOpt = citaRepository.findById(citaId);
                if (citaOpt.isPresent()) {
                    cita = citaOpt.get();
                    mascota = cita.getMascota();
                    // Al registrar la atención, la cita pasa automáticamente a ATENDIDA
                    cita.setEstado("ATENDIDA");
                    citaRepository.save(cita);
                }
            }

            if (mascota == null && mascotaId != null) {
                Optional<Mascota> mascOpt = mascotaRepository.findById(mascotaId);
                if (mascOpt.isPresent()) {
                    mascota = mascOpt.get();
                }
            }

            if (mascota == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe especificar un paciente (mascota) válido."));
            }

            // Resolver Veterinario
            Usuario veterinario = null;
            if (veterinarioId != null) {
                veterinario = usuarioRepository.findById(veterinarioId).orElse(null);
            } else if (cita != null && cita.getVeterinario() != null) {
                veterinario = cita.getVeterinario();
            } else {
                Usuario userActual = obtenerUsuarioActual(request);
                if (userActual != null && "VETERINARIO".equalsIgnoreCase(userActual.getRol())) {
                    veterinario = userActual;
                }
            }

            // Si la cita aún no existe (consulta directa/walk-in), se genera una cita ATENDIDA para cumplir FK de base de datos
            if (cita == null) {
                cita = new Cita();
                cita.setFecha(fecha);
                cita.setHora(LocalTime.now().withNano(0));
                cita.setMotivo("Consulta Médica Directa");
                cita.setEstado("ATENDIDA");
                cita.setMascota(mascota);
                cita.setVeterinario(veterinario != null ? veterinario : usuarioRepository.findAll().stream().findFirst().orElse(null));
                cita = citaRepository.save(cita);
            }

            AtencionMedica atencion = new AtencionMedica();
            atencion.setFecha(fecha);
            atencion.setDiagnostico(diagnostico);
            atencion.setTratamiento(tratamiento);
            atencion.setVacunas(vacunas.isEmpty() ? "Ninguna" : vacunas);
            atencion.setAlergias(alergias.isEmpty() ? "Ninguna conocida" : alergias);
            atencion.setCita(cita);
            atencion.setMascota(mascota);
            atencion.setVeterinario(veterinario);

            AtencionMedica guardada = atencionMedicaRepository.save(atencion);

            return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(guardada));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar la atención médica: " + e.getMessage()));
        }
    }

    /**
     * Actualizar una atención médica existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarAtencion(@PathVariable Integer id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        if (!esPersonalClinico(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Se requiere rol VETERINARIO o ADMINISTRADOR para modificar atenciones médicas."));
        }

        Optional<AtencionMedica> atencionOpt = atencionMedicaRepository.findById(id);
        if (atencionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "La atención médica con ID " + id + " no existe."));
        }

        try {
            AtencionMedica atencion = atencionOpt.get();

            String diagnostico = payload.get("diagnostico") != null ? payload.get("diagnostico").toString().trim() : "";
            String tratamiento = payload.get("tratamiento") != null ? payload.get("tratamiento").toString().trim() : "";
            String vacunas = payload.get("vacunas") != null ? payload.get("vacunas").toString().trim() : "";
            String alergias = payload.get("alergias") != null ? payload.get("alergias").toString().trim() : "";
            String fechaStr = payload.get("fecha") != null ? payload.get("fecha").toString().trim() : "";

            if (!diagnostico.isEmpty()) atencion.setDiagnostico(diagnostico);
            if (!tratamiento.isEmpty()) atencion.setTratamiento(tratamiento);
            if (payload.containsKey("vacunas")) atencion.setVacunas(vacunas);
            if (payload.containsKey("alergias")) atencion.setAlergias(alergias);
            if (!fechaStr.isEmpty()) atencion.setFecha(LocalDate.parse(fechaStr));

            if (payload.containsKey("veterinarioId") && payload.get("veterinarioId") != null) {
                Integer vetId = Integer.valueOf(payload.get("veterinarioId").toString());
                Usuario vet = usuarioRepository.findById(vetId).orElse(null);
                atencion.setVeterinario(vet);
            }

            AtencionMedica actualizada = atencionMedicaRepository.save(atencion);
            return ResponseEntity.ok(convertirADTO(actualizada));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al actualizar la atención: " + e.getMessage()));
        }
    }

    /**
     * Eliminar una atención médica
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarAtencion(@PathVariable Integer id, HttpServletRequest request) {
        if (!esPersonalClinico(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Se requiere rol VETERINARIO o ADMINISTRADOR para eliminar atenciones médicas."));
        }

        Optional<AtencionMedica> atencionOpt = atencionMedicaRepository.findById(id);
        if (atencionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "La atención médica con ID " + id + " no existe."));
        }

        atencionMedicaRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Atención médica eliminada correctamente."));
    }

    private AtencionMedicaDTO convertirADTO(AtencionMedica a) {
        String mascotaNombre = a.getMascota() != null ? a.getMascota().getNombre() : "N/D";
        String mascotaEspecie = a.getMascota() != null ? a.getMascota().getEspecie() : "N/D";
        String mascotaRaza = a.getMascota() != null ? a.getMascota().getRaza() : "";
        Integer mascotaId = a.getMascota() != null ? a.getMascota().getId() : null;

        Integer clienteId = null;
        String clienteNombre = "Sin propietario";
        String clienteTelefono = "";
        if (a.getMascota() != null && a.getMascota().getCliente() != null) {
            clienteId = a.getMascota().getCliente().getId();
            clienteNombre = a.getMascota().getCliente().getNombre() + " " + a.getMascota().getCliente().getApellido();
            clienteTelefono = a.getMascota().getCliente().getTelefono() != null ? a.getMascota().getCliente().getTelefono() : "";
        }

        Integer citaId = a.getCita() != null ? a.getCita().getId() : null;
        String citaMotivo = a.getCita() != null ? a.getCita().getMotivo() : "";
        String citaEstado = a.getCita() != null ? a.getCita().getEstado() : "";

        Integer vetId = a.getVeterinario() != null ? a.getVeterinario().getId() : null;
        String vetNombre = a.getVeterinario() != null
                ? "Dr. " + a.getVeterinario().getNombre() + " " + a.getVeterinario().getApellido()
                : "No asignado";

        return new AtencionMedicaDTO(
                a.getId(),
                a.getFecha(),
                a.getDiagnostico(),
                a.getTratamiento(),
                a.getVacunas(),
                a.getAlergias(),
                citaId,
                citaMotivo,
                citaEstado,
                mascotaId,
                mascotaNombre,
                mascotaEspecie,
                mascotaRaza,
                clienteId,
                clienteNombre,
                clienteTelefono,
                vetId,
                vetNombre
        );
    }
}
