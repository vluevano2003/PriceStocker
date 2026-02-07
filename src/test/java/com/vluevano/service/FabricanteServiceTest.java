package com.vluevano.service;

import com.vluevano.model.Fabricante;
import com.vluevano.repository.FabricanteRepository;
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
class FabricanteServiceTest {

    @Mock
    private FabricanteRepository fabricanteRepository;

    @InjectMocks
    private FabricanteService fabricanteService;

    // --- TEST: CONSULTAS ---

    @Test
    void testConsultarFabricantes() {
        when(fabricanteRepository.findAll()).thenReturn(new ArrayList<>());
        fabricanteService.consultarFabricantes();
        verify(fabricanteRepository).findAll();
    }

    @Test
    void testBuscarFabricantes_FiltroVacio() {
        fabricanteService.buscarFabricantes("");
        verify(fabricanteRepository).findAll();
    }

    @Test
    void testBuscarFabricantes_ConFiltro() {
        fabricanteService.buscarFabricantes("Industrias");
        verify(fabricanteRepository).buscarPorFiltro("Industrias");
    }

    // --- TEST: GUARDAR (VALIDACIONES) ---

    @Test
    @DisplayName("Falla si Nombre es nulo/vacío")
    void testGuardar_NombreInvalido() {
        Fabricante f = new Fabricante();
        assertEquals("El nombre del fabricante es obligatorio.", fabricanteService.guardarFabricante(f));
        verify(fabricanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si RFC tiene formato incorrecto")
    void testGuardar_RFCInvalido() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica X");
        
        f.setRfcFabricante("ABC"); // Corto
        assertEquals("El RFC debe contener 12 o 13 caracteres alfanuméricos.", fabricanteService.guardarFabricante(f));
        
        f.setRfcFabricante("ABCDEFGHIJKLM123"); // Largo
        assertEquals("El RFC debe contener 12 o 13 caracteres alfanuméricos.", fabricanteService.guardarFabricante(f));
    }

    @Test
    @DisplayName("Falla si Teléfono no son 10 dígitos")
    void testGuardar_TelefonoInvalido() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica X");
        f.setRfcFabricante("XAXX010101000"); // RFC ok

        f.setTelefonoFabricante("123"); // Corto
        assertEquals("El teléfono debe tener exactamente 10 dígitos.", fabricanteService.guardarFabricante(f));
        
        f.setTelefonoFabricante("ABCDEFGHIJ"); // Letras
        assertEquals("El teléfono debe tener exactamente 10 dígitos.", fabricanteService.guardarFabricante(f));
    }

    @Test
    @DisplayName("Falla si Correo es inválido")
    void testGuardar_CorreoInvalido() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica X");
        f.setTelefonoFabricante("1234567890");

        f.setCorreoFabricante("correo.com"); // Falta @
        assertEquals("El correo electrónico no es válido.", fabricanteService.guardarFabricante(f));
    }

    @Test
    @DisplayName("Falla si CP no tiene 5 dígitos")
    void testGuardar_CPInvalido() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica X");
        f.setTelefonoFabricante("1234567890");
        f.setCorreoFabricante("correo@mail.com");

        f.setCpFabricante(6400); // 4 dígitos
        assertEquals("El código postal debe tener 5 dígitos.", fabricanteService.guardarFabricante(f));
    }

    @Test
    @DisplayName("Guarda exitosamente con datos válidos")
    void testGuardar_Exito() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica Correcta");
        f.setRfcFabricante("XAXX010101000");
        f.setTelefonoFabricante("9211234567");
        f.setCorreoFabricante("fabrica@mail.com");
        f.setCpFabricante(64000); // 5 dígitos

        String res = fabricanteService.guardarFabricante(f);
        assertEquals("Fabricante guardado exitosamente.", res);
        verify(fabricanteRepository).save(f);
    }

    @Test
    @DisplayName("Manejo de Excepción BD al guardar")
    void testGuardar_ErrorBD() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica");
        f.setCpFabricante(64000);
        
        doThrow(new RuntimeException("Error SQL")).when(fabricanteRepository).save(any());
        
        String res = fabricanteService.guardarFabricante(f);
        assertTrue(res.contains("Error al guardar"));
    }

    // --- TEST: ELIMINAR ---

    @Test
    void testEliminar_Exito() {
        Fabricante f = new Fabricante();
        assertTrue(fabricanteService.eliminarFabricante(f));
        verify(fabricanteRepository).delete(f);
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(fabricanteRepository).delete(any());
        assertFalse(fabricanteService.eliminarFabricante(new Fabricante()));
    }
}