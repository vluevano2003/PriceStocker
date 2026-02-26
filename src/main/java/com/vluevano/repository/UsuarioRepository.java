package com.vluevano.repository;

import com.vluevano.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByNombreUsuarioAndContrasenaUsuario(String usuario, String pass);
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByNombreUsuarioAndContrasenaUsuarioAndActivoTrue(String usuario, String pass);
    Optional<Usuario> findByNombreUsuarioAndActivoTrue(String nombreUsuario);
    boolean existsByNombreUsuario(String usuario);

    @Query("SELECT u FROM Usuario u WHERE u.activo = true")
    List<Usuario> findAllActivos();
}