package com.vluevano.repository;

import com.vluevano.model.Categoria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CategoriaRepositoryTest {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository; 

    @BeforeEach
    void setUp() {
        productoRepository.deleteAll();
        
        categoriaRepository.deleteAll(); 
        categoriaRepository.flush(); 

        Categoria c = new Categoria();
        c.setNombreCategoria("Materiales de Construcción");
        c.setDescripcionCategoria("Cementos, cal, arena");
        categoriaRepository.save(c);
    }
    
    @Test
    @DisplayName("Debe encontrar la categoría por su nombre exacto")
    void testBuscarPorNombre_Existe() {
        Optional<Categoria> encontrada = categoriaRepository.findByNombreCategoriaAndActivoTrue("Materiales de Construcción");
        assertTrue(encontrada.isPresent(), "Debería encontrar la categoría");
        assertEquals("Materiales de Construcción", encontrada.get().getNombreCategoria());
    }

    @Test
    @DisplayName("Devuelve vacío si el nombre no existe")
    void testBuscarPorNombre_NoExiste() {
        Optional<Categoria> encontrada = categoriaRepository.findByNombreCategoriaAndActivoTrue("Electrónica");
        assertFalse(encontrada.isPresent(), "No debería encontrar categorías inexistentes");
    }
}