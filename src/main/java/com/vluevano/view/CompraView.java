package com.vluevano.view;

import com.vluevano.model.*;
import com.vluevano.service.*;
import com.vluevano.util.*;
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

    @Autowired private CompraService compraService;
    @Autowired private ProductoService productoService;
    @Autowired private ProveedorService proveedorService;
    @Autowired private FabricanteService fabricanteService;
    @Autowired private DialogService dialogService;
    @Autowired @Lazy private MenuPrincipalScreen menuPrincipalScreen;
    @Autowired private MonedaService monedaService;
    @Autowired private GestorIdioma idioma;

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
     * Muestra la pantalla de registro de compras, permitiendo al usuario seleccionar el origen (proveedor o fabricante)
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
        root.setTop(UIFactory.crearHeader(
                idioma.get("purchase.header.title"), 
                idioma.get("purchase.header.subtitle"),
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        HBox contenido = new HBox(20);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        VBox panelDerecho = crearPanelDetalle(); 
        VBox panelIzquierdo = crearPanelControl();
        
        HBox.setHgrow(panelDerecho, Priority.ALWAYS);

        contenido.getChildren().addAll(panelIzquierdo, panelDerecho);
        root.setCenter(contenido);

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1280, 800);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        String css = getClass().getResource("/css/styles.css").toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }

        stage.setTitle("PriceStocker | " + idioma.get("purchase.window.title"));
        stage.show();

        cargarCatalogos();
        configurarListeners();
    }

    /**
     * Crea el panel de control para seleccionar el origen de la compra (proveedor o fabricante), elegir el producto, costo y cantidad
     * @return
     */
    private VBox crearPanelControl() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle(AppTheme.STYLE_CARD);
        box.setMinWidth(350);
        box.setMaxWidth(350);

        ToggleGroup tgOrigen = new ToggleGroup();
        rbProveedor = new RadioButton(idioma.get("purchase.rb.supplier"));
        rbProveedor.setToggleGroup(tgOrigen);
        rbProveedor.setSelected(true);

        rbFabricante = new RadioButton(idioma.get("purchase.rb.manufacturer"));
        rbFabricante.setToggleGroup(tgOrigen);

        HBox radioBox = new HBox(20, rbProveedor, rbFabricante);

        cmbProveedor = new ComboBox<>();
        cmbProveedor.setMaxWidth(Double.MAX_VALUE);
        cmbProveedor.setStyle(AppTheme.STYLE_INPUT);
        cmbProveedor.setConverter(new javafx.util.StringConverter<Proveedor>() {
            @Override public String toString(Proveedor p) { return (p != null) ? p.getNombreProv() : ""; }
            @Override public Proveedor fromString(String string) { return null; }
        });

        cmbFabricante = new ComboBox<>();
        cmbFabricante.setMaxWidth(Double.MAX_VALUE);
        cmbFabricante.setStyle(AppTheme.STYLE_INPUT);
        cmbFabricante.setVisible(false);
        cmbFabricante.setManaged(false);
        cmbFabricante.setConverter(new javafx.util.StringConverter<Fabricante>() {
            @Override public String toString(Fabricante f) { return (f != null) ? f.getNombreFabricante() : ""; }
            @Override public Fabricante fromString(String string) { return null; }
        });

        containerSelectorOrigen = new VBox(10, cmbProveedor, cmbFabricante);

        cmbProducto = new ComboBox<>();
        cmbProducto.setMaxWidth(Double.MAX_VALUE);
        cmbProducto.setStyle(AppTheme.STYLE_INPUT);
        cmbProducto.setConverter(new javafx.util.StringConverter<Producto>() {
            @Override public String toString(Producto p) { return (p != null) ? p.getNombreProducto() : ""; }
            @Override public Producto fromString(String string) { return null; }
        });

        txtCostou = UIFactory.crearInput("0.00");
        txtCantidad = UIFactory.crearInput("1");
        
        lblMonedaSugerida = new Label("");
        lblMonedaSugerida.setStyle("-fx-text-fill: #F97316; -fx-font-weight: bold; -fx-font-size: 11px;");
        VBox boxCostoConMoneda = new VBox(2, txtCostou, lblMonedaSugerida);

        Button btnAgregar = UIFactory.crearBotonPrimario(idioma.get("purchase.btn.add"));
        btnAgregar.setMaxWidth(Double.MAX_VALUE);
        btnAgregar.setOnAction(e -> agregarProducto());

        box.getChildren().addAll(
                UIFactory.crearTituloSeccion(idioma.get("purchase.section.origin")),
                radioBox,
                containerSelectorOrigen,
                new Separator(),
                UIFactory.crearTituloSeccion(idioma.get("purchase.section.detail")),
                UIFactory.crearGrupoInput(idioma.get("purchase.lbl.product"), cmbProducto),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("purchase.lbl.cost"), boxCostoConMoneda), 
                        UIFactory.crearGrupoInput(idioma.get("purchase.lbl.quantity"), txtCantidad)),
                btnAgregar);
        return box;
    }

    /**
     * Crea el panel de detalle donde se muestran los productos agregados a la compra, con su cantidad, costo unitario y subtotal, además del total general y la opción para registrar la compra
     * @return
     */
    private VBox crearPanelDetalle() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle(AppTheme.STYLE_CARD);

        tablaDetalles = new TableView<>();
        tablaDetalles.setItems(listaDetalles);
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<DetalleCompra, String> colProd = UIFactory.crearColumna(idioma.get("purchase.col.product"), 
                d -> d.getProducto().getNombreProducto(), 0);
                
        TableColumn<DetalleCompra, String> colCant = UIFactory.crearColumna(idioma.get("purchase.col.qty"), 
                d -> String.valueOf(d.getCantidad()), 0);

        TableColumn<DetalleCompra, String> colCosto = UIFactory.crearColumna(idioma.get("purchase.col.cost"), 
                d -> String.format("$%.2f", d.getCostoUnitario()), 0);

        TableColumn<DetalleCompra, String> colSub = UIFactory.crearColumna(idioma.get("purchase.col.subtotal"), 
                d -> String.format("$%.2f", d.getSubtotal()), 0);

        TableColumn<DetalleCompra, Void> colAccion = new TableColumn<>("");
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btn = UIFactory.crearBotonTablaEliminar(() -> listaDetalles.remove(getIndex()));
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tablaDetalles.getColumns().addAll(java.util.List.of(colProd, colCant, colCosto, colSub, colAccion));
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

        lblTotal = new Label(idioma.get("purchase.lbl.total") + " $0.00");
        lblTotal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY);

        VBox boxTotales = new VBox(0, lblTotal, lblTotalEquivalente);
        boxTotales.setAlignment(Pos.CENTER_LEFT);

        Button btnGuardar = UIFactory.crearBotonPrimario(idioma.get("purchase.btn.register"));
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
     * Configura los listeners para los controles de selección de origen, producto y moneda, de manera que se actualice el costo sugerido y se habiliten/deshabiliten controles según corresponda
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
     * Habilita o deshabilita los controles de selección de origen (proveedor/fabricante) dependiendo de si hay productos ya agregados a la compra, para evitar inconsistencias en el origen de los productos
     */
    private void actualizarEstadoControlesOrigen() {
        boolean hayItems = !listaDetalles.isEmpty();
        rbProveedor.setDisable(hayItems);
        rbFabricante.setDisable(hayItems);
        cmbProveedor.setDisable(hayItems);
        cmbFabricante.setDisable(hayItems);
    }

    /**
     * Actualiza el costo sugerido en base al producto seleccionado y su historial de compras con el proveedor o fabricante seleccionado, realizando la conversión de moneda si es necesario
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
                    lblMonedaSugerida.setText(idioma.get("purchase.lbl.base", monedaHistorica));
                } else {
                    double tc = monedaService.getTipoCambioActual();
                    if (tc == 0) tc = 20.0;
                    double valorConvertido = monedaCompraSeleccionada.equals("MXN") 
                        ? (costoHistorico * tc) 
                        : (costoHistorico / tc);
                    
                    txtCostou.setText(String.format("%.2f", valorConvertido).replace(",", "."));
                    lblMonedaSugerida.setText(idioma.get("purchase.lbl.autoconv", monedaHistorica));
                }
            } else {
                txtCostou.setText("0.00");
                lblMonedaSugerida.setText("");
            }
        }
    }

    /**
     * Carga los catálogos de productos, proveedores y fabricantes desde la base de datos para poblar los ComboBox correspondientes, permitiendo al usuario seleccionar el origen y los productos a comprar
     */
    private void cargarCatalogos() {
        cmbProducto.getItems().setAll(productoService.consultarProductos());
        cmbProveedor.getItems().setAll(proveedorService.consultarProveedores());
        cmbFabricante.getItems().setAll(fabricanteService.consultarFabricantes());
    }

    /**
     * Agrega un producto a la lista de detalles de la compra, validando que se haya seleccionado un origen (proveedor o fabricante) y un producto, y que los campos de cantidad y costo sean numéricos y positivos. Si el producto ya existe en la lista, actualiza su cantidad y subtotal en lugar de agregar una nueva entrada.
     */
    private void agregarProducto() {
        if (rbProveedor.isSelected() && cmbProveedor.getValue() == null) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("purchase.msg.origin.title"), idioma.get("purchase.msg.origin.supplier"), stage);
            return;
        }
        if (rbFabricante.isSelected() && cmbFabricante.getValue() == null) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("purchase.msg.origin.title"), idioma.get("purchase.msg.origin.manufacturer"), stage);
            return;
        }

        Producto p = cmbProducto.getValue();
        if (p == null) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("purchase.msg.validation.title"), idioma.get("purchase.msg.validation.product"), stage);
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
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("purchase.msg.numeric.title"), idioma.get("purchase.msg.numeric.content"), stage);
        }
    }

    /**
     * Calcula el total general de la compra sumando los subtotales de cada detalle, y actualiza la etiqueta del total. Si la moneda de la compra es diferente a la moneda por defecto, también calcula y muestra el equivalente en la moneda por defecto utilizando el tipo de cambio actual.
     */
    private void calcularTotalGeneral() {
        double total = listaDetalles.stream().mapToDouble(DetalleCompra::getSubtotal).sum();
        lblTotal.setText(idioma.get("purchase.lbl.total") + String.format(" $%.2f", total));

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
     * Procesa la compra al hacer clic en el botón de registrar, validando que haya productos en la lista de detalles, y mostrando una confirmación antes de registrar la compra. Si el usuario confirma, crea un objeto Compra con los detalles ingresados, llama al servicio para registrar la compra en la base de datos, y muestra el resultado de la operación. Si la compra se registra exitosamente, limpia la lista de detalles y recarga los catálogos para permitir registrar una nueva compra
     */
    private void procesarCompra() {
        if (listaDetalles.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("purchase.msg.empty.title"), idioma.get("purchase.msg.empty.content"), stage);
            return;
        }

        Proveedor prov = cmbProveedor.getValue();
        Fabricante fab = cmbFabricante.getValue();

        if (dialogService.mostrarConfirmacion(idioma.get("purchase.msg.confirm.title"), idioma.get("purchase.msg.confirm.content"), stage)) {
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
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("purchase.msg.result.title"), resultado, stage);

            if (resultado.contains("exitosamente") || resultado.contains("guardad") || resultado.contains("successfully") || resultado.contains("saved")) {
                listaDetalles.clear();
                cargarCatalogos();
            }
        }
    }
}