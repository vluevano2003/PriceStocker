package com.vluevano.view;

import com.vluevano.model.*;
import com.vluevano.service.ProductoService;
import com.vluevano.service.MonedaService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import com.vluevano.view.base.BaseDirectorioView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

@Component
public class ProductoView extends BaseDirectorioView<Producto> {

    @Autowired
    private ProductoService productoService;
    
    @Autowired
    private MonedaService monedaService;

    private TextField txtNombre, txtFicha, txtAlterno, txtExistencia, txtPrecio;
    private ComboBox<String> cmbMoneda;
    private Label lblConversion;

    private BorderPane contentPanel;
    private Map<String, Button> navButtons = new HashMap<>();

    private final ObservableList<Categoria> categoriasSeleccionadas = FXCollections.observableArrayList();
    private final ObservableList<ProductoProveedor> proveedoresAgregados = FXCollections.observableArrayList();
    private final ObservableList<ProductoCliente> clientesAgregados = FXCollections.observableArrayList();
    private final ObservableList<ProductoFabricante> fabricantesAgregados = FXCollections.observableArrayList();
    private final ObservableList<ProductoEmpresa> empresasAgregadas = FXCollections.observableArrayList();
    private final ObservableList<Servicio> serviciosSeleccionados = FXCollections.observableArrayList();

    private static final String MODERN_SCROLL_CSS = "data:text/css," +
        ".scroll-pane { -fx-background-color: transparent; -fx-background: transparent; }" +
        ".scroll-pane > .viewport { -fx-background-color: transparent; }" +
        ".scroll-bar:horizontal { -fx-min-height: 14px; -fx-pref-height: 14px; -fx-max-height: 14px; -fx-background-color: #F3F4F6; -fx-background-radius: 7px; }" +
        ".scroll-bar:horizontal .track { -fx-background-color: transparent; -fx-border-color: transparent; }" +
        ".scroll-bar:horizontal .thumb { -fx-background-color: #D9DCE2; -fx-background-radius: 7px; -fx-background-insets: 2px; }" +
        ".scroll-bar:horizontal:hover .thumb { -fx-background-color: #BEC4CE; }" + 
        ".scroll-bar:horizontal .increment-button, .scroll-bar:horizontal .decrement-button { -fx-padding: 0; -fx-pref-width: 0; }" +
        ".scroll-bar:horizontal .increment-arrow, .scroll-bar:horizontal .decrement-arrow { -fx-shape: null; -fx-padding: 0; }";

    @FunctionalInterface
    interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    /**
     * Sobrescribe el método show para asegurarse de que el servicio de moneda esté inicializado antes de cargar la vista
     */
    @Override
    public void show(Stage stage, String usuarioActual) {
        monedaService.inicializar();
        super.show(stage, usuarioActual);
    }

    @Override
    protected String getTituloVentana() {
        return idioma.get("product.window.title");
    }

    @Override
    protected String getTituloHeader() {
        return idioma.get("product.header.title");
    }

    @Override
    protected String getSubtituloHeader() {
        return idioma.get("product.header.subtitle");
    }

    @Override
    protected String getPromptBusqueda() {
        return idioma.get("product.search.prompt");
    }

    @Override
    protected String getTituloFormularioNuevo() {
        return idioma.get("product.form.new");
    }

    @Override
    protected void cargarDatos() {
        tablaDatos.getItems().setAll(productoService.consultarProductos());
    }

    @Override
    protected void buscarDatos(String valor) {
        tablaDatos.getItems().setAll(productoService.buscarProductos(valor));
    }

    /**
     * Configura las columnas de la tabla de productos, incluyendo una columna de acciones con botones para editar y eliminar cada producto
     */
    @Override
    protected void configurarColumnasTabla() {
        Label lblVacio = new Label(idioma.get("product.table.empty"));
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaDatos.setPlaceholder(lblVacio);

        TableColumn<Producto, String> colId = UIFactory.crearColumna(idioma.get("product.col.id"), d -> String.valueOf(d.getIdProducto()), 50);
        colId.setMaxWidth(50);
        colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<Producto, String> colNombre = UIFactory.crearColumna(idioma.get("product.col.product"), Producto::getNombreProducto, 150);

        TableColumn<Producto, String> colAlterno = UIFactory.crearColumna(idioma.get("product.col.altern"), 
                d -> d.getAlternoProducto() != null ? d.getAlternoProducto() : "---", 100);

        TableColumn<Producto, String> colPrecio = UIFactory.crearColumna(idioma.get("product.col.price"), d -> {
            String moneda = d.getMonedaProducto() != null ? d.getMonedaProducto() : "MXN";
            String precio = d.getPrecioProducto() != null ? String.format("$%.2f", d.getPrecioProducto()) : "$0.00";
            return precio + " " + moneda;
        }, 100);

        TableColumn<Producto, String> colCategoria = UIFactory.crearColumna(idioma.get("product.col.category"), d -> {
            String cats = d.getCategorias().stream().map(Categoria::getNombreCategoria).collect(Collectors.joining(", "));
            return cats.isEmpty() ? "---" : cats;
        }, 120);

        TableColumn<Producto, String> colExistencia = UIFactory.crearColumna(idioma.get("product.col.stock"), 
                d -> String.valueOf(d.getExistenciaProducto()), 60);
        colExistencia.setMaxWidth(80);
        colExistencia.setStyle("-fx-alignment: CENTER;");

        TableColumn<Producto, Void> colAcciones = new TableColumn<>(idioma.get("product.col.actions"));
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = UIFactory.crearBotonTablaEditar(() -> prepararEdicion(getTableView().getItems().get(getIndex())));
            private final Button btnEliminar = UIFactory.crearBotonTablaEliminar(() -> eliminarEntidad(getTableView().getItems().get(getIndex())));
            private final HBox pane = new HBox(5, btnEditar, btnEliminar);
            { pane.setAlignment(Pos.CENTER); }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tablaDatos.getColumns().addAll(List.of(colId, colNombre, colAlterno, colPrecio, colCategoria, colExistencia, colAcciones));
    }

