package com.vluevano.view;

import com.vluevano.model.Fabricante;
import com.vluevano.service.DialogService;
import com.vluevano.service.FabricanteService;
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
     * Muestra la pantalla de gestión de fabricantes
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
     * Crea el contenido principal de la pantalla
     * 
     * @return
     */
    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader("Gestión de Fabricantes",
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
     * Crea el panel con la tabla de fabricantes y el filtro de búsqueda
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
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

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

        TableColumn<Fabricante, String> colDireccion = new TableColumn<>("Dirección Completa");
        colDireccion.setCellValueFactory(data -> {
            Fabricante f = data.getValue();
            return new SimpleStringProperty(String.format("%s #%d, %s, CP %d, %s, %s, %s",
                    f.getCalle(), f.getNoExtFabricante(), f.getColonia(), f.getCpFabricante(),
                    f.getCiudad(), f.getEstado(), f.getPais()));
        });

        TableColumn<Fabricante, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            {
                btnEditar.setStyle(
                        "-fx-background-color: #DBEAFE; -fx-text-fill: #1D4ED8; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: bold;");
                btnEditar.setOnAction(e -> prepararEdicion(getTableView().getItems().get(getIndex())));
            }

            private final Button btnEliminar = new Button("Eliminar");
            {
                btnEliminar.setStyle(
                        "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-cursor: hand; -fx-font-size: 11px;");
                btnEliminar.setOnAction(e -> eliminarFabricante(getTableView().getItems().get(getIndex())));
            }

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
     * Crea el panel del formulario para agregar/editar fabricantes
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

        txtNombre = UIFactory.crearInput("Ej. Intel Corp");
        txtTelefono = UIFactory.crearInput("Ej. 55 1234 5678");
        txtCorreo = UIFactory.crearInput("contacto@intel.com");
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
     * Registra un nuevo fabricante o actualiza uno existente
     */
    private void registrarFabricante() {
        if (esVacio(txtNombre) || esVacio(txtTelefono) || esVacio(txtCorreo) ||
                esVacio(txtCalle) || esVacio(txtNoExt) || esVacio(txtCp) ||
                esVacio(txtColonia) || esVacio(txtCiudad) || esVacio(txtEstado) || esVacio(txtPais)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Campos Faltantes",
                    "Por favor llene todos los campos marcados con *", stage);
            return;
        }

        Fabricante f;
        if (fabricanteEnEdicion != null) {
            f = fabricanteEnEdicion;
        } else {
            f = new Fabricante();
        }

        f.setNombreFabricante(txtNombre.getText().trim());
        f.setTelefonoFabricante(txtTelefono.getText().trim());
        f.setCorreoFabricante(txtCorreo.getText().trim());
        f.setRfcFabricante(esVacio(txtRfc) ? null : txtRfc.getText().trim());
        f.setCurp(esVacio(txtCurp) ? null : txtCurp.getText().trim());

        try {
            f.setCpFabricante(Integer.parseInt(txtCp.getText().trim()));
            f.setNoExtFabricante(Integer.parseInt(txtNoExt.getText().trim()));
            f.setNoIntFabricante(esVacio(txtNoInt) ? 0 : Integer.parseInt(txtNoInt.getText().trim()));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error Numérico",
                    "CP, No. Ext y No. Int deben ser números válidos.", stage);
            return;
        }

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
     * Prepara el formulario para editar un fabricante existente
     * 
     * @param f
     */
    private void prepararEdicion(Fabricante f) {
        this.fabricanteEnEdicion = f;
        lblTituloFormulario.setText("Editar Fabricante (ID: " + f.getIdFabricante() + ")");
        btnGuardar.setText("Actualizar Fabricante");
        btnGuardar.setStyle(
                "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");

        txtNombre.setText(f.getNombreFabricante());
        txtTelefono.setText(f.getTelefonoFabricante());
        txtCorreo.setText(f.getCorreoFabricante());
        txtCalle.setText(f.getCalle());
        txtNoExt.setText(String.valueOf(f.getNoExtFabricante()));
        txtNoInt.setText(f.getNoIntFabricante() == 0 ? "" : String.valueOf(f.getNoIntFabricante()));
        txtCp.setText(String.valueOf(f.getCpFabricante()));
        txtColonia.setText(f.getColonia());
        txtCiudad.setText(f.getCiudad());
        txtMunicipio.setText(f.getMunicipio());
        txtEstado.setText(f.getEstado());
        txtPais.setText(f.getPais());
        txtRfc.setText(f.getRfcFabricante() == null ? "" : f.getRfcFabricante());
        txtCurp.setText(f.getCurp() == null ? "" : f.getCurp());

        if (f.isEsPersonaFisica()) {
            cmbTipoPersona.getSelectionModel().select("Persona Física");
        } else {
            cmbTipoPersona.getSelectionModel().select("Persona Moral");
        }
    }

    /**
     * Elimina un fabricante después de la confirmación del usuario
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
     * Carga los fabricantes desde el servicio y los muestra en la tabla
     */
    private void cargarFabricantes() {
        tablaFabricantes.getItems().setAll(fabricanteService.consultarFabricantes());
    }

    /**
     * Limpia el formulario y resetea el estado de edición
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
     * Muestra un diálogo con el detalle completo del fabricante
     * @param f
     */
    private void mostrarDetalleFabricante(Fabricante f) {
        Stage dialog = new Stage();
        dialog.initOwner(stage);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initStyle(StageStyle.TRANSPARENT);

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

        agregarDatoGrid(grid, "Teléfono:", f.getTelefonoFabricante(), 0, 0);
        agregarDatoGrid(grid, "Correo:", f.getCorreoFabricante(), 1, 0);

        String direccion = String.format("%s #%d%s", f.getCalle(), f.getNoExtFabricante(),
                (f.getNoIntFabricante() > 0 ? " Int " + f.getNoIntFabricante() : ""));
        agregarDatoGrid(grid, "Dirección:", direccion, 0, 1);
        agregarDatoGrid(grid, "Colonia/CP:", f.getColonia() + " C.P. " + f.getCpFabricante(), 1, 1);

        agregarDatoGrid(grid, "Ciudad/Mun:", f.getCiudad() + ", " + f.getMunicipio(), 0, 2);
        agregarDatoGrid(grid, "Estado/País:", f.getEstado() + ", " + f.getPais(), 1, 2);

        if (f.getCurp() != null && !f.getCurp().isEmpty())
            agregarDatoGrid(grid, "CURP:", f.getCurp(), 0, 3);

        if (f.getRfcFabricante() != null && !f.getRfcFabricante().isEmpty())
            agregarDatoGrid(grid, "RFC:", f.getRfcFabricante(), 1, 3);

        Button btnCerrar = UIFactory.crearBotonSecundario("Cerrar");
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);

        dialogService.mostrarDialogoModal(dialog, root, stage);
    }

    /**
     * Agrega un par de etiqueta-valor al grid del detalle
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
     * Crea un contenedor VBox con un label y un campo de entrada
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
     * Crea un label de sección estilizado
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
     * Verifica si un TextField está vacío
     * @param tf
     * @return
     */
    private boolean esVacio(TextField tf) {
        return tf.getText() == null || tf.getText().trim().isEmpty();
    }
}