/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigevet;

import com.sigevet.entity.Cita;
import com.sigevet.entity.Usuario;
import com.sigevet.repository.CitaRepository;
import com.sigevet.repository.MascotaRepository;
import com.sigevet.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class CitaController {

    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaController(
            CitaRepository citaRepository,
            MascotaRepository mascotaRepository,
            UsuarioRepository usuarioRepository) {

        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/citas")
    public String citas(Model model, HttpSession session) {

        model.addAttribute("citas", citaRepository.findAll());
        model.addAttribute("nuevaCita", new Cita());
        model.addAttribute("mascotas", mascotaRepository.findAll());
        model.addAttribute("veterinarios", usuarioRepository.findAll());

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        model.addAttribute("puedeAgendar",
                usuario != null &&
                "RECEPCIONISTA".equalsIgnoreCase(usuario.getRol()));

        return "citas";
    }

    @PostMapping("/citas/guardar")
    public String guardarCita(
            @ModelAttribute Cita cita,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        // Solo el RECEPCIONISTA puede agendar citas
        if (usuario == null ||
                !"RECEPCIONISTA".equalsIgnoreCase(usuario.getRol())) {

            return "redirect:/citas";
        }

        citaRepository.save(cita);

        return "redirect:/citas";
    }
}