    /**
     * Construye el formulario de creación/edición de productos, con pestañas para detalles generales, proveedores, clientes, fabricantes, empresas y servicios relacionados
     */
    @Override
    protected VBox construirCamposFormulario() {
        inicializarInputsGenerales();

        HBox navBar = new HBox(8);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(0, 0, 10, 0));
        
        ScrollPane scrollNav = new ScrollPane(navBar);
        scrollNav.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollNav.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollNav.setFitToHeight(true);
        scrollNav.setMinHeight(60);
        scrollNav.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 1 0 5 0;");
        scrollNav.getStylesheets().add(MODERN_SCROLL_CSS);

        contentPanel = new BorderPane();
        contentPanel.setPadding(new Insets(15, 0, 0, 0));

        Node vistaGeneral = crearVistaGeneral();
        crearBotonNav("TAB_GENERAL", idioma.get("product.tab.general"), navBar, vistaGeneral);

        crearBotonNav("TAB_PROV", idioma.get("product.tab.suppliers"), navBar, crearPanelGestionRelacion(
                productoService.obtenerProveedores(), proveedoresAgregados, Proveedor::getNombreProv,
                idioma.get("product.rel.supplier"), idioma.get("product.rel.cost_purchase"),
                (item, costo, moneda) -> { ProductoProveedor pp = new ProductoProveedor(); pp.setProveedor(item); pp.setCosto(costo); pp.setMoneda(moneda); return pp; },
                pp -> pp.getProveedor().getNombreProv(), pp -> "$" + pp.getCosto() + " " + pp.getMoneda()));

        crearBotonNav("TAB_CLI", idioma.get("product.tab.clients"), navBar, crearPanelGestionRelacion(
                productoService.obtenerClientes(), clientesAgregados, Cliente::getNombreCliente,
                idioma.get("product.rel.client"), idioma.get("product.rel.price_sale"),
                (item, costo, moneda) -> { ProductoCliente pc = new ProductoCliente(); pc.setCliente(item); pc.setCosto(costo); pc.setMoneda(moneda); return pc; },
                pc -> pc.getCliente().getNombreCliente(), pc -> "$" + pc.getCosto() + " " + pc.getMoneda()));

        crearBotonNav("TAB_FAB", idioma.get("product.tab.manufacturers"), navBar, crearPanelGestionRelacion(
                productoService.obtenerFabricantes(), fabricantesAgregados, Fabricante::getNombreFabricante,
                idioma.get("product.rel.manufacturer"), idioma.get("product.rel.cost_purchase"),
                (item, costo, moneda) -> { ProductoFabricante pf = new ProductoFabricante(); pf.setFabricante(item); pf.setCosto(costo); pf.setMoneda(moneda); return pf; },
                pf -> pf.getFabricante().getNombreFabricante(), pf -> "$" + pf.getCosto() + " " + pf.getMoneda()));

        crearBotonNav("TAB_EMP", idioma.get("product.tab.companies"), navBar, crearPanelGestionRelacion(
                productoService.obtenerEmpresas(), empresasAgregadas, Empresa::getNombreEmpresa,
                idioma.get("product.rel.company"), idioma.get("product.rel.price_sale"),
                (item, costo, moneda) -> { ProductoEmpresa pe = new ProductoEmpresa(); pe.setEmpresa(item); pe.setCosto(costo); pe.setMoneda(moneda); return pe; },
                pe -> pe.getEmpresa().getNombreEmpresa(), pe -> "$" + pe.getCosto() + " " + pe.getMoneda()));

        crearBotonNav("TAB_SERV", idioma.get("product.tab.services"), navBar, crearSubFormularioServicios());

        contentPanel.setCenter(vistaGeneral);
        actualizarEstiloBotones("TAB_GENERAL");

        VBox root = new VBox(5, scrollNav, contentPanel); // Menos espacio aquí porque el Scroll ya tiene MinHeight
        root.setPadding(new Insets(0, 20, 20, 20));
        return root;
    }

    /**
     * Limpia los campos específicos del formulario de producto, restableciendo los valores a sus estados iniciales y limpiando las listas de relaciones
     */
    @Override
    protected void limpiarCamposEspecificos() {
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

        navButtons.get("TAB_GENERAL").fire();
    }

    /**
     * Valida los campos del formulario y construye un objeto Producto con los datos ingresados, incluyendo las relaciones con categorías, proveedores, clientes, fabricantes, empresas y servicios. Luego llama al servicio para guardar el producto y muestra una alerta con el resultado de la operación
     */
    @Override
    protected void registrarEntidad() {
        if (txtNombre.getText().trim().isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("product.msg.validation.title"), idioma.get("product.msg.req_name"), stage);
            return;
        }
        if (categoriasSeleccionadas.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("product.msg.validation.title"), idioma.get("product.msg.req_category"), stage);
            return;
        }
        if (txtPrecio.getText().trim().isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("product.msg.validation.title"), idioma.get("product.msg.req_price"), stage);
            return;
        }

        Producto p = (entidadEnEdicion != null) ? entidadEnEdicion : new Producto();
        p.setNombreProducto(txtNombre.getText().trim());
        p.setFichaProducto(txtFicha.getText().trim());
        p.setAlternoProducto(txtAlterno.getText().trim());

        try {
            p.setExistenciaProducto(Integer.parseInt(txtExistencia.getText().trim()));
            p.setPrecioProducto(Double.parseDouble(txtPrecio.getText().trim()));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("product.msg.error.title"), idioma.get("product.msg.numeric_error"), stage);
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

        if (resultado.contains("exitosamente") || resultado.contains("guardad") || resultado.contains("successfully") || resultado.contains("saved")) {
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("product.msg.success.title"), resultado, stage);
            limpiarFormulario();
            cargarDatos();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("product.msg.error.title"), resultado, stage);
        }
    }

    /**
     * Prepara el formulario para editar un producto existente, cargando todos los datos del producto seleccionado, incluyendo sus relaciones, y actualizando el título y el texto del botón de guardar para reflejar que se está editando un producto en lugar de creando uno nuevo
     * @param pResumen
     */
    private void prepararEdicion(Producto pResumen) {
        Producto pFull = productoService.obtenerProductoCompleto(pResumen.getIdProducto());
        this.entidadEnEdicion = pFull;

        lblTituloFormulario.setText(idioma.get("product.form.edit", pFull.getIdProducto()));
        btnGuardar.setText(idioma.get("product.btn.update"));

        String estiloNormal = "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        btnGuardar.setStyle(estiloNormal);
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(estiloNormal));

        txtNombre.setText(pFull.getNombreProducto());
        txtFicha.setText(pFull.getFichaProducto());
        txtAlterno.setText(pFull.getAlternoProducto());
        txtExistencia.setText(String.valueOf(pFull.getExistenciaProducto()));
        txtPrecio.setText(String.valueOf(pFull.getPrecioProducto() != null ? pFull.getPrecioProducto() : "0.00"));

        String monedaGuardada = pFull.getMonedaProducto() != null ? pFull.getMonedaProducto() : monedaService.getMonedaPorDefecto();
        cmbMoneda.setValue(monedaGuardada);

        categoriasSeleccionadas.setAll(pFull.getCategorias());
        proveedoresAgregados.setAll(pFull.getProductoProveedores());
        clientesAgregados.setAll(pFull.getProductoClientes());
        fabricantesAgregados.setAll(pFull.getProductoFabricantes());
        empresasAgregadas.setAll(pFull.getProductoEmpresas());
        serviciosSeleccionados.setAll(pFull.getServicios());

        navButtons.get("TAB_GENERAL").fire();
    }

    /**
     * Elimina un producto, mostrando una confirmación antes de proceder. Si el producto que se está eliminando es el mismo que se está editando actualmente, limpia el formulario para evitar inconsistencias. Luego llama al servicio para eliminar el producto y muestra una alerta con el resultado de la operación
     * @param p
     */
    private void eliminarEntidad(Producto p) {
        if (entidadEnEdicion != null && entidadEnEdicion.getIdProducto().equals(p.getIdProducto())) {
            limpiarFormulario();
        }
        if (dialogService.mostrarConfirmacion(idioma.get("product.msg.delete.title"), idioma.get("product.msg.delete.confirm", p.getNombreProducto()), stage)) {
            if (productoService.eliminarProducto(p)) {
                cargarDatos();
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("product.msg.error.title"), idioma.get("product.msg.delete.error"), stage);
            }
        }
    }

    /**
     * Muestra un diálogo con los detalles completos de un producto, incluyendo sus relaciones con proveedores, clientes, fabricantes, empresas y servicios. El diálogo tiene una barra de navegación para cambiar entre las diferentes secciones de información relacionada
     */
    @Override
    protected void mostrarDetalle(Producto p) {
        Producto pFull = productoService.obtenerProductoCompleto(p.getIdProducto());
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        root.setMinWidth(900);
        root.setMaxWidth(900);
        root.setMaxHeight(750);

        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(25, 25, 0, 25));

        Label lblTitulo = new Label(pFull.getNombreProducto());
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label((pFull.getFichaProducto() != null && !pFull.getFichaProducto().isEmpty()) ? pFull.getFichaProducto() : idioma.get("product.detail.no_desc"));
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

        Node viewProv = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getProductoProveedores()), idioma.get("product.rel.supplier"), d -> d.getProveedor().getNombreProv(), idioma.get("product.rel.cost_purchase"), d -> formatearPrecioInteligente(d.getCosto(), d.getMoneda()), false);
        ajustarTablaFullWidth(viewProv);
        crearBotonPopup("BTN_PROV", idioma.get("product.tab.suppliers"), viewProv, navBar, dynamicContent, localNavButtons);

        Node viewCli = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getProductoClientes()), idioma.get("product.rel.client"), d -> d.getCliente().getNombreCliente(), idioma.get("product.rel.price_sale"), d -> formatearPrecioInteligente(d.getCosto(), d.getMoneda()), false);
        ajustarTablaFullWidth(viewCli);
        crearBotonPopup("BTN_CLI", idioma.get("product.tab.clients"), viewCli, navBar, dynamicContent, localNavButtons);

        Node viewFab = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getProductoFabricantes()), idioma.get("product.rel.manufacturer"), d -> d.getFabricante().getNombreFabricante(), idioma.get("product.rel.cost_purchase"), d -> formatearPrecioInteligente(d.getCosto(), d.getMoneda()), false);
        ajustarTablaFullWidth(viewFab);
        crearBotonPopup("BTN_FAB", idioma.get("product.tab.manufacturers"), viewFab, navBar, dynamicContent, localNavButtons);

        Node viewEmp = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getProductoEmpresas()), idioma.get("product.rel.company"), d -> d.getEmpresa().getNombreEmpresa(), idioma.get("product.rel.cost_market"), d -> formatearPrecioInteligente(d.getCosto(), d.getMoneda()), false);
        ajustarTablaFullWidth(viewEmp);
        crearBotonPopup("BTN_EMP", idioma.get("product.tab.companies"), viewEmp, navBar, dynamicContent, localNavButtons);

        Node viewServ = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getServicios()), idioma.get("product.rel.service"), s -> s.getDescripcionServicio() + (s.getPrestador() != null ? " (" + s.getPrestador().getNombrePrestador() + ")" : ""), idioma.get("product.lbl.base_price"), s -> formatearPrecioInteligente(s.getCostoServicio(), s.getMonedaServicio()), false);
        ajustarTablaFullWidth(viewServ);
        crearBotonPopup("BTN_SERV", idioma.get("product.tab.linked_services"), viewServ, navBar, dynamicContent, localNavButtons);

        centerContainer.getChildren().addAll(navBar, dynamicContent);
        root.setCenter(centerContainer);

        if (localNavButtons.containsKey("BTN_PROV")) {
            localNavButtons.get("BTN_PROV").fire();
        } else {
            localNavButtons.values().stream().findFirst().ifPresent(Button::fire);
        }

        Button btnCerrar = UIFactory.crearBotonSecundario(idioma.get("product.btn.close"));
        btnCerrar.setPrefWidth(100);
        btnCerrar.setOnAction(e -> dialog.close());
        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(10, 25, 25, 25));
        root.setBottom(footer);

        dialogService.mostrarDialogoModal(dialog, root, stage);
    }

    /**
     * Inicializa los campos de entrada del formulario general del producto, incluyendo validaciones para los campos numéricos y un cálculo dinámico de equivalencia de moneda basado en la selección del usuario. También configura un listener para actualizar la conversión cada vez que el usuario cambia el precio o la moneda seleccionada
     */
    private void inicializarInputsGenerales() {
        txtNombre = UIFactory.crearInput(idioma.get("product.placeholder.name"));
        txtFicha = UIFactory.crearInput(idioma.get("product.placeholder.desc"));
        txtAlterno = UIFactory.crearInput(idioma.get("product.placeholder.altern"));
        txtExistencia = UIFactory.crearInput("0");
        txtPrecio = UIFactory.crearInput("0.00");

        cmbMoneda = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMoneda.setValue(monedaService.getMonedaPorDefecto());
        cmbMoneda.setPrefWidth(120);
        cmbMoneda.setStyle(AppTheme.STYLE_INPUT);

        lblConversion = new Label("");
        lblConversion.setStyle("-fx-text-fill: #F97316; -fx-font-size: 13px; -fx-font-weight: bold;");
        lblConversion.setPadding(new Insets(2, 0, 0, 0));

        Runnable actualizarCalculo = () -> calcularEquivalenciaGeneral(txtPrecio, cmbMoneda, lblConversion);
        txtPrecio.textProperty().addListener((o, v, n) -> actualizarCalculo.run());
        cmbMoneda.setOnAction(e -> actualizarCalculo.run());

        UnaryOperator<TextFormatter.Change> filterInt = change -> change.getControlNewText().matches("\\d*") ? change : null;
        txtExistencia.setTextFormatter(new TextFormatter<>(filterInt));

        UnaryOperator<TextFormatter.Change> filterDouble = change -> change.getControlNewText().matches("\\d*|\\d+\\.\\d*") ? change : null;
        txtPrecio.setTextFormatter(new TextFormatter<>(filterDouble));
    }

    /**
     * Calcula la equivalencia del precio ingresado en el campo de precio general, convirtiendo entre MXN y USD según la moneda seleccionada y la moneda preferida del sistema. Actualiza el label de conversión para mostrar el valor equivalente en la otra moneda, utilizando el tipo de cambio obtenido del servicio de moneda. Si el campo de precio está vacío o contiene un valor no numérico, limpia el label de conversión
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
            if (tipoCambio == 0) tipoCambio = 20.0;

            if (monedaPreferida.equals("MXN")) {
                labelDestino.setText(String.format("≈ $%.2f MXN", val * tipoCambio));
            } else {
                labelDestino.setText(String.format("≈ $%.2f USD", val / tipoCambio));
            }
        } catch (Exception e) {
            labelDestino.setText("");
        }
    }

    /**
     * Formatea el precio de manera inteligente para mostrar tanto el valor original con su moneda como una equivalencia aproximada en la moneda preferida del sistema, utilizando el tipo de cambio actual. Si la moneda del precio ya es la misma que la moneda preferida, simplemente muestra el precio original sin conversión. Si ocurre algún error al obtener el tipo de cambio o realizar la conversión, devuelve el texto original sin formato adicional
     * @param precio
     * @param monedaItem
     * @return
     */
    private String formatearPrecioInteligente(double precio, String monedaItem) {
        String monedaPref = monedaService.getMonedaPorDefecto();
        String textoOriginal = String.format("$%.2f %s", precio, monedaItem);
        if (monedaItem.equals(monedaPref)) return textoOriginal;
        
        try {
            double tipoCambio = monedaService.convertirAMxn(1.0, "USD");
            if (tipoCambio == 0) tipoCambio = 20.0;

            double precioConvertido = monedaPref.equals("MXN") ? (precio * tipoCambio) : (precio / tipoCambio);
            return String.format("%s (≈ $%.2f %s)", textoOriginal, precioConvertido, monedaPref);
        } catch (Exception e) {
            return textoOriginal;
        }
    }

    /**
     * Construye la vista general del formulario de producto, organizando los campos de entrada para el nombre, ficha técnica, alterno, precio y existencia, junto con un selector de categorías. Aplica estilos para resaltar los títulos de cada sección y asegurar una presentación clara y organizada de la información general del producto
     * @return
     */
    private VBox crearVistaGeneral() {
        HBox precioContainer = new HBox(5, txtPrecio, cmbMoneda);
        HBox.setHgrow(txtPrecio, Priority.ALWAYS);

        VBox grupoPrecio = new VBox(5, new Label(idioma.get("product.lbl.base_price")), precioContainer, lblConversion);
        grupoPrecio.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");

        VBox grupoExistencia = UIFactory.crearGrupoInput(idioma.get("product.lbl.stock"), txtExistencia);
        grupoExistencia.setMaxWidth(260);

        return new VBox(15,
                UIFactory.crearGrupoInput(idioma.get("product.lbl.name"), txtNombre),
                UIFactory.crearGrupoInput(idioma.get("product.lbl.desc"), txtFicha),
                UIFactory.crearGrupoInput(idioma.get("product.lbl.altern"), txtAlterno),
                grupoPrecio,
                grupoExistencia,
                crearSelectorCategorias());
    }

    /**
     * Crea un panel para gestionar las relaciones entre el producto y otras entidades (proveedores, clientes, fabricantes, empresas), permitiendo al usuario seleccionar un ítem disponible, ingresar un costo o precio asociado, y agregarlo a la lista de relaciones del producto. El panel también muestra una tabla con las relaciones ya agregadas, y maneja el caso en que no haya ítems disponibles para relacionar mostrando un estado vacío informativo
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
    private <T, R> VBox crearPanelGestionRelacion(List<T> itemsDisponibles, ObservableList<R> itemsAgregados,
            Function<T, String> textExtractor, String labelItem, String labelCosto,
            TriFunction<T, Double, String, R> constructorRelacion,
            Function<R, String> col1Extractor, Function<R, String> col2Extractor) {

        VBox root = new VBox(15);
        if (itemsDisponibles.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setPadding(new Insets(20));
            emptyState.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; -fx-border-radius: 6; -fx-border-style: dashed;");

            Label lblIcon = new Label("⚠");
            lblIcon.setStyle("-fx-font-size: 24px; -fx-text-fill: #9CA3AF;");
            Label lblMsg = new Label(idioma.get("product.rel.empty_msg", labelItem.toLowerCase()));
            lblMsg.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-font-size: 13px;");
            Label lblHint = new Label(idioma.get("product.rel.empty_hint"));
            lblHint.setWrapText(true);
            lblHint.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-text-alignment: CENTER;");

            emptyState.getChildren().addAll(lblIcon, lblMsg, lblHint);
            root.getChildren().add(emptyState);
        } else {
            ComboBox<T> cmbItem = new ComboBox<>(FXCollections.observableArrayList(itemsDisponibles));
            configurarCombo(cmbItem, textExtractor);
            cmbItem.setMaxWidth(Double.MAX_VALUE);
            cmbItem.setPromptText(idioma.get("product.rel.select_prompt", labelItem));
            
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
            txtCosto.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*|\\d+\\.\\d*") ? change : null));

            HBox costoContainer = new HBox(5, txtCosto, cmbMon);
            HBox.setHgrow(txtCosto, Priority.ALWAYS);

            VBox groupCosto = new VBox(5, new Label(labelCosto), costoContainer, lblConvInterna);
            groupCosto.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");

            Button btnAdd = new Button(idioma.get("product.btn.add_rel"));
            btnAdd.setMinWidth(100);
            btnAdd.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6;");

            btnAdd.setOnAction(e -> {
                T itemSel = cmbItem.getValue();
                String costoTxt = txtCosto.getText().trim();
                if (itemSel != null && !costoTxt.isEmpty()) {
                    try {
                        double val = Double.parseDouble(costoTxt);
                        itemsAgregados.add(constructorRelacion.apply(itemSel, val, cmbMon.getValue()));
                        cmbItem.getSelectionModel().clearSelection();
                        txtCosto.setText("0.00");
                        lblConvInterna.setText("");
                    } catch (NumberFormatException ex) {}
                }
            });
            root.getChildren().addAll(UIFactory.crearGrupoInput(labelItem, cmbItem), groupCosto, btnAdd);
        }
        
        root.getChildren().add(UIFactory.crearTablaRelacion(itemsAgregados, labelItem, col1Extractor, labelCosto, col2Extractor, true));
        return root;
    }

    /**
     * Crea un panel con información detallada de un producto, mostrando campos fijos como el código alterno, existencia, precio base y categorías asociadas. Aplica estilos para resaltar los valores importantes como el precio y la existencia, utilizando colores para indicar si el producto está en stock o agotado. También maneja el caso en que no haya información disponible para ciertos campos mostrando un texto predeterminado
     * @param p
     * @return
     */
    private GridPane crearPanelDetallesFijos(Producto p) {
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));

        VBox vAlterno = UIFactory.crearDatoDetalle(idioma.get("product.detail.altern"), (p.getAlternoProducto() != null && !p.getAlternoProducto().isEmpty()) ? p.getAlternoProducto() : "---");

        Label lblExistenciaVal = new Label(String.valueOf(p.getExistenciaProducto()));
        lblExistenciaVal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + (p.getExistenciaProducto() > 0 ? "#059669" : "#DC2626") + ";");
        VBox vExistencia = new VBox(2, new Label(idioma.get("product.detail.stock")), lblExistenciaVal);
        vExistencia.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        Label lblPrecioVal = new Label(formatearPrecioInteligente(p.getPrecioProducto(), p.getMonedaProducto()));
        lblPrecioVal.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #2563EB;");
        VBox vPrecio = new VBox(2, new Label(idioma.get("product.detail.base_price")), lblPrecioVal);
        vPrecio.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        String catStr = p.getCategorias().stream().map(Categoria::getNombreCategoria).collect(Collectors.joining(", "));
        VBox vCategorias = UIFactory.crearDatoDetalle(idioma.get("product.detail.categories"), catStr.isEmpty() ? idioma.get("product.detail.no_category") : catStr);

        grid.add(vAlterno, 0, 0);
        grid.add(vExistencia, 1, 0);
        grid.add(vPrecio, 2, 0);
        grid.add(vCategorias, 3, 0);
        return grid;
    }

    /**
     * Crea un botón de navegación para las pestañas del formulario de producto, configurando su estilo y su acción para mostrar la vista correspondiente en el panel de contenido. También mantiene un mapa de botones para actualizar su estilo cuando se selecciona una pestaña, resaltando la pestaña activa y aplicando un estilo diferente a las pestañas inactivas
     * @param id
     * @param titulo
     * @param container
     * @param view
     */
    private void crearBotonNav(String id, String titulo, HBox container, Node view) {
        Button btn = new Button(titulo);
        btn.setPrefHeight(30);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;");

        btn.setOnAction(e -> {
            contentPanel.setCenter(view);
            actualizarEstiloBotones(id);
        });
        container.getChildren().add(btn);
        navButtons.put(id, btn);
    }

    /**
     * Actualiza el estilo de los botones de navegación para resaltar el botón activo y aplicar un estilo diferente a los botones inactivos. El botón activo tendrá un fondo resaltado, texto del color primario y un peso de fuente más alto, mientras que los botones inactivos tendrán un fondo transparente, texto gris y un efecto hover para mejorar la experiencia del usuario
     * @param activo
     */
    private void actualizarEstiloBotones(String activo) {
        navButtons.forEach((k, btn) -> {
            if (k.equals(activo)) {
                btn.setStyle("-fx-background-color: #FFEDD5; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + "; -fx-font-weight: bold; -fx-cursor: default; -fx-background-radius: 6;");
                btn.setOnMouseEntered(null);
                btn.setOnMouseExited(null);
            } else {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;");
                btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-background-radius: 6;"));
                btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-background-radius: 6;"));
            }
        });
    }

    /**
     * Crea un botón para las vistas de detalle del producto, configurando su acción para mostrar la vista correspondiente en el panel de contenido y actualizando el estilo de los botones para resaltar el botón activo. Este método es similar a crearBotonNav pero se utiliza específicamente para las vistas de detalle que se muestran en un diálogo modal, manteniendo un mapa local de botones para manejar el estilo dentro del contexto del diálogo
     * @param id
     * @param titulo
     * @param view
     * @param container
     * @param contentArea
     * @param mapButtons
     */
    private void crearBotonPopup(String id, String titulo, Node view, HBox container, BorderPane contentArea, Map<String, Button> mapButtons) {
        Button btn = new Button(titulo);
        btn.setPrefHeight(30);
        String styleInactive = "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;";
        btn.setStyle(styleInactive);

        btn.setOnAction(e -> {
            contentArea.setCenter(view);
            mapButtons.forEach((k, b) -> {
                if (b == btn) {
                    b.setStyle("-fx-background-color: #FFEDD5; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + "; -fx-font-weight: bold; -fx-cursor: default; -fx-background-radius: 6;");
                } else {
                    b.setStyle(styleInactive);
                }
            });
        });
        container.getChildren().add(btn);
        mapButtons.put(id, btn);
    }

    /**
     * Crea un panel para seleccionar y gestionar las categorías asociadas a un producto, permitiendo al usuario elegir entre las categorías existentes o crear una nueva categoría directamente desde el formulario. El panel muestra una lista de categorías seleccionadas con la opción de eliminar cada categoría individualmente, y maneja la lógica para evitar duplicados tanto en las categorías existentes como en las nuevas categorías creadas por el usuario
     * @return
     */
    private VBox crearSelectorCategorias() {
        VBox box = new VBox(10);
        List<Categoria> listaDb = productoService.obtenerCategorias();
        Label lblTitulo = new Label(idioma.get("product.lbl.category"));
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");

        ComboBox<Categoria> cmbCat = new ComboBox<>(FXCollections.observableArrayList(listaDb));
        configurarCombo(cmbCat, Categoria::getNombreCategoria);
        cmbCat.setPromptText(idioma.get("product.cat.select_prompt"));
        cmbCat.setMaxWidth(Double.MAX_VALUE);

        TextField txtNueva = UIFactory.crearInput(idioma.get("product.cat.new_prompt"));
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
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #DC2626; -fx-border-radius: 3; -fx-padding: 2 6 2 6;");
                btnEliminar.setOnAction(e -> categoriasSeleccionadas.remove(getItem()));
            }
            @Override protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setGraphic(null);
                else { lblTexto.setText(item.getNombreCategoria()); setGraphic(container); }
            }
        });

        Button btnAdd = new Button(idioma.get("product.cat.btn_add"));
        btnAdd.setMinWidth(120);
        btnAdd.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #A7F3D0; -fx-background-radius: 6;");
        btnAdd.setOnAction(e -> {
            String textoNuevo = txtNueva.getText().trim();
            Categoria seleccionCombo = cmbCat.getValue();
            if (!textoNuevo.isEmpty()) {
                Categoria existente = listaDb.stream().filter(c -> c.getNombreCategoria().equalsIgnoreCase(textoNuevo)).findFirst().orElse(null);
                if (existente != null) {
                    if (!categoriasSeleccionadas.contains(existente)) categoriasSeleccionadas.add(existente);
                } else {
                    Categoria nuevaCat = new Categoria();
                    nuevaCat.setNombreCategoria(textoNuevo);
                    nuevaCat.setDescripcionCategoria("Creada desde Producto");
                    if (categoriasSeleccionadas.stream().noneMatch(c -> c.getNombreCategoria().equalsIgnoreCase(textoNuevo)))
                        categoriasSeleccionadas.add(nuevaCat);
                }
                txtNueva.clear();
                cmbCat.getSelectionModel().clearSelection();
            } else if (seleccionCombo != null) {
                if (!categoriasSeleccionadas.contains(seleccionCombo)) categoriasSeleccionadas.add(seleccionCombo);
                cmbCat.getSelectionModel().clearSelection();
            }
        });
        
        HBox rowInput = new HBox(10, txtNueva, btnAdd);
        HBox.setHgrow(txtNueva, Priority.ALWAYS);

        box.getChildren().addAll(lblTitulo, cmbCat, rowInput, listCat);
        return box;
    }

    /**
     * Crea un panel para gestionar los servicios relacionados con el producto, permitiendo al usuario seleccionar un servicio existente, ingresar un costo base para ese servicio y agregarlo a la lista de servicios relacionados del producto. El panel también muestra una tabla con los servicios ya relacionados, incluyendo su descripción y costo base, y maneja el caso en que no haya servicios disponibles para relacionar mostrando un estado vacío informativo
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
            Label lblMsg = new Label(idioma.get("product.srv.empty_msg"));
            lblMsg.setStyle("-fx-font-weight: bold; -fx-text-fill: #6B7280; -fx-font-size: 13px;");
            Label lblHint = new Label(idioma.get("product.srv.empty_hint"));
            lblHint.setWrapText(true);
            lblHint.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px; -fx-text-alignment: CENTER;");

            emptyState.getChildren().addAll(lblIcon, lblMsg, lblHint);
            box.getChildren().add(emptyState);
        } else {
            ComboBox<Servicio> cmb = new ComboBox<>(FXCollections.observableArrayList(lista));
            configurarCombo(cmb, s -> s.getDescripcionServicio() + (s.getPrestador() != null ? " (" + s.getPrestador().getNombrePrestador() + ")" : ""));
            cmb.setMaxWidth(Double.MAX_VALUE);
            cmb.setPromptText(idioma.get("product.srv.select_prompt"));

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
            VBox groupCosto = new VBox(5, new Label(idioma.get("product.lbl.base_price")), costoContainer, lblConvServ);
            HBox.setHgrow(txtCosto, Priority.ALWAYS);

            Button btnAdd = new Button(idioma.get("product.btn.add_srv"));
            btnAdd.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6;");
            btnAdd.setOnAction(e -> {
                if (cmb.getValue() != null && !serviciosSeleccionados.contains(cmb.getValue())) {
                    serviciosSeleccionados.add(cmb.getValue());
                }
            });

            box.getChildren().addAll(UIFactory.crearGrupoInput(idioma.get("product.rel.service"), cmb), groupCosto, btnAdd);
        }

        box.getChildren().add(UIFactory.crearTablaRelacion(serviciosSeleccionados, idioma.get("product.rel.service"),
                Servicio::getDescripcionServicio, idioma.get("product.lbl.base_price"),
                s -> "$" + s.getCostoServicio() + " " + s.getMonedaServicio(), true));

        return box;
    }

    /**
     * Configura un ComboBox para mostrar objetos complejos utilizando un extractor de texto personalizado. Este método establece un StringConverter para convertir los objetos a su representación de texto en el ComboBox, y también configura una celda personalizada para mostrar el texto extraído en la lista desplegable y en el botón del ComboBox. Esto permite que el ComboBox muestre información relevante de los objetos sin necesidad de que los objetos tengan un método toString() adecuado
     * @param <T>
     * @param combo
     * @param textExtractor
     */
    private <T> void configurarCombo(ComboBox<T> combo, Function<T, String> textExtractor) {
        combo.setStyle(AppTheme.STYLE_INPUT);
        combo.setConverter(new StringConverter<T>() {
            @Override public String toString(T object) { return object == null ? null : textExtractor.apply(object); }
            @Override public T fromString(String string) { return null; }
        });
        combo.setCellFactory(lv -> new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textExtractor.apply(item));
            }
        });
        combo.setButtonCell(new ListCell<T>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textExtractor.apply(item));
            }
        });
    }
    
    /**
     * Ajusta el ancho de las columnas de una TableView para que ocupen todo el espacio disponible, distribuyendo el ancho de manera equitativa entre las columnas. Este método se aplica recursivamente a cualquier TableView encontrada dentro del nodo proporcionado, lo que permite que las tablas dentro de los paneles de detalle del producto se ajusten correctamente al tamaño del diálogo modal
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
                if (n instanceof TableView || n instanceof Parent) ajustarTablaFullWidth(n);
            }
        }
    }
}