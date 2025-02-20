package com.vluevano.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Empresa {
    private int idEmpresa;
    private String nombreEmpresa;
    private int cpEmpresa;
    private int noExtEmpresa;
    private int noIntEmpresa;
    private String rfcEmpresa;
    private String municipio;
    private String estado;
    private String calle;
    private String colonia;
    private String ciudad;
    private String pais;
    private String telefonoEmpresa;
    private String correoEmpresa;
    private String curp;
    private boolean esPersonaFisica;
    private List<Categoria> categorias;

    private StringProperty rfcEmpresaProperty;
    private StringProperty cpEmpresaProperty;
    private StringProperty noExtEmpresaProperty;
    private StringProperty noIntEmpresaProperty;
    private StringProperty telefonoEmpresaProperty;
    private StringProperty correoEmpresaProperty;

    @SuppressWarnings("unused")
    private String categoriasAsString;

    public Empresa(int idEmpresa, String nombreEmpresa, int cpEmpresa, int noExtEmpresa, int noIntEmpresa,
            String rfcEmpresa, String municipio, String estado, String calle, String colonia, String ciudad,
            String pais, String telefonoEmpresa, String correoEmpresa, String curp, boolean esPersonaFisica) {
        this.idEmpresa = idEmpresa;
        this.nombreEmpresa = nombreEmpresa;
        this.cpEmpresa = cpEmpresa;
        this.noExtEmpresa = noExtEmpresa;
        this.noIntEmpresa = noIntEmpresa;
        this.rfcEmpresa = rfcEmpresa;
        this.municipio = municipio;
        this.estado = estado;
        this.calle = calle;
        this.colonia = colonia;
        this.ciudad = ciudad;
        this.pais = pais;
        this.telefonoEmpresa = telefonoEmpresa;
        this.correoEmpresa = correoEmpresa;
        this.curp = curp;
        this.esPersonaFisica = esPersonaFisica;
        this.categorias = new ArrayList<>();
        this.rfcEmpresaProperty = new SimpleStringProperty(rfcEmpresa);
        this.cpEmpresaProperty = new SimpleStringProperty(String.valueOf(cpEmpresa));
        this.noExtEmpresaProperty = new SimpleStringProperty(String.valueOf(noExtEmpresa));
        this.noIntEmpresaProperty = new SimpleStringProperty(String.valueOf(noIntEmpresa));
        this.telefonoEmpresaProperty = new SimpleStringProperty(telefonoEmpresa);
        this.correoEmpresaProperty = new SimpleStringProperty(correoEmpresa);
    }

    public Empresa(int idEmpresa, String nombreEmpresa) {
        this.idEmpresa = idEmpresa;
        this.nombreEmpresa = nombreEmpresa;
    }

    public int getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(int idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public int getCpEmpresa() {
        return cpEmpresa;
    }

    public void setCpEmpresa(int cpEmpresa) {
        this.cpEmpresa = cpEmpresa;
    }

    public int getNoExtEmpresa() {
        return noExtEmpresa;
    }

    public void setNoExtEmpresa(int noExtEmpresa) {
        this.noExtEmpresa = noExtEmpresa;
    }

    public int getNoIntEmpresa() {
        return noIntEmpresa;
    }

    public void setNoIntEmpresa(int noIntEmpresa) {
        this.noIntEmpresa = noIntEmpresa;
    }

    public String getRfcEmpresa() {
        return rfcEmpresa;
    }

    public void setRfcEmpresa(String rfcEmpresa) {
        this.rfcEmpresa = rfcEmpresa;
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

    public String getTelefonoEmpresa() {
        return telefonoEmpresa;
    }

    public void setTelefonoEmpresa(String telefonoEmpresa) {
        this.telefonoEmpresa = telefonoEmpresa;
    }

    public String getCorreoEmpresa() {
        return correoEmpresa;
    }

    public void setCorreoEmpresa(String correoEmpresa) {
        this.correoEmpresa = correoEmpresa;
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

    public StringProperty rfcEmpresaProperty() {
        return rfcEmpresaProperty; // Método para acceder a la propiedad
    }

    public StringProperty cpEmpresaProperty() {
        return cpEmpresaProperty; // Método para acceder a la propiedad
    }

    public StringProperty noExtEmpresaProperty() {
        return noExtEmpresaProperty; // Método para acceder a la propiedad
    }

    public StringProperty noIntEmpresaProperty() {
        return noIntEmpresaProperty; // Método para acceder a la propiedad
    }

    public StringProperty telefonoEmpresaProperty() {
        return telefonoEmpresaProperty; // Método para acceder a la propiedad
    }

    public StringProperty correoEmpresaProperty() {
        return correoEmpresaProperty; // Método para acceder a la propiedad
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
        return this.nombreEmpresa;
    }
}
