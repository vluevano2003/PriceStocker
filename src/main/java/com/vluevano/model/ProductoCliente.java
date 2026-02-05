package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "productocliente")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"producto", "cliente"})
public class ProductoCliente {

    @EmbeddedId
    private ProductoClienteId id = new ProductoClienteId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto")
    @JoinColumn(name = "idproducto")
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idCliente")
    @JoinColumn(name = "idcliente")
    private Cliente cliente;

    @Column(name = "costoventa")
    private Double costo;

    @Column(name = "monedaventa")
    private String moneda;
}