package com.vluevano.service;

import com.vluevano.model.Cliente;
import com.vluevano.repository.ClienteRepository;
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
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private GestorIdioma idioma;

    @InjectMocks
    private ClienteService clienteService;

    // --- CONFIGURACIÓN DEL MOCK ANTES DE CADA TEST ---
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(idioma.get(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- TEST: CONSULTAS ---
    @Test
    void testConsultarClientes() {
        when(clienteRepository.findAllActivos()).thenReturn(new ArrayList<>());
        clienteService.consultarClientes();
        verify(clienteRepository).findAllActivos();
    }

    @Test
    void testBuscarClientes_FiltroVacio() {
        clienteService.buscarClientes("");
        verify(clienteRepository).findAllActivos(); // Si es vacío, trae todos los activos
    }

    @Test
    void testBuscarClientes_ConFiltro() {
        clienteService.buscarClientes("Juan");
        verify(clienteRepository).buscarPorFiltro("Juan");
    }

    // --- TEST: GUARDAR (VALIDACIONES) ---
    @Test
    @DisplayName("Falla si Nombre es nulo/vacío")
    void testGuardar_NombreInvalido() {
        Cliente c = new Cliente();
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.client.val.name", clienteService.guardarCliente(c));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si RFC tiene formato incorrecto")
    void testGuardar_RFCInvalido() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan");
        
        c.setRfcCliente("ABC"); // Muy corto
        assertEquals("srv.client.val.rfc", clienteService.guardarCliente(c));
        
        c.setRfcCliente("ABCDEFGHIJKLM123"); // Muy largo
        assertEquals("srv.client.val.rfc", clienteService.guardarCliente(c));
    }

    @Test
    @DisplayName("Falla si Teléfono no son 10 dígitos")
    void testGuardar_TelefonoInvalido() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan");
        c.setRfcCliente("XAXX010101000"); // RFC válido para pasar el filtro anterior

        c.setTelefonoCliente("123"); // Corto
        assertEquals("srv.client.val.phone", clienteService.guardarCliente(c));
        
        c.setTelefonoCliente("12345678901"); // Largo (11)
        assertEquals("srv.client.val.phone", clienteService.guardarCliente(c));
        
        c.setTelefonoCliente("ABCDEFGHIJ"); // Letras
        assertEquals("srv.client.val.phone", clienteService.guardarCliente(c));
    }

    @Test
    @DisplayName("Falla si Correo es inválido")
    void testGuardar_CorreoInvalido() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan");
        c.setTelefonoCliente("1234567890"); // Valido

        c.setCorreoCliente("juan.com"); // Falta @
        assertEquals("srv.client.val.email", clienteService.guardarCliente(c));
    }

    @Test
    @DisplayName("Falla si CP no tiene 5 dígitos")
    void testGuardar_CPInvalido() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan");
        c.setTelefonoCliente("1234567890");
        c.setCorreoCliente("juan@mail.com");

        c.setCpCliente(9640); // 4 dígitos
        assertEquals("srv.client.val.cp", clienteService.guardarCliente(c));
    }

    @Test
    @DisplayName("Guarda exitosamente con datos válidos")
    void testGuardar_Exito() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan Correcto");
        c.setRfcCliente("XAXX010101000");
        c.setTelefonoCliente("9211234567");
        c.setCorreoCliente("juan@mail.com");
        c.setCpCliente(96400); // 5 dígitos

        String res = clienteService.guardarCliente(c);
        // AHORA ESPERA LA LLAVE DE ÉXITO
        assertEquals("srv.client.msg.success", res);
        verify(clienteRepository).save(c);
    }

    @Test
    @DisplayName("Manejo de Excepción BD")
    void testGuardar_ErrorBD() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan");
        c.setCpCliente(96400);
        
        doThrow(new RuntimeException("Error SQL")).when(clienteRepository).save(any());
        
        String res = clienteService.guardarCliente(c);
        // VERIFICA QUE CONTENGA LA LLAVE DE ERROR
        assertTrue(res.contains("srv.client.msg.error"));
    }

    // --- TEST: ELIMINAR ---
    @Test
    void testEliminar_Exito() {
        Cliente c = new Cliente();
        assertTrue(clienteService.eliminarCliente(c));
        verify(clienteRepository).save(c); // Ahora se hace un soft delete (save)
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(clienteRepository).save(any());
        assertFalse(clienteService.eliminarCliente(new Cliente()));
    }
}