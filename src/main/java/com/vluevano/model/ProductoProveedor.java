package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "productoproveedor")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"producto", "proveedor"}) 
public class ProductoProveedor {

    @EmbeddedId
    private ProductoProveedorId id = new ProductoProveedorId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto")
    @JoinColumn(name = "idproducto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idProveedor")
    @JoinColumn(name = "idproveedor")
    private Proveedor proveedor;

    @Column(name = "costocompraprov")
    private Double costo;

    @Column(name = "monedacompraprov")
    private String moneda;
}