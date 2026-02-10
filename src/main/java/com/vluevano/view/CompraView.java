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
public class CompraView {

    @Autowired
    private CompraService compraService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private ProveedorService proveedorService;
    @Autowired
    private FabricanteService fabricanteService;
    @Autowired
    private DialogService dialogService;
    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;

    private TableView<DetalleCompra> tablaDetalles;
    private ObservableList<DetalleCompra> listaDetalles;

    private RadioButton rbProveedor;
    private RadioButton rbFabricante;
    private ComboBox<Proveedor> cmbProveedor;
    private ComboBox<Fabricante> cmbFabricante;
    private VBox containerSelectorOrigen;

    private ComboBox<Producto> cmbProducto;
    private TextField txtCostou;
    private TextField txtCantidad;

    private Label lblTotal;

    /**
     * Muestra la pantalla de registro de compras
     * 
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;
        this.listaDetalles = FXCollections.observableArrayList();

        BorderPane root = new BorderPane();
        root.setTop(UIFactory.crearHeader("Nueva Compra", "Entrada de mercancía a inventario",
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        HBox contenido = new HBox(20);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        VBox panelIzquierdo = crearPanelControl();
        VBox panelDerecho = crearPanelDetalle();
        HBox.setHgrow(panelDerecho, Priority.ALWAYS);

        contenido.getChildren().addAll(panelIzquierdo, panelDerecho);
        root.setCenter(contenido);

        if (stage.getScene() != null) {
            stage.getScene().setRoot(root);
        } else {
            Scene scene = new Scene(root, 1280, 800);
            stage.setScene(scene);
        }

        stage.setTitle("PriceStocker | Registro de Compras");
        stage.show();

        cargarCatalogos();
        configurarListeners();
    }

    /**
     * Crea el panel de control izquierdo
     * 
     * @return
     */
    private VBox crearPanelControl() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle(AppTheme.STYLE_CARD);
        box.setMinWidth(350);
        box.setMaxWidth(350);

        ToggleGroup tgOrigen = new ToggleGroup();
        rbProveedor = new RadioButton("Proveedor");
        rbProveedor.setToggleGroup(tgOrigen);
        rbProveedor.setSelected(true);

        rbFabricante = new RadioButton("Fabricante");
        rbFabricante.setToggleGroup(tgOrigen);

        HBox radioBox = new HBox(20, rbProveedor, rbFabricante);

        cmbProveedor = new ComboBox<>();
        cmbProveedor.setMaxWidth(Double.MAX_VALUE);
        cmbProveedor.setStyle(AppTheme.STYLE_INPUT);

        cmbFabricante = new ComboBox<>();
        cmbFabricante.setMaxWidth(Double.MAX_VALUE);
        cmbFabricante.setStyle(AppTheme.STYLE_INPUT);
        cmbFabricante.setVisible(false);
        cmbFabricante.setManaged(false);

        containerSelectorOrigen = new VBox(10, cmbProveedor, cmbFabricante);

        cmbProducto = new ComboBox<>();
        cmbProducto.setMaxWidth(Double.MAX_VALUE);
        cmbProducto.setStyle(AppTheme.STYLE_INPUT);

        txtCostou = UIFactory.crearInput("0.00");
        txtCantidad = UIFactory.crearInput("1");

        Button btnAgregar = UIFactory.crearBotonPrimario("Agregar a la Lista");
        btnAgregar.setMaxWidth(Double.MAX_VALUE);
        btnAgregar.setOnAction(e -> agregarProducto());

