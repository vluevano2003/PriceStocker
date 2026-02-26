package com.vluevano.service;

import com.vluevano.model.PrestadorServicio;
import com.vluevano.repository.PrestadorServicioRepository;
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
class PrestadorServicioServiceTest {

    @Mock
    private PrestadorServicioRepository repository;

    @Mock
    private GestorIdioma idioma;

    @InjectMocks
    private PrestadorServicioService service;

    // --- CONFIGURACIÓN DEL MOCK ANTES DE CADA TEST ---
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Simulamos que el gestor devuelve la misma llave que se le pide
        lenient().when(idioma.get(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- TEST: CONSULTAS ---
    @Test
    void testConsultarPrestadores() {
        when(repository.findAllActivos()).thenReturn(new ArrayList<>());
        service.consultarPrestadores();
        verify(repository).findAllActivos();
    }

    @Test
    void testBuscarPrestadores_FiltroVacio() {
        service.buscarPrestadores("");
        verify(repository).findAllActivos(); // Si es vacío, llama a findAllActivos
    }

    @Test
    void testBuscarPrestadores_ConFiltro() {
        service.buscarPrestadores("Servicio");
        verify(repository).buscarPorFiltro("Servicio");
    }

    // --- TEST: GUARDAR ---
    @Test
    @DisplayName("Falla si Nombre es nulo/vacío")
    void testGuardar_NombreInvalido() {
        PrestadorServicio p = new PrestadorServicio();
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.provider.val.name", service.guardarPrestador(p));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Guarda exitosamente")
    void testGuardar_Exito() {
        PrestadorServicio p = new PrestadorServicio();
        p.setNombrePrestador("Prestador OK");
        
        String res = service.guardarPrestador(p);
        // AHORA ESPERA LA LLAVE DE ÉXITO
        assertEquals("srv.provider.msg.success", res);
        verify(repository).save(p);
    }

    @Test
    @DisplayName("Manejo de Excepción BD al guardar")
    void testGuardar_ErrorBD() {
        PrestadorServicio p = new PrestadorServicio();
        p.setNombrePrestador("Prestador Error");
        
        doThrow(new RuntimeException("Error SQL")).when(repository).save(any());
        
        String res = service.guardarPrestador(p);
        // VERIFICA QUE CONTENGA LA LLAVE DE ERROR
        assertTrue(res.contains("srv.provider.msg.error"));
    }

    // --- TEST: ELIMINAR ---
    @Test
    void testEliminar_Exito() {
        PrestadorServicio p = new PrestadorServicio();
        assertTrue(service.eliminarPrestador(p));
        verify(repository).save(p); // Ahora es un soft delete
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(repository).save(any());
        assertFalse(service.eliminarPrestador(new PrestadorServicio()));
    }
}