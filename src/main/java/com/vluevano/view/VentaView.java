package com.vluevano.view;

import com.vluevano.model.*;
import com.vluevano.service.*;
import com.vluevano.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.function.UnaryOperator;

@Component
public class VentaView {

    @Autowired
    private VentaService ventaService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private DialogService dialogService;
    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;

    private TableView<DetalleVenta> tablaDetalles;
    private ObservableList<DetalleVenta> listaDetalles;
    private ComboBox<Cliente> cmbCliente;
    private ComboBox<Producto> cmbProducto;
    private TextField txtCantidad;
    private TextField txtPrecioVenta;
    private Label lblTotal;
    private Label lblStockActual;

    /**
     * Muestra la pantalla de venta
     * 
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;
        this.listaDetalles = FXCollections.observableArrayList();

        BorderPane root = new BorderPane();
        root.setTop(UIFactory.crearHeader("Nueva Venta", "Registro de salida de mercancía",
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        HBox contenido = new HBox(20);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        VBox panelIzquierdo = crearPanelControl();

        VBox panelDerecho = crearPanelDetalle();
        HBox.setHgrow(panelDerecho, Priority.ALWAYS);

        contenido.getChildren().addAll(panelIzquierdo, panelDerecho);
        root.setCenter(contenido);

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("PriceStocker | Punto de Venta");
        stage.show();

        cargarCatalogos();
    }

    /**
     * Crea el panel de control para seleccionar cliente, producto, cantidad y
     * precio
     * 
     * @return
     */
    private VBox crearPanelControl() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle(AppTheme.STYLE_CARD);
        box.setMinWidth(350);
        box.setMaxWidth(350);

        cmbCliente = new ComboBox<>();
        cmbCliente.setMaxWidth(Double.MAX_VALUE);
        cmbCliente.setStyle(AppTheme.STYLE_INPUT);

        cmbCliente.setConverter(new javafx.util.StringConverter<Cliente>() {
            @Override
            public String toString(Cliente c) {
                return (c != null) ? c.getNombreCliente() : "";
            }

            @Override
            public Cliente fromString(String string) {
                return null;
            }
        });

        cmbCliente.setOnAction(e -> buscarPrecioPersonalizado());

        cmbProducto = new ComboBox<>();
        cmbProducto.setMaxWidth(Double.MAX_VALUE);
        cmbProducto.setStyle(AppTheme.STYLE_INPUT);

        cmbProducto.setConverter(new javafx.util.StringConverter<Producto>() {
            @Override
            public String toString(Producto p) {
                return (p != null) ? p.getNombreProducto() : "";
            }

            @Override
            public Producto fromString(String string) {
                return null;
            }
        });

        cmbProducto.setOnAction(e -> actualizarInfoProducto());

        txtCantidad = UIFactory.crearInput("1");

        txtPrecioVenta = UIFactory.crearInput("0.00");
        UnaryOperator<TextFormatter.Change> filterDouble = change -> {
            String text = change.getControlNewText();
            if (text.matches("\\d*|\\d+\\.\\d*")) {
                return change;
            }
            return null;
        };
        txtPrecioVenta.setTextFormatter(new TextFormatter<>(filterDouble));

        lblStockActual = new Label("Stock disponible: -");
        lblStockActual.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Button btnAgregar = UIFactory.crearBotonPrimario("Agregar al Carrito");
        btnAgregar.setMaxWidth(Double.MAX_VALUE);
        btnAgregar.setOnAction(e -> agregarProducto());

        box.getChildren().addAll(
                UIFactory.crearTituloSeccion("Datos del Cliente"),
                UIFactory.crearGrupoInput("Seleccionar Cliente (Opcional)", cmbCliente),
                new Separator(),
                UIFactory.crearTituloSeccion("Agregar Producto"),
                UIFactory.crearGrupoInput("Buscar Producto", cmbProducto),
                lblStockActual,
                UIFactory.crearGrupoInput("Precio Unitario", txtPrecioVenta),
                UIFactory.crearGrupoInput("Cantidad", txtCantidad),
                btnAgregar);
        return box;
    }

    /**
     * Crea el panel derecho con la tabla de detalles y el total
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelDetalle() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle(AppTheme.STYLE_CARD);

        tablaDetalles = new TableView<>();
        tablaDetalles.setItems(listaDetalles);

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

        tablaDetalles.getStylesheets().add(estiloThumb);

        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        tablaDetalles.setStyle(
                "-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        TableColumn<DetalleVenta, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto().getNombreProducto()));

        TableColumn<DetalleVenta, String> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));

        TableColumn<DetalleVenta, String> colPrecio = new TableColumn<>("Precio U.");
        colPrecio.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().getPrecioUnitario()));

        TableColumn<DetalleVenta, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().getSubtotal()));

        TableColumn<DetalleVenta, Void> colAccion = new TableColumn<>("");
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btn = UIFactory.crearBotonTablaEliminar(() -> {
                listaDetalles.remove(getIndex());
                calcularTotalGeneral();
            });

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tablaDetalles.getColumns().addAll(colProd, colCant, colPrecio, colSub, colAccion);
        VBox.setVgrow(tablaDetalles, Priority.ALWAYS);

        lblTotal = new Label("Total: $0.00");
        lblTotal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY);

        Button btnFinalizar = UIFactory.crearBotonPrimario("FINALIZAR VENTA");
        btnFinalizar.setPrefHeight(50);
        btnFinalizar.setPrefWidth(200);
        btnFinalizar.setOnAction(e -> procesarVenta());

        HBox footer = new HBox(20, lblTotal, new Region(), btnFinalizar);
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);
        footer.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(new Label("Detalle de la Venta"), tablaDetalles, new Separator(), footer);
        return box;
    }

    /**
     * Carga los clientes y productos en los ComboBox al iniciar la pantalla
     */
    private void cargarCatalogos() {
        cmbCliente.getItems().setAll(clienteService.consultarClientes());
        cmbProducto.getItems().setAll(productoService.consultarProductos());
    }

    /**
     * Actualiza el stock disponible y el precio sugerido al cambiar de producto
     */
    private void actualizarInfoProducto() {
        Producto p = cmbProducto.getValue();
        if (p != null) {
            lblStockActual.setText("Stock disponible: " + p.getExistenciaProducto());
            buscarPrecioPersonalizado();
        } else {
            txtPrecioVenta.setText("0.00");
        }
    }

    /**
     * Busca el precio personalizado para el producto-cliente seleccionado y lo
     * muestra en el campo editable
     */
    private void buscarPrecioPersonalizado() {
        Producto p = cmbProducto.getValue();
        Cliente c = cmbCliente.getValue();

        if (p != null) {
            Double precioSugerido = ventaService.obtenerPrecioVenta(p, c);
            if (precioSugerido == null)
                precioSugerido = 0.0;
            txtPrecioVenta.setText(String.valueOf(precioSugerido));
        }
    }

    /**
     * Agrega el producto seleccionado al carrito, validando cantidad, stock y
     * precio. Si el producto ya está en el carrito, actualiza la cantidad y
     * subtotal
     */
    private void agregarProducto() {
        Producto p = cmbProducto.getValue();
        if (p == null)
            return;

        try {
            int cantidadInput = Integer.parseInt(txtCantidad.getText());
            double precioFinal = Double.parseDouble(txtPrecioVenta.getText());

            if (cantidadInput <= 0)
                throw new NumberFormatException("Cantidad negativa");

            if (precioFinal < 0) {
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Precio inválido",
                        "El precio no puede ser negativo.", stage);
                return;
            }

            DetalleVenta detalleExistente = listaDetalles.stream()
                    .filter(d -> d.getProducto().getIdProducto() == p.getIdProducto())
                    .findFirst().orElse(null);

            int cantidadEnCarrito = (detalleExistente != null) ? detalleExistente.getCantidad() : 0;
            int cantidadTotalDeseada = cantidadEnCarrito + cantidadInput;

            if (cantidadTotalDeseada > p.getExistenciaProducto()) {
                int maximoPosible = p.getExistenciaProducto() - cantidadEnCarrito;
                String msg = (maximoPosible > 0) ? "Solo puedes agregar " + maximoPosible + " más."
                        : "No hay más stock.";
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Stock insuficiente", msg, stage);
                return;
            }

            if (detalleExistente != null) {
                detalleExistente.setCantidad(cantidadTotalDeseada);
                detalleExistente.setPrecioUnitario(precioFinal);
                detalleExistente.setSubtotal(cantidadTotalDeseada * precioFinal);
                tablaDetalles.refresh();
            } else {
                DetalleVenta d = new DetalleVenta();
                d.setProducto(p);
                d.setCantidad(cantidadInput);
                d.setPrecioUnitario(precioFinal);
                d.setSubtotal(cantidadInput * precioFinal);
                listaDetalles.add(d);
            }

            calcularTotalGeneral();
            txtCantidad.setText("1");

        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "Verifica la cantidad y el precio.", stage);
        }
    }

    /**
     * Calcula el total general sumando los subtotales de cada detalle y lo muestra
     * en el label correspondiente
     */
    private void calcularTotalGeneral() {
        double total = listaDetalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        lblTotal.setText(String.format("Total: $%.2f", total));
    }

    /**
     * Procesa la venta al hacer clic en el botón "Finalizar Venta". Valida que haya
     * productos en el carrito, muestra una confirmación y luego registra la venta
     * usando el servicio correspondiente. Si la venta se registra exitosamente,
     * limpia el carrito y resetea los campos para una nueva venta
     */
    private void procesarVenta() {
        if (listaDetalles.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Carrito vacío", "Agrega productos antes de vender.",
                    stage);
            return;
        }

        if (dialogService.mostrarConfirmacion("Confirmar Venta", "¿Deseas procesar la venta?", stage)) {
            Venta v = new Venta();
            v.setCliente(cmbCliente.getValue());
            v.setTotalVenta(listaDetalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum());

            String resultado = ventaService.registrarVenta(v, new ArrayList<>(listaDetalles), usuarioActual);

            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Venta", resultado, stage);
            if (resultado.contains("exitosamente")) {
                listaDetalles.clear();
                calcularTotalGeneral();
                cargarCatalogos();
                txtPrecioVenta.setText("0.00");
                txtCantidad.setText("1");
                cmbProducto.getSelectionModel().clearSelection();
                cmbCliente.getSelectionModel().clearSelection();
            }
        }
    }
}