package com.vluevano.model;

public class Precio {
    private double monto;
    private String moneda;
    
    public Precio(double monto, String moneda) {
        this.monto = monto;
        this.moneda = moneda;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }
   
    
    // Métodos: convertirMoneda, agregarHistorial, etc.
}