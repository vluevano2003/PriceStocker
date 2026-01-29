package com.vluevano.view;

import com.vluevano.model.Proveedor;
import com.vluevano.service.DialogService;
import com.vluevano.service.ProveedorService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
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
    private DialogService dialogService;
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

    /**
     * Muestra la vista de gestión de proveedores
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
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader("Gestión de Proveedores",
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

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
     * Crea el panel con la tabla de proveedores
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        txtFiltro = UIFactory.crearInput("Nombre, RFC, Municipio...");
        txtFiltro.setPrefWidth(300);
        txtFiltro.textProperty().addListener((obs, oldVal, newVal) -> tablaProveedores.getItems()
                .setAll(proveedorService.buscarProveedores(newVal)));

        HBox topBar = new HBox(10, new Label("Buscar:"), txtFiltro);
        topBar.setAlignment(Pos.CENTER_LEFT);

        tablaProveedores = new TableView<>();
        tablaProveedores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaProveedores.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        TableColumn<Proveedor, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdProveedor())));
        colId.setMinWidth(40);
        colId.setMaxWidth(40);

        TableColumn<Proveedor, String> colNombre = new TableColumn<>("Proveedor");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreProv()));

        TableColumn<Proveedor, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoProv()));

        TableColumn<Proveedor, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorreoProv()));

        TableColumn<Proveedor, String> colDireccion = new TableColumn<>("Dirección Completa");
        colDireccion.setCellValueFactory(data -> {
            Proveedor p = data.getValue();
            return new SimpleStringProperty(String.format("%s #%d, %s, CP %d, %s, %s, %s",
                    p.getCalle(), p.getNoExtProv(), p.getColonia(), p.getCpProveedor(),
                    p.getCiudad(), p.getEstado(), p.getPais()));
        });

        TableColumn<Proveedor, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");
            {
                btnEliminar.setStyle(
                        "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEliminar.setOnAction(e -> eliminarProveedor(getTableView().getItems().get(getIndex())));
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnEliminar);
            }
        });

        tablaProveedores.getColumns().addAll(colId, colNombre, colTel, colCorreo, colDireccion, colAcciones);
        VBox.setVgrow(tablaProveedores, Priority.ALWAYS);
        tablaProveedores.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null)
                mostrarDetalleProveedor(newVal);
        });

        box.getChildren().addAll(topBar, tablaProveedores);
        return box;
    }

    /**
     * Crea el panel con el formulario para agregar un nuevo proveedor
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        Label lblTitle = new Label("Nuevo Proveedor");
        lblTitle.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTitle.setPadding(new Insets(20));

        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput("Ej. Juan Pérez");
        txtTelefono = UIFactory.crearInput("Ej. 55 1234 5678");
        txtCorreo = UIFactory.crearInput("contacto@empresa.com");
        txtCalle = UIFactory.crearInput("Ej. Av. Reforma");
        txtNoExt = UIFactory.crearInput("Ej. 123");
        txtNoInt = UIFactory.crearInput("Ej. Piso 2");
        txtCp = UIFactory.crearInput("Ej. 06600");
        txtColonia = UIFactory.crearInput("Ej. Juárez");
        txtCiudad = UIFactory.crearInput("Ej. Ciudad de México");
        txtMunicipio = UIFactory.crearInput("Ej. Cuauhtémoc");
        txtEstado = UIFactory.crearInput("Ej. CDMX");
        txtPais = UIFactory.crearInput("Ej. México");
        txtRfc = UIFactory.crearInput("Ej. XAXX010101000");
        txtCurp = UIFactory.crearInput("Ej. CURP");

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll("Persona Moral", "Persona Física");
        cmbTipoPersona.getSelectionModel().select(0);
        cmbTipoPersona.setMaxWidth(Double.MAX_VALUE);
        cmbTipoPersona.setStyle(AppTheme.STYLE_INPUT);

        inputsContainer.getChildren().addAll(
                crearSeccion("Datos Personales"),
                crearInputConLabel("Nombre / Razón *", txtNombre),
                crearInputConLabel("Teléfono *", txtTelefono),
                crearInputConLabel("Correo *", txtCorreo),
                crearSeccion("Dirección"),
                crearInputConLabel("Calle *", txtCalle),
                new HBox(10, crearInputConLabel("No. Ext *", txtNoExt), crearInputConLabel("No. Int", txtNoInt),
                        crearInputConLabel("C.P. *", txtCp)),
                crearInputConLabel("Colonia *", txtColonia),
                crearInputConLabel("Ciudad *", txtCiudad),
                crearInputConLabel("Municipio *", txtMunicipio),
                new HBox(10, crearInputConLabel("Estado *", txtEstado), crearInputConLabel("País *", txtPais)),
                crearSeccion("Datos Fiscales"),
                crearInputConLabel("Tipo Persona", cmbTipoPersona),
                crearInputConLabel("RFC", txtRfc),
                crearInputConLabel("CURP", txtCurp));

        ScrollPane scrollPane = new ScrollPane(inputsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));

        Button btnGuardar = UIFactory.crearBotonPrimario("Registrar Proveedor");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarProveedor());

        Button btnLimpiar = UIFactory.crearBotonTexto("Limpiar Formulario");
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
     * @param textoLabel
     * @param campo
     * @return
     */
    private VBox crearInputConLabel(String textoLabel, Node campo) {
        Label l = new Label(textoLabel);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13px;");
        if (textoLabel.contains("*"))
            l.setTextFill(Color.web(AppTheme.COLOR_PRIMARY));
        VBox v = new VBox(5, l, campo);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    /**
     * Crea un Label para secciones del formulario
     * @param texto
     * @return
     */
    private Label crearSeccion(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY
                + "; -fx-font-size: 14px; -fx-padding: 15 0 5 0;");
        return l;
    }

    /**
     * Registra un nuevo proveedor
     */
    private void registrarProveedor() {
        if (esVacio(txtNombre) || esVacio(txtTelefono) || esVacio(txtCorreo) ||
                esVacio(txtCalle) || esVacio(txtNoExt) || esVacio(txtCp) ||
                esVacio(txtColonia) || esVacio(txtCiudad) || esVacio(txtEstado) || esVacio(txtPais)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Campos Faltantes",
                    "Por favor llene todos los campos marcados con *", stage);
            return;
        }

        Proveedor prov = new Proveedor();
        prov.setNombreProv(txtNombre.getText().trim());
        prov.setTelefonoProv(txtTelefono.getText().trim());
        prov.setCorreoProv(txtCorreo.getText().trim());

        prov.setRfcProveedor(esVacio(txtRfc) ? null : txtRfc.getText().trim());
        prov.setCurp(esVacio(txtCurp) ? null : txtCurp.getText().trim());

        try {
            prov.setCpProveedor(Integer.parseInt(txtCp.getText().trim()));
            prov.setNoExtProv(Integer.parseInt(txtNoExt.getText().trim()));
            prov.setNoIntProv(esVacio(txtNoInt) ? 0 : Integer.parseInt(txtNoInt.getText().trim()));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico",
                    "CP, No. Ext y No. Int deben ser números válidos.", stage);
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

        String res = proveedorService.guardarProveedor(prov);
        if (res.toLowerCase().contains("exitosamente") || res.toLowerCase().contains("guardado")) {
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", res, stage);
            limpiarFormulario();
            cargarProveedores();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", res, stage);
        }
    }

    /**
     * Elimina un proveedor
     * @param prov
     */
    private void eliminarProveedor(Proveedor prov) {
        if (dialogService.mostrarConfirmacion("Eliminar Proveedor",
                "¿Seguro que deseas eliminar a " + prov.getNombreProv() + "?", stage)) {
            if (proveedorService.eliminarProveedor(prov)) {
                cargarProveedores();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado",
                        "Proveedor eliminado correctamente.", stage);
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el proveedor.", stage);
            }
        }
    }

    /**
     * Carga los proveedores en la tabla
     */
    private void cargarProveedores() {
        tablaProveedores.getItems().setAll(proveedorService.consultarProveedores());
    }

    /**
     * Limpia el formulario de entrada
     */
    private void limpiarFormulario() {
        txtNombre.clear();
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
        txtRfc.clear();
        txtCurp.clear();
        cmbTipoPersona.getSelectionModel().select(0);
        lblMensaje.setText("");
    }

    /**
     * Verifica si un TextField está vacío
     * @param tf
     * @return
     */
    private boolean esVacio(TextField tf) {
        return tf.getText() == null || tf.getText().trim().isEmpty();
    }

    /**
     * Muestra un diálogo con el detalle del proveedor
     * @param prov
     */
    private void mostrarDetalleProveedor(Proveedor prov) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(500);
        root.setMaxWidth(500);

        Label lblTitulo = new Label(prov.getNombreProv());
        lblTitulo
                .setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
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

        if (prov.getCurp() != null && !prov.getCurp().isEmpty())
            agregarDatoGrid(grid, "CURP:", prov.getCurp(), 0, 3);

        if (prov.getRfcProveedor() != null && !prov.getRfcProveedor().isEmpty())
            agregarDatoGrid(grid, "RFC:", prov.getRfcProveedor(), 1, 3);

        Button btnCerrar = UIFactory.crearBotonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);

        dialogService.mostrarDialogoModal(dialog, root, stage);
    }

    /**
     * Agrega un label y valor al GridPane
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
}