package com.vluevano.view;

import com.vluevano.model.Empresa;
import com.vluevano.service.DialogService;
import com.vluevano.service.EmpresaService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
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
public class EmpresaView {

    @Autowired
    private EmpresaService empresaService;
    @Autowired
    private DialogService dialogService;
    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;
    private TableView<Empresa> tablaEmpresas;
    private TextField txtFiltro;

    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo;
    private TextField txtCp, txtNoExt, txtNoInt, txtCalle, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private TextField txtCurp;
    private ComboBox<String> cmbTipoPersona;

    private Label lblMensaje;
    private Empresa empresaEnEdicion = null;
    private Button btnGuardar;
    private Label lblTituloFormulario;

    /**
     * Muestra la vista de gestión de empresas
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
        stage.setTitle("PriceStocker | Gestión de Empresas");
        stage.show();
        cargarEmpresas();
    }

    /**
     * Crea el contenido principal de la vista
     * 
     * @return
     */
    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader("Gestión de Empresas",
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
     * Crea el panel con la tabla de empresas y el filtro de búsqueda
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        txtFiltro = UIFactory.crearInput("Nombre, RFC, Municipio...");
        txtFiltro.setPrefWidth(300);
        txtFiltro.textProperty().addListener((obs, oldVal, newVal) -> tablaEmpresas.getItems()
                .setAll(empresaService.buscarEmpresas(newVal)));

        HBox topBar = new HBox(10, new Label("Buscar:"), txtFiltro);
        topBar.setAlignment(Pos.CENTER_LEFT);

        tablaEmpresas = new TableView<>();
        tablaEmpresas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaEmpresas.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        TableColumn<Empresa, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdEmpresa())));
        colId.setMinWidth(40);
        colId.setMaxWidth(40);

        TableColumn<Empresa, String> colNombre = new TableColumn<>("Empresa");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreEmpresa()));
        colNombre.setMinWidth(150);

        TableColumn<Empresa, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoEmpresa()));
        colTel.setMinWidth(100);

        TableColumn<Empresa, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorreoEmpresa()));
        colCorreo.setMinWidth(150);

        TableColumn<Empresa, String> colDireccion = new TableColumn<>("Dirección Completa");
        colDireccion.setCellValueFactory(data -> {
            Empresa e = data.getValue();
            return new SimpleStringProperty(String.format("%s #%d, %s",
                    e.getCalle(), e.getNoExtEmpresa(), e.getColonia()));
        });

        TableColumn<Empresa, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = UIFactory
                    .crearBotonTablaEditar(() -> prepararEdicion(getTableView().getItems().get(getIndex())));

            private final Button btnEliminar = UIFactory
                    .crearBotonTablaEliminar(() -> eliminarEmpresa(getTableView().getItems().get(getIndex())));

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

        tablaEmpresas.getColumns().addAll(colId, colNombre, colTel, colCorreo, colDireccion, colAcciones);
        VBox.setVgrow(tablaEmpresas, Priority.ALWAYS);

        tablaEmpresas.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaEmpresas.getSelectionModel().getSelectedItem() != null) {
                mostrarDetalleEmpresa(tablaEmpresas.getSelectionModel().getSelectedItem());
            }
        });

        box.getChildren().addAll(topBar, tablaEmpresas);
        return box;
    }

    /**
     * Crea el panel del formulario para agregar/editar empresas
     * 
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        lblTituloFormulario = new Label("Nueva Empresa");
        lblTituloFormulario.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTituloFormulario.setPadding(new Insets(20));

        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput("Ej. Soluciones Tecnológicas");
        txtTelefono = UIFactory.crearInput("Ej. 55 1234 5678");
        txtCorreo = UIFactory.crearInput("contacto@empresa.com");
        txtCalle = UIFactory.crearInput("Ej. Av. Reforma");
        txtNoExt = UIFactory.crearInput("Ej. 100");
        txtNoInt = UIFactory.crearInput("Ej. 2B");
        txtCp = UIFactory.crearInput("Ej. 96400");
        txtColonia = UIFactory.crearInput("Ej. Centro");
        txtCiudad = UIFactory.crearInput("Ej. Coatzacoalcos");
        txtMunicipio = UIFactory.crearInput("Ej. Coatzacoalcos");
        txtEstado = UIFactory.crearInput("Ej. Veracruz");
        txtPais = UIFactory.crearInput("Ej. México");
        txtRfc = UIFactory.crearInput("Ej. RFC123456789");
        txtCurp = UIFactory.crearInput("CURP (Si aplica)");

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll("Persona Moral", "Persona Física");
        cmbTipoPersona.getSelectionModel().select(0);
        cmbTipoPersona.setMaxWidth(Double.MAX_VALUE);
        cmbTipoPersona.setStyle(AppTheme.STYLE_INPUT);

        inputsContainer.getChildren().addAll(
                UIFactory.crearTituloSeccion("Datos Generales"),
                UIFactory.crearGrupoInput("Nombre Empresa *", txtNombre),
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

        btnGuardar = UIFactory.crearBotonPrimario("Registrar Empresa");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarEmpresa());

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
     * Registra o actualiza una empresa según el estado del formulario
     */
    private void registrarEmpresa() {
        if (esVacio(txtNombre) || esVacio(txtTelefono) || esVacio(txtCorreo) ||
                esVacio(txtCalle) || esVacio(txtNoExt) || esVacio(txtCp) ||
                esVacio(txtColonia) || esVacio(txtCiudad) || esVacio(txtEstado) || esVacio(txtPais)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Campos Faltantes",
                    "Por favor llene todos los campos marcados con *", stage);
            return;
        }

        Empresa emp = (empresaEnEdicion != null) ? empresaEnEdicion : new Empresa();

        emp.setNombreEmpresa(txtNombre.getText().trim());
        emp.setTelefonoEmpresa(txtTelefono.getText().trim());
        emp.setCorreoEmpresa(txtCorreo.getText().trim());
        emp.setRfcEmpresa(esVacio(txtRfc) ? null : txtRfc.getText().trim());
        emp.setCurp(esVacio(txtCurp) ? null : txtCurp.getText().trim());

        try {
            emp.setCpEmpresa(Integer.parseInt(txtCp.getText().trim()));
            emp.setNoExtEmpresa(Integer.parseInt(txtNoExt.getText().trim()));
            emp.setNoIntEmpresa(esVacio(txtNoInt) ? 0 : Integer.parseInt(txtNoInt.getText().trim()));
        } catch (NumberFormatException ex) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico",
                    "CP, No. Ext y No. Int deben ser números válidos.", stage);
            return;
        }

        emp.setCalle(txtCalle.getText().trim());
        emp.setColonia(txtColonia.getText().trim());
        emp.setCiudad(txtCiudad.getText().trim());
        emp.setMunicipio(txtMunicipio.getText().trim());
        emp.setEstado(txtEstado.getText().trim());
        emp.setPais(txtPais.getText().trim());

        String tipoSeleccionado = cmbTipoPersona.getValue();
        emp.setEsPersonaFisica(tipoSeleccionado != null && tipoSeleccionado.equals("Persona Física"));

        String res = empresaService.guardarEmpresa(emp);

        if (res.toLowerCase().contains("exitosamente") || res.toLowerCase().contains("guardada")) {
            String accion = (empresaEnEdicion != null) ? "actualizada" : "guardada";
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Empresa " + accion + " correctamente.",
                    stage);
            limpiarFormulario();
            cargarEmpresas();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", res, stage);
        }
    }

    /**
     * Elimina una empresa después de la confirmación del usuario
     * 
     * @param emp
     */
    private void eliminarEmpresa(Empresa emp) {
        if (empresaEnEdicion != null && empresaEnEdicion.getIdEmpresa().equals(emp.getIdEmpresa())) {
            limpiarFormulario();
        }

        if (dialogService.mostrarConfirmacion("Eliminar Empresa",
                "¿Seguro que deseas eliminar a " + emp.getNombreEmpresa() + "?", stage)) {
            if (empresaService.eliminarEmpresa(emp)) {
                cargarEmpresas();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminada",
                        "Empresa eliminada correctamente.", stage);
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar la empresa.", stage);
            }
        }
    }

    /**
     * Carga las empresas desde el servicio y las muestra en la tabla
     */
    private void cargarEmpresas() {
        tablaEmpresas.getItems().setAll(empresaService.consultarEmpresas());
    }

    /**
     * Limpia el formulario y resetea su estado
     */
    private void limpiarFormulario() {
        this.empresaEnEdicion = null;
        lblTituloFormulario.setText("Nueva Empresa");
        btnGuardar.setText("Registrar Empresa");
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
     * Prepara el formulario para editar una empresa existente
     * 
     * @param e
     */
    private void prepararEdicion(Empresa e) {
        this.empresaEnEdicion = e;
        lblTituloFormulario.setText("Editar Empresa (ID: " + e.getIdEmpresa() + ")");
        btnGuardar.setText("Actualizar Empresa");
        btnGuardar.setStyle(
                "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");

        txtNombre.setText(e.getNombreEmpresa());
        txtTelefono.setText(e.getTelefonoEmpresa());
        txtCorreo.setText(e.getCorreoEmpresa());
        txtCalle.setText(e.getCalle());
        txtNoExt.setText(String.valueOf(e.getNoExtEmpresa()));
        txtNoInt.setText(e.getNoIntEmpresa() == 0 ? "" : String.valueOf(e.getNoIntEmpresa()));
        txtCp.setText(String.valueOf(e.getCpEmpresa()));
        txtColonia.setText(e.getColonia());
        txtCiudad.setText(e.getCiudad());
        txtMunicipio.setText(e.getMunicipio());
        txtEstado.setText(e.getEstado());
        txtPais.setText(e.getPais());
        txtRfc.setText(e.getRfcEmpresa() == null ? "" : e.getRfcEmpresa());
        txtCurp.setText(e.getCurp() == null ? "" : e.getCurp());

        if (e.isEsPersonaFisica())
            cmbTipoPersona.getSelectionModel().select("Persona Física");
        else
            cmbTipoPersona.getSelectionModel().select("Persona Moral");
    }

    /**
     * Muestra un diálogo con el detalle completo de una empresa
     * 
     * @param emp
     */
    private void mostrarDetalleEmpresa(Empresa emp) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(500);
        root.setMaxWidth(500);

        Label lblTitulo = new Label(emp.getNombreEmpresa());
        lblTitulo
                .setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(emp.isEsPersonaFisica() ? "Persona Física" : "Persona Moral");
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        grid.add(UIFactory.crearDatoDetalle("Teléfono:", emp.getTelefonoEmpresa()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle("Correo:", emp.getCorreoEmpresa()), 1, 0);

        String direccion = String.format("%s #%d%s", emp.getCalle(), emp.getNoExtEmpresa(),
                (emp.getNoIntEmpresa() > 0 ? " Int " + emp.getNoIntEmpresa() : ""));
        grid.add(UIFactory.crearDatoDetalle("Dirección:", direccion), 0, 1);
        grid.add(UIFactory.crearDatoDetalle("Colonia/CP:", emp.getColonia() + " C.P. " + emp.getCpEmpresa()), 1, 1);
        grid.add(UIFactory.crearDatoDetalle("Ciudad/Mun:", emp.getCiudad() + ", " + emp.getMunicipio()), 0, 2);
        grid.add(UIFactory.crearDatoDetalle("Estado/País:", emp.getEstado() + ", " + emp.getPais()), 1, 2);

        if (emp.getCurp() != null && !emp.getCurp().isEmpty())
            grid.add(UIFactory.crearDatoDetalle("CURP:", emp.getCurp()), 0, 3);
        if (emp.getRfcEmpresa() != null && !emp.getRfcEmpresa().isEmpty())
            grid.add(UIFactory.crearDatoDetalle("RFC:", emp.getRfcEmpresa()), 1, 3);

        Button btnCerrar = UIFactory.crearBotonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);
        dialogService.mostrarDialogoModal(dialog, root, stage);
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
}