package com.vluevano.service;

import com.vluevano.model.*;
import com.vluevano.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock private ProductoRepository productoRepository;
    @Mock private CategoriaRepository categoriaRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private FabricanteRepository fabricanteRepository;
    @Mock private EmpresaRepository empresaRepository;
    @Mock private ServicioRepository servicioRepository;

    @InjectMocks
    private ProductoService productoService;

    // --- TEST: CONSULTAS ---

    @Test
    void testConsultarProductos() {
        when(productoRepository.findAll()).thenReturn(new ArrayList<>());
        productoService.consultarProductos();
        verify(productoRepository).findAll();
    }

    @Test
    void testBuscarProductos_Filtro() {
        productoService.buscarProductos("Taladro");
        verify(productoRepository).buscarPorFiltro("Taladro");
    }

    @Test
    void testObtenerProductoCompleto() {
        Producto p = new Producto();
        p.setIdProducto(1);
        when(productoRepository.findById(1)).thenReturn(Optional.of(p));

        Producto resultado = productoService.obtenerProductoCompleto(1);
        assertNotNull(resultado);
        // Hibernate.initialize no hace nada en objetos POJO simples en tests unitarios,
        // así que solo verificamos que retornó el objeto.
    }

    // --- TEST: GUARDAR (VALIDACIONES Y LÓGICA) ---

    @Test
    @DisplayName("Falla si el nombre es nulo o vacío")
    void testGuardar_NombreInvalido() {
        Producto p = new Producto();
        assertEquals("El nombre del producto es obligatorio.", productoService.guardarProducto(p));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guarda un producto simple sin relaciones")
    void testGuardar_Simple_Exito() {
        Producto p = new Producto();
        p.setNombreProducto("Martillo");

        String res = productoService.guardarProducto(p);
        
        assertEquals("Producto guardado exitosamente.", res);
        verify(productoRepository).save(p);
    }

    @Test
    @DisplayName("Guarda un producto con Categorías existentes")
    void testGuardar_ConCategorias() {
        Producto p = new Producto();
        p.setNombreProducto("Cemento");

        // Simulamos una categoría seleccionada en la UI
        Categoria cat = new Categoria();
        cat.setIdCategoria(5);
        
        List<Categoria> categorias = new ArrayList<>();
        categorias.add(cat);
        p.setCategorias(categorias);

        // Simulamos que el repo de categorías devuelve la referencia
        when(categoriaRepository.getReferenceById(5)).thenReturn(cat);

        String res = productoService.guardarProducto(p);

        assertEquals("Producto guardado exitosamente.", res);
        verify(categoriaRepository).getReferenceById(5); // Verificamos que buscó la categoría
        verify(productoRepository).save(p);
    }

    @Test
    @DisplayName("Manejo de excepción al guardar")
    void testGuardar_ErrorBD() {
        Producto p = new Producto();
        p.setNombreProducto("Error Product");
        
        doThrow(new RuntimeException("Error SQL")).when(productoRepository).save(any());
        
        String res = productoService.guardarProducto(p);
        assertTrue(res.contains("Error al guardar"));
    }

    // --- TEST: ELIMINAR ---

    @Test
    void testEliminar_Exito() {
        Producto p = new Producto();
        assertTrue(productoService.eliminarProducto(p));
        verify(productoRepository).delete(p);
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(productoRepository).delete(any());
        assertFalse(productoService.eliminarProducto(new Producto()));
    }
}