package com.vluevano.service;

import com.vluevano.model.Empresa;
import com.vluevano.repository.EmpresaRepository;
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
class EmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private EmpresaService empresaService;

    // --- TEST: CONSULTAS ---

    @Test
    void testConsultarEmpresas() {
        when(empresaRepository.findAll()).thenReturn(new ArrayList<>());
        empresaService.consultarEmpresas();
        verify(empresaRepository).findAll();
    }

    @Test
    void testBuscarEmpresas_FiltroVacio() {
        empresaService.buscarEmpresas("");
        verify(empresaRepository).findAll();
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
        assertEquals("El nombre de la empresa es obligatorio.", empresaService.guardarEmpresa(e));
        
        e.setNombreEmpresa("   ");
        assertEquals("El nombre de la empresa es obligatorio.", empresaService.guardarEmpresa(e));
        
        verify(empresaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si RFC tiene longitud incorrecta (<12 o >13)")
    void testGuardar_RFCInvalido() {
        Empresa e = new Empresa();
        e.setNombreEmpresa("Empresa X");

        e.setRfcEmpresa("ABC"); // Corto (3)
        assertEquals("El RFC debe tener 12 o 13 caracteres.", empresaService.guardarEmpresa(e));

        e.setRfcEmpresa("ABCDEFGHIJKLMN"); // Largo (14)
        assertEquals("El RFC debe tener 12 o 13 caracteres.", empresaService.guardarEmpresa(e));
    }

    @Test
    @DisplayName("Guarda exitosamente con datos válidos")
    void testGuardar_Exito() {
        Empresa e = new Empresa();
        e.setNombreEmpresa("Empresa Correcta");
        e.setRfcEmpresa("ABC101010XYZ"); // 12 chars (Válido Persona Moral)
        
        String res = empresaService.guardarEmpresa(e);
        assertEquals("Empresa guardada exitosamente.", res);
        verify(empresaRepository).save(e);
    }

    @Test
    @DisplayName("Manejo de Excepción BD al guardar")
    void testGuardar_ErrorBD() {
        Empresa e = new Empresa();
        e.setNombreEmpresa("Empresa");
        
        doThrow(new RuntimeException("Error SQL")).when(empresaRepository).save(any());
        
        String res = empresaService.guardarEmpresa(e);
        assertTrue(res.contains("Error al guardar"));
    }

    // --- TEST: ELIMINAR ---

    @Test
    void testEliminar_Exito() {
        Empresa e = new Empresa();
        assertTrue(empresaService.eliminarEmpresa(e));
        verify(empresaRepository).delete(e);
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(empresaRepository).delete(any());
        assertFalse(empresaService.eliminarEmpresa(new Empresa()));
    }
}