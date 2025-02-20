package com.vluevano.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Proveedor {
    private int idProveedor;
    private String nombreProv;
    private int cpProveedor;
    private int noExtProv;
    private int noIntProv;
    private String rfcProveedor;
    private String municipio;
    private String estado;
    private String calle;
    private String colonia;
    private String ciudad;
    private String pais;
    private String telefonoProv;
    private String correoProv;
    private String curp;
    private boolean esPersonaFisica;
    private List<Categoria> categorias;

    private StringProperty rfcProveedorProperty;
    private StringProperty cpProveedorProperty;
    private StringProperty noExtProvProveedorProperty;
    private StringProperty noIntProvProveedorProperty;
    private StringProperty telefonoProvProveedorProperty;
    private StringProperty correoProvProveedorProperty;

    @SuppressWarnings("unused")
    private String categoriasAsString;

    public Proveedor(int idProveedor, String nombreProv, int cpProveedor, int noExtProv, int noIntProv,
            String rfcProveedor, String municipio, String estado, String calle, String colonia, String ciudad,
            String pais, String telefonoProv, String correoProv, String curp, boolean esPersonaFisica) {
        this.idProveedor = idProveedor;
        this.nombreProv = nombreProv;
        this.cpProveedor = cpProveedor;
        this.noExtProv = noExtProv;
        this.noIntProv = noIntProv;
        this.rfcProveedor = rfcProveedor;
        this.municipio = municipio;
        this.estado = estado;
        this.calle = calle;
        this.colonia = colonia;
        this.ciudad = ciudad;
        this.pais = pais;
        this.telefonoProv = telefonoProv;
        this.correoProv = correoProv;
        this.curp = curp;
        this.esPersonaFisica = esPersonaFisica;
        this.categorias = new ArrayList<>();
        this.rfcProveedorProperty = new SimpleStringProperty(rfcProveedor);
        this.cpProveedorProperty = new SimpleStringProperty(String.valueOf(cpProveedor));
        this.noExtProvProveedorProperty = new SimpleStringProperty(String.valueOf(noExtProv));
        this.noIntProvProveedorProperty = new SimpleStringProperty(String.valueOf(noIntProv));
        this.telefonoProvProveedorProperty = new SimpleStringProperty(telefonoProv);
        this.correoProvProveedorProperty = new SimpleStringProperty(correoProv);
    }

    public Proveedor(int idProveedor, String nombreProv) {
        this.idProveedor = idProveedor;
        this.nombreProv = nombreProv;
    }

    public int getIdProveedor() {
        return idProveedor;
    }

    public void setIdProveedor(int idProveedor) {
        this.idProveedor = idProveedor;
    }

    public String getNombreProv() {
        return nombreProv;
    }

    public void setNombreProv(String nombreProv) {
        this.nombreProv = nombreProv;
    }

    public int getCpProveedor() {
        return cpProveedor;
    }

    public void setCpProveedor(int cpProveedor) {
        this.cpProveedor = cpProveedor;
    }

    public int getNoExtProv() {
        return noExtProv;
    }

    public void setNoExtProv(int noExtProv) {
        this.noExtProv = noExtProv;
    }

    public int getNoIntProv() {
        return noIntProv;
    }

    public void setNoIntProv(int noIntProv) {
        this.noIntProv = noIntProv;
    }

    public String getRfcProveedor() {
        return rfcProveedor;
    }

    public void setRfcProveedor(String rfcProveedor) {
        this.rfcProveedor = rfcProveedor;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getColonia() {
        return colonia;
    }

    public void setColonia(String colonia) {
        this.colonia = colonia;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getTelefonoProv() {
        return telefonoProv;
    }

    public void setTelefonoProv(String telefonoProv) {
        this.telefonoProv = telefonoProv;
    }

    public String getCorreoProv() {
        return correoProv;
    }

    public void setCorreoProv(String correoProv) {
        this.correoProv = correoProv;
    }

    public String getCurp() {
        return curp;
    }

    public void setCurp(String curp) {
        this.curp = curp;
    }

    public boolean isEsPersonaFisica() {
        return esPersonaFisica;
    }

    public void setEsPersonaFisica(boolean esPersonaFisica) {
        this.esPersonaFisica = esPersonaFisica;
    }

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    public StringProperty rfcProveedorProperty() {
        return rfcProveedorProperty; // Método para acceder a la propiedad
    }

    public StringProperty cpProveedorProperty() {
        return cpProveedorProperty; // Método para acceder a la propiedad
    }

    public StringProperty noExtProvProveedorProperty() {
        return noExtProvProveedorProperty; // Método para acceder a la propiedad
    }

    public StringProperty noIntProvProveedorProperty() {
        return noIntProvProveedorProperty; // Método para acceder a la propiedad
    }

    public StringProperty telefonoProvProveedorProperty() {
        return telefonoProvProveedorProperty; // Método para acceder a la propiedad
    }

    public StringProperty correoProvProveedorProperty() {
        return correoProvProveedorProperty; // Método para acceder a la propiedad
    }

    public String getCategoriasAsString() {
        StringBuilder categoriasString = new StringBuilder();
        for (Categoria categoria : categorias) {
            if (categoriasString.length() > 0) {
                categoriasString.append(", "); // Separador entre categorías
            }
            categoriasString.append(categoria.getNombreCategoria()); // Asegúrate de que 'nombreCategoria' esté
                                                                     // correctamente asignado
        }
        return categoriasString.toString();
    }

    public void setCategoriasAsString(String categoriasAsString) {
        this.categoriasAsString = categoriasAsString;
    }

    @Override
    public String toString() {
        return this.nombreProv;
    }

}
