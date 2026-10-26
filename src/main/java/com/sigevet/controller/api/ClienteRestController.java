package com.sigevet.controller.api;

import com.sigevet.dto.ClienteDTO;
import com.sigevet.entity.Cliente;
import com.sigevet.entity.Mascota;
import com.sigevet.repository.ClienteRepository;
import com.sigevet.repository.MascotaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clientes")
public class ClienteRestController {

    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;

    public ClienteRestController(ClienteRepository clienteRepository, MascotaRepository mascotaRepository) {
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
    }

    /**
     * Listar todos los clientes o buscar con filtro en tiempo real
     */
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> listarClientes(@RequestParam(required = false) String buscar) {
        List<Cliente> clientes;
        if (buscar != null && !buscar.trim().isEmpty()) {
            clientes = clienteRepository.buscarPorFiltro(buscar.trim());
        } else {
            clientes = clienteRepository.findAll();
        }

        List<ClienteDTO> dtos = clientes.stream().map(c -> {
            long totalMascotas = mascotaRepository.countByClienteId(c.getId());
            return new ClienteDTO(
                    c.getId(),
                    c.getDni(),
                    c.getNombre(),
                    c.getApellido(),
                    c.getTelefono(),
                    c.getCorreo(),
                    c.getDireccion(),
                    totalMascotas
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Obtener un cliente por su ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Integer id) {
        return clienteRepository.findById(id)
                .map(c -> {
                    long totalMascotas = mascotaRepository.countByClienteId(c.getId());
                    ClienteDTO dto = new ClienteDTO(
                            c.getId(),
                            c.getDni(),
                            c.getNombre(),
                            c.getApellido(),
                            c.getTelefono(),
                            c.getCorreo(),
                            c.getDireccion(),
                            totalMascotas
                    );
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Registrar un nuevo cliente
     */
    @PostMapping
    public ResponseEntity<?> crearCliente(@RequestBody Cliente cliente) {
        // Validaciones de negocio
        if (cliente.getNombre() == null || cliente.getNombre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre es obligatorio"));
        }
        if (cliente.getApellido() == null || cliente.getApellido().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El apellido es obligatorio"));
        }
        if (cliente.getDni() == null || cliente.getDni().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El DNI es obligatorio"));
        }

        String dniLimpio = cliente.getDni().trim();
        if (clienteRepository.existsByDni(dniLimpio)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ya existe un cliente registrado con el DNI: " + dniLimpio));
        }

        cliente.setDni(dniLimpio);
        cliente.setNombre(cliente.getNombre().trim());
        cliente.setApellido(cliente.getApellido().trim());
        if (cliente.getTelefono() != null) cliente.setTelefono(cliente.getTelefono().trim());
        if (cliente.getCorreo() != null) cliente.setCorreo(cliente.getCorreo().trim());
        if (cliente.getDireccion() != null) cliente.setDireccion(cliente.getDireccion().trim());

        Cliente guardado = clienteRepository.save(cliente);

        ClienteDTO dto = new ClienteDTO(
                guardado.getId(),
                guardado.getDni(),
                guardado.getNombre(),
                guardado.getApellido(),
                guardado.getTelefono(),
                guardado.getCorreo(),
                guardado.getDireccion(),
                0
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * Actualizar datos de un cliente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(@PathVariable Integer id, @RequestBody Cliente datos) {
        return clienteRepository.findById(id).map(clienteExistente -> {
            if (datos.getNombre() == null || datos.getNombre().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El nombre no puede estar vacío"));
            }
            if (datos.getApellido() == null || datos.getApellido().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El apellido no puede estar vacío"));
            }
            if (datos.getDni() == null || datos.getDni().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "El DNI no puede estar vacío"));
            }

            String dniLimpio = datos.getDni().trim();
            if (clienteRepository.existsByDniAndIdNot(dniLimpio, id)) {
                return ResponseEntity.badRequest().body(Map.of("error", "El DNI " + dniLimpio + " ya está asignado a otro cliente"));
            }

            clienteExistente.setDni(dniLimpio);
            clienteExistente.setNombre(datos.getNombre().trim());
            clienteExistente.setApellido(datos.getApellido().trim());
            clienteExistente.setTelefono(datos.getTelefono() != null ? datos.getTelefono().trim() : null);
            clienteExistente.setCorreo(datos.getCorreo() != null ? datos.getCorreo().trim() : null);
            clienteExistente.setDireccion(datos.getDireccion() != null ? datos.getDireccion().trim() : null);

            Cliente actualizado = clienteRepository.save(clienteExistente);
            long totalMascotas = mascotaRepository.countByClienteId(actualizado.getId());

            ClienteDTO dto = new ClienteDTO(
                    actualizado.getId(),
                    actualizado.getDni(),
                    actualizado.getNombre(),
                    actualizado.getApellido(),
                    actualizado.getTelefono(),
                    actualizado.getCorreo(),
                    actualizado.getDireccion(),
                    totalMascotas
            );

            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Eliminar un cliente comprobando integridad referencial
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCliente(@PathVariable Integer id) {
        return clienteRepository.findById(id).map(cliente -> {
            long cantidadMascotas = mascotaRepository.countByClienteId(id);
            if (cantidadMascotas > 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "No se puede eliminar al cliente " + cliente.getNombre() + " " + cliente.getApellido() +
                                 " porque tiene " + cantidadMascotas + " mascota(s) vinculada(s). Reasigne o elimine las mascotas antes."
                ));
            }

            clienteRepository.delete(cliente);
            return ResponseEntity.ok(Map.of("mensaje", "Cliente eliminado con éxito", "id", id));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtener estadísticas de clientes para las tarjetas métricas (KPIs)
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        List<Cliente> clientes = clienteRepository.findAll();
        long totalClientes = clientes.size();

        long conMascotas = clientes.stream()
                .filter(c -> mascotaRepository.countByClienteId(c.getId()) > 0)
                .count();

        long sinMascotas = totalClientes - conMascotas;
        long totalMascotas = mascotaRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClientes", totalClientes);
        stats.put("conMascotas", conMascotas);
        stats.put("sinMascotas", sinMascotas);
        stats.put("totalMascotas", totalMascotas);

        return ResponseEntity.ok(stats);
    }

    /**
     * Obtener las mascotas de un cliente específico
     */
    @GetMapping("/{id}/mascotas")
    public ResponseEntity<?> obtenerMascotasDeCliente(@PathVariable Integer id) {
        if (!clienteRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<Mascota> mascotas = mascotaRepository.findByClienteId(id);
        return ResponseEntity.ok(mascotas);
    }
}
