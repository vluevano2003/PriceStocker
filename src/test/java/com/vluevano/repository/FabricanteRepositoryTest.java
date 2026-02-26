package com.vluevano.repository;

import com.vluevano.model.Fabricante;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FabricanteRepositoryTest {

    @Autowired
    private FabricanteRepository fabricanteRepository;

    @BeforeEach
    void setUp() {
        fabricanteRepository.deleteAll(); // Limpieza nuclear

        // Insertamos un fabricante "semilla" completo (Respetando NOT NULL)
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Industrias del Norte S.A.");
        f.setRfcFabricante("INO101010XXX");
        f.setTelefonoFabricante("8181234567");
        f.setCorreoFabricante("contacto@industrias.com");
        f.setCpFabricante(64000);
        f.setNoExtFabricante(200);
        
        // Dirección obligatoria
        f.setCalle("Av. Fundidora");
        f.setColonia("Obrera");
        f.setCiudad("Monterrey");
        f.setMunicipio("Monterrey");
        f.setEstado("Nuevo León"); // Campo crítico
        f.setPais("México");

        fabricanteRepository.save(f);
    }

    // --- TEST: BUSCAR POR FILTRO ---

    @Test
    @DisplayName("Encuentra por Nombre (case insensitive)")
    void testBuscarPorNombre() {
        List<Fabricante> res = fabricanteRepository.buscarPorFiltro("industrias");
        assertFalse(res.isEmpty());
        assertEquals("Industrias del Norte S.A.", res.get(0).getNombreFabricante());
    }

    @Test
    @DisplayName("Encuentra por RFC")
    void testBuscarPorRFC() {
        List<Fabricante> res = fabricanteRepository.buscarPorFiltro("INO101010");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("Encuentra por Municipio")
    void testBuscarPorMunicipio() {
        List<Fabricante> res = fabricanteRepository.buscarPorFiltro("monterrey");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("Encuentra por Teléfono")
    void testBuscarPorTelefono() {
        List<Fabricante> res = fabricanteRepository.buscarPorFiltro("818123");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("No encuentra resultados inválidos")
    void testBuscarSinResultados() {
        assertTrue(fabricanteRepository.buscarPorFiltro("China").isEmpty());
    }
}