package com.vluevano.controller;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.vluevano.database.DatabaseConnection;
import com.vluevano.model.Usuario;

public class UsuarioController {

    // Método para iniciar sesión
    public static boolean iniciarSesion(String nombreUsuario, String contrasenaUsuario) {
        String query = "SELECT * FROM usuario WHERE nombreusuario = ? AND contrasenausuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombreUsuario);
            stmt.setString(2, contrasenaUsuario);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Retorna true si hay resultados (usuario válido)
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para cambiar la contraseña de un usuario
    public static boolean cambiarContrasena(int idUsuario, String nuevaContrasena) {
        String query = "UPDATE usuario SET contrasenausuario = ? WHERE idusuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nuevaContrasena);
            stmt.setInt(2, idUsuario);

            return stmt.executeUpdate() > 0; // Retorna true si se actualizó al menos una fila
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para registrar un nuevo usuario
    public static boolean registrarUsuario(Usuario usuario, StringBuilder mensaje) {
        if (usuario.getNombreUsuario().isEmpty() || usuario.getContrasenaUsuario().length() < 6) {
            mensaje.append("Nombre de usuario o contraseña inválidos.\n");
            return false;
        }

        // Verificar si el nombre de usuario ya existe
        if (nombreUsuarioExiste(usuario.getNombreUsuario())) {
            mensaje.append("El nombre de usuario ya existe.\n");
            return false;
        }

        String query = "INSERT INTO usuario (nombreusuario, contrasenausuario, permiso) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombreUsuario());
            stmt.setString(2, usuario.getContrasenaUsuario());
            stmt.setBoolean(3, usuario.isPermiso());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setIdUsuario(generatedKeys.getInt(1)); // Asigna el nuevo ID al usuario
                    }
                }
                mensaje.append("Usuario registrado exitosamente.\n");
                return true;
            }
        } catch (SQLException | IOException e) {
            mensaje.append("Error al registrar usuario: " + e.getMessage() + "\n");
        }
        return false;
    }

    // Método para verificar si el nombre de usuario ya existe
    private static boolean nombreUsuarioExiste(String nombreUsuario) {
        String query = "SELECT 1 FROM usuario WHERE nombreusuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Si existe, retorna true
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para eliminar un usuario
    public static boolean eliminarUsuario(int idUsuario) {
        String query = "DELETE FROM usuario WHERE idusuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, idUsuario);

            return stmt.executeUpdate() > 0; // Retorna true si se eliminó al menos una fila
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Método para consultar todos los usuarios
    public static List<Usuario> consultarUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();
        String query = "SELECT * FROM usuario";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getInt("idusuario"),
                        rs.getString("nombreusuario"),
                        rs.getString("contrasenausuario"),
                        rs.getBoolean("permiso") // Leer el valor del permiso
                );
                usuarios.add(usuario);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return usuarios;
    }

    // Método para verificar si un usuario tiene permiso de administrador
    public static boolean tienePermiso(String nombreUsuario) {
        String query = "SELECT permiso FROM usuario WHERE nombreusuario = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, nombreUsuario);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBoolean("permiso");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
