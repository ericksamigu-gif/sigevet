package com.sigevet.security;

import com.sigevet.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalUserAdvice {

    @ModelAttribute("usuario")
    public Usuario populateUsuario(HttpServletRequest request) {
        return (Usuario) request.getAttribute("usuario");
    }

    @ModelAttribute("jwtToken")
    public String populateToken(HttpServletRequest request) {
        return (String) request.getAttribute("jwt_token");
    }
}
