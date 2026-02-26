package com.vluevano.service;

import com.vluevano.model.Empresa;
import com.vluevano.repository.EmpresaRepository;
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
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private GestorIdioma idioma; 

    @InjectMocks
    private EmpresaService empresaService;

    // --- CONFIGURACIÓN DEL MOCK ANTES DE CADA TEST ---
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Simulamos que el gestor devuelve la misma llave que se le pide
        lenient().when(idioma.get(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- TEST: CONSULTAS ---
    @Test
    void testConsultarEmpresas() {
        when(empresaRepository.findAllActivos()).thenReturn(new ArrayList<>());
        empresaService.consultarEmpresas();
        verify(empresaRepository).findAllActivos();
    }

    @Test
    void testBuscarEmpresas_FiltroVacio() {
        empresaService.buscarEmpresas("");
        verify(empresaRepository).findAllActivos();
    }

    @Test
    void testBuscarEmpresas_ConFiltro() {
        empresaService.buscarEmpresas("Tech");
        verify(empresaRepository).buscarPorFiltro("Tech");
    }

    // --- TEST: GUARDAR (VALIDACIONES) ---
    @Test
    @DisplayName("Falla si Nombre es nulo/vacío")
    void testGuardar_NombreInvalido() {
        Empresa e = new Empresa();
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.company.val.name", empresaService.guardarEmpresa(e));
        
        e.setNombreEmpresa("   ");
        assertEquals("srv.company.val.name", empresaService.guardarEmpresa(e));
        
        verify(empresaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si RFC tiene longitud incorrecta (<12 o >13)")
    void testGuardar_RFCInvalido() {
        Empresa e = new Empresa();
        e.setNombreEmpresa("Empresa X");

        e.setRfcEmpresa("ABC"); // Corto (3)
        assertEquals("srv.company.val.rfc", empresaService.guardarEmpresa(e));

        e.setRfcEmpresa("ABCDEFGHIJKLMN"); // Largo (14)
        assertEquals("srv.company.val.rfc", empresaService.guardarEmpresa(e));
    }

    @Test
    @DisplayName("Guarda exitosamente con datos válidos")
    void testGuardar_Exito() {
        Empresa e = new Empresa();
        e.setNombreEmpresa("Empresa Correcta");
        e.setRfcEmpresa("ABC101010XYZ"); // 12 chars (Válido Persona Moral)
        
        String res = empresaService.guardarEmpresa(e);
        // AHORA ESPERA LA LLAVE DE ÉXITO
        assertEquals("srv.company.msg.success", res);
        verify(empresaRepository).save(e);
    }

    @Test
    @DisplayName("Manejo de Excepción BD al guardar")
    void testGuardar_ErrorBD() {
        Empresa e = new Empresa();
        e.setNombreEmpresa("Empresa");
        
        doThrow(new RuntimeException("Error SQL")).when(empresaRepository).save(any());
        
        String res = empresaService.guardarEmpresa(e);
        // VERIFICA QUE CONTENGA LA LLAVE DE ERROR
        assertTrue(res.contains("srv.company.msg.error"));
    }

    // --- TEST: ELIMINAR ---
    @Test
    void testEliminar_Exito() {
        Empresa e = new Empresa();
        assertTrue(empresaService.eliminarEmpresa(e));
        verify(empresaRepository).save(e); // Ahora es un soft delete
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(empresaRepository).save(any());
        assertFalse(empresaService.eliminarEmpresa(new Empresa()));
    }
}