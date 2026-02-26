package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "proveedor")
@Data
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproveedor")
    private Integer idProveedor;

    @Column(name = "nombreprov", nullable = false) 
    private String nombreProv;

    @Column(name = "rfcproveedor", length = 13)
    private String rfcProveedor;

    @Column(name = "telefonoprov")
    private String telefonoProv;

    @Column(name = "correoprov")
    private String correoProv;

    @Column(name = "calle")
    private String calle;

    @Column(name = "noextprov")
    private int noExtProv;

    @Column(name = "nointprov")
    private String noIntProv;

    @Column(name = "cpproveedor")
    private int cpProveedor;

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

    @Column(name = "curpproveedor")
    private String curp;
    
    @Column(name = "pfisicaproveedor")
    private boolean esPersonaFisica;

    @Column(name = "activo")
    private Boolean activo = true;
}