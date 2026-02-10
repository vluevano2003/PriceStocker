package com.vluevano.view;

import com.vluevano.model.Proveedor;
import com.vluevano.service.DialogService;
import com.vluevano.service.ProveedorService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import com.vluevano.util.ValidationUtils;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
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

    private Proveedor proveedorEnEdicion = null;
    private Button btnGuardar;
    private Label lblTituloFormulario;

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
                "Administra los proveedores a los que se les compra productos de forma regular",
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
     * Crea el panel de la tabla de proveedores
     * 
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
        String estiloThumb = "data:text/css," +
                ".scroll-bar:vertical .thumb {" +
                "    -fx-background-color: #DADADA;" +
                "    -fx-background-insets: 0 4 0 4;" + 
                "    -fx-background-radius: 4;" + 
                "}" +
                ".scroll-bar:horizontal .thumb {" +
                "    -fx-background-color: #DADADA;" +
                "    -fx-background-insets: 4 0 4 0;" +
                "    -fx-background-radius: 4;" +
                "}";
                
        tablaProveedores.getStylesheets().add(estiloThumb);

        tablaProveedores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaProveedores.setStyle(
                "-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        Label lblVacio = new Label("No hay proveedores registrados aún.");
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaProveedores.setPlaceholder(lblVacio);

        TableColumn<Proveedor, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdProveedor())));
        colId.setMinWidth(40);
        colId.setMaxWidth(40);

        TableColumn<Proveedor, String> colNombre = new TableColumn<>("Proveedor");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreProv()));
        colNombre.setMinWidth(150);

        TableColumn<Proveedor, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoProv()));
        colTel.setMinWidth(100);

        TableColumn<Proveedor, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorreoProv()));
        colCorreo.setMinWidth(150);

        TableColumn<Proveedor, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(data -> {
            Proveedor p = data.getValue();
            return new SimpleStringProperty(String.format("%s #%d, %s",
                    p.getCalle(), p.getNoExtProv(), p.getColonia()));
        });

        TableColumn<Proveedor, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = UIFactory
                    .crearBotonTablaEditar(() -> prepararEdicion(getTableView().getItems().get(getIndex())));

            private final Button btnEliminar = UIFactory
                    .crearBotonTablaEliminar(() -> eliminarProveedor(getTableView().getItems().get(getIndex())));

            private final HBox container = new HBox(5, btnEditar, btnEliminar);
            {
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tablaProveedores.getColumns().addAll(colId, colNombre, colTel, colCorreo, colDireccion, colAcciones);
        VBox.setVgrow(tablaProveedores, Priority.ALWAYS);

        tablaProveedores.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaProveedores.getSelectionModel().getSelectedItem() != null) {
                mostrarDetalleProveedor(tablaProveedores.getSelectionModel().getSelectedItem());
            }
        });

        box.getChildren().addAll(topBar, tablaProveedores);
        return box;
    }

    /**
     * Crea el panel del formulario de registro/edición de proveedores
     * 
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        lblTituloFormulario = new Label("Nuevo Proveedor");
        lblTituloFormulario.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTituloFormulario.setPadding(new Insets(20));

        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput("Ej. Distribuidora del Sur S.A. de C.V.");
        txtTelefono = UIFactory.crearInput("Ej. 55 8123 4567");
        txtCorreo = UIFactory.crearInput("Ej. ventas@proveedor.com");
        txtCalle = UIFactory.crearInput("Ej. Av. Vallarta");
        txtNoExt = UIFactory.crearInput("Ej. 505");
        txtNoInt = UIFactory.crearInput("Ej. Bodega 3");
        txtCp = UIFactory.crearInput("Ej. 44100");
        txtColonia = UIFactory.crearInput("Ej. Centro");
        txtCiudad = UIFactory.crearInput("Ej. Guadalajara");
        txtMunicipio = UIFactory.crearInput("Ej. Guadalajara");
        txtEstado = UIFactory.crearInput("Ej. Jalisco");
        txtPais = UIFactory.crearInput("Ej. México");
        txtRfc = UIFactory.crearInput("Ej. PRO800101A12");
        txtCurp = UIFactory.crearInput("Ej. ABCD800101HDFRXX01");

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll("Persona Moral", "Persona Física");
        cmbTipoPersona.getSelectionModel().select(0);
        cmbTipoPersona.setMaxWidth(Double.MAX_VALUE);
        cmbTipoPersona.setStyle(AppTheme.STYLE_INPUT);

        inputsContainer.getChildren().addAll(
                UIFactory.crearTituloSeccion("Datos Personales"),
                UIFactory.crearGrupoInput("Nombre *", txtNombre),
                UIFactory.crearGrupoInput("Teléfono *", txtTelefono),
                UIFactory.crearGrupoInput("Correo *", txtCorreo),

                UIFactory.crearTituloSeccion("Dirección"),
                UIFactory.crearGrupoInput("Calle *", txtCalle),
                new HBox(10,
                        UIFactory.crearGrupoInput("No. Ext *", txtNoExt),
                        UIFactory.crearGrupoInput("No. Int", txtNoInt),
                        UIFactory.crearGrupoInput("C.P. *", txtCp)),
                UIFactory.crearGrupoInput("Colonia *", txtColonia),
                UIFactory.crearGrupoInput("Ciudad *", txtCiudad),
                UIFactory.crearGrupoInput("Municipio *", txtMunicipio),
                new HBox(10,
                        UIFactory.crearGrupoInput("Estado *", txtEstado),
                        UIFactory.crearGrupoInput("País *", txtPais)),

                UIFactory.crearTituloSeccion("Datos Fiscales"),
                UIFactory.crearGrupoInput("Tipo Persona", cmbTipoPersona),
                UIFactory.crearGrupoInput("RFC", txtRfc),
                UIFactory.crearGrupoInput("CURP", txtCurp));

        ScrollPane scrollPane = new ScrollPane(inputsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));

        btnGuardar = UIFactory.crearBotonPrimario("Registrar Proveedor");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarProveedor());

        Button btnLimpiar = UIFactory.crearBotonTexto("Limpiar / Cancelar");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        footer.getChildren().addAll(btnGuardar, btnLimpiar, lblMensaje);
        card.getChildren().addAll(lblTituloFormulario, scrollPane, footer);
        return card;
    }

    /**
     * Registra o actualiza un proveedor según el estado del formulario
     */
    private void registrarProveedor() {
        if (ValidationUtils.esVacio(txtNombre) || ValidationUtils.esVacio(txtTelefono)
                || ValidationUtils.esVacio(txtCorreo) ||
                ValidationUtils.esVacio(txtCalle) || ValidationUtils.esVacio(txtNoExt) || ValidationUtils.esVacio(txtCp)
                ||
                ValidationUtils.esVacio(txtColonia) || ValidationUtils.esVacio(txtCiudad)
                || ValidationUtils.esVacio(txtEstado) || ValidationUtils.esVacio(txtPais)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Campos Faltantes",
                    "Por favor llene todos los campos marcados con *", stage);
            return;
        }

        if (!ValidationUtils.esEmailValido(txtCorreo.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Formato Inválido",
                    "El correo electrónico no es válido.", stage);
            return;
        }
        if (!ValidationUtils.esTelefonoValido(txtTelefono.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Formato Inválido",
                    "El teléfono debe tener 10 dígitos.", stage);
            return;
        }
        if (!ValidationUtils.esVacio(txtRfc) && !ValidationUtils.esRfcValido(txtRfc.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Formato Inválido",
                    "El RFC no tiene un formato válido.", stage);
            return;
        }
        if (!ValidationUtils.esVacio(txtCurp) && !ValidationUtils.esCurpValido(txtCurp.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Formato Inválido",
                    "La CURP no tiene un formato válido.", stage);
            return;
        }

        Proveedor prov = (proveedorEnEdicion != null) ? proveedorEnEdicion : new Proveedor();

        prov.setNombreProv(txtNombre.getText().trim());
        prov.setTelefonoProv(txtTelefono.getText().trim());
        prov.setCorreoProv(txtCorreo.getText().trim());
        prov.setRfcProveedor(ValidationUtils.esVacio(txtRfc) ? null : txtRfc.getText().trim().toUpperCase());
        prov.setCurp(ValidationUtils.esVacio(txtCurp) ? null : txtCurp.getText().trim().toUpperCase());

        try {
            prov.setCpProveedor(Integer.parseInt(txtCp.getText().trim()));
            prov.setNoExtProv(Integer.parseInt(txtNoExt.getText().trim()));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico",
                    "El CP y el No. Exterior deben ser números válidos.", stage);
            return;
        }

        prov.setNoIntProv(ValidationUtils.esVacio(txtNoInt) ? null : txtNoInt.getText().trim());

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
            String accion = (proveedorEnEdicion != null) ? "actualizado" : "guardado";
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Proveedor " + accion + " correctamente.",
                    stage);
            limpiarFormulario();
            cargarProveedores();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", res, stage);
        }
    }

    /**
     * Elimina un proveedor después de la confirmación del usuario
     * 
     * @param prov
     */
    private void eliminarProveedor(Proveedor prov) {
        if (proveedorEnEdicion != null && proveedorEnEdicion.getIdProveedor().equals(prov.getIdProveedor())) {
            limpiarFormulario();
        }

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
     * Limpia el formulario y resetea su estado
     */
    private void limpiarFormulario() {
        this.proveedorEnEdicion = null;
        lblTituloFormulario.setText("Nuevo Proveedor");

        btnGuardar.setText("Registrar Proveedor");
        btnGuardar.setStyle("-fx-background-color: " + AppTheme.COLOR_PRIMARY
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");

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
     * Prepara el formulario para editar un proveedor existente
     * 
     * @param p
     */
    private void prepararEdicion(Proveedor p) {
        this.proveedorEnEdicion = p;
        lblTituloFormulario.setText("Editar Proveedor (ID: " + p.getIdProveedor() + ")");
        btnGuardar.setText("Actualizar Proveedor");

        String styleBlue = "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        String styleBlueHover = "-fx-background-color: #1D4ED8; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        btnGuardar.setStyle(styleBlue);
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle(styleBlueHover));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(styleBlue));

        txtNombre.setText(p.getNombreProv());
        txtTelefono.setText(p.getTelefonoProv());
        txtCorreo.setText(p.getCorreoProv());
        txtCalle.setText(p.getCalle());
        txtNoExt.setText(String.valueOf(p.getNoExtProv()));
        txtNoInt.setText(p.getNoIntProv() == null ? "" : p.getNoIntProv());
        txtCp.setText(String.valueOf(p.getCpProveedor()));
        txtColonia.setText(p.getColonia());
        txtCiudad.setText(p.getCiudad());
        txtMunicipio.setText(p.getMunicipio());
        txtEstado.setText(p.getEstado());
        txtPais.setText(p.getPais());
        txtRfc.setText(p.getRfcProveedor() == null ? "" : p.getRfcProveedor());
        txtCurp.setText(p.getCurp() == null ? "" : p.getCurp());

        if (p.isEsPersonaFisica())
            cmbTipoPersona.getSelectionModel().select("Persona Física");
        else
            cmbTipoPersona.getSelectionModel().select("Persona Moral");
    }

    /**
     * Muestra un diálogo con los detalles completos del proveedor
     * 
     * @param prov
     */
    private void mostrarDetalleProveedor(Proveedor prov) {
        Stage dialog = new Stage();

        UIFactory.configurarStageModal(dialog, stage);

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

        grid.add(UIFactory.crearDatoDetalle("Teléfono:", prov.getTelefonoProv()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle("Correo:", prov.getCorreoProv()), 1, 0);

        String direccion = String.format("%s #%d%s", prov.getCalle(), prov.getNoExtProv(),
                (prov.getNoIntProv() != null && !prov.getNoIntProv().isEmpty() ? " Int " + prov.getNoIntProv() : ""));
        grid.add(UIFactory.crearDatoDetalle("Dirección:", direccion), 0, 1);
        grid.add(UIFactory.crearDatoDetalle("Colonia/CP:", prov.getColonia() + " C.P. " + prov.getCpProveedor()), 1, 1);

        grid.add(UIFactory.crearDatoDetalle("Ciudad/Mun:", prov.getCiudad() + ", " + prov.getMunicipio()), 0, 2);
        grid.add(UIFactory.crearDatoDetalle("Estado/País:", prov.getEstado() + ", " + prov.getPais()), 1, 2);

        if (prov.getCurp() != null && !prov.getCurp().isEmpty())
            grid.add(UIFactory.crearDatoDetalle("CURP:", prov.getCurp()), 0, 3);

        if (prov.getRfcProveedor() != null && !prov.getRfcProveedor().isEmpty())
            grid.add(UIFactory.crearDatoDetalle("RFC:", prov.getRfcProveedor()), 1, 3);

        Button btnCerrar = UIFactory.crearBotonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);
        dialogService.mostrarDialogoModal(dialog, root, stage);
    }
}