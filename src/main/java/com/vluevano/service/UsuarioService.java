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
     * INICIAR SESIÓN
     * @param nombreUsuario
     * @param contrasena
     * @return
     */
    public boolean iniciarSesion(String nombreUsuario, String contrasena) {
        return usuarioRepository.findByNombreUsuarioAndContrasenaUsuario(nombreUsuario, contrasena).isPresent();
    }

    /**
     * CONSULTAR USUARIOS
     * @return
     */
    public List<Usuario> consultarUsuarios() {
        return usuarioRepository.findAll();
    }

    /**
     * REGISTRAR USUARIO
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
     * CAMBIAR CONTRASEÑA
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
     * ELIMINAR USUARIO
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
     * VERIFICAR PERMISO DE USUARIO
     * @param nombreUsuario
     * @return
     */
    public boolean tienePermiso(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuario(nombreUsuario)
                .map(Usuario::isPermiso)
                .orElse(false);
    }

    @Transactional
    public boolean actualizarPermiso(int idUsuario, boolean nuevoPermiso) {
        Optional<Usuario> opt = usuarioRepository.findById(idUsuario);
        if (opt.isPresent()) {
            Usuario u = opt.get();
            u.setPermiso(nuevoPermiso);
            usuarioRepository.save(u);
            return true;
        }
        return false;
    }

    /**
     * ACTUALIZAR PERFIL
     * @param usuarioActual
     * @param nuevoNombre
     * @param nuevaContrasena
     * @return
     */
    @Transactional
    public String actualizarPerfil(String usuarioActual, String nuevoNombre, String nuevaContrasena) {
        Optional<Usuario> opt = usuarioRepository.findByNombreUsuario(usuarioActual);
        
        if (opt.isPresent()) {
            Usuario u = opt.get();
            boolean huboCambios = false;

            if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
                return "El nombre de usuario no puede estar vacío.";
            }
            if (!usuarioActual.equals(nuevoNombre)) {
                if (usuarioRepository.existsByNombreUsuario(nuevoNombre)) {
                    return "El nombre de usuario ya está en uso por otra persona.";
                }
                u.setNombreUsuario(nuevoNombre);
                huboCambios = true;
            }
            if (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()) {
                if (nuevaContrasena.length() < 6) {
                    return "La nueva contraseña debe tener al menos 6 caracteres.";
                }
                u.setContrasenaUsuario(nuevaContrasena);
                huboCambios = true;
            }

            if (huboCambios) {
                usuarioRepository.save(u);
                return "Perfil actualizado exitosamente.";
            } else {
                return "No se detectaron cambios para guardar.";
            }
        }
        return "Usuario no encontrado (Error de sesión).";
    }
    

    /**
     * OBTENER ID DE USUARIO POR NOMBRE
     * @param nombre
     * @return
     */
    public Integer obtenerIdPorNombre(String nombre) {
        return usuarioRepository.findByNombreUsuario(nombre).map(Usuario::getIdUsuario).orElse(null);
    }
}