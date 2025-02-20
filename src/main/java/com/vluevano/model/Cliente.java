package com.vluevano.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Cliente {
    private int idCliente;
    private String nombreCliente;
    private String nombreFiscal;
    private int cpCliente;
    private int noExtCliente;
    private int noIntCliente;
    private String rfcCliente;
    private String municipio;
    private String estado;
    private String calle;
    private String colonia;
    private String ciudad;
    private String pais;
    private String telefonoCliente;
    private String correoCliente;
    private String curp;
    private boolean esPersonaFisica;
    private List<Categoria> categorias;

    private StringProperty rfcClienteProperty;
    private StringProperty cpClienteProperty;
    private StringProperty noExtClienteProperty;
    private StringProperty noIntClienteProperty;
    private StringProperty telefonoClienteProperty;
    private StringProperty correoClienteProperty;

    @SuppressWarnings("unused")
    private String categoriasAsString;

    public Cliente(int idCliente, String nombreCliente, String nombreFiscal, int cpCliente, int noExtCliente,
            int noIntCliente, String rfcCliente, String municipio, String estado, String calle,
            String colonia, String ciudad, String pais, String telefonoCliente, String correoCliente,
            String curp, boolean esPersonaFisica) {
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.nombreFiscal = nombreFiscal;
        this.cpCliente = cpCliente;
        this.noExtCliente = noExtCliente;
        this.noIntCliente = noIntCliente;
        this.rfcCliente = rfcCliente;
        this.municipio = municipio;
        this.estado = estado;
        this.calle = calle;
        this.colonia = colonia;
        this.ciudad = ciudad;
        this.pais = pais;
        this.telefonoCliente = telefonoCliente;
        this.correoCliente = correoCliente;
        this.curp = curp;
        this.esPersonaFisica = esPersonaFisica;
        this.categorias = new ArrayList<>();
        this.rfcClienteProperty = new SimpleStringProperty(rfcCliente);
        this.cpClienteProperty = new SimpleStringProperty(String.valueOf(cpCliente));
        this.noExtClienteProperty = new SimpleStringProperty(String.valueOf(noExtCliente));
        this.noIntClienteProperty = new SimpleStringProperty(String.valueOf(noIntCliente));
        this.telefonoClienteProperty = new SimpleStringProperty(telefonoCliente);
        this.correoClienteProperty = new SimpleStringProperty(correoCliente);
    }

    public Cliente(int idCliente, String nombreCliente) {
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public String getNombreFiscal() {
        return nombreFiscal;
    }

    public void setNombreFiscal(String nombreFiscal) {
        this.nombreFiscal = nombreFiscal;
    }

    public int getCpCliente() {
        return cpCliente;
    }

    public void setCpCliente(int cpCliente) {
        this.cpCliente = cpCliente;
    }

    public int getNoExtCliente() {
        return noExtCliente;
    }

    public void setNoExtCliente(int noExtCliente) {
        this.noExtCliente = noExtCliente;
    }

    public int getNoIntCliente() {
        return noIntCliente;
    }

    public void setNoIntCliente(int noIntCliente) {
        this.noIntCliente = noIntCliente;
    }

    public String getRfcCliente() {
        return rfcCliente;
    }

    public void setRfcCliente(String rfcCliente) {
        this.rfcCliente = rfcCliente;
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

    public String getTelefonoCliente() {
        return telefonoCliente;
    }

    public void setTelefonoCliente(String telefonoCliente) {
        this.telefonoCliente = telefonoCliente;
    }

    public String getCorreoCliente() {
        return correoCliente;
    }

    public void setCorreoCliente(String correoCliente) {
        this.correoCliente = correoCliente;
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

    public StringProperty rfcClienteProperty() {
        return rfcClienteProperty; // Método para acceder a la propiedad
    }

    public StringProperty cpClienteProperty() {
        return cpClienteProperty; // Método para acceder a la propiedad
    }

    public StringProperty noExtClienteProperty() {
        return noExtClienteProperty; // Método para acceder a la propiedad
    }

    public StringProperty noIntClienteProperty() {
        return noIntClienteProperty; // Método para acceder a la propiedad
    }

    public StringProperty telefonoClienteProperty() {
        return telefonoClienteProperty; // Método para acceder a la propiedad
    }

    public StringProperty correoClienteProperty() {
        return correoClienteProperty; // Método para acceder a la propiedad
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
        return this.nombreCliente;
    }

}
