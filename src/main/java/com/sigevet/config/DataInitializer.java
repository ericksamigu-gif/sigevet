/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.sigevet.config;

import com.sigevet.entity.Usuario;
import com.sigevet.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
/**
 *
 * @author USUARIO
 */
@Configuration
public class DataInitializer {
    
    @Bean
    public CommandLineRunner initData(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Tus usuarios actuales de la base de datos con su contraseña en plano
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, "jlopez", "123456", "Juan", "Lopez", "VETERINARIO", "jlopez@sigevet.com");
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, "mgarcia", "123456", "Maria", "Garcia", "VETERINARIO", "mgarcia@sigevet.com");
            crearOActualizarUsuario(usuarioRepository, passwordEncoder, "aperez", "123456", "Ana", "Perez", "RECEPCIONISTA", "aperez@sigevet.com");
        };
    }

    private void crearOActualizarUsuario(UsuarioRepository repo, PasswordEncoder encoder, 
                                         String username, String passwordPlana, 
                                         String nombre, String apellido, String rol, String correo) {
        
        Usuario usuario = repo.findByUsername(username);

        if (usuario == null) {
            usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(encoder.encode(passwordPlana)); // Cifra automáticamente
            usuario.setNombre(nombre);
            usuario.setApellido(apellido);
            usuario.setRol(rol);
            usuario.setCorreo(correo);
            usuario.setEstado("ACTIVO");
            repo.save(usuario);
        } else {
            // Si ya existe, asegura que su contraseña esté cifrada con BCrypt
            usuario.setPassword(encoder.encode(passwordPlana));
            repo.save(usuario);
        }
    }
}
