package com.vluevano.view;

import com.vluevano.model.PrestadorServicio;
import com.vluevano.model.Servicio;
import com.vluevano.service.DialogService;
import com.vluevano.service.PrestadorServicioService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PrestadorServicioView {

    @Autowired
    private PrestadorServicioService prestadorService;
    @Autowired
    private DialogService dialogService;
    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;

    private TableView<PrestadorServicio> tablaPrestadores;
    private TextField txtFiltro;
    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo, txtCurp;
    private TextField txtCalle, txtNoExt, txtNoInt, txtCp, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private ComboBox<String> cmbTipoPersona;
    private ListView<Servicio> listServicios;
    private ObservableList<Servicio> serviciosObservable;
    private TextField txtDescServicio, txtCostoServicio;
    private ComboBox<String> cmbMoneda;
    private Button btnGuardar;
    private Label lblTituloFormulario;
    private PrestadorServicio prestadorEnEdicion = null;

    /**
     * Muestra la pantalla de gestión de prestadores de servicio
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
        stage.setTitle("PriceStocker | Gestión de Prestadores de Servicio");
        stage.show();
        cargarPrestadores();
    }

    /**
     * Crea el contenido principal de la vista
     * 
     * @return
     */
    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");
        root.setTop(UIFactory.crearHeader("Gestión de Prestadores",
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
     * Crea el panel de la tabla de prestadores
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        txtFiltro = UIFactory.crearInput("Nombre, RFC, Municipio...");
        txtFiltro.setPrefWidth(300);
        txtFiltro.textProperty().addListener((obs, oldVal, newVal) -> tablaPrestadores.getItems()
                .setAll(prestadorService.buscarPrestadores(newVal)));

        HBox topBar = new HBox(10, new Label("Buscar:"), txtFiltro);
        topBar.setAlignment(Pos.CENTER_LEFT);

        tablaPrestadores = new TableView<>();
        tablaPrestadores.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaPrestadores.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        TableColumn<PrestadorServicio, String> colNombre = new TableColumn<>("Prestador");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombrePrestador()));
        colNombre.setMinWidth(140);

        TableColumn<PrestadorServicio, String> colTel = new TableColumn<>("Teléfono");
        colTel.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefonoPrestador()));

        TableColumn<PrestadorServicio, String> colUbicacion = new TableColumn<>("Ubicación");
        colUbicacion.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getCiudad() + ", " + data.getValue().getEstado()));

        TableColumn<PrestadorServicio, String> colServicios = new TableColumn<>("Servicios");
        colServicios.setCellValueFactory(data -> {
            String resumen = data.getValue().getServicios().stream()
                    .map(Servicio::getDescripcionServicio)
                    .limit(2)
                    .collect(Collectors.joining(", "));
            if (data.getValue().getServicios().size() > 2)
                resumen += "...";
            return new SimpleStringProperty(resumen);
        });

        TableColumn<PrestadorServicio, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = UIFactory
                    .crearBotonTablaEditar(() -> prepararEdicion(getTableView().getItems().get(getIndex())));
            private final Button btnEliminar = UIFactory
                    .crearBotonTablaEliminar(() -> eliminarPrestador(getTableView().getItems().get(getIndex())));
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

        tablaPrestadores.getColumns().addAll(colNombre, colTel, colUbicacion, colServicios, colAcciones);
        VBox.setVgrow(tablaPrestadores, Priority.ALWAYS);

        box.getChildren().addAll(topBar, tablaPrestadores);
        return box;
    }

    /**
     * Crea el panel del formulario de prestador
     * 
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        lblTituloFormulario = new Label("Nuevo Prestador");
        lblTituloFormulario.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTituloFormulario.setPadding(new Insets(20));

        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput("Ej. Transportes del Norte");
        txtRfc = UIFactory.crearInput("RFC");
        txtTelefono = UIFactory.crearInput("Teléfono");
        txtCorreo = UIFactory.crearInput("Email");
        txtCurp = UIFactory.crearInput("CURP");

        txtCalle = UIFactory.crearInput("Calle");
        txtNoExt = UIFactory.crearInput("No. Ext");
        txtNoInt = UIFactory.crearInput("Int");
        txtCp = UIFactory.crearInput("CP");
        txtColonia = UIFactory.crearInput("Colonia");
        txtCiudad = UIFactory.crearInput("Ciudad");
        txtMunicipio = UIFactory.crearInput("Municipio");
        txtEstado = UIFactory.crearInput("Estado");
        txtPais = UIFactory.crearInput("País");

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll("Persona Moral", "Persona Física");
        cmbTipoPersona.getSelectionModel().select(0);
        cmbTipoPersona.setMaxWidth(Double.MAX_VALUE);
        cmbTipoPersona.setStyle(AppTheme.STYLE_INPUT);

        serviciosObservable = FXCollections.observableArrayList();
        listServicios = new ListView<>(serviciosObservable);
        listServicios.setPrefHeight(150);
        listServicios.setStyle("-fx-border-color: #E5E7EB; -fx-background-radius: 4; -fx-font-size: 13px;");
        listServicios.setCellFactory(param -> new ListCell<>() {
            private final Button btnEliminar = new Button("X");
            private final Label lblTexto = new Label();
            private final HBox container = new HBox(10, lblTexto, new Region(), btnEliminar);

            {
                container.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(container.getChildren().get(1), Priority.ALWAYS);

                btnEliminar.setStyle(
                        "-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #DC2626; -fx-border-radius: 3; -fx-padding: 2 6 2 6;");
                btnEliminar.setOnAction(e -> {
                    Servicio item = getItem();
                    if (item != null)
                        serviciosObservable.remove(item);
                });
            }

            @Override
            protected void updateItem(Servicio item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblTexto.setText(item.toString());
                    setGraphic(container);
                }
            }
        });

        txtDescServicio = UIFactory.crearInput("Descripción (Ej. Flete local)");
        txtCostoServicio = UIFactory.crearInput("Costo");

        cmbMoneda = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMoneda.getSelectionModel().select(0);
        cmbMoneda.setPrefWidth(90);
        cmbMoneda.setStyle(
                "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px;");

        Button btnAddServicio = new Button("Agregar");
        String styleNormal = "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #A7F3D0; -fx-background-radius: 6; -fx-border-radius: 6;";
        String styleHover = "-fx-background-color: #A7F3D0; -fx-text-fill: #064E3B; -fx-cursor: hand; -fx-font-weight: bold; -fx-border-color: #6EE7B7; -fx-background-radius: 6; -fx-border-radius: 6;";

        btnAddServicio.setStyle(styleNormal);
        btnAddServicio.setOnMouseEntered(e -> btnAddServicio.setStyle(styleHover));
        btnAddServicio.setOnMouseExited(e -> btnAddServicio.setStyle(styleNormal));
        btnAddServicio.setMinWidth(80);

        btnAddServicio.setOnAction(e -> agregarServicioALista());

        VBox boxInputsServicio = new VBox(8);
        boxInputsServicio.getChildren().add(txtDescServicio);

        HBox row2 = new HBox(10, txtCostoServicio, cmbMoneda, btnAddServicio);
        row2.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtCostoServicio, Priority.ALWAYS);

        boxInputsServicio.getChildren().add(row2);

        Label lblTituloServicios = UIFactory.crearTituloSeccion("Servicios Ofrecidos *");
        lblTituloServicios.setPadding(new Insets(15, 0, 2, 0));

        VBox seccionServicios = new VBox(5);
        seccionServicios.getChildren().addAll(
                lblTituloServicios,
                new Label("Agregue al menos un servicio:"),
                boxInputsServicio,
                listServicios);

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
                UIFactory.crearGrupoInput("CURP", txtCurp),
                seccionServicios);

        ScrollPane scrollPane = new ScrollPane(inputsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));
        btnGuardar = UIFactory.crearBotonPrimario("Registrar Prestador");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarPrestador());

        Button btnLimpiar = UIFactory.crearBotonTexto("Cancelar / Limpiar");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        footer.getChildren().addAll(btnGuardar, btnLimpiar);
        card.getChildren().addAll(lblTituloFormulario, scrollPane, footer);
        return card;
    }

    /**
     * Agrega un servicio a la lista del prestador
     */
    private void agregarServicioALista() {
        if (esVacio(txtDescServicio) || esVacio(txtCostoServicio)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Datos Incompletos",
                    "Ingrese descripción y costo del servicio.", stage);
            return;
        }
        try {
            double costo = Double.parseDouble(txtCostoServicio.getText());
            Servicio s = new Servicio(txtDescServicio.getText(), costo, cmbMoneda.getValue());
            serviciosObservable.add(s);
            txtDescServicio.clear();
            txtCostoServicio.clear();
            txtDescServicio.requestFocus();
        } catch (NumberFormatException ex) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "El costo debe ser numérico", stage);
        }
    }

    /**
     * Registra o actualiza un prestador de servicio
     */
    private void registrarPrestador() {
        if (esVacio(txtNombre) || esVacio(txtTelefono) || esVacio(txtCorreo) ||
                esVacio(txtCalle) || esVacio(txtNoExt) || esVacio(txtCp) ||
                esVacio(txtColonia) || esVacio(txtCiudad) || esVacio(txtEstado) || esVacio(txtPais)) {

            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Campos Faltantes",
                    "Por favor llene todos los campos marcados con *", stage);
            return;
        }
        if (serviciosObservable.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Servicios Requeridos",
                    "Debe agregar al menos un servicio a la lista para registrar al prestador.", stage);
            txtDescServicio.requestFocus();
            return;
        }

        PrestadorServicio p = (prestadorEnEdicion != null) ? prestadorEnEdicion : new PrestadorServicio();
        p.setNombrePrestador(txtNombre.getText().trim());
        p.setRfcPrestador(esVacio(txtRfc) ? null : txtRfc.getText().trim());
        p.setTelefonoPrestador(txtTelefono.getText().trim());
        p.setCorreoPrestador(txtCorreo.getText().trim());
        p.setCurp(esVacio(txtCurp) ? null : txtCurp.getText().trim());

        String tipoSeleccionado = cmbTipoPersona.getValue();
        p.setEsPersonaFisica(tipoSeleccionado != null && tipoSeleccionado.equals("Persona Física"));

        try {
            p.setCpPrestador(Integer.parseInt(txtCp.getText().trim()));
            p.setNoExtPrestador(Integer.parseInt(txtNoExt.getText().trim()));
            String noInt = txtNoInt.getText();
            p.setNoIntPrestador(esVacio(txtNoInt) ? 0 : Integer.parseInt(noInt.trim()));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico",
                    "CP, No. Ext y No. Int deben ser números válidos.", stage);
            return;
        }

        p.setCalle(txtCalle.getText().trim());
        p.setColonia(txtColonia.getText().trim());
        p.setCiudad(txtCiudad.getText().trim());
        p.setMunicipio(txtMunicipio.getText().trim());
        p.setEstado(txtEstado.getText().trim());
        p.setPais(txtPais.getText().trim());
        p.getServicios().clear();
        for (Servicio s : serviciosObservable) {
            p.addServicio(s);
        }

        String res = prestadorService.guardarPrestador(p);
        if (res.toLowerCase().contains("exitosamente")) {
            String accion = (prestadorEnEdicion != null) ? "actualizado" : "guardado";
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Prestador " + accion + " correctamente.",
                    stage);
            limpiarFormulario();
            cargarPrestadores();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", res, stage);
        }
    }

    /**
     * Prepara el formulario para editar un prestador existente
     * 
     * @param p
     */
    private void prepararEdicion(PrestadorServicio p) {
        this.prestadorEnEdicion = p;
        lblTituloFormulario.setText("Editar Prestador (ID: " + p.getIdPrestador() + ")");
        btnGuardar.setText("Actualizar Prestador");
        btnGuardar.setStyle(
                "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");

        txtNombre.setText(p.getNombrePrestador());
        txtRfc.setText(p.getRfcPrestador() == null ? "" : p.getRfcPrestador());
        txtTelefono.setText(p.getTelefonoPrestador());
        txtCorreo.setText(p.getCorreoPrestador());
        txtCurp.setText(p.getCurp() == null ? "" : p.getCurp());

        if (p.isEsPersonaFisica())
            cmbTipoPersona.getSelectionModel().select("Persona Física");
        else
            cmbTipoPersona.getSelectionModel().select("Persona Moral");

        txtCalle.setText(p.getCalle());
        txtNoExt.setText(String.valueOf(p.getNoExtPrestador()));
        txtNoInt.setText(p.getNoIntPrestador() == 0 ? "" : String.valueOf(p.getNoIntPrestador()));
        txtCp.setText(String.valueOf(p.getCpPrestador()));
        txtColonia.setText(p.getColonia());
        txtCiudad.setText(p.getCiudad());
        txtMunicipio.setText(p.getMunicipio());
        txtEstado.setText(p.getEstado());
        txtPais.setText(p.getPais());

        serviciosObservable.setAll(p.getServicios());
    }

    /**
     * Elimina un prestador de servicio
     * 
     * @param p
     */
    private void eliminarPrestador(PrestadorServicio p) {
        if (dialogService.mostrarConfirmacion("Eliminar Prestador",
                "¿Seguro que deseas eliminar a " + p.getNombrePrestador() + "?", stage)) {
            if (prestadorService.eliminarPrestador(p)) {
                cargarPrestadores();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Eliminado",
                        "Prestador eliminado correctamente.", stage);
                if (prestadorEnEdicion != null && prestadorEnEdicion.getIdPrestador().equals(p.getIdPrestador())) {
                    limpiarFormulario();
                }
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el prestador.", stage);
            }
        }
    }

    /**
     * Limpia el formulario y resetea el estado de edición
     */
    private void limpiarFormulario() {
        prestadorEnEdicion = null;
        lblTituloFormulario.setText("Nuevo Prestador");
        btnGuardar.setText("Registrar Prestador");
        btnGuardar.setStyle("-fx-background-color: " + AppTheme.COLOR_PRIMARY
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        txtNombre.clear();
        txtRfc.clear();
        txtTelefono.clear();
        txtCorreo.clear();
        txtCurp.clear();
        txtCalle.clear();
        txtNoExt.clear();
        txtNoInt.clear();
        txtCp.clear();
        txtColonia.clear();
        txtCiudad.clear();
        txtMunicipio.clear();
        txtEstado.clear();
        txtPais.clear();
        cmbTipoPersona.getSelectionModel().select(0);
        serviciosObservable.clear();
        txtDescServicio.clear();
        txtCostoServicio.clear();
    }

    /**
     * Carga los prestadores desde el servicio y los muestra en la tabla
     */
    private void cargarPrestadores() {
        tablaPrestadores.getItems().setAll(prestadorService.consultarPrestadores());
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