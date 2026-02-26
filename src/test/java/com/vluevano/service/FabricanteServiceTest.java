package com.vluevano.service;

import com.vluevano.model.Fabricante;
import com.vluevano.repository.FabricanteRepository;
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
class FabricanteServiceTest {

    @Mock
    private FabricanteRepository fabricanteRepository;

    @Mock
    private GestorIdioma idioma;

    @InjectMocks
    private FabricanteService fabricanteService;

    // --- CONFIGURACIÓN DEL MOCK ANTES DE CADA TEST ---
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Simulamos que el gestor devuelve la misma llave que se le pide
        lenient().when(idioma.get(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- TEST: CONSULTAS ---
    @Test
    void testConsultarFabricantes() {
        when(fabricanteRepository.findAllActivos()).thenReturn(new ArrayList<>());
        fabricanteService.consultarFabricantes();
        verify(fabricanteRepository).findAllActivos();
    }

    @Test
    void testBuscarFabricantes_FiltroVacio() {
        fabricanteService.buscarFabricantes("");
        verify(fabricanteRepository).findAllActivos();
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
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.manufacturer.val.name", fabricanteService.guardarFabricante(f));
        verify(fabricanteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si RFC tiene formato incorrecto")
    void testGuardar_RFCInvalido() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica X");
        
        f.setRfcFabricante("ABC"); // Corto
        assertEquals("srv.manufacturer.val.rfc", fabricanteService.guardarFabricante(f));
        
        f.setRfcFabricante("ABCDEFGHIJKLM123"); // Largo
        assertEquals("srv.manufacturer.val.rfc", fabricanteService.guardarFabricante(f));
    }

    @Test
    @DisplayName("Falla si Teléfono no son 10 dígitos")
    void testGuardar_TelefonoInvalido() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica X");
        f.setRfcFabricante("XAXX010101000"); // RFC ok

        f.setTelefonoFabricante("123"); // Corto
        assertEquals("srv.manufacturer.val.phone", fabricanteService.guardarFabricante(f));
        
        f.setTelefonoFabricante("ABCDEFGHIJ"); // Letras
        assertEquals("srv.manufacturer.val.phone", fabricanteService.guardarFabricante(f));
    }

    @Test
    @DisplayName("Falla si Correo es inválido")
    void testGuardar_CorreoInvalido() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica X");
        f.setTelefonoFabricante("1234567890");

        f.setCorreoFabricante("correo.com"); // Falta @
        assertEquals("srv.manufacturer.val.email", fabricanteService.guardarFabricante(f));
    }

    @Test
    @DisplayName("Falla si CP no tiene 5 dígitos")
    void testGuardar_CPInvalido() {
        Fabricante f = new Fabricante();
        f.setNombreFabricante("Fabrica X");
        f.setTelefonoFabricante("1234567890");
        f.setCorreoFabricante("correo@mail.com");

        f.setCpFabricante(6400); // 4 dígitos
        assertEquals("srv.manufacturer.val.cp", fabricanteService.guardarFabricante(f));
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
        // AHORA ESPERA LA LLAVE DE ÉXITO
        assertEquals("srv.manufacturer.msg.success", res);
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
        // VERIFICA QUE CONTENGA LA LLAVE DE ERROR
        assertTrue(res.contains("srv.manufacturer.msg.error"));
    }

    // --- TEST: ELIMINAR ---
    @Test
    void testEliminar_Exito() {
        Fabricante f = new Fabricante();
        assertTrue(fabricanteService.eliminarFabricante(f));
        verify(fabricanteRepository).save(f); // Ahora es un soft delete
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(fabricanteRepository).save(any());
        assertFalse(fabricanteService.eliminarFabricante(new Fabricante()));
    }
}