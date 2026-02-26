package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(name = "moneda", length = 3)
    private String moneda;

    @Column(name = "tipo_cambio")
    private Double tipoCambio;

    @OneToMany(mappedBy = "venta", fetch = FetchType.EAGER)
    private List<DetalleVenta> detalles;
}