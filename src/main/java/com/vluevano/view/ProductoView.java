package com.vluevano.view;

import com.vluevano.model.*;
import com.vluevano.service.DialogService;
import com.vluevano.service.MonedaService;
import com.vluevano.service.ProductoService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Component
public class ProductoView {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private DialogService dialogService;
    @Autowired
    private MonedaService monedaService;
    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;
    private TableView<Producto> tablaProductos;
    private TextField txtFiltro;

    private TextField txtNombre, txtFicha, txtAlterno, txtExistencia, txtPrecio;
    private ComboBox<String> cmbMoneda;
    private Label lblConversion;

    private Label lblTituloFormulario;
    private Button btnGuardar;
    private BorderPane contentPanel;
    private Map<String, Button> navButtons = new HashMap<>();
    private Producto productoEnEdicion = null;

    private final ObservableList<Categoria> categoriasSeleccionadas = FXCollections.observableArrayList();
    private final ObservableList<ProductoProveedor> proveedoresAgregados = FXCollections.observableArrayList();
    private final ObservableList<ProductoCliente> clientesAgregados = FXCollections.observableArrayList();
    private final ObservableList<ProductoFabricante> fabricantesAgregados = FXCollections.observableArrayList();
    private final ObservableList<ProductoEmpresa> empresasAgregadas = FXCollections.observableArrayList();
    private final ObservableList<Servicio> serviciosSeleccionados = FXCollections.observableArrayList();

    /**
     * Muestra la pantalla de gestión de productos
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        monedaService.inicializar();

        BorderPane root = crearContenido();

        if (stage.getScene() != null) {
            stage.getScene().setRoot(root);
        } else {
            Scene scene = new Scene(root, 1280, 850);
            stage.setScene(scene);
            stage.centerOnScreen();
        }
        stage.setTitle("PriceStocker | Gestión de Productos");
        stage.show();
        cargarProductos();
    }

    /**
     * Crea la estructura principal de la pantalla, con el header, tabla y formulario
     * @return
     */
    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader("Gestión de Productos",
                "Administra el inventario y sus relaciones comerciales",
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        HBox contenidoCentral = new HBox(30);
        contenidoCentral.setPadding(new Insets(30));

        VBox panelTabla = crearPanelTabla();
        HBox.setHgrow(panelTabla, Priority.ALWAYS);

        VBox panelFormulario = crearPanelFormulario();
        panelFormulario.setMinWidth(550);
        panelFormulario.setMaxWidth(550);

        contenidoCentral.getChildren().addAll(panelTabla, panelFormulario);
        root.setCenter(contenidoCentral);

        return root;
    }

    /**
     * Crea el panel izquierdo con la tabla de productos y el filtro de búsqueda
     * @return
     */
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        txtFiltro = UIFactory.crearInput("Buscar por nombre, alterno...");
        txtFiltro.setPrefWidth(300);
        txtFiltro.textProperty().addListener(
                (obs, oldVal, newVal) -> tablaProductos.getItems().setAll(productoService.buscarProductos(newVal)));

        HBox topBar = new HBox(10, new Label("Buscar:"), txtFiltro);
        topBar.setAlignment(Pos.CENTER_LEFT);

        tablaProductos = new TableView<>();