        box.getChildren().addAll(
                UIFactory.crearTituloSeccion("Origen de la Compra"),
                radioBox,
                containerSelectorOrigen,
                new Separator(),
                UIFactory.crearTituloSeccion("Detalle del Producto"),
                UIFactory.crearGrupoInput("Producto", cmbProducto),
                new HBox(10,
                        UIFactory.crearGrupoInput("Costo Unit. $", txtCostou),
                        UIFactory.crearGrupoInput("Cantidad", txtCantidad)),
                btnAgregar);
        return box;
    }

    /**
     * Crea el panel de detalle derecho
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

        TableColumn<DetalleCompra, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto().getNombreProducto()));

        TableColumn<DetalleCompra, String> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));

        TableColumn<DetalleCompra, String> colCosto = new TableColumn<>("Costo U.");
        colCosto.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().getCostoUnitario()));

        TableColumn<DetalleCompra, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d -> new SimpleStringProperty("$" + d.getValue().getSubtotal()));

        TableColumn<DetalleCompra, Void> colAccion = new TableColumn<>("");
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

        tablaDetalles.getColumns().addAll(colProd, colCant, colCosto, colSub, colAccion);
        VBox.setVgrow(tablaDetalles, Priority.ALWAYS);

        lblTotal = new Label("Total: $0.00");
        lblTotal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY);

        Button btnGuardar = UIFactory.crearBotonPrimario("REGISTRAR COMPRA");
        btnGuardar.setPrefHeight(50);
        btnGuardar.setPrefWidth(200);
        btnGuardar.setStyle(
                "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-background-radius: 8; -fx-cursor: hand;");
        btnGuardar.setOnAction(e -> procesarCompra());

        HBox footer = new HBox(20, lblTotal, new Region(), btnGuardar);
        HBox.setHgrow(footer.getChildren().get(1), Priority.ALWAYS);
        footer.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(new Label("Resumen de Compra"), tablaDetalles, new Separator(), footer);
        return box;
    }

    /**
     * Configura los listeners para los RadioButtons y otros componentes
     * interactivos
     */
    private void configurarListeners() {
        rbProveedor.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                cmbProveedor.setVisible(true);
                cmbProveedor.setManaged(true);
                cmbFabricante.setVisible(false);
                cmbFabricante.setManaged(false);
                cmbFabricante.getSelectionModel().clearSelection();
            }
        });

        rbFabricante.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                cmbFabricante.setVisible(true);
                cmbFabricante.setManaged(true);
                cmbProveedor.setVisible(false);
                cmbProveedor.setManaged(false);
                cmbProveedor.getSelectionModel().clearSelection();
            }
        });
    }

    /**
     * Carga los productos, proveedores y fabricantes en los ComboBoxes
     * correspondientes
     */
    private void cargarCatalogos() {
        cmbProducto.getItems().setAll(productoService.consultarProductos());
        cmbProveedor.getItems().setAll(proveedorService.consultarProveedores());
        cmbFabricante.getItems().setAll(fabricanteService.consultarFabricantes());
    }

    /**
     * Agrega un producto a la lista de detalles de compra después de validar los
     * campos de cantidad y costo unitario
     */
    private void agregarProducto() {
        Producto p = cmbProducto.getValue();
        if (p == null) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Selecciona un producto.", stage);
            return;
        }

        try {
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            double costo = Double.parseDouble(txtCostou.getText().trim());

            if (cantidad <= 0 || costo < 0)
                throw new NumberFormatException();

            DetalleCompra det = new DetalleCompra();
            det.setProducto(p);
            det.setCantidad(cantidad);
            det.setCostoUnitario(costo);
            det.setSubtotal(cantidad * costo);

            listaDetalles.add(det);
            calcularTotalGeneral();

            txtCantidad.setText("1");
            txtCostou.setText("0.00");
            cmbProducto.getSelectionModel().clearSelection();
            cmbProducto.requestFocus();

        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico", "Cantidad o Costo inválidos.", stage);
        }
    }

    /**
     * Calcula el total general de la compra sumando los subtotales de cada detalle
     * y actualiza la etiqueta correspondiente
     */
    private void calcularTotalGeneral() {
        double total = listaDetalles.stream().mapToDouble(DetalleCompra::getSubtotal).sum();
        lblTotal.setText(String.format("Total: $%.2f", total));
    }

    /**
     * Procesa la compra validando que haya detalles, que se haya seleccionado un
     * origen (proveedor o fabricante) y luego llama al servicio para registrar la
     * compra. Muestra el resultado al usuario y limpia la lista si fue exitosa.
     */
    private void procesarCompra() {
        if (listaDetalles.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Vacío", "La lista de compra está vacía.", stage);
            return;
        }

        Proveedor prov = cmbProveedor.getValue();
        Fabricante fab = cmbFabricante.getValue();

        if (rbProveedor.isSelected() && prov == null) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Origen", "Selecciona un Proveedor.", stage);
            return;
        }
        if (rbFabricante.isSelected() && fab == null) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Origen", "Selecciona un Fabricante.", stage);
            return;
        }

        if (dialogService.mostrarConfirmacion("Confirmar", "¿Registrar entrada de mercancía?", stage)) {
            Compra compra = new Compra();
            compra.setTotalCompra(listaDetalles.stream().mapToDouble(DetalleCompra::getSubtotal).sum());

            if (rbProveedor.isSelected()) {
                compra.setProveedor(prov);
            } else {
                compra.setFabricante(fab);
            }

            String resultado = compraService.registrarCompra(compra, new ArrayList<>(listaDetalles), usuarioActual);

            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Resultado", resultado, stage);

            if (resultado.contains("exitosamente")) {
                listaDetalles.clear();
                calcularTotalGeneral();
                cargarCatalogos();
            }
        }
    }
}