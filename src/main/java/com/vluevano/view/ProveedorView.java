package com.vluevano.view;

import com.vluevano.model.Proveedor;
import com.vluevano.service.ProveedorService;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ProveedorView {

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;

    private TableView<Proveedor> tablaProveedores;
    private TextField txtFiltro;

    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo;
    private TextField txtCp, txtNoExt, txtNoInt, txtCalle, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private TextField txtCurp;
    private ComboBox<String> cmbTipoPersona;
    private Label lblMensaje;

    private static final String COLOR_PRIMARY = "#F97316";
    private static final String COLOR_PRIMARY_HOVER = "#EA580C";
    private static final String COLOR_BG_LIGHT = "#F3F4F6";
    private static final String STYLE_INPUT = "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14px;";
    private static final String STYLE_LABEL_INPUT = "-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;";
    private static final String STYLE_CARD = "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);";
    private static final String STYLE_DIALOG_BG = "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);";

    /**
     * Muestra la pantalla de gestión de proveedores
     * 
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
            Scene scene = new Scene(root, 1280, 800);
            stage.setScene(scene);
            stage.centerOnScreen();
        }
        stage.setResizable(true);
        stage.setTitle("PriceStocker | Gestión de Proveedores");
        stage.show();

        cargarProveedores();
    }

    /**
     * Crea el contenido principal de la vista
     * 
     * @return
     */
    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + COLOR_BG_LIGHT + ";");

        // HEADER
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 40, 20, 40));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: #111827; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label lblTitulo = new Label("Gestión de Proveedores");
        lblTitulo.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVolver = new Button("Volver al Menú");
        crearBotonSecundario(btnVolver);
        btnVolver.setOnAction(e -> menuPrincipalScreen.show(stage, this.usuarioActual));

        header.getChildren().addAll(lblTitulo, spacer, btnVolver);
        root.setTop(header);

        // CONTENIDO CENTRAL
        HBox contenidoCentral = new HBox(30);
        contenidoCentral.setPadding(new Insets(30));

        VBox panelTabla = crearPanelTabla();
        HBox.setHgrow(panelTabla, Priority.ALWAYS);

        VBox panelFormulario = crearPanelFormulario();
        panelFormulario.setMinWidth(420);
        panelFormulario.setMaxWidth(420);

        contenidoCentral.getChildren().addAll(panelTabla, panelFormulario);
        root.setCenter(contenidoCentral);

        return root;
    }

    /**
     * Crea el panel con la tabla de proveedores y barra de búsqueda
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        // Barra de búsqueda
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label lblBuscar = new Label("Buscar:");
        lblBuscar.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        txtFiltro = new TextField();
        txtFiltro.setPromptText("Nombre, RFC, Municipio...");
        txtFiltro.setStyle(STYLE_INPUT);
        txtFiltro.setPrefWidth(300);
        txtFiltro.textProperty().addListener((obs, oldVal, newVal) -> {
            tablaProveedores.getItems().setAll(proveedorService.buscarProveedores(newVal));
        });

        topBar.getChildren().addAll(lblBuscar, txtFiltro);

        // Tabla
        tablaProveedores = new TableView<>();
        tablaProveedores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaProveedores.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        // ID
        TableColumn<Proveedor, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdProveedor())));
        colId.setResizable(false);

        // Proveedor
        TableColumn<Proveedor, String> colNombre = new TableColumn<>("Proveedor");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreProv()));
        colNombre.setResizable(false);

        // Teléfono
        TableColumn<Proveedor, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoProv()));
        colTel.setResizable(false);

        // Correo
        TableColumn<Proveedor, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorreoProv()));
        colCorreo.setResizable(false);

        // Dirección
        TableColumn<Proveedor, String> colDireccion = new TableColumn<>("Dirección Completa");
        colDireccion.setCellValueFactory(data -> {
            Proveedor p = data.getValue();
            String dir = String.format("%s #%d, %s, CP %d, %s, %s, %s",
                    p.getCalle(), p.getNoExtProv(), p.getColonia(), p.getCpProveedor(),
                    p.getCiudad(), p.getEstado(), p.getPais());
            return new SimpleStringProperty(dir);
        });
        colDireccion.setResizable(false);

        // Acciones
        TableColumn<Proveedor, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setResizable(false);

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");
            {
                btnEliminar.setStyle(
                        "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEliminar.setOnAction(e -> eliminarProveedor(getTableView().getItems().get(getIndex())));
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });

        // DISTRIBUCIÓN DE LAS COLUMNAS
        colId.prefWidthProperty().bind(tablaProveedores.widthProperty().multiply(0.05));
        colNombre.prefWidthProperty().bind(tablaProveedores.widthProperty().multiply(0.20));
        colTel.prefWidthProperty().bind(tablaProveedores.widthProperty().multiply(0.10));
        colCorreo.prefWidthProperty().bind(tablaProveedores.widthProperty().multiply(0.15));
        colDireccion.prefWidthProperty().bind(tablaProveedores.widthProperty().multiply(0.40));
        colAcciones.prefWidthProperty().bind(tablaProveedores.widthProperty().multiply(0.10));

        tablaProveedores.getColumns().addAll(colId, colNombre, colTel, colCorreo, colDireccion, colAcciones);
        VBox.setVgrow(tablaProveedores, Priority.ALWAYS);
        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null)
                mostrarDetalleProveedor(newSelection);
        });
        box.getChildren().addAll(topBar, tablaProveedores);
        return box;
    }

    /**
     * Crea el panel con el formulario
     * 
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(STYLE_CARD);

        Label lblTitle = new Label("Nuevo Proveedor");
        lblTitle.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTitle.setPadding(new Insets(20));

        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        // Campos de entrada
        txtNombre = crearCampoTexto("Ej. Juan Pérez / Distribuidora del Norte");
        txtTelefono = crearCampoTexto("Ej. 55 1234 5678");
        txtCorreo = crearCampoTexto("contacto@empresa.com");

        txtCalle = crearCampoTexto("Ej. Av. Reforma");
        txtNoExt = crearCampoTexto("Ej. 123");
        txtNoInt = crearCampoTexto("Ej. Piso 2 (Opcional)");
        txtCp = crearCampoTexto("Ej. 06600");
        txtColonia = crearCampoTexto("Ej. Juárez");
        txtCiudad = crearCampoTexto("Ej. Ciudad de México");
        txtMunicipio = crearCampoTexto("Ej. Cuauhtémoc");
        txtEstado = crearCampoTexto("Ej. CDMX");
        txtPais = crearCampoTexto("Ej. México");

        txtRfc = crearCampoTexto("Ej. XAXX010101000 (Opcional)");
        txtCurp = crearCampoTexto("Ej. AAAA999999HDFXXX00 (Opcional)");

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll("Persona Moral", "Persona Física");
        cmbTipoPersona.getSelectionModel().select(0);
        cmbTipoPersona.setMaxWidth(Double.MAX_VALUE);
        cmbTipoPersona.setStyle(STYLE_INPUT);

        // Estructura
        inputsContainer.getChildren().addAll(
                crearSeccion("Datos Personales"), // Color Naranja
                crearInputConLabel("Nombre / Razón Social *", txtNombre),
                crearInputConLabel("Teléfono *", txtTelefono),
                crearInputConLabel("Correo Electrónico *", txtCorreo),

                crearSeccion("Dirección"), // Color Naranja
                crearInputConLabel("Calle *", txtCalle),
                new HBox(10,
                        crearInputConLabel("No. Ext *", txtNoExt),
                        crearInputConLabel("No. Int", txtNoInt),
                        crearInputConLabel("C.P. *", txtCp)),
                crearInputConLabel("Colonia *", txtColonia),
                crearInputConLabel("Ciudad *", txtCiudad),
                crearInputConLabel("Municipio *", txtMunicipio),
                new HBox(10,
                        crearInputConLabel("Estado *", txtEstado),
                        crearInputConLabel("País *", txtPais)),

                crearSeccion("Datos Fiscales"), // Color Naranja
                crearInputConLabel("Tipo de Persona", cmbTipoPersona),
                crearInputConLabel("RFC", txtRfc),
                crearInputConLabel("CURP", txtCurp));

        ScrollPane scrollPane = new ScrollPane(inputsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));

        Button btnGuardar = new Button("Registrar Proveedor");
        crearBotonPrimario(btnGuardar);
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarProveedor());

        Button btnLimpiar = new Button("Limpiar Formulario");
        crearBotonTexto(btnLimpiar);
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        footer.getChildren().addAll(btnGuardar, btnLimpiar, lblMensaje);
        card.getChildren().addAll(lblTitle, scrollPane, footer);
        return card;
    }

    /**
     * Crea un VBox con un Label y un campo de entrada
     * 
     * @param textoLabel
     * @param campo
     * @return
     */
    private VBox crearInputConLabel(String textoLabel, Node campo) {
        Label l = new Label(textoLabel);
        l.setStyle(STYLE_LABEL_INPUT);
        if (textoLabel.contains("*"))
            l.setTextFill(Color.web(COLOR_PRIMARY));
        VBox v = new VBox(5, l, campo);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    /**
     * Crea una sección con un título destacado
     * 
     * @param texto
     * @return
     */
    private Label crearSeccion(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + COLOR_PRIMARY
                + "; -fx-font-size: 14px; -fx-padding: 15 0 5 0;");
        return l;
    }

    /**
     * Carga los proveedores en la tabla
     */
    private void cargarProveedores() {
        tablaProveedores.getItems().setAll(proveedorService.consultarProveedores());
    }

    /**
     * Registra un nuevo proveedor
     */
    private void registrarProveedor() {
        if (esVacio(txtNombre) || esVacio(txtTelefono) || esVacio(txtCorreo) ||
                esVacio(txtCalle) || esVacio(txtNoExt) || esVacio(txtCp) ||
                esVacio(txtColonia) || esVacio(txtCiudad) || esVacio(txtEstado) || esVacio(txtPais)) {
            mostrarAlertaPersonalizada(Alert.AlertType.WARNING, "Campos Faltantes",
                    "Por favor llene todos los campos marcados con *");
            return;
        }

        Proveedor prov = new Proveedor();
        prov.setNombreProv(txtNombre.getText().trim());
        prov.setTelefonoProv(txtTelefono.getText().trim());
        prov.setCorreoProv(txtCorreo.getText().trim());

        // Opcionales
        prov.setRfcProveedor(txtRfc.getText().isEmpty() ? null : txtRfc.getText().trim());
        prov.setCurp(txtCurp.getText().isEmpty() ? null : txtCurp.getText().trim());

        try {
            prov.setCpProveedor(Integer.parseInt(txtCp.getText().trim()));
            prov.setNoExtProv(Integer.parseInt(txtNoExt.getText().trim()));
            prov.setNoIntProv(txtNoInt.getText().isEmpty() ? 0 : Integer.parseInt(txtNoInt.getText().trim()));
        } catch (NumberFormatException e) {
            mostrarAlertaPersonalizada(Alert.AlertType.ERROR, "Error Numérico",
                    "CP, No. Ext y No. Int deben ser números válidos.");
            return;
        }

        prov.setCalle(txtCalle.getText().trim());
        prov.setColonia(txtColonia.getText().trim());
        prov.setCiudad(txtCiudad.getText().trim());
        prov.setMunicipio(txtMunicipio.getText().trim());
        prov.setEstado(txtEstado.getText().trim());
        prov.setPais(txtPais.getText().trim());

        String tipoSeleccionado = cmbTipoPersona.getValue();
        prov.setEsPersonaFisica(tipoSeleccionado != null && tipoSeleccionado.equals("Persona Física"));

        String resultado = proveedorService.guardarProveedor(prov);

        if (resultado.toLowerCase().contains("exitosamente") || resultado.toLowerCase().contains("guardado")) {
            mostrarAlertaPersonalizada(Alert.AlertType.INFORMATION, "Éxito", resultado);
            limpiarFormulario();
            cargarProveedores();
        } else {
            mostrarAlertaPersonalizada(Alert.AlertType.ERROR, "Error", resultado);
        }
    }

    /**
     * Verifica si un TextField está vacío
     * 
     * @param tf
     * @return
     */
    private boolean esVacio(TextField tf) {
        return tf.getText() == null || tf.getText().trim().isEmpty();
    }

    /**
     * Elimina un proveedor
     * 
     * @param prov
     */
    private void eliminarProveedor(Proveedor prov) {
        boolean confirmado = mostrarAlertaConfirmacion("Eliminar Proveedor",
                "¿Seguro que deseas eliminar a: " + prov.getNombreProv() + "?");

        if (confirmado) {
            if (proveedorService.eliminarProveedor(prov)) {
                cargarProveedores();
                mostrarAlertaPersonalizada(Alert.AlertType.INFORMATION, "Eliminado",
                        "Proveedor eliminado correctamente.");
            } else {
                mostrarAlertaPersonalizada(Alert.AlertType.ERROR, "Error", "No se pudo eliminar.");
            }
        }
    }

    /**
     * Limpia el formulario de entrada
     */
    private void limpiarFormulario() {
        txtNombre.clear();
        txtRfc.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        txtCalle.clear();
        txtNoExt.clear();
        txtNoInt.clear();
        txtCp.clear();
        txtColonia.clear();
        txtCiudad.clear();
        txtMunicipio.clear();
        txtEstado.clear();
        txtPais.clear();
        txtCurp.clear();
        cmbTipoPersona.getSelectionModel().select(0);
        lblMensaje.setText("");
    }

    /**
     * Muestra un diálogo con el detalle completo del proveedor
     * 
     * @param prov
     */
    private void mostrarDetalleProveedor(Proveedor prov) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(500);

        Label lblTitulo = new Label(prov.getNombreProv());
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_PRIMARY + ";");
        Label lblSub = new Label(prov.isEsPersonaFisica() ? "Persona Física" : "Persona Moral");
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        agregarDatoGrid(grid, "Teléfono:", prov.getTelefonoProv(), 0, 0);
        agregarDatoGrid(grid, "Correo:", prov.getCorreoProv(), 1, 0);

        String direccion = String.format("%s #%d%s", prov.getCalle(), prov.getNoExtProv(),
                (prov.getNoIntProv() > 0 ? " Int " + prov.getNoIntProv() : ""));
        agregarDatoGrid(grid, "Dirección:", direccion, 0, 1);
        agregarDatoGrid(grid, "Colonia/CP:", prov.getColonia() + " C.P. " + prov.getCpProveedor(), 1, 1);

        agregarDatoGrid(grid, "Ciudad/Mun:", prov.getCiudad() + ", " + prov.getMunicipio(), 0, 2);
        agregarDatoGrid(grid, "Estado/País:", prov.getEstado() + ", " + prov.getPais(), 1, 2);

        if (prov.getCurp() != null)
            agregarDatoGrid(grid, "CURP:", prov.getCurp(), 0, 3);

        if (prov.getRfcProveedor() != null) {
            agregarDatoGrid(grid, "RFC:", prov.getRfcProveedor(), 1, 3);
            
        }

        Button btnCerrar = new Button("Cerrar");
        crearBotonSecundario(btnCerrar);
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * Agrega un par de Label al GridPane para mostrar un dato
     * 
     * @param grid
     * @param label
     * @param valor
     * @param col
     * @param row
     */
    private void agregarDatoGrid(GridPane grid, String label, String valor, int col, int row) {
        Label l = new Label(label);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        Label v = new Label(valor != null ? valor : "-");
        v.setStyle("-fx-text-fill: #4B5563; -fx-wrap-text: true;");
        v.setMaxWidth(200);
        VBox box = new VBox(2, l, v);
        grid.add(box, col, row);
    }

    /**
     * Crea un TextField estilizado
     * 
     * @param prompt
     * @return
     */
    private TextField crearCampoTexto(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(STYLE_INPUT);
        return tf;
    }

    /**
     * Botón primario
     * 
     * @param btn
     */
    private void crearBotonPrimario(Button btn) {
        btn.setPrefHeight(40);
        btn.setStyle("-fx-background-color: " + COLOR_PRIMARY
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + COLOR_PRIMARY_HOVER
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + COLOR_PRIMARY
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;"));
    }

    /**
     * Botón secundario
     * 
     * @param btn
     */
    private void crearBotonSecundario(Button btn) {
        btn.setPrefHeight(35);
        btn.setStyle(
                "-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #F9FAFB; -fx-border-color: #9CA3AF; -fx-text-fill: #111827; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"));
    }

    /**
     * Botón de texto
     * 
     * @param btn
     */
    private void crearBotonTexto(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_PRIMARY
                + "; -fx-cursor: hand; -fx-underline: true;"));
        btn.setOnMouseExited(
                e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-cursor: hand;"));
    }

    /**
     * Muestra una alerta personalizada
     * 
     * @param tipo
     * @param titulo
     * @param mensaje
     */
    private void mostrarAlertaPersonalizada(Alert.AlertType tipo, String titulo, String mensaje) {
        Stage alertStage = new Stage();
        alertStage.initOwner(stage);
        alertStage.initModality(Modality.WINDOW_MODAL);
        alertStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle(STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(350);
        root.setMaxWidth(350);

        String colorTitulo = tipo == Alert.AlertType.ERROR ? "#DC2626"
                : (tipo == Alert.AlertType.WARNING ? "#D97706" : COLOR_PRIMARY);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + colorTitulo + ";");

        Label lblMsg = new Label(mensaje);
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");

        HBox botones = new HBox();
        botones.setAlignment(Pos.CENTER_RIGHT);

        Button btnOk = new Button("Entendido");
        if (tipo == Alert.AlertType.ERROR) {
            btnOk.setStyle(
                    "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        } else {
            crearBotonPrimario(btnOk);
        }
        btnOk.setOnAction(e -> alertStage.close());

        botones.getChildren().add(btnOk);
        root.getChildren().addAll(lblTitulo, lblMsg, botones);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        alertStage.setScene(scene);
        alertStage.showAndWait();
    }

    /**
     * Muestra una alerta de confirmación
     * 
     * @param titulo
     * @param mensaje
     * @return
     */
    private boolean mostrarAlertaConfirmacion(String titulo, String mensaje) {
        Stage alertStage = new Stage();
        alertStage.initOwner(stage);
        alertStage.initModality(Modality.WINDOW_MODAL);
        alertStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle(STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(380);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label lblMsg = new Label(mensaje);
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");

        HBox botones = new HBox(15);
        botones.setAlignment(Pos.CENTER_RIGHT);

        Button btnNo = new Button("Cancelar");
        crearBotonSecundario(btnNo);

        Button btnSi = new Button("Confirmar");
        btnSi.setStyle(
                "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");

        final boolean[] respuesta = { false };

        btnNo.setOnAction(e -> {
            respuesta[0] = false;
            alertStage.close();
        });

        btnSi.setOnAction(e -> {
            respuesta[0] = true;
            alertStage.close();
        });

        botones.getChildren().addAll(btnNo, btnSi);
        root.getChildren().addAll(lblTitulo, lblMsg, botones);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        alertStage.setScene(scene);
        alertStage.showAndWait();

        return respuesta[0];
    }
}