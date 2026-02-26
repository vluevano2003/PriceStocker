package com.vluevano.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.Map;

@Service
public class SupabaseAuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestClient restClient;

    /**
     * Constructor que inicializa el RestClient para las operaciones HTTP con Supabase. Este cliente se utilizará para enviar solicitudes de autenticación y registro a la API de Supabase
     */
    public SupabaseAuthService() {
        this.restClient = RestClient.create();
    }

    /**
     * Inicia sesión en Supabase utilizando el endpoint de autenticación. Envía una solicitud POST con las credenciales del usuario (email y password) y maneja la respuesta para extraer el token de acceso si la autenticación es exitosa. En caso de error, se captura la excepción y se imprime un mensaje de error
     * @param email
     * @param password
     * @return
     */
    public String iniciarSesion(String email, String password) {
        String endpoint = supabaseUrl + "/auth/v1/token?grant_type=password";
        Map<String, String> credenciales = Map.of("email", email, "password", password);

        try {
            ResponseEntity<Map<String, Object>> response = restClient.post()
                .uri(endpoint)
                .header("apikey", supabaseKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(credenciales)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return String.valueOf(response.getBody().get("access_token"));
            }
        } catch (Exception e) {
            System.err.println("Error al iniciar sesión: " + e.getMessage());
        }
        return null;
    }

    /**
     * Registra un nuevo usuario en Supabase utilizando el endpoint de registro. Envía una solicitud POST con las credenciales del nuevo usuario (email y password) y maneja la respuesta para determinar si el registro fue exitoso. En caso de error, se captura la excepción y se imprime un mensaje de error
     * @param email
     * @param password
     * @return
     */
    public boolean registrarUsuario(String email, String password) {
        String endpoint = supabaseUrl + "/auth/v1/signup";
        Map<String, String> credenciales = Map.of("email", email, "password", password);

        try {
            ResponseEntity<Map<String, Object>> response = restClient.post()
                .uri(endpoint)
                .header("apikey", supabaseKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(credenciales)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error al registrar en Supabase: " + e.getMessage());
            return false;
        }
    }
}