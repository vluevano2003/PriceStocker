package com.vluevano.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class UIFactory {

    /**
     * Crea un botón primario con estilos predefinidos
     * @param texto
     * @return
     */
    public static Button crearBotonPrimario(String texto) {
        Button btn = new Button(texto);
        btn.setPrefHeight(40);
        String styleBase = "-fx-background-color: " + AppTheme.COLOR_PRIMARY
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        String styleHover = "-fx-background-color: " + AppTheme.COLOR_PRIMARY_HOVER
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";

        btn.setStyle(styleBase);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleBase));
        return btn;
    }

    /**
     * Crea un botón secundario con estilos predefinidos
     * @param texto
     * @return
     */
    public static Button crearBotonSecundario(String texto) {
        Button btn = new Button(texto);
        btn.setPrefHeight(35);
        String styleBase = "-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;";
        String styleHover = "-fx-background-color: #F9FAFB; -fx-border-color: #9CA3AF; -fx-text-fill: #111827; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;";

        btn.setStyle(styleBase);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleBase));
        return btn;
    }

    /**
     * Crea un botón de texto con estilos predefinidos
     * @param texto
     * @return
     */
    public static Button crearBotonTexto(String texto) {
        Button btn = new Button(texto);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-cursor: hand;");
        btn.setOnMouseEntered(
                e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + AppTheme.COLOR_PRIMARY
                        + "; -fx-cursor: hand; -fx-underline: true;"));
        btn.setOnMouseExited(
                e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-cursor: hand;"));
        return btn;
    }

    /**
     * Crea un campo de texto con estilos predefinidos
     * @param prompt
     * @return
     */
    public static TextField crearInput(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(AppTheme.STYLE_INPUT);
        return tf;
    }

    /**
     * Crea un header con título y botón de volver
     * @param titulo
     * @param accionVolver
     * @return
     */
    public static HBox crearHeader(String titulo, Runnable accionVolver) {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 40, 20, 40));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: #111827; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVolver = crearBotonSecundario("Volver al Menú");
        btnVolver.setOnAction(e -> accionVolver.run());

        header.getChildren().addAll(lblTitulo, spacer, btnVolver);
        return header;
    }
}