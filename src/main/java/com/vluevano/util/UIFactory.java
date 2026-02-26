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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UIFactory {

    private static GestorIdioma idioma;

    /**
     * Inyección de dependencia para el gestor de idioma, permitiendo que los textos de los componentes sean dinámicos según el idioma seleccionado
     * @param gestor
     */
    @Autowired
    public void setGestorIdioma(GestorIdioma gestor) {
        UIFactory.idioma = gestor;
    }

    /**
     * Crea un botón con estilo primario, utilizando colores y estilos definidos en AppTheme. El texto del botón es dinámico y se puede adaptar a diferentes idiomas gracias al gestor de idioma.
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
     * Crea un botón con estilo secundario, utilizando colores neutros y estilos definidos en AppTheme. El texto del botón es dinámico y se puede adaptar a diferentes idiomas gracias al gestor de idioma.
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
     * Crea un botón con estilo de texto, sin fondo ni borde, utilizando colores neutros y estilos definidos en AppTheme. El texto del botón es dinámico y se puede adaptar a diferentes idiomas gracias al gestor de idioma.
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
     * Crea un campo de texto con estilo definido en AppTheme, utilizando colores neutros y estilos modernos. El prompt del campo de texto es dinámico y se puede adaptar a diferentes idiomas gracias al gestor de idioma.
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
     * Crea un header para las pantallas de detalle, utilizando un HBox con estilos definidos en AppTheme. El título y subtítulo del header son dinámicos y se pueden adaptar a diferentes idiomas gracias al gestor de idioma. Además, incluye un botón para volver al menú principal, que ejecuta una acción proporcionada como parámetro.
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

        Button btnVolver = crearBotonSecundario(idioma.get("ui.btn.back_menu"));
        btnVolver.setOnAction(e -> accionVolver.run());
        
        header.getChildren().addAll(textosContainer, spacer, btnVolver);
        return header;
    }

    /**
     * Crea un grupo de input con un label y un campo, utilizando un VBox con estilos definidos en AppTheme. El texto del label es dinámico y se puede adaptar a diferentes idiomas gracias al gestor de idioma. Si el texto del label contiene un asterisco (*), se resalta en color primario para indicar que es un campo obligatorio.
     * @param textoLabel
     * @param campo
     * @return
     */
    public static VBox crearGrupoInput(String textoLabel, Node campo) {
        Label l = new Label(textoLabel);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");
        if (textoLabel.contains("*")) {
            l.setTextFill(Color.web(AppTheme.COLOR_PRIMARY));
        }

        VBox v = new VBox(5, l, campo);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    /**
     * Crea un título para secciones dentro de las pantallas, utilizando un Label con estilos definidos en AppTheme. El texto del título es dinámico y se puede adaptar a diferentes idiomas gracias al gestor de idioma. El título se resalta con un color primario y un tamaño de fuente mayor para diferenciarlo del resto del contenido.
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
     * Crea un botón para las tablas de datos, con estilos definidos en AppTheme. El texto del botón es dinámico y se puede adaptar a diferentes idiomas gracias al gestor de idioma. El botón se utiliza para acciones como editar o eliminar registros dentro de las tablas, y su estilo se diferencia según la acción que representa (editar con colores azules y eliminar con colores rojos).
     * @param accion
     * @return
     */
    public static Button crearBotonTablaEditar(Runnable accion) {
        Button btn = new Button(idioma.get("ui.btn.edit"));
        btn.setStyle(
                "-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: bold;");
        btn.setOnAction(e -> accion.run());
        return btn;
    }

    /**
     * Crea un botón para las tablas de datos, con estilos definidos en AppTheme. El texto del botón es dinámico y se puede adaptar a diferentes idiomas gracias al gestor de idioma. El botón se utiliza para acciones como editar o eliminar registros dentro de las tablas, y su estilo se diferencia según la acción que representa (editar con colores azules y eliminar con colores rojos).
     * @param accion
     * @return
     */
    public static Button crearBotonTablaEliminar(Runnable accion) {
        Button btn = new Button(idioma.get("ui.btn.delete"));
        btn.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-cursor: hand; -fx-font-size: 11px;");
        btn.setOnAction(e -> accion.run());
        return btn;
    }

    /**
     * Crea un contenedor para mostrar datos en las pantallas de detalle, utilizando un VBox con estilos definidos en AppTheme. El contenedor incluye un label para la etiqueta del dato y otro label para el valor del dato. El texto de la etiqueta es dinámico y se puede adaptar a diferentes idiomas gracias al gestor de idioma. Si el valor del dato es nulo o vacío, se muestra un guion (-) para indicar que no hay información disponible.
     * @param etiqueta
     * @param valor
     * @return
     */
    public static VBox crearDatoDetalle(String etiqueta, String valor) {
        Label l = new Label(etiqueta);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        Label v = new Label(valor != null && !valor.isEmpty() ? valor : "-");
        v.setStyle("-fx-text-fill: #4B5563; -fx-wrap-text: true;");
        v.setMaxWidth(200);

        return new VBox(2, l, v);
    }

    /**
     * Configura un Stage para que se comporte como un modal, bloqueando la interacción con la ventana principal mientras el modal esté abierto. El modal se muestra de forma transparente y centrada sobre la ventana principal, utilizando estilos definidos en AppTheme para mantener la coherencia visual de la aplicación.
     * @param dialogStage
     * @param ownerStage
     */
    public static void configurarStageModal(Stage dialogStage, Stage ownerStage) {
        dialogStage.initOwner(ownerStage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
    }

    /**
     * Crea una tabla para mostrar relaciones entre entidades, utilizando un TableView con estilos definidos en AppTheme. La tabla incluye dos columnas para mostrar los valores de las entidades relacionadas, y opcionalmente una columna adicional con un botón para eliminar la relación. Los títulos de las columnas y los valores que se muestran en cada fila son dinámicos y se pueden adaptar a diferentes idiomas gracias al gestor de idioma.
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

    /**
     * Crea una columna para un TableView, utilizando un TableColumn con estilos definidos en AppTheme. El título de la columna y los valores que se muestran en cada fila son dinámicos y se pueden adaptar a diferentes idiomas gracias al gestor de idioma. La columna se configura para mostrar los datos extraídos de los objetos de la tabla utilizando una función proporcionada como parámetro, lo que permite una gran flexibilidad para mostrar diferentes tipos de datos en las tablas de la aplicación.
     * @param <S>
     * @param <T>
     * @param titulo
     * @param extractor
     * @param minWidth
     * @return
     */
    public static <S, T> TableColumn<S, T> crearColumna(String titulo, Function<S, T> extractor, int minWidth) {
        TableColumn<S, T> col = new TableColumn<>(titulo);
        col.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(extractor.apply(data.getValue())));
        if (minWidth > 0) {
            col.setMinWidth(minWidth);
        }
        return col;
    }
}