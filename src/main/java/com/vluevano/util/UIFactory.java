package com.vluevano.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.control.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import java.util.List;
import java.util.function.Function;

public class UIFactory {

    /**
     * Crea un botón primario estilizado
     * 
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
     * Crea un botón secundario estilizado
     * 
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
     * Crea un botón de texto estilizado
     * 
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
     * Crea un campo de texto estilizado
     * 
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
     * Crea un header estilizado con título, subtítulo y botón de volver
     * @param titulo
     * @param subtitulo
     * @param accionVolver
     * @return
     */
    public static HBox crearHeader(String titulo, String subtitulo, Runnable accionVolver) {
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 40, 20, 40));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #111827; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: white;");

        Label lblSubtitulo = new Label(subtitulo);
        lblSubtitulo.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-text-fill: #e0e0e0;");

        VBox textosContainer = new VBox(2, lblTitulo, lblSubtitulo);
        textosContainer.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVolver = crearBotonSecundario("Volver al Menú");
        btnVolver.setOnAction(e -> accionVolver.run());
        
        header.getChildren().addAll(textosContainer, spacer, btnVolver);
        return header;
    }

    /**
     * Crea un grupo de input (Label + Campo)
     * 
     * @param textoLabel
     * @param campo
     * @return
     */
    public static VBox crearGrupoInput(String textoLabel, Node campo) {
        Label l = new Label(textoLabel);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");
        // Si tiene asterisco, lo pintamos del color primario
        if (textoLabel.contains("*")) {
            l.setTextFill(Color.web(AppTheme.COLOR_PRIMARY));
        }

        VBox v = new VBox(5, l, campo);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    /**
     * Crea un título de sección estilizado
     * 
     * @param texto
     * @return
     */
    public static Label crearTituloSeccion(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY
                + "; -fx-font-size: 14px; -fx-padding: 15 0 5 0;");
        return l;
    }

    /**
     * Crea un botón pequeño de "Editar" para usar dentro de las tablas
     * 
     * @param accion
     * @return
     */
    public static Button crearBotonTablaEditar(Runnable accion) {
        Button btn = new Button("Editar");
        btn.setStyle(
                "-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: bold;");
        btn.setOnAction(e -> accion.run());
        return btn;
    }

    /**
     * Crea un botón pequeño de "Eliminar" para usar dentro de las tablas
     * 
     * @param accion
     * @return
     */
    public static Button crearBotonTablaEliminar(Runnable accion) {
        Button btn = new Button("Eliminar");
        btn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-cursor: hand; -fx-font-size: 11px;");
        btn.setOnAction(e -> accion.run());
        return btn;
    }

    /**
     * Crea un dato de detalle (etiqueta + valor) estilizado
     * 
     * @param etiqueta
     * @param valor
     * @return
     */
    public static VBox crearDatoDetalle(String etiqueta, String valor) {
        Label l = new Label(etiqueta);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        Label v = new Label(valor != null ? valor : "-");
        v.setStyle("-fx-text-fill: #4B5563; -fx-wrap-text: true;");
        v.setMaxWidth(200);

        return new VBox(2, l, v);
    }

    /**
     * Configura un Stage como modal
     * 
     * @param dialogStage
     * @param ownerStage
     */
    public static void configurarStageModal(Stage dialogStage, Stage ownerStage) {
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
    }

    /**
     * Crea una tabla para gestionar relaciones
     * @param <T>
     * @param items
     * @param tituloCol1
     * @param valCol1
     * @param tituloCol2
     * @param valCol2
     * @param permitirEliminar
     * @return
     */
    public static <T> TableView<T> crearTablaRelacion(
            ObservableList<T> items,
            String tituloCol1, Function<T, String> valCol1,
            String tituloCol2, Function<T, String> valCol2,
            boolean permitirEliminar) {

        TableView<T> table = new TableView<>();
        table.setItems(items);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(150);
        table.setStyle("-fx-base: #202020; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        TableColumn<T, String> col1 = new TableColumn<>(tituloCol1);
        col1.setCellValueFactory(d -> new SimpleStringProperty(valCol1.apply(d.getValue())));

        TableColumn<T, String> col2 = new TableColumn<>(tituloCol2);
        col2.setCellValueFactory(d -> new SimpleStringProperty(valCol2.apply(d.getValue())));
        col2.setStyle("-fx-alignment: CENTER-RIGHT;");
        col2.setMinWidth(120);
        col2.setMaxWidth(150);

        table.getColumns().addAll(List.of(col1, col2));

        if (permitirEliminar) {
            TableColumn<T, Void> colEliminar = new TableColumn<>("");
            colEliminar.setMinWidth(45);
            colEliminar.setMaxWidth(45);
            colEliminar.setStyle("-fx-alignment: CENTER;");
            colEliminar.setCellFactory(param -> new TableCell<>() {
                private final Button btn = new Button("X");
                {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #DC2626; -fx-border-radius: 3; -fx-padding: 2 6 2 6;");
                    btn.setOnAction(event -> {
                        T item = getTableView().getItems().get(getIndex());
                        items.remove(item);
                    });
                }
                @Override protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(empty ? null : btn);
                }
            });
            table.getColumns().add(colEliminar);
        }
        return table;
    }
}