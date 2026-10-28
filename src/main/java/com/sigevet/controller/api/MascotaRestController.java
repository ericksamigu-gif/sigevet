package com.sigevet.controller.api;

import com.sigevet.dto.MascotaDTO;
import com.sigevet.entity.Cliente;
import com.sigevet.entity.Mascota;
import com.sigevet.repository.CitaRepository;
import com.sigevet.repository.ClienteRepository;
import com.sigevet.repository.MascotaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaRestController {

    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;
    private final CitaRepository citaRepository;

    public MascotaRestController(MascotaRepository mascotaRepository, ClienteRepository clienteRepository, CitaRepository citaRepository) {
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
        this.citaRepository = citaRepository;
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
            return ResponseEntity.badRequest().body(Map.of("error", "Debe asignar un propietario a la mascota"));
        }

        Integer clienteId = Integer.parseInt(clienteIdObj.toString());
        Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
        if (cliente == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El cliente seleccionado no existe"));
        }

        Mascota mascota = new Mascota();
        mascota.setNombre(nombre.trim());
        mascota.setEspecie(especie.trim());
        mascota.setRaza(raza != null ? raza.trim() : null);
        mascota.setGenero(genero != null ? genero.trim() : null);

        if (fechaNacStr != null && !fechaNacStr.trim().isEmpty()) {
            mascota.setFechaNacimiento(LocalDate.parse(fechaNacStr.trim()));
        }

        if (pesoObj != null && !pesoObj.toString().trim().isEmpty()) {
            mascota.setPeso(Double.parseDouble(pesoObj.toString().trim()));
        }

        mascota.setCliente(cliente);

        Mascota guardada = mascotaRepository.save(mascota);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(guardada));
    }

    /**
     * Actualizar una mascota existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMascota(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        return mascotaRepository.findById(id).map(mascota -> {
            String nombre = (String) body.get("nombre");
            String especie = (String) body.get("especie");
            String raza = (String) body.get("raza");
            String genero = (String) body.get("genero");
            String fechaNacStr = (String) body.get("fechaNacimiento");
            Object pesoObj = body.get("peso");
            Object clienteIdObj = body.get("clienteId");

            if (nombre == null || nombre.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre no puede estar vacío"));
            }
            if (especie == null || especie.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "La especie no puede estar vacía"));
            }

            if (clienteIdObj != null) {
                Integer clienteId = Integer.parseInt(clienteIdObj.toString());
                Cliente cliente = clienteRepository.findById(clienteId).orElse(null);
                if (cliente != null) {
                    mascota.setCliente(cliente);
                }
            }

            mascota.setNombre(nombre.trim());
            mascota.setEspecie(especie.trim());
            mascota.setRaza(raza != null ? raza.trim() : null);
            mascota.setGenero(genero != null ? genero.trim() : null);

            if (fechaNacStr != null && !fechaNacStr.trim().isEmpty()) {
                mascota.setFechaNacimiento(LocalDate.parse(fechaNacStr.trim()));
            } else {
                mascota.setFechaNacimiento(null);
            }

            if (pesoObj != null && !pesoObj.toString().trim().isEmpty()) {
                mascota.setPeso(Double.parseDouble(pesoObj.toString().trim()));
            } else {
                mascota.setPeso(null);
            }

            Mascota actualizada = mascotaRepository.save(mascota);
            return ResponseEntity.ok(convertirADTO(actualizada));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Eliminar una mascota
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMascota(@PathVariable Integer id) {
        return mascotaRepository.findById(id).map(mascota -> {
            long citasCount = citaRepository.countByMascotaId(id);
            if (citasCount > 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "No se puede eliminar la mascota '" + mascota.getNombre() +
                                 "' porque tiene " + citasCount + " cita(s) o atenciones registradas."
                ));
            }

            mascotaRepository.delete(mascota);
            return ResponseEntity.ok(Map.of("mensaje", "Mascota eliminada con éxito", "id", id));
        }).orElse(ResponseEntity.notFound().build());
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
        String clienteNombre = m.getCliente() != null ? m.getCliente().getNombre() + " " + m.getCliente().getApellido() : "Sin propietario";
        String clienteTelefono = m.getCliente() != null ? m.getCliente().getTelefono() : "";
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
                citasCount
        );
    }
}
