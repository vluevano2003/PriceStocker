package com.vluevano;

import com.vluevano.util.AppTheme;
import javafx.application.Preloader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AppPreloader extends Preloader {

    private Stage preloaderStage;
    private ProgressBar progressBar;
    private Label lblStatus;

    /**
     * Este método se ejecuta al iniciar la aplicación y es donde se configura la ventana del preloader
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.preloaderStage = primaryStage;

        ImageView logo = new ImageView();
        try {
            logo.setImage(new Image(getClass().getResourceAsStream("/images/PriceStockerLogo.png")));
            logo.setFitWidth(280);
            logo.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Logo no encontrado.");
        }

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(320);
        progressBar.setPrefHeight(14);
        
        progressBar.setStyle(
            "-fx-accent: " + AppTheme.COLOR_PRIMARY + "; " +
            "-fx-control-inner-background: #E5E7EB; " +
            "-fx-background-color: transparent; " +
            "-fx-background-radius: 20; " + 
            "-fx-padding: 0;"
        );

        lblStatus = new Label("Iniciando componentes...");
        lblStatus.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-text-fill: #6B7280;");

        VBox card = new VBox(25, logo, progressBar, lblStatus);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(40));
        card.setStyle(AppTheme.STYLE_CARD + "-fx-background-color: white;");

        VBox outerRoot = new VBox(card);
        outerRoot.setPadding(new Insets(25));
        outerRoot.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(outerRoot, 480, 380);
        scene.setFill(Color.TRANSPARENT);

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        
        primaryStage.centerOnScreen();
        
        primaryStage.show();
    }

    /**
     * Este método se ejecuta durante el proceso de carga y es donde se actualiza el progreso del preloader
     */
    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof StateChangeNotification) {
            StateChangeNotification scn = (StateChangeNotification) info;
            if (scn.getType() == StateChangeNotification.Type.BEFORE_START) {
                if (preloaderStage != null) {
                    preloaderStage.close();
                }
            }
        } else if (info instanceof ProgressNotificationCustom) {
            ProgressNotificationCustom customInfo = (ProgressNotificationCustom) info;
            lblStatus.setText(customInfo.getMessage());
            progressBar.setProgress(customInfo.getProgress());
        }
    }

    /**
     * Este método se ejecuta durante el proceso de carga y es donde se actualiza el progreso del preloader
     */
    @Override
    public void handleProgressNotification(ProgressNotification info) {
        progressBar.setProgress(info.getProgress());
    }

    /**
     * Clase personalizada para enviar notificaciones de progreso con mensaje desde la aplicación al preloader
     */
    public static class ProgressNotificationCustom implements PreloaderNotification {
        private final String message;
        private final double progress;

        public ProgressNotificationCustom(double progress, String message) {
            this.progress = progress;
            this.message = message;
        }
        public String getMessage() { return message; }
        public double getProgress() { return progress; }
    }
}