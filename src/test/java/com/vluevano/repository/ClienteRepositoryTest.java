package com.vluevano.repository;

import com.vluevano.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll(); // Limpieza para evitar errores de duplicados

        // Insertamos un cliente completo (Respetando todos los NOT NULL de tu BD)
        Cliente c = new Cliente();
        c.setNombreCliente("Juan Perez S.A.");
        c.setRfcCliente("XAXX010101000");
        c.setTelefonoCliente("9211234567");
        c.setCorreoCliente("juan@email.com");
        c.setCpCliente(96400);
        c.setNoExtCliente(101);
        
        // Dirección obligatoria
        c.setCalle("Av. Zaragoza");
        c.setColonia("Centro");
        c.setCiudad("Coatzacoalcos");
        c.setMunicipio("Coatzacoalcos");
        c.setEstado("Veracruz"); // Campo crítico que fallaba antes
        c.setPais("México");

        clienteRepository.save(c);
    }

    // --- TEST: BUSCAR POR FILTRO ---

    @Test
    @DisplayName("Encuentra por Nombre (case insensitive)")
    void testBuscarPorNombre() {
        List<Cliente> res = clienteRepository.buscarPorFiltro("juan");
        assertFalse(res.isEmpty());
        assertEquals("Juan Perez S.A.", res.get(0).getNombreCliente());
    }

    @Test
    @DisplayName("Encuentra por RFC")
    void testBuscarPorRFC() {
        List<Cliente> res = clienteRepository.buscarPorFiltro("XAXX010101");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("Encuentra por Municipio")
    void testBuscarPorMunicipio() {
        List<Cliente> res = clienteRepository.buscarPorFiltro("coatza");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("Encuentra por Teléfono")
    void testBuscarPorTelefono() {
        List<Cliente> res = clienteRepository.buscarPorFiltro("921123");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("No encuentra resultados con filtro inválido")
    void testBuscarSinResultados() {
        assertTrue(clienteRepository.buscarPorFiltro("Alemania").isEmpty());
    }
}