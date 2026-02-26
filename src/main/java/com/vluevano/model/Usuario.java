package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "usuario")
@Data // Lombok genera getters, setters, toString, etc.
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idusuario")
    private Integer idUsuario;

    @Column(name = "nombreusuario", unique = true, nullable = false)
    private String nombreUsuario;

    @Column(name = "contrasenausuario", nullable = false)
    private String contrasenaUsuario;

    @Column(name = "permiso")
    private boolean permiso;

    @Column(name = "activo")
    private Boolean activo = true;
}