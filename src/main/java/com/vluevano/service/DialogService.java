package com.vluevano.service;

import com.vluevano.util.AppTheme;
import com.vluevano.util.GestorIdioma;
import com.vluevano.util.UIFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DialogService {

    @Autowired
    private GestorIdioma idioma; 

    /**
     * Muestra un diálogo de alerta con el tipo, título y mensaje especificados. El diálogo se muestra centrado sobre su propietario.
     * @param tipo
     * @param titulo
     * @param mensaje
     * @param owner
     */
    public void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje, Stage owner) {
        Stage alertStage = crearStage(owner);

        VBox card = new VBox(20);
        card.setPadding(new Insets(25));
        card.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        card.setMinWidth(350);
        card.setMaxWidth(350);

        String colorTitulo = tipo == Alert.AlertType.ERROR ? AppTheme.COLOR_ERROR
                : (tipo == Alert.AlertType.WARNING ? AppTheme.COLOR_WARNING : AppTheme.COLOR_PRIMARY);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + colorTitulo + ";");

        Label lblMsg = new Label(mensaje);
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");

        Button btnOk = new Button(idioma.get("dialog.btn.understood"));
        
        if (tipo == Alert.AlertType.ERROR) {
            btnOk.setStyle(
                    "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        } else {
            btnOk.setStyle("-fx-background-color: " + AppTheme.COLOR_PRIMARY
                    + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        }
        btnOk.setPrefHeight(35);
        btnOk.setOnAction(e -> alertStage.close());

        HBox botones = new HBox(btnOk);
        botones.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(lblTitulo, lblMsg, botones);

        mostrarDialogoModal(alertStage, card, owner);
    }

    /**
     * Muestra un diálogo de confirmación con opciones "Sí" y "No". Devuelve true si el usuario confirma, false si cancela.
     * @param titulo
     * @param mensaje
     * @param owner
     * @return
     */
    public boolean mostrarConfirmacion(String titulo, String mensaje, Stage owner) {
        Stage alertStage = crearStage(owner);

        VBox card = new VBox(20);
        card.setPadding(new Insets(25));
        card.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        card.setMinWidth(380);
        card.setMaxWidth(380);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label lblMsg = new Label(mensaje);
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");

        Button btnNo = UIFactory.crearBotonSecundario(idioma.get("dialog.btn.cancel"));
        Button btnSi = new Button(idioma.get("dialog.btn.confirm"));
        
        btnSi.setStyle(
                "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        btnSi.setPrefHeight(35);

        final boolean[] respuesta = { false };
        btnNo.setOnAction(e -> {
            respuesta[0] = false;
            alertStage.close();
        });
        btnSi.setOnAction(e -> {
            respuesta[0] = true;
            alertStage.close();
        });

        HBox botones = new HBox(15, btnNo, btnSi);
        botones.setAlignment(Pos.CENTER_RIGHT);

        card.getChildren().addAll(lblTitulo, lblMsg, botones);

        mostrarDialogoModal(alertStage, card, owner);
        return respuesta[0];
    }

    /**
     * Muestra un diálogo modal con el contenido proporcionado, centrado sobre su propietario
     * @param dialogStage
     * @param content
     * @param owner
     */
    public void mostrarDialogoModal(Stage dialogStage, Region content, Stage owner) {

        content.setMaxHeight(Region.USE_PREF_SIZE);
        if (content.getMaxWidth() == Region.USE_COMPUTED_SIZE) {
            content.setMaxWidth(Region.USE_PREF_SIZE);
        }

        StackPane rootOverlay = new StackPane(content);
        rootOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.4);");
        rootOverlay.setAlignment(Pos.CENTER);

        Scene scene = new Scene(rootOverlay);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);

        if (owner != null) {
            dialogStage.setX(owner.getX());
            dialogStage.setY(owner.getY());
            dialogStage.setWidth(owner.getWidth());
            dialogStage.setHeight(owner.getHeight());
        }
        dialogStage.showAndWait();
    }

    /**
     * Crea un nuevo Stage configurado para ser modal respecto a su propietario
     * @param owner
     * @return
     */
    private Stage crearStage(Stage owner) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        return stage;
    }
}