package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "compra")
@Data
public class Compra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idcompra;

    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra;

    @ManyToOne
    @JoinColumn(name = "idproveedor")
    private Proveedor proveedor;

    @ManyToOne
    @JoinColumn(name = "idfabricante")
    private Fabricante fabricante;

    @ManyToOne
    @JoinColumn(name = "idusuario")
    private Usuario usuario;

    @Column(name = "total_compra")
    private Double totalCompra;
}