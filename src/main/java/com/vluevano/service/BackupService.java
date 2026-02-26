package com.vluevano.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class BackupService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    private final RestClient restClient;
    private final String NOMBRE_ARCHIVO = "pricestocker_backup.zip";

    /**
     * Constructor que inicializa el RestClient para las operaciones HTTP con Supabase
     */
    public BackupService() {
        this.restClient = RestClient.create();
    }

    /**
     * Obtiene la ruta del directorio donde se almacenarán los respaldos locales. Si el directorio no existe, lo crea
     * @return
     */
    private String getDirectorioBaseDatos() {
        String userHome = System.getProperty("user.home");
        File dir = new File(userHome, "PriceStockerData");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    /**
     * Genera un respaldo local de la base de datos H2 en formato ZIP. El archivo se guarda en la carpeta PriceStockerData dentro del directorio del usuario
     * @param email
     * @return
     */
    private String obtenerRutaUsuario(String email) {
        String carpeta = email.replaceAll("[^a-zA-Z0-9]", "_");
        return "/storage/v1/object/backups/" + carpeta + "/" + NOMBRE_ARCHIVO;
    }

    /**
     * Genera un respaldo local de la base de datos H2 en formato ZIP. El archivo se guarda en la carpeta PriceStockerData dentro del directorio del usuario
     * @return
     */
    public File generarRespaldoLocal() {
        try {
            String rutaDirectorio = getDirectorioBaseDatos();
            File archivoBackup = new File(rutaDirectorio, NOMBRE_ARCHIVO);
            
            String rutaSql = archivoBackup.getAbsolutePath().replace("\\", "/");
            
            jdbcTemplate.execute("BACKUP TO '" + rutaSql + "'");
            
            if (archivoBackup.exists()) return archivoBackup;
        } catch (Exception e) {
            System.err.println("Error al generar backup local: " + e.getMessage());
        }
        return null;
    }

    /**
     * Sube el archivo de respaldo a Supabase Storage bajo una ruta específica del usuario. Se utiliza el token de autenticación para autorizar la operación
     * @param archivo
     * @param token
     * @param email
     * @return
     */
    public boolean subirRespaldoNube(File archivo, String token, String email) {
        if (archivo == null || !archivo.exists()) return false;
        String endpoint = supabaseUrl + obtenerRutaUsuario(email);

        try {
            FileSystemResource resource = new FileSystemResource(archivo);
            ResponseEntity<String> response = restClient.post()
                    .uri(endpoint)
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + token)
                    .header("x-upsert", "true") 
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource)
                    .retrieve()
                    .toEntity(String.class);

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("Error al subir el archivo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Descarga el archivo de respaldo desde Supabase Storage utilizando el token de autenticación. El archivo se guarda temporalmente en la carpeta PriceStockerData para su posterior aplicación
     * @param token
     * @param email
     * @return
     */
    public File descargarRespaldoNube(String token, String email) {
        String endpoint = supabaseUrl + obtenerRutaUsuario(email);
        try {
            byte[] fileBytes = restClient.get()
                    .uri(endpoint)
                    .header("apikey", supabaseKey)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .body(byte[].class);

            File fileDescargado = new File(getDirectorioBaseDatos(), "descarga_" + NOMBRE_ARCHIVO);
            Files.write(fileDescargado.toPath(), fileBytes);
            return fileDescargado;
        } catch (Exception e) {
            System.err.println("Error al descargar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Aplica el respaldo descargado desde Supabase a la base de datos local. El proceso incluye apagar H2, desconectar Spring Boot, extraer el ZIP y reemplazar los archivos de la base de datos. Se maneja cuidadosamente para minimizar riesgos de corrupción
     * @param archivoZip
     * @return
     */
    public boolean aplicarRespaldoAutomatico(File archivoZip) {
        try {
            try {
                jdbcTemplate.execute("SHUTDOWN");
            } catch (Exception e) {
                System.out.println("Aviso al apagar H2: " + e.getMessage());
            }

            if (dataSource instanceof java.io.Closeable) {
                ((java.io.Closeable) dataSource).close();
            }

            System.gc();
            Thread.sleep(1500);

            String dbDir = getDirectorioBaseDatos();

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archivoZip))) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    if (!zipEntry.isDirectory()) {
                        String nombreReal = new File(zipEntry.getName()).getName();
                        
                        File newFile = new File(dbDir, nombreReal);
                        
                        if (newFile.exists()) {
                            newFile.delete(); 
                        }
                        
                        Files.copy(zis, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
            
            archivoZip.delete(); 
            return true;
            
        } catch (Exception e) {
            System.err.println("Error crítico al aplicar el respaldo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}