/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigevet;

import com.sigevet.entity.AtencionMedica;
import com.sigevet.repository.AtencionMedicaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AtencionMedicaController {

    private final AtencionMedicaRepository atencionMedicaRepository;

    public AtencionMedicaController(AtencionMedicaRepository atencionMedicaRepository) {
        this.atencionMedicaRepository = atencionMedicaRepository;
    }

    @GetMapping("/historial")
    public String historial(Model model) {
        model.addAttribute("atenciones", atencionMedicaRepository.findAll());
        return "historial";
    }
}