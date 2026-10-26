package com.sigevet.controller.api;

import com.sigevet.repository.AtencionMedicaRepository;
import com.sigevet.repository.CitaRepository;
import com.sigevet.repository.ClienteRepository;
import com.sigevet.repository.MascotaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private final ClienteRepository clienteRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;
    private final AtencionMedicaRepository atencionMedicaRepository;

    public DashboardRestController(ClienteRepository clienteRepository,
                                  MascotaRepository mascotaRepository,
                                  CitaRepository citaRepository,
                                  AtencionMedicaRepository atencionMedicaRepository) {
        this.clienteRepository = clienteRepository;
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
        this.atencionMedicaRepository = atencionMedicaRepository;
    }

    /**
     * Retorna todas las métricas para las tarjetas principales del Inicio
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasGenerales() {
        long totalClientes = clienteRepository.count();
        long totalMascotas = mascotaRepository.count();

        long citasProgramadas = citaRepository.findAll().stream()
                .filter(c -> c.getEstado() != null &&
                        (c.getEstado().equalsIgnoreCase("PROGRAMADA") || c.getEstado().equalsIgnoreCase("PENDIENTE")))
                .count();

        long totalAtenciones = atencionMedicaRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalClientes", totalClientes);
        stats.put("totalMascotas", totalMascotas);
        stats.put("citasProgramadas", citasProgramadas);
        stats.put("totalAtenciones", totalAtenciones);

        return ResponseEntity.ok(stats);
    }
}
