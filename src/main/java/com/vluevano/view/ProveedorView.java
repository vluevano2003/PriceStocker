package com.vluevano.view;

import java.util.List;

import com.vluevano.model.Proveedor;
import com.vluevano.service.ProveedorService;
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
public class ProveedorView extends BaseDirectorioView<Proveedor> {

    @Autowired
    private ProveedorService proveedorService;

    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo;
    private TextField txtCp, txtNoExt, txtNoInt, txtCalle, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private TextField txtCurp;
    private ComboBox<String> cmbTipoPersona;

    @Override
    protected String getTituloVentana() {
        return idioma.get("supplier.window.title");
    }

    @Override
    protected String getTituloHeader() {
        return idioma.get("supplier.header.title");
    }

    @Override
    protected String getSubtituloHeader() {
        return idioma.get("supplier.header.subtitle");
    }

    @Override
    protected String getPromptBusqueda() {
        return idioma.get("supplier.search.prompt");
    }

    @Override
    protected String getTituloFormularioNuevo() {
        return idioma.get("supplier.form.new");
    }

    @Override
    protected void cargarDatos() {
        tablaDatos.getItems().setAll(proveedorService.consultarProveedores());
    }

    @Override
    protected void buscarDatos(String valor) {
        tablaDatos.getItems().setAll(proveedorService.buscarProveedores(valor));
    }

    /**
     * Construye el formulario de registro/edición para proveedores, organizando los campos en secciones claras (personal, dirección, fiscal) y aplicando estilos consistentes
     */
    @Override
    protected VBox construirCamposFormulario() {
        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput(idioma.get("supplier.placeholder.name"));
        txtTelefono = UIFactory.crearInput(idioma.get("supplier.placeholder.phone"));
        txtCorreo = UIFactory.crearInput("ventas@proveedor.com");
        txtCalle = UIFactory.crearInput(idioma.get("supplier.placeholder.street"));
        txtNoExt = UIFactory.crearInput("Ej. 505");
        txtNoInt = UIFactory.crearInput(idioma.get("supplier.placeholder.int"));
        txtCp = UIFactory.crearInput("Ej. 44100");
        txtColonia = UIFactory.crearInput(idioma.get("supplier.placeholder.colonia"));
        txtCiudad = UIFactory.crearInput(idioma.get("supplier.placeholder.city"));
        txtMunicipio = UIFactory.crearInput(idioma.get("supplier.placeholder.mun"));
        txtEstado = UIFactory.crearInput(idioma.get("supplier.placeholder.state"));
        txtPais = UIFactory.crearInput(idioma.get("supplier.placeholder.country"));
        txtRfc = UIFactory.crearInput("Ej. PRO800101A12");
        txtCurp = UIFactory.crearInput("Ej. ABCD800101HDFRXX01");

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll(idioma.get("supplier.person.moral"), idioma.get("supplier.person.fisica"));
        cmbTipoPersona.getSelectionModel().select(0);
        cmbTipoPersona.setMaxWidth(Double.MAX_VALUE);
        cmbTipoPersona.setStyle(AppTheme.STYLE_INPUT);

        inputsContainer.getChildren().addAll(
                UIFactory.crearTituloSeccion(idioma.get("supplier.section.personal")),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.name"), txtNombre),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.phone"), txtTelefono),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.email"), txtCorreo),

