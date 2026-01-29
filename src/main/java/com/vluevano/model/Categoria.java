package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "categoria")
@Data @NoArgsConstructor @AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCategoria;

    @Column(unique = true, nullable = false)
    private String nombreCategoria;

    private String descripcionCategoria;

    @Override
    public String toString() {
        return nombreCategoria;
    }
}