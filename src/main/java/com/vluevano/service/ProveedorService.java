package com.vluevano.service;

import com.vluevano.model.Proveedor;
import com.vluevano.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    /**
     * Consulta todos los proveedores
     * @return
     */
    public List<Proveedor> consultarProveedores() {
        return proveedorRepository.findAll();
    }

    /**
     * Busca proveedores por un filtro en nombre, RFC, municipio o teléfono
     * @param filtro
     * @return
     */
    public List<Proveedor> buscarProveedores(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return consultarProveedores();
        }
        return proveedorRepository.buscarPorFiltro(filtro);
    }

    /**
     * Guarda un proveedor con validaciones básicas
     * @param proveedor
     * @return
     */
    @Transactional
    public String guardarProveedor(Proveedor proveedor) {
        // Validaciones básicas
        if (proveedor.getNombreProv() == null || proveedor.getNombreProv().trim().isEmpty()) {
            return "El nombre del proveedor es obligatorio.";
        }
        
        try {
            proveedorRepository.save(proveedor);
            return "Proveedor guardado exitosamente.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al guardar: " + e.getMessage();
        }
    }

    /**
     * Elimina un proveedor
     * @param proveedor
     * @return
     */
    @Transactional
    public boolean eliminarProveedor(Proveedor proveedor) {
        try {
            proveedorRepository.delete(proveedor);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}