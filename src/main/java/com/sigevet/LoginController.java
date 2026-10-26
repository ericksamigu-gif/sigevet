package com.sigevet;

import com.sigevet.entity.Usuario;
import com.sigevet.repository.UsuarioRepository;
import com.sigevet.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LoginController {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public LoginController(UsuarioRepository usuarioRepository, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(required = false) String auth,
            @RequestParam(required = false) String logout,
            HttpServletRequest request,
            Model model) {

        // Si ya cuenta con un token JWT válido, redirigir al panel principal
        String existingToken = extraerTokenDeCookie(request);
        if (existingToken != null && jwtService.esTokenValido(existingToken)) {
            return "redirect:/";
        }

        if (auth != null) {
            model.addAttribute("mensajeInfo", "Debes iniciar sesión con tu token para acceder a las opciones del sistema.");
        }
        if (logout != null) {
            model.addAttribute("mensajeExito", "Has cerrado sesión correctamente.");
        }

        return "login";
    }

    @PostMapping("/login")
    public String iniciarSesion(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response,
            HttpSession session,
            Model model) {

        Usuario usuario = usuarioRepository.findByUsername(username.trim());

        if (usuario == null) {
            model.addAttribute("error", "El usuario ingresado no existe.");
            return "login";
        }

        if (usuario.getEstado() != null && !"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            model.addAttribute("error", "El usuario se encuentra inactivo. Contacte al administrador.");
            return "login";
        }

        if (!usuario.getPassword().equals(password)) {
            model.addAttribute("error", "Contraseña incorrecta.");
            return "login";
        }

        // Generar token JWT seguro con los claims del usuario
        String token = jwtService.generarToken(usuario);

        // Guardar token en cookie HttpOnly para seguridad en todas las solicitudes del navegador
        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60); // 24 horas
        response.addCookie(jwtCookie);

        // Almacenar en sesión para compatibilidad con las vistas
        session.setAttribute("usuario", usuario);
        session.setAttribute("jwt_token", token);

        return "redirect:/";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpServletResponse response, HttpSession session) {
        // Eliminar Cookie del Token JWT
        Cookie deleteCookie = new Cookie("jwt_token", "");
        deleteCookie.setHttpOnly(true);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/login?logout=true";
    }

    /**
     * Endpoint API REST para autenticación por token (consumo externo o fetch)
     */
    @PostMapping("/api/auth/login")
    @ResponseBody
    public ResponseEntity<?> apiLogin(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Debe ingresar usuario y contraseña"));
        }

        Usuario usuario = usuarioRepository.findByUsername(username.trim());
        if (usuario == null || !usuario.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales incorrectas"));
        }

        if (usuario.getEstado() != null && !"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Usuario inactivo"));
        }

        String token = jwtService.generarToken(usuario);

        Cookie jwtCookie = new Cookie("jwt_token", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60);
        response.addCookie(jwtCookie);

        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("tipo", "Bearer");
        resp.put("username", usuario.getUsername());
        resp.put("nombre", usuario.getNombre() + " " + usuario.getApellido());
        resp.put("rol", usuario.getRol());

        return ResponseEntity.ok(resp);
    }

    private String extraerTokenDeCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("jwt_token".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
