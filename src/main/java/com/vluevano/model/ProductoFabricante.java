package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "productofabricante")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"producto", "fabricante"})
public class ProductoFabricante {

    @EmbeddedId
    private ProductoFabricanteId id = new ProductoFabricanteId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto")
    @JoinColumn(name = "idproducto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idFabricante")
    @JoinColumn(name = "idfabricante")
    private Fabricante fabricante;

    @Column(name = "costocomprafab")
    private Double costo;

    @Column(name = "monedacomprafab")
    private String moneda;
}