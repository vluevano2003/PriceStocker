package com.vluevano.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "producto")
@Data
@EqualsAndHashCode(of = "idProducto") // Evita ciclos infinitos en Lombok
@ToString(exclude = {"productoProveedores", "productoClientes", "productoEmpresas", "productoFabricantes", "servicios", "categorias"})
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idproducto")
    private Integer idProducto;

    @Column(name = "nombreproducto", nullable = false)
    private String nombreProducto;

    @Column(name = "fichaproducto", columnDefinition = "TEXT")
    private String fichaProducto;

    @Column(name = "alternoproducto")
    private String alternoProducto;

    @Column(name = "existenciaproducto")
    private int existenciaProducto;

    // Relación simple (sin columnas extra)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "productocategoria", joinColumns = @JoinColumn(name = "idproducto"), inverseJoinColumns = @JoinColumn(name = "idcategoria"))
    private List<Categoria> categorias = new ArrayList<>();

    // Relaciones complejas (con precio y moneda)
    // Usamos cascade ALL para que al guardar el producto, se guarden sus relaciones

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductoProveedor> productoProveedores = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductoCliente> productoClientes = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductoEmpresa> productoEmpresas = new ArrayList<>();

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ProductoFabricante> productoFabricantes = new ArrayList<>();

    // Servicios (asumiendo tabla simple o relación N:M directa según tu schema
    // último)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "productoservicio", joinColumns = @JoinColumn(name = "idproducto"), inverseJoinColumns = @JoinColumn(name = "idservicio"))
    private List<Servicio> servicios = new ArrayList<>();

    // Métodos Helper para mantener la consistencia bidireccional
    public void addProveedor(ProductoProveedor pp) {
        productoProveedores.add(pp);
        pp.setProducto(this);
    }

    public void addCliente(ProductoCliente pc) {
        productoClientes.add(pc);
        pc.setProducto(this);
    }

    public void addEmpresa(ProductoEmpresa pe) {
        productoEmpresas.add(pe);
        pe.setProducto(this);
    }

    public void addFabricante(ProductoFabricante pf) {
        productoFabricantes.add(pf);
        pf.setProducto(this);
    }
}