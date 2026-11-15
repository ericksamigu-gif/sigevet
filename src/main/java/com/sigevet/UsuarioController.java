package com.sigevet;

import com.sigevet.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsuarioController {

    @GetMapping("/usuarios")
    public String usuarios(HttpServletRequest request) {
        Usuario usuario = (Usuario) request.getAttribute("usuario");
        if (usuario == null || !"ADMINISTRADOR".equalsIgnoreCase(usuario.getRol())) {
            // Solo los administradores pueden ver la pantalla de gestión de personal
            return "redirect:/?auth=forbidden";
        }
        return "usuarios";
    }
}
