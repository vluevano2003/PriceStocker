package com.vluevano.view;

import java.util.List;

import com.vluevano.model.Empresa;
import com.vluevano.service.EmpresaService;
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
public class EmpresaView extends BaseDirectorioView<Empresa> {

    @Autowired
    private EmpresaService empresaService;

    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo;
    private TextField txtCp, txtNoExt, txtNoInt, txtCalle, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private TextField txtCurp;
    private ComboBox<String> cmbTipoPersona;

    @Override
    protected String getTituloVentana() {
        return idioma.get("company.window.title");
    }

    @Override
    protected String getTituloHeader() {
        return idioma.get("company.header.title");
    }

    @Override
    protected String getSubtituloHeader() {
        return idioma.get("company.header.subtitle");
    }

    @Override
    protected String getPromptBusqueda() {
        return idioma.get("company.search.prompt");
    }

    @Override
    protected String getTituloFormularioNuevo() {
        return idioma.get("company.form.new");
    }

    @Override
    protected void cargarDatos() {
        tablaDatos.getItems().setAll(empresaService.consultarEmpresas());
    }

    @Override
    protected void buscarDatos(String valor) {
        tablaDatos.getItems().setAll(empresaService.buscarEmpresas(valor));
    }

    /**
     * Construye el formulario de registro/edición para una empresa, organizando los campos en secciones claras (General, Dirección, Fiscal) y aplicando estilos consistentes
     */
    @Override
    protected VBox construirCamposFormulario() {
        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput(idioma.get("company.placeholder.name"));
        txtTelefono = UIFactory.crearInput(idioma.get("company.placeholder.phone"));
        txtCorreo = UIFactory.crearInput("info@competidor.com");
        txtCalle = UIFactory.crearInput(idioma.get("company.placeholder.street"));
        txtNoExt = UIFactory.crearInput("Ej. 1500");
        txtNoInt = UIFactory.crearInput(idioma.get("company.placeholder.int"));
        txtCp = UIFactory.crearInput("Ej. 03200");
        txtColonia = UIFactory.crearInput(idioma.get("company.placeholder.colonia"));
        txtCiudad = UIFactory.crearInput(idioma.get("company.placeholder.city"));
        txtMunicipio = UIFactory.crearInput(idioma.get("company.placeholder.mun"));
        txtEstado = UIFactory.crearInput(idioma.get("company.placeholder.state"));
        txtPais = UIFactory.crearInput(idioma.get("company.placeholder.country"));
        txtRfc = UIFactory.crearInput("Ej. IDC900515K10");
        txtCurp = UIFactory.crearInput("Ej. ABCD900515HDFRXX09");

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll(idioma.get("company.person.moral"), idioma.get("company.person.fisica"));
        cmbTipoPersona.getSelectionModel().select(0);
        cmbTipoPersona.setMaxWidth(Double.MAX_VALUE);
        cmbTipoPersona.setStyle(AppTheme.STYLE_INPUT);

        inputsContainer.getChildren().addAll(
                UIFactory.crearTituloSeccion(idioma.get("company.section.general")),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.name"), txtNombre),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.phone"), txtTelefono),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.email"), txtCorreo),

