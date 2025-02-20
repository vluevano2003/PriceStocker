package com.vluevano.model;

public class Servicio {
    private int idServicio;
    private String descripcionServicio;
    private double costoServicio;
    private String monedaServicio;

    public Servicio(int idServicio, String descripcionServicio, double costoServicio, String monedaServicio) {
        this.idServicio = idServicio;
        this.descripcionServicio = descripcionServicio;
        this.costoServicio = costoServicio;
        this.monedaServicio = monedaServicio;
    }

    public Servicio() {
    }


    public Servicio(int idServicio, String descripcionServicio, double costoServicio) {
        this.idServicio = idServicio;
        this.descripcionServicio = descripcionServicio;
        this.costoServicio = costoServicio;
    }

    public Servicio(String descripcionServicio) {
        this.descripcionServicio = descripcionServicio;
    }

    public Servicio(int idServicio2, String descripcionServicio2) {
        this.idServicio = idServicio2;
        this.descripcionServicio = descripcionServicio2;
    }

    public int getIdServicio() {
        return idServicio;
    }

    public void setIdServicio(int idServicio) {
        this.idServicio = idServicio;
    }

    public String getDescripcionServicio() {
        return descripcionServicio;
    }

    public void setDescripcionServicio(String descripcionServicio) {
        this.descripcionServicio = descripcionServicio;
    }

    public double getCostoServicio() {
        return costoServicio;
    }

    public void setCostoServicio(double costoServicio) {
        this.costoServicio = costoServicio;
    }

    public String getMonedaServicio() {
        return monedaServicio;
    }

    public void setMonedaServicio(String monedaServicio) {
        this.monedaServicio = monedaServicio;
    }

    @Override
    public String toString() {
        return this.descripcionServicio;
    }
}
