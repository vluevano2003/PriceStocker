package com.vluevano.view;

import com.vluevano.model.PrestadorServicio;
import com.vluevano.model.Servicio;
import com.vluevano.service.MonedaService;
import com.vluevano.service.PrestadorServicioService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import com.vluevano.util.ValidationUtils;
import com.vluevano.view.base.BaseDirectorioView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PrestadorServicioView extends BaseDirectorioView<PrestadorServicio> {

    @Autowired
    private PrestadorServicioService prestadorService;
    
    @Autowired
    private MonedaService monedaService;

    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo, txtCurp;
    private TextField txtCalle, txtNoExt, txtNoInt, txtCp, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private ComboBox<String> cmbTipoPersona;
    
    private ListView<Servicio> listServicios;
    private ObservableList<Servicio> serviciosObservable;
    private TextField txtDescServicio, txtCostoServicio;
    private ComboBox<String> cmbMoneda;

    @Override
    public void show(Stage stage, String usuarioActual) {
        monedaService.inicializar();
        super.show(stage, usuarioActual);
    }

    @Override
    protected String getTituloVentana() {
        return idioma.get("provider.window.title");
    }

    @Override
    protected String getTituloHeader() {
        return idioma.get("provider.header.title");
    }

    @Override
    protected String getSubtituloHeader() {
        return idioma.get("provider.header.subtitle");
    }

    @Override
    protected String getPromptBusqueda() {
        return idioma.get("provider.search.prompt");
    }

    @Override
    protected String getTituloFormularioNuevo() {
        return idioma.get("provider.form.new");
    }

    @Override
    protected void cargarDatos() {
        tablaDatos.getItems().setAll(prestadorService.consultarPrestadores());
    }

    @Override
    protected void buscarDatos(String valor) {
        tablaDatos.getItems().setAll(prestadorService.buscarPrestadores(valor));
    }

    /**
     * Construye el formulario con campos específicos para PrestadorServicio, incluyendo validaciones y lógica para agregar servicios al proveedor
     */
    @Override
    protected VBox construirCamposFormulario() {
        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput(idioma.get("provider.placeholder.name"));
        txtRfc = UIFactory.crearInput("Ej. TFR101010XYZ");
        txtTelefono = UIFactory.crearInput("Ej. 229 988 7766");
        txtCorreo = UIFactory.crearInput("facturacion@transportes.com");
        txtCurp = UIFactory.crearInput("Ej. GOMA850215HVZRRX05");

        txtCalle = UIFactory.crearInput(idioma.get("provider.placeholder.street"));
        txtNoExt = UIFactory.crearInput("Ej. 45");
        txtNoInt = UIFactory.crearInput(idioma.get("provider.placeholder.int"));
        txtCp = UIFactory.crearInput("Ej. 91000");
        txtColonia = UIFactory.crearInput(idioma.get("provider.placeholder.colonia"));
        txtCiudad = UIFactory.crearInput(idioma.get("provider.placeholder.city"));
        txtMunicipio = UIFactory.crearInput(idioma.get("provider.placeholder.mun"));
        txtEstado = UIFactory.crearInput(idioma.get("provider.placeholder.state"));
        txtPais = UIFactory.crearInput(idioma.get("provider.placeholder.country"));

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll(idioma.get("provider.person.moral"), idioma.get("provider.person.fisica"));
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
                btnEliminar.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #DC2626; -fx-border-radius: 3; -fx-padding: 2 6 2 6;");
                btnEliminar.setOnAction(e -> {
                    Servicio item = getItem();
                    if (item != null) serviciosObservable.remove(item);
                });
            }

            @Override
            protected void updateItem(Servicio item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    lblTexto.setText(item.getDescripcionServicio() + " - " + formatearPrecioInteligente(item.getCostoServicio(), item.getMonedaServicio()));
                    setGraphic(container);
                }
            }
        });

        txtDescServicio = UIFactory.crearInput(idioma.get("provider.placeholder.service_desc"));
        txtCostoServicio = UIFactory.crearInput(idioma.get("provider.placeholder.service_cost"));
        txtCostoServicio.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().matches("\\d*|\\d+\\.\\d*") ? change : null));

        cmbMoneda = new ComboBox<>(FXCollections.observableArrayList("MXN", "USD"));
        cmbMoneda.setValue(monedaService.getMonedaPorDefecto());
        cmbMoneda.setPrefWidth(90);
        cmbMoneda.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 13px;");

        Label lblConvServicio = new Label("");
        lblConvServicio.setStyle("-fx-text-fill: #F97316; -fx-font-size: 11px; -fx-font-weight: bold;");

        Runnable calc = () -> calcularEquivalenciaGeneral(txtCostoServicio, cmbMoneda, lblConvServicio);
        txtCostoServicio.textProperty().addListener((o, v, n) -> calc.run());
        cmbMoneda.setOnAction(e -> calc.run());

        Button btnAddServicio = new Button(idioma.get("provider.btn.add"));
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

        VBox groupCosto = new VBox(2, row2, lblConvServicio);
        boxInputsServicio.getChildren().add(groupCosto);

        Label lblTituloServicios = UIFactory.crearTituloSeccion(idioma.get("provider.section.services"));
        lblTituloServicios.setPadding(new Insets(15, 0, 2, 0));

        VBox seccionServicios = new VBox(5);
        seccionServicios.getChildren().addAll(
                lblTituloServicios,
                new Label(idioma.get("provider.lbl.add_service")),
                boxInputsServicio,
                listServicios);

        inputsContainer.getChildren().addAll(
                UIFactory.crearTituloSeccion(idioma.get("provider.section.personal")),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.name"), txtNombre),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.phone"), txtTelefono),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.email"), txtCorreo),

                UIFactory.crearTituloSeccion(idioma.get("provider.section.address")),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.street"), txtCalle),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("provider.lbl.noext"), txtNoExt),
                        UIFactory.crearGrupoInput(idioma.get("provider.lbl.noint"), txtNoInt),
                        UIFactory.crearGrupoInput(idioma.get("provider.lbl.cp"), txtCp)),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.colonia"), txtColonia),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.city"), txtCiudad),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.mun"), txtMunicipio),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("provider.lbl.state"), txtEstado),
                        UIFactory.crearGrupoInput(idioma.get("provider.lbl.country"), txtPais)),

                UIFactory.crearTituloSeccion(idioma.get("provider.section.fiscal")),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.person_type"), cmbTipoPersona),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.rfc"), txtRfc),
                UIFactory.crearGrupoInput(idioma.get("provider.lbl.curp"), txtCurp),
                seccionServicios
        );

        return inputsContainer;
    }

    /**
     * Configura las columnas de la tabla para mostrar los datos relevantes de PrestadorServicio, incluyendo una columna de acciones para editar/eliminar cada registro
     */
    @Override
    protected void configurarColumnasTabla() {
        Label lblVacio = new Label(idioma.get("provider.table.empty"));
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaDatos.setPlaceholder(lblVacio);

        TableColumn<PrestadorServicio, String> colId = UIFactory.crearColumna(idioma.get("provider.col.id"), p -> String.valueOf(p.getIdPrestador()), 50);
        colId.setMaxWidth(50);
        colId.setStyle("-fx-alignment: CENTER;");

        TableColumn<PrestadorServicio, String> colNombre = UIFactory.crearColumna(idioma.get("provider.col.name"), PrestadorServicio::getNombrePrestador, 140);
        TableColumn<PrestadorServicio, String> colTel = UIFactory.crearColumna(idioma.get("provider.col.phone"), PrestadorServicio::getTelefonoPrestador, 0);
        TableColumn<PrestadorServicio, String> colUbicacion = UIFactory.crearColumna(idioma.get("provider.col.location"), p -> p.getCiudad() + ", " + p.getEstado(), 0);

        TableColumn<PrestadorServicio, String> colServicios = UIFactory.crearColumna(idioma.get("provider.col.services"), p -> {
            String resumen = p.getServicios().stream()
                    .map(Servicio::getDescripcionServicio)
                    .limit(2)
                    .collect(Collectors.joining(", "));
            if (p.getServicios().size() > 2) resumen += "...";
            return resumen;
        }, 0);

        TableColumn<PrestadorServicio, Void> colAcciones = new TableColumn<>(idioma.get("provider.col.actions"));
        colAcciones.setMinWidth(140);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = UIFactory.crearBotonTablaEditar(() -> prepararEdicion(getTableView().getItems().get(getIndex())));
            private final Button btnEliminar = UIFactory.crearBotonTablaEliminar(() -> eliminarEntidad(getTableView().getItems().get(getIndex())));
            private final HBox container = new HBox(5, btnEditar, btnEliminar);
            { container.setAlignment(Pos.CENTER); }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        tablaDatos.getColumns().addAll(List.of(colId, colNombre, colTel, colUbicacion, colServicios, colAcciones));
    }

    /**
     * Limpia los campos del formulario específicos de PrestadorServicio, incluyendo la lista de servicios y los campos de descripción/costo para agregar nuevos servicios
     */
    @Override
    protected void limpiarCamposEspecificos() {
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
     * Valida los campos del formulario y construye un objeto PrestadorServicio con los datos ingresados, luego llama al servicio para guardar o actualizar el registro en la base de datos. También maneja la lógica para mostrar mensajes de éxito/error según el resultado de la operación
     */
    @Override
    protected void registrarEntidad() {
        if (ValidationUtils.esVacio(txtNombre) || ValidationUtils.esVacio(txtTelefono) || ValidationUtils.esVacio(txtCorreo)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("provider.msg.missing.title"), idioma.get("provider.msg.missing.content"), stage);
            return;
        }

        if (!ValidationUtils.esEmailValido(txtCorreo.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("provider.msg.invalid.title"), idioma.get("provider.msg.invalid.email"), stage);
            return;
        }
        if (!ValidationUtils.esTelefonoValido(txtTelefono.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("provider.msg.invalid.title"), idioma.get("provider.msg.invalid.phone"), stage);
            return;
        }

        if (serviciosObservable.isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("provider.msg.services_req.title"), idioma.get("provider.msg.services_req.content"), stage);
            txtDescServicio.requestFocus();
            return;
        }

        PrestadorServicio p = (entidadEnEdicion != null) ? entidadEnEdicion : new PrestadorServicio();
        p.setNombrePrestador(txtNombre.getText().trim());
        p.setRfcPrestador(ValidationUtils.esVacio(txtRfc) ? null : txtRfc.getText().trim().toUpperCase());
        p.setTelefonoPrestador(txtTelefono.getText().trim());
        p.setCorreoPrestador(txtCorreo.getText().trim());
        p.setCurp(ValidationUtils.esVacio(txtCurp) ? null : txtCurp.getText().trim().toUpperCase());
        p.setEsPersonaFisica(cmbTipoPersona.getValue() != null && cmbTipoPersona.getValue().equals(idioma.get("provider.person.fisica")));

        try {
            String cpStr = txtCp.getText().trim();
            p.setCpPrestador(cpStr.isEmpty() ? 0 : Integer.parseInt(cpStr));

            String noExtStr = txtNoExt.getText().trim();
            p.setNoExtPrestador(noExtStr.isEmpty() ? 0 : Integer.parseInt(noExtStr));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("provider.msg.numeric.title"), idioma.get("provider.msg.numeric.content"), stage);
            return;
        }

        p.setNoIntPrestador(ValidationUtils.esVacio(txtNoInt) ? null : txtNoInt.getText().trim());
        p.setCalle(txtCalle.getText().trim());
        p.setColonia(txtColonia.getText().trim());
        p.setCiudad(txtCiudad.getText().trim());
        p.setMunicipio(txtMunicipio.getText().trim());
        p.setEstado(txtEstado.getText().trim());
        p.setPais(txtPais.getText().trim());
        
        p.getServicios().clear();
        p.getServicios().addAll(serviciosObservable);

        String res = prestadorService.guardarPrestador(p);

        if (res.toLowerCase().contains("exitosamente") || res.toLowerCase().contains("guardado")) {
            String accion = (entidadEnEdicion != null) ? idioma.get("provider.msg.success.updated") : idioma.get("provider.msg.success.saved");
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("provider.msg.success.title"), accion, stage);
            limpiarFormulario();
            cargarDatos();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("provider.msg.error.title"), res, stage);
        }
    }

    /**
     * Llena el formulario con los datos del PrestadorServicio seleccionado para edición, permitiendo modificar sus campos y servicios asociados. Cambia el título del formulario y el texto del botón de guardar para reflejar que se está editando un registro existente
     * @param p
     */
    private void prepararEdicion(PrestadorServicio p) {
        this.entidadEnEdicion = p;
        lblTituloFormulario.setText(idioma.get("provider.form.edit", p.getIdPrestador()));
        btnGuardar.setText(idioma.get("provider.btn.update"));

        String styleBlue = "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        btnGuardar.setStyle(styleBlue);
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(styleBlue));

        txtNombre.setText(p.getNombrePrestador());
        txtRfc.setText(p.getRfcPrestador() == null ? "" : p.getRfcPrestador());
        txtTelefono.setText(p.getTelefonoPrestador());
        txtCorreo.setText(p.getCorreoPrestador());
        txtCurp.setText(p.getCurp() == null ? "" : p.getCurp());

        cmbTipoPersona.getSelectionModel().select(p.isEsPersonaFisica() ? idioma.get("provider.person.fisica") : idioma.get("provider.person.moral"));

        txtCalle.setText(p.getCalle());
        txtNoExt.setText(String.valueOf(p.getNoExtPrestador()));
        txtNoInt.setText(p.getNoIntPrestador() == null ? "" : p.getNoIntPrestador());
        txtCp.setText(String.valueOf(p.getCpPrestador()));
        txtColonia.setText(p.getColonia());
        txtCiudad.setText(p.getCiudad());
        txtMunicipio.setText(p.getMunicipio());
        txtEstado.setText(p.getEstado());
        txtPais.setText(p.getPais());

        serviciosObservable.setAll(p.getServicios());
    }

    /**
     * Muestra un diálogo de confirmación antes de eliminar el PrestadorServicio seleccionado. Si se confirma, llama al servicio para eliminar el registro de la base de datos y muestra un mensaje de éxito o error según el resultado de la operación. También limpia el formulario si el registro eliminado es el que se estaba editando actualmente
     * @param p
     */
    private void eliminarEntidad(PrestadorServicio p) {
        if (entidadEnEdicion != null && entidadEnEdicion.getIdPrestador().equals(p.getIdPrestador())) {
            limpiarFormulario();
        }
        if (dialogService.mostrarConfirmacion(idioma.get("provider.msg.delete.title"), idioma.get("provider.msg.delete.confirm", p.getNombrePrestador()), stage)) {
            if (prestadorService.eliminarPrestador(p)) {
                cargarDatos();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("provider.msg.delete.title"), idioma.get("provider.msg.delete.success"), stage);
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("provider.msg.error.title"), idioma.get("provider.msg.delete.error"), stage);
            }
        }
    }

    /**
     * Muestra un diálogo con los detalles completos del PrestadorServicio seleccionado, incluyendo su información personal, dirección, tipo de persona, servicios ofrecidos y precios. El diseño del diálogo es limpio y organizado, con secciones claramente diferenciadas para cada grupo de información. También incluye un botón para cerrar el diálogo y volver a la vista principal de la lista de prestadores de servicio
     * @param prestador
     */
    @Override
    protected void mostrarDetalle(PrestadorServicio prestador) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(600);
        root.setMaxWidth(600);

        Label lblTitulo = new Label(prestador.getNombrePrestador());
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(prestador.isEsPersonaFisica() ? idioma.get("provider.person.fisica") : idioma.get("provider.person.moral"));
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10);

        grid.add(UIFactory.crearDatoDetalle(idioma.get("provider.detail.phone"), prestador.getTelefonoPrestador()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("provider.detail.email"), prestador.getCorreoPrestador()), 1, 0);

        String direccion = String.format("%s #%d%s", prestador.getCalle(), prestador.getNoExtPrestador(),
                (prestador.getNoIntPrestador() != null && !prestador.getNoIntPrestador().isEmpty() ? " Int " + prestador.getNoIntPrestador() : ""));
        grid.add(UIFactory.crearDatoDetalle(idioma.get("provider.detail.address"), direccion), 0, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("provider.detail.colonia_cp"), prestador.getColonia() + " C.P. " + prestador.getCpPrestador()), 1, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("provider.detail.city_mun"), prestador.getCiudad() + ", " + prestador.getMunicipio()), 0, 2);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("provider.detail.state_country"), prestador.getEstado() + ", " + prestador.getPais()), 1, 2);

        if (prestador.getCurp() != null && !prestador.getCurp().isEmpty())
            grid.add(UIFactory.crearDatoDetalle(idioma.get("provider.detail.curp"), prestador.getCurp()), 0, 3);
        if (prestador.getRfcPrestador() != null && !prestador.getRfcPrestador().isEmpty())
            grid.add(UIFactory.crearDatoDetalle(idioma.get("provider.detail.rfc"), prestador.getRfcPrestador()), 1, 3);

        Label lblServicios = new Label(idioma.get("provider.detail.services"));
        lblServicios.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-padding: 10 0 5 0;");

        VBox listaServicios = new VBox(5);
        for (Servicio s : prestador.getServicios()) {
            Label ls = new Label("• " + s.getDescripcionServicio() + " - " + formatearPrecioInteligente(s.getCostoServicio(), s.getMonedaServicio()));
            ls.setStyle("-fx-text-fill: #4B5563;");
            listaServicios.getChildren().add(ls);
        }

        ScrollPane scrollServicios = new ScrollPane(listaServicios);
        scrollServicios.setMaxHeight(100);
        scrollServicios.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Button btnCerrar = UIFactory.crearBotonSecundario(idioma.get("provider.btn.close"));
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, lblServicios, scrollServicios, new Separator(), footer);
        dialogService.mostrarDialogoModal(dialog, root, stage);
    }

    /**
     * Valida los campos de descripción y costo del servicio, y si son correctos, crea un nuevo objeto Servicio con la información ingresada y lo agrega a la lista observable de servicios asociados al prestador. Luego limpia los campos de entrada para permitir agregar otro servicio fácilmente. Si los campos no son válidos, muestra un mensaje de alerta indicando el error
     */
    private void agregarServicioALista() {
        if (txtDescServicio.getText() == null || txtDescServicio.getText().trim().isEmpty() || 
            txtCostoServicio.getText() == null || txtCostoServicio.getText().trim().isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("provider.msg.incomplete.title"), idioma.get("provider.msg.incomplete.content"), stage);
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
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("provider.msg.error.title"), idioma.get("provider.msg.numeric_cost"), stage);
        }
    }

    /**
     * Calcula la equivalencia del monto ingresado en el campo de costo del servicio a la moneda preferida del sistema, utilizando el servicio de moneda para obtener el tipo de cambio actual. Si la moneda seleccionada es la misma que la preferida, no muestra ninguna equivalencia. Si el tipo de cambio no está disponible, asume un valor por defecto para evitar errores. El resultado se muestra en una etiqueta debajo del campo de costo para que el usuario tenga una referencia rápida del precio en su moneda preferida
     * @param inputMonto
     * @param comboMoneda
     * @param labelDestino
     */
    private void calcularEquivalenciaGeneral(TextField inputMonto, ComboBox<String> comboMoneda, Label labelDestino) {
        try {
            String textVal = inputMonto.getText().trim();
            if (textVal.isEmpty()) {
                labelDestino.setText("");
                return;
            }
            double val = Double.parseDouble(textVal);
            String monedaSeleccionada = comboMoneda.getValue();
            String monedaPreferida = monedaService.getMonedaPorDefecto();

            if (monedaSeleccionada.equals(monedaPreferida)) {
                labelDestino.setText("");
                return;
            }

            double tipoCambio = monedaService.convertirAMxn(1.0, "USD");
            if (tipoCambio == 0) tipoCambio = 20.0;

            if (monedaPreferida.equals("MXN")) {
                double enPesos = val * tipoCambio;
                labelDestino.setText(String.format("≈ $%.2f MXN", enPesos));
            } else {
                double enDolares = val / tipoCambio;
                labelDestino.setText(String.format("≈ $%.2f USD", enDolares));
            }
        } catch (Exception e) {
            labelDestino.setText("");
        }
    }

    /**
     * Formatea el precio del servicio mostrando el monto original con su moneda, y si la moneda es diferente a la preferida del sistema, también muestra una equivalencia aproximada en la moneda preferida entre paréntesis. Utiliza el servicio de moneda para obtener el tipo de cambio actual y realizar la conversión. Si el tipo de cambio no está disponible, muestra solo el precio original sin equivalencia
     * @param precio
     * @param monedaItem
     * @return
     */
    private String formatearPrecioInteligente(double precio, String monedaItem) {
        String monedaPref = monedaService.getMonedaPorDefecto();
        String textoOriginal = String.format("$%.2f %s", precio, monedaItem);

        if (monedaItem.equalsIgnoreCase(monedaPref)) {
            return textoOriginal;
        }
        try {
            double tipoCambio = monedaService.convertirAMxn(1.0, "USD");
            if (tipoCambio == 0) tipoCambio = 20.0;

            double precioConvertido;
            String monedaDestino;

            if (monedaPref.equalsIgnoreCase("MXN")) {
                precioConvertido = precio * tipoCambio;
                monedaDestino = "MXN";
            } else {
                precioConvertido = precio / tipoCambio;
                monedaDestino = "USD";
            }
            return String.format("%s (≈ $%.2f %s)", textoOriginal, precioConvertido, monedaDestino);
        } catch (Exception e) {
            return textoOriginal;
        }
    }
}