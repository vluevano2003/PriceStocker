package com.vluevano.view;

import com.vluevano.model.Cliente;
import com.vluevano.service.ClienteService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import com.vluevano.util.ValidationUtils;
import com.vluevano.view.base.BaseDirectorioView;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClienteView extends BaseDirectorioView<Cliente> {

    @Autowired
    private ClienteService clienteService;

    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo;
    private TextField txtCp, txtNoExt, txtNoInt, txtCalle, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private TextField txtCurp;
    private ComboBox<String> cmbTipoPersona;

    @Override
    protected String getTituloVentana() {
        return idioma.get("client.window.title");
    }

    @Override
    protected String getTituloHeader() {
        return idioma.get("client.header.title");
    }

    @Override
    protected String getSubtituloHeader() {
        return idioma.get("client.header.subtitle");
    }

    @Override
    protected String getPromptBusqueda() {
        return idioma.get("client.search.prompt");
    }

    @Override
    protected String getTituloFormularioNuevo() {
        return idioma.get("client.form.new");
    }

    @Override
    protected void cargarDatos() {
        tablaDatos.getItems().setAll(clienteService.consultarClientes());
    }

    @Override
    protected void buscarDatos(String valor) {
        tablaDatos.getItems().setAll(clienteService.buscarClientes(valor));
    }

    /**
     * Construye el formulario de cliente con campos específicos para nombre, teléfono, correo, dirección, RFC, CURP y tipo de persona
     */
    @Override
    protected VBox construirCamposFormulario() {
        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput(idioma.get("client.placeholder.name"));
        txtTelefono = UIFactory.crearInput(idioma.get("client.placeholder.phone"));
        txtCorreo = UIFactory.crearInput("cliente@empresa.com");
        txtCalle = UIFactory.crearInput(idioma.get("client.placeholder.street"));
        txtNoExt = UIFactory.crearInput("Ej. 123");
        txtNoInt = UIFactory.crearInput(idioma.get("client.placeholder.int"));
        txtCp = UIFactory.crearInput("Ej. 06600");
        txtColonia = UIFactory.crearInput(idioma.get("client.placeholder.colonia"));
        txtCiudad = UIFactory.crearInput(idioma.get("client.placeholder.city"));
        txtMunicipio = UIFactory.crearInput(idioma.get("client.placeholder.mun"));
        txtEstado = UIFactory.crearInput(idioma.get("client.placeholder.state"));
        txtPais = UIFactory.crearInput(idioma.get("client.placeholder.country"));
        txtRfc = UIFactory.crearInput("Ej. XAXX010101000");
        txtCurp = UIFactory.crearInput("Ej. SAMA950520MDFRXX03");

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll(idioma.get("client.person.moral"), idioma.get("client.person.fisica"));
        cmbTipoPersona.getSelectionModel().select(0);
        cmbTipoPersona.setMaxWidth(Double.MAX_VALUE);
        cmbTipoPersona.setStyle(AppTheme.STYLE_INPUT);

        inputsContainer.getChildren().addAll(
                UIFactory.crearTituloSeccion(idioma.get("client.section.personal")),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.name"), txtNombre),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.phone"), txtTelefono),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.email"), txtCorreo),

                UIFactory.crearTituloSeccion(idioma.get("client.section.address")),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.street"), txtCalle),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("client.lbl.noext"), txtNoExt),
                        UIFactory.crearGrupoInput(idioma.get("client.lbl.noint"), txtNoInt),
                        UIFactory.crearGrupoInput(idioma.get("client.lbl.cp"), txtCp)),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.colonia"), txtColonia),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.city"), txtCiudad),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.mun"), txtMunicipio),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("client.lbl.state"), txtEstado),
                        UIFactory.crearGrupoInput(idioma.get("client.lbl.country"), txtPais)),

                UIFactory.crearTituloSeccion(idioma.get("client.section.fiscal")),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.person_type"), cmbTipoPersona),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.rfc"), txtRfc),
                UIFactory.crearGrupoInput(idioma.get("client.lbl.curp"), txtCurp)
        );

        return inputsContainer;
    }

    /**
     * Configura las columnas de la tabla para mostrar ID, nombre, teléfono, correo, dirección y acciones (editar/eliminar) con estilos personalizados
     */
    @Override
    protected void configurarColumnasTabla() {
        Label lblVacio = new Label(idioma.get("client.table.empty"));
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaDatos.setPlaceholder(lblVacio);

        TableColumn<Cliente, String> colId = UIFactory.crearColumna(idioma.get("client.col.id"), c -> String.valueOf(c.getIdCliente()), 40);
        colId.setMaxWidth(40);
        
        TableColumn<Cliente, String> colNombre = UIFactory.crearColumna(idioma.get("client.col.name"), Cliente::getNombreCliente, 150);
        TableColumn<Cliente, String> colTel = UIFactory.crearColumna(idioma.get("client.col.phone"), Cliente::getTelefonoCliente, 100);
        TableColumn<Cliente, String> colCorreo = UIFactory.crearColumna(idioma.get("client.col.email"), Cliente::getCorreoCliente, 150);
        
        TableColumn<Cliente, String> colDireccion = UIFactory.crearColumna(idioma.get("client.col.address"), 
            c -> String.format("%s #%d, %s", c.getCalle(), c.getNoExtCliente(), c.getColonia()), 0);

        TableColumn<Cliente, Void> colAcciones = new TableColumn<>(idioma.get("client.col.actions"));
        colAcciones.setMinWidth(140);
        colAcciones.setMaxWidth(140);
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

        tablaDatos.getColumns().addAll(java.util.List.of(colId, colNombre, colTel, colCorreo, colDireccion, colAcciones));
    }

    /**
     * Limpia los campos del formulario específicos de Cliente para preparar el formulario para una nueva entrada o después de guardar/actualizar
     */
    @Override
    protected void limpiarCamposEspecificos() {
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
    }

    /**
     * Valida los campos del formulario y guarda o actualiza el cliente utilizando el servicio. Muestra mensajes de éxito o error según corresponda
     */
    @Override
    protected void registrarEntidad() {
        if (ValidationUtils.esVacio(txtNombre) || ValidationUtils.esVacio(txtTelefono) || ValidationUtils.esVacio(txtCorreo)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("client.msg.missing.title"), idioma.get("client.msg.missing.content"), stage);
            return;
        }

        if (!ValidationUtils.esEmailValido(txtCorreo.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("client.msg.invalid.title"), idioma.get("client.msg.invalid.email"), stage);
            return;
        }
        if (!ValidationUtils.esTelefonoValido(txtTelefono.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("client.msg.invalid.title"), idioma.get("client.msg.invalid.phone"), stage);
            return;
        }

        Cliente cliente = (entidadEnEdicion != null) ? entidadEnEdicion : new Cliente();

        cliente.setNombreCliente(txtNombre.getText().trim());
        cliente.setTelefonoCliente(txtTelefono.getText().trim());
        cliente.setCorreoCliente(txtCorreo.getText().trim());
        cliente.setRfcCliente(ValidationUtils.esVacio(txtRfc) ? null : txtRfc.getText().trim().toUpperCase());
        cliente.setCurp(ValidationUtils.esVacio(txtCurp) ? null : txtCurp.getText().trim().toUpperCase());

        try {
            String cpStr = txtCp.getText().trim();
            cliente.setCpCliente(cpStr.isEmpty() ? 0 : Integer.parseInt(cpStr));

            String noExtStr = txtNoExt.getText().trim();
            cliente.setNoExtCliente(noExtStr.isEmpty() ? 0 : Integer.parseInt(noExtStr));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("client.msg.numeric.title"), idioma.get("client.msg.numeric.content"), stage);
            return;
        }

        cliente.setNoIntCliente(ValidationUtils.esVacio(txtNoInt) ? null : txtNoInt.getText().trim());
        cliente.setCalle(txtCalle.getText().trim());
        cliente.setColonia(txtColonia.getText().trim());
        cliente.setCiudad(txtCiudad.getText().trim());
        cliente.setMunicipio(txtMunicipio.getText().trim());
        cliente.setEstado(txtEstado.getText().trim());
        cliente.setPais(txtPais.getText().trim());
        cliente.setEsPersonaFisica(cmbTipoPersona.getValue() != null && cmbTipoPersona.getValue().equals(idioma.get("client.person.fisica")));

        String res = clienteService.guardarCliente(cliente);

        if (res.contains("exitosamente") || res.contains("guardad") || res.contains("successfully") || res.contains("saved")) {
            String msgSuccess = (entidadEnEdicion != null) ? idioma.get("client.msg.success.updated") : idioma.get("client.msg.success.saved");
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("client.msg.success.title"), msgSuccess, stage);
            limpiarFormulario();
            cargarDatos();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("client.msg.error.title"), res, stage);
        }
    }
    

    /**
     * Prepara el formulario para editar un cliente existente, llenando los campos con los datos del cliente seleccionado y cambiando el título y el texto del botón de guardar para reflejar que se está editando en lugar de creando un nuevo cliente
     * @param cliente
     */
    private void prepararEdicion(Cliente cliente) {
        this.entidadEnEdicion = cliente;
        lblTituloFormulario.setText(idioma.get("client.form.edit", cliente.getIdCliente()));
        btnGuardar.setText(idioma.get("client.btn.update"));

        String styleBlue = "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        btnGuardar.setStyle(styleBlue);
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(styleBlue));

        txtNombre.setText(cliente.getNombreCliente());
        txtTelefono.setText(cliente.getTelefonoCliente());
        txtCorreo.setText(cliente.getCorreoCliente());
        txtCalle.setText(cliente.getCalle());
        txtNoExt.setText(String.valueOf(cliente.getNoExtCliente()));
        txtNoInt.setText(cliente.getNoIntCliente() == null ? "" : cliente.getNoIntCliente());
        txtCp.setText(String.valueOf(cliente.getCpCliente()));
        txtColonia.setText(cliente.getColonia());
        txtCiudad.setText(cliente.getCiudad());
        txtMunicipio.setText(cliente.getMunicipio());
        txtEstado.setText(cliente.getEstado());
        txtPais.setText(cliente.getPais());
        txtRfc.setText(cliente.getRfcCliente() == null ? "" : cliente.getRfcCliente());
        txtCurp.setText(cliente.getCurp() == null ? "" : cliente.getCurp());

        cmbTipoPersona.getSelectionModel().select(cliente.isEsPersonaFisica() ? idioma.get("client.person.fisica") : idioma.get("client.person.moral"));
    }

    /**
     * Elimina un cliente después de confirmar la acción con el usuario. Si el cliente que se está editando es el mismo que se va a eliminar, limpia el formulario para evitar mostrar datos de un cliente que ya no existe. Muestra mensajes de éxito o error según corresponda
     * @param c
     */
    private void eliminarEntidad(Cliente c) {
        if (entidadEnEdicion != null && entidadEnEdicion.getIdCliente().equals(c.getIdCliente())) {
            limpiarFormulario();
        }
        if (dialogService.mostrarConfirmacion(idioma.get("client.msg.delete.title"), idioma.get("client.msg.delete.confirm", c.getNombreCliente()), stage)) {
            if (clienteService.eliminarCliente(c)) {
                cargarDatos();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("client.msg.delete.title"), idioma.get("client.msg.delete.success"), stage);
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("client.msg.error.title"), idioma.get("client.msg.delete.error"), stage);
            }
        }
    }

    /**
     * Muestra un diálogo modal con los detalles completos de un cliente seleccionado, incluyendo su nombre, tipo de persona, teléfono, correo, dirección completa, RFC y CURP. El diseño del diálogo es limpio y organizado, con secciones claramente diferenciadas y un botón para cerrar el diálogo
     */
    @Override
    protected void mostrarDetalle(Cliente c) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(500);
        root.setMaxWidth(500);

        Label lblTitulo = new Label(c.getNombreCliente());
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(c.isEsPersonaFisica() ? idioma.get("client.person.fisica") : idioma.get("client.person.moral"));
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10);

        grid.add(UIFactory.crearDatoDetalle(idioma.get("client.detail.phone"), c.getTelefonoCliente()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("client.detail.email"), c.getCorreoCliente()), 1, 0);

        String direccion = String.format("%s #%d%s", c.getCalle(), c.getNoExtCliente(), (c.getNoIntCliente() != null && !c.getNoIntCliente().isEmpty() ? " Int " + c.getNoIntCliente() : ""));
        grid.add(UIFactory.crearDatoDetalle(idioma.get("client.detail.address"), direccion), 0, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("client.detail.colonia_cp"), c.getColonia() + " C.P. " + c.getCpCliente()), 1, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("client.detail.city_mun"), c.getCiudad() + ", " + c.getMunicipio()), 0, 2);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("client.detail.state_country"), c.getEstado() + ", " + c.getPais()), 1, 2);

        if (c.getCurp() != null && !c.getCurp().isEmpty()) grid.add(UIFactory.crearDatoDetalle(idioma.get("client.detail.curp"), c.getCurp()), 0, 3);
        if (c.getRfcCliente() != null && !c.getRfcCliente().isEmpty()) grid.add(UIFactory.crearDatoDetalle(idioma.get("client.detail.rfc"), c.getRfcCliente()), 1, 3);

        Button btnCerrar = UIFactory.crearBotonSecundario(idioma.get("client.btn.close"));
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);
        dialogService.mostrarDialogoModal(dialog, root, stage);
    }
}