package com.vluevano.service;

import com.vluevano.util.AppTheme;
import com.vluevano.util.GestorIdioma;
import com.vluevano.util.UIFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;

@Service
public class UpdateService {

    private static final String GITHUB_USER = "vluevano2003";
    private static final String GITHUB_REPO = "PriceStocker";
    private static final String VERSION_ACTUAL = "v1.4.0";

    @Autowired
    private GestorIdioma idioma;

    /**
     * Busca en GitHub la última versión disponible y si es mayor que la actual, muestra un diálogo al usuario para actualizar
     */
    public void buscarYActualizar() {
        new Thread(() -> {
            try {
                System.out.println("Buscando actualizaciones...");

                String urlApi = "https://api.github.com/repos/" + GITHUB_USER + "/" + GITHUB_REPO + "/releases/latest";

                HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlApi))
                        .header("User-Agent", "PriceStocker-App")
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String json = response.body();
                    String versionRemota = extraerValorJson(json, "tag_name");

                    System.out.println("Versión actual: " + VERSION_ACTUAL);
                    System.out.println("Versión remota: " + versionRemota);

                    if (esVersionMayor(versionRemota, VERSION_ACTUAL)) {

                        String versionSinV = versionRemota.replace("v", "");
                        String downloadUrl = "https://github.com/" + GITHUB_USER + "/" + GITHUB_REPO +
                                "/releases/download/" + versionRemota + "/PriceStocker-Installer-" + versionSinV
                                + ".exe";

                        Platform.runLater(() -> mostrarDialogoActualizacion(versionRemota, downloadUrl));
                    }
                } else {
                    System.err.println("GitHub rechazó la petición. Código: " + response.statusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Muestra un diálogo informando al usuario que hay una nueva versión disponible y preguntando si desea actualizar ahora o más tarde
     * @param versionRemota
     * @param downloadUrl
     */
    private void mostrarDialogoActualizacion(String versionRemota, String downloadUrl) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + "-fx-background-radius: 8; -fx-border-radius: 8;");
        root.setAlignment(Pos.CENTER);
        root.setMaxWidth(400);
        root.setMaxHeight(250);

        Label lblTitulo = UIFactory.crearTituloSeccion(idioma.get("update.title.available"));
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");

        Label lblMensaje = new Label(idioma.get("update.msg.available", versionRemota));
        lblMensaje.setStyle("-fx-text-fill: " + AppTheme.COLOR_TEXT_MAIN + "; -fx-font-size: 14px;");
        lblMensaje.setWrapText(true);
        lblMensaje.setAlignment(Pos.CENTER);
        lblMensaje.setTextAlignment(TextAlignment.CENTER);

        Button btnActualizar = UIFactory.crearBotonPrimario(idioma.get("update.btn.now"));
        Button btnCancelar = UIFactory.crearBotonSecundario(idioma.get("update.btn.later"));

        btnCancelar.setOnAction(e -> stage.close());

        btnActualizar.setOnAction(e -> {
            stage.close();
            mostrarDialogoDescargaYActualizar(downloadUrl);
        });

        HBox botones = new HBox(15, btnCancelar, btnActualizar);
        botones.setAlignment(Pos.CENTER);
        botones.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(lblTitulo, lblMensaje, botones);
        stage.setScene(crearEscenaConFondoOscuro(root, stage));
        stage.show();
    }

    /**
     * Muestra un diálogo con un indicador de progreso mientras se descarga la nueva versión, luego ejecuta el instalador y reinicia la aplicación
     * @param downloadUrl
     */
    private void mostrarDialogoDescargaYActualizar(String downloadUrl) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(30, 40, 30, 40));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + "-fx-background-radius: 8; -fx-border-radius: 8;");
        root.setAlignment(Pos.CENTER);
        root.setMaxWidth(350);
        root.setMaxHeight(200);

        Label lblTitulo = UIFactory.crearTituloSeccion(idioma.get("update.title.downloading"));
        lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");

        Label lblMensaje = new Label(idioma.get("update.msg.downloading"));
        lblMensaje.setStyle("-fx-text-fill: " + AppTheme.COLOR_TEXT_MAIN + "; -fx-font-size: 13px;");
        lblMensaje.setTextAlignment(TextAlignment.CENTER);

        ProgressIndicator pi = new ProgressIndicator();
        pi.setStyle("-fx-progress-color: " + AppTheme.COLOR_PRIMARY + ";");

        root.getChildren().addAll(lblTitulo, pi, lblMensaje);
        stage.setScene(crearEscenaConFondoOscuro(root, stage));
        stage.show();

        new Thread(() -> {
            try {
                descargarEInstalar(downloadUrl);
            } catch (Exception ex) {
                ex.printStackTrace();
                Platform.runLater(() -> {
                    stage.close();
                    mostrarDialogoError();
                });
            }
        }).start();
    }

    /**
     * Muestra un diálogo de error si la descarga o instalación falla, informando al usuario que algo salió mal y que intente actualizar manualmente desde GitHub
     */
    private void mostrarDialogoError() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + "-fx-background-radius: 8; -fx-border-radius: 8;");
        root.setAlignment(Pos.CENTER);
        root.setMaxWidth(350);
        root.setMaxHeight(200);

        Label lblTitulo = new Label(idioma.get("update.error.title"));
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_ERROR + ";");

        Label lblMensaje = new Label(idioma.get("update.error.msg"));
        lblMensaje.setStyle("-fx-text-fill: " + AppTheme.COLOR_TEXT_MAIN + "; -fx-font-size: 14px;");
        lblMensaje.setTextAlignment(TextAlignment.CENTER);

        Button btnCerrar = UIFactory.crearBotonSecundario(idioma.get("ui.btn.close"));
        btnCerrar.setOnAction(e -> stage.close());

        root.getChildren().addAll(lblTitulo, lblMensaje, btnCerrar);
        stage.setScene(crearEscenaConFondoOscuro(root, stage));
        stage.show();
    }

    /**
     * Crea una escena con un fondo oscuro semitransparente para resaltar el contenido del diálogo
     * @param cajitaBlanca
     * @param stage
     * @return
     */
    private Scene crearEscenaConFondoOscuro(VBox cajitaBlanca, Stage stage) {
        StackPane overlay = new StackPane(cajitaBlanca);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.6);");
        overlay.setAlignment(Pos.CENTER);

        Scene scene = new Scene(overlay);
        scene.setFill(Color.TRANSPARENT);

        Window mainWindow = Window.getWindows().stream().filter(Window::isShowing).findFirst().orElse(null);
        if (mainWindow != null) {
            stage.initOwner(mainWindow);
            overlay.setPrefSize(mainWindow.getWidth(), mainWindow.getHeight());
            overlay.prefWidthProperty().bind(mainWindow.widthProperty());
            overlay.prefHeightProperty().bind(mainWindow.heightProperty());
        }
        return scene;
    }

    /**
     * Descarga el instalador de la nueva versión, lo ejecuta en modo silencioso y luego reinicia la aplicación. Si algo falla, lanza una excepción para mostrar el diálogo de error
     * @param urlDescarga
     * @throws IOException
     * @throws InterruptedException
     */
    private void descargarEInstalar(String urlDescarga) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlDescarga))
                .header("User-Agent", "PriceStocker-App")
                .build();

        String tmpDir = System.getProperty("java.io.tmpdir");
        Path updateFile = Paths.get(tmpDir, "PriceStockerUpdateInstaller.exe");

        HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(updateFile));

        if (response.statusCode() == 200) {
            System.out.println("Descarga completada. Ejecutando instalador y reiniciando...");

            String appPath = System.getProperty("user.dir") + "\\PriceStocker.exe";
            File batFile = new File(tmpDir, "updater.bat");

            String script = "@echo off\r\n"
                    + "timeout /t 2 /nobreak > NUL\r\n"
                    + "start /wait \"\" \"" + updateFile.toAbsolutePath().toString() + "\" /SILENT /SUPPRESSMSGBOXES\r\n"
                    + "start \"\" \"" + appPath + "\"\r\n"
                    + "del \"%~f0\"\r\n";

            try (FileWriter fw = new FileWriter(batFile)) {
                fw.write(script);
            }

            Runtime.getRuntime().exec("cmd /c start /min \"\" \"" + batFile.getAbsolutePath() + "\"");
            
            System.exit(0);
        } else {
            throw new IOException("Código HTTP: " + response.statusCode());
        }
    }

    /**
     * Compara dos versiones en formato "vX.Y.Z" y determina si la versión remota es mayor que la local. Si el formato no es correcto, devuelve false
     * @param remota
     * @param local
     * @return
     */
    private boolean esVersionMayor(String remota, String local) {
        try {
            String[] v1 = remota.replace("v", "").split("\\.");
            String[] v2 = local.replace("v", "").split("\\.");

            int length = Math.max(v1.length, v2.length);
            for (int i = 0; i < length; i++) {
                int numRemoto = i < v1.length ? Integer.parseInt(v1[i]) : 0;
                int numLocal = i < v2.length ? Integer.parseInt(v2[i]) : 0;

                if (numRemoto > numLocal) return true;
                if (numRemoto < numLocal) return false;
            }
        } catch (Exception e) {
            System.err.println("Error al comparar versiones: " + e.getMessage());
        }
        return false;
    }

    /**
     * Extrae el valor de una clave específica de un JSON simple sin usar librerías externas. Si la clave no existe o el formato es incorrecto, devuelve una cadena vacía
     * @param json
     * @param key
     * @return
     */
    private String extraerValorJson(String json, String key) {
        String search = "\"" + key + "\"";
        int keyIndex = json.indexOf(search);
        if (keyIndex == -1) return "";
        int colonIndex = json.indexOf(":", keyIndex);
        int startQuote = json.indexOf("\"", colonIndex) + 1;
        int endQuote = json.indexOf("\"", startQuote);
        if (startQuote == 0 || endQuote == -1) return "";
        return json.substring(startQuote, endQuote);
    }
}