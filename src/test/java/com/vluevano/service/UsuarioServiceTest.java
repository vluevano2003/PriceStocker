package com.vluevano.service;

import com.vluevano.model.Usuario;
import com.vluevano.repository.UsuarioRepository;
import com.vluevano.util.GestorIdioma;
import org.junit.jupiter.api.BeforeEach; 
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations; 
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString; 
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private GestorIdioma idioma; 

    @InjectMocks
    private UsuarioService usuarioService; 

    // --- CONFIGURACIÓN DEL MOCK ANTES DE CADA TEST ---
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        lenient().when(idioma.get(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    // --- TEST: INICIAR SESIÓN ---
    @Test
    void testIniciarSesion_Exito() {
        // Simulamos que el repo encuentra al usuario activo
        when(usuarioRepository.findByNombreUsuarioAndContrasenaUsuarioAndActivoTrue("user", "pass"))
                .thenReturn(Optional.of(new Usuario()));
        assertTrue(usuarioService.iniciarSesion("user", "pass"));
    }

    @Test
    void testIniciarSesion_Fallo() {
        // Simulamos que el repo NO encuentra nada
        when(usuarioRepository.findByNombreUsuarioAndContrasenaUsuarioAndActivoTrue("user", "mal"))
                .thenReturn(Optional.empty());
        assertFalse(usuarioService.iniciarSesion("user", "mal"));
    }

    // --- TEST: REGISTRAR USUARIO ---
    @Test
    @DisplayName("Registro falla si nombre o pass son nulos/vacíos")
    void testRegistrar_DatosInvalidos() {
        Usuario u = new Usuario(); 
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.user.val.invalid_credentials", usuarioService.registrarUsuario(u));
        
        // Verificamos que NUNCA se intente guardar en BD
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro falla si el usuario ya existe y está activo")
    void testRegistrar_Duplicado_Activo() {
        Usuario u = new Usuario();
        u.setNombreUsuario("admin");
        u.setContrasenaUsuario("123456");
        
        Usuario existente = new Usuario();
        existente.setActivo(true);

        // Simulamos que el usuario YA existe en la BD
        when(usuarioRepository.findByNombreUsuario("admin")).thenReturn(Optional.of(existente));
        
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.user.val.username_exists", usuarioService.registrarUsuario(u));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Registro exitoso")
    void testRegistrar_Exito() {
        Usuario u = new Usuario();
        u.setNombreUsuario("nuevo");
        u.setContrasenaUsuario("123456");
        
        // Simulamos que NO existe
        when(usuarioRepository.findByNombreUsuario("nuevo")).thenReturn(Optional.empty());
        
        // AHORA ESPERA LA LLAVE DE ÉXITO
        assertEquals("srv.user.msg.success_register", usuarioService.registrarUsuario(u));
        verify(usuarioRepository).save(u); // Confirmamos que se llamó al método guardar
    }

    // --- TEST: CAMBIAR CONTRASEÑA ---
    @Test
    void testCambiarContrasena_UsuarioExiste() {
        Usuario u = new Usuario();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(u));
        
        assertTrue(usuarioService.cambiarContrasena(1, "nuevaPass"));
        assertEquals("nuevaPass", u.getContrasenaUsuario()); // Verificamos cambio en objeto
        verify(usuarioRepository).save(u);
    }

    @Test
    void testCambiarContrasena_UsuarioNoExiste() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());
        assertFalse(usuarioService.cambiarContrasena(99, "pass"));
        verify(usuarioRepository, never()).save(any());
    }

    // --- TEST: ELIMINAR USUARIO ---
    @Test
    void testEliminar_Existe() {
        Usuario u = new Usuario();
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(u));
        assertTrue(usuarioService.eliminarUsuario(1));
        verify(usuarioRepository).save(u); // Ahora es un soft delete
    }

    // --- TEST: PERMISOS ---
    @Test
    void testTienePermiso() {
        Usuario admin = new Usuario(); admin.setPermiso(true);
        Usuario normal = new Usuario(); normal.setPermiso(false);

        // Configuramos respuestas mock para diferentes usuarios
        when(usuarioRepository.findByNombreUsuarioAndActivoTrue("admin")).thenReturn(Optional.of(admin));
        when(usuarioRepository.findByNombreUsuarioAndActivoTrue("user")).thenReturn(Optional.of(normal));
        
        assertTrue(usuarioService.tienePermiso("admin"));
        assertFalse(usuarioService.tienePermiso("user"));
    }

    // --- TEST: ACTUALIZAR PERFIL ---
    @Test
    @DisplayName("Perfil: Falla si usuario actual no existe (error sesión)")
    void testActualizarPerfil_UsuarioNoEncontrado() {
        when(usuarioRepository.findByNombreUsuarioAndActivoTrue("viejo")).thenReturn(Optional.empty());
        String res = usuarioService.actualizarPerfil("viejo", "nuevo", "pass");
        
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.user.msg.user_not_found", res);
    }

    @Test
    @DisplayName("Perfil: Falla si intenta cambiar a un nombre que YA existe")
    void testActualizarPerfil_NombreOcupado() {
        Usuario u = new Usuario();
        u.setNombreUsuario("yo");
        when(usuarioRepository.findByNombreUsuarioAndActivoTrue("yo")).thenReturn(Optional.of(u));
        
        // Simulamos choque de nombre
        when(usuarioRepository.existsByNombreUsuario("otro")).thenReturn(true);

        String res = usuarioService.actualizarPerfil("yo", "otro", null);
        
        // AHORA ESPERA LA LLAVE DEL PROPERTIES
        assertEquals("srv.user.val.username_in_use", res);
    }

    @Test
    @DisplayName("Perfil: Actualiza todo correctamente")
    void testActualizarPerfil_Exito() {
        Usuario u = new Usuario();
        u.setNombreUsuario("antiguo");
        
        when(usuarioRepository.findByNombreUsuarioAndActivoTrue("antiguo")).thenReturn(Optional.of(u));
        when(usuarioRepository.existsByNombreUsuario("nuevo")).thenReturn(false); // Nombre libre
        
        String res = usuarioService.actualizarPerfil("antiguo", "nuevo", "newPass123");
        
        // AHORA ESPERA LA LLAVE DE ÉXITO
        assertEquals("srv.user.msg.success_profile", res);
        assertEquals("nuevo", u.getNombreUsuario()); 
        verify(usuarioRepository).save(u);
    }
}