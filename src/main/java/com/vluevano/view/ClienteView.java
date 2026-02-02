package com.vluevano.view;

import com.vluevano.model.Cliente;
import com.vluevano.service.ClienteService;
import com.vluevano.service.DialogService;
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
public class ClienteView {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private DialogService dialogService;
    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;
    private TableView<Cliente> tablaClientes;
    private TextField txtFiltro;

    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo;
    private TextField txtCp, txtNoExt, txtNoInt, txtCalle, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private TextField txtCurp;
    private ComboBox<String> cmbTipoPersona;

    private Label lblMensaje;
    private Cliente clienteEnEdicion = null;
    private Button btnGuardar;
    private Label lblTituloFormulario;

    /**
     * Muestra la vista de gestión de clientes
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
        stage.setTitle("PriceStocker | Gestión de Clientes");
        stage.show();
        cargarClientes();
    }

    /**
     * Crea el contenido principal de la vista
     * 
     * @return
     */
    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader("Gestión de Clientes",
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
     * Crea el panel de la tabla de clientes con barra de búsqueda
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        txtFiltro = UIFactory.crearInput("Nombre, RFC, Municipio...");
        txtFiltro.setPrefWidth(300);
        txtFiltro.textProperty().addListener(
                (obs, oldVal, newVal) -> tablaClientes.getItems().setAll(clienteService.buscarClientes(newVal)));

        HBox topBar = new HBox(10, new Label("Buscar:"), txtFiltro);
        topBar.setAlignment(Pos.CENTER_LEFT);

        tablaClientes = new TableView<>();
        tablaClientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaClientes.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        TableColumn<Cliente, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdCliente())));
        colId.setMinWidth(40);
        colId.setMaxWidth(40);

        TableColumn<Cliente, String> colNombre = new TableColumn<>("Cliente");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCliente()));
        colNombre.setMinWidth(150);

        TableColumn<Cliente, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoCliente()));
        colTel.setMinWidth(100);

        TableColumn<Cliente, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorreoCliente()));
        colCorreo.setMinWidth(150);

        TableColumn<Cliente, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(data -> {
            Cliente c = data.getValue();
            return new SimpleStringProperty(
                    String.format("%s #%d, %s", c.getCalle(), c.getNoExtCliente(), c.getColonia()));
        });

        TableColumn<Cliente, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = UIFactory
                    .crearBotonTablaEditar(() -> prepararEdicion(getTableView().getItems().get(getIndex())));

            private final Button btnEliminar = UIFactory
                    .crearBotonTablaEliminar(() -> eliminarCliente(getTableView().getItems().get(getIndex())));

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

        tablaClientes.getColumns().addAll(colId, colNombre, colTel, colCorreo, colDireccion, colAcciones);
        VBox.setVgrow(tablaClientes, Priority.ALWAYS);

        tablaClientes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaClientes.getSelectionModel().getSelectedItem() != null) {
                mostrarDetalleCliente(tablaClientes.getSelectionModel().getSelectedItem());
            }
        });

        box.getChildren().addAll(topBar, tablaClientes);
        return box;
    }

    /**
     * Crea el panel del formulario para agregar/editar clientes
     * 
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        lblTituloFormulario = new Label("Nuevo Cliente");
        lblTituloFormulario.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTituloFormulario.setPadding(new Insets(20));

        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput("Ej. Juan Pérez");
        txtTelefono = UIFactory.crearInput("Ej. 55 1234 5678");
        txtCorreo = UIFactory.crearInput("cliente@empresa.com");
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
                UIFactory.crearTituloSeccion("Datos Personales"),
                UIFactory.crearGrupoInput("Nombre / Razón *", txtNombre),
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

        btnGuardar = UIFactory.crearBotonPrimario("Registrar Cliente");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarCliente());

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
     * Registra o actualiza un cliente según el estado del formulario
     */
    private void registrarCliente() {
        if (esVacio(txtNombre) || esVacio(txtTelefono) || esVacio(txtCorreo) ||
                esVacio(txtCalle) || esVacio(txtNoExt) || esVacio(txtCp) ||
                esVacio(txtColonia) || esVacio(txtCiudad) || esVacio(txtEstado) || esVacio(txtPais)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Campos Faltantes",
                    "Por favor llene todos los campos marcados con *", stage);
            return;
        }

        Cliente cliente = (clienteEnEdicion != null) ? clienteEnEdicion : new Cliente();

        cliente.setNombreCliente(txtNombre.getText().trim());
        cliente.setNombreFiscal(txtNombre.getText().trim());
        cliente.setTelefonoCliente(txtTelefono.getText().trim());
        cliente.setCorreoCliente(txtCorreo.getText().trim());
        cliente.setRfcCliente(esVacio(txtRfc) ? null : txtRfc.getText().trim());
        cliente.setCurp(esVacio(txtCurp) ? null : txtCurp.getText().trim());

        try {
            cliente.setCpCliente(Integer.parseInt(txtCp.getText().trim()));
            cliente.setNoExtCliente(Integer.parseInt(txtNoExt.getText().trim()));
            cliente.setNoIntCliente(esVacio(txtNoInt) ? 0 : Integer.parseInt(txtNoInt.getText().trim()));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico",
                    "CP, No. Ext y No. Int deben ser números válidos.", stage);
            return;
        }

        cliente.setCalle(txtCalle.getText().trim());
        cliente.setColonia(txtColonia.getText().trim());
        cliente.setCiudad(txtCiudad.getText().trim());
        cliente.setMunicipio(txtMunicipio.getText().trim());
        cliente.setEstado(txtEstado.getText().trim());
        cliente.setPais(txtPais.getText().trim());

        String tipoSeleccionado = cmbTipoPersona.getValue();
        cliente.setEsPersonaFisica(tipoSeleccionado != null && tipoSeleccionado.equals("Persona Física"));

        String res = clienteService.guardarCliente(cliente);

        if (res.toLowerCase().contains("exitosamente") || res.toLowerCase().contains("guardado")) {
            String accion = (clienteEnEdicion != null) ? "actualizado" : "guardado";
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Cliente " + accion + " correctamente.",
                    stage);
            limpiarFormulario();
            cargarClientes();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", res, stage);
        }
    }

    /**
     * Prepara el formulario para editar un cliente existente
     * 
     * @param cliente
     */
    private void prepararEdicion(Cliente cliente) {
        this.clienteEnEdicion = cliente;
        lblTituloFormulario.setText("Editar Cliente (ID: " + cliente.getIdCliente() + ")");
        btnGuardar.setText("Actualizar Cliente");
        btnGuardar.setStyle(
                "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");

        txtNombre.setText(cliente.getNombreCliente());
        txtTelefono.setText(cliente.getTelefonoCliente());
        txtCorreo.setText(cliente.getCorreoCliente());
        txtCalle.setText(cliente.getCalle());
        txtNoExt.setText(String.valueOf(cliente.getNoExtCliente()));
        txtNoInt.setText(cliente.getNoIntCliente() == 0 ? "" : String.valueOf(cliente.getNoIntCliente()));
        txtCp.setText(String.valueOf(cliente.getCpCliente()));
        txtColonia.setText(cliente.getColonia());
        txtCiudad.setText(cliente.getCiudad());
        txtMunicipio.setText(cliente.getMunicipio());
        txtEstado.setText(cliente.getEstado());
        txtPais.setText(cliente.getPais());
        txtRfc.setText(cliente.getRfcCliente() == null ? "" : cliente.getRfcCliente());
        txtCurp.setText(cliente.getCurp() == null ? "" : cliente.getCurp());

        if (cliente.isEsPersonaFisica()) {
            cmbTipoPersona.getSelectionModel().select("Persona Física");
        } else {
            cmbTipoPersona.getSelectionModel().select("Persona Moral");
        }
    }

    /**
     * Elimina un cliente después de la confirmación del usuario
     * 
     * @param c
     */
    private void eliminarCliente(Cliente c) {
        if (clienteEnEdicion != null && clienteEnEdicion.getIdCliente().equals(c.getIdCliente())) {
            limpiarFormulario();
        }

        if (dialogService.mostrarConfirmacion("Eliminar Cliente",
                "¿Seguro que deseas eliminar a " + c.getNombreCliente() + "?", stage)) {
            if (clienteService.eliminarCliente(c)) {
                cargarClientes();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado",
                        "Cliente eliminado correctamente.", stage);
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el cliente.", stage);
            }
        }
    }

    /**
     * Carga la lista de clientes en la tabla.
     */
    private void cargarClientes() {
        tablaClientes.getItems().setAll(clienteService.consultarClientes());
    }

    /**
     * Limpia el formulario y restablece su estado inicial
     */
    private void limpiarFormulario() {
        this.clienteEnEdicion = null;
        lblTituloFormulario.setText("Nuevo Cliente");

        btnGuardar.setText("Registrar Cliente");
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
     * Muestra un diálogo con el detalle completo de un cliente
     * 
     * @param c
     */
    private void mostrarDetalleCliente(Cliente c) {
        Stage dialog = new Stage();

        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(500);
        root.setMaxWidth(500);

        Label lblTitulo = new Label(c.getNombreCliente());
        lblTitulo
                .setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(c.isEsPersonaFisica() ? "Persona Física" : "Persona Moral");
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        grid.add(UIFactory.crearDatoDetalle("Teléfono:", c.getTelefonoCliente()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle("Correo:", c.getCorreoCliente()), 1, 0);

        String direccion = String.format("%s #%d%s", c.getCalle(), c.getNoExtCliente(),
                (c.getNoIntCliente() > 0 ? " Int " + c.getNoIntCliente() : ""));
        grid.add(UIFactory.crearDatoDetalle("Dirección:", direccion), 0, 1);
        grid.add(UIFactory.crearDatoDetalle("Colonia/CP:", c.getColonia() + " C.P. " + c.getCpCliente()), 1, 1);

        grid.add(UIFactory.crearDatoDetalle("Ciudad/Mun:", c.getCiudad() + ", " + c.getMunicipio()), 0, 2);
        grid.add(UIFactory.crearDatoDetalle("Estado/País:", c.getEstado() + ", " + c.getPais()), 1, 2);

        if (c.getCurp() != null && !c.getCurp().isEmpty())
            grid.add(UIFactory.crearDatoDetalle("CURP:", c.getCurp()), 0, 3);

        if (c.getRfcCliente() != null && !c.getRfcCliente().isEmpty())
            grid.add(UIFactory.crearDatoDetalle("RFC:", c.getRfcCliente()), 1, 3);

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