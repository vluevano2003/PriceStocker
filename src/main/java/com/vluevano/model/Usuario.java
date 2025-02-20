package com.vluevano.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Usuario {
    private int idUsuario;
    private String nombreUsuario;
    private String contrasenaUsuario;
    private boolean permiso;

    private IntegerProperty idUsuarioProperty;
    private StringProperty nombreUsuarioProperty;
    private StringProperty contrasenaUsuarioProperty;
    private BooleanProperty permisoProperty;

    // Constructor
    public Usuario(int idUsuario, String nombreUsuario, String contrasenaUsuario, boolean permiso) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.contrasenaUsuario = contrasenaUsuario;
        this.permiso = permiso;
        this.idUsuarioProperty = new SimpleIntegerProperty(idUsuario);
        this.nombreUsuarioProperty = new SimpleStringProperty(nombreUsuario);
        this.contrasenaUsuarioProperty = new SimpleStringProperty(contrasenaUsuario);
        this.permisoProperty = new SimpleBooleanProperty(permiso);
    }

    // Getters y Setters
    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getContrasenaUsuario() {
        return contrasenaUsuario;
    }

    public void setContrasenaUsuario(String contrasenaUsuario) {
        this.contrasenaUsuario = contrasenaUsuario;
    }

    public boolean isPermiso() {
        return permiso;
    }

    public void setPermiso(boolean permiso) {
        this.permiso = permiso;
    }

    public IntegerProperty getIdUsuarioProperty() {
        return idUsuarioProperty;
    }

    public void setIdUsuarioProperty(IntegerProperty idUsuarioProperty) {
        this.idUsuarioProperty = idUsuarioProperty;
    }

    public StringProperty getNombreUsuarioProperty() {
        return nombreUsuarioProperty;
    }

    public void setNombreUsuarioProperty(StringProperty nombreUsuarioProperty) {
        this.nombreUsuarioProperty = nombreUsuarioProperty;
    }

    public StringProperty getContrasenaUsuarioProperty() {
        return contrasenaUsuarioProperty;
    }

    public void setContrasenaUsuarioProperty(StringProperty contrasenaUsuarioProperty) {
        this.contrasenaUsuarioProperty = contrasenaUsuarioProperty;
    }

    public BooleanProperty getPermisoProperty() {
        return permisoProperty;
    }

    public void setPermisoProperty(BooleanProperty permisoProperty) {
        this.permisoProperty = permisoProperty;
    }

    public IntegerProperty idUsuarioProperty() {
        return idUsuarioProperty;
    }

    public StringProperty nombreUsuarioProperty() {
        return nombreUsuarioProperty;
    }

    public StringProperty contrasenaUsuarioProperty() {
        return contrasenaUsuarioProperty;
    }

    public BooleanProperty permisoProperty() {
        return permisoProperty;
    }
}
