package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "productoproveedor")
@Data
public class ProductoProveedor {

    @EmbeddedId
    private ProductoProveedorId id = new ProductoProveedorId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto") // Mapea parte de la llave compuesta
    @JoinColumn(name = "idproducto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idProveedor") // Mapea parte de la llave compuesta
    @JoinColumn(name = "idproveedor")
    private Proveedor proveedor;

    @Column(name = "costocompraprov")
    private Double costo;

    @Column(name = "monedacompraprov")
    private String moneda;
}