package com.vluevano.view;

import com.vluevano.model.Venta;
import com.vluevano.service.VentaService;
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

import com.vluevano.service.DialogService;
import com.vluevano.service.PdfService;
import javafx.stage.FileChooser;
import java.io.File;

@Component
public class HistorialVentasView {

    @Autowired
    private VentaService ventaService;

    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private DialogService dialogService;

    private String usuarioActual;
    private TableView<Venta> tabla;
    private Stage stage;

    /**
     * Muestra la pantalla de historial de ventas
     * 
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        BorderPane root = new BorderPane();

        root.setTop(UIFactory.crearHeader("Historial de Ventas", "Consulta y Reportes",
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        root.setCenter(crearContenidoPrincipal());

        Scene scene = new Scene(root, 1200, 768);
        stage.setScene(scene);
        stage.setTitle("PriceStocker | Historial");
        stage.show();
    }

    /**
     * Crea el contenido principal de la pantalla
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

        DatePicker dpInicio = new DatePicker(LocalDate.now().minusDays(7));
        DatePicker dpFin = new DatePicker(LocalDate.now());

        Button btnBuscar = UIFactory.crearBotonPrimario("Filtrar Resultados");

        Button btnReporte = new Button("Exportar Reporte (PDF)");
        btnReporte.setStyle(
                "-fx-background-color: #1E7145; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15;");

        btnReporte.setOnAction(e -> {
            if (tabla.getItems().isEmpty()) {
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos",
                        "No hay datos en la tabla para exportar.", stage);
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Guardar Reporte de Ventas");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos PDF", "*.pdf"));
            fileChooser.setInitialFileName("Reporte_Ventas_" + LocalDate.now() + ".pdf");

            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                try {
                    String rango = "Del " + dpInicio.getValue() + " al " + dpFin.getValue();
                    pdfService.generarReporteVentas(file, tabla.getItems(), rango);

                    dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                            "Reporte guardado exitosamente en: " + file.getName(), stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error de Exportación",
                            "No se pudo generar el PDF: " + ex.getMessage(), stage);
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

        TableColumn<Venta, String> colId = new TableColumn<>("Folio");
        colId.setCellValueFactory(v -> new SimpleStringProperty(String.valueOf(v.getValue().getIdventa())));

        TableColumn<Venta, String> colFecha = new TableColumn<>("Fecha");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colFecha.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getFechaVenta().format(fmt)));

        TableColumn<Venta, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(v -> new SimpleStringProperty(
                v.getValue().getCliente() != null ? v.getValue().getCliente().getNombreCliente() : "Público General"));

        TableColumn<Venta, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getUsuario().getNombreUsuario()));

        TableColumn<Venta, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(
                v -> new SimpleStringProperty(String.format("$%.2f", v.getValue().getTotalVenta())));
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tabla.getColumns().addAll(colId, colFecha, colCliente, colUsuario, colTotal);

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
     * Carga los datos de ventas en la tabla según el rango de fechas seleccionado
     * 
     * @param inicio
     * @param fin
     */
    private void cargarDatos(LocalDate inicio, LocalDate fin) {
        List<Venta> ventas = ventaService.obtenerVentasPorRango(inicio, fin);
        tabla.setItems(FXCollections.observableArrayList(ventas));

        if (ventas.isEmpty()) {
            tabla.setPlaceholder(new Label("No se encontraron ventas en este rango de fechas."));
        }
    }
}