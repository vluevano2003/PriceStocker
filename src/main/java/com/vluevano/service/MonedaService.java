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
public class MonedaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Double tipoCambioActual = 20.0; 
    private String monedaPorDefecto = "MXN";

    /**
     * Inicializa el servicio cargando la configuración desde la base de datos. Si no hay datos, se mantienen los valores por defecto
     */
    public void inicializar() {
        try {
            String valorTC = obtenerConfig("TIPO_CAMBIO_USD_MXN");
            if (valorTC != null) tipoCambioActual = Double.parseDouble(valorTC);

            String valorMoneda = obtenerConfig("PREFERENCIA_MONEDA");
            if (valorMoneda != null) monedaPorDefecto = valorMoneda;

        } catch (Exception e) {
            System.out.println("Usando configuración por defecto.");
        }
    }

    /**
     * Obtiene un valor de configuración por su clave. Retorna null si no existe o si hay un error
     * @param clave
     * @return
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
     * @param clave
     * @param valor
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
     * Retorna el tipo de cambio actual almacenado en el servicio. Este valor se actualiza al llamar a actualizarTipoCambioManual o actualizarDesdeInternet
     * @return
     */
    public Double getTipoCambioActual() {
        return tipoCambioActual;
    }

    /**
     * Actualiza el tipo de cambio manualmente y guarda el nuevo valor en la base de datos
     * @param nuevoValor
     */
    public void actualizarTipoCambioManual(Double nuevoValor) {
        this.tipoCambioActual = nuevoValor;
        guardarConfig("TIPO_CAMBIO_USD_MXN", String.valueOf(nuevoValor));
    }

    /**
     * Retorna la moneda por defecto para nuevos productos. Este valor se utiliza en la interfaz para preseleccionar la moneda al crear o editar productos
     * @return
     */
    public String getMonedaPorDefecto() {
        return monedaPorDefecto;
    }

    /**
     * Actualiza la moneda por defecto para nuevos productos y guarda la preferencia en la base de datos
     * @param moneda
     */
    public void setMonedaPorDefecto(String moneda) {
        this.monedaPorDefecto = moneda;
        guardarConfig("PREFERENCIA_MONEDA", moneda);
    }

    /**
     * Intenta actualizar el tipo de cambio desde una API pública. Si la consulta es exitosa y se obtiene un valor válido, actualiza el tipo de cambio en el servicio y lo guarda en la base de datos. Retorna true si la actualización fue exitosa, o false si hubo un error (como problemas de conexión o formato de respuesta inesperado)
     * @return
     */
    public boolean actualizarDesdeInternet() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://open.er-api.com/v6/latest/USD"))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            double valorMXN = root.path("rates").path("MXN").asDouble();

            if (valorMXN > 0) {
                actualizarTipoCambioManual(valorMXN);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Convierte un monto dado en una moneda de origen a MXN utilizando el tipo de cambio actual. Si la moneda de origen es MXN, retorna el mismo monto. Si la moneda de origen es USD, realiza la conversión multiplicando por el tipo de cambio. Si la moneda de origen no es reconocida, retorna el monto sin convertir (puedes ajustar esta lógica según tus necesidades)
     * @param monto
     * @param monedaOrigen
     * @return
     */
    public Double convertirAMxn(Double monto, String monedaOrigen) {
        if (monto == null) return 0.0;
        if ("MXN".equalsIgnoreCase(monedaOrigen)) return monto;
        if ("USD".equalsIgnoreCase(monedaOrigen)) return monto * tipoCambioActual;
        return monto;
    }

    /**
     * Formatea un monto con su moneda original y, si la moneda es diferente a la preferida, muestra también el equivalente en la moneda preferida utilizando el tipo de cambio actual. Por ejemplo, si el monto es 10 USD y la moneda preferida es MXN, podría mostrar algo como "10 USD (≈ 200 MXN)". Si el monto es null, retorna "$0.00". Si la moneda del item es igual a la preferida, solo muestra el monto con su moneda sin conversión
     * @param monto
     * @param monedaItem
     * @return
     */
    public String formatPrecioConConversion(Double monto, String monedaItem) {
        if (monto == null) return "$0.00";
        if (monedaItem == null) monedaItem = "MXN";

        if (monedaItem.equalsIgnoreCase(monedaPorDefecto)) {
            return String.format("$%.2f %s", monto, monedaItem);
        }

        if ("MXN".equals(monedaPorDefecto)) {
            double convertido = convertirAMxn(monto, monedaItem);
            return String.format("$%.2f %s (≈ $%.2f MXN)", monto, monedaItem, convertido);
        }
        
        return String.format("$%.2f %s", monto, monedaItem);
    }

    /**
     * Este método se ejecuta automáticamente cada 12 horas (después de un retraso inicial de 5 segundos al iniciar la aplicación) para intentar actualizar el tipo de cambio desde internet. Si la actualización es exitosa, se muestra un mensaje con el nuevo tipo de cambio. Si falla (por ejemplo, debido a un error de red), se muestra un mensaje indicando que se usará el último valor guardado sin interrumpir el funcionamiento del programa
     */
    @Scheduled(initialDelay = 5000, fixedRate = 43200000) 
    public void actualizarMonedaAutomaticamente() {
        System.out.println("[Sistema] Buscando actualización automática de tipo de cambio...");
        boolean exito = actualizarDesdeInternet();
        
        if (exito) {
            System.out.println("[Sistema] Tipo de cambio actualizado automáticamente a: $" + tipoCambioActual);
        } else {
            System.out.println("[Sistema] Fallo la actualización automática (posible error de red). Se usará el último valor guardado.");
        }
    }
}