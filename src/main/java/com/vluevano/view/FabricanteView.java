package com.vluevano.view;

import com.vluevano.model.Fabricante;
import com.vluevano.service.DialogService;
import com.vluevano.service.FabricanteService;
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
public class FabricanteView {

    @Autowired
    private FabricanteService fabricanteService;
    @Autowired
    private DialogService dialogService;
    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;
    private TableView<Fabricante> tablaFabricantes;
    private TextField txtFiltro;

    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo;
    private TextField txtCp, txtNoExt, txtNoInt, txtCalle, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private TextField txtCurp;
    private ComboBox<String> cmbTipoPersona;

    private Label lblMensaje;
    private Fabricante fabricanteEnEdicion = null;
    private Button btnGuardar;
    private Label lblTituloFormulario;

    /**
     * Muestra la vista de gestión de fabricantes
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
        stage.setTitle("PriceStocker | Gestión de Fabricantes");
        stage.show();
        cargarFabricantes();
    }

    /**
     * Crea el contenido principal de la vista
     * 
     * @return
     */
    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader("Gestión de Fabricantes",
                "Administra los fabricantes a los que se les compra productos de forma regular",
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
     * Crea el panel con la tabla de fabricantes
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        txtFiltro = UIFactory.crearInput("Nombre, RFC, Municipio...");
        txtFiltro.setPrefWidth(300);
        txtFiltro.textProperty().addListener((obs, oldVal, newVal) -> tablaFabricantes.getItems()
                .setAll(fabricanteService.buscarFabricantes(newVal)));

        HBox topBar = new HBox(10, new Label("Buscar:"), txtFiltro);
        topBar.setAlignment(Pos.CENTER_LEFT);

        tablaFabricantes = new TableView<>();
        tablaFabricantes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaFabricantes.setStyle(
                "-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        Label lblVacio = new Label("No hay fabricantes registrados aún.");
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaFabricantes.setPlaceholder(lblVacio);

        TableColumn<Fabricante, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getIdFabricante())));
        colId.setMinWidth(40);
        colId.setMaxWidth(40);

        TableColumn<Fabricante, String> colNombre = new TableColumn<>("Fabricante");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreFabricante()));
        colNombre.setMinWidth(150);

        TableColumn<Fabricante, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoFabricante()));
        colTel.setMinWidth(100);

        TableColumn<Fabricante, String> colCorreo = new TableColumn<>("Correo");
        colCorreo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCorreoFabricante()));
        colCorreo.setMinWidth(150);

        TableColumn<Fabricante, String> colDireccion = new TableColumn<>("Dirección");
        colDireccion.setCellValueFactory(data -> {
            Fabricante f = data.getValue();
            return new SimpleStringProperty(String.format("%s #%d, %s",
                    f.getCalle(), f.getNoExtFabricante(), f.getColonia()));
        });

        TableColumn<Fabricante, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = UIFactory
                    .crearBotonTablaEditar(() -> prepararEdicion(getTableView().getItems().get(getIndex())));

            private final Button btnEliminar = UIFactory
                    .crearBotonTablaEliminar(() -> eliminarFabricante(getTableView().getItems().get(getIndex())));

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

        tablaFabricantes.getColumns().addAll(colId, colNombre, colTel, colCorreo, colDireccion, colAcciones);
        VBox.setVgrow(tablaFabricantes, Priority.ALWAYS);

        tablaFabricantes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaFabricantes.getSelectionModel().getSelectedItem() != null) {
                mostrarDetalleFabricante(tablaFabricantes.getSelectionModel().getSelectedItem());
            }
        });

        box.getChildren().addAll(topBar, tablaFabricantes);
        return box;
    }

    /**
     * Crea el panel con el formulario de registro/edición
     * 
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        lblTituloFormulario = new Label("Nuevo Fabricante");
        lblTituloFormulario.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTituloFormulario.setPadding(new Insets(20));

        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput("Ej. Manufacturas Industriales S.A.");
        txtTelefono = UIFactory.crearInput("Ej. 81 2233 4455");
        txtCorreo = UIFactory.crearInput("Ej. planta@fabricante.com");
        txtCalle = UIFactory.crearInput("Ej. Carr. Monterrey-Saltillo");
        txtNoExt = UIFactory.crearInput("Ej. Km 12.5");
        txtNoInt = UIFactory.crearInput("Ej. Nave 4");
        txtCp = UIFactory.crearInput("Ej. 66350");
        txtColonia = UIFactory.crearInput("Ej. Zona Industrial");
        txtCiudad = UIFactory.crearInput("Ej. Santa Catarina");
        txtMunicipio = UIFactory.crearInput("Ej. Santa Catarina");
        txtEstado = UIFactory.crearInput("Ej. Nuevo León");
        txtPais = UIFactory.crearInput("Ej. México");
        txtRfc = UIFactory.crearInput("Ej. MIN701231T12");
        txtCurp = UIFactory.crearInput("Ej. ABCD701231HNLRSX05");

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

        btnGuardar = UIFactory.crearBotonPrimario("Registrar Fabricante");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarFabricante());

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
     * Registra o actualiza un fabricante según el estado del formulario
     */
    private void registrarFabricante() {
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

        Fabricante f = (fabricanteEnEdicion != null) ? fabricanteEnEdicion : new Fabricante();

        f.setNombreFabricante(txtNombre.getText().trim());
        f.setTelefonoFabricante(txtTelefono.getText().trim());
        f.setCorreoFabricante(txtCorreo.getText().trim());
        f.setRfcFabricante(ValidationUtils.esVacio(txtRfc) ? null : txtRfc.getText().trim().toUpperCase());
        f.setCurp(ValidationUtils.esVacio(txtCurp) ? null : txtCurp.getText().trim().toUpperCase());

        try {
            f.setCpFabricante(Integer.parseInt(txtCp.getText().trim()));
            f.setNoExtFabricante(Integer.parseInt(txtNoExt.getText().trim()));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico",
                    "El CP y el No. Exterior deben ser números válidos.", stage);
            return;
        }

        f.setNoIntFabricante(ValidationUtils.esVacio(txtNoInt) ? null : txtNoInt.getText().trim());

        f.setCalle(txtCalle.getText().trim());
        f.setColonia(txtColonia.getText().trim());
        f.setCiudad(txtCiudad.getText().trim());
        f.setMunicipio(txtMunicipio.getText().trim());
        f.setEstado(txtEstado.getText().trim());
        f.setPais(txtPais.getText().trim());

        String tipoSeleccionado = cmbTipoPersona.getValue();
        f.setEsPersonaFisica(tipoSeleccionado != null && tipoSeleccionado.equals("Persona Física"));

        String res = fabricanteService.guardarFabricante(f);

        if (res.toLowerCase().contains("exitosamente") || res.toLowerCase().contains("guardado")) {
            String accion = (fabricanteEnEdicion != null) ? "actualizado" : "guardado";
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito",
                    "Fabricante " + accion + " correctamente.", stage);
            limpiarFormulario();
            cargarFabricantes();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", res, stage);
        }
    }

    /**
     * Elimina un fabricante
     * 
     * @param f
     */
    private void eliminarFabricante(Fabricante f) {
        if (fabricanteEnEdicion != null && fabricanteEnEdicion.getIdFabricante().equals(f.getIdFabricante())) {
            limpiarFormulario();
        }

        if (dialogService.mostrarConfirmacion("Eliminar Fabricante",
                "¿Seguro que deseas eliminar a " + f.getNombreFabricante() + "?", stage)) {
            if (fabricanteService.eliminarFabricante(f)) {
                cargarFabricantes();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado",
                        "Fabricante eliminado correctamente.", stage);
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el fabricante.",
                        stage);
            }
        }
    }

    /**
     * Carga los fabricantes en la tabla
     */
    private void cargarFabricantes() {
        tablaFabricantes.getItems().setAll(fabricanteService.consultarFabricantes());
    }

    /**
     * Limpia el formulario y resetea su estado
     */
    private void limpiarFormulario() {
        this.fabricanteEnEdicion = null;
        lblTituloFormulario.setText("Nuevo Fabricante");
        btnGuardar.setText("Registrar Fabricante");
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
     * Prepara el formulario para editar un fabricante existente
     * 
     * @param f
     */
    private void prepararEdicion(Fabricante f) {
        this.fabricanteEnEdicion = f;
        lblTituloFormulario.setText("Editar Fabricante (ID: " + f.getIdFabricante() + ")");
        btnGuardar.setText("Actualizar Fabricante");

        String styleBlue = "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        String styleBlueHover = "-fx-background-color: #1D4ED8; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        btnGuardar.setStyle(styleBlue);
        btnGuardar.setOnMouseEntered(e -> btnGuardar.setStyle(styleBlueHover));
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(styleBlue));

        txtNombre.setText(f.getNombreFabricante());
        txtTelefono.setText(f.getTelefonoFabricante());
        txtCorreo.setText(f.getCorreoFabricante());
        txtCalle.setText(f.getCalle());
        txtNoExt.setText(String.valueOf(f.getNoExtFabricante()));
        txtNoInt.setText(f.getNoIntFabricante() == null ? "" : f.getNoIntFabricante());
        txtCp.setText(String.valueOf(f.getCpFabricante()));
        txtColonia.setText(f.getColonia());
        txtCiudad.setText(f.getCiudad());
        txtMunicipio.setText(f.getMunicipio());
        txtEstado.setText(f.getEstado());
        txtPais.setText(f.getPais());
        txtRfc.setText(f.getRfcFabricante() == null ? "" : f.getRfcFabricante());
        txtCurp.setText(f.getCurp() == null ? "" : f.getCurp());

        if (f.isEsPersonaFisica())
            cmbTipoPersona.getSelectionModel().select("Persona Física");
        else
            cmbTipoPersona.getSelectionModel().select("Persona Moral");
    }

    /**
     * Muestra un diálogo con el detalle completo del fabricante
     * 
     * @param f
     */
    private void mostrarDetalleFabricante(Fabricante f) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(500);
        root.setMaxWidth(500);

        Label lblTitulo = new Label(f.getNombreFabricante());
        lblTitulo
                .setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(f.isEsPersonaFisica() ? "Persona Física" : "Persona Moral");
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        grid.add(UIFactory.crearDatoDetalle("Teléfono:", f.getTelefonoFabricante()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle("Correo:", f.getCorreoFabricante()), 1, 0);

        String direccion = String.format("%s #%d%s", f.getCalle(), f.getNoExtFabricante(),
                (f.getNoIntFabricante() != null && !f.getNoIntFabricante().isEmpty() ? " Int " + f.getNoIntFabricante()
                        : ""));
        grid.add(UIFactory.crearDatoDetalle("Dirección:", direccion), 0, 1);
        grid.add(UIFactory.crearDatoDetalle("Colonia/CP:", f.getColonia() + " C.P. " + f.getCpFabricante()), 1, 1);

        grid.add(UIFactory.crearDatoDetalle("Ciudad/Mun:", f.getCiudad() + ", " + f.getMunicipio()), 0, 2);
        grid.add(UIFactory.crearDatoDetalle("Estado/País:", f.getEstado() + ", " + f.getPais()), 1, 2);

        if (f.getCurp() != null && !f.getCurp().isEmpty())
            grid.add(UIFactory.crearDatoDetalle("CURP:", f.getCurp()), 0, 3);

        if (f.getRfcFabricante() != null && !f.getRfcFabricante().isEmpty())
            grid.add(UIFactory.crearDatoDetalle("RFC:", f.getRfcFabricante()), 1, 3);

        Button btnCerrar = UIFactory.crearBotonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);
        dialogService.mostrarDialogoModal(dialog, root, stage);
    }
}