        String estiloThumb = "data:text/css,.scroll-bar:vertical .thumb {-fx-background-color: #DADADA; -fx-background-radius: 4;} .scroll-bar:horizontal .thumb {-fx-background-color: #DADADA; -fx-background-radius: 4;}";
        tablaProductos.getStylesheets().add(estiloThumb);

        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaProductos.setStyle(
                "-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        Label lblVacio = new Label("No hay productos registrados aún.");
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaProductos.setPlaceholder(lblVacio);

        TableColumn<Producto, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getIdProducto())));
        colId.setMinWidth(50);
        colId.setMaxWidth(50);
        colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Producto, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProducto()));
        colNombre.setMinWidth(150);

        TableColumn<Producto, String> colAlterno = new TableColumn<>("Alterno");
        colAlterno.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getAlternoProducto() != null ? d.getValue().getAlternoProducto() : "---"));
        colAlterno.setMinWidth(100);

        TableColumn<Producto, String> colPrecio = new TableColumn<>("Precio Base");
        colPrecio.setCellValueFactory(d -> {
            String moneda = d.getValue().getMonedaProducto() != null ? d.getValue().getMonedaProducto() : "MXN";
            String precio = d.getValue().getPrecioProducto() != null
                    ? String.format("$%.2f", d.getValue().getPrecioProducto())
                    : "$0.00";
            return new SimpleStringProperty(precio + " " + moneda);
        });
        colPrecio.setMinWidth(100);

        TableColumn<Producto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(d -> {
            String cats = d.getValue().getCategorias().stream()
                    .map(Categoria::getNombreCategoria)
                    .collect(Collectors.joining(", "));
            return new SimpleStringProperty(cats.isEmpty() ? "---" : cats);
        });
        colCategoria.setMinWidth(120);

        TableColumn<Producto, String> colExistencia = new TableColumn<>("Stock");
        colExistencia.setCellValueFactory(
                d -> new SimpleStringProperty(String.valueOf(d.getValue().getExistenciaProducto())));
        colExistencia.setMinWidth(60);
        colExistencia.setMaxWidth(80);
        colExistencia.setStyle("-fx-alignment: CENTER;");

        TableColumn<Producto, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = UIFactory
                    .crearBotonTablaEditar(() -> prepararEdicion(getTableView().getItems().get(getIndex())));
            private final Button btnEliminar = UIFactory
                    .crearBotonTablaEliminar(() -> eliminarProducto(getTableView().getItems().get(getIndex())));
            private final HBox pane = new HBox(5, btnEditar, btnEliminar);
            {
                pane.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tablaProductos.getColumns()
                .addAll(List.of(colId, colNombre, colAlterno, colPrecio, colCategoria, colExistencia, colAcciones));
        VBox.setVgrow(tablaProductos, Priority.ALWAYS);

        tablaProductos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaProductos.getSelectionModel().getSelectedItem() != null) {
                mostrarDetalleProducto(tablaProductos.getSelectionModel().getSelectedItem());
            }
        });

        box.getChildren().addAll(topBar, tablaProductos);
        return box;
    }

    /**
     * Crea el panel derecho con el formulario para agregar/editar productos y sus relaciones
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        lblTituloFormulario = new Label("Nuevo Producto");
        lblTituloFormulario.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTituloFormulario.setPadding(new Insets(20));

        inicializarInputsGenerales();

        HBox navBar = new HBox(5);
        navBar.setPadding(new Insets(0, 20, 10, 20));
        navBar.setStyle("-fx-border-color: transparent transparent #E5E7EB transparent; -fx-border-width: 0 0 1 0;");

        contentPanel = new BorderPane();
        contentPanel.setPadding(new Insets(20));

        Node vistaGeneral = crearVistaGeneral();

        crearBotonNav("General", navBar, vistaGeneral);

        crearBotonNav("Proveedores", navBar, crearPanelGestionRelacion(
                productoService.obtenerProveedores(), proveedoresAgregados, Proveedor::getNombreProv,
                "Proveedor", "Costo Compra",
                (item, costo, moneda) -> {
                    ProductoProveedor pp = new ProductoProveedor();
                    pp.setProveedor(item);
                    pp.setCosto(costo);
                    pp.setMoneda(moneda);
                    return pp;
                },
                pp -> pp.getProveedor().getNombreProv(), pp -> "$" + pp.getCosto() + " " + pp.getMoneda()));

        crearBotonNav("Clientes", navBar, crearPanelGestionRelacion(
                productoService.obtenerClientes(), clientesAgregados, Cliente::getNombreCliente,
                "Cliente", "Precio Venta",
                (item, costo, moneda) -> {
                    ProductoCliente pc = new ProductoCliente();
                    pc.setCliente(item);
                    pc.setCosto(costo);
                    pc.setMoneda(moneda);
                    return pc;
                },
                pc -> pc.getCliente().getNombreCliente(), pc -> "$" + pc.getCosto() + " " + pc.getMoneda()));

        crearBotonNav("Fabricantes", navBar, crearPanelGestionRelacion(
                productoService.obtenerFabricantes(), fabricantesAgregados, Fabricante::getNombreFabricante,
                "Fabricante", "Costo compra",
                (item, costo, moneda) -> {
                    ProductoFabricante pf = new ProductoFabricante();
                    pf.setFabricante(item);
                    pf.setCosto(costo);
                    pf.setMoneda(moneda);
                    return pf;
                },
                pf -> pf.getFabricante().getNombreFabricante(), pf -> "$" + pf.getCosto() + " " + pf.getMoneda()));

        crearBotonNav("Empresas", navBar, crearPanelGestionRelacion(
                productoService.obtenerEmpresas(), empresasAgregadas, Empresa::getNombreEmpresa,
                "Empresa", "Precio venta",
                (item, costo, moneda) -> {
                    ProductoEmpresa pe = new ProductoEmpresa();
                    pe.setEmpresa(item);
                    pe.setCosto(costo);
                    pe.setMoneda(moneda);
                    return pe;
                },
                pe -> pe.getEmpresa().getNombreEmpresa(), pe -> "$" + pe.getCosto() + " " + pe.getMoneda()));

        crearBotonNav("Servicios", navBar, crearSubFormularioServicios());

        contentPanel.setCenter(vistaGeneral);
        actualizarEstiloBotones("General");

        ScrollPane scrollPane = new ScrollPane(contentPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));
        footer.setStyle("-fx-border-color: #E5E7EB transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        btnGuardar = UIFactory.crearBotonPrimario("Guardar Producto");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> guardarProducto());

        Button btnLimpiar = UIFactory.crearBotonTexto("Limpiar / Cancelar");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        footer.getChildren().addAll(btnGuardar, btnLimpiar);
        card.getChildren().addAll(lblTituloFormulario, navBar, scrollPane, footer);

        return card;
    }

    /**
     * Inicializa los campos de texto, combo y etiquetas para la sección general del formulario
     */
    private void inicializarInputsGenerales() {
        txtNombre = UIFactory.crearInput("Ej. Monitor LED 24 pulgadas");
        txtFicha = UIFactory.crearInput("Ej. Resolución 1080p, HDMI");
        txtAlterno = UIFactory.crearInput("Ej. MON-24-BLK");
        txtExistencia = UIFactory.crearInput("0");
        txtPrecio = UIFactory.crearInput("0.00");

        cmbMoneda = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMoneda.setValue(monedaService.getMonedaPorDefecto());
        cmbMoneda.setPrefWidth(120); 
        cmbMoneda.setStyle(AppTheme.STYLE_INPUT);

        lblConversion = new Label("");
        lblConversion.setStyle("-fx-text-fill: #F97316; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblConversion.setPadding(new Insets(2, 0, 0, 0));

        Runnable actualizarCalculo = () -> {
            calcularEquivalenciaGeneral(txtPrecio, cmbMoneda, lblConversion);
        };

        txtPrecio.textProperty().addListener((o, v, n) -> actualizarCalculo.run());
        cmbMoneda.setOnAction(e -> actualizarCalculo.run());

        UnaryOperator<TextFormatter.Change> filterInt = change -> change.getControlNewText().matches("\\d*") ? change
                : null;
        txtExistencia.setTextFormatter(new TextFormatter<>(filterInt));

        UnaryOperator<TextFormatter.Change> filterDouble = change -> change.getControlNewText()
                .matches("\\d*|\\d+\\.\\d*") ? change : null;
        txtPrecio.setTextFormatter(new TextFormatter<>(filterDouble));
    }

    /**
     * Calcula y muestra la equivalencia del precio ingresado en la moneda seleccionada, usando la moneda preferida del sistema como referencia. Si la moneda seleccionada es la misma que la preferida, no muestra nada. Si no se puede calcular (ej. error en el servicio), tampoco muestra nada
     * @param inputMonto
     * @param comboMoneda
     * @param labelDestino
     */
    private void calcularEquivalenciaGeneral(TextField inputMonto, ComboBox<String> comboMoneda, Label labelDestino) {
        try {
            String textVal = inputMonto.getText().trim();
            if (textVal.isEmpty()) {
                labelDestino.setText("");
                return;
            }
            double val = Double.parseDouble(textVal);
            String monedaSeleccionada = comboMoneda.getValue();

            String monedaPreferida = monedaService.getMonedaPorDefecto();

            if (monedaSeleccionada.equals(monedaPreferida)) {
                labelDestino.setText("");
                return;
            }

            double tipoCambio = monedaService.convertirAMxn(1.0, "USD");
            if (tipoCambio == 0)
                tipoCambio = 20.0;

            if (monedaPreferida.equals("MXN")) {
                double enPesos = val * tipoCambio;
                labelDestino.setText(String.format("≈ $%.2f MXN", enPesos));
            } else {
                double enDolares = val / tipoCambio;
                labelDestino.setText(String.format("≈ $%.2f USD", enDolares));
            }

        } catch (Exception e) {
            labelDestino.setText("");
        }
    }

    /**
     * Formatea un precio mostrando su valor original con la moneda seleccionada, y entre paréntesis su equivalencia aproximada en la moneda preferida del sistema (si es diferente). Si no se puede calcular la equivalencia, solo muestra el valor original
     * @param precio
     * @param monedaItem
     * @return
     */
    private String formatearPrecioInteligente(double precio, String monedaItem) {
        String monedaPref = monedaService.getMonedaPorDefecto();
        String textoOriginal = String.format("$%.2f %s", precio, monedaItem);
        if (monedaItem.equals(monedaPref)) {
            return textoOriginal;
        }
        try {
            double tipoCambio = monedaService.convertirAMxn(1.0, "USD");
            if (tipoCambio == 0)
                tipoCambio = 20.0;

            double precioConvertido;
            String monedaDestino;

            if (monedaPref.equals("MXN")) {
                precioConvertido = precio * tipoCambio;
                monedaDestino = "MXN";
            } else {
                precioConvertido = precio / tipoCambio;
                monedaDestino = "USD";
            }
            return String.format("%s (≈ $%.2f %s)", textoOriginal, precioConvertido, monedaDestino);

        } catch (Exception e) {
            return textoOriginal;
        }
    }

    /**
     * Crea la vista general del formulario
     * @return
     */
    private VBox crearVistaGeneral() {
        HBox precioContainer = new HBox(5, txtPrecio, cmbMoneda);
        HBox.setHgrow(txtPrecio, Priority.ALWAYS);

        VBox grupoPrecio = new VBox(5, new Label("Precio Base *"), precioContainer, lblConversion);
        grupoPrecio.getChildren().get(0)
                .setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");

        VBox grupoExistencia = UIFactory.crearGrupoInput("Existencia Actual *", txtExistencia);
        grupoExistencia.setMaxWidth(260); 

        return new VBox(15,
                UIFactory.crearGrupoInput("Nombre *", txtNombre),
                UIFactory.crearGrupoInput("Descripción", txtFicha),
                UIFactory.crearGrupoInput("Nombre alterno", txtAlterno),
                grupoPrecio,
                grupoExistencia,
                crearSelectorCategorias());
    }

    /**
     * Crea el panel para gestionar las relaciones del producto con proveedores, clientes, fabricantes o empresas. Este panel es reutilizable y se configura con los parámetros que se le pasen (lista de items disponibles en la base de datos, lista de relaciones ya agregadas al producto, funciones para extraer el texto a mostrar en el combo, para construir la nueva relación al agregar, y para mostrar los datos en la tabla)
     * @param <T>
     * @param <R>
     * @param itemsDisponibles
     * @param itemsAgregados
     * @param textExtractor
     * @param labelItem
     * @param labelCosto
     * @param constructorRelacion
     * @param col1Extractor
     * @param col2Extractor
     * @return
     */
    private <T, R> VBox crearPanelGestionRelacion(
            List<T> itemsDisponibles,
            ObservableList<R> itemsAgregados,
            Function<T, String> textExtractor,
            String labelItem,
            String labelCosto,
            TriFunction<T, Double, String, R> constructorRelacion,
            Function<R, String> col1Extractor,
            Function<R, String> col2Extractor) {
        
        VBox root = new VBox(15);

        if (itemsDisponibles.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(20));
            emptyState.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-radius: 6; -fx-border-style: dashed;");
            
            Label lblIcon = new Label("⚠");
            lblIcon.setStyle("-fx-font-size: 24px; -fx-text-fill: #9CA3AF;");
            
            Label lblMsg = new Label("No hay " + labelItem.toLowerCase() + "s registrados.");
            lblMsg.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-font-size: 13px;");
            
            Label lblHint = new Label("Registra uno nuevo en el menú principal para seleccionarlo aquí.");
            lblHint.setWrapText(true);
            lblHint.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-text-alignment: CENTER;");
            
            emptyState.getChildren().addAll(lblIcon, lblMsg, lblHint);
            root.getChildren().add(emptyState);
            
        } else {
            ComboBox<T> cmbItem = new ComboBox<>(FXCollections.observableArrayList(itemsDisponibles));
            configurarCombo(cmbItem, textExtractor);
            cmbItem.setMaxWidth(Double.MAX_VALUE);
            cmbItem.setPromptText("Seleccionar " + labelItem);
            VBox groupItem = UIFactory.crearGrupoInput(labelItem, cmbItem);

            TextField txtCosto = UIFactory.crearInput("0.00");
            ComboBox<String> cmbMon = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
            cmbMon.setValue("MXN");
            cmbMon.setPrefWidth(120); 
            cmbMon.setStyle(AppTheme.STYLE_INPUT);

            Label lblConvInterna = new Label("");
            lblConvInterna.setStyle("-fx-text-fill: #F97316; -fx-font-size: 11px; -fx-font-weight: bold;");

            Runnable calc = () -> calcularEquivalenciaGeneral(txtCosto, cmbMon, lblConvInterna);
            txtCosto.textProperty().addListener((o, v, n) -> calc.run());
            cmbMon.setOnAction(e -> calc.run());
            txtCosto.setTextFormatter(
                    new TextFormatter<>(change -> change.getControlNewText().matches("\\d*|\\d+\\.\\d*") ? change : null));

            HBox costoContainer = new HBox(5, txtCosto, cmbMon);
            HBox.setHgrow(txtCosto, Priority.ALWAYS);
            
            VBox groupCosto = new VBox(5, new Label(labelCosto), costoContainer, lblConvInterna);
            groupCosto.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");

            Button btnAdd = new Button("Añadir");
            btnAdd.setMinWidth(100);
            btnAdd.setStyle(
                    "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6;");

            btnAdd.setOnAction(e -> {
                T itemSel = cmbItem.getValue();
                String costoTxt = txtCosto.getText().trim();
                if (itemSel != null && !costoTxt.isEmpty()) {
                    try {
                        double val = Double.parseDouble(costoTxt);
                        R nuevaRelacion = constructorRelacion.apply(itemSel, val, cmbMon.getValue());
                        itemsAgregados.add(nuevaRelacion);
                        cmbItem.getSelectionModel().clearSelection();
                        txtCosto.setText("0.00");
                        lblConvInterna.setText("");
                    } catch (NumberFormatException ex) {
                    }
                }
            });
            root.getChildren().addAll(groupItem, groupCosto, btnAdd);
        }
        Node tabla = UIFactory.crearTablaRelacion(itemsAgregados, labelItem, col1Extractor, labelCosto, col2Extractor, true);
        root.getChildren().add(tabla);

        return root;
    }

    /**
     * Interfaz para pasar la función constructora de las relaciones en el método crearPanelGestionRelacion, para no tener que repetir código creando cada relación manualmente en cada sección (proveedores, clientes, fabricantes, empresas)
     */
    @FunctionalInterface
    interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    /**
     * Muestra un diálogo con el detalle completo del producto, incluyendo sus relaciones con proveedores, clientes, fabricantes, empresas y servicios. Se muestra al hacer doble click en un producto de la tabla
     * @param p
     */
    private void mostrarDetalleProducto(Producto p) {
        Producto pFull = productoService.obtenerProductoCompleto(p.getIdProducto());
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        BorderPane root = new BorderPane();
        root.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        root.setMinWidth(900);
        root.setMaxWidth(900);
        root.setMaxHeight(750);

        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(25, 25, 0, 25));

        Label lblTitulo = new Label(pFull.getNombreProducto());
        lblTitulo
                .setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(pFull.getFichaProducto() != null ? pFull.getFichaProducto() : "Sin descripción.");
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        topSection.getChildren().addAll(lblTitulo, lblSub, new Separator(), crearPanelDetallesFijos(pFull));
        root.setTop(topSection);

        VBox centerContainer = new VBox(10);
        centerContainer.setPadding(new Insets(10, 25, 10, 25));

        HBox navBar = new HBox(5);
        navBar.setStyle("-fx-border-color: transparent transparent #E5E7EB transparent; -fx-border-width: 0 0 1 0;");
        navBar.setPadding(new Insets(0, 0, 5, 0));

        BorderPane dynamicContent = new BorderPane();
        dynamicContent.setPadding(new Insets(10, 0, 0, 0));
        Map<String, Button> localNavButtons = new HashMap<>();

        Node viewProv = UIFactory.crearTablaRelacion(
                FXCollections.observableArrayList(pFull.getProductoProveedores()), "Proveedor",
                d -> d.getProveedor().getNombreProv(),
                "Costo Compra",
                d -> formatearPrecioInteligente(d.getCosto(), d.getMoneda()),
                false);
        ajustarTablaFullWidth(viewProv);
        crearBotonPopup("Proveedores", viewProv, navBar, dynamicContent, localNavButtons);

        Node viewCli = UIFactory.crearTablaRelacion(
                FXCollections.observableArrayList(pFull.getProductoClientes()), "Cliente",
                d -> d.getCliente().getNombreCliente(),
                "Precio Venta",
                d -> formatearPrecioInteligente(d.getCosto(), d.getMoneda()),
                false);
        ajustarTablaFullWidth(viewCli);
        crearBotonPopup("Clientes", viewCli, navBar, dynamicContent, localNavButtons);

        Node viewFab = UIFactory.crearTablaRelacion(
                FXCollections.observableArrayList(pFull.getProductoFabricantes()), "Fabricante",
                d -> d.getFabricante().getNombreFabricante(),
                "Costo",
                d -> formatearPrecioInteligente(d.getCosto(), d.getMoneda()),
                false);
        ajustarTablaFullWidth(viewFab);
        crearBotonPopup("Fabricantes", viewFab, navBar, dynamicContent, localNavButtons);

        Node viewEmp = UIFactory.crearTablaRelacion(
                FXCollections.observableArrayList(pFull.getProductoEmpresas()), "Empresa",
                d -> d.getEmpresa().getNombreEmpresa(),
                "Costo Mercado",
                d -> formatearPrecioInteligente(d.getCosto(), d.getMoneda()),
                false);
        ajustarTablaFullWidth(viewEmp);
        crearBotonPopup("Empresas", viewEmp, navBar, dynamicContent, localNavButtons);

        Node viewServ = UIFactory.crearTablaRelacion(
                FXCollections.observableArrayList(pFull.getServicios()), "Servicio",
                s -> s.getDescripcionServicio()
                        + (s.getPrestador() != null ? " (" + s.getPrestador().getNombrePrestador() + ")" : ""),
                "Costo Base",
                s -> formatearPrecioInteligente(s.getCostoServicio(), s.getMonedaServicio()),
                false);
        ajustarTablaFullWidth(viewServ);
        crearBotonPopup("Servicios Vinculados", viewServ, navBar, dynamicContent, localNavButtons);

        centerContainer.getChildren().addAll(navBar, dynamicContent);
        root.setCenter(centerContainer);

        if (localNavButtons.containsKey("Proveedores")) {
            localNavButtons.get("Proveedores").fire();
        } else {
            localNavButtons.values().stream().findFirst().ifPresent(Button::fire);
        }

        Button btnCerrar = UIFactory.crearBotonSecundario("Cerrar");
        btnCerrar.setPrefWidth(100);
        btnCerrar.setOnAction(e -> dialog.close());
        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 25, 25, 25));
        root.setBottom(footer);

        dialogService.mostrarDialogoModal(dialog, root, stage);
    }

    /**
     * Ajusta el ancho de las columnas de una tabla para que ocupen todo el espacio disponible
     * @param view
     */
    private void ajustarTablaFullWidth(Node view) {
        if (view instanceof TableView) {
            TableView<?> tv = (TableView<?>) view;

            tv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

            javafx.application.Platform.runLater(() -> {
                int numColumnas = tv.getColumns().size();
                if (numColumnas > 0) {
                    double porcentaje = 1.0 / numColumnas;

                    for (TableColumn<?, ?> col : tv.getColumns()) {
                        col.setMinWidth(0);
                        col.setMaxWidth(Double.MAX_VALUE);

                        col.prefWidthProperty().unbind();

                        col.prefWidthProperty().bind(tv.widthProperty().multiply(porcentaje).subtract(2));
                    }
                }
            });

        } else if (view instanceof Parent) {
            for (Node n : ((Parent) view).getChildrenUnmodifiable()) {
                if (n instanceof TableView || n instanceof Parent) {
                    ajustarTablaFullWidth(n);
                }
            }
        }
    }

    /**
     * Crea el panel con los detalles fijos del producto que se muestran en la parte superior del diálogo de detalle (nombre alterno, existencia, precio base y categorías). Este panel no cambia aunque se navegue entre las diferentes secciones del detalle (proveedores, clientes, fabricantes, empresas, servicios)
     * @param p
     * @return
     */
    private GridPane crearPanelDetallesFijos(Producto p) {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));

        VBox vAlterno = UIFactory.crearDatoDetalle("Nombre alterno",
                (p.getAlternoProducto() != null && !p.getAlternoProducto().isEmpty()) ? p.getAlternoProducto() : "---");

        Label lblExistenciaVal = new Label(String.valueOf(p.getExistenciaProducto()));
        lblExistenciaVal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: "
                + (p.getExistenciaProducto() > 0 ? "#059669" : "#DC2626") + ";");
        VBox vExistencia = new VBox(2, new Label("Existencia Actual"), lblExistenciaVal);
        vExistencia.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        String textoPrecio = formatearPrecioInteligente(p.getPrecioProducto(), p.getMonedaProducto());

        Label lblPrecioVal = new Label(textoPrecio);

        lblPrecioVal.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2563EB;");

        VBox vPrecio = new VBox(2, new Label("Precio Base"), lblPrecioVal);
        vPrecio.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        String catStr = p.getCategorias().stream().map(Categoria::getNombreCategoria).collect(Collectors.joining(", "));
        VBox vCategorias = UIFactory.crearDatoDetalle("Categorías", catStr.isEmpty() ? "Sin categoría" : catStr);

        grid.add(vAlterno, 0, 0);
        grid.add(vExistencia, 1, 0);
        grid.add(vPrecio, 2, 0);
        grid.add(vCategorias, 3, 0);
        return grid;
    }

    /**
     * Guarda el producto con los datos ingresados en el formulario. Si se está editando un producto existente, actualiza ese producto, si no, crea uno nuevo
     */
    private void guardarProducto() {
        if (txtNombre.getText().trim().isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Validación", "El nombre del producto es obligatorio.",
                    stage);
            return;
        }
        if (categoriasSeleccionadas.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Validación", "Seleccione al menos una Categoría.",
                    stage);
            return;
        }
        if (txtPrecio.getText().trim().isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Validación", "El precio base no puede estar vacío.",
                    stage);
            return;
        }

        Producto p = (productoEnEdicion != null) ? productoEnEdicion : new Producto();
        p.setNombreProducto(txtNombre.getText().trim());
        p.setFichaProducto(txtFicha.getText().trim());
        p.setAlternoProducto(txtAlterno.getText().trim());

        try {
            p.setExistenciaProducto(Integer.parseInt(txtExistencia.getText().trim()));
            p.setPrecioProducto(Double.parseDouble(txtPrecio.getText().trim()));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "Verifique los campos numéricos.", stage);
            return;
        }

        p.setMonedaProducto(cmbMoneda.getValue());

        p.setCategorias(new ArrayList<>(categoriasSeleccionadas));
        p.getProductoProveedores().clear();
        p.getProductoProveedores().addAll(proveedoresAgregados);
        p.getProductoClientes().clear();
        p.getProductoClientes().addAll(clientesAgregados);
        p.getProductoFabricantes().clear();
        p.getProductoFabricantes().addAll(fabricantesAgregados);
        p.getProductoEmpresas().clear();
        p.getProductoEmpresas().addAll(empresasAgregadas);
        p.setServicios(new ArrayList<>(serviciosSeleccionados));

        String resultado = productoService.guardarProducto(p);

        if (resultado.toLowerCase().contains("exitosamente")) {
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", resultado, stage);
            limpiarFormulario();
            cargarProductos();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", resultado, stage);
        }
    }

    /**
     * Prepara el formulario para editar un producto existente, cargando sus datos completos (incluyendo relaciones) y mostrando la sección general del formulario. Se llama al hacer click en el botón de editar de cada producto en la tabla
     * @param pResumen
     */
    private void prepararEdicion(Producto pResumen) {
        Producto pFull = productoService.obtenerProductoCompleto(pResumen.getIdProducto());
        this.productoEnEdicion = pFull;

        lblTituloFormulario.setText("Editar Producto (ID: " + pFull.getIdProducto() + ")");
        btnGuardar.setText("Actualizar Producto");

        String estiloNormal = "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        String estiloHover = "-fx-background-color: #1D4ED8; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";

        btnGuardar.setStyle(estiloNormal);
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle(estiloHover));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(estiloNormal));

        txtNombre.setText(pFull.getNombreProducto());
        txtFicha.setText(pFull.getFichaProducto());
        txtAlterno.setText(pFull.getAlternoProducto());
        txtExistencia.setText(String.valueOf(pFull.getExistenciaProducto()));
        txtPrecio.setText(String.valueOf(pFull.getPrecioProducto() != null ? pFull.getPrecioProducto() : "0.00"));

        String monedaGuardada = pFull.getMonedaProducto() != null ? pFull.getMonedaProducto()
                : monedaService.getMonedaPorDefecto();
        cmbMoneda.setValue(monedaGuardada);

        categoriasSeleccionadas.setAll(pFull.getCategorias());
        proveedoresAgregados.setAll(pFull.getProductoProveedores());
        clientesAgregados.setAll(pFull.getProductoClientes());
        fabricantesAgregados.setAll(pFull.getProductoFabricantes());
        empresasAgregadas.setAll(pFull.getProductoEmpresas());
        serviciosSeleccionados.setAll(pFull.getServicios());

        navButtons.get("General").fire();
    }

    /**
     * Elimina un producto después de pedir confirmación al usuario. Si el producto que se está editando es el mismo que se elimina, limpia el formulario para evitar inconsistencias. Se llama al hacer click en el botón de eliminar de cada producto en la tabla
     * @param p
     */
    private void eliminarProducto(Producto p) {
        if (dialogService.mostrarConfirmacion("Eliminar", "¿Deseas eliminar '" + p.getNombreProducto() + "'?", stage)) {
            if (productoService.eliminarProducto(p)) {
                cargarProductos();
                if (productoEnEdicion != null && productoEnEdicion.getIdProducto().equals(p.getIdProducto())) {
                    limpiarFormulario();
                }
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar.", stage);
            }
        }
    }

    /**
     * Limpia el formulario de edición, reseteando todos los campos y listas a su estado inicial para crear un nuevo producto
     */
    private void limpiarFormulario() {
        productoEnEdicion = null;
        lblTituloFormulario.setText("Nuevo Producto");
        btnGuardar.setText("Guardar Producto");

        String estiloNormal = "-fx-background-color: " + AppTheme.COLOR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";

        String estiloHover = "-fx-background-color: #EA580C; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";

        btnGuardar.setStyle(estiloNormal);
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle(estiloHover));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(estiloNormal));

        txtNombre.clear();
        txtFicha.clear();
        txtAlterno.clear();
        txtExistencia.setText("0");
        txtPrecio.setText("0.00");
        cmbMoneda.setValue(monedaService.getMonedaPorDefecto());
        lblConversion.setText("");

        categoriasSeleccionadas.clear();
        proveedoresAgregados.clear();
        clientesAgregados.clear();
        fabricantesAgregados.clear();
        empresasAgregadas.clear();
        serviciosSeleccionados.clear();

        navButtons.get("General").fire();
    }

    /**
     * Carga los productos desde la base de datos y los muestra en la tabla
     */
    private void cargarProductos() {
        tablaProductos.getItems().setAll(productoService.consultarProductos());
    }

    /**
     * Crea un botón para la barra de navegación del formulario, que al hacer click muestra la vista correspondiente en el panel de contenido. También guarda el botón en un mapa para poder actualizar su estilo cuando se navegue entre secciones
     * @param titulo
     * @param container
     * @param view
     */
    private void crearBotonNav(String titulo, HBox container, Node view) {
        Button btn = new Button(titulo);
        btn.setPrefHeight(30);
        btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;");

        btn.setOnAction(e -> {
            contentPanel.setCenter(view);
            actualizarEstiloBotones(titulo);
        });
        container.getChildren().add(btn);
        navButtons.put(titulo, btn);
    }

    /**
     * Actualiza el estilo de los botones de navegación para resaltar el botón activo y mostrar un estilo diferente en los botones inactivos
     * @param activo
     */
    private void actualizarEstiloBotones(String activo) {
        navButtons.forEach((k, btn) -> {
            if (k.equals(activo)) {
                btn.setStyle("-fx-background-color: #FFEDD5; -fx-text-fill: " + AppTheme.COLOR_PRIMARY
                        + "; -fx-font-weight: bold; -fx-cursor: default; -fx-background-radius: 6;");
                btn.setOnMouseEntered(null);
                btn.setOnMouseExited(null);
            } else {
                btn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;");
                btn.setOnMouseEntered(e -> btn.setStyle(
                        "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-background-radius: 6;"));
                btn.setOnMouseExited(e -> btn.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-background-radius: 6;"));
            }
        });
    }

    /**
     * Crea un botón para la barra de navegación del diálogo de detalle del producto, que al hacer click muestra la vista correspondiente en el panel de contenido dinámico
     * @param titulo
     * @param view
     * @param container
     * @param contentArea
     * @param mapButtons
     */
    private void crearBotonPopup(String titulo, Node view, HBox container, BorderPane contentArea,
            Map<String, Button> mapButtons) {
        Button btn = new Button(titulo);
        btn.setPrefHeight(30);
        String styleInactive = "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;";
        btn.setStyle(styleInactive);

        btn.setOnAction(e -> {
            contentArea.setCenter(view);
            mapButtons.forEach((k, b) -> {
                if (b == btn) {
                    b.setStyle("-fx-background-color: #FFEDD5; -fx-text-fill: " + AppTheme.COLOR_PRIMARY
                            + "; -fx-font-weight: bold; -fx-cursor: default; -fx-background-radius: 6;");
                } else {
                    b.setStyle(styleInactive);
                }
            });
        });
        container.getChildren().add(btn);
        mapButtons.put(titulo, btn);
    }

    /**
     * Crea el panel para seleccionar las categorías del producto, permitiendo elegir entre categorías existentes o crear nuevas categorías
     * @return
     */
    private VBox crearSelectorCategorias() {
        VBox box = new VBox(10);
        List<Categoria> listaDb = productoService.obtenerCategorias();
        Label lblTitulo = new Label("Categoría *");
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");

        ComboBox<Categoria> cmbCat = new ComboBox<>(FXCollections.observableArrayList(listaDb));
        configurarCombo(cmbCat, Categoria::getNombreCategoria);
        cmbCat.setPromptText("Seleccionar existente...");
        cmbCat.setMaxWidth(Double.MAX_VALUE);

        TextField txtNueva = UIFactory.crearInput("O escribe una nueva categoría...");
        ListView<Categoria> listCat = new ListView<>(categoriasSeleccionadas);
        listCat.setPrefHeight(100);
        listCat.setStyle("-fx-border-color: #E5E7EB; -fx-border-radius: 4; -fx-font-size: 13px;");

        listCat.setCellFactory(param -> new ListCell<>() {
            private final Button btnEliminar = new Button("X");
            private final Label lblTexto = new Label();
            private final HBox container = new HBox(10, lblTexto, new Region(), btnEliminar);
            {
                container.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(container.getChildren().get(1), Priority.ALWAYS);
                btnEliminar.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #DC2626; -fx-border-radius: 3; -fx-padding: 2 6 2 6;");
                btnEliminar.setOnAction(e -> categoriasSeleccionadas.remove(getItem()));
            }

            @Override
            protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null)
                    setGraphic(null);
                else {
                    lblTexto.setText(item.getNombreCategoria());
                    setGraphic(container);
                }
            }
        });

        Button btnAdd = new Button("Añadir / Crear");
        btnAdd.setMinWidth(120);
        btnAdd.setStyle(
                "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #A7F3D0; -fx-background-radius: 6;");
        btnAdd.setOnAction(e -> {
            String textoNuevo = txtNueva.getText().trim();
            Categoria seleccionCombo = cmbCat.getValue();
            if (!textoNuevo.isEmpty()) {
                Categoria existente = listaDb.stream().filter(c -> c.getNombreCategoria().equalsIgnoreCase(textoNuevo))
                        .findFirst().orElse(null);
                if (existente != null) {
                    if (!categoriasSeleccionadas.contains(existente))
                        categoriasSeleccionadas.add(existente);
                } else {
                    Categoria nuevaCat = new Categoria();
                    nuevaCat.setNombreCategoria(textoNuevo);
                    nuevaCat.setDescripcionCategoria("Creada desde Producto");
                    if (categoriasSeleccionadas.stream()
                            .noneMatch(c -> c.getNombreCategoria().equalsIgnoreCase(textoNuevo)))
                        categoriasSeleccionadas.add(nuevaCat);
                }
                txtNueva.clear();
                cmbCat.getSelectionModel().clearSelection();
            } else if (seleccionCombo != null) {
                if (!categoriasSeleccionadas.contains(seleccionCombo))
                    categoriasSeleccionadas.add(seleccionCombo);
                cmbCat.getSelectionModel().clearSelection();
            }
        });
        HBox rowInput = new HBox(10, txtNueva, btnAdd);
        HBox.setHgrow(txtNueva, Priority.ALWAYS);

        box.getChildren().addAll(lblTitulo, cmbCat, rowInput, listCat);
        return box;
    }

    /**
     * Crea el panel para gestionar los servicios vinculados al producto, permitiendo seleccionar servicios existentes
     * @return
     */
    private VBox crearSubFormularioServicios() {
        VBox box = new VBox(15);
        List<Servicio> lista = productoService.obtenerServicios();

        if (lista.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(20));
            emptyState.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-radius: 6; -fx-border-style: dashed;");
            
            Label lblIcon = new Label("🛠");
            lblIcon.setStyle("-fx-font-size: 24px; -fx-text-fill: #9CA3AF;");
            
            Label lblMsg = new Label("No hay servicios registrados.");
            lblMsg.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-font-size: 13px;");
            
            Label lblHint = new Label("Primero registra Servicios (Instalación, Configuración, etc.) en su módulo.");
            lblHint.setWrapText(true);
            lblHint.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-text-alignment: CENTER;");
            
            emptyState.getChildren().addAll(lblIcon, lblMsg, lblHint);
            box.getChildren().add(emptyState);
            
        } else {
            ComboBox<Servicio> cmb = new ComboBox<>(FXCollections.observableArrayList(lista));
            configurarCombo(cmb, s -> s.getDescripcionServicio()
                    + (s.getPrestador() != null ? " (" + s.getPrestador().getNombrePrestador() + ")" : ""));
            cmb.setMaxWidth(Double.MAX_VALUE);
            cmb.setPromptText("Seleccionar Servicio");

            TextField txtCosto = UIFactory.crearInput("0.00");
            ComboBox<String> cmbMonedaServ = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
            cmbMonedaServ.setValue("MXN");
            cmbMonedaServ.setStyle(AppTheme.STYLE_INPUT);
            cmbMonedaServ.setPrefWidth(120);
            Label lblConvServ = new Label("");
            lblConvServ.setStyle("-fx-text-fill: #F97316; -fx-font-size: 11px; -fx-font-weight: bold;");

            Runnable calc = () -> calcularEquivalenciaGeneral(txtCosto, cmbMonedaServ, lblConvServ);
            txtCosto.textProperty().addListener((o, v, n) -> calc.run());
            cmbMonedaServ.setOnAction(e -> calc.run());

            HBox costoContainer = new HBox(5, txtCosto, cmbMonedaServ);
            VBox groupCosto = new VBox(5, new Label("Costo Base"), costoContainer, lblConvServ);
            HBox.setHgrow(txtCosto, Priority.ALWAYS);

            Button btnAdd = new Button("Añadir Servicio");
            btnAdd.setStyle(
                    "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6;");
            btnAdd.setOnAction(e -> {
                if (cmb.getValue() != null && !serviciosSeleccionados.contains(cmb.getValue())) {
                    Servicio s = cmb.getValue();
                    serviciosSeleccionados.add(s);
                }
            });

            box.getChildren().addAll(UIFactory.crearGrupoInput("Servicio", cmb), groupCosto, btnAdd);
        }

        box.getChildren().add(
                UIFactory.crearTablaRelacion(serviciosSeleccionados, "Servicio", Servicio::getDescripcionServicio,
                        "Costo Base", s -> "$" + s.getCostoServicio() + " " + s.getMonedaServicio(), true));
        
        return box;
    }

    /**
     * Configura un ComboBox para mostrar objetos complejos, utilizando una función extractora para obtener el texto a mostrar de cada objeto. Esto permite reutilizar el mismo método para configurar los ComboBox de categorías, proveedores, clientes, fabricantes, empresas y servicios sin tener que repetir código.
     * @param <T>
     * @param combo
     * @param textExtractor
     */
    private <T> void configurarCombo(ComboBox<T> combo, Function<T, String> textExtractor) {
        combo.setStyle(AppTheme.STYLE_INPUT);
        combo.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object == null ? null : textExtractor.apply(object);
            }

            @Override
            public T fromString(String string) {
                return null;
            }
        });
        combo.setCellFactory(lv -> new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textExtractor.apply(item));
            }
        });
        combo.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textExtractor.apply(item));
            }
        });
    }
}