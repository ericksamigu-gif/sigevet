package com.sigevet.controller.api;

import com.sigevet.dto.CitaDTO;
import com.sigevet.entity.Cita;
import com.sigevet.entity.Mascota;
import com.sigevet.entity.Usuario;
import com.sigevet.repository.CitaRepository;
import com.sigevet.repository.MascotaRepository;
import com.sigevet.repository.UsuarioRepository;
import com.sigevet.util.AuditoriaUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        LocalDate fecha = LocalDate.parse(fechaStr.trim());
        LocalTime hora = LocalTime.parse(horaStr.trim());

        // Regla de Negocio (No retroactividad): Solo permitir agendar citas en el horario actual o posterior
        LocalDateTime fechaHoraCita = LocalDateTime.of(fecha, hora);
        if (fechaHoraCita.isBefore(LocalDateTime.now().minusMinutes(1))) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No se puede agendar una cita en una fecha u hora pasada. Debe seleccionar el horario actual o posterior."
            ));
        }

        Mascota mascota = mascotaRepository.findById(mascotaId).orElse(null);
        if (mascota == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "La mascota seleccionada no existe"));
        }

        Usuario veterinario = usuarioRepository.findById(veterinarioId).orElse(null);
        if (veterinario == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El veterinario seleccionado no existe"));
        }

        // Regla de Negocio (RN-04): No permitir cruce de horarios para el mismo médico (salvo cita cancelada o médico diferente)
        List<Cita> citasDelDoctor = citaRepository.findByVeterinarioIdAndFecha(veterinarioId, fecha);
        boolean cruceHorario = citasDelDoctor.stream()
                .filter(c -> c.getHora() != null && c.getHora().equals(hora))
                .anyMatch(c -> !"CANCELADA".equalsIgnoreCase(c.getEstado()));

        if (cruceHorario) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "El Dr. " + veterinario.getNombre() + " " + veterinario.getApellido() +
                             " ya cuenta con una cita agendada en la fecha " + fecha + " a las " + hora + ". " +
                             "Seleccione otro horario u otro médico disponible."
            ));
        }

        // Regla de Negocio (RN-02): No solapamiento de paciente (un mismo paciente no puede tener 2 citas activas en la misma fecha y hora)
        List<Cita> citasDeMascota = citaRepository.findByMascotaIdAndFecha(mascotaId, fecha);
        boolean cruceMascota = citasDeMascota.stream()
                .filter(c -> c.getHora() != null && c.getHora().equals(hora))
                .anyMatch(c -> !"CANCELADA".equalsIgnoreCase(c.getEstado()));

        if (cruceMascota) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La mascota paciente '" + mascota.getNombre() + "' ya tiene otra cita activa agendada en la fecha " +
                             fecha + " a las " + hora + ". No es posible solapar la atención del mismo paciente."
            ));
        }

        String estadoFinal = (estado != null && !estado.trim().isEmpty() && !"PENDIENTE".equalsIgnoreCase(estado.trim()))
                ? estado.trim().toUpperCase()
                : "PROGRAMADA";

        Cita cita = new Cita();
        cita.setFecha(fecha);
        cita.setHora(hora);
        cita.setMotivo(motivo != null && !motivo.trim().isEmpty() ? motivo.trim() : "Consulta General");
        cita.setEstado(estadoFinal);
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

            LocalDate nuevaFecha = (fechaStr != null && !fechaStr.trim().isEmpty()) ? LocalDate.parse(fechaStr.trim()) : cita.getFecha();
            LocalTime nuevaHora = (horaStr != null && !horaStr.trim().isEmpty()) ? LocalTime.parse(horaStr.trim()) : cita.getHora();
            String nuevoEstado = (estado != null && !estado.trim().isEmpty()) ? estado.trim().toUpperCase() : cita.getEstado();
            if ("PENDIENTE".equalsIgnoreCase(nuevoEstado)) {
                nuevoEstado = "PROGRAMADA";
            }

            Integer nuevoVetId = (veterinarioIdObj != null) ? Integer.parseInt(veterinarioIdObj.toString())
                    : (cita.getVeterinario() != null ? cita.getVeterinario().getId() : null);

            // Si la cita se mantiene o programa en estado activo, validar no retroactividad ante cambios de fecha/hora
            if (!"ATENDIDA".equalsIgnoreCase(nuevoEstado) && !"CANCELADA".equalsIgnoreCase(nuevoEstado)) {
                LocalDateTime nuevaFechaHora = LocalDateTime.of(nuevaFecha, nuevaHora);
                if (nuevaFechaHora.isBefore(LocalDateTime.now().minusMinutes(1))) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "No se puede reprogramar la cita a una fecha u hora pasada. Debe seleccionar el horario actual o posterior."
                    ));
                }
            }

            // Regla de Negocio (RN-09): Edición restringida para citas Atendidas
            if ("ATENDIDA".equalsIgnoreCase(cita.getEstado()) && (!nuevaFecha.equals(cita.getFecha()) || !nuevaHora.equals(cita.getHora()))) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Una cita que ya fue ATENDIDA no puede cambiar de fecha u hora para mantener la integridad de la historia clínica."
                ));
            }

            // Validar cruce de horario del doctor si la cita permanece activa (no cancelada)
            if (!"CANCELADA".equalsIgnoreCase(nuevoEstado) && nuevoVetId != null) {
                List<Cita> citasDelDoctor = citaRepository.findByVeterinarioIdAndFecha(nuevoVetId, nuevaFecha);
                boolean cruceHorario = citasDelDoctor.stream()
                        .filter(c -> !c.getId().equals(id))
                        .filter(c -> c.getHora() != null && c.getHora().equals(nuevaHora))
                        .anyMatch(c -> !"CANCELADA".equalsIgnoreCase(c.getEstado()));

                if (cruceHorario) {
                    Usuario vetDoc = usuarioRepository.findById(nuevoVetId).orElse(null);
                    String nomVet = (vetDoc != null) ? "Dr. " + vetDoc.getNombre() + " " + vetDoc.getApellido() : "El médico";
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", nomVet + " ya cuenta con una cita agendada en la fecha " + nuevaFecha + " a las " + nuevaHora + ". " +
                                     "Seleccione otro horario u otro médico disponible."
                    ));
                }
            }

            // Regla de Negocio (RN-02): Validar cruce de horario del paciente (mascota)
            Integer targetMascotaId = mascotaIdObj != null ? Integer.parseInt(mascotaIdObj.toString()) : (cita.getMascota() != null ? cita.getMascota().getId() : null);
            if (!"CANCELADA".equalsIgnoreCase(nuevoEstado) && targetMascotaId != null) {
                List<Cita> citasDeMascota = citaRepository.findByMascotaIdAndFecha(targetMascotaId, nuevaFecha);
                boolean cruceMascota = citasDeMascota.stream()
                        .filter(c -> !c.getId().equals(id))
                        .filter(c -> c.getHora() != null && c.getHora().equals(nuevaHora))
                        .anyMatch(c -> !"CANCELADA".equalsIgnoreCase(c.getEstado()));

                if (cruceMascota) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "El paciente ya cuenta con otra cita activa en la fecha " + nuevaFecha + " a las " + nuevaHora + ". No se permite solapar horarios del mismo paciente."
                    ));
                }
            }

            cita.setFecha(nuevaFecha);
            cita.setHora(nuevaHora);
            cita.setEstado(nuevoEstado);

            if (motivo != null) {
                cita.setMotivo(motivo.trim());
            }
            if (mascotaIdObj != null) {
                Integer mascotaId = Integer.parseInt(mascotaIdObj.toString());
                mascotaRepository.findById(mascotaId).ifPresent(cita::setMascota);
            }
            if (nuevoVetId != null) {
                usuarioRepository.findById(nuevoVetId).ifPresent(cita::setVeterinario);
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

        String estadoLimpio = "PENDIENTE".equalsIgnoreCase(nuevoEstado.trim()) ? "PROGRAMADA" : nuevoEstado.trim().toUpperCase();

        return citaRepository.findById(id).map(cita -> {
            cita.setEstado(estadoLimpio);
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
            // Regla de Negocio: Solo permitir eliminar citas en estado PROGRAMADA (no canceladas ni atendidas)
            if (!"PROGRAMADA".equalsIgnoreCase(cita.getEstado())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Solo se permite eliminar citas en estado PROGRAMADA. Las citas con estado " +
                                 cita.getEstado() + " deben conservarse en el registro histórico."
                ));
            }

            cita.setActivo(false);
            cita.setFechaEliminacion(LocalDateTime.now());
            cita.setUsuarioEliminacion(AuditoriaUtil.getUsuarioActual());
            citaRepository.save(cita);
            return ResponseEntity.ok(Map.of("mensaje", "Cita programada eliminada con éxito", "id", id));
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
