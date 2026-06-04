package com.vluevano.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class ImpuestoService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Double tasaIvaActual = 0.16; // 16% por defecto

    /**
     * Inicializa el servicio cargando la configuración desde la base de datos. Si no hay datos, mantiene el 0.16
     */
    public void inicializar() {
        try {
            String valorIva = obtenerConfig("TASA_IVA_ACTUAL");
            if (valorIva != null) tasaIvaActual = Double.parseDouble(valorIva);
        } catch (Exception e) {
            System.out.println("Usando configuración de IVA por defecto.");
        }
    }

    /**
     * Obtiene un valor de configuración por su clave.
     */
    private String obtenerConfig(String clave) {
        try {
            List<String> resultados = jdbcTemplate.query(
                "SELECT valor FROM configuracion WHERE clave = ?", 
                (rs, rowNum) -> rs.getString("valor"), 
                clave
            );
            return resultados.isEmpty() ? null : resultados.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Guarda o actualiza un valor de configuración en la base de datos
     */
    private void guardarConfig(String clave, String valor) {
        int count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM configuracion WHERE clave = ?", Integer.class, clave);
        
        if (count > 0) {
            jdbcTemplate.update("UPDATE configuracion SET valor = ? WHERE clave = ?", valor, clave);
        } else {
            jdbcTemplate.update("INSERT INTO configuracion (clave, valor) VALUES (?, ?)", clave, valor);
        }
    }

    /**
     * Retorna la tasa actual (ej: 0.16)
     */
    public Double getTasaIva() {
        return tasaIvaActual;
    }

    /**
     * Retorna el factor matemático de IVA (ej: 1.16)
     */
    public Double getFactorIva() {
        return 1.0 + tasaIvaActual;
    }

    /**
     * Retorna el porcentaje en formato numérico entero (ej: 16)
     */
    public int getIvaPorcentaje() {
        return (int) (tasaIvaActual * 100);
    }

    public void actualizarIvaManual(Double nuevoValor) {
        this.tasaIvaActual = nuevoValor;
        guardarConfig("TASA_IVA_ACTUAL", String.valueOf(nuevoValor));
    }

    /**
     * Intenta actualizar el IVA desde el archivo config en GitHub.
     */
    public boolean actualizarDesdeInternet() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            // Leemos directo desde el repositorio principal (raw branch)
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://raw.githubusercontent.com/vluevano2003/PriceStocker/main/config/impuestos.json"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                double valorIva = root.path("iva").asDouble(0.16);
                
                if (valorIva > 0) {
                    actualizarIvaManual(valorIva);
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error descargando IVA desde internet: " + e.getMessage());
        }
        return false;
    }

    /**
     * Se ejecuta automáticamente cada 12 horas.
     */
    @Scheduled(initialDelay = 10000, fixedRate = 43200000) 
    public void actualizarIvaAutomaticamente() {
        System.out.println("[Sistema] Buscando actualización de IVA desde la nube...");
        boolean exito = actualizarDesdeInternet();
        
        if (exito) {
            System.out.println("[Sistema] Tasa de IVA actualizada automáticamente a: " + (tasaIvaActual * 100) + "%");
        } else {
            System.out.println("[Sistema] Fallo la actualización automática de IVA. Se usará el último valor guardado (" + (tasaIvaActual * 100) + "%).");
        }
    }
}
