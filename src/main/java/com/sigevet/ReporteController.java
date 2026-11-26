package com.sigevet;

import com.sigevet.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReporteController {

    @GetMapping("/reportes")
    public String verReportes(HttpServletRequest request, Model model) {
        Usuario usuario = (Usuario) request.getAttribute("usuario");
        if (usuario == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                usuario = (Usuario) session.getAttribute("usuario");
            }
        }
        model.addAttribute("usuario", usuario);
        return "reportes";
    }
}
