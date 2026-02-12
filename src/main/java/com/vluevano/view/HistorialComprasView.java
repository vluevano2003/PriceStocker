package com.vluevano.view;

import com.vluevano.model.Compra;
import com.vluevano.service.CompraService;
import com.vluevano.service.DialogService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.vluevano.service.PdfService;
import javafx.stage.FileChooser;
import java.io.File;

@Component
public class HistorialComprasView {

    @Autowired
    private CompraService compraService;

    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private DialogService dialogService;

    private String usuarioActual;
    private TableView<Compra> tabla;
    private Stage stage;

    /**
     * Muestra la pantalla de historial de compras
     * 
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        BorderPane root = new BorderPane();

        root.setTop(UIFactory.crearHeader("Historial de Compras", "Entradas de inventario y gastos",
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        root.setCenter(crearContenidoPrincipal());

        Scene scene = new Scene(root, 1200, 768);
        stage.setScene(scene);
        stage.setTitle("PriceStocker | Historial de Compras");
        stage.show();
    }

    /**
     * Crea el contenido principal de la pantalla, incluyendo la barra de filtros y
     * la tabla de resultados
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearContenidoPrincipal() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        HBox filtros = new HBox(15);
        filtros.setStyle(AppTheme.STYLE_CARD);
        filtros.setPadding(new Insets(20));
        filtros.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        DatePicker dpInicio = new DatePicker(LocalDate.now().minusDays(30));
        DatePicker dpFin = new DatePicker(LocalDate.now());

        Button btnBuscar = UIFactory.crearBotonPrimario("Filtrar Resultados");

        Button btnReporte = new Button("Exportar Reporte (PDF)");
        btnReporte.setStyle(
                "-fx-background-color: #1E7145; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15;");

        btnReporte.setOnAction(e -> {
            if (tabla.getItems().isEmpty()) {
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Tabla Vacía",
                        "No hay compras registradas en este periodo para exportar.", stage);
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Compras");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
            fileChooser.setInitialFileName("Reporte_Compras_" + LocalDate.now() + ".pdf");

            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try {
                    String rango = "Del " + dpInicio.getValue() + " al " + dpFin.getValue();
                    pdfService.generarReporteCompras(file, tabla.getItems(), rango);

                    dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Reporte Generado",
                            "El archivo se ha guardado correctamente.", stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error",
                            "Ocurrió un fallo al crear el PDF: " + ex.getMessage(), stage);
                }
            }
        });

        filtros.getChildren().addAll(
                new Label("Desde:"), dpInicio,
                new Label("Hasta:"), dpFin,
                btnBuscar,
                new Region(),
                btnReporte);
        HBox.setHgrow(filtros.getChildren().get(5), Priority.ALWAYS);

        tabla = new TableView<>();
        String estiloThumb = "data:text/css," +
                ".scroll-bar:vertical .thumb {" +
                "    -fx-background-color: #DADADA;" +
                "    -fx-background-insets: 0 4 0 4;" +
                "    -fx-background-radius: 4;" +
                "}" +
                ".scroll-bar:horizontal .thumb {" +
                "    -fx-background-color: #DADADA;" +
                "    -fx-background-insets: 4 0 4 0;" +
                "    -fx-background-radius: 4;" +
                "}";

        tabla.getStylesheets().add(estiloThumb);
        tabla.setStyle(
                "-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Compra, String> colId = new TableColumn<>("ID Compra");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdcompra())));

        TableColumn<Compra, String> colFecha = new TableColumn<>("Fecha");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaCompra().format(fmt)));

        TableColumn<Compra, String> colOrigen = new TableColumn<>("Origen (Prov./Fab.)");
        colOrigen.setMinWidth(250);
        colOrigen.setCellValueFactory(c -> {
            String origen = "Desconocido";
            if (c.getValue().getProveedor() != null) {
                origen = "[P] " + c.getValue().getProveedor().getNombreProv();
            } else if (c.getValue().getFabricante() != null) {
                origen = "[F] " + c.getValue().getFabricante().getNombreFabricante();
            }
            return new SimpleStringProperty(origen);
        });

        TableColumn<Compra, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsuario().getNombreUsuario()));

        TableColumn<Compra, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(
                c -> new SimpleStringProperty(String.format("$%.2f", c.getValue().getTotalCompra())));
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tabla.getColumns().addAll(colId, colFecha, colOrigen, colUsuario, colTotal);

        btnBuscar.setOnAction(e -> {
            if (dpInicio.getValue() != null && dpFin.getValue() != null) {
                cargarDatos(dpInicio.getValue(), dpFin.getValue());
            }
        });

        cargarDatos(LocalDate.now().minusDays(30), LocalDate.now());

        layout.getChildren().addAll(filtros, tabla);
        return layout;
    }

    /**
     * Carga las compras realizadas en el rango de fechas seleccionado y las muestra
     * en la tabla. Si no hay compras, muestra un mensaje indicándolo
     * 
     * @param inicio
     * @param fin
     */
    private void cargarDatos(LocalDate inicio, LocalDate fin) {
        List<Compra> compras = compraService.obtenerComprasPorRango(inicio, fin);
        tabla.setItems(FXCollections.observableArrayList(compras));

        if (compras.isEmpty()) {
            tabla.setPlaceholder(new Label("No se encontraron compras en este rango de fechas."));
        }
    }
}