package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "empresa")
@Data
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idempresa")
    private Integer idEmpresa;

    @Column(name = "nombreemp", nullable = false)
    private String nombreEmpresa;

    @Column(name = "rfcempresa", length = 13)
    private String rfcEmpresa;

    @Column(name = "telefonoempresa")
    private String telefonoEmpresa;

    @Column(name = "correoempresa")
    private String correoEmpresa;

    @Column(name = "calle")
    private String calle;

    @Column(name = "noextempresa")
    private int noExtEmpresa;

    @Column(name = "nointempresa")
    private String noIntEmpresa;

    @Column(name = "cpempresa")
    private int cpEmpresa;

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

    @Column(name = "curpempresa")
    private String curp;
    
    @Column(name = "pfisicaempresa")
    private boolean esPersonaFisica;

    @Column(name = "activo")
    private Boolean activo = true;
}