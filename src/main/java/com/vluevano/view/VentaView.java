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
    @Autowired
    private MonedaService monedaService;

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
     * Muestra la pantalla de nueva venta, donde se pueden seleccionar cliente, producto, cantidad y precio para agregar al carrito de ventas, así como finalizar la venta con el total calculado y la moneda seleccionada
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
     * Crea el panel de control en el lado izquierdo de la pantalla, donde se pueden seleccionar cliente, producto, cantidad y precio para agregar al carrito de ventas
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
            public String toString(Cliente c) { return (c != null) ? c.getNombreCliente() : ""; }
            @Override
            public Cliente fromString(String string) { return null; }
        });

        cmbCliente.setOnAction(e -> actualizarInfoProducto());

        cmbProducto = new ComboBox<>();
        cmbProducto.setMaxWidth(Double.MAX_VALUE);
        cmbProducto.setStyle(AppTheme.STYLE_INPUT);

        cmbProducto.setConverter(new javafx.util.StringConverter<Producto>() {
            @Override
            public String toString(Producto p) { return (p != null) ? p.getNombreProducto() : ""; }
            @Override
            public Producto fromString(String string) { return null; }
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
                UIFactory.crearGrupoInput("Precio Unitario $", boxCostoConMoneda),
                UIFactory.crearGrupoInput("Cantidad", txtCantidad),
                btnAgregar);
        return box;
    }

    /**
     * Crea el panel de detalle en el lado derecho de la pantalla, donde se muestra la tabla con los productos agregados al carrito de ventas, el total calculado, selector de moneda y botón para finalizar la venta
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
                ".scroll-bar:vertical .thumb {-fx-background-color: #DADADA;-fx-background-insets: 0 4 0 4;-fx-background-radius: 4;}" +
                ".scroll-bar:horizontal .thumb {-fx-background-color: #DADADA;-fx-background-insets: 4 0 4 0;-fx-background-radius: 4;}";
        tablaDetalles.getStylesheets().add(estiloThumb);
        tablaDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaDetalles.setStyle("-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        TableColumn<DetalleVenta, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto().getNombreProducto()));

        TableColumn<DetalleVenta, String> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getCantidad())));

        TableColumn<DetalleVenta, String> colPrecio = new TableColumn<>("Precio U.");
        colPrecio.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getPrecioUnitario())));

        TableColumn<DetalleVenta, String> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d -> new SimpleStringProperty(String.format("$%.2f", d.getValue().getSubtotal())));

        TableColumn<DetalleVenta, Void> colAccion = new TableColumn<>("");
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

        tablaDetalles.getColumns().addAll(colProd, colCant, colPrecio, colSub, colAccion);
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

        lblTotal = new Label("Total: $0.00");
        lblTotal.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY);

        VBox boxTotales = new VBox(0, lblTotal, lblTotalEquivalente);
        boxTotales.setAlignment(Pos.CENTER_LEFT);

        Button btnFinalizar = UIFactory.crearBotonPrimario("FINALIZAR VENTA");
        btnFinalizar.setPrefHeight(50);
        btnFinalizar.setPrefWidth(200);
        btnFinalizar.setOnAction(e -> procesarVenta());

        HBox footer = new HBox(20, cmbMonedaVenta, boxTotales, new Region(), btnFinalizar);
        HBox.setHgrow(footer.getChildren().get(3), Priority.ALWAYS);
        footer.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(new Label("Detalle de la Venta"), tablaDetalles, new Separator(), footer);
        return box;
    }

    /**
     * Actualiza el estado de los controles de selección de cliente y producto según si hay productos agregados al carrito de ventas, deshabilitando la selección de cliente si ya se han agregado productos para evitar inconsistencias en precios históricos y sugeridos
     */
    private void actualizarEstadoControlesOrigen() {
        boolean hayItems = !listaDetalles.isEmpty();
        cmbCliente.setDisable(hayItems);
    }

    /**
     * Carga los catálogos de clientes y productos en los ComboBox correspondientes para que el usuario pueda seleccionar al agregar productos al carrito de ventas
     */
    private void cargarCatalogos() {
        cmbCliente.getItems().setAll(clienteService.consultarClientes());
        cmbProducto.getItems().setAll(productoService.consultarProductos());
    }

    /**
     * Actualiza la información mostrada sobre el producto seleccionado, incluyendo el stock disponible y el precio histórico de venta para ese producto y cliente, sugiriendo una conversión automática si la moneda de venta seleccionada es diferente a la del precio histórico
     */
    private void actualizarInfoProducto() {
        Producto p = cmbProducto.getValue();
        Cliente c = cmbCliente.getValue();
        
        if (p != null) {
            lblStockActual.setText("Stock disponible: " + p.getExistenciaProducto());
            
            Double precioHistorico = ventaService.obtenerPrecioVenta(p, c);
            String monedaHistorica = ventaService.obtenerMonedaVenta(p, c);
            String monedaVentaSeleccionada = cmbMonedaVenta.getValue();

            if (precioHistorico > 0) {
                if (monedaHistorica.equalsIgnoreCase(monedaVentaSeleccionada)) {
                    txtPrecioVenta.setText(String.format("%.2f", precioHistorico).replace(",", "."));
                    lblMonedaSugerida.setText("Base: " + monedaHistorica);
                } else {
                    double tc = monedaService.getTipoCambioActual();
                    if (tc == 0) tc = 20.0;
                    double valorConvertido = monedaVentaSeleccionada.equals("MXN") 
                        ? (precioHistorico * tc) 
                        : (precioHistorico / tc);
                    
                    txtPrecioVenta.setText(String.format("%.2f", valorConvertido).replace(",", "."));
                    lblMonedaSugerida.setText("Auto-conv. de " + monedaHistorica);
                }
            } else {
                txtPrecioVenta.setText("0.00");
                lblMonedaSugerida.setText("");
            }
        } else {
            lblStockActual.setText("Stock disponible: -");
            txtPrecioVenta.setText("0.00");
            lblMonedaSugerida.setText("");
        }
    }

    /**
     * Agrega el producto seleccionado al carrito de ventas con la cantidad y precio especificados, validando que no se exceda el stock disponible y que los valores ingresados sean correctos, actualizando el subtotal del producto en el carrito y recalculando el total general de la venta
     */
    private void agregarProducto() {
        Producto p = cmbProducto.getValue();
        if (p == null) return;

        try {
            int cantidadInput = Integer.parseInt(txtCantidad.getText());
            double precioFinal = Double.parseDouble(txtPrecioVenta.getText());

            if (cantidadInput <= 0) throw new NumberFormatException("Cantidad negativa");

            if (precioFinal < 0) {
                dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Precio inválido", "El precio no puede ser negativo.", stage);
                return;
            }

            DetalleVenta detalleExistente = listaDetalles.stream()
                    .filter(d -> d.getProducto().getIdProducto() == p.getIdProducto())
                    .findFirst().orElse(null);

            int cantidadEnCarrito = (detalleExistente != null) ? detalleExistente.getCantidad() : 0;
            int cantidadTotalDeseada = cantidadEnCarrito + cantidadInput;

            if (cantidadTotalDeseada > p.getExistenciaProducto()) {
                int maximoPosible = p.getExistenciaProducto() - cantidadEnCarrito;
                String msg = (maximoPosible > 0) ? "Solo puedes agregar " + maximoPosible + " más." : "No hay más stock.";
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

            txtCantidad.setText("1");
            cmbProducto.getSelectionModel().clearSelection();
            cmbProducto.requestFocus();

        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico", "Verifica la cantidad y el precio.", stage);
        }
    }

    /**
     * Calcula el total general de la venta sumando los subtotales de cada producto en el carrito, actualiza el label del total y muestra una conversión aproximada a la moneda preferida del usuario si es diferente a la moneda de venta seleccionada
     */
    private void calcularTotalGeneral() {
        double total = listaDetalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum();
        lblTotal.setText(String.format("Total: $%.2f", total));

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
     * Procesa la venta al confirmar con el usuario, creando un objeto Venta con los datos seleccionados, registrando la venta a través del servicio correspondiente y mostrando el resultado, limpiando el carrito y reseteando los controles si la venta se registró exitosamente
     */
    private void procesarVenta() {
        if (listaDetalles.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Vacío", "El carrito de ventas está vacío.", stage);
            return;
        }

        if (dialogService.mostrarConfirmacion("Confirmar Venta", "¿Deseas procesar la venta?", stage)) {
            Venta v = new Venta();
            v.setCliente(cmbCliente.getValue());
            v.setTotalVenta(listaDetalles.stream().mapToDouble(DetalleVenta::getSubtotal).sum());
            
            v.setMoneda(cmbMonedaVenta.getValue());
            v.setTipoCambio(monedaService.getTipoCambioActual());

            String resultado = ventaService.registrarVenta(v, new ArrayList<>(listaDetalles), usuarioActual);

            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Venta", resultado, stage);
            if (resultado.contains("exitosamente")) {
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