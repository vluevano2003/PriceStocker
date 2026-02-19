package com.vluevano;

import com.vluevano.service.UpdateService; 
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

@SpringBootApplication
@ComponentScan(basePackages = "com.vluevano")
@EnableScheduling
public class Main extends Application {

    private ConfigurableApplicationContext context;

    /**
     * Este método se ejecuta antes de start() y es ideal para cargar recursos pesados o hacer tareas de inicialización sin bloquear la interfaz gráfica
      * Aquí es donde se inicia el contexto de Spring Boot y se envían notificaciones al preloader para actualizar la barra de progreso
      * Se recomienda usarlo para cargar servicios, configurar la base de datos, etc. mientras el preloader muestra el progreso al usuario
      * De esta forma, cuando start() se ejecute, todo ya estará listo y la interfaz se mostrará rápidamente
      * Además, al ejecutar el contexto en este método, los beans estarán disponibles para start() sin problemas
     */
    @Override
    public void init() throws Exception {
        notifyPreloader(new AppPreloader.ProgressNotificationCustom(0.1, "Iniciando sistema..."));
        
        SpringApplicationBuilder builder = new SpringApplicationBuilder(Main.class);
        notifyPreloader(new AppPreloader.ProgressNotificationCustom(0.3, "Configurando base de datos..."));

        context = builder.run();

        notifyPreloader(new AppPreloader.ProgressNotificationCustom(0.7, "Preparando interfaz de usuario..."));
        Thread.sleep(500);

        notifyPreloader(new AppPreloader.ProgressNotificationCustom(1.0, "¡Todo listo!"));
        Thread.sleep(200);
    }

    /**
     * Este método se ejecuta después de init() y es donde se muestra la interfaz gráfica. Aquí es donde se debe mostrar el LoginScreen
      * Además, aquí es donde se llama al servicio de actualización para buscar nuevas versiones sin bloquear la interfaz
      * Se recomienda usar try-catch para manejar cualquier error que pueda ocurrir al cargar el LoginScreen o el servicio de actualización, para evitar que el programa se cierre inesperadamente
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
     * Este método se ejecuta al cerrar la aplicación. Aquí es donde se deben liberar recursos, cerrar conexiones, etc. para asegurar un cierre limpio
      * Se recomienda usarlo para cerrar el contexto de Spring Boot y cualquier otro recurso que pueda estar abierto
      * Además, es importante llamar a Platform.exit() y System.exit(0) para asegurarse de que la aplicación se cierre completamente
     */
    @Override
    public void stop() {
        if (context != null) {
            context.close();
        }
        Platform.exit();
        System.exit(0);
    }

    /**
     * Este es el punto de entrada de la aplicación. Aquí se configura el preloader y se lanza la aplicación JavaFX
     * @param args
     */
    public static void main(String[] args) {
        System.setProperty("javafx.preloader", AppPreloader.class.getCanonicalName());
        launch(Main.class, args);
    }
}