package com.sigevet.dto;

public class UsuarioDTO {

    private Integer id;
    private String username;
    private String nombre;
    private String apellido;
    private String rol;
    private String correo;
    private String estado;

    public UsuarioDTO() {}

    public UsuarioDTO(Integer id, String username, String nombre, String apellido, String rol, String correo, String estado) {
        this.id = id;
        this.username = username;
        this.nombre = nombre;
        this.apellido = apellido;
        this.rol = rol;
        this.correo = correo;
        this.estado = estado;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
