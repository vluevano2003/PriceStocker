package com.vluevano.service;

import com.vluevano.model.Usuario;
import com.vluevano.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * 1. INICIAR SESIÓN
     * @param nombreUsuario
     * @param contrasena
     * @return
     */
    public boolean iniciarSesion(String nombreUsuario, String contrasena) {
        return usuarioRepository.findByNombreUsuarioAndContrasenaUsuario(nombreUsuario, contrasena).isPresent();
    }

    /**
     * 2. CONSULTAR USUARIOS
     * @return
     */
    public List<Usuario> consultarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * 3. REGISTRAR USUARIO
     * @param usuario
     * @return
     */
    @Transactional
    public String registrarUsuario(Usuario usuario) {
        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().isEmpty() || 
            usuario.getContrasenaUsuario() == null || usuario.getContrasenaUsuario().length() < 6) {
            return "Nombre de usuario o contraseña inválidos.";
        }

        if (usuarioRepository.existsByNombreUsuario(usuario.getNombreUsuario())) {
            return "El nombre de usuario ya existe.";
        }

        usuarioRepository.save(usuario);
        return "Usuario registrado exitosamente.";
    }

    /**
     * 4. CAMBIAR CONTRASEÑA
     * @param idUsuario
     * @param nuevaContrasena
     * @return
     */
    @Transactional
    public boolean cambiarContrasena(int idUsuario, String nuevaContrasena) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(idUsuario);
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            usuario.setContrasenaUsuario(nuevaContrasena);
            usuarioRepository.save(usuario);
            return true;
        }
        return false;
    }

    /**
     * 5. ELIMINAR USUARIO
     * @param idUsuario
     * @return
     */
    @Transactional
    public boolean eliminarUsuario(int idUsuario) {
        if (usuarioRepository.existsById(idUsuario)) {
            usuarioRepository.deleteById(idUsuario);
            return true;
        }
        return false;
    }

    /**
     * 6. VERIFICAR PERMISO DE USUARIO
     * @param nombreUsuario
     * @return
     */
    public boolean tienePermiso(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .map(Usuario::isPermiso)
                .orElse(false);
    }
}