package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "cliente")
@Data
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcliente")
    private Integer idCliente;

    @Column(name = "nombrecliente", nullable = false)
    private String nombreCliente;

    @Column(name = "rfccliente", length = 13)
    private String rfcCliente;

    @Column(name = "telefonocliente")
    private String telefonoCliente;

    @Column(name = "correocliente")
    private String correoCliente;

    @Column(name = "calle")
    private String calle;

    @Column(name = "noextcliente")
    private int noExtCliente;

    @Column(name = "nointcliente")
    private String noIntCliente;

    @Column(name = "cpcliente")
    private int cpCliente;

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

    @Column(name = "curpcliente")
    private String curp;

    @Column(name = "pfisicacliente")
    private boolean esPersonaFisica;

    @Column(name = "activo")
    private Boolean activo = true;
}