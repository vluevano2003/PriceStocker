package com.vluevano.view;

import com.vluevano.model.*;
import com.vluevano.service.DialogService;
import com.vluevano.service.ProductoService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProductoView {

    @Autowired private ProductoService productoService;
    @Autowired private DialogService dialogService;
    @Autowired @Lazy private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;
    private TableView<Producto> tablaProductos;
    private TextField txtFiltro;

    // Campos del formulario
    private TextField txtNombre, txtFicha, txtAlterno, txtExistencia;
    private Label lblTituloFormulario;
    private Button btnGuardar;
    private Producto productoEnEdicion = null;

    // Listas observables (Binding con la UI)
    private ObservableList<Categoria> categoriasSeleccionadas = FXCollections.observableArrayList();
    private ObservableList<ProductoProveedor> proveedoresAgregados = FXCollections.observableArrayList();
    private ObservableList<ProductoCliente> clientesAgregados = FXCollections.observableArrayList();
    private ObservableList<ProductoFabricante> fabricantesAgregados = FXCollections.observableArrayList();
    private ObservableList<ProductoEmpresa> empresasAgregadas = FXCollections.observableArrayList();
    private ObservableList<Servicio> serviciosSeleccionados = FXCollections.observableArrayList();

    // Cache de vistas para los "Tabs"
    private VBox viewGeneral, viewProveedores, viewClientes, viewFabricantes, viewEmpresas, viewServicios;
    private BorderPane contentPanel;
    private Map<String, Button> navButtons;

    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

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

    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader("Gestión de Productos", 
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        HBox contenidoCentral = new HBox(30);
        contenidoCentral.setPadding(new Insets(30));

        // Panel de Tabla (Izquierda)
        VBox panelTabla = crearPanelTabla();
        HBox.setHgrow(panelTabla, Priority.ALWAYS);

        // Panel de Formulario (Derecha)
        VBox panelFormulario = crearPanelFormulario();
        panelFormulario.setMinWidth(550); // Un poco más ancho para acomodar mejor los botones
        panelFormulario.setMaxWidth(550);

        contenidoCentral.getChildren().addAll(panelTabla, panelFormulario);
        root.setCenter(contenidoCentral);

        return root;
    }

    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        txtFiltro = UIFactory.crearInput("Buscar por nombre, código...");
        txtFiltro.setPrefWidth(300);
        txtFiltro.textProperty().addListener((obs, oldVal, newVal) -> 
            tablaProductos.getItems().setAll(productoService.buscarProductos(newVal)));

        HBox topBar = new HBox(10, new Label("Buscar:"), txtFiltro);
        topBar.setAlignment(Pos.CENTER_LEFT);

        tablaProductos = new TableView<>();
        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaProductos.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        TableColumn<Producto, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProducto()));
        
        TableColumn<Producto, String> colExistencia = new TableColumn<>("Exist.");
        colExistencia.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getExistenciaProducto())));
        colExistencia.setMaxWidth(60);
        colExistencia.setMinWidth(60);
        colExistencia.setStyle("-fx-alignment: CENTER;");

        TableColumn<Producto, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = UIFactory.crearBotonTablaEditar(() -> prepararEdicion(getTableView().getItems().get(getIndex())));
            private final Button btnEliminar = UIFactory.crearBotonTablaEliminar(() -> eliminarProducto(getTableView().getItems().get(getIndex())));
            private final HBox pane = new HBox(5, btnEditar, btnEliminar);
            { pane.setAlignment(Pos.CENTER); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tablaProductos.getColumns().addAll(colNombre, colExistencia, colAcciones);
        VBox.setVgrow(tablaProductos, Priority.ALWAYS);

        // Doble clic para ver detalles
        tablaProductos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaProductos.getSelectionModel().getSelectedItem() != null) {
                mostrarDetalleProducto(tablaProductos.getSelectionModel().getSelectedItem());
            }
        });

        box.getChildren().addAll(topBar, tablaProductos);
        return box;
    }

    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        lblTituloFormulario = new Label("Nuevo Producto");
        lblTituloFormulario.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTituloFormulario.setPadding(new Insets(20));

        // Inicializar vistas internas
        inicializarVistasFormulario();

        // Crear Barra de Navegación (Tabs estilo Botones)
        HBox navBar = new HBox(5);
        navBar.setPadding(new Insets(0, 20, 10, 20));
        navBar.setStyle("-fx-border-color: transparent transparent #E5E7EB transparent; -fx-border-width: 0 0 1 0;");
        
        navButtons = new HashMap<>();
        // Creamos los botones de navegación
        crearBotonNav("General", navBar, viewGeneral);
        crearBotonNav("Proveedores", navBar, viewProveedores);
        crearBotonNav("Clientes", navBar, viewClientes);
        crearBotonNav("Fabricantes", navBar, viewFabricantes);
        crearBotonNav("Empresas", navBar, viewEmpresas);
        crearBotonNav("Servicios", navBar, viewServicios);

        // Panel donde se mostrará el contenido dinámico
        contentPanel = new BorderPane();
        contentPanel.setPadding(new Insets(20));
        contentPanel.setCenter(viewGeneral); // Por defecto
        actualizarEstiloBotones("General");

        ScrollPane scrollPane = new ScrollPane(contentPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // --- Footer ---
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

    // Método helper para crear botones de navegación
    private void crearBotonNav(String titulo, HBox container, Node view) {
        Button btn = new Button(titulo);
        btn.setPrefHeight(30);
        // Estilo base
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;");
        
        btn.setOnAction(e -> {
            contentPanel.setCenter(view);
            actualizarEstiloBotones(titulo);
        });
        
        container.getChildren().add(btn);
        navButtons.put(titulo, btn);
    }

    private void actualizarEstiloBotones(String activo) {
        navButtons.forEach((k, btn) -> {
            if (k.equals(activo)) {
                // Estilo Activo: Color primario suave de fondo, texto primario
                btn.setStyle("-fx-background-color: #FFEDD5; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + "; -fx-font-weight: bold; -fx-cursor: default; -fx-background-radius: 6;");
            } else {
                // Estilo Inactivo
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;");
                btn.setOnMouseEntered(e -> {
                    if(!btn.getText().equals(activo)) 
                        btn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-background-radius: 6;");
                });
                btn.setOnMouseExited(e -> {
                    if(!btn.getText().equals(activo)) 
                        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-background-radius: 6;");
                });
            }
        });
    }

    private void inicializarVistasFormulario() {
        // 1. General
        txtNombre = UIFactory.crearInput("Nombre del Producto *");
        txtFicha = UIFactory.crearInput("Descripción / Ficha");
        txtAlterno = UIFactory.crearInput("Código Alterno");
        txtExistencia = UIFactory.crearInput("0");
        
        viewGeneral = new VBox(15,
            UIFactory.crearGrupoInput("Nombre *", txtNombre),
            UIFactory.crearGrupoInput("Ficha Técnica", txtFicha),
            new HBox(15, 
                UIFactory.crearGrupoInput("Alterno", txtAlterno),
                UIFactory.crearGrupoInput("Existencia", txtExistencia)
            ),
            crearSelectorCategorias()
        );

        // 2. Otras Vistas
        viewProveedores = crearSubFormularioProveedores();
        viewClientes = crearSubFormularioClientes();
        viewFabricantes = crearSubFormularioFabricantes();
        viewEmpresas = crearSubFormularioEmpresas();
        viewServicios = crearSubFormularioServicios();
    }

    // ==========================================
    // SUB-FORMULARIOS (Relaciones)
    // ==========================================

    private VBox crearSelectorCategorias() {
        VBox box = new VBox(10);
        ComboBox<Categoria> cmbCat = new ComboBox<>();
        cmbCat.setItems(FXCollections.observableArrayList(productoService.obtenerCategorias()));
        configurarCombo(cmbCat, Categoria::getNombreCategoria);
        cmbCat.setPromptText("Seleccionar Categoría");
        cmbCat.setMaxWidth(Double.MAX_VALUE);
        
        ListView<String> listCat = new ListView<>();
        listCat.setPrefHeight(100);
        listCat.setStyle("-fx-border-color: #E5E7EB; -fx-border-radius: 4; -fx-font-size: 13px;");
        
        actualizarListaString(listCat, categoriasSeleccionadas, Categoria::getNombreCategoria);
        
        Button btnAdd = UIFactory.crearBotonSecundario("Añadir Categoría");
        btnAdd.setOnAction(e -> {
            if(cmbCat.getValue() != null && !categoriasSeleccionadas.contains(cmbCat.getValue())) {
                categoriasSeleccionadas.add(cmbCat.getValue());
                actualizarListaString(listCat, categoriasSeleccionadas, Categoria::getNombreCategoria);
            }
        });

        configurarMenuContextual(listCat, index -> {
            if (index >= 0 && index < categoriasSeleccionadas.size()) {
                categoriasSeleccionadas.remove((int)index);
                actualizarListaString(listCat, categoriasSeleccionadas, Categoria::getNombreCategoria);
            }
        });

        box.getChildren().addAll(new Label("Categorías Asignadas:"), new HBox(10, cmbCat, btnAdd), listCat);
        return box;
    }

    private VBox crearSubFormularioProveedores() {
        VBox box = new VBox(15);
        
        ComboBox<Proveedor> cmb = new ComboBox<>();
        cmb.setItems(FXCollections.observableArrayList(productoService.obtenerProveedores()));
        configurarCombo(cmb, Proveedor::getNombreProv);
        cmb.setPromptText("Seleccionar Proveedor");
        cmb.setMaxWidth(Double.MAX_VALUE);

        TextField txtCosto = UIFactory.crearInput("Costo Compra");
        ComboBox<String> cmbMoneda = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMoneda.setStyle(AppTheme.STYLE_INPUT);
        cmbMoneda.getSelectionModel().selectFirst();
        cmbMoneda.setPrefWidth(100);

        Button btnAdd = UIFactory.crearBotonSecundario("Añadir");
        btnAdd.setOnAction(e -> {
            if(cmb.getValue() != null && !txtCosto.getText().isEmpty()) {
                ProductoProveedor pp = new ProductoProveedor();
                pp.setProveedor(cmb.getValue());
                try { pp.setCosto(Double.parseDouble(txtCosto.getText())); } catch(Exception ex) { return; }
                pp.setMoneda(cmbMoneda.getValue());
                proveedoresAgregados.add(pp);
                txtCosto.clear();
            }
        });

        HBox controls = new HBox(10, UIFactory.crearGrupoInput("Costo", txtCosto), UIFactory.crearGrupoInput("Moneda", cmbMoneda), new VBox(19, new Label(""), btnAdd));
        controls.setAlignment(Pos.BOTTOM_LEFT);

        TableView<ProductoProveedor> table = crearTablaRelacion("Proveedor");
        table.setItems(proveedoresAgregados);
        
        TableColumn<ProductoProveedor, String> colNombre = new TableColumn<>("Proveedor");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProveedor().getNombreProv()));
        
        TableColumn<ProductoProveedor, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(data -> new SimpleStringProperty("$" + data.getValue().getCosto() + " " + data.getValue().getMoneda()));
        
        table.getColumns().addAll(colNombre, colPrecio);
        configurarMenuTabla(table, proveedoresAgregados);

        box.getChildren().addAll(UIFactory.crearGrupoInput("Proveedor", cmb), controls, table);
        return box;
    }

    private VBox crearSubFormularioClientes() {
        VBox box = new VBox(15);
        
        ComboBox<Cliente> cmb = new ComboBox<>();
        cmb.setItems(FXCollections.observableArrayList(productoService.obtenerClientes()));
        configurarCombo(cmb, Cliente::getNombreCliente);
        cmb.setMaxWidth(Double.MAX_VALUE);

        TextField txtCosto = UIFactory.crearInput("Precio Venta");
        ComboBox<String> cmbMoneda = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMoneda.setStyle(AppTheme.STYLE_INPUT);
        cmbMoneda.getSelectionModel().selectFirst();
        cmbMoneda.setPrefWidth(100);

        Button btnAdd = UIFactory.crearBotonSecundario("Añadir");
        btnAdd.setOnAction(e -> {
            if(cmb.getValue() != null && !txtCosto.getText().isEmpty()) {
                ProductoCliente pc = new ProductoCliente();
                pc.setCliente(cmb.getValue());
                try { pc.setCosto(Double.parseDouble(txtCosto.getText())); } catch(Exception ex) { return; }
                pc.setMoneda(cmbMoneda.getValue());
                clientesAgregados.add(pc);
                txtCosto.clear();
            }
        });

        HBox controls = new HBox(10, UIFactory.crearGrupoInput("Precio Venta", txtCosto), UIFactory.crearGrupoInput("Moneda", cmbMoneda), new VBox(19, new Label(""), btnAdd));
        controls.setAlignment(Pos.BOTTOM_LEFT);

        TableView<ProductoCliente> table = crearTablaRelacion("Cliente");
        table.setItems(clientesAgregados);
        
        TableColumn<ProductoCliente, String> colNombre = new TableColumn<>("Cliente");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCliente().getNombreCliente()));
        
        TableColumn<ProductoCliente, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(data -> new SimpleStringProperty("$" + data.getValue().getCosto() + " " + data.getValue().getMoneda()));
        
        table.getColumns().addAll(colNombre, colPrecio);
        configurarMenuTabla(table, clientesAgregados);

        box.getChildren().addAll(UIFactory.crearGrupoInput("Cliente", cmb), controls, table);
        return box;
    }

    private VBox crearSubFormularioFabricantes() {
        VBox box = new VBox(15);
        
        ComboBox<Fabricante> cmb = new ComboBox<>();
        cmb.setItems(FXCollections.observableArrayList(productoService.obtenerFabricantes()));
        configurarCombo(cmb, Fabricante::getNombreFabricante);
        cmb.setMaxWidth(Double.MAX_VALUE);

        TextField txtCosto = UIFactory.crearInput("Costo");
        ComboBox<String> cmbMoneda = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMoneda.setStyle(AppTheme.STYLE_INPUT);
        cmbMoneda.getSelectionModel().selectFirst();
        cmbMoneda.setPrefWidth(100);

        Button btnAdd = UIFactory.crearBotonSecundario("Añadir");
        btnAdd.setOnAction(e -> {
            if(cmb.getValue() != null && !txtCosto.getText().isEmpty()) {
                ProductoFabricante pf = new ProductoFabricante();
                pf.setFabricante(cmb.getValue());
                try { pf.setCosto(Double.parseDouble(txtCosto.getText())); } catch(Exception ex) { return; }
                pf.setMoneda(cmbMoneda.getValue());
                fabricantesAgregados.add(pf);
                txtCosto.clear();
            }
        });

        HBox controls = new HBox(10, UIFactory.crearGrupoInput("Costo", txtCosto), UIFactory.crearGrupoInput("Moneda", cmbMoneda), new VBox(19, new Label(""), btnAdd));
        controls.setAlignment(Pos.BOTTOM_LEFT);

        TableView<ProductoFabricante> table = crearTablaRelacion("Fabricante");
        table.setItems(fabricantesAgregados);
        
        TableColumn<ProductoFabricante, String> colNombre = new TableColumn<>("Fabricante");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFabricante().getNombreFabricante()));
        
        TableColumn<ProductoFabricante, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(data -> new SimpleStringProperty("$" + data.getValue().getCosto() + " " + data.getValue().getMoneda()));
        
        table.getColumns().addAll(colNombre, colPrecio);
        configurarMenuTabla(table, fabricantesAgregados);

        box.getChildren().addAll(UIFactory.crearGrupoInput("Fabricante", cmb), controls, table);
        return box;
    }

    private VBox crearSubFormularioEmpresas() {
        VBox box = new VBox(15);
        
        ComboBox<Empresa> cmb = new ComboBox<>();
        cmb.setItems(FXCollections.observableArrayList(productoService.obtenerEmpresas()));
        configurarCombo(cmb, Empresa::getNombreEmpresa);
        cmb.setMaxWidth(Double.MAX_VALUE);

        TextField txtCosto = UIFactory.crearInput("Costo Mercado");
        ComboBox<String> cmbMoneda = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMoneda.setStyle(AppTheme.STYLE_INPUT);
        cmbMoneda.getSelectionModel().selectFirst();
        cmbMoneda.setPrefWidth(100);

        Button btnAdd = UIFactory.crearBotonSecundario("Añadir");
        btnAdd.setOnAction(e -> {
            if(cmb.getValue() != null && !txtCosto.getText().isEmpty()) {
                ProductoEmpresa pe = new ProductoEmpresa();
                pe.setEmpresa(cmb.getValue());
                try { pe.setCosto(Double.parseDouble(txtCosto.getText())); } catch(Exception ex) { return; }
                pe.setMoneda(cmbMoneda.getValue());
                empresasAgregadas.add(pe);
                txtCosto.clear();
            }
        });

        HBox controls = new HBox(10, UIFactory.crearGrupoInput("Costo", txtCosto), UIFactory.crearGrupoInput("Moneda", cmbMoneda), new VBox(19, new Label(""), btnAdd));
        controls.setAlignment(Pos.BOTTOM_LEFT);

        TableView<ProductoEmpresa> table = crearTablaRelacion("Empresa");
        table.setItems(empresasAgregadas);
        
        TableColumn<ProductoEmpresa, String> colNombre = new TableColumn<>("Empresa");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmpresa().getNombreEmpresa()));
        
        TableColumn<ProductoEmpresa, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(data -> new SimpleStringProperty("$" + data.getValue().getCosto() + " " + data.getValue().getMoneda()));
        
        table.getColumns().addAll(colNombre, colPrecio);
        configurarMenuTabla(table, empresasAgregadas);

        box.getChildren().addAll(UIFactory.crearGrupoInput("Empresa", cmb), controls, table);
        return box;
    }

    private VBox crearSubFormularioServicios() {
        VBox box = new VBox(15);
        
        ComboBox<Servicio> cmb = new ComboBox<>();
        cmb.setItems(FXCollections.observableArrayList(productoService.obtenerServicios()));
        configurarCombo(cmb, Servicio::getDescripcionServicio);
        cmb.setMaxWidth(Double.MAX_VALUE);

        Button btnAdd = UIFactory.crearBotonSecundario("Añadir Servicio");
        btnAdd.setOnAction(e -> {
            if(cmb.getValue() != null && !serviciosSeleccionados.contains(cmb.getValue())) {
                serviciosSeleccionados.add(cmb.getValue());
            }
        });

        TableView<Servicio> table = crearTablaRelacion("Servicio");
        table.setItems(serviciosSeleccionados);
        
        TableColumn<Servicio, String> colNombre = new TableColumn<>("Servicio");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescripcionServicio()));
        
        TableColumn<Servicio, String> colPrecio = new TableColumn<>("Costo Base");
        colPrecio.setCellValueFactory(data -> new SimpleStringProperty("$" + data.getValue().getCostoServicio() + " " + data.getValue().getMonedaServicio()));
        
        table.getColumns().addAll(colNombre, colPrecio);
        configurarMenuTabla(table, serviciosSeleccionados);

        box.getChildren().addAll(UIFactory.crearGrupoInput("Servicio", cmb), btnAdd, table);
        return box;
    }

    // ==========================================
    // UTILERÍAS DE UI (Privadas)
    // ==========================================

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

    private <T> TableView<T> crearTablaRelacion(String nombreEntidad) {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(150);
        table.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-font-size: 12px;");
        table.setPlaceholder(new Label("Sin " + nombreEntidad.toLowerCase() + "s asignados"));
        return table;
    }

    private <T> void actualizarListaString(ListView<String> listView, ObservableList<T> source, Function<T, String> textExtractor) {
        listView.getItems().setAll(source.stream().map(textExtractor).collect(Collectors.toList()));
    }

    private <T> void configurarMenuTabla(TableView<T> table, ObservableList<T> list) {
        ContextMenu cm = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Quitar selección");
        deleteItem.setStyle("-fx-text-fill: red;");
        deleteItem.setOnAction(e -> {
            T selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) list.remove(selected);
        });
        cm.getItems().add(deleteItem);
        table.setContextMenu(cm);
    }

    private void configurarMenuContextual(Control control, java.util.function.Consumer<Integer> onDelete) {
        ContextMenu cm = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Eliminar");
        deleteItem.setStyle("-fx-text-fill: red;");
        if (control instanceof ListView) {
            deleteItem.setOnAction(e -> onDelete.accept(((ListView<?>) control).getSelectionModel().getSelectedIndex()));
        }
        cm.getItems().add(deleteItem);
        control.setContextMenu(cm);
    }

    // ==========================================
    // LÓGICA CRUD
    // ==========================================

    private void guardarProducto() {
        if (txtNombre.getText().trim().isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Campos Faltantes", "El nombre es obligatorio", stage);
            return;
        }

        Producto p = (productoEnEdicion != null) ? productoEnEdicion : new Producto();
        p.setNombreProducto(txtNombre.getText().trim());
        p.setFichaProducto(txtFicha.getText().trim());
        p.setAlternoProducto(txtAlterno.getText().trim());
        
        try {
            p.setExistenciaProducto(Integer.parseInt(txtExistencia.getText().trim()));
        } catch (NumberFormatException e) {
            p.setExistenciaProducto(0);
        }

        p.setCategorias(new ArrayList<>(categoriasSeleccionadas));

        p.getProductoProveedores().clear();
        for (ProductoProveedor pp : proveedoresAgregados) p.addProveedor(pp);

        p.getProductoClientes().clear();
        for (ProductoCliente pc : clientesAgregados) p.addCliente(pc);

        p.getProductoFabricantes().clear();
        for (ProductoFabricante pf : fabricantesAgregados) p.addFabricante(pf);

        p.getProductoEmpresas().clear();
        for (ProductoEmpresa pe : empresasAgregadas) p.addEmpresa(pe);

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

    private void prepararEdicion(Producto pResumen) {
        Producto pFull = productoService.obtenerProductoCompleto(pResumen.getIdProducto());
        
        this.productoEnEdicion = pFull;
        lblTituloFormulario.setText("Editar Producto (ID: " + pFull.getIdProducto() + ")");
        btnGuardar.setText("Actualizar Producto");
        btnGuardar.setStyle("-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");

        txtNombre.setText(pFull.getNombreProducto());
        txtFicha.setText(pFull.getFichaProducto());
        txtAlterno.setText(pFull.getAlternoProducto());
        txtExistencia.setText(String.valueOf(pFull.getExistenciaProducto()));

        categoriasSeleccionadas.setAll(pFull.getCategorias());
        proveedoresAgregados.setAll(pFull.getProductoProveedores());
        clientesAgregados.setAll(pFull.getProductoClientes());
        fabricantesAgregados.setAll(pFull.getProductoFabricantes());
        empresasAgregadas.setAll(pFull.getProductoEmpresas());
        serviciosSeleccionados.setAll(pFull.getServicios());
        
        // Volver a la pestaña general al editar
        navButtons.get("General").fire();
    }

    private void mostrarDetalleProducto(Producto p) {
        Producto pFull = productoService.obtenerProductoCompleto(p.getIdProducto());
        
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(15);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(600);
        root.setMaxWidth(600);
        root.setMaxHeight(600);

        // Header
        Label lblTitulo = new Label(pFull.getNombreProducto());
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(pFull.getFichaProducto() != null ? pFull.getFichaProducto() : "Sin ficha técnica");
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(0, 10, 0, 0));

        // Grid básica
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.add(UIFactory.crearDatoDetalle("Código Alterno:", pFull.getAlternoProducto()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle("Existencia:", String.valueOf(pFull.getExistenciaProducto())), 1, 0);
        
        // Categorías
        String catStr = pFull.getCategorias().stream().map(Categoria::getNombreCategoria).collect(Collectors.joining(", "));
        grid.add(UIFactory.crearDatoDetalle("Categorías:", catStr.isEmpty() ? "Ninguna" : catStr), 0, 1, 2, 1);

        content.getChildren().addAll(grid, new Separator());

        // Secciones dinámicas
        agregarSeccionDetalle(content, "Proveedores", pFull.getProductoProveedores().stream()
            .map(pp -> pp.getProveedor().getNombreProv() + " ($" + pp.getCosto() + " " + pp.getMoneda() + ")")
            .collect(Collectors.toList()));
            
        agregarSeccionDetalle(content, "Clientes", pFull.getProductoClientes().stream()
            .map(pc -> pc.getCliente().getNombreCliente() + " ($" + pc.getCosto() + " " + pc.getMoneda() + ")")
            .collect(Collectors.toList()));

        agregarSeccionDetalle(content, "Fabricantes", pFull.getProductoFabricantes().stream()
            .map(pf -> pf.getFabricante().getNombreFabricante() + " ($" + pf.getCosto() + " " + pf.getMoneda() + ")")
            .collect(Collectors.toList()));

        scroll.setContent(content);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Footer
        Button btnCerrar = UIFactory.crearBotonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());
        HBox boxBtn = new HBox(btnCerrar);
        boxBtn.setAlignment(Pos.CENTER_RIGHT);
        boxBtn.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), scroll, new Separator(), boxBtn);
        dialogService.mostrarDialogoModal(dialog, root, stage);
    }

    private void agregarSeccionDetalle(VBox parent, String titulo, java.util.List<String> items) {
        if (items.isEmpty()) return;
        
        Label lblHeader = new Label(titulo);
        lblHeader.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827; -fx-padding: 5 0 2 0;");
        parent.getChildren().add(lblHeader);
        
        for (String item : items) {
            Label lblItem = new Label("• " + item);
            lblItem.setStyle("-fx-text-fill: #4B5563; -fx-padding: 0 0 0 10;");
            parent.getChildren().add(lblItem);
        }
        parent.getChildren().add(new Region()); // Spacer tiny
    }

    private void eliminarProducto(Producto p) {
        if (dialogService.mostrarConfirmacion("Eliminar Producto", "¿Deseas eliminar '" + p.getNombreProducto() + "'?", stage)) {
            if (productoService.eliminarProducto(p)) {
                cargarProductos();
                if (productoEnEdicion != null && productoEnEdicion.getIdProducto().equals(p.getIdProducto())) {
                    limpiarFormulario();
                }
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el producto.", stage);
            }
        }
    }

    private void limpiarFormulario() {
        productoEnEdicion = null;
        lblTituloFormulario.setText("Nuevo Producto");
        btnGuardar.setText("Guardar Producto");
        btnGuardar.setStyle("-fx-background-color: " + AppTheme.COLOR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");

        txtNombre.clear();
        txtFicha.clear();
        txtAlterno.clear();
        txtExistencia.setText("0");

        categoriasSeleccionadas.clear();
        proveedoresAgregados.clear();
        clientesAgregados.clear();
        fabricantesAgregados.clear();
        empresasAgregadas.clear();
        serviciosSeleccionados.clear();
        
        navButtons.get("General").fire();
    }

    private void cargarProductos() {
        tablaProductos.getItems().setAll(productoService.consultarProductos());
    }
}