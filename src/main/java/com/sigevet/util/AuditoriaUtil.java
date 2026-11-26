package com.sigevet.util;

import com.sigevet.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class AuditoriaUtil {

    private AuditoriaUtil() {}

    /**
     * Obtiene el username o nombre del usuario autenticado en la solicitud actual.
     * Si no hay sesión o se ejecuta en segundo plano, retorna 'SISTEMA'.
     */
    public static String getUsuarioActual() {
        try {
            RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
            if (attribs instanceof ServletRequestAttributes servletAttribs) {
                HttpServletRequest request = servletAttribs.getRequest();
                Usuario u = (Usuario) request.getAttribute("usuario");
                if (u == null) {
                    HttpSession session = request.getSession(false);
                    if (session != null) {
                        u = (Usuario) session.getAttribute("usuario");
                    }
                }
                if (u != null) {
                    if (u.getUsername() != null && !u.getUsername().isBlank()) {
                        return u.getUsername().trim();
                    }
                    if (u.getNombre() != null && !u.getNombre().isBlank()) {
                        return u.getNombre().trim();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "SISTEMA";
    }
}
