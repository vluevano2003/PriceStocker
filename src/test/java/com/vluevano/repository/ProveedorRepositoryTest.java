package com.vluevano.repository;

import com.vluevano.model.Proveedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProveedorRepositoryTest {

    @Autowired
    private ProveedorRepository proveedorRepository;

    @BeforeEach
    void setUp() {
        proveedorRepository.deleteAll(); // Limpieza nuclear

        // Creamos un proveedor
        Proveedor p = new Proveedor();
        p.setNombreProv("Materiales del Sur S.A.");
        p.setRfcProveedor("MSU101010XXX");
        p.setTelefonoProv("9211234567");
        p.setCorreoProv("contacto@materiales.com");
        
        // Dirección Completa
        p.setCalle("Av. Independencia");
        p.setNoExtProv(100); 
        p.setColonia("Centro");
        p.setCiudad("Coatzacoalcos");
        p.setMunicipio("Coatzacoalcos");
        p.setEstado("Veracruz");
        p.setPais("México");
        p.setCpProveedor(96400); 

        proveedorRepository.save(p);
    }

    // TEST: BÚSQUEDAS
    @Test
    @DisplayName("Debe encontrar por Nombre (ignorando mayúsculas)")
    void testBuscarPorNombre() {
        List<Proveedor> resultados = proveedorRepository.buscarPorFiltro("materiales");
        assertFalse(resultados.isEmpty(), "Debería encontrar resultados");
        assertEquals("Materiales del Sur S.A.", resultados.get(0).getNombreProv());
    }

    @Test
    @DisplayName("Debe encontrar por RFC")
    void testBuscarPorRFC() {
        List<Proveedor> resultados = proveedorRepository.buscarPorFiltro("MSU101010");
        assertFalse(resultados.isEmpty());
    }

    @Test
    @DisplayName("Debe encontrar por Municipio")
    void testBuscarPorMunicipio() {
        List<Proveedor> resultados = proveedorRepository.buscarPorFiltro("Coatza");
        assertFalse(resultados.isEmpty());
    }

    @Test
    @DisplayName("Debe encontrar por Teléfono")
    void testBuscarPorTelefono() {
        List<Proveedor> resultados = proveedorRepository.buscarPorFiltro("921123");
        assertFalse(resultados.isEmpty());
    }

    @Test
    @DisplayName("No debe encontrar nada si el filtro no coincide")
    void testBuscarSinResultados() {
        List<Proveedor> resultados = proveedorRepository.buscarPorFiltro("Norte");
        assertTrue(resultados.isEmpty());
    }
}