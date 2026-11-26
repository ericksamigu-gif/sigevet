package com.sigevet.controller.api;

import com.sigevet.entity.AtencionMedica;
import com.sigevet.entity.Cita;
import com.sigevet.entity.Reporte;
import com.sigevet.entity.Usuario;
import com.sigevet.repository.AtencionMedicaRepository;
import com.sigevet.repository.CitaRepository;
import com.sigevet.repository.ReporteRepository;
import com.sigevet.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
public class ReporteRestController {

    private final CitaRepository citaRepository;
    private final AtencionMedicaRepository atencionMedicaRepository;
    private final ReporteRepository reporteRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteRestController(CitaRepository citaRepository,
                                 AtencionMedicaRepository atencionMedicaRepository,
                                 ReporteRepository reporteRepository,
                                 UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.atencionMedicaRepository = atencionMedicaRepository;
        this.reporteRepository = reporteRepository;
        this.usuarioRepository = usuarioRepository;
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
     * Generar datos consolidados del reporte (Citas Atendidas y Flujo de Pacientes)
     */
    @GetMapping("/resumen")
    public ResponseEntity<?> obtenerResumenReporte(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Integer veterinarioId) {

        LocalDate hoy = LocalDate.now();
        LocalDate fInicio = (fechaInicio != null && !fechaInicio.isBlank())
                ? LocalDate.parse(fechaInicio.trim())
                : hoy.minusDays(6); // Últimos 7 días por defecto
        LocalDate fFin = (fechaFin != null && !fechaFin.isBlank())
                ? LocalDate.parse(fechaFin.trim())
                : hoy;

        List<Cita> todasCitas = citaRepository.findAll();
        List<AtencionMedica> todasAtenciones = atencionMedicaRepository.findAll();

        // 1. Filtrar citas por rango de fecha y veterinario
        List<Cita> citasFiltradas = todasCitas.stream()
                .filter(c -> c.getFecha() != null)
                .filter(c -> !c.getFecha().isBefore(fInicio) && !c.getFecha().isAfter(fFin))
                .filter(c -> veterinarioId == null || (c.getVeterinario() != null && c.getVeterinario().getId().equals(veterinarioId)))
                .collect(Collectors.toList());

        // 2. Filtrar atenciones médicas por rango de fecha y veterinario
        List<AtencionMedica> atencionesFiltradas = todasAtenciones.stream()
                .filter(a -> a.getFecha() != null)
                .filter(a -> !a.getFecha().isBefore(fInicio) && !a.getFecha().isAfter(fFin))
                .filter(a -> veterinarioId == null || (a.getVeterinario() != null && a.getVeterinario().getId().equals(veterinarioId)))
                .collect(Collectors.toList());

        long totalCitas = citasFiltradas.size();
        long atendidas = citasFiltradas.stream().filter(c -> "ATENDIDA".equalsIgnoreCase(c.getEstado())).count();
        long pendientes = citasFiltradas.stream().filter(c -> "PENDIENTE".equalsIgnoreCase(c.getEstado()) || "PROGRAMADA".equalsIgnoreCase(c.getEstado())).count();
        long canceladas = citasFiltradas.stream().filter(c -> "CANCELADA".equalsIgnoreCase(c.getEstado())).count();

        double tasaCumplimiento = totalCitas > 0 ? ((double) atendidas / totalCitas) * 100.0 : 0.0;
        tasaCumplimiento = Math.round(tasaCumplimiento * 10.0) / 10.0;

        // Pacientes únicos atendidos
        Set<Integer> pacientesUnicos = new HashSet<>();
        for (Cita c : citasFiltradas) {
            if ("ATENDIDA".equalsIgnoreCase(c.getEstado()) && c.getMascota() != null) {
                pacientesUnicos.add(c.getMascota().getId());
            }
        }
        for (AtencionMedica a : atencionesFiltradas) {
            if (a.getMascota() != null) {
                pacientesUnicos.add(a.getMascota().getId());
            }
        }

        // 3. Flujo semanal de pacientes (Lunes a Domingo)
        Map<DayOfWeek, Long> conteoDias = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek dow : DayOfWeek.values()) {
            conteoDias.put(dow, 0L);
        }

        for (Cita c : citasFiltradas) {
            if (!"CANCELADA".equalsIgnoreCase(c.getEstado())) {
                DayOfWeek dow = c.getFecha().getDayOfWeek();
                conteoDias.put(dow, conteoDias.get(dow) + 1);
            }
        }

        List<Map<String, Object>> flujoSemanal = new ArrayList<>();
        String[] nombresDias = {"Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"};
        DayOfWeek[] dows = {DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        long maxDia = 1;
        for (DayOfWeek dow : dows) {
            if (conteoDias.get(dow) > maxDia) {
                maxDia = conteoDias.get(dow);
            }
        }

        for (int i = 0; i < dows.length; i++) {
            long cant = conteoDias.get(dows[i]);
            Map<String, Object> dMap = new HashMap<>();
            dMap.put("dia", nombresDias[i]);
            dMap.put("cantidad", cant);
            dMap.put("porcentaje", maxDia > 0 ? Math.round(((double) cant / maxDia) * 100.0) : 0);
            flujoSemanal.add(dMap);
        }

        // 4. Distribución por especie
        long caninos = 0;
        long felinos = 0;
        long otros = 0;

        for (Cita c : citasFiltradas) {
            if (c.getMascota() != null && c.getMascota().getEspecie() != null) {
                String esp = c.getMascota().getEspecie().toLowerCase();
                if (esp.contains("canino") || esp.contains("perro")) caninos++;
                else if (esp.contains("felino") || esp.contains("gato")) felinos++;
                else otros++;
            }
        }

        Map<String, Object> distribucionEspecies = new HashMap<>();
        distribucionEspecies.put("caninos", caninos);
        distribucionEspecies.put("felinos", felinos);
        distribucionEspecies.put("otros", otros);

        // 5. Ranking de Veterinarios
        Map<String, Long> vetCount = new HashMap<>();
        for (Cita c : citasFiltradas) {
            if ("ATENDIDA".equalsIgnoreCase(c.getEstado()) && c.getVeterinario() != null) {
                String nombreVet = "Dr. " + c.getVeterinario().getNombre() + " " + c.getVeterinario().getApellido();
                vetCount.put(nombreVet, vetCount.getOrDefault(nombreVet, 0L) + 1);
            }
        }
        String doctorDestacado = "Sin atenciones";
        long maxVet = 0;
        for (Map.Entry<String, Long> entry : vetCount.entrySet()) {
            if (entry.getValue() > maxVet) {
                maxVet = entry.getValue();
                doctorDestacado = entry.getKey() + " (" + maxVet + " atenciones)";
            }
        }

        // 6. Lista detallada de atenciones para la tabla y para la impresión oficial A4
        List<Map<String, Object>> detalle = citasFiltradas.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("fecha", c.getFecha());
            map.put("hora", c.getHora() != null ? c.getHora().toString() : "--:--");
            map.put("mascotaNombre", c.getMascota() != null ? c.getMascota().getNombre() : "N/D");
            map.put("mascotaEspecie", c.getMascota() != null ? c.getMascota().getEspecie() : "N/D");
            map.put("clienteNombre", (c.getMascota() != null && c.getMascota().getCliente() != null)
                    ? c.getMascota().getCliente().getNombre() + " " + c.getMascota().getCliente().getApellido()
                    : "N/D");
            map.put("veterinarioNombre", c.getVeterinario() != null
                    ? "Dr. " + c.getVeterinario().getNombre() + " " + c.getVeterinario().getApellido()
                    : "No asignado");
            map.put("motivo", c.getMotivo() != null ? c.getMotivo() : "Consulta General");
            map.put("estado", c.getEstado() != null ? c.getEstado() : "PROGRAMADA");
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("fechaInicio", fInicio.toString());
        respuesta.put("fechaFin", fFin.toString());
        respuesta.put("totalCitas", totalCitas);
        respuesta.put("atendidas", atendidas);
        respuesta.put("pendientes", pendientes);
        respuesta.put("canceladas", canceladas);
        respuesta.put("tasaCumplimiento", tasaCumplimiento);
        respuesta.put("pacientesUnicos", pacientesUnicos.size());
        respuesta.put("doctorDestacado", doctorDestacado);
        respuesta.put("flujoSemanal", flujoSemanal);
        respuesta.put("distribucionEspecies", distribucionEspecies);
        respuesta.put("detalle", detalle);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Registrar la emisión de un reporte en la tabla de auditoría 'reporte'
     */
    @PostMapping("/registrar")
    public ResponseEntity<?> registrarEmisionReporte(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String titulo = body.getOrDefault("titulo", "Reporte de Citas Atendidas y Flujo de Pacientes");
        String tipo = body.getOrDefault("tipoReporte", "GERENCIAL");

        Usuario usuarioActual = obtenerUsuarioActual(request);

        Reporte reporte = new Reporte();
        reporte.setTitulo(titulo);
        reporte.setTipoReporte(tipo);
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setUsuario(usuarioActual);

        Reporte guardado = reporteRepository.save(reporte);

        Map<String, Object> resp = new HashMap<>();
        resp.put("id", guardado.getId());
        resp.put("titulo", guardado.getTitulo());
        resp.put("tipoReporte", guardado.getTipoReporte());
        resp.put("fechaGeneracion", guardado.getFechaGeneracion());
        resp.put("mensaje", "Reporte registrado en auditoría correctamente");

        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * Listar historial de reportes generados
     */
    @GetMapping("/historial")
    public ResponseEntity<?> listarHistorialReportes() {
        List<Reporte> lista = reporteRepository.findAllByOrderByFechaGeneracionDesc();
        List<Map<String, Object>> result = lista.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("titulo", r.getTitulo());
            map.put("tipoReporte", r.getTipoReporte());
            map.put("fechaGeneracion", r.getFechaGeneracion());
            map.put("usuario", r.getUsuario() != null
                    ? r.getUsuario().getNombre() + " " + r.getUsuario().getApellido()
                    : "Sistema");
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * Exportar el detalle de citas y atenciones en formato Excel (.xls / SpreadsheetML)
     */
    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarExcel(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) Integer veterinarioId,
            HttpServletRequest request) {

        LocalDate hoy = LocalDate.now();
        LocalDate fInicio = (fechaInicio != null && !fechaInicio.isBlank())
                ? LocalDate.parse(fechaInicio.trim())
                : hoy.minusDays(6);
        LocalDate fFin = (fechaFin != null && !fechaFin.isBlank())
                ? LocalDate.parse(fechaFin.trim())
                : hoy;

        List<Cita> todasCitas = citaRepository.findAll();
        List<Cita> citasFiltradas = todasCitas.stream()
                .filter(c -> c.getFecha() != null)
                .filter(c -> !c.getFecha().isBefore(fInicio) && !c.getFecha().isAfter(fFin))
                .filter(c -> veterinarioId == null || (c.getVeterinario() != null && c.getVeterinario().getId().equals(veterinarioId)))
                .sorted(Comparator.comparing(Cita::getFecha).thenComparing(c -> c.getHora() != null ? c.getHora() : java.time.LocalTime.MIN))
                .collect(Collectors.toList());

        // Registrar auditoría
        try {
            Usuario u = obtenerUsuarioActual(request);
            Reporte r = new Reporte();
            r.setTitulo("Exportación Excel Citas y Atenciones (" + fInicio + " al " + fFin + ")");
            r.setTipoReporte("EXCEL_CITAS");
            r.setFechaGeneracion(LocalDateTime.now());
            r.setUsuario(u);
            reporteRepository.save(r);
        } catch (Exception ignored) {}

        StringBuilder html = new StringBuilder();
        html.append("<html xmlns:o=\"urn:schemas-microsoft-com:office:office\" xmlns:x=\"urn:schemas-microsoft-com:office:excel\" xmlns=\"http://www.w3.org/TR/REC-html40\">");
        html.append("<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");
        html.append("<style>");
        html.append("table { border-collapse: collapse; width: 100%; font-family: Calibri, Arial, sans-serif; font-size: 11pt; }");
        html.append("th { background-color: #0f766e; color: #ffffff; font-weight: bold; border: 1px solid #cbd5e1; padding: 8px; text-align: left; }");
        html.append("td { border: 1px solid #e2e8f0; padding: 6px; }");
        html.append(".title { font-size: 16pt; font-weight: bold; color: #0f766e; }");
        html.append(".subtitle { font-size: 11pt; color: #475569; }");
        html.append("</style></head><body>");

        html.append("<table>");
        html.append("<tr><td colspan=\"9\" class=\"title\">CLÍNICA VETERINARIA SIGEVET</td></tr>");
        html.append("<tr><td colspan=\"9\" class=\"subtitle\">REPORTE DETALLADO DE CITAS Y ATENCIONES CLÍNICAS</td></tr>");
        html.append("<tr><td colspan=\"9\" class=\"subtitle\">Período auditado: Del ").append(fInicio).append(" al ").append(fFin).append(" | Generado: ").append(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</td></tr>");
        html.append("<tr><td colspan=\"9\"></td></tr>");

        // Encabezados de tabla
        html.append("<tr>");
        html.append("<th>ID</th>");
        html.append("<th>Fecha</th>");
        html.append("<th>Hora</th>");
        html.append("<th>Paciente (Mascota)</th>");
        html.append("<th>Especie</th>");
        html.append("<th>Propietario</th>");
        html.append("<th>Médico Veterinario</th>");
        html.append("<th>Motivo / Diagnóstico</th>");
        html.append("<th>Estado</th>");
        html.append("</tr>");

        for (Cita c : citasFiltradas) {
            String mascotaNom = c.getMascota() != null ? c.getMascota().getNombre() : "N/D";
            String mascotaEsp = c.getMascota() != null ? c.getMascota().getEspecie() : "N/D";
            String clienteNom = (c.getMascota() != null && c.getMascota().getCliente() != null)
                    ? c.getMascota().getCliente().getNombre() + " " + c.getMascota().getCliente().getApellido()
                    : "N/D";
            String vetNom = c.getVeterinario() != null
                    ? "Dr. " + c.getVeterinario().getNombre() + " " + c.getVeterinario().getApellido()
                    : "No asignado";

            html.append("<tr>");
            html.append("<td>#").append(c.getId()).append("</td>");
            html.append("<td>").append(c.getFecha() != null ? c.getFecha().toString() : "").append("</td>");
            html.append("<td>").append(c.getHora() != null ? c.getHora().toString() : "--:--").append("</td>");
            html.append("<td><b>").append(mascotaNom).append("</b></td>");
            html.append("<td>").append(mascotaEsp).append("</td>");
            html.append("<td>").append(clienteNom).append("</td>");
            html.append("<td>").append(vetNom).append("</td>");
            html.append("<td>").append(c.getMotivo() != null ? c.getMotivo() : "").append("</td>");
            html.append("<td><b>").append(c.getEstado() != null ? c.getEstado() : "").append("</b></td>");
            html.append("</tr>");
        }

        html.append("</table></body></html>");

        byte[] bytes = html.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "application/vnd.ms-excel; charset=UTF-8")
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Citas_SIGEVET_" + fInicio + "_al_" + fFin + ".xls\"")
                .body(bytes);
    }
}