                UIFactory.crearTituloSeccion(idioma.get("supplier.section.address")),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.street"), txtCalle),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("supplier.lbl.noext"), txtNoExt),
                        UIFactory.crearGrupoInput(idioma.get("supplier.lbl.noint"), txtNoInt),
                        UIFactory.crearGrupoInput(idioma.get("supplier.lbl.cp"), txtCp)),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.colonia"), txtColonia),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.city"), txtCiudad),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.mun"), txtMunicipio),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("supplier.lbl.state"), txtEstado),
                        UIFactory.crearGrupoInput(idioma.get("supplier.lbl.country"), txtPais)),

                UIFactory.crearTituloSeccion(idioma.get("supplier.section.fiscal")),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.person_type"), cmbTipoPersona),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.rfc"), txtRfc),
                UIFactory.crearGrupoInput(idioma.get("supplier.lbl.curp"), txtCurp)
        );

        return inputsContainer;
    }

    /**
     * Configura las columnas de la tabla de proveedores, incluyendo una columna de acciones con botones de editar y eliminar, y un mensaje personalizado para cuando no hay datos que mostrar
     */
    @Override
    protected void configurarColumnasTabla() {
        Label lblVacio = new Label(idioma.get("supplier.table.empty"));
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaDatos.setPlaceholder(lblVacio);

        TableColumn<Proveedor, String> colId = UIFactory.crearColumna(idioma.get("supplier.col.id"), p -> String.valueOf(p.getIdProveedor()), 40);
        colId.setMaxWidth(40);
        
        TableColumn<Proveedor, String> colNombre = UIFactory.crearColumna(idioma.get("supplier.col.name"), Proveedor::getNombreProv, 150);
        TableColumn<Proveedor, String> colTel = UIFactory.crearColumna(idioma.get("supplier.col.phone"), Proveedor::getTelefonoProv, 100);
        TableColumn<Proveedor, String> colCorreo = UIFactory.crearColumna(idioma.get("supplier.col.email"), Proveedor::getCorreoProv, 150);
        
        TableColumn<Proveedor, String> colDireccion = UIFactory.crearColumna(idioma.get("supplier.col.address"), 
            p -> String.format("%s #%d, %s", p.getCalle(), p.getNoExtProv(), p.getColonia()), 0);

        TableColumn<Proveedor, Void> colAcciones = new TableColumn<>(idioma.get("supplier.col.actions"));
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

        tablaDatos.getColumns().addAll(List.of(colId, colNombre, colTel, colCorreo, colDireccion, colAcciones));
    }

    /**
     * Limpia los campos específicos del formulario de proveedor, restableciendo los valores a su estado inicial y seleccionando la opción predeterminada en el combo de tipo de persona
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
     * Valida los campos del formulario de proveedor antes de guardar, asegurándose de que los campos obligatorios estén completos y que el formato de correo electrónico y teléfono sea correcto. Si la validación es exitosa, crea o actualiza un objeto Proveedor con los datos ingresados y llama al servicio para guardarlo, mostrando mensajes de éxito o error según corresponda
     */
    @Override
    protected void registrarEntidad() {
        if (ValidationUtils.esVacio(txtNombre) || ValidationUtils.esVacio(txtTelefono) || ValidationUtils.esVacio(txtCorreo)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("supplier.msg.missing.title"), idioma.get("supplier.msg.missing.content"), stage);
            return;
        }

        if (!ValidationUtils.esEmailValido(txtCorreo.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("supplier.msg.invalid.title"), idioma.get("supplier.msg.invalid.email"), stage);
            return;
        }
        if (!ValidationUtils.esTelefonoValido(txtTelefono.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("supplier.msg.invalid.title"), idioma.get("supplier.msg.invalid.phone"), stage);
            return;
        }

        Proveedor prov = (entidadEnEdicion != null) ? entidadEnEdicion : new Proveedor();

        prov.setNombreProv(txtNombre.getText().trim());
        prov.setTelefonoProv(txtTelefono.getText().trim());
        prov.setCorreoProv(txtCorreo.getText().trim());
        prov.setRfcProveedor(ValidationUtils.esVacio(txtRfc) ? null : txtRfc.getText().trim().toUpperCase());
        prov.setCurp(ValidationUtils.esVacio(txtCurp) ? null : txtCurp.getText().trim().toUpperCase());

        try {
            String cpStr = txtCp.getText().trim();
            prov.setCpProveedor(cpStr.isEmpty() ? 0 : Integer.parseInt(cpStr));

            String noExtStr = txtNoExt.getText().trim();
            prov.setNoExtProv(noExtStr.isEmpty() ? 0 : Integer.parseInt(noExtStr));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("supplier.msg.numeric.title"), idioma.get("supplier.msg.numeric.content"), stage);
            return;
        }

        prov.setNoIntProv(ValidationUtils.esVacio(txtNoInt) ? null : txtNoInt.getText().trim());
        prov.setCalle(txtCalle.getText().trim());
        prov.setColonia(txtColonia.getText().trim());
        prov.setCiudad(txtCiudad.getText().trim());
        prov.setMunicipio(txtMunicipio.getText().trim());
        prov.setEstado(txtEstado.getText().trim());
        prov.setPais(txtPais.getText().trim());
        prov.setEsPersonaFisica(cmbTipoPersona.getValue() != null && cmbTipoPersona.getValue().equals(idioma.get("supplier.person.fisica")));

        String res = proveedorService.guardarProveedor(prov);

        if (res.contains("exitosamente") || res.contains("guardad") || res.contains("successfully") || res.contains("saved")) {
            String accion = (entidadEnEdicion != null) ? idioma.get("supplier.msg.success.updated") : idioma.get("supplier.msg.success.saved");
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("supplier.msg.success.title"), accion, stage);
            limpiarFormulario();
            cargarDatos();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("supplier.msg.error.title"), res, stage);
        }
    }

    /**
     * Prepara el formulario para editar un proveedor existente, cargando los datos del proveedor seleccionado en los campos correspondientes y cambiando el título y el texto del botón de guardar para reflejar que se está editando en lugar de creando un nuevo proveedor. También cambia el estilo del botón para indicar que se trata de una acción de actualización
     * @param p
     */
    private void prepararEdicion(Proveedor p) {
        this.entidadEnEdicion = p;
        lblTituloFormulario.setText(idioma.get("supplier.form.edit", p.getIdProveedor()));
        btnGuardar.setText(idioma.get("supplier.btn.update"));

        String styleBlue = "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        btnGuardar.setStyle(styleBlue);
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

        cmbTipoPersona.getSelectionModel().select(p.isEsPersonaFisica() ? idioma.get("supplier.person.fisica") : idioma.get("supplier.person.moral"));
    }

    /**
     * Elimina un proveedor seleccionado, mostrando una confirmación antes de realizar la acción y un mensaje de éxito o error después. Si el proveedor que se está eliminando es el mismo que se tiene cargado en el formulario de edición, limpia el formulario para evitar mostrar datos que ya no existen
     * @param p
     */
    private void eliminarEntidad(Proveedor p) {
        if (entidadEnEdicion != null && entidadEnEdicion.getIdProveedor().equals(p.getIdProveedor())) {
            limpiarFormulario();
        }
        if (dialogService.mostrarConfirmacion(idioma.get("supplier.msg.delete.title"), idioma.get("supplier.msg.delete.confirm", p.getNombreProv()), stage)) {
            if (proveedorService.eliminarProveedor(p)) {
                cargarDatos();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("supplier.msg.delete.title"), idioma.get("supplier.msg.delete.success"), stage);
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("supplier.msg.error.title"), idioma.get("supplier.msg.delete.error"), stage);
            }
        }
    }

    /**
     * Muestra un diálogo con los detalles completos de un proveedor seleccionado, organizando la información en secciones claras y aplicando estilos para resaltar los datos más importantes. El diálogo incluye un botón para cerrar y volver a la vista principal
     */
    @Override
    protected void mostrarDetalle(Proveedor p) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(500);
        root.setMaxWidth(500);

        Label lblTitulo = new Label(p.getNombreProv());
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(p.isEsPersonaFisica() ? idioma.get("supplier.person.fisica") : idioma.get("supplier.person.moral"));
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(10);

        grid.add(UIFactory.crearDatoDetalle(idioma.get("supplier.detail.phone"), p.getTelefonoProv()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("supplier.detail.email"), p.getCorreoProv()), 1, 0);

        String direccion = String.format("%s #%d%s", p.getCalle(), p.getNoExtProv(), (p.getNoIntProv() != null && !p.getNoIntProv().isEmpty() ? " Int " + p.getNoIntProv() : ""));
        grid.add(UIFactory.crearDatoDetalle(idioma.get("supplier.detail.address"), direccion), 0, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("supplier.detail.colonia_cp"), p.getColonia() + " C.P. " + p.getCpProveedor()), 1, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("supplier.detail.city_mun"), p.getCiudad() + ", " + p.getMunicipio()), 0, 2);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("supplier.detail.state_country"), p.getEstado() + ", " + p.getPais()), 1, 2);

        if (p.getCurp() != null && !p.getCurp().isEmpty()) grid.add(UIFactory.crearDatoDetalle(idioma.get("supplier.detail.curp"), p.getCurp()), 0, 3);
        if (p.getRfcProveedor() != null && !p.getRfcProveedor().isEmpty()) grid.add(UIFactory.crearDatoDetalle(idioma.get("supplier.detail.rfc"), p.getRfcProveedor()), 1, 3);

        Button btnCerrar = UIFactory.crearBotonSecundario(idioma.get("supplier.btn.close"));
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);
        dialogService.mostrarDialogoModal(dialog, root, stage);
    }
}