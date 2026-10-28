package com.sigevet.security;

import com.sigevet.entity.Usuario;
import com.sigevet.repository.UsuarioRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtInterceptor(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = extraerToken(request);

        if (token != null && jwtService.esTokenValido(token)) {
            String username = jwtService.obtenerUsername(token);
            if (username != null) {
                Usuario usuario = usuarioRepository.findByUsername(username);
                if (usuario != null && (usuario.getEstado() == null || "ACTIVO".equalsIgnoreCase(usuario.getEstado()))) {
                    // Inyectar datos de autenticación en la solicitud y sesión
                    request.setAttribute("usuario", usuario);
                    request.setAttribute("jwt_token", token);

                    HttpSession session = request.getSession(true);
                    session.setAttribute("usuario", usuario);
                    session.setAttribute("jwt_token", token);

                    return true;
                }
            }
        }

        // Si es una petición AJAX / API REST devuelve 401 en JSON
        String acceptHeader = request.getHeader("Accept");
        String requestedWith = request.getHeader("X-Requested-With");
        if ((acceptHeader != null && acceptHeader.contains("application/json")) ||
            "XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"No autorizado\", \"mensaje\": \"Token JWT ausente o inválido\"}");
            return false;
        }

        // Si es navegación web estándar, redirige a /login
        Cookie deleteCookie = new Cookie("jwt_token", "");
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);

        response.sendRedirect(request.getContextPath() + "/login?auth=required");
        return false;
    }

    private String extraerToken(HttpServletRequest request) {
        // 1. Intentar desde cabecera Authorization: Bearer <token>
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        // 2. Intentar desde Cookies (navegación del navegador)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
