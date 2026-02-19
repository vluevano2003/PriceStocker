package com.vluevano.view;

import com.vluevano.model.*;
import com.vluevano.service.*;
import com.vluevano.util.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
    @Autowired
    private MonedaService monedaService;

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
    private Label lblMonedaSugerida;
    private TextField txtCantidad;

    private Label lblTotal;

    private ComboBox<String> cmbMonedaCompra;
    private Label lblTotalEquivalente;
    private String monedaAnterior;

    /**
     * Muestra la pantalla de registro de compras
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;
        this.listaDetalles = FXCollections.observableArrayList();

        monedaService.inicializar();

        this.listaDetalles.addListener((ListChangeListener<DetalleCompra>) c -> {
            actualizarEstadoControlesOrigen();
            calcularTotalGeneral();
        });

        BorderPane root = new BorderPane();
        root.setTop(UIFactory.crearHeader("Nueva Compra", "Registro de entrada de mercancía",
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        HBox contenido = new HBox(20);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        VBox panelDerecho = crearPanelDetalle(); 
        VBox panelIzquierdo = crearPanelControl();
        
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
     * Crea el panel de control para agregar productos a la compra
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
        cmbProveedor.setConverter(new javafx.util.StringConverter<Proveedor>() {
            @Override
            public String toString(Proveedor p) { return (p != null) ? p.getNombreProv() : ""; }
            @Override
            public Proveedor fromString(String string) { return null; }
        });

        cmbFabricante = new ComboBox<>();
        cmbFabricante.setMaxWidth(Double.MAX_VALUE);
        cmbFabricante.setStyle(AppTheme.STYLE_INPUT);
        cmbFabricante.setVisible(false);
        cmbFabricante.setManaged(false);
        cmbFabricante.setConverter(new javafx.util.StringConverter<Fabricante>() {
            @Override
            public String toString(Fabricante f) { return (f != null) ? f.getNombreFabricante() : ""; }
            @Override
            public Fabricante fromString(String string) { return null; }
        });

        containerSelectorOrigen = new VBox(10, cmbProveedor, cmbFabricante);

        cmbProducto = new ComboBox<>();
        cmbProducto.setMaxWidth(Double.MAX_VALUE);
        cmbProducto.setStyle(AppTheme.STYLE_INPUT);
        cmbProducto.setConverter(new javafx.util.StringConverter<Producto>() {
            @Override
            public String toString(Producto p) { return (p != null) ? p.getNombreProducto() : ""; }
            @Override
            public Producto fromString(String string) { return null; }
        });

        txtCostou = UIFactory.crearInput("0.00");
        txtCantidad = UIFactory.crearInput("1");
        
        lblMonedaSugerida = new Label("");
        lblMonedaSugerida.setStyle("-fx-text-fill: #F97316; -fx-font-weight: bold; -fx-font-size: 11px;");
        VBox boxCostoConMoneda = new VBox(2, txtCostou, lblMonedaSugerida);

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
                        UIFactory.crearGrupoInput("Costo Unit. $", boxCostoConMoneda), 
                        UIFactory.crearGrupoInput("Cantidad", txtCantidad)),
                btnAgregar);
        return box;
    }

    /**
     * Crea el panel derecho con la tabla de detalles y totales
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelDetalle() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle(AppTheme.STYLE_CARD);

        tablaDetalles = new TableView<>();
        tablaDetalles.setItems(listaDetalles);

        String estiloThumb = "data:text/css,.scroll-bar:vertical .thumb {-fx-background-color: #DADADA;-fx-background-insets: 0 4 0 4;-fx-background-radius: 4;} .scroll-bar:horizontal .thumb {-fx-background-color: #DADADA;-fx-background-insets: 4 0 4 0;-fx-background-radius: 4;}";

        tablaDetalles.getStylesheets().add(estiloThumb);
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaDetalles.setStyle("-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        TableColumn<DetalleCompra, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto().getNombreProducto()));

        TableColumn<DetalleCompra, String> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));

        TableColumn<DetalleCompra, String> colCosto = new TableColumn<>("Costo U.");
        colCosto.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getCostoUnitario())));

        TableColumn<DetalleCompra, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getSubtotal())));

        TableColumn<DetalleCompra, Void> colAccion = new TableColumn<>("");
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btn = UIFactory.crearBotonTablaEliminar(() -> {
                listaDetalles.remove(getIndex());
            });

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tablaDetalles.getColumns().addAll(colProd, colCant, colCosto, colSub, colAccion);
        VBox.setVgrow(tablaDetalles, Priority.ALWAYS);

        cmbMonedaCompra = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMonedaCompra.setValue(monedaService.getMonedaPorDefecto());
        monedaAnterior = cmbMonedaCompra.getValue();
        cmbMonedaCompra.setStyle(AppTheme.STYLE_INPUT);

        cmbMonedaCompra.setOnAction(e -> {
            String nuevaMoneda = cmbMonedaCompra.getValue();
            if (nuevaMoneda == null || nuevaMoneda.equals(monedaAnterior)) return;

            double tc = monedaService.getTipoCambioActual();
            if (tc == 0) tc = 20.0;

            if (!listaDetalles.isEmpty()) {
                for (DetalleCompra d : listaDetalles) {
                    double costo = d.getCostoUnitario();
                    
                    if (monedaAnterior.equals("MXN") && nuevaMoneda.equals("USD")) {
                        costo = costo / tc;
                    } else if (monedaAnterior.equals("USD") && nuevaMoneda.equals("MXN")) {
                        costo = costo * tc;
                    }
                    
                    d.setCostoUnitario(costo);
                    d.setSubtotal(d.getCantidad() * costo);
                }
                tablaDetalles.refresh();
            }

            monedaAnterior = nuevaMoneda;
            calcularTotalGeneral();
            actualizarCostoSugerido();
        }); 

        lblTotalEquivalente = new Label("");
        lblTotalEquivalente.setStyle("-fx-font-size: 14px; -fx-text-fill: #F97316; -fx-font-weight: bold;");

        lblTotal = new Label("Total: $0.00");
        lblTotal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY);

        VBox boxTotales = new VBox(0, lblTotal, lblTotalEquivalente);
        boxTotales.setAlignment(Pos.CENTER_LEFT);

        Button btnGuardar = UIFactory.crearBotonPrimario("REGISTRAR COMPRA");
        btnGuardar.setPrefHeight(50);
        btnGuardar.setPrefWidth(200);
        btnGuardar.setOnAction(e -> procesarCompra());

        HBox footer = new HBox(20, cmbMonedaCompra, boxTotales, new Region(), btnGuardar);
        HBox.setHgrow(footer.getChildren().get(3), Priority.ALWAYS);
        footer.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(tablaDetalles, footer);
        return box;
    }

    /**
     * Configura los listeners para los controles de origen y producto, para actualizar el costo sugerido automáticamente
     */
    private void configurarListeners() {
        rbProveedor.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                cmbProveedor.setVisible(true);
                cmbProveedor.setManaged(true);
                cmbFabricante.setVisible(false);
                cmbFabricante.setManaged(false);
                cmbFabricante.getSelectionModel().clearSelection();
                actualizarCostoSugerido();
            }
        });

        rbFabricante.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                cmbFabricante.setVisible(true);
                cmbFabricante.setManaged(true);
                cmbProveedor.setVisible(false);
                cmbProveedor.setManaged(false);
                cmbProveedor.getSelectionModel().clearSelection();
                actualizarCostoSugerido();
            }
        });

        cmbProveedor.setOnAction(e -> actualizarCostoSugerido());
        cmbFabricante.setOnAction(e -> actualizarCostoSugerido());
        cmbProducto.setOnAction(e -> actualizarCostoSugerido());
    }

    /**
     * Actualiza el estado de los controles de selección de origen (Proveedor/Fabricante) para evitar cambios que puedan afectar el costo sugerido mientras hay productos en la lista de compra
     */
    private void actualizarEstadoControlesOrigen() {
        boolean hayItems = !listaDetalles.isEmpty();
        rbProveedor.setDisable(hayItems);
        rbFabricante.setDisable(hayItems);
        cmbProveedor.setDisable(hayItems);
        cmbFabricante.setDisable(hayItems);
    }

    /**
     * Actualiza el costo sugerido en base al producto seleccionado y su relación con el proveedor o fabricante seleccionado, considerando también la moneda de la compra para hacer una conversión automática si es necesario
     */
    private void actualizarCostoSugerido() {
        Producto p = cmbProducto.getValue();
        if (p == null) {
            lblMonedaSugerida.setText("");
            return;
        }

        Proveedor prov = rbProveedor.isSelected() ? cmbProveedor.getValue() : null;
        Fabricante fab = !rbProveedor.isSelected() ? cmbFabricante.getValue() : null;

        if (prov != null || fab != null) {
            Double costoHistorico = compraService.obtenerCostoCompra(p, prov, fab);
            String monedaHistorica = compraService.obtenerMonedaCompra(p, prov, fab);
            String monedaCompraSeleccionada = cmbMonedaCompra.getValue();

            if (costoHistorico > 0) {
                if (monedaHistorica.equalsIgnoreCase(monedaCompraSeleccionada)) {
                    txtCostou.setText(String.format("%.2f", costoHistorico).replace(",", "."));
                    lblMonedaSugerida.setText("Base: " + monedaHistorica);
                } else {
                    double tc = monedaService.getTipoCambioActual();
                    if (tc == 0) tc = 20.0;
                    double valorConvertido = monedaCompraSeleccionada.equals("MXN") 
                        ? (costoHistorico * tc) 
                        : (costoHistorico / tc);
                    
                    txtCostou.setText(String.format("%.2f", valorConvertido).replace(",", "."));
                    lblMonedaSugerida.setText("Auto-conv. de " + monedaHistorica);
                }
            } else {
                txtCostou.setText("0.00");
                lblMonedaSugerida.setText("");
            }
        }
    }

    /**
     * Carga los catálogos de productos, proveedores y fabricantes para los ComboBox correspondientes. Este método se llama al iniciar la pantalla y también después de registrar una compra exitosamente para refrescar los datos disponibles
     */
    private void cargarCatalogos() {
        cmbProducto.getItems().setAll(productoService.consultarProductos());
        cmbProveedor.getItems().setAll(proveedorService.consultarProveedores());
        cmbFabricante.getItems().setAll(fabricanteService.consultarFabricantes());
    }

    /**
     * Agrega un producto a la lista de detalles de compra. Si el producto ya existe en la lista, se actualiza la cantidad y el costo unitario en lugar de agregar una nueva línea. También se realizan validaciones para asegurar que se haya seleccionado un origen (Proveedor o Fabricante) y un producto, y que los valores de cantidad y costo sean numéricos y positivos
     */
    private void agregarProducto() {
        if (rbProveedor.isSelected() && cmbProveedor.getValue() == null) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Origen", "Selecciona un Proveedor primero.", stage);
            return;
        }
        if (rbFabricante.isSelected() && cmbFabricante.getValue() == null) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Origen", "Selecciona un Fabricante primero.", stage);
            return;
        }

        Producto p = cmbProducto.getValue();
        if (p == null) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Selecciona un producto.", stage);
            return;
        }

        try {
            int cantidadInput = Integer.parseInt(txtCantidad.getText().trim());
            double costoInput = Double.parseDouble(txtCostou.getText().trim());

            if (cantidadInput <= 0 || costoInput < 0)
                throw new NumberFormatException();

            DetalleCompra detalleExistente = listaDetalles.stream()
                    .filter(d -> d.getProducto().getIdProducto() == p.getIdProducto())
                    .findFirst().orElse(null);

            if (detalleExistente != null) {
                int nuevaCantidadTotal = detalleExistente.getCantidad() + cantidadInput;
                detalleExistente.setCantidad(nuevaCantidadTotal);
                detalleExistente.setCostoUnitario(costoInput);
                detalleExistente.setSubtotal(nuevaCantidadTotal * costoInput);
                tablaDetalles.refresh();
            } else {
                DetalleCompra det = new DetalleCompra();
                det.setProducto(p);
                det.setCantidad(cantidadInput);
                det.setCostoUnitario(costoInput);
                det.setSubtotal(cantidadInput * costoInput);
                listaDetalles.add(det);
            }

            txtCantidad.setText("1");
            cmbProducto.getSelectionModel().clearSelection();
            cmbProducto.requestFocus();

        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico", "Cantidad o Costo inválidos.", stage);
        }
    }

    /**
     * Calcula el total general de la compra sumando los subtotales de cada detalle, y también muestra una equivalencia en la moneda preferida del usuario si es diferente a la moneda seleccionada para la compra, utilizando el tipo de cambio actual para hacer la conversión automáticamente. Si la moneda seleccionada es la misma que la preferida, se oculta la equivalencia para evitar confusiones
     */
    private void calcularTotalGeneral() {
        double total = listaDetalles.stream().mapToDouble(DetalleCompra::getSubtotal).sum();
        lblTotal.setText(String.format("Total: $%.2f", total));

        String monedaSel = cmbMonedaCompra.getValue();
        String monedaPref = monedaService.getMonedaPorDefecto();
        double tc = monedaService.getTipoCambioActual();
        if (tc == 0) tc = 20.0;

        if (monedaSel != null && monedaSel.equalsIgnoreCase(monedaPref)) {
            lblTotalEquivalente.setText(""); 
        } else {
            double equivalente;
            if (monedaPref.equalsIgnoreCase("MXN")) {
                equivalente = total * tc; 
            } else {
                equivalente = total / tc;
            }
            lblTotalEquivalente.setText(String.format("≈ $%.2f %s", equivalente, monedaPref));
        }
    }

    /**
     * Procesa la compra al hacer clic en el botón de registrar. Primero valida que haya productos en la lista, y que se haya seleccionado un origen (Proveedor o Fabricante). Luego muestra una confirmación al usuario, y si confirma, crea un objeto Compra con los datos necesarios (total, origen, moneda, tipo de cambio) y llama al servicio para registrar la compra junto con los detalles. Después muestra el resultado de la operación, y si fue exitosa, limpia la lista de detalles y recarga los catálogos para reflejar cualquier cambio en costos históricos o disponibilidad de productos
     */
    private void procesarCompra() {
        if (listaDetalles.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Vacío", "La lista de compra está vacía.", stage);
            return;
        }

        Proveedor prov = cmbProveedor.getValue();
        Fabricante fab = cmbFabricante.getValue();

        if (dialogService.mostrarConfirmacion("Confirmar", "¿Registrar entrada de mercancía?", stage)) {
            Compra compra = new Compra();
            compra.setTotalCompra(listaDetalles.stream().mapToDouble(DetalleCompra::getSubtotal).sum());

            if (rbProveedor.isSelected()) {
                compra.setProveedor(prov);
            } else {
                compra.setFabricante(fab);
            }

            compra.setMoneda(cmbMonedaCompra.getValue());
            compra.setTipoCambio(monedaService.getTipoCambioActual());

            String resultado = compraService.registrarCompra(compra, new ArrayList<>(listaDetalles), usuarioActual);
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Resultado", resultado, stage);

            if (resultado.contains("exitosamente")) {
                listaDetalles.clear();
                cargarCatalogos();
            }
        }
    }
}