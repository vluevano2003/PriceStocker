package com.vluevano.view;

import com.vluevano.model.Compra;
import com.vluevano.service.CompraService;
import com.vluevano.service.DialogService;
import com.vluevano.service.MonedaService;
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
import com.vluevano.model.DetalleCompra;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private MonedaService monedaService;

    private String usuarioActual;
    private TableView<Compra> tabla;
    private Stage stage;

    private Label lblPlaceholderDefault;
    private Label lblPlaceholderFiltro;

    private TextField txtBusqueda;

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

        root.setTop(UIFactory.crearHeader("Historial de Compras", "Consulta y reportes de compras realizadas",
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        root.setCenter(crearContenidoPrincipal());

        Scene scene = new Scene(root, 1200, 768);
        stage.setScene(scene);
        stage.setTitle("PriceStocker | Historial de Compras");
        stage.show();
    }

    /**
     * Crea el contenido principal de la pantalla, incluyendo filtros y tabla de
     * resultados
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

        txtBusqueda = UIFactory.crearInput("Proveedor, Fabricante, Usuario...");
        txtBusqueda.setPrefWidth(260);

        DatePicker dpInicio = new DatePicker(LocalDate.now().minusDays(30));
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
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Tabla Vacía", "No hay compras para exportar.",
                        stage);
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
                            "Guardado correctamente.", stage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error",
                            "Fallo al crear PDF: " + ex.getMessage(), stage);
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
        String estiloThumb = "data:text/css,.scroll-bar:vertical .thumb {-fx-background-color: #DADADA;-fx-background-insets: 0 4 0 4;-fx-background-radius: 4;}.scroll-bar:horizontal .thumb {-fx-background-color: #DADADA;-fx-background-insets: 4 0 4 0;-fx-background-radius: 4;}";
        tabla.getStylesheets().add(estiloThumb);
        tabla.setStyle(
                "-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        TableColumn<Compra, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getIdcompra())));
        colId.setMinWidth(60);
        colId.setMaxWidth(60);

        TableColumn<Compra, String> colFecha = new TableColumn<>("Fecha");
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaCompra().format(fmt)));
        colFecha.setMinWidth(140);
        colFecha.setMaxWidth(140);

        TableColumn<Compra, Void> colDetalles = new TableColumn<>("Contenido");
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
                    Compra c = getTableView().getItems().get(getIndex());
                    int items = (c.getDetalles() != null) ? c.getDetalles().size() : 0;
                    btnVer.setText("Ver (" + items + ") Items");
                    setGraphic(btnVer);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Compra, String> colOrigen = new TableColumn<>("Origen (Prov./Fab.)");
        colOrigen.setMinWidth(140);
        colOrigen.setCellValueFactory(c -> {
            if (c.getValue().getProveedor() != null)
                return new SimpleStringProperty("[P] " + c.getValue().getProveedor().getNombreProv());
            else if (c.getValue().getFabricante() != null)
                return new SimpleStringProperty("[F] " + c.getValue().getFabricante().getNombreFabricante());
            return new SimpleStringProperty("Desconocido");
        });

        TableColumn<Compra, String> colUsuario = new TableColumn<>("Usuario");
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsuario().getNombreUsuario()));
        colUsuario.setMinWidth(120);
        colUsuario.setMaxWidth(120);

        TableColumn<Compra, String> colTotal = new TableColumn<>("Total");
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(
                formatearPrecioInteligente(c.getValue().getTotalCompra(), c.getValue().getMoneda())));
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colTotal.setMinWidth(240);
        colTotal.setMaxWidth(280);

        tabla.getColumns().addAll(colId, colFecha, colDetalles, colOrigen, colUsuario, colTotal);

        btnBuscar.setOnAction(e -> {
            if (dpInicio.getValue() != null && dpFin.getValue() != null)
                cargarDatos(dpInicio.getValue(), dpFin.getValue(), true);
        });
        cargarDatos(LocalDate.now().minusDays(30), LocalDate.now(), false);

        layout.getChildren().addAll(filtros, tabla);
        return layout;
    }

    /**
     * Muestra un pop-up con los detalles de los productos incluidos en la compra
     * seleccionada
     * 
     * @param compra
     */
    @SuppressWarnings("unchecked")
    private void mostrarPopUpDetalles(Compra compra) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        root.setMinWidth(700);
        root.setMinHeight(400);

        Label lblTitulo = new Label("Detalle de Compra #" + compra.getIdcompra());
        lblTitulo
                .setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");

        TableView<DetalleCompra> tableDetalles = new TableView<>();

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

        TableColumn<DetalleCompra, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto().getNombreProducto()));

        TableColumn<DetalleCompra, String> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));
        colCant.setStyle("-fx-alignment: CENTER;");

        TableColumn<DetalleCompra, String> colCosto = new TableColumn<>("Costo U.");
        colCosto.setCellValueFactory(d -> new SimpleStringProperty(
                formatearPrecioInteligente(d.getValue().getCostoUnitario(), compra.getMoneda())));
        colCosto.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCosto.setMinWidth(180);

        TableColumn<DetalleCompra, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d -> new SimpleStringProperty(
                formatearPrecioInteligente(d.getValue().getSubtotal(), compra.getMoneda())));
        colSub.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colSub.setMinWidth(180);

        tableDetalles.getColumns().addAll(colProd, colCant, colCosto, colSub);

        if (compra.getDetalles() != null) {
            tableDetalles.setItems(FXCollections.observableArrayList(compra.getDetalles()));
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
     * Inicializa los placeholders para la tabla, con estilos consistentes y
     * mensajes claros para el usuario
     */
    private void inicializarPlaceholders() {
        String estiloBase = "-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;";

        lblPlaceholderDefault = new Label("No hay compras registradas en el sistema.");
        lblPlaceholderDefault.setStyle(estiloBase);

        lblPlaceholderFiltro = new Label("No se encontraron compras con los criterios seleccionados.");
        lblPlaceholderFiltro.setStyle(estiloBase);
    }

    /**
     * Carga los datos de compras según el rango de fechas seleccionado y el texto
     * de búsqueda, aplicando ambos filtros de manera eficiente
     * 
     * @param inicio
     * @param fin
     * @param esFiltro
     */
    private void cargarDatos(LocalDate inicio, LocalDate fin, boolean esFiltro) {
        if (esFiltro) {
            tabla.setPlaceholder(lblPlaceholderFiltro);
        } else {
            tabla.setPlaceholder(lblPlaceholderDefault);
        }

        List<Compra> compras = compraService.obtenerComprasPorRango(inicio, fin);

        String busqueda = txtBusqueda.getText().trim().toLowerCase();

        if (!busqueda.isEmpty()) {
            compras = compras.stream()
                    .filter(c -> {
                        boolean matchId = String.valueOf(c.getIdcompra()).contains(busqueda);

                        boolean matchUsuario = c.getUsuario() != null &&
                                c.getUsuario().getNombreUsuario().toLowerCase().contains(busqueda);

                        boolean matchProveedor = c.getProveedor() != null &&
                                c.getProveedor().getNombreProv().toLowerCase().contains(busqueda);

                        boolean matchFabricante = c.getFabricante() != null &&
                                c.getFabricante().getNombreFabricante().toLowerCase().contains(busqueda);

                        return matchId || matchUsuario || matchProveedor || matchFabricante;
                    })
                    .collect(Collectors.toList());
        }

        tabla.setItems(FXCollections.observableArrayList(compras));
    }

    private String formatearPrecioInteligente(double precio, String monedaItem) {
        if (monedaItem == null)
            monedaItem = "MXN";
        String monedaPref = monedaService.getMonedaPorDefecto();
        String textoOriginal = String.format("$%.2f %s", precio, monedaItem);

        if (monedaItem.equalsIgnoreCase(monedaPref))
            return textoOriginal;
        try {
            double tipoCambio = monedaService.convertirAMxn(1.0, "USD");
            if (tipoCambio == 0)
                tipoCambio = 20.0;
            double precioConvertido = monedaPref.equalsIgnoreCase("MXN") ? (precio * tipoCambio)
                    : (precio / tipoCambio);
            return String.format("%s (≈ $%.2f %s)", textoOriginal, precioConvertido, monedaPref);
        } catch (Exception e) {
            return textoOriginal;
        }
    }
}