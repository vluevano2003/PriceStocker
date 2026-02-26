package com.vluevano.service;

import com.vluevano.model.Usuario;
import com.vluevano.repository.UsuarioRepository;
import com.vluevano.util.GestorIdioma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private GestorIdioma idioma; 

    public boolean iniciarSesion(String nombreUsuario, String contrasena) {
        return usuarioRepository.findByNombreUsuarioAndContrasenaUsuarioAndActivoTrue(nombreUsuario, contrasena).isPresent();
    }

    /**
     * MODIFICADO: Ahora trae TODOS los usuarios, incluso los inactivos
     */
    public List<Usuario> consultarUsuarios() {
        return usuarioRepository.findAll(); 
    }

    @Transactional
    public String registrarUsuario(Usuario usuario) {
        if (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().isEmpty() || 
            usuario.getContrasenaUsuario() == null || usuario.getContrasenaUsuario().length() < 6) {
            return idioma.get("srv.user.val.invalid_credentials");
        }

        Optional<Usuario> usuarioExistente = usuarioRepository.findByNombreUsuario(usuario.getNombreUsuario());

        if (usuarioExistente.isPresent()) {
            Usuario u = usuarioExistente.get();
            if (u.getActivo()) {
                return idioma.get("srv.user.val.username_exists");
            } else {
                // Si estaba inactivo, lo revive con la nueva contraseña y permiso
                u.setActivo(true);
                u.setContrasenaUsuario(usuario.getContrasenaUsuario());
                u.setPermiso(usuario.isPermiso());
                usuarioRepository.save(u);
                return idioma.get("srv.user.msg.success_register");
            }
        }

        usuarioRepository.save(usuario);
        return idioma.get("srv.user.msg.success_register");
    }

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

    @Transactional
    public boolean eliminarUsuario(int idUsuario) {
        Optional<Usuario> opt = usuarioRepository.findById(idUsuario);
        if (opt.isPresent()) {
            Usuario u = opt.get();
            u.setActivo(false);
            usuarioRepository.save(u);
            return true;
        }
        return false;
    }

    /**
     * NUEVO MÉTODO: Para reactivar un usuario inactivo
     */
    @Transactional
    public boolean reactivarUsuario(int idUsuario) {
        Optional<Usuario> opt = usuarioRepository.findById(idUsuario);
        if (opt.isPresent()) {
            Usuario u = opt.get();
            u.setActivo(true);
            usuarioRepository.save(u);
            return true;
        }
        return false;
    }

    public boolean tienePermiso(String nombreUsuario) {
        return usuarioRepository.findByNombreUsuarioAndActivoTrue(nombreUsuario)
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

    @Transactional
    public String actualizarPerfil(String usuarioActual, String nuevoNombre, String nuevaContrasena) {
        Optional<Usuario> opt = usuarioRepository.findByNombreUsuarioAndActivoTrue(usuarioActual);
        
        if (opt.isPresent()) {
            Usuario u = opt.get();
            boolean huboCambios = false;

            if (nuevoNombre == null || nuevoNombre.trim().isEmpty()) {
                return idioma.get("srv.user.val.empty_username");
            }
            if (!usuarioActual.equals(nuevoNombre)) {
                if (usuarioRepository.existsByNombreUsuario(nuevoNombre)) {
                    return idioma.get("srv.user.val.username_in_use");
                }
                u.setNombreUsuario(nuevoNombre);
                huboCambios = true;
            }
            if (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()) {
                if (nuevaContrasena.length() < 6) {
                    return idioma.get("srv.user.val.pass_length");
                }
                u.setContrasenaUsuario(nuevaContrasena);
                huboCambios = true;
            }

            if (huboCambios) {
                usuarioRepository.save(u);
                return idioma.get("srv.user.msg.success_profile");
            } else {
                return idioma.get("srv.user.msg.no_changes");
            }
        }
        return idioma.get("srv.user.msg.user_not_found");
    }
    
    public Integer obtenerIdPorNombre(String nombre) {
        return usuarioRepository.findByNombreUsuarioAndActivoTrue(nombre)
                .map(Usuario::getIdUsuario)
                .orElse(null);
    }
}