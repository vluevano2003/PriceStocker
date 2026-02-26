package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "prestadorservicio")
@Data
public class PrestadorServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idprestador")
    private Integer idPrestador;

    @Column(name = "nombreprestador", nullable = false)
    private String nombrePrestador;

    @Column(name = "rfcprestador", length = 13)
    private String rfcPrestador;

    @Column(name = "telefonoprest")
    private String telefonoPrestador;

    @Column(name = "correoprest")
    private String correoPrestador;

    @Column(name = "calle")
    private String calle;

    @Column(name = "noextprestador")
    private int noExtPrestador;

    @Column(name = "nointprestador")
    private String noIntPrestador;

    @Column(name = "cpprestador")
    private int cpPrestador;

    @Column(name = "colonia")
    private String colonia;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "estado")
    private String estado;

    @Column(name = "pais")
    private String pais;

    @Column(name = "curpprestador")
    private String curp;

    @Column(name = "pfisicaprestador")
    private boolean esPersonaFisica;

    @Column(name = "activo")
    private Boolean activo = true;

    // Relación Uno a Muchos con Servicio
    @OneToMany(mappedBy = "prestador", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @ToString.Exclude
    private List<Servicio> servicios = new ArrayList<>();

    /**
     * Añade un servicio al prestador de servicios
     * 
     * @param servicio
     */
    public void addServicio(Servicio servicio) {
        servicios.add(servicio);
        servicio.setPrestador(this);
    }

    /**
     * Elimina un servicio del prestador de servicios
     * 
     * @param servicio
     */
    public void removeServicio(Servicio servicio) {
        servicios.remove(servicio);
        servicio.setPrestador(null);
    }
}