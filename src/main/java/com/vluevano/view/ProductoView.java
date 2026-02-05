package com.vluevano.view;

import com.vluevano.model.*;
import com.vluevano.service.DialogService;
import com.vluevano.service.ProductoService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import com.vluevano.util.RelacionViewBuilder;
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
import java.util.List;
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
    private TextField txtNombre, txtFicha, txtAlterno, txtExistencia;
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
     * Crea el contenido principal de la vista
     * @return
     */
    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader("Gestión de Productos", 
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
     * Crea el panel de la tabla de productos
     * @return
     */
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

        tablaProductos.getColumns().addAll(List.of(colNombre, colExistencia, colAcciones));
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
     * Crea el panel del formulario de producto
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        lblTituloFormulario = new Label("Nuevo Producto");
        lblTituloFormulario.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTituloFormulario.setPadding(new Insets(20));

        inicializarInputsGenerales();

        HBox navBar = new HBox(5);
        navBar.setPadding(new Insets(0, 20, 10, 20));
        navBar.setStyle("-fx-border-color: transparent transparent #E5E7EB transparent; -fx-border-width: 0 0 1 0;");
        
        contentPanel = new BorderPane();
        contentPanel.setPadding(new Insets(20));

        crearBotonNav("General", navBar, crearVistaGeneral());

        crearBotonNav("Proveedores", navBar, RelacionViewBuilder.crearPanelGestion(
            productoService.obtenerProveedores(), proveedoresAgregados, Proveedor::getNombreProv,
            "Proveedor", "Costo Compra",
            (item, costo, moneda) -> {
                ProductoProveedor pp = new ProductoProveedor();
                pp.setProveedor(item); pp.setCosto(costo); pp.setMoneda(moneda); return pp;
            },
            pp -> pp.getProveedor().getNombreProv(), pp -> "$" + pp.getCosto() + " " + pp.getMoneda()
        ));

        crearBotonNav("Clientes", navBar, RelacionViewBuilder.crearPanelGestion(
            productoService.obtenerClientes(), clientesAgregados, Cliente::getNombreCliente,
            "Cliente", "Precio Venta",
            (item, costo, moneda) -> {
                ProductoCliente pc = new ProductoCliente();
                pc.setCliente(item); pc.setCosto(costo); pc.setMoneda(moneda); return pc;
            },
            pc -> pc.getCliente().getNombreCliente(), pc -> "$" + pc.getCosto() + " " + pc.getMoneda()
        ));

        crearBotonNav("Fabricantes", navBar, RelacionViewBuilder.crearPanelGestion(
            productoService.obtenerFabricantes(), fabricantesAgregados, Fabricante::getNombreFabricante,
            "Fabricante", "Costo",
            (item, costo, moneda) -> {
                ProductoFabricante pf = new ProductoFabricante();
                pf.setFabricante(item); pf.setCosto(costo); pf.setMoneda(moneda); return pf;
            },
            pf -> pf.getFabricante().getNombreFabricante(), pf -> "$" + pf.getCosto() + " " + pf.getMoneda()
        ));

        crearBotonNav("Empresas", navBar, RelacionViewBuilder.crearPanelGestion(
            productoService.obtenerEmpresas(), empresasAgregadas, Empresa::getNombreEmpresa,
            "Empresa", "Costo Mercado",
            (item, costo, moneda) -> {
                ProductoEmpresa pe = new ProductoEmpresa();
                pe.setEmpresa(item); pe.setCosto(costo); pe.setMoneda(moneda); return pe;
            },
            pe -> pe.getEmpresa().getNombreEmpresa(), pe -> "$" + pe.getCosto() + " " + pe.getMoneda()
        ));

        crearBotonNav("Servicios", navBar, crearSubFormularioServicios());

        contentPanel.setCenter(crearVistaGeneral());
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
     * Inicializa los inputs generales del formulario
     */
    private void inicializarInputsGenerales() {
        txtNombre = UIFactory.crearInput("Nombre del Producto *");
        txtFicha = UIFactory.crearInput("Descripción / Ficha");
        txtAlterno = UIFactory.crearInput("Código Alterno");
        txtExistencia = UIFactory.crearInput("0");
    }

    /**
     * Crea un botón de navegación para el formulario
     * @param titulo
     * @param container
     * @param view
     */
    private void crearBotonNav(String titulo, HBox container, Node view) {
        Button btn = new Button(titulo);
        btn.setPrefHeight(30);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;");
        
        btn.setOnAction(e -> {
            contentPanel.setCenter(view);
            actualizarEstiloBotones(titulo);
        });
        
        container.getChildren().add(btn);
        navButtons.put(titulo, btn);
    }

    /**
     * Actualiza el estilo de los botones de navegación
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
     * Crea la vista general del formulario
     * @return
     */
    private VBox crearVistaGeneral() {
        return new VBox(15,
            UIFactory.crearGrupoInput("Nombre *", txtNombre),
            UIFactory.crearGrupoInput("Ficha Técnica", txtFicha),
            new HBox(15, 
                UIFactory.crearGrupoInput("Alterno", txtAlterno),
                UIFactory.crearGrupoInput("Existencia", txtExistencia)
            ),
            crearSelectorCategorias()
        );
    }

    /**
     * Crea el selector de categorías
     * @return
     */
    private VBox crearSelectorCategorias() {
        VBox box = new VBox(10);
        List<Categoria> listaDb = productoService.obtenerCategorias();
        
        Label lblTitulo = new Label("Categoría");
        lblTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");

        ComboBox<Categoria> cmbCat = new ComboBox<>();
        cmbCat.setItems(FXCollections.observableArrayList(listaDb));
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
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #DC2626; -fx-border-radius: 3; -fx-padding: 2 6 2 6;");
                btnEliminar.setOnAction(e -> {
                    if (getItem() != null) categoriasSeleccionadas.remove(getItem());
                });
            }

            @Override protected void updateItem(Categoria item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblTexto.setText(item.getNombreCategoria());
                    setGraphic(container);
                }
            }
        });

        Button btnAdd = new Button("Añadir / Crear");
        btnAdd.setMinWidth(120);
        btnAdd.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #A7F3D0; -fx-background-radius: 6;");
        
        btnAdd.setOnAction(e -> {
            String textoNuevo = txtNueva.getText().trim();
            Categoria seleccionCombo = cmbCat.getValue();

            if (!textoNuevo.isEmpty()) {
                Categoria existente = listaDb.stream()
                    .filter(c -> c.getNombreCategoria().equalsIgnoreCase(textoNuevo))
                    .findFirst().orElse(null);

                if (existente != null) {
                    if (!categoriasSeleccionadas.contains(existente)) categoriasSeleccionadas.add(existente);
                } else {
                    Categoria nuevaCat = new Categoria();
                    nuevaCat.setNombreCategoria(textoNuevo);
                    nuevaCat.setDescripcionCategoria("Creada desde Producto");
                    boolean yaEnLista = categoriasSeleccionadas.stream().anyMatch(c -> c.getNombreCategoria().equalsIgnoreCase(textoNuevo));
                    if (!yaEnLista) categoriasSeleccionadas.add(nuevaCat);
                }
                txtNueva.clear();
                cmbCat.getSelectionModel().clearSelection();

            } else if (seleccionCombo != null) {
                if (!categoriasSeleccionadas.contains(seleccionCombo)) categoriasSeleccionadas.add(seleccionCombo);
                cmbCat.getSelectionModel().clearSelection();
            }
        });

        HBox rowInputBtn = new HBox(10, txtNueva, btnAdd);
        rowInputBtn.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtNueva, Priority.ALWAYS);

        if (listaDb.isEmpty()) {
            cmbCat.setDisable(true);
            cmbCat.setPromptText("No hay categorías registradas");
        }
        
        box.getChildren().addAll(lblTitulo, cmbCat, rowInputBtn, listCat);
        return box;
    }

    /**
     * Crea el subformulario de servicios
     * @return
     */
    private VBox crearSubFormularioServicios() {
        VBox box = new VBox(15);
        List<Servicio> lista = productoService.obtenerServicios();

        if (lista.isEmpty()) {
            Label lblVacio = new Label("No hay servicios registrados.");
            lblVacio.setStyle("-fx-text-fill: #EF4444; -fx-font-style: italic; -fx-font-size: 13px;");
            box.getChildren().add(lblVacio);
        } else {
            ComboBox<Servicio> cmb = new ComboBox<>();
            cmb.setItems(FXCollections.observableArrayList(lista));
            configurarCombo(cmb, Servicio::getDescripcionServicio);
            cmb.setMaxWidth(Double.MAX_VALUE);
            cmb.setPromptText("Seleccionar Servicio");

            Button btnAdd = new Button("Añadir Servicio");
            btnAdd.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-weight: bold; -fx-background-radius: 6;");
            btnAdd.setOnAction(e -> {
                if(cmb.getValue() != null && !serviciosSeleccionados.contains(cmb.getValue())) {
                    serviciosSeleccionados.add(cmb.getValue());
                }
            });
            
            box.getChildren().addAll(UIFactory.crearGrupoInput("Servicio", cmb), btnAdd);
        }

        TableView<Servicio> table = UIFactory.crearTablaRelacion(
            serviciosSeleccionados,
            "Servicio", Servicio::getDescripcionServicio,
            "Costo Base", s -> "$" + s.getCostoServicio() + " " + s.getMonedaServicio(),
            true
        );

        box.getChildren().add(table);
        return box;
    }

    /**
     * Guarda el producto actual (nuevo o editado)
     */
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
     * Prepara el formulario para editar un producto existente
     * @param pResumen
     */
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
        
        navButtons.get("General").fire();
    }

    /**
     * Muestra el detalle completo de un producto en un diálogo modal
     * @param p
     */
    private void mostrarDetalleProducto(Producto p) {
        Producto pFull = productoService.obtenerProductoCompleto(p.getIdProducto());

        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);");
        root.setMinWidth(850);
        root.setMaxWidth(850);
        root.setMaxHeight(700);

        VBox topSection = new VBox(15);
        topSection.setPadding(new Insets(25, 25, 0, 25));
        
        Label lblTitulo = new Label(pFull.getNombreProducto());
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
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

        Node viewProv = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getProductoProveedores()), "Proveedor", d -> d.getProveedor().getNombreProv(), "Costo Compra", d -> "$" + d.getCosto() + " " + d.getMoneda(), false);
        Node viewCli = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getProductoClientes()), "Cliente", d -> d.getCliente().getNombreCliente(), "Precio Venta", d -> "$" + d.getCosto() + " " + d.getMoneda(), false);
        Node viewFab = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getProductoFabricantes()), "Fabricante", d -> d.getFabricante().getNombreFabricante(), "Costo", d -> "$" + d.getCosto() + " " + d.getMoneda(), false);
        Node viewEmp = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getProductoEmpresas()), "Empresa", d -> d.getEmpresa().getNombreEmpresa(), "Costo Mercado", d -> "$" + d.getCosto() + " " + d.getMoneda(), false);
        Node viewServ = UIFactory.crearTablaRelacion(FXCollections.observableArrayList(pFull.getServicios()), "Servicio", Servicio::getDescripcionServicio, "Costo Base", d -> "$" + d.getCostoServicio() + " " + d.getMonedaServicio(), false);

        crearBotonPopup("Proveedores (" + pFull.getProductoProveedores().size() + ")", viewProv, navBar, dynamicContent, localNavButtons);
        crearBotonPopup("Clientes (" + pFull.getProductoClientes().size() + ")", viewCli, navBar, dynamicContent, localNavButtons);
        crearBotonPopup("Fabricantes (" + pFull.getProductoFabricantes().size() + ")", viewFab, navBar, dynamicContent, localNavButtons);
        crearBotonPopup("Empresas (" + pFull.getProductoEmpresas().size() + ")", viewEmp, navBar, dynamicContent, localNavButtons);
        crearBotonPopup("Servicios (" + pFull.getServicios().size() + ")", viewServ, navBar, dynamicContent, localNavButtons);

        centerContainer.getChildren().addAll(navBar, dynamicContent);
        root.setCenter(centerContainer);

        localNavButtons.values().stream().findFirst().ifPresent(Button::fire);

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
     * Crea el panel de detalles fijos del diálogo de detalle de producto
     * @param p
     * @return
     */
    private GridPane crearPanelDetallesFijos(Producto p) {
        GridPane grid = new GridPane();
        grid.setHgap(30); grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 10, 0));

        VBox vAlterno = UIFactory.crearDatoDetalle("Código Alterno", (p.getAlternoProducto() != null && !p.getAlternoProducto().isEmpty()) ? p.getAlternoProducto() : "---");
        
        Label lblExistenciaVal = new Label(String.valueOf(p.getExistenciaProducto()));
        lblExistenciaVal.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + (p.getExistenciaProducto() > 0 ? "#059669" : "#DC2626") + ";"); 
        VBox vExistencia = new VBox(2, new Label("Existencia Actual"), lblExistenciaVal);
        vExistencia.getChildren().get(0).setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        String catStr = p.getCategorias().stream().map(Categoria::getNombreCategoria).collect(Collectors.joining(", "));
        VBox vCategorias = UIFactory.crearDatoDetalle("Categorías", catStr.isEmpty() ? "Sin categoría" : catStr);

        grid.add(vAlterno, 0, 0);
        grid.add(vExistencia, 1, 0);
        grid.add(vCategorias, 2, 0);
        return grid;
    }

    /**
     * Crea un botón para el popup de detalle de producto
     * @param titulo
     * @param view
     * @param container
     * @param contentArea
     * @param mapButtons
     */
    private void crearBotonPopup(String titulo, Node view, HBox container, BorderPane contentArea, Map<String, Button> mapButtons) {
        Button btn = new Button(titulo);
        btn.setPrefHeight(30);
        String styleInactive = "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;";
        btn.setStyle(styleInactive);

        btn.setOnAction(e -> {
            contentArea.setCenter(view);
            mapButtons.forEach((k, b) -> {
                if (b == btn) {
                    b.setStyle("-fx-background-color: #FFEDD5; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + "; -fx-font-weight: bold; -fx-cursor: default; -fx-background-radius: 6;");
                    b.setOnMouseEntered(null);
                    b.setOnMouseExited(null);
                } else {
                    b.setStyle(styleInactive);
                    b.setOnMouseEntered(ev -> b.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-background-radius: 6;"));
                    b.setOnMouseExited(ev -> b.setStyle(styleInactive));
                }
            });
        });
        container.getChildren().add(btn);
        mapButtons.put(titulo, btn);
    }

    /**
     * Elimina un producto después de la confirmación del usuario
     * @param p
     */
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

    /**
     * Limpia el formulario y lo prepara para un nuevo producto
     */
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

    /**
     * Carga los productos en la tabla
     */
    private void cargarProductos() {
        tablaProductos.getItems().setAll(productoService.consultarProductos());
    }

    /**
     * Configura un ComboBox para mostrar objetos personalizados
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
}