package com.vluevano.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;

@Service
public class UpdateService {

    private static final String GITHUB_USER = "vluevano"; 
    private static final String GITHUB_REPO = "pricestocker";
    
    private static final String VERSION_ACTUAL = "v1.1.0";

    /**
     * Este método busca actualizaciones en GitHub y, si encuentra una versión más nueva, la descarga e instala
      * Se ejecuta en un hilo separado para no bloquear la interfaz gráfica
     */
    public void buscarYActualizar() {
        new Thread(() -> {
            try {
                System.out.println("Buscando actualizaciones...");
                
                String urlApi = "https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/releases/latest";
                
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlApi)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String json = response.body();
                    
                    String versionRemota = extraerValorJson(json, "tag_name");
                    
                    System.out.println("Versión actual: " + VERSION_ACTUAL);
                    System.out.println("Versión remota: " + versionRemota);

                    if (esVersionMayor(versionRemota, VERSION_ACTUAL)) {
                        System.out.println("¡Nueva versión encontrada! Descargando...");
                        
                        String downloadUrl = "https://github.com/" + GITHUB_USER + "/" + GITHUB_REPO + 
                                             "/releases/download/" + versionRemota + "/PriceStocker.exe";
                        
                        descargarEInstalar(downloadUrl);
                    } else {
                        System.out.println("El sistema está actualizado.");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Descarga el nuevo EXE y ejecuta un script para reemplazar el actual al cerrar la aplicación
     * @param urlDescarga
     * @throws IOException
     * @throws InterruptedException
     */
    private void descargarEInstalar(String urlDescarga) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(urlDescarga)).build();
        
        Path updateFile = Paths.get("update.tmp");
        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(updateFile));

        if (response.statusCode() == 200) {
            System.out.println("Descarga completada. Iniciando protocolo de actualización...");
            crearYEjecutarScriptBat();
        } else {
            System.err.println("Error al descargar: " + response.statusCode());
        }
    }

    /**
     * Crea un archivo .bat que se ejecutará al cerrar la aplicación para reemplazar el EXE actual por el nuevo
     * @throws IOException
     */
    private void crearYEjecutarScriptBat() throws IOException {
        String nombreExe = "PriceStocker.exe";
        File batFile = new File("updater.bat");

        String script = "@echo off\r\n"
                + "timeout /t 2 /nobreak > NUL\r\n"
                + "del \"" + nombreExe + "\"\r\n" 
                + "ren update.tmp \"" + nombreExe + "\"\r\n" 
                + "start \"\" \"" + nombreExe + "\"\r\n" 
                + "del \"%~f0\"\r\n"; 

        try (FileWriter fw = new FileWriter(batFile)) {
            fw.write(script);
        }
        Runtime.getRuntime().exec("cmd /c start updater.bat");
        System.exit(0);
    }

    /**
     * Compara dos versiones en formato "vX.Y.Z" para determinar si la remota es mayor que la local
     * @param remota
     * @param local
     * @return
     */
    private boolean esVersionMayor(String remota, String local) {
        String v1 = remota.replace("v", "");
        String v2 = local.replace("v", "");
        return v1.compareTo(v2) > 0;
    }

    /**
     * Extrae el valor de una clave específica de un JSON simple (sin librerías externas)
     * @param json
     * @param key
     * @return
     */
    private String extraerValorJson(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return "";
        start += search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }
}