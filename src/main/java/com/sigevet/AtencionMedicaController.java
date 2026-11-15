package com.sigevet;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AtencionMedicaController {

    @GetMapping("/historial")
    public String historial() {
        return "historial";
    }
}
