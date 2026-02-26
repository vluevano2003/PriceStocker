package com.vluevano.repository;

import com.vluevano.model.Empresa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmpresaRepositoryTest {

    @Autowired
    private EmpresaRepository empresaRepository;

    @BeforeEach
    void setUp() {
        empresaRepository.deleteAll(); // Limpieza nuclear

        // Insertamos una empresa válida llenando TODOS los campos obligatorios de tu BD
        Empresa e = new Empresa();
        e.setNombreEmpresa("Tech Solutions S.A. de C.V.");
        e.setRfcEmpresa("TSO101010XXX");
        e.setTelefonoEmpresa("9211234567");
        e.setCorreoEmpresa("contacto@tech.com");
        e.setCpEmpresa(96400);
        e.setNoExtEmpresa(50);
        
        // Dirección obligatoria
        e.setCalle("Av. Tecnológico");
        e.setColonia("Centro");
        e.setCiudad("Minatitlán");
        e.setMunicipio("Minatitlán");
        e.setEstado("Veracruz");
        e.setPais("México");

        empresaRepository.save(e);
    }

    // --- TEST: BUSCAR POR FILTRO ---

    @Test
    @DisplayName("Encuentra por Nombre (case insensitive)")
    void testBuscarPorNombre() {
        List<Empresa> res = empresaRepository.buscarPorFiltro("tech solutions");
        assertFalse(res.isEmpty());
        assertEquals("Tech Solutions S.A. de C.V.", res.get(0).getNombreEmpresa());
    }

    @Test
    @DisplayName("Encuentra por RFC")
    void testBuscarPorRFC() {
        List<Empresa> res = empresaRepository.buscarPorFiltro("TSO101010");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("Encuentra por Municipio")
    void testBuscarPorMunicipio() {
        List<Empresa> res = empresaRepository.buscarPorFiltro("Minatitlán");
        assertFalse(res.isEmpty(), "Debería encontrar el municipio Minatitlán");
    }

    @Test
    @DisplayName("Encuentra por Teléfono")
    void testBuscarPorTelefono() {
        List<Empresa> res = empresaRepository.buscarPorFiltro("921123");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("No encuentra resultados con filtro sin coincidencia")
    void testBuscarSinResultados() {
        assertTrue(empresaRepository.buscarPorFiltro("Microsoft").isEmpty());
    }
}