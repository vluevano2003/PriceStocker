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
import java.util.function.UnaryOperator;

@Component
public class VentaView {

    @Autowired private VentaService ventaService;
    @Autowired private ProductoService productoService;
    @Autowired private ClienteService clienteService;
    @Autowired private DialogService dialogService;
    @Autowired @Lazy private MenuPrincipalScreen menuPrincipalScreen;
    @Autowired private MonedaService monedaService;
    @Autowired private GestorIdioma idioma; 

    private Stage stage;
    private String usuarioActual;

    private TableView<DetalleVenta> tablaDetalles;
    private ObservableList<DetalleVenta> listaDetalles;
    private ComboBox<Cliente> cmbCliente;
    private ComboBox<Producto> cmbProducto;
    private TextField txtCantidad;
    private TextField txtPrecioVenta;
    private Label lblMonedaSugerida;
    private Label lblStockActual;

    private Label lblTotal;
    private ComboBox<String> cmbMonedaVenta;
    private Label lblTotalEquivalente;
    private String monedaAnterior;

    /**
     * Muestra la pantalla de venta
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;
        this.listaDetalles = FXCollections.observableArrayList();

        monedaService.inicializar();

        this.listaDetalles.addListener((ListChangeListener<DetalleVenta>) c -> {
            actualizarEstadoControlesOrigen();
            calcularTotalGeneral();
        });

        BorderPane root = new BorderPane();
        root.setTop(UIFactory.crearHeader(
                idioma.get("sale.header.title"),
                idioma.get("sale.header.subtitle"),
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        HBox contenido = new HBox(20);
        contenido.setPadding(new Insets(20));
        contenido.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        VBox panelIzquierdo = crearPanelControl();
        VBox panelDerecho = crearPanelDetalle();
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

        stage.setTitle("PriceStocker | " + idioma.get("sale.window.title"));
        stage.show();

        cargarCatalogos();
    }

    /**
     * Crea el panel de control para seleccionar cliente, producto, cantidad y precio
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
            @Override public String toString(Cliente c) { return (c != null) ? c.getNombreCliente() : ""; }
            @Override public Cliente fromString(String string) { return null; }
        });

        cmbCliente.setOnAction(e -> actualizarInfoProducto());

        cmbProducto = new ComboBox<>();
        cmbProducto.setMaxWidth(Double.MAX_VALUE);
        cmbProducto.setStyle(AppTheme.STYLE_INPUT);

        cmbProducto.setConverter(new javafx.util.StringConverter<Producto>() {
            @Override public String toString(Producto p) { return (p != null) ? p.getNombreProducto() : ""; }
            @Override public Producto fromString(String string) { return null; }
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

        lblMonedaSugerida = new Label("");
        lblMonedaSugerida.setStyle("-fx-text-fill: #F97316; -fx-font-weight: bold; -fx-font-size: 11px;");
        VBox boxCostoConMoneda = new VBox(2, txtPrecioVenta, lblMonedaSugerida);

        lblStockActual = new Label(idioma.get("sale.lbl.stock_empty"));
        lblStockActual.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Button btnAgregar = UIFactory.crearBotonPrimario(idioma.get("sale.btn.add"));
        btnAgregar.setMaxWidth(Double.MAX_VALUE);
        btnAgregar.setOnAction(e -> agregarProducto());

        box.getChildren().addAll(
                UIFactory.crearTituloSeccion(idioma.get("sale.section.client")),
                UIFactory.crearGrupoInput(idioma.get("sale.lbl.client"), cmbCliente),
                new Separator(),
                UIFactory.crearTituloSeccion(idioma.get("sale.section.product")),
                UIFactory.crearGrupoInput(idioma.get("sale.lbl.product"), cmbProducto),
                lblStockActual,
                UIFactory.crearGrupoInput(idioma.get("sale.lbl.price"), boxCostoConMoneda),
                UIFactory.crearGrupoInput(idioma.get("sale.lbl.quantity"), txtCantidad),
                btnAgregar);
        return box;
    }

    /**
     * Crea el panel de detalle de la venta con la tabla de productos agregados, selección de moneda y total
     * @return
     */
    private VBox crearPanelDetalle() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle(AppTheme.STYLE_CARD);

        tablaDetalles = new TableView<>();
        tablaDetalles.setItems(listaDetalles);
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<DetalleVenta, String> colProd = UIFactory.crearColumna(idioma.get("sale.col.product"), 
                d -> d.getProducto().getNombreProducto(), 0);

        TableColumn<DetalleVenta, String> colCant = UIFactory.crearColumna(idioma.get("sale.col.qty"), 
                d -> String.valueOf(d.getCantidad()), 0);

        TableColumn<DetalleVenta, String> colPrecio = UIFactory.crearColumna(idioma.get("sale.col.price"), 
                d -> String.format("$%.2f", d.getPrecioUnitario()), 0);

        TableColumn<DetalleVenta, String> colSub = UIFactory.crearColumna(idioma.get("sale.col.subtotal"), 
                d -> String.format("$%.2f", d.getSubtotal()), 0);

        TableColumn<DetalleVenta, Void> colAccion = new TableColumn<>("");
        colAccion.setCellFactory(param -> new TableCell<>() {
            private final Button btn = UIFactory.crearBotonTablaEliminar(() -> listaDetalles.remove(getIndex()));
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tablaDetalles.getColumns().addAll(java.util.List.of(colProd, colCant, colPrecio, colSub, colAccion));
        VBox.setVgrow(tablaDetalles, Priority.ALWAYS);

        cmbMonedaVenta = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMonedaVenta.setValue(monedaService.getMonedaPorDefecto());
        monedaAnterior = cmbMonedaVenta.getValue();
        cmbMonedaVenta.setStyle(AppTheme.STYLE_INPUT);

        cmbMonedaVenta.setOnAction(e -> {
            String nuevaMoneda = cmbMonedaVenta.getValue();
            if (nuevaMoneda == null || nuevaMoneda.equals(monedaAnterior)) return;

            double tc = monedaService.getTipoCambioActual();
            if (tc == 0) tc = 20.0;

            if (!listaDetalles.isEmpty()) {
                for (DetalleVenta d : listaDetalles) {
                    double precio = d.getPrecioUnitario();

                    if (monedaAnterior.equals("MXN") && nuevaMoneda.equals("USD")) {
                        precio = precio / tc;
                    } else if (monedaAnterior.equals("USD") && nuevaMoneda.equals("MXN")) {
                        precio = precio * tc;
                    }

                    d.setPrecioUnitario(precio);
                    d.setSubtotal(d.getCantidad() * precio);
                }
                tablaDetalles.refresh();
            }

            monedaAnterior = nuevaMoneda;
            calcularTotalGeneral();
            actualizarInfoProducto();
        });

        lblTotalEquivalente = new Label("");
        lblTotalEquivalente.setStyle("-fx-font-size: 14px; -fx-text-fill: #F97316; -fx-font-weight: bold;");

        lblTotal = new Label(idioma.get("sale.lbl.total") + " $0.00");
        lblTotal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY);

        VBox boxTotales = new VBox(0, lblTotal, lblTotalEquivalente);
        boxTotales.setAlignment(Pos.CENTER_LEFT);

        Button btnFinalizar = UIFactory.crearBotonPrimario(idioma.get("sale.btn.register"));
        btnFinalizar.setPrefHeight(50);
        btnFinalizar.setPrefWidth(200);
        btnFinalizar.setOnAction(e -> procesarVenta());

        HBox footer = new HBox(20, cmbMonedaVenta, boxTotales, new Region(), btnFinalizar);
        HBox.setHgrow(footer.getChildren().get(3), Priority.ALWAYS);
        footer.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(new Label(idioma.get("sale.section.detail")), tablaDetalles, new Separator(), footer);
        return box;
    }

    /**
     * Habilita o deshabilita controles de origen (cliente) según si hay productos en la venta para evitar inconsistencias
     */
    private void actualizarEstadoControlesOrigen() {
        boolean hayItems = !listaDetalles.isEmpty();
        cmbCliente.setDisable(hayItems);
    }

    /**
     * Carga los clientes y productos en los ComboBox desde la base de datos
     */
    private void cargarCatalogos() {
        cmbCliente.getItems().setAll(clienteService.consultarClientes());
        cmbProducto.getItems().setAll(productoService.consultarProductos());
    }

