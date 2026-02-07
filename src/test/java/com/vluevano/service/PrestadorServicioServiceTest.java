package com.vluevano.service;

import com.vluevano.model.PrestadorServicio;
import com.vluevano.repository.PrestadorServicioRepository;
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
class PrestadorServicioServiceTest {

    @Mock
    private PrestadorServicioRepository repository;

    @InjectMocks
    private PrestadorServicioService service;

    // --- TEST: CONSULTAS ---

    @Test
    void testConsultarPrestadores() {
        when(repository.findAll()).thenReturn(new ArrayList<>());
        service.consultarPrestadores();
        verify(repository).findAll();
    }

    @Test
    void testBuscarPrestadores_FiltroVacio() {
        service.buscarPrestadores("");
        verify(repository).findAll(); // Si es vacío, llama a findAll
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
        assertEquals("El nombre del prestador es obligatorio.", service.guardarPrestador(p));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Guarda exitosamente")
    void testGuardar_Exito() {
        PrestadorServicio p = new PrestadorServicio();
        p.setNombrePrestador("Prestador OK");
        
        String res = service.guardarPrestador(p);
        assertEquals("Prestador guardado exitosamente.", res);
        verify(repository).save(p);
    }

    @Test
    @DisplayName("Manejo de Excepción BD al guardar")
    void testGuardar_ErrorBD() {
        PrestadorServicio p = new PrestadorServicio();
        p.setNombrePrestador("Prestador Error");
        
        doThrow(new RuntimeException("Error SQL")).when(repository).save(any());
        
        String res = service.guardarPrestador(p);
        assertTrue(res.contains("Error al guardar"));
    }

    // --- TEST: ELIMINAR ---

    @Test
    void testEliminar_Exito() {
        PrestadorServicio p = new PrestadorServicio();
        assertTrue(service.eliminarPrestador(p));
        verify(repository).delete(p);
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(repository).delete(any());
        assertFalse(service.eliminarPrestador(new PrestadorServicio()));
    }
}