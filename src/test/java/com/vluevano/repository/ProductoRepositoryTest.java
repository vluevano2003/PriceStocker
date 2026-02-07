package com.vluevano.repository;

import com.vluevano.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProductoRepositoryTest {

    @Autowired
    private ProductoRepository productoRepository;

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();

        // Insertamos un producto con datos suficientes para probar los filtros
        Producto p = new Producto();
        p.setNombreProducto("Taladro Percutor 500W");
        p.setFichaProducto("Herramienta de alto poder para concreto");
        p.setAlternoProducto("TAL-500-PRO");
        p.setExistenciaProducto(10);
        
        productoRepository.save(p);
    }

    // --- TEST: BÚSQUEDA POR FILTRO MULTICAMPO ---

    @Test
    @DisplayName("Encuentra por Nombre")
    void testBuscarPorNombre() {
        List<Producto> res = productoRepository.buscarPorFiltro("Percutor");
        assertFalse(res.isEmpty());
        assertEquals("Taladro Percutor 500W", res.get(0).getNombreProducto());
    }

    @Test
    @DisplayName("Encuentra por Ficha/Descripción")
    void testBuscarPorFicha() {
        List<Producto> res = productoRepository.buscarPorFiltro("concreto");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("Encuentra por Código Alterno")
    void testBuscarPorAlterno() {
        List<Producto> res = productoRepository.buscarPorFiltro("TAL-500");
        assertFalse(res.isEmpty());
    }

    @Test
    @DisplayName("No encuentra resultados sin coincidencia")
    void testBuscarSinResultados() {
        assertTrue(productoRepository.buscarPorFiltro("Sierra Circular").isEmpty());
    }
}