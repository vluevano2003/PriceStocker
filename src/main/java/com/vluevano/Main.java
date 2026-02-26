package com.vluevano;

import com.vluevano.service.UpdateService; 
import com.vluevano.service.SupabaseAuthService;
import com.vluevano.service.BackupService;
import com.vluevano.util.GestorIdioma; 
import com.vluevano.view.ConfiguracionView;
import com.vluevano.view.LoginScreen; 

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader.StateChangeNotification;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.io.File;
import java.util.prefs.Preferences;

@SpringBootApplication
@ComponentScan(basePackages = "com.vluevano")
@EnableScheduling
public class Main extends Application {

    private ConfigurableApplicationContext context;

    /**
     * Inicialización de la aplicación. Aquí es donde arrancamos Spring y preparamos todo antes de mostrar la UI
     */
    @Override
    public void init() throws Exception {
        notifyPreloader(new AppPreloader.ProgressNotificationCustom(0.1, "")); 
        
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        
        notifyPreloader(new AppPreloader.ProgressNotificationCustom(0.3, ""));

        context = builder.run();

        GestorIdioma idioma = context.getBean(GestorIdioma.class);

        notifyPreloader(new AppPreloader.ProgressNotificationCustom(0.7, idioma.get("preloader.ui")));
        Thread.sleep(500);

        notifyPreloader(new AppPreloader.ProgressNotificationCustom(1.0, idioma.get("preloader.ready")));
        Thread.sleep(200);
    }

    /**
     * Aquí es donde mostramos la pantalla de login. Si el servicio de actualizaciones falla, se muestra una advertencia pero no se detiene la aplicación
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            LoginScreen loginScreen = context.getBean(LoginScreen.class);
            notifyPreloader(new StateChangeNotification(StateChangeNotification.Type.BEFORE_START));
            loginScreen.show(primaryStage);
        } catch (Exception e) {
            System.err.println("ERROR CRÍTICO AL INICIAR LOGIN: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            UpdateService updateService = context.getBean(UpdateService.class);
            updateService.buscarYActualizar();
            System.out.println("Servicio de actualizaciones iniciado correctamente.");
        } catch (Exception e) {
            System.err.println("ADVERTENCIA: No se pudo cargar el servicio de actualizaciones.");
            System.err.println("Causa: " + e.getMessage());
        }
    }

    /**
     * Cierre limpio de la aplicación, asegurando que Spring se detenga correctamente y que la aplicación salga sin problemas
     */
    @Override
    public void stop() {
        try {
            if (context != null) {
                Preferences prefs = Preferences.userNodeForPackage(ConfiguracionView.class);
                boolean autoBackup = prefs.getBoolean("AUTO_BACKUP", false);
                String savedEmail = prefs.get("SUPA_EMAIL", "");
                String savedPass = prefs.get("SUPA_PASS", "");

                if (autoBackup && !savedEmail.isEmpty() && !savedPass.isEmpty()) {
                    System.out.println("Iniciando auto-respaldo silencioso en la nube antes de salir...");
                    
                    SupabaseAuthService authService = context.getBean(SupabaseAuthService.class);
                    BackupService backupService = context.getBean(BackupService.class);

                    String token = authService.iniciarSesion(savedEmail, savedPass);
                    if (token != null) {
                        File zip = backupService.generarRespaldoLocal();
                        if (zip != null) {
                            boolean exito = backupService.subirRespaldoNube(zip, token, savedEmail);
                            if(exito) {
                                System.out.println("¡Respaldo subido con éxito en segundo plano!");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error ejecutando el auto-respaldo al cerrar: " + e.getMessage());
        }
        if (context != null) {
            context.close();
        }
        Platform.exit();
        System.exit(0);
    }

    /**
     * Punto de entrada principal de la aplicación. Configura el preloader y lanza la aplicación JavaFX
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("javafx.preloader", AppPreloader.class.getCanonicalName());
        launch(Main.class, args);
    }
}