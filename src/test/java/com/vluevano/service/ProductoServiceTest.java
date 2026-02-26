package com.vluevano.service;

import com.vluevano.model.*;
import com.vluevano.repository.*;
import com.vluevano.util.GestorIdioma;
import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString; 
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

    @Mock private GestorIdioma idioma;

    @InjectMocks
    private ProductoService productoService;

    // --- CONFIGURACIÓN DEL MOCK ANTES DE CADA TEST ---
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(idioma.get(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- TEST: CONSULTAS ---

    @Test
    void testConsultarProductos() {
        when(productoRepository.findAllActivos()).thenReturn(new ArrayList<>());
        productoService.consultarProductos();
        verify(productoRepository).findAllActivos();
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
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.product.val.name", productoService.guardarProducto(p));
        verify(productoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guarda un producto simple sin relaciones")
    void testGuardar_Simple_Exito() {
        Producto p = new Producto();
        p.setNombreProducto("Martillo");

        String res = productoService.guardarProducto(p);
        
        // AHORA ESPERA LA LLAVE DE ÉXITO
        assertEquals("srv.product.msg.success", res);
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

        // AHORA ESPERA LA LLAVE DE ÉXITO
        assertEquals("srv.product.msg.success", res);
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
        // VERIFICA QUE CONTENGA LA LLAVE DE ERROR
        assertTrue(res.contains("srv.product.msg.error"));
    }

    // --- TEST: ELIMINAR ---

    @Test
    void testEliminar_Exito() {
        Producto p = new Producto();
        assertTrue(productoService.eliminarProducto(p));
        verify(productoRepository).save(p); // Ahora es un soft delete
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(productoRepository).save(any());
        assertFalse(productoService.eliminarProducto(new Producto()));
    }
}