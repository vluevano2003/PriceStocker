/**package com.vluevano.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Fabricante {
    private int idFabricante;
    private String nombreFabricante;
    private int cpFabricante;
    private int noExtFabricante;
    private int noIntFabricante;
    private String rfcFabricante;
    private String municipio;
    private String estado;
    private String calle;
    private String colonia;
    private String ciudad;
    private String pais;
    private String telefonoFabricante;
    private String correoFabricante;
    private String curp;
    private boolean esPersonaFisica;
    private List<Categoria> categorias;

    private StringProperty rfcFabricanteProperty;
    private StringProperty cpFabricanteProperty;
    private StringProperty noExtFabricanteProperty;
    private StringProperty noIntFabricanteProperty;
    private StringProperty telefonoFabricanteProperty;
    private StringProperty correoFabricanteProperty;

    @SuppressWarnings("unused")
    private String categoriasAsString;

    public Fabricante(int idFabricante, String nombreFabricante, int cpFabricante, int noExtFabricante, int noIntFabricante,
            String rfcFabricante, String municipio, String estado, String calle, String colonia, String ciudad,
            String pais, String telefonoFabricante, String correoFabricante, String curp, boolean esPersonaFisica) {
        this.idFabricante = idFabricante;
        this.nombreFabricante = nombreFabricante;
        this.cpFabricante = cpFabricante;
        this.noExtFabricante = noExtFabricante;
        this.noIntFabricante = noIntFabricante;
        this.rfcFabricante = rfcFabricante;
        this.municipio = municipio;
        this.estado = estado;
        this.calle = calle;
        this.colonia = colonia;
        this.ciudad = ciudad;
        this.pais = pais;
        this.telefonoFabricante = telefonoFabricante;
        this.correoFabricante = correoFabricante;
        this.curp = curp;
        this.esPersonaFisica = esPersonaFisica;
        this.categorias = new ArrayList<>();
        this.rfcFabricanteProperty = new SimpleStringProperty(rfcFabricante);
        this.cpFabricanteProperty = new SimpleStringProperty(String.valueOf(cpFabricante));
        this.noExtFabricanteProperty = new SimpleStringProperty(String.valueOf(noExtFabricante));
        this.noIntFabricanteProperty = new SimpleStringProperty(String.valueOf(noIntFabricante));
        this.telefonoFabricanteProperty = new SimpleStringProperty(telefonoFabricante);
        this.correoFabricanteProperty = new SimpleStringProperty(correoFabricante);
    }

    public Fabricante(int idFabricante, String nombreFabricante) {
        this.idFabricante = idFabricante;
        this.nombreFabricante = nombreFabricante;
    }

    public int getIdFabricante() {
        return idFabricante;
    }

    public void setIdFabricante(int idFabricante) {
        this.idFabricante = idFabricante;
    }

    public String getNombreFabricante() {
        return nombreFabricante;
    }

    public void setNombreFabricante(String nombreFabricante) {
        this.nombreFabricante = nombreFabricante;
    }

    public int getCpFabricante() {
        return cpFabricante;
    }

    public void setCpFabricante(int cpFabricante) {
        this.cpFabricante = cpFabricante;
    }

    public int getNoExtFabricante() {
        return noExtFabricante;
    }

    public void setNoExtFabricante(int noExtFabricante) {
        this.noExtFabricante = noExtFabricante;
    }

    public int getNoIntFabricante() {
        return noIntFabricante;
    }

    public void setNoIntFabricante(int noIntFabricante) {
        this.noIntFabricante = noIntFabricante;
    }

    public String getRfcFabricante() {
        return rfcFabricante;
    }

    public void setRfcFabricante(String rfcFabricante) {
        this.rfcFabricante = rfcFabricante;
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

    public String getTelefonoFabricante() {
        return telefonoFabricante;
    }

    public void setTelefonoFabricante(String telefonoFabricante) {
        this.telefonoFabricante = telefonoFabricante;
    }

    public String getCorreoFabricante() {
        return correoFabricante;
    }

    public void setCorreoFabricante(String correoFabricante) {
        this.correoFabricante = correoFabricante;
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

    public StringProperty rfcFabricanteProperty() {
        return rfcFabricanteProperty; // Método para acceder a la propiedad
    }

    public StringProperty cpFabricanteProperty() {
        return cpFabricanteProperty; // Método para acceder a la propiedad
    }

    public StringProperty noExtFabricanteProperty() {
        return noExtFabricanteProperty; // Método para acceder a la propiedad
    }

    public StringProperty noIntFabricanteProperty() {
        return noIntFabricanteProperty; // Método para acceder a la propiedad
    }

    public StringProperty telefonoFabricanteProperty() {
        return telefonoFabricanteProperty; // Método para acceder a la propiedad
    }

    public StringProperty correoFabricanteProperty() {
        return correoFabricanteProperty; // Método para acceder a la propiedad
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
        return this.nombreFabricante;
    }
}*/
