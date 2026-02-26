package com.vluevano.service;

import com.vluevano.model.Proveedor;
import com.vluevano.repository.ProveedorRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString; 
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @Mock
    private GestorIdioma idioma;

    @InjectMocks
    private ProveedorService proveedorService;

    // --- CONFIGURACIÓN DEL MOCK ANTES DE CADA TEST ---
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(idioma.get(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- TEST: CONSULTAR ---
    @Test
    void testConsultarProveedores() {
        when(proveedorRepository.findAllActivos()).thenReturn(new ArrayList<>());
        proveedorService.consultarProveedores();
        verify(proveedorRepository).findAllActivos();
    }

    // --- TEST: BUSCAR (FILTRO) ---
    @Test
    @DisplayName("Filtro vacío devuelve todos los activos")
    void testBuscar_FiltroVacio() {
        proveedorService.buscarProveedores("");
        verify(proveedorRepository).findAllActivos(); // Llama a findAllActivos, no al filtro
    }

    @Test
    @DisplayName("Con filtro usa búsqueda específica")
    void testBuscar_ConFiltro() {
        proveedorService.buscarProveedores("cemento");
        verify(proveedorRepository).buscarPorFiltro("cemento");
    }

    // --- TEST: GUARDAR ---
    @Test
    @DisplayName("Falla si nombre es vacío")
    void testGuardar_NombreInvalido() {
        Proveedor p = new Proveedor();
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.supplier.val.name", proveedorService.guardarProveedor(p));
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guarda correctamente")
    void testGuardar_Exito() {
        Proveedor p = new Proveedor();
        p.setNombreProv("Valido");
        
        String res = proveedorService.guardarProveedor(p);
        // AHORA ESPERA LA LLAVE DE ÉXITO
        assertEquals("srv.supplier.msg.success", res);
        verify(proveedorRepository).save(p);
    }

    @Test
    @DisplayName("Maneja error de BD")
    void testGuardar_ErrorBD() {
        Proveedor p = new Proveedor();
        p.setNombreProv("Valido");
        // Simulamos excepción
        doThrow(new RuntimeException("Error BD")).when(proveedorRepository).save(any());
        
        String res = proveedorService.guardarProveedor(p);
        // VERIFICA QUE CONTENGA LA LLAVE DE ERROR
        assertTrue(res.contains("srv.supplier.msg.error"));
    }

    // --- TEST: ELIMINAR ---
    @Test
    void testEliminar_Exito() {
        Proveedor p = new Proveedor();
        assertTrue(proveedorService.eliminarProveedor(p));
        verify(proveedorRepository).save(p); // Ahora es un soft delete
    }
}