package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "detalle_venta")
@Data
public class DetalleVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer iddetalleventa;

    @ManyToOne
    @JoinColumn(name = "idventa")
    private Venta venta;

    @ManyToOne
    @JoinColumn(name = "idproducto")
    private Producto producto;

    private Integer cantidad;

    @Column(name = "precio_unitario")
    private Double precioUnitario;

    private Double subtotal;
}