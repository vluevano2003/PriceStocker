package com.vluevano.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Map;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.StringProperty;

public class Producto {
    private IntegerProperty idProducto;
    private StringProperty nombreProducto;
    private StringProperty fichaProducto;
    private StringProperty alternoProducto;
    private IntegerProperty existenciaProducto;

    // Listas para relaciones con otras entidades
    private List<Proveedor> proveedores;
    private List<Fabricante> fabricantes;
    private List<Cliente> clientes;
    private List<Empresa> empresas;
    private List<Servicio> servicios;
    private List<Precio> precios;
    private List<Categoria> categorias;

    private Map<Proveedor, Precio> preciosPorProveedor;
    private Map<Cliente, Precio> preciosPorCliente;
    private Map<Empresa, Precio> preciosPorEmpresa;
    private Map<Fabricante, Precio> preciosPorFabricante;

    public Producto(int idProducto, String nombreProducto, String fichaProducto, String alternoProducto,
            int existenciaProducto) {
        this.idProducto = new SimpleIntegerProperty(idProducto);
        this.nombreProducto = new SimpleStringProperty(nombreProducto);
        this.fichaProducto = new SimpleStringProperty(fichaProducto);
        this.alternoProducto = new SimpleStringProperty(alternoProducto);
        this.existenciaProducto = new SimpleIntegerProperty(existenciaProducto);
        this.proveedores = new ArrayList<>();
        this.fabricantes = new ArrayList<>();
        this.clientes = new ArrayList<>();
        this.empresas = new ArrayList<>();
        this.servicios = new ArrayList<>();
        this.precios = new ArrayList<>();
        this.categorias = new ArrayList<>();
        this.preciosPorProveedor = new HashMap<Proveedor, Precio>();
        this.preciosPorCliente = new HashMap<>();
        this.preciosPorEmpresa = new HashMap<>();
        this.preciosPorFabricante = new HashMap<>();

    }

    // Getters y Setters con propiedades
    public int getIdProducto() {
        return idProducto.get();
    }

    public void setIdProducto(int idProducto) {
        this.idProducto.set(idProducto);
    }

    public IntegerProperty idProductoProperty() {
        return idProducto;
    }

    public String getNombreProducto() {
        return nombreProducto.get();
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto.set(nombreProducto);
    }

    public StringProperty nombreProductoProperty() {
        return nombreProducto;
    }

    public String getFichaProducto() {
        return fichaProducto.get();
    }

    public void setFichaProducto(String fichaProducto) {
        this.fichaProducto.set(fichaProducto);
    }

    public StringProperty fichaProductoProperty() {
        return fichaProducto;
    }

    public String getAlternoProducto() {
        return alternoProducto.get();
    }

    public void setAlternoProducto(String alternoProducto) {
        this.alternoProducto.set(alternoProducto);
    }

    public StringProperty alternoProductoProperty() {
        return alternoProducto;
    }

    public int getExistenciaProducto() {
        return existenciaProducto.get();
    }

    public void setExistenciaProducto(int existenciaProducto) {
        this.existenciaProducto.set(existenciaProducto);
    }

    public IntegerProperty existenciaProductoProperty() {
        return existenciaProducto;
    }

    // Métodos adicionales para manejar listas de relaciones
    public List<Proveedor> getProveedores() {
        return proveedores;
    }

    public void setProveedores(List<Proveedor> proveedores) {
        this.proveedores = proveedores;
    }

    public List<Fabricante> getFabricantes() {
        return fabricantes;
    }

    public void setFabricantes(List<Fabricante> fabricantes) {
        this.fabricantes = fabricantes;
    }

    public List<Cliente> getClientes() {
        return clientes;
    }

    public void setClientes(List<Cliente> clientes) {
        this.clientes = clientes;
    }

    public List<Empresa> getEmpresas() {
        return empresas;
    }

    public void setEmpresas(List<Empresa> empresas) {
        this.empresas = empresas;
    }

    public List<Servicio> getServicios() {
        return servicios;
    }

    public void setServicios(List<Servicio> servicios) {
        this.servicios = servicios;
    }

    public List<Precio> getPrecios() {
        return precios;
    }

    public void setPrecios(List<Precio> precios) {
        this.precios = precios;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    // Método para obtener las categorías como cadena
    public String getCategoriasString() {
        if (categorias != null && !categorias.isEmpty()) {
            return categorias.stream()
                    .map(Categoria::toString) // Asume que Categoria tiene un método toString
                    .collect(Collectors.joining(", "));
        }
        return "Sin categorías"; // Si no hay categorías, muestra un mensaje por defecto
    }

    // Método para obtener los proveedores como cadena
    public String getProveedoresString() {
        if (proveedores != null && !proveedores.isEmpty()) {
            return proveedores.stream()
                    .map(Proveedor::toString) // Asume que Proveedor tiene un método toString
                    .collect(Collectors.joining(", "));
        }
        return "Sin proveedores"; // Si no hay proveedores, muestra un mensaje por defecto
    }

    // Método para obtener las empresas como cadena
    public String getEmpresasString() {
        if (empresas != null && !empresas.isEmpty()) {
            return empresas.stream()
                    .map(Empresa::toString) // Asume que Empresa tiene un método toString
                    .collect(Collectors.joining(", "));
        }
        return "Sin empresas"; // Si no hay empresas, muestra un mensaje por defecto
    }

    // Método para obtener los clientes como cadena
    public String getClientesString() {
        if (clientes != null && !clientes.isEmpty()) {
            return clientes.stream()
                    .map(Cliente::toString) // Asume que Cliente tiene un método toString
                    .collect(Collectors.joining(", "));
        }
        return "Sin clientes"; // Si no hay clientes, muestra un mensaje por defecto
    }

    // Método para obtener los fabricantes como cadena
    public String getFabricantesString() {
        if (fabricantes != null && !fabricantes.isEmpty()) {
            return fabricantes.stream()
                    .map(Fabricante::toString) // Asume que Fabricante tiene un método toString
                    .collect(Collectors.joining(", "));
        }
        return "Sin fabricantes"; // Si no hay fabricantes, muestra un mensaje por defecto
    }

    // Método para obtener los servicios como cadena
    public String getServiciosString() {
        if (servicios != null && !servicios.isEmpty()) {
            return servicios.stream()
                    .map(Servicio::toString) // Asume que Servicio tiene un método toString
                    .collect(Collectors.joining(", "));
        }
        return "Sin servicios"; // Si no hay servicios, muestra un mensaje por defecto
    }

    public Map<Proveedor, Precio> getPreciosPorProveedor() {
        return preciosPorProveedor;
    }

    public void setPreciosPorProveedor(Map<Proveedor, Precio> preciosPorProveedor) {
        this.preciosPorProveedor = preciosPorProveedor;
    }

    public Map<Cliente, Precio> getPreciosPorCliente() {
        return preciosPorCliente;
    }

    public void setPreciosPorCliente(Map<Cliente, Precio> preciosPorCliente) {
        this.preciosPorCliente = preciosPorCliente;
    }

    public Map<Empresa, Precio> getPreciosPorEmpresa() {
        return preciosPorEmpresa;
    }

    public void setPreciosPorEmpresa(Map<Empresa, Precio> preciosPorEmpresa) {
        this.preciosPorEmpresa = preciosPorEmpresa;
    }

    public Map<Fabricante, Precio> getPreciosPorFabricante() {
        return preciosPorFabricante;
    }

    public void setPreciosPorFabricante(Map<Fabricante, Precio> preciosPorFabricante) {
        this.preciosPorFabricante = preciosPorFabricante;
    }

    

}
