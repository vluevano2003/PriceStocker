package com.vluevano.service;

import com.vluevano.model.Proveedor;
import com.vluevano.repository.ProveedorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    // --- TEST: CONSULTAR ---
    @Test
    void testConsultarProveedores() {
        when(proveedorRepository.findAll()).thenReturn(new ArrayList<>());
        proveedorService.consultarProveedores();
        verify(proveedorRepository).findAll();
    }

    // --- TEST: BUSCAR (FILTRO) ---
    @Test
    @DisplayName("Filtro vacío devuelve todos")
    void testBuscar_FiltroVacio() {
        proveedorService.buscarProveedores("");
        verify(proveedorRepository).findAll(); // Llama a findAll, no al filtro
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
        assertEquals("El nombre del proveedor es obligatorio.", proveedorService.guardarProveedor(p));
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    @DisplayName("Guarda correctamente")
    void testGuardar_Exito() {
        Proveedor p = new Proveedor();
        p.setNombreProv("Valido");
        
        String res = proveedorService.guardarProveedor(p);
        assertEquals("Proveedor guardado exitosamente.", res);
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
        assertTrue(res.contains("Error al guardar"));
    }

    // --- TEST: ELIMINAR ---
    @Test
    void testEliminar_Exito() {
        Proveedor p = new Proveedor();
        assertTrue(proveedorService.eliminarProveedor(p));
        verify(proveedorRepository).delete(p);
    }
}