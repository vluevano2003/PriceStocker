package com.vluevano.service;

import com.vluevano.model.Cliente;
import com.vluevano.repository.ClienteRepository;
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
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    // --- TEST: CONSULTAS ---

    @Test
    void testConsultarClientes() {
        when(clienteRepository.findAll()).thenReturn(new ArrayList<>());
        clienteService.consultarClientes();
        verify(clienteRepository).findAll();
    }

    @Test
    void testBuscarClientes_FiltroVacio() {
        clienteService.buscarClientes("");
        verify(clienteRepository).findAll(); // Si es vacío, trae todos
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
        assertEquals("El nombre del cliente es obligatorio.", clienteService.guardarCliente(c));
        verify(clienteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Falla si RFC tiene formato incorrecto")
    void testGuardar_RFCInvalido() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan");
        
        c.setRfcCliente("ABC"); // Muy corto
        assertEquals("El RFC debe contener 12 o 13 caracteres alfanuméricos.", clienteService.guardarCliente(c));
        
        c.setRfcCliente("ABCDEFGHIJKLM123"); // Muy largo
        assertEquals("El RFC debe contener 12 o 13 caracteres alfanuméricos.", clienteService.guardarCliente(c));
    }

    @Test
    @DisplayName("Falla si Teléfono no son 10 dígitos")
    void testGuardar_TelefonoInvalido() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan");
        c.setRfcCliente("XAXX010101000"); // RFC válido para pasar el filtro anterior

        c.setTelefonoCliente("123"); // Corto
        assertEquals("El teléfono debe tener exactamente 10 dígitos.", clienteService.guardarCliente(c));
        
        c.setTelefonoCliente("12345678901"); // Largo (11)
        assertEquals("El teléfono debe tener exactamente 10 dígitos.", clienteService.guardarCliente(c));
        
        c.setTelefonoCliente("ABCDEFGHIJ"); // Letras
        assertEquals("El teléfono debe tener exactamente 10 dígitos.", clienteService.guardarCliente(c));
    }

    @Test
    @DisplayName("Falla si Correo es inválido")
    void testGuardar_CorreoInvalido() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan");
        c.setTelefonoCliente("1234567890"); // Valido

        c.setCorreoCliente("juan.com"); // Falta @
        assertEquals("El correo electrónico no es válido.", clienteService.guardarCliente(c));
    }

    @Test
    @DisplayName("Falla si CP no tiene 5 dígitos")
    void testGuardar_CPInvalido() {
        Cliente c = new Cliente();
        c.setNombreCliente("Juan");
        c.setTelefonoCliente("1234567890");
        c.setCorreoCliente("juan@mail.com");

        c.setCpCliente(9640); // 4 dígitos
        assertEquals("El código postal debe tener 5 dígitos.", clienteService.guardarCliente(c));
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
        assertEquals("Cliente guardado exitosamente.", res);
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
        assertTrue(res.contains("Error al guardar"));
    }

    // --- TEST: ELIMINAR ---

    @Test
    void testEliminar_Exito() {
        Cliente c = new Cliente();
        assertTrue(clienteService.eliminarCliente(c));
        verify(clienteRepository).delete(c);
    }

    @Test
    void testEliminar_Fallo() {
        doThrow(new RuntimeException()).when(clienteRepository).delete(any());
        assertFalse(clienteService.eliminarCliente(new Cliente()));
    }
}