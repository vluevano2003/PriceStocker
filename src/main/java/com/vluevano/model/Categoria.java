package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "categoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idcategoria")
    private Integer idCategoria;

    @Column(name = "nombrecategoria", unique = true, nullable = false)
    private String nombreCategoria;

    @Column(name = "desccategoria")
    private String descripcionCategoria;

    @Column(name = "activo")
    private Boolean activo = true;

    @Override
    public String toString() {
        return nombreCategoria;
    }
}