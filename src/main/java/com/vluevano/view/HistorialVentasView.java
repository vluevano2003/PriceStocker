package com.vluevano.view;

import com.vluevano.model.DetalleVenta;
import com.vluevano.model.Venta;
import com.vluevano.service.VentaService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.util.stream.Collectors;
import java.io.File;
import com.vluevano.service.DialogService;
import com.vluevano.service.PdfService;
import javafx.stage.FileChooser;

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
    private Label lblPlaceholderDefault;
    private Label lblPlaceholderFiltro;

    private TextField txtBusqueda;

    /**
     * Muestra la vista de historial de ventas en el escenario principal,
     * configurando
     * 
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;
        BorderPane root = new BorderPane();
        root.setTop(UIFactory.crearHeader("Historial de Ventas", "Consulta y reportes de ventas realizadas",
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));
        root.setCenter(crearContenidoPrincipal());
        Scene scene = new Scene(root, 1200, 768);
        stage.setScene(scene);
        stage.setTitle("PriceStocker | Historial");
        stage.show();
    }

    /**
     * Crea el contenido principal de la vista, incluyendo los filtros y la tabla de
     * ventas
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearContenidoPrincipal() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        inicializarPlaceholders();

        HBox filtros = new HBox(15);
        filtros.setStyle(AppTheme.STYLE_CARD);
        filtros.setPadding(new Insets(20));
        filtros.setAlignment(Pos.CENTER_LEFT);

        txtBusqueda = UIFactory.crearInput("Cliente, Usuario o Folio...");
        txtBusqueda.setPrefWidth(250);

        DatePicker dpInicio = new DatePicker(LocalDate.now().minusDays(7));
        DatePicker dpFin = new DatePicker(LocalDate.now());
        Button btnBuscar = UIFactory.crearBotonPrimario("Filtrar Resultados");

        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> {
            if (dpInicio.getValue() != null && dpFin.getValue() != null) {
                cargarDatos(dpInicio.getValue(), dpFin.getValue(), true);
            }
        });

        Button btnReporte = new Button("Exportar Reporte (PDF)");
        btnReporte.setStyle(
                "-fx-background-color: #1E7145; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15;");
        btnReporte.setOnAction(e -> {
            if (tabla.getItems().isEmpty()) {
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Sin datos", "No hay datos para exportar.", stage);
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
                            "Reporte guardado: " + file.getName(), stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error",
                            "Error al crear PDF: " + ex.getMessage(), stage);
                }
            }
        });

        filtros.getChildren().addAll(
                new Label("Buscar:"), txtBusqueda,
                new Label("Desde:"), dpInicio,
                new Label("Hasta:"), dpFin,
                btnBuscar,
                new Region(),
                btnReporte);
        HBox.setHgrow(filtros.getChildren().get(7), Priority.ALWAYS);

        tabla = new TableView<>();
        tabla.setStyle(
                "-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Venta, String> colId = new TableColumn<>("Folio");
        colId.setCellValueFactory(v -> new SimpleStringProperty(String.valueOf(v.getValue().getIdventa())));
        colId.setMinWidth(60);
        colId.setMaxWidth(60);

        TableColumn<Venta, String> colFecha = new TableColumn<>("Fecha");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colFecha.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getFechaVenta().format(fmt)));
        colFecha.setMinWidth(140);
        colFecha.setMaxWidth(140);

        TableColumn<Venta, Void> colDetalles = new TableColumn<>("Contenido");
        colDetalles.setMinWidth(140);
        colDetalles.setMaxWidth(140);
        colDetalles.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("Ver Productos");
            {
                btnVer.setStyle("-fx-background-color: " + AppTheme.COLOR_PRIMARY
                        + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-radius: 4;");
                btnVer.setOnAction(event -> mostrarPopUpDetalles(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Venta v = getTableView().getItems().get(getIndex());
                    int items = (v.getDetalles() != null) ? v.getDetalles().size() : 0;
                    btnVer.setText("Ver (" + items + ") Productos");
                    setGraphic(btnVer);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Venta, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setMinWidth(200);
        colCliente.setCellValueFactory(v -> new SimpleStringProperty(
                v.getValue().getCliente() != null ? v.getValue().getCliente().getNombreCliente() : "Público General"));

        TableColumn<Venta, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getUsuario().getNombreUsuario()));
        colUsuario.setMinWidth(120);
        colUsuario.setMaxWidth(120);

        TableColumn<Venta, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(
                v -> new SimpleStringProperty(String.format("$%.2f", v.getValue().getTotalVenta())));
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colTotal.setMinWidth(120);
        colTotal.setMaxWidth(120);

        tabla.getColumns().addAll(colId, colFecha, colDetalles, colCliente, colUsuario, colTotal);

        btnBuscar.setOnAction(e -> {
            if (dpInicio.getValue() != null && dpFin.getValue() != null)
                cargarDatos(dpInicio.getValue(), dpFin.getValue(), true);
        });
        cargarDatos(LocalDate.now().minusDays(30), LocalDate.now(), false);

        layout.getChildren().addAll(filtros, tabla);
        return layout;
    }

    /**
     * Muestra un pop-up con los detalles de una venta específica
     * 
     * @param venta
     */
    @SuppressWarnings("unchecked")
    private void mostrarPopUpDetalles(Venta venta) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        root.setMinWidth(500);
        root.setMinHeight(400);

        Label lblTitulo = new Label("Detalle de Venta #" + venta.getIdventa());
        lblTitulo
                .setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");

        TableView<DetalleVenta> tableDetalles = new TableView<>();

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

        tableDetalles.getStylesheets().add(estiloThumb);

        tableDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableDetalles.setStyle(
                "-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        tableDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<DetalleVenta, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto().getNombreProducto()));

        TableColumn<DetalleVenta, String> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));
        colCant.setStyle("-fx-alignment: CENTER;");

        TableColumn<DetalleVenta, String> colPrecio = new TableColumn<>("P. Unitario");
        colPrecio.setCellValueFactory(
                d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getPrecioUnitario())));
        colPrecio.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<DetalleVenta, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getSubtotal())));
        colSub.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tableDetalles.getColumns().addAll(colProd, colCant, colPrecio, colSub);

        if (venta.getDetalles() != null) {
            tableDetalles.setItems(FXCollections.observableArrayList(venta.getDetalles()));
        }
        VBox.setVgrow(tableDetalles, Priority.ALWAYS);

        Button btnCerrar = UIFactory.crearBotonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());
        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, new Separator(), tableDetalles, footer);

        dialogService.mostrarDialogoModal(dialog, root, stage);
    }

    /**
     * Inicializa los placeholders para la tabla, diferenciando entre ausencia total
     * de datos y ausencia por filtro
     */
    private void inicializarPlaceholders() {
        String estiloBase = "-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;";
        lblPlaceholderDefault = new Label("No hay ventas registradas.");
        lblPlaceholderDefault.setStyle(estiloBase);
        lblPlaceholderFiltro = new Label("No se encontraron ventas con los criterios.");
        lblPlaceholderFiltro.setStyle(estiloBase);
    }

    /**
     * Carga los datos de ventas en la tabla según el rango de fechas y el texto de
     * búsqueda, aplicando filtros en memoria
     * 
     * @param inicio
     * @param fin
     * @param esFiltro
     */
    private void cargarDatos(LocalDate inicio, LocalDate fin, boolean esFiltro) {
        tabla.setPlaceholder(esFiltro ? lblPlaceholderFiltro : lblPlaceholderDefault);

        List<Venta> ventas = ventaService.obtenerVentasPorRango(inicio, fin);

        String busqueda = txtBusqueda.getText().trim().toLowerCase();

        if (!busqueda.isEmpty()) {
            ventas = ventas.stream()
                    .filter(v -> {
                        boolean matchId = String.valueOf(v.getIdventa()).contains(busqueda);
                        
                        boolean matchCliente = v.getCliente() != null &&
                                v.getCliente().getNombreCliente().toLowerCase().contains(busqueda);
                        boolean matchUsuario = v.getUsuario() != null &&
                                v.getUsuario().getNombreUsuario().toLowerCase().contains(busqueda);

                        return matchId || matchCliente || matchUsuario;
                    })
                    .collect(Collectors.toList());
        }

        tabla.setItems(FXCollections.observableArrayList(ventas));
    }
}