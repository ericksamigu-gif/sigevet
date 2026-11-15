package com.sigevet.controller.api;

import com.sigevet.dto.AtencionMedicaDTO;
import com.sigevet.dto.MascotaDTO;
import com.sigevet.entity.*;
import com.sigevet.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaRestController {

    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;
    private final AtencionMedicaRepository atencionMedicaRepository;
    private final VacunaRepository vacunaRepository;
    private final VacunaAplicadaRepository vacunaAplicadaRepository;
    private final AlergiaRepository alergiaRepository;
    private final MascotaAlergiaRepository mascotaAlergiaRepository;

    public MascotaRestController(MascotaRepository mascotaRepository,
                                 ClienteRepository clienteRepository,
                                 CitaRepository citaRepository,
                                 AtencionMedicaRepository atencionMedicaRepository,
                                 VacunaRepository vacunaRepository,
                                 VacunaAplicadaRepository vacunaAplicadaRepository,
                                 AlergiaRepository alergiaRepository,
                                 MascotaAlergiaRepository mascotaAlergiaRepository) {
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
        this.citaRepository = citaRepository;
        this.atencionMedicaRepository = atencionMedicaRepository;
        this.vacunaRepository = vacunaRepository;
        this.vacunaAplicadaRepository = vacunaAplicadaRepository;
        this.alergiaRepository = alergiaRepository;
        this.mascotaAlergiaRepository = mascotaAlergiaRepository;
    }

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

    /**
     * Listar mascotas con filtro de búsqueda opcional
     */
    @GetMapping
    public ResponseEntity<List<MascotaDTO>> listarMascotas(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) Integer clienteId) {

        List<Mascota> mascotas;
        if (clienteId != null) {
            mascotas = mascotaRepository.findByClienteId(clienteId);
        } else if (buscar != null && !buscar.trim().isEmpty()) {
            mascotas = mascotaRepository.buscarPorFiltro(buscar.trim());
        } else {
            mascotas = mascotaRepository.findAll();
        }

        List<MascotaDTO> dtos = mascotas.stream().map(this::convertirADTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtener una mascota por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        return mascotaRepository.findById(id)
                .map(m -> ResponseEntity.ok(convertirADTO(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * EXPEDIENTE CLÍNICO COMPLETO DE LA MASCOTA (Ficha Integral, Consultas, Vacunas y Alergias)
     */
    @GetMapping("/{id}/expediente")
    public ResponseEntity<?> obtenerExpedienteClinico(@PathVariable Integer id) {
        Optional<Mascota> mascOpt = mascotaRepository.findById(id);
        if (mascOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "La mascota con ID " + id + " no existe."));
        }

        Mascota m = mascOpt.get();
        MascotaDTO mascotaDTO = convertirADTO(m);

        // 1. Historial de Atenciones Médicas
        List<AtencionMedica> atenciones = atencionMedicaRepository.findByMascotaIdOrderByFechaDesc(id);
        List<Map<String, Object>> atencionesList = atenciones.stream().map(a -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.getId());
            map.put("fecha", a.getFecha());
            map.put("diagnostico", a.getDiagnostico());
            map.put("tratamiento", a.getTratamiento());
            map.put("vacunas", a.getVacunas());
            map.put("alergias", a.getAlergias());
            map.put("citaId", a.getCita() != null ? a.getCita().getId() : null);
            map.put("veterinarioNombre", a.getVeterinario() != null
                    ? "Dr. " + a.getVeterinario().getNombre() + " " + a.getVeterinario().getApellido()
                    : "No asignado");
            return map;
        }).collect(Collectors.toList());

        // 2. Historial de Citas
        List<Cita> citas = citaRepository.findAllByOrderByFechaDescHoraDesc().stream()
                .filter(c -> c.getMascota() != null && c.getMascota().getId().equals(id))
                .collect(Collectors.toList());

        List<Map<String, Object>> citasList = citas.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("fecha", c.getFecha());
            map.put("hora", c.getHora());
            map.put("motivo", c.getMotivo());
            map.put("estado", c.getEstado());
            map.put("veterinarioNombre", c.getVeterinario() != null
                    ? "Dr. " + c.getVeterinario().getNombre() + " " + c.getVeterinario().getApellido()
                    : "No asignado");
            return map;
        }).collect(Collectors.toList());

        // 3. Carnet de Vacunas Aplicadas (de vacuna_aplicada y de atenciones médicas)
        List<Map<String, Object>> vacunasList = new ArrayList<>();

        // Registradas en tabla vacuna_aplicada
        List<VacunaAplicada> aplicadas = vacunaAplicadaRepository.findByMascotaId(id);
        for (VacunaAplicada va : aplicadas) {
            Map<String, Object> vMap = new HashMap<>();
            vMap.put("id", va.getId());
            vMap.put("nombre", va.getVacuna() != null ? va.getVacuna().getNombre() : "Vacuna");
            vMap.put("fechaAplicacion", va.getFechaAplicacion());
            vMap.put("dosis", va.getDosis() != null ? va.getDosis() : "Dosis estándar");
            vMap.put("observaciones", va.getObservaciones());
            vacunasList.add(vMap);
        }

        // También consolidar las vacunas anotadas en las atenciones
        for (AtencionMedica a : atenciones) {
            if (a.getVacunas() != null && !a.getVacunas().trim().isEmpty() &&
                !a.getVacunas().equalsIgnoreCase("Ninguna") && !a.getVacunas().equalsIgnoreCase("No")) {
                Map<String, Object> vMap = new HashMap<>();
                vMap.put("id", "at-" + a.getId());
                vMap.put("nombre", a.getVacunas());
                vMap.put("fechaAplicacion", a.getFecha());
                vMap.put("dosis", "Aplicada en consulta #" + a.getId());
                vMap.put("observaciones", "Veterinario: " + (a.getVeterinario() != null ? a.getVeterinario().getNombre() : "Clínica"));
                vacunasList.add(vMap);
            }
        }

        // 4. Alergias Conocidas (de mascota_alergia y de atenciones médicas)
        List<Map<String, Object>> alergiasList = new ArrayList<>();
        List<MascotaAlergia> alrg = mascotaAlergiaRepository.findByMascotaId(id);
        for (MascotaAlergia ma : alrg) {
            Map<String, Object> aMap = new HashMap<>();
            aMap.put("id", ma.getId());
            aMap.put("nombre", ma.getAlergia() != null ? ma.getAlergia().getNombre() : "Alergia");
            aMap.put("categoria", ma.getAlergia() != null ? ma.getAlergia().getCategoria() : "General");
            aMap.put("severidad", ma.getSeveridad() != null ? ma.getSeveridad() : "Moderada");
            aMap.put("fechaDeteccion", ma.getFechaDeteccion());
            alergiasList.add(aMap);
        }

        for (AtencionMedica a : atenciones) {
            if (a.getAlergias() != null && !a.getAlergias().trim().isEmpty() &&
                !a.getAlergias().equalsIgnoreCase("Ninguna") && !a.getAlergias().equalsIgnoreCase("Ninguna conocida")) {
                Map<String, Object> aMap = new HashMap<>();
                aMap.put("id", "at-" + a.getId());
                aMap.put("nombre", a.getAlergias());
                aMap.put("categoria", "Observada en consulta");
                aMap.put("severidad", "Alerta clínica");
                aMap.put("fechaDeteccion", a.getFecha());
                alergiasList.add(aMap);
            }
        }

        // 5. Resumen Ejecutivo
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("totalVisitas", atenciones.size());
        resumen.put("ultimaVisita", !atenciones.isEmpty() ? atenciones.get(0).getFecha() : "Sin atenciones");
        Optional<Cita> prox = citas.stream()
                .filter(c -> c.getEstado() != null &&
                        (c.getEstado().equalsIgnoreCase("PROGRAMADA") || c.getEstado().equalsIgnoreCase("PENDIENTE")))
                .findFirst();
        resumen.put("proximaCita", prox.isPresent() ? prox.get().getFecha() + " (" + prox.get().getHora() + ")" : "Ninguna programada");

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("mascota", mascotaDTO);
        respuesta.put("atenciones", atencionesList);
        respuesta.put("citas", citasList);
        respuesta.put("vacunas", vacunasList);
        respuesta.put("alergias", alergiasList);
        respuesta.put("resumen", resumen);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Catálogo de vacunas disponibles
     */
    @GetMapping("/catalogo-vacunas")
    public ResponseEntity<?> catalogoVacunas() {
        return ResponseEntity.ok(vacunaRepository.findAll());
    }

    /**
     * Catálogo de alergias disponibles
     */
    @GetMapping("/catalogo-alergias")
    public ResponseEntity<?> catalogoAlergias() {
        return ResponseEntity.ok(alergiaRepository.findAll());
    }

    /**
     * Registrar una vacuna aplicada a la mascota en su carnet
     */
    @PostMapping("/{id}/vacunas")
    public ResponseEntity<?> registrarVacunaMascota(@PathVariable Integer id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        if (!esPersonalClinico(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Se requiere rol VETERINARIO o ADMINISTRADOR para registrar vacunas en el carnet."));
        }

        Optional<Mascota> mascOpt = mascotaRepository.findById(id);
        if (mascOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Mascota no encontrada"));
        }

        try {
            Integer vacunaId = payload.get("vacunaId") != null && !payload.get("vacunaId").toString().isEmpty()
                    ? Integer.valueOf(payload.get("vacunaId").toString()) : null;
            String dosis = payload.get("dosis") != null ? payload.get("dosis").toString().trim() : "Dosis Anual";
            String observaciones = payload.get("observaciones") != null ? payload.get("observaciones").toString().trim() : "";
            String fechaStr = payload.get("fechaAplicacion") != null ? payload.get("fechaAplicacion").toString().trim() : "";

            LocalDate fecha = !fechaStr.isEmpty() ? LocalDate.parse(fechaStr) : LocalDate.now();

            Vacuna vacuna = null;
            if (vacunaId != null) {
                vacuna = vacunaRepository.findById(vacunaId).orElse(null);
            }

            // Si no existe la vacuna seleccionada, buscar o crear una por nombre
            if (vacuna == null && payload.containsKey("nombreVacuna")) {
                String nombreVacuna = payload.get("nombreVacuna").toString().trim();
                vacuna = new Vacuna();
                vacuna.setNombre(nombreVacuna);
                vacuna.setEspecieDestino(mascOpt.get().getEspecie());
                vacuna = vacunaRepository.save(vacuna);
            }

            if (vacuna == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe seleccionar o indicar el nombre de la vacuna."));
            }

            // Buscar la última atención médica de la mascota para asociar (o crear una referencia si existe)
            List<AtencionMedica> atenciones = atencionMedicaRepository.findByMascotaIdOrderByFechaDesc(id);
            AtencionMedica am = atenciones.isEmpty() ? null : atenciones.get(0);

            // Si no tiene atención previa, creamos una atención tipo "Control de Vacunación"
            if (am == null) {
                Cita citaWalkIn = new Cita();
                citaWalkIn.setFecha(fecha);
                citaWalkIn.setHora(java.time.LocalTime.now().withNano(0));
                citaWalkIn.setMotivo("Control de Inmunización / Vacunación");
                citaWalkIn.setEstado("ATENDIDA");
                citaWalkIn.setMascota(mascOpt.get());
                citaWalkIn = citaRepository.save(citaWalkIn);

                am = new AtencionMedica();
                am.setFecha(fecha);
                am.setDiagnostico("Aplicación preventiva de biológico / vacuna.");
                am.setTratamiento("Observación preventiva de 24 horas tras inmunización.");
                am.setVacunas(vacuna.getNombre());
                am.setAlergias("Ninguna");
                am.setCita(citaWalkIn);
                am.setMascota(mascOpt.get());
                am = atencionMedicaRepository.save(am);
            }

            VacunaAplicada va = new VacunaAplicada();
            va.setFechaAplicacion(fecha);
            va.setDosis(dosis);
            va.setObservaciones(observaciones);
            va.setVacuna(vacuna);
            va.setAtencionMedica(am);

            vacunaAplicadaRepository.save(va);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Vacuna registrada con éxito en el carnet"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar vacuna: " + e.getMessage()));
        }
    }

    /**
     * Registrar una alergia a la mascota
     */
    @PostMapping("/{id}/alergias")
    public ResponseEntity<?> registrarAlergiaMascota(@PathVariable Integer id, @RequestBody Map<String, Object> payload, HttpServletRequest request) {
        if (!esPersonalClinico(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: Se requiere rol VETERINARIO o ADMINISTRADOR para registrar alergias en el expediente."));
        }

        Optional<Mascota> mascOpt = mascotaRepository.findById(id);
        if (mascOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Mascota no encontrada"));
        }

        try {
            Integer alergiaId = payload.get("alergiaId") != null && !payload.get("alergiaId").toString().isEmpty()
                    ? Integer.valueOf(payload.get("alergiaId").toString()) : null;
            String severidad = payload.get("severidad") != null ? payload.get("severidad").toString().trim() : "Moderada";
            String fechaStr = payload.get("fechaDeteccion") != null ? payload.get("fechaDeteccion").toString().trim() : "";
            LocalDate fecha = !fechaStr.isEmpty() ? LocalDate.parse(fechaStr) : LocalDate.now();

            Alergia alergia = null;
            if (alergiaId != null) {
                alergia = alergiaRepository.findById(alergiaId).orElse(null);
            }
            if (alergia == null && payload.containsKey("nombreAlergia")) {
                String nombre = payload.get("nombreAlergia").toString().trim();
                alergia = new Alergia();
                alergia.setNombre(nombre);
                alergia.setCategoria("Clínica");
                alergia = alergiaRepository.save(alergia);
            }

            if (alergia == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Debe seleccionar o indicar la alergia."));
            }

            MascotaAlergia ma = new MascotaAlergia();
            ma.setMascota(mascOpt.get());
            ma.setAlergia(alergia);
            ma.setSeveridad(severidad);
            ma.setFechaDeteccion(fecha);
            mascotaAlergiaRepository.save(ma);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("mensaje", "Alergia registrada en el expediente"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar alergia: " + e.getMessage()));
        }
    }

    /**
     * Registrar una nueva mascota
     */
    @PostMapping
    public ResponseEntity<?> crearMascota(@RequestBody Map<String, Object> body) {
        String nombre = (String) body.get("nombre");
        String especie = (String) body.get("especie");
        String raza = (String) body.get("raza");
        String genero = (String) body.get("genero");
        String fechaNacStr = (String) body.get("fechaNacimiento");
        Object pesoObj = body.get("peso");
        Object clienteIdObj = body.get("clienteId");

        if (nombre == null || nombre.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de la mascota es obligatorio"));
        }
        if (especie == null || especie.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "La especie es obligatoria"));
        }
        if (clienteIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El cliente propietario es obligatorio"));
        }

        Integer clienteId = Integer.valueOf(clienteIdObj.toString());
        Optional<Cliente> clienteOpt = clienteRepository.findById(clienteId);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El cliente especificado no existe"));
        }

        Mascota m = new Mascota();
        m.setNombre(nombre.trim());
        m.setEspecie(especie.trim());
        m.setRaza(raza != null ? raza.trim() : "");
        m.setGenero(genero != null ? genero.trim() : "Macho");

        if (fechaNacStr != null && !fechaNacStr.trim().isEmpty()) {
            m.setFechaNacimiento(LocalDate.parse(fechaNacStr.trim()));
        }
        if (pesoObj != null && !pesoObj.toString().trim().isEmpty()) {
            m.setPeso(Double.valueOf(pesoObj.toString()));
        }

        m.setCliente(clienteOpt.get());
        Mascota guardada = mascotaRepository.save(m);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(guardada));
    }

    /**
     * Actualizar una mascota existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMascota(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        Optional<Mascota> opt = mascotaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Mascota m = opt.get();

        if (body.containsKey("nombre")) m.setNombre(((String) body.get("nombre")).trim());
        if (body.containsKey("especie")) m.setEspecie(((String) body.get("especie")).trim());
        if (body.containsKey("raza")) m.setRaza(((String) body.get("raza")).trim());
        if (body.containsKey("genero")) m.setGenero(((String) body.get("genero")).trim());

        if (body.containsKey("fechaNacimiento")) {
            String f = (String) body.get("fechaNacimiento");
            m.setFechaNacimiento((f != null && !f.trim().isEmpty()) ? LocalDate.parse(f.trim()) : null);
        }

        if (body.containsKey("peso")) {
            Object p = body.get("peso");
            m.setPeso((p != null && !p.toString().trim().isEmpty()) ? Double.valueOf(p.toString()) : null);
        }

        if (body.containsKey("clienteId")) {
            Integer clienteId = Integer.valueOf(body.get("clienteId").toString());
            Optional<Cliente> cOpt = clienteRepository.findById(clienteId);
            cOpt.ifPresent(m::setCliente);
        }

        Mascota actualizada = mascotaRepository.save(m);
        return ResponseEntity.ok(convertirADTO(actualizada));
    }

    /**
     * Eliminar una mascota
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMascota(@PathVariable Integer id) {
        Optional<Mascota> opt = mascotaRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        long citasCount = citaRepository.countByMascotaId(id);
        long atencionesCount = atencionMedicaRepository.countByMascotaId(id);

        if (citasCount > 0 || atencionesCount > 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No se puede eliminar la mascota porque tiene historial médico (" + atencionesCount + " atenciones) o citas agendadas (" + citasCount + " citas)."));
        }

        mascotaRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("mensaje", "Mascota eliminada correctamente"));
    }

    /**
     * Estadísticas para las tarjetas KPI de Mascotas
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        List<Mascota> todas = mascotaRepository.findAll();
        long total = todas.size();

        long caninos = todas.stream().filter(m -> m.getEspecie() != null &&
                (m.getEspecie().equalsIgnoreCase("Perro") || m.getEspecie().equalsIgnoreCase("Canino"))).count();

        long felinos = todas.stream().filter(m -> m.getEspecie() != null &&
                (m.getEspecie().equalsIgnoreCase("Gato") || m.getEspecie().equalsIgnoreCase("Felino"))).count();

        long otros = total - (caninos + felinos);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMascotas", total);
        stats.put("caninos", caninos);
        stats.put("felinos", felinos);
        stats.put("otros", otros);

        return ResponseEntity.ok(stats);
    }

    private MascotaDTO convertirADTO(Mascota m) {
        long citasCount = citaRepository.countByMascotaId(m.getId());
        long atencionesCount = atencionMedicaRepository.countByMascotaId(m.getId());

        String clienteNombre = m.getCliente() != null ? m.getCliente().getNombre() + " " + m.getCliente().getApellido() : "Sin propietario";
        String clienteTelefono = m.getCliente() != null ? m.getCliente().getTelefono() : "";
        String clienteDni = m.getCliente() != null ? m.getCliente().getDni() : "";
        Integer clienteId = m.getCliente() != null ? m.getCliente().getId() : null;

        return new MascotaDTO(
                m.getId(),
                m.getNombre(),
                m.getEspecie(),
                m.getRaza(),
                m.getFechaNacimiento(),
                m.getGenero(),
                m.getPeso(),
                clienteId,
                clienteNombre,
                clienteTelefono,
                clienteDni,
                citasCount,
                atencionesCount
        );
    }
}