    /**
     * Actualiza la información del producto seleccionado, como el stock actual y el precio histórico para ese cliente si existe
     */
    private void actualizarInfoProducto() {
        Producto p = cmbProducto.getValue();
        Cliente c = cmbCliente.getValue();

        if (p != null) {
            lblStockActual.setText(idioma.get("sale.lbl.stock_val", p.getExistenciaProducto()));

            Double precioHistorico = ventaService.obtenerPrecioVenta(p, c);
            String monedaHistorica = ventaService.obtenerMonedaVenta(p, c);
            String monedaVentaSeleccionada = cmbMonedaVenta.getValue();

            if (precioHistorico > 0) {
                if (monedaHistorica.equalsIgnoreCase(monedaVentaSeleccionada)) {
                    txtPrecioVenta.setText(String.format("%.2f", precioHistorico).replace(",", "."));
                    lblMonedaSugerida.setText(idioma.get("sale.lbl.base", monedaHistorica));
                } else {
                    double tc = monedaService.getTipoCambioActual();
                    if (tc == 0) tc = 20.0;
                    double valorConvertido = monedaVentaSeleccionada.equals("MXN")
                            ? (precioHistorico * tc)
                            : (precioHistorico / tc);

                    txtPrecioVenta.setText(String.format("%.2f", valorConvertido).replace(",", "."));
                    lblMonedaSugerida.setText(idioma.get("sale.lbl.autoconv", monedaHistorica));
                }
            } else {
                txtPrecioVenta.setText("0.00");
                lblMonedaSugerida.setText("");
            }
        } else {
            lblStockActual.setText(idioma.get("sale.lbl.stock_empty"));
            txtPrecioVenta.setText("0.00");
            lblMonedaSugerida.setText("");
        }
    }

    /**
     * Agrega el producto seleccionado al detalle de la venta, validando cantidad, precio y stock disponible. Si el producto ya existe en el detalle, actualiza la cantidad y subtotal
     */
    private void agregarProducto() {
        Producto p = cmbProducto.getValue();
        if (p == null) return;

        try {
            int cantidadInput = Integer.parseInt(txtCantidad.getText());
            double precioFinal = Double.parseDouble(txtPrecioVenta.getText());

            if (cantidadInput <= 0) throw new NumberFormatException("Cantidad negativa");

            if (precioFinal < 0) {
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("sale.msg.neg_price.title"), idioma.get("sale.msg.neg_price.content"), stage);
                return;
            }

            DetalleVenta detalleExistente = listaDetalles.stream()
                    .filter(d -> d.getProducto().getIdProducto() == p.getIdProducto())
                    .findFirst().orElse(null);

            int cantidadEnCarrito = (detalleExistente != null) ? detalleExistente.getCantidad() : 0;
            int cantidadTotalDeseada = cantidadEnCarrito + cantidadInput;

            if (cantidadTotalDeseada > p.getExistenciaProducto()) {
                int maximoPosible = p.getExistenciaProducto() - cantidadEnCarrito;
                String msg = (maximoPosible > 0) ? idioma.get("sale.msg.stock.partial", maximoPosible) : idioma.get("sale.msg.stock.none");
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("sale.msg.stock.title"), msg, stage);
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

            txtCantidad.setText("1");
            cmbProducto.getSelectionModel().clearSelection();
            cmbProducto.requestFocus();

        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("sale.msg.numeric.title"), idioma.get("sale.msg.numeric.content"), stage);
        }
    }

    /**
     * Calcula el total general de la venta sumando los subtotales de cada detalle y actualiza la etiqueta del total. También calcula el equivalente en la moneda opuesta si es necesario
     */
    private void calcularTotalGeneral() {
        double total = listaDetalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        lblTotal.setText(idioma.get("sale.lbl.total") + String.format(" $%.2f", total));

        String monedaSel = cmbMonedaVenta.getValue();
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
     * Procesa la venta al hacer clic en el botón de finalizar. Valida que haya productos en el detalle, muestra una confirmación y luego registra la venta usando el servicio. Si la venta se registra exitosamente, limpia el detalle y recarga los catálogos
     */
    private void procesarVenta() {
        if (listaDetalles.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("sale.msg.empty.title"), idioma.get("sale.msg.empty.content"), stage);
            return;
        }

        if (dialogService.mostrarConfirmacion(idioma.get("sale.msg.confirm.title"), idioma.get("sale.msg.confirm.content"), stage)) {
            Venta v = new Venta();
            v.setCliente(cmbCliente.getValue());
            v.setTotalVenta(listaDetalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum());

            v.setMoneda(cmbMonedaVenta.getValue());
            v.setTipoCambio(monedaService.getTipoCambioActual());

            String resultado = ventaService.registrarVenta(v, new ArrayList<>(listaDetalles), usuarioActual);

            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("sale.msg.result.title"), resultado, stage);
            if (resultado.contains("exitosamente") || resultado.contains("guardad") || resultado.contains("successfully") || resultado.contains("saved")) {
                listaDetalles.clear();
                cargarCatalogos();
                txtPrecioVenta.setText("0.00");
                txtCantidad.setText("1");
                lblMonedaSugerida.setText("");
                cmbProducto.getSelectionModel().clearSelection();
                cmbCliente.getSelectionModel().clearSelection();
            }
        }
    }
}