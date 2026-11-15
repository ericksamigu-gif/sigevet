package com.sigevet.dto;

import java.time.LocalDate;
import java.time.Period;

public class MascotaDTO {

    private Integer id;
    private String nombre;
    private String especie;
    private String raza;
    private LocalDate fechaNacimiento;
    private String genero;
    private Double peso;
    private Integer clienteId;
    private String clienteNombre;
    private String clienteTelefono;
    private String clienteDni;
    private long totalCitas;
    private long totalAtenciones;
    private String edadCalculada;

    public MascotaDTO() {
    }

    public MascotaDTO(Integer id, String nombre, String especie, String raza, LocalDate fechaNacimiento,
                      String genero, Double peso, Integer clienteId, String clienteNombre,
                      String clienteTelefono, String clienteDni, long totalCitas, long totalAtenciones) {
        this.id = id;
        this.nombre = nombre;
        this.especie = especie;
        this.raza = raza;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.peso = peso;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.clienteTelefono = clienteTelefono;
        this.clienteDni = clienteDni;
        this.totalCitas = totalCitas;
        this.totalAtenciones = totalAtenciones;
        this.edadCalculada = calcularEdad(fechaNacimiento);
    }

    private String calcularEdad(LocalDate fecha) {
        if (fecha == null) return "No registrada";
        LocalDate hoy = LocalDate.now();
        if (fecha.isAfter(hoy)) return "Recién nacido";
        Period p = Period.between(fecha, hoy);
        int anos = p.getYears();
        int meses = p.getMonths();
        if (anos > 0) {
            return anos + (anos == 1 ? " año" : " años") + (meses > 0 ? ", " + meses + (meses == 1 ? " mes" : " meses") : "");
        } else if (meses > 0) {
            return meses + (meses == 1 ? " mes" : " meses");
        } else {
            int dias = p.getDays();
            return dias + (dias == 1 ? " día" : " días");
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEspecie() {
        return especie;
    }

    public void setEspecie(String especie) {
        this.especie = especie;
    }

    public String getRaza() {
        return raza;
    }

    public void setRaza(String raza) {
        this.raza = raza;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
        this.edadCalculada = calcularEdad(fechaNacimiento);
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Double getPeso() {
        return peso;
    }

    public void setPeso(Double peso) {
        this.peso = peso;
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

    public String getClienteDni() {
        return clienteDni;
    }

    public void setClienteDni(String clienteDni) {
        this.clienteDni = clienteDni;
    }

    public long getTotalCitas() {
        return totalCitas;
    }

    public void setTotalCitas(long totalCitas) {
        this.totalCitas = totalCitas;
    }

    public long getTotalAtenciones() {
        return totalAtenciones;
    }

    public void setTotalAtenciones(long totalAtenciones) {
        this.totalAtenciones = totalAtenciones;
    }

    public String getEdadCalculada() {
        return edadCalculada;
    }

    public void setEdadCalculada(String edadCalculada) {
        this.edadCalculada = edadCalculada;
    }
}
