package com.vluevano.model;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PrestadorServicio {
    private int idPrestador;
    private String nombrePrestador;
    private int cpPrestador;
    private int noExtPrestador;
    private int noIntPrestador;
    private String rfcPrestador;
    private String municipio;
    private String estado;
    private String calle;
    private String colonia;
    private String ciudad;
    private String pais;
    private String telefonoPrestador;
    private String correoPrestador;
    private String curp;
    private boolean esPersonaFisica;
    private List<Servicio> servicios;
    private List<Ruta> rutas;

    private StringProperty nombrePrestadorProveedorProperty;
    private StringProperty municipioProveedorProperty;
    private StringProperty estadoProveedorProperty;

    public PrestadorServicio(int idPrestador, String nombrePrestador, int cpPrestador, int noExtPrestador,
            int noIntPrestador, String rfcPrestador, String municipio, String estado, String calle, String colonia,
            String ciudad, String pais, String telefonoPrestador, String correoPrestador, String curp,
            boolean esPersonaFisica) {
        this.idPrestador = idPrestador;
        this.nombrePrestador = nombrePrestador;
        this.cpPrestador = cpPrestador;
        this.noExtPrestador = noExtPrestador;
        this.noIntPrestador = noIntPrestador;
        this.rfcPrestador = rfcPrestador;
        this.municipio = municipio;
        this.estado = estado;
        this.calle = calle;
        this.colonia = colonia;
        this.ciudad = ciudad;
        this.pais = pais;
        this.telefonoPrestador = telefonoPrestador;
        this.correoPrestador = correoPrestador;
        this.curp = curp;
        this.esPersonaFisica = esPersonaFisica;
        this.servicios = new ArrayList<>();
        this.rutas = new ArrayList<>();
        this.nombrePrestadorProveedorProperty = new SimpleStringProperty();
        this.municipioProveedorProperty = new SimpleStringProperty();
        this.estadoProveedorProperty = new SimpleStringProperty();
    }

    public PrestadorServicio() {
        this.servicios = new ArrayList<>();
        this.rutas = new ArrayList<>();
    }

    public PrestadorServicio(int idPrestador, String nombrePrestador) {
        this.idPrestador = idPrestador;
        this.nombrePrestador = nombrePrestador;
    }

    public int getIdPrestador() {
        return idPrestador;
    }

    public void setIdPrestador(int idPrestador) {
        this.idPrestador = idPrestador;
    }

    public String getNombrePrestador() {
        return nombrePrestador;
    }

    public void setNombrePrestador(String nombrePrestador) {
        this.nombrePrestador = nombrePrestador;
    }

    public int getCpPrestador() {
        return cpPrestador;
    }

    public void setCpPrestador(int cpPrestador) {
        this.cpPrestador = cpPrestador;
    }

    public int getNoExtPrestador() {
        return noExtPrestador;
    }

    public void setNoExtPrestador(int noExtPrestador) {
        this.noExtPrestador = noExtPrestador;
    }

    public int getNoIntPrestador() {
        return noIntPrestador;
    }

    public void setNoIntPrestador(int noIntPrestador) {
        this.noIntPrestador = noIntPrestador;
    }

    public String getRfcPrestador() {
        return rfcPrestador;
    }

    public void setRfcPrestador(String rfcPrestador) {
        this.rfcPrestador = rfcPrestador;
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

    public String getTelefonoPrestador() {
        return telefonoPrestador;
    }

    public void setTelefonoPrestador(String telefonoPrestador) {
        this.telefonoPrestador = telefonoPrestador;
    }

    public String getCorreoPrestador() {
        return correoPrestador;
    }

    public void setCorreoPrestador(String correoPrestador) {
        this.correoPrestador = correoPrestador;
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

    public List<Servicio> getServicios() {
        return servicios;
    }

    public void setServicios(List<Servicio> servicios) {
        this.servicios = servicios;
    }

    public List<Ruta> getRutas() {
        return rutas;
    }

    public void setRutas(List<Ruta> rutas) {
        this.rutas = rutas;
    }

    public StringProperty nombrePrestadorProveedorProperty() {
        return nombrePrestadorProveedorProperty;
    }

    public StringProperty municipioProveedorProperty() {
        return municipioProveedorProperty;
    }

    public StringProperty estadoProveedorProperty() {
        return estadoProveedorProperty;
    }

    public void agregarServicio(Servicio servicio) {
        this.servicios.add(servicio);
    }

    public void agregarRuta(Ruta ruta) {
        this.rutas.add(ruta);
    }

    @Override
    public String toString() {
        return this.nombrePrestador;
    }
}