                UIFactory.crearTituloSeccion(idioma.get("company.section.address")),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.street"), txtCalle),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("company.lbl.noext"), txtNoExt),
                        UIFactory.crearGrupoInput(idioma.get("company.lbl.noint"), txtNoInt),
                        UIFactory.crearGrupoInput(idioma.get("company.lbl.cp"), txtCp)),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.colonia"), txtColonia),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.city"), txtCiudad),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.mun"), txtMunicipio),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("company.lbl.state"), txtEstado),
                        UIFactory.crearGrupoInput(idioma.get("company.lbl.country"), txtPais)),

                UIFactory.crearTituloSeccion(idioma.get("company.section.fiscal")),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.person_type"), cmbTipoPersona),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.rfc"), txtRfc),
                UIFactory.crearGrupoInput(idioma.get("company.lbl.curp"), txtCurp)
        );

        return inputsContainer;
    }

    /**
     * Configura las columnas de la tabla de empresas, incluyendo una columna de acciones con botones de editar y eliminar, y un mensaje personalizado para cuando no hay datos que mostrar
     */
    @Override
    protected void configurarColumnasTabla() {
        Label lblVacio = new Label(idioma.get("company.table.empty"));
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaDatos.setPlaceholder(lblVacio);

        TableColumn<Empresa, String> colId = UIFactory.crearColumna(idioma.get("company.col.id"), e -> String.valueOf(e.getIdEmpresa()), 40);
        colId.setMaxWidth(40);
        
        TableColumn<Empresa, String> colNombre = UIFactory.crearColumna(idioma.get("company.col.name"), Empresa::getNombreEmpresa, 150);
        TableColumn<Empresa, String> colTel = UIFactory.crearColumna(idioma.get("company.col.phone"), Empresa::getTelefonoEmpresa, 100);
        TableColumn<Empresa, String> colCorreo = UIFactory.crearColumna(idioma.get("company.col.email"), Empresa::getCorreoEmpresa, 150);
        
        TableColumn<Empresa, String> colDireccion = UIFactory.crearColumna(idioma.get("company.col.address"), 
            e -> String.format("%s #%d, %s", e.getCalle(), e.getNoExtEmpresa(), e.getColonia()), 0);

        TableColumn<Empresa, Void> colAcciones = new TableColumn<>(idioma.get("company.col.actions"));
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
     * Limpia los campos del formulario de empresa, restableciendo los valores a su estado inicial y seleccionando el tipo de persona moral por defecto
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
     * Valida los campos del formulario de empresa y, si son correctos, construye un objeto Empresa con los datos ingresados y lo guarda a través del servicio correspondiente, mostrando mensajes de éxito o error según el resultado
     */
    @Override
    protected void registrarEntidad() {
        if (ValidationUtils.esVacio(txtNombre) || ValidationUtils.esVacio(txtTelefono) || ValidationUtils.esVacio(txtCorreo)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("company.msg.missing.title"), idioma.get("company.msg.missing.content"), stage);
            return;
        }

        if (!ValidationUtils.esEmailValido(txtCorreo.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("company.msg.invalid.title"), idioma.get("company.msg.invalid.email"), stage);
            return;
        }
        if (!ValidationUtils.esTelefonoValido(txtTelefono.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("company.msg.invalid.title"), idioma.get("company.msg.invalid.phone"), stage);
            return;
        }

        Empresa emp = (entidadEnEdicion != null) ? entidadEnEdicion : new Empresa();

        emp.setNombreEmpresa(txtNombre.getText().trim());
        emp.setTelefonoEmpresa(txtTelefono.getText().trim());
        emp.setCorreoEmpresa(txtCorreo.getText().trim());
        emp.setRfcEmpresa(ValidationUtils.esVacio(txtRfc) ? null : txtRfc.getText().trim().toUpperCase());
        emp.setCurp(ValidationUtils.esVacio(txtCurp) ? null : txtCurp.getText().trim().toUpperCase());

        try {
            String cpStr = txtCp.getText().trim();
            emp.setCpEmpresa(cpStr.isEmpty() ? 0 : Integer.parseInt(cpStr));

            String noExtStr = txtNoExt.getText().trim();
            emp.setNoExtEmpresa(noExtStr.isEmpty() ? 0 : Integer.parseInt(noExtStr));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("company.msg.numeric.title"), idioma.get("company.msg.numeric.content"), stage);
            return;
        }

        emp.setNoIntEmpresa(ValidationUtils.esVacio(txtNoInt) ? null : txtNoInt.getText().trim());
        emp.setCalle(txtCalle.getText().trim());
        emp.setColonia(txtColonia.getText().trim());
        emp.setCiudad(txtCiudad.getText().trim());
        emp.setMunicipio(txtMunicipio.getText().trim());
        emp.setEstado(txtEstado.getText().trim());
        emp.setPais(txtPais.getText().trim());
        emp.setEsPersonaFisica(cmbTipoPersona.getValue() != null && cmbTipoPersona.getValue().equals(idioma.get("company.person.fisica")));

        String res = empresaService.guardarEmpresa(emp);

        if (res.contains("exitosamente") || res.contains("guardad") || res.contains("successfully") || res.contains("saved")) {
            String accion = (entidadEnEdicion != null) ? idioma.get("company.msg.success.updated") : idioma.get("company.msg.success.saved");
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("company.msg.success.title"), accion, stage);
            limpiarFormulario();
            cargarDatos();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("company.msg.error.title"), res, stage);
        }
    }

    /**
     * Prepara el formulario para editar una empresa existente, cargando los datos de la entidad seleccionada en los campos correspondientes y ajustando el título y el botón de acción para reflejar que se está en modo edición
     * @param emp
     */
    private void prepararEdicion(Empresa emp) {
        this.entidadEnEdicion = emp;
        lblTituloFormulario.setText(idioma.get("company.form.edit", emp.getIdEmpresa()));
        btnGuardar.setText(idioma.get("company.btn.update"));

        String styleBlue = "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        btnGuardar.setStyle(styleBlue);
        btnGuardar.setOnMouseExited(e -> btnGuardar.setStyle(styleBlue));

        txtNombre.setText(emp.getNombreEmpresa());
        txtTelefono.setText(emp.getTelefonoEmpresa());
        txtCorreo.setText(emp.getCorreoEmpresa());
        txtCalle.setText(emp.getCalle());
        txtNoExt.setText(String.valueOf(emp.getNoExtEmpresa()));
        txtNoInt.setText(emp.getNoIntEmpresa() == null ? "" : emp.getNoIntEmpresa());
        txtCp.setText(String.valueOf(emp.getCpEmpresa()));
        txtColonia.setText(emp.getColonia());
        txtCiudad.setText(emp.getCiudad());
        txtMunicipio.setText(emp.getMunicipio());
        txtEstado.setText(emp.getEstado());
        txtPais.setText(emp.getPais());
        txtRfc.setText(emp.getRfcEmpresa() == null ? "" : emp.getRfcEmpresa());
        txtCurp.setText(emp.getCurp() == null ? "" : emp.getCurp());

        cmbTipoPersona.getSelectionModel().select(emp.isEsPersonaFisica() ? idioma.get("company.person.fisica") : idioma.get("company.person.moral"));
    }

    /**
     * Elimina una empresa después de confirmar la acción con el usuario, y si la empresa que se está editando es la misma que se va a eliminar, limpia el formulario para evitar inconsistencias en la interfaz
     * @param emp
     */
    private void eliminarEntidad(Empresa emp) {
        if (entidadEnEdicion != null && entidadEnEdicion.getIdEmpresa().equals(emp.getIdEmpresa())) {
            limpiarFormulario();
        }

        if (dialogService.mostrarConfirmacion(idioma.get("company.msg.delete.title"), idioma.get("company.msg.delete.confirm", emp.getNombreEmpresa()), stage)) {
            if (empresaService.eliminarEmpresa(emp)) {
                cargarDatos();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("company.msg.delete.title"), idioma.get("company.msg.delete.success"), stage);
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("company.msg.error.title"), idioma.get("company.msg.delete.error"), stage);
            }
        }
    }
     
    /**
     * Muestra un diálogo con los detalles completos de una empresa, organizando la información en secciones claras y aplicando estilos para resaltar los datos más importantes, como el nombre de la empresa y su tipo (persona física o moral)
      * @param emp
     */
    @Override
    protected void mostrarDetalle(Empresa emp) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(500);
        root.setMaxWidth(500);

        Label lblTitulo = new Label(emp.getNombreEmpresa());
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(emp.isEsPersonaFisica() ? idioma.get("company.person.fisica") : idioma.get("company.person.moral"));
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        grid.add(UIFactory.crearDatoDetalle(idioma.get("company.detail.phone"), emp.getTelefonoEmpresa()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("company.detail.email"), emp.getCorreoEmpresa()), 1, 0);

        String direccion = String.format("%s #%d%s", emp.getCalle(), emp.getNoExtEmpresa(),
                (emp.getNoIntEmpresa() != null && !emp.getNoIntEmpresa().isEmpty() ? " Int " + emp.getNoIntEmpresa() : ""));
        grid.add(UIFactory.crearDatoDetalle(idioma.get("company.detail.address"), direccion), 0, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("company.detail.colonia_cp"), emp.getColonia() + " C.P. " + emp.getCpEmpresa()), 1, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("company.detail.city_mun"), emp.getCiudad() + ", " + emp.getMunicipio()), 0, 2);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("company.detail.state_country"), emp.getEstado() + ", " + emp.getPais()), 1, 2);

        if (emp.getCurp() != null && !emp.getCurp().isEmpty())
            grid.add(UIFactory.crearDatoDetalle(idioma.get("company.detail.curp"), emp.getCurp()), 0, 3);
        if (emp.getRfcEmpresa() != null && !emp.getRfcEmpresa().isEmpty())
            grid.add(UIFactory.crearDatoDetalle(idioma.get("company.detail.rfc"), emp.getRfcEmpresa()), 1, 3);

        Button btnCerrar = UIFactory.crearBotonSecundario(idioma.get("company.btn.close"));
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);
        dialogService.mostrarDialogoModal(dialog, root, stage);
    }
}