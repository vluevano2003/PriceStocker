package com.vluevano.service;

import com.vluevano.model.Categoria;
import com.vluevano.repository.CategoriaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    // --- TEST: CONSULTAR CATEGORIAS ---

    @Test
    @DisplayName("Debe retornar la lista de categorías activas del repositorio")
    void testConsultarCategorias() {
        // Simulamos que la BD tiene 2 categorías activas
        when(categoriaRepository.findAllActivos()).thenReturn(Arrays.asList(new Categoria(), new Categoria()));

        List<Categoria> resultado = categoriaService.consultarCategorias();

        assertEquals(2, resultado.size(), "Debería retornar 2 categorías");
        verify(categoriaRepository, times(1)).findAllActivos(); // Verificamos que llamó a findAllActivos
    }
}