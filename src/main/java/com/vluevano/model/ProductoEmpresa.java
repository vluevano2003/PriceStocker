package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "productoempresa")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"producto", "empresa"})
public class ProductoEmpresa {

    @EmbeddedId
    private ProductoEmpresaId id = new ProductoEmpresaId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto")
    @JoinColumn(name = "idproducto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idEmpresa")
    @JoinColumn(name = "idempresa")
    private Empresa empresa;

    // Según tu SQL: costomercado
    @Column(name = "costomercado")
    private Double costo;

    // Según tu SQL: monedamercado
    @Column(name = "monedamercado")
    private String moneda;
}