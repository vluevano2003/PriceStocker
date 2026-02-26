package com.vluevano.service;

import com.vluevano.model.Proveedor;
import com.vluevano.repository.ProveedorRepository;
import com.vluevano.util.GestorIdioma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private GestorIdioma idioma;

    /**
     * Consulta todos los proveedores
     * @return
     */
    public List<Proveedor> consultarProveedores() {
        return proveedorRepository.findAllActivos();
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
            return idioma.get("srv.supplier.val.name");
        }
        
        try {
            proveedorRepository.save(proveedor);
            return idioma.get("srv.supplier.msg.success");
        } catch (Exception e) {
            e.printStackTrace();
            return idioma.get("srv.supplier.msg.error") + " " + e.getMessage();
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
            proveedor.setActivo(false);
            proveedorRepository.save(proveedor);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}