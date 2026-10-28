package com.sigevet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MascotaController {

    @GetMapping("/mascotas")
    public String mascotas() {
        return "mascotas";
    }
}
