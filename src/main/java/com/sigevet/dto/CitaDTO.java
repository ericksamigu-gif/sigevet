package com.sigevet.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class CitaDTO {

    private Integer id;
    private LocalDate fecha;
    private LocalTime hora;
    private String horaFormateada;
    private String motivo;
    private String estado;
    private Integer mascotaId;
    private String mascotaNombre;
    private String mascotaEspecie;
    private String mascotaRaza;
    private String clienteNombre;
    private String clienteTelefono;
    private Integer veterinarioId;
    private String veterinarioNombre;

    public CitaDTO() {
    }

    public CitaDTO(Integer id, LocalDate fecha, LocalTime hora, String motivo, String estado, Integer mascotaId, String mascotaNombre, String mascotaEspecie, String mascotaRaza, String clienteNombre, String clienteTelefono, Integer veterinarioId, String veterinarioNombre) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.horaFormateada = hora != null ? hora.toString().substring(0, Math.min(5, hora.toString().length())) : "";
        this.motivo = motivo;
        this.estado = estado;
        this.mascotaId = mascotaId;
        this.mascotaNombre = mascotaNombre;
        this.mascotaEspecie = mascotaEspecie;
        this.mascotaRaza = mascotaRaza;
        this.clienteNombre = clienteNombre;
        this.clienteTelefono = clienteTelefono;
        this.veterinarioId = veterinarioId;
        this.veterinarioNombre = veterinarioNombre;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
        this.horaFormateada = hora != null ? hora.toString().substring(0, Math.min(5, hora.toString().length())) : "";
    }

    public String getHoraFormateada() {
        return horaFormateada;
    }

    public void setHoraFormateada(String horaFormateada) {
        this.horaFormateada = horaFormateada;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getMascotaId() {
        return mascotaId;
    }

    public void setMascotaId(Integer mascotaId) {
        this.mascotaId = mascotaId;
    }

    public String getMascotaNombre() {
        return mascotaNombre;
    }

    public void setMascotaNombre(String mascotaNombre) {
        this.mascotaNombre = mascotaNombre;
    }

    public String getMascotaEspecie() {
        return mascotaEspecie;
    }

    public void setMascotaEspecie(String mascotaEspecie) {
        this.mascotaEspecie = mascotaEspecie;
    }

    public String getMascotaRaza() {
        return mascotaRaza;
    }

    public void setMascotaRaza(String mascotaRaza) {
        this.mascotaRaza = mascotaRaza;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    public Integer getVeterinarioId() {
        return veterinarioId;
    }

    public void setVeterinarioId(Integer veterinarioId) {
        this.veterinarioId = veterinarioId;
    }

    public String getVeterinarioNombre() {
        return veterinarioNombre;
    }

    public void setVeterinarioNombre(String veterinarioNombre) {
        this.veterinarioNombre = veterinarioNombre;
    }
}
