package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "fabricante")
@Data
public class Fabricante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idfabricante")
    private Integer idFabricante;

    @Column(name = "nombrefabricante", nullable = false)
    private String nombreFabricante;

    @Column(name = "rfcfabricante", length = 13)
    private String rfcFabricante;

    @Column(name = "telefonofabricante")
    private String telefonoFabricante;

    @Column(name = "correofabricante")
    private String correoFabricante;

    @Column(name = "calle")
    private String calle;

    @Column(name = "noextfabricante")
    private int noExtFabricante;

    @Column(name = "nointfabricante")
    private String noIntFabricante;

    @Column(name = "cpfabricante")
    private int cpFabricante;

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

    @Column(name = "curpfabricante")
    private String curp;
    
    @Column(name = "pfisicafabricante")
    private boolean esPersonaFisica;

    @Column(name = "activo")
    private Boolean activo = true;
}