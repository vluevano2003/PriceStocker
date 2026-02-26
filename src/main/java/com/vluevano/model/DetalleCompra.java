package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "detalle_compra")
@Data
public class DetalleCompra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer iddetallecompra;

    @ManyToOne
    @JoinColumn(name = "idcompra")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "idproducto")
    private Producto producto;

    private Integer cantidad;

    @Column(name = "costo_unitario") 
    private Double costoUnitario;

    private Double subtotal;
}