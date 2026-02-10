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
     * Crea el panel de control para seleccionar cliente, producto y cantidad
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

        cmbProducto = new ComboBox<>();
        cmbProducto.setMaxWidth(Double.MAX_VALUE);
        cmbProducto.setStyle(AppTheme.STYLE_INPUT);
        cmbProducto.setOnAction(e -> actualizarInfoProducto());

        txtCantidad = UIFactory.crearInput("1");
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
                UIFactory.crearGrupoInput("Cantidad", txtCantidad),
                btnAgregar);
        return box;
    }

    /**
     * Crea el panel derecho con la tabla de detalles y el footer con total y botón
     * de finalizar
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
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

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
        btnFinalizar.setStyle(
                "-fx-background-color: #10B981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 8; -fx-cursor: hand;");
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
     * Actualiza la información del producto seleccionado, como el stock disponible
     */
    private void actualizarInfoProducto() {
        Producto p = cmbProducto.getValue();
        if (p != null) {
            lblStockActual.setText("Stock disponible: " + p.getExistenciaProducto());
        }
    }

    /**
     * Agrega el producto seleccionado al carrito de venta, validando cantidad y
     * stock
     */
    private void agregarProducto() {
        Producto p = cmbProducto.getValue();
        if (p == null)
            return;

        try {
            int cant = Integer.parseInt(txtCantidad.getText());
            if (cant <= 0)
                throw new NumberFormatException();
            if (cant > p.getExistenciaProducto()) {
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Stock insuficiente",
                        "No hay suficiente existencia.", stage);
                return;
            }

            double precio = 100.00;

            DetalleVenta d = new DetalleVenta();
            d.setProducto(p);
            d.setCantidad(cant);
            d.setPrecioUnitario(precio);
            d.setSubtotal(cant * precio);

            listaDetalles.add(d);
            calcularTotalGeneral();
            txtCantidad.setText("1");
            cmbProducto.getSelectionModel().clearSelection();

        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "Cantidad inválida", stage);
        }
    }

    /**
     * Calcula el total general de la venta y actualiza la etiqueta correspondiente
     */
    private void calcularTotalGeneral() {
        double total = listaDetalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        lblTotal.setText(String.format("Total: $%.2f", total));
    }

    /**
     * Procesa la venta, validando el carrito y confirmando la acción con el usuario
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
            }
        }
    }
}