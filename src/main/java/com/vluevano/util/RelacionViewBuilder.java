package com.vluevano.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.function.Function;

public class RelacionViewBuilder {

    private static GestorIdioma idioma;

    /**
     * Inyección del gestor de idioma para obtener textos dinámicos en la interfaz
     * @param gestor
     */
    @Autowired
    public void setGestorIdioma(GestorIdioma gestor) {
        RelacionViewBuilder.idioma = gestor;
    }

    /**
     * Interfaz funcional para crear objetos de relación entre dos entidades, recibiendo el item seleccionado del catálogo, el costo y la moneda
     * @param <T> Tipo del item seleccionado del catálogo
     */
    @FunctionalInterface
    public interface RelacionFactory<T, R> {
        R crear(T itemSeleccionado, Double costo, String moneda);
    }

    /**
     * Crea un panel de gestión de relaciones entre dos entidades, con un catálogo para seleccionar, un campo de costo y una tabla para mostrar los agregados
     * @param <T>
     * @param <R>
     * @param catalogoDisponible
     * @param listaAgregados
     * @param extractorNombreCatalogo
     * @param labelNombre
     * @param labelCosto
     * @param factoryRelacion
     * @param extractorNombreRelacion
     * @param extractorPrecioRelacion
     * @return
     */
    public static <T, R> VBox crearPanelGestion(
            List<T> catalogoDisponible,
            ObservableList<R> listaAgregados,
            Function<T, String> extractorNombreCatalogo,
            String labelNombre,
            String labelCosto,
            RelacionFactory<T, R> factoryRelacion,
            Function<R, String> extractorNombreRelacion,
            Function<R, String> extractorPrecioRelacion
    ) {
        VBox box = new VBox(15);

        if (catalogoDisponible.isEmpty()) {
            Label lblVacio = new Label(idioma.get("relacion.msg.empty", labelNombre));
            lblVacio.setStyle("-fx-text-fill: #EF4444; -fx-font-style: italic; -fx-font-size: 13px;");
            box.getChildren().add(lblVacio);
            return box;
        }

        ComboBox<T> cmb = new ComboBox<>(FXCollections.observableArrayList(catalogoDisponible));
        configurarCombo(cmb, extractorNombreCatalogo);
        cmb.setPromptText(idioma.get("relacion.prompt.select", labelNombre));
        cmb.setMaxWidth(Double.MAX_VALUE);

        TextField txtCosto = UIFactory.crearInput(labelCosto);
        ComboBox<String> cmbMoneda = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMoneda.setStyle(AppTheme.STYLE_INPUT);
        cmbMoneda.getSelectionModel().selectFirst();
        cmbMoneda.setPrefWidth(150);

        Button btnAdd = new Button(idioma.get("relacion.btn.add"));
        btnAdd.setStyle(
                "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-font-weight: bold; -fx-background-radius: 6; -fx-border-color: #A7F3D0; -fx-border-radius: 6; -fx-cursor: hand;");

        btnAdd.setOnAction(e -> {
            if (cmb.getValue() != null && !txtCosto.getText().isEmpty()) {
                try {
                    Double costoVal = Double.parseDouble(txtCosto.getText());
                    R nuevoObjeto = factoryRelacion.crear(cmb.getValue(), costoVal, cmbMoneda.getValue());
                    listaAgregados.add(nuevoObjeto);
                    txtCosto.clear();
                } catch (NumberFormatException ex) {
                }
            }
        });

        HBox controls = new HBox(10,
                UIFactory.crearGrupoInput(labelCosto, txtCosto),
                UIFactory.crearGrupoInput(idioma.get("relacion.lbl.currency"), cmbMoneda),
                new VBox(19, new Label(""), btnAdd));
        controls.setAlignment(Pos.BOTTOM_LEFT);

        TableView<R> tabla = UIFactory.crearTablaRelacion(
                listaAgregados,
                labelNombre, extractorNombreRelacion,
                idioma.get("relacion.lbl.price_cost"), extractorPrecioRelacion,
                true
        );

        box.getChildren().addAll(UIFactory.crearGrupoInput(labelNombre, cmb), controls, tabla);
        return box;
    }

    /**
     * Configura un ComboBox para mostrar objetos complejos usando un extractor de texto
     * @param <T>
     * @param combo
     * @param textExtractor
     */
    private static <T> void configurarCombo(ComboBox<T> combo, Function<T, String> textExtractor) {
        combo.setStyle(AppTheme.STYLE_INPUT);
        StringConverter<T> converter = new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object == null ? null : textExtractor.apply(object);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        };
        combo.setConverter(converter);
        combo.setCellFactory(lv -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textExtractor.apply(item));
            }
        });
        combo.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textExtractor.apply(item));
            }
        });
    }
}