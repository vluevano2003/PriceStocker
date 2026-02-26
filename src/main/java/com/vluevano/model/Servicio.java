package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name = "servicio")
@Data
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idservicio")
    private Integer idServicio;

    @Column(name = "descripcionservicio")
    private String descripcionServicio;

    @Column(name = "costoservicio")
    private double costoServicio;

    @Column(name = "monedaservicio")
    private String monedaServicio;

    @ManyToOne
    @JoinColumn(name = "idprestador")
    @ToString.Exclude
    private PrestadorServicio prestador;

    @Column(name = "activo")
    private Boolean activo = true;

    public Servicio() {
    }

    public Servicio(String descripcion, double costo, String moneda) {
        this.descripcionServicio = descripcion;
        this.costoServicio = costo;
        this.monedaServicio = moneda;
    }

    @Override
    public String toString() {
        return String.format("%s - $%.2f %s", descripcionServicio, costoServicio, monedaServicio);
    }
}