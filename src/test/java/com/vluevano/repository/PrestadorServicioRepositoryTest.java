package com.vluevano.repository;

import com.vluevano.model.PrestadorServicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PrestadorServicioRepositoryTest {

    @Autowired
    private PrestadorServicioRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll(); // Limpieza para evitar errores de duplicados

        // Insertamos un prestador completo (Respetando todos los NOT NULL)
        PrestadorServicio p = new PrestadorServicio();
        p.setNombrePrestador("Servicios Rápidos S.A.");
        p.setRfcPrestador("SRA101010XXX");
        p.setTelefonoPrestador("5512345678");
        p.setCorreoPrestador("contacto@servicios.com");
        p.setCpPrestador(11000);
        p.setNoExtPrestador(55);
        
        // Dirección obligatoria
        p.setCalle("Av. Reforma");
        p.setColonia("Centro");
        p.setCiudad("CDMX");
        p.setMunicipio("Cuauhtémoc");
        p.setEstado("Ciudad de México"); // Campo crítico
        p.setPais("México");

        repository.save(p);
    }

    // --- TEST: BUSCAR POR FILTRO (Con JOIN FETCH) ---

    @Test
    @DisplayName("Encuentra por Nombre")
    void testBuscarPorNombre() {
        List<PrestadorServicio> res = repository.buscarPorFiltro("Rápidos");
        assertFalse(res.isEmpty());
        assertEquals("Servicios Rápidos S.A.", res.get(0).getNombrePrestador());
    }

    @Test
    @DisplayName("Encuentra por RFC")
    void testBuscarPorRFC() {
        List<PrestadorServicio> res = repository.buscarPorFiltro("SRA101010");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("Encuentra por Municipio")
    void testBuscarPorMunicipio() {
        List<PrestadorServicio> res = repository.buscarPorFiltro("Cuauhtémoc");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("Encuentra por Teléfono")
    void testBuscarPorTelefono() {
        List<PrestadorServicio> res = repository.buscarPorFiltro("551234");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("No encuentra resultados sin coincidencia")
    void testBuscarSinResultados() {
        assertTrue(repository.buscarPorFiltro("Inexistente").isEmpty());
    }
}