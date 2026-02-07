package com.vluevano.repository;

import com.vluevano.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest // Configura H2 en memoria automáticamente
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioPrueba;

    @BeforeEach
    void setUp() {
        // Limpiamos la BD antes de cada test para evitar choques con datos de Flyway u otros tests
        usuarioRepository.deleteAll(); 
        
        // Creamos un usuario controlado para las pruebas
        usuarioPrueba = new Usuario();
        usuarioPrueba.setNombreUsuario("usuario_test_unico");
        usuarioPrueba.setContrasenaUsuario("123456");
        usuarioPrueba.setPermiso(true);  
        usuarioRepository.save(usuarioPrueba);
    }

    // --- TEST: Búsqueda por credenciales ---
    @Test
    @DisplayName("Debe encontrar usuario si nombre y contraseña coinciden")
    void testCredencialesCorrectas() {
        // Buscamos exactamente al usuario creado en setUp
        Optional<Usuario> encontrado = usuarioRepository.findByNombreUsuarioAndContrasenaUsuario("usuario_test_unico", "123456");
        
        assertTrue(encontrado.isPresent(), "El usuario debería ser encontrado");
        assertEquals("usuario_test_unico", encontrado.get().getNombreUsuario());
    }

    @Test
    @DisplayName("No debe encontrar usuario si la contraseña está mal")
    void testContrasenaIncorrecta() {
        // Usuario correcto, password incorrecto
        Optional<Usuario> encontrado = usuarioRepository.findByNombreUsuarioAndContrasenaUsuario("usuario_test_unico", "incorrecta");
        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("No debe encontrar usuario si el nombre no existe")
    void testUsuarioNoExistente() {
        Optional<Usuario> encontrado = usuarioRepository.findByNombreUsuarioAndContrasenaUsuario("fantasma", "123456");
        assertFalse(encontrado.isPresent());
    }

    // --- TEST: Verificación de existencia ---
    @Test
    void testExistePorNombre_True() {
        boolean existe = usuarioRepository.existsByNombreUsuario("usuario_test_unico");
        assertTrue(existe);
    }

    @Test
    void testExistePorNombre_False() {
        boolean existe = usuarioRepository.existsByNombreUsuario("nadie");
        assertFalse(existe);
    }

    // --- TEST: Búsqueda simple ---
    @Test
    void testBuscarPorNombre() {
        Optional<Usuario> u = usuarioRepository.findByNombreUsuario("usuario_test_unico");
        assertTrue(u.isPresent());
        assertEquals(usuarioPrueba.getIdUsuario(), u.get().getIdUsuario());
    }
}