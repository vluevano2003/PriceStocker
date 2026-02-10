package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
@Data
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idventa;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta;

    @ManyToOne
    @JoinColumn(name = "idcliente")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "idusuario")
    private Usuario usuario;

    @Column(name = "total_venta")
    private Double totalVenta;
}