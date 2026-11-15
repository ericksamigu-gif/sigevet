package com.sigevet.dto;

import java.time.LocalDate;

public class AtencionMedicaDTO {

    private Integer id;
    private LocalDate fecha;
    private String diagnostico;
    private String tratamiento;
    private String vacunas;
    private String alergias;

    // Relación con Cita
    private Integer citaId;
    private String citaMotivo;
    private String citaEstado;

    // Relación con Mascota
    private Integer mascotaId;
    private String mascotaNombre;
    private String mascotaEspecie;
    private String mascotaRaza;

    // Relación con Propietario (Cliente)
    private Integer clienteId;
    private String clienteNombre;
    private String clienteTelefono;

    // Relación con Veterinario (Usuario)
    private Integer veterinarioId;
    private String veterinarioNombre;

    public AtencionMedicaDTO() {
    }

    public AtencionMedicaDTO(Integer id, LocalDate fecha, String diagnostico, String tratamiento,
                             String vacunas, String alergias, Integer citaId, String citaMotivo,
                             String citaEstado, Integer mascotaId, String mascotaNombre,
                             String mascotaEspecie, String mascotaRaza, Integer clienteId,
                             String clienteNombre, String clienteTelefono,
                             Integer veterinarioId, String veterinarioNombre) {
        this.id = id;
        this.fecha = fecha;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.vacunas = vacunas;
        this.alergias = alergias;
        this.citaId = citaId;
        this.citaMotivo = citaMotivo;
        this.citaEstado = citaEstado;
        this.mascotaId = mascotaId;
        this.mascotaNombre = mascotaNombre;
        this.mascotaEspecie = mascotaEspecie;
        this.mascotaRaza = mascotaRaza;
        this.clienteId = clienteId;
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

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public String getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(String tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getVacunas() {
        return vacunas;
    }

    public void setVacunas(String vacunas) {
        this.vacunas = vacunas;
    }

    public String getAlergias() {
        return alergias;
    }

    public void setAlergias(String alergias) {
        this.alergias = alergias;
    }

    public Integer getCitaId() {
        return citaId;
    }

    public void setCitaId(Integer citaId) {
        this.citaId = citaId;
    }

    public String getCitaMotivo() {
        return citaMotivo;
    }

    public void setCitaMotivo(String citaMotivo) {
        this.citaMotivo = citaMotivo;
    }

    public String getCitaEstado() {
        return citaEstado;
    }

    public void setCitaEstado(String citaEstado) {
        this.citaEstado = citaEstado;
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

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
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
