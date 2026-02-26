package com.vluevano.view;

import java.util.List;

import com.vluevano.model.Fabricante;
import com.vluevano.service.FabricanteService;
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
public class FabricanteView extends BaseDirectorioView<Fabricante> {

    @Autowired
    private FabricanteService fabricanteService;

    private TextField txtNombre, txtRfc, txtTelefono, txtCorreo;
    private TextField txtCp, txtNoExt, txtNoInt, txtCalle, txtColonia, txtCiudad, txtMunicipio, txtEstado, txtPais;
    private TextField txtCurp;
    private ComboBox<String> cmbTipoPersona;

    @Override
    protected String getTituloVentana() {
        return idioma.get("manufacturer.window.title");
    }

    @Override
    protected String getTituloHeader() {
        return idioma.get("manufacturer.header.title");
    }

    @Override
    protected String getSubtituloHeader() {
        return idioma.get("manufacturer.header.subtitle");
    }

    @Override
    protected String getPromptBusqueda() {
        return idioma.get("manufacturer.search.prompt");
    }

    @Override
    protected String getTituloFormularioNuevo() {
        return idioma.get("manufacturer.form.new");
    }

    @Override
    protected void cargarDatos() {
        tablaDatos.getItems().setAll(fabricanteService.consultarFabricantes());
    }

    @Override
    protected void buscarDatos(String valor) {
        tablaDatos.getItems().setAll(fabricanteService.buscarFabricantes(valor));
    }

    /**
     * Construye los campos del formulario para crear/editar un fabricante, organizados en secciones de información personal, dirección y fiscal
     */
    @Override
    protected VBox construirCamposFormulario() {
        VBox inputsContainer = new VBox(10);
        inputsContainer.setPadding(new Insets(0, 20, 20, 20));

        txtNombre = UIFactory.crearInput(idioma.get("manufacturer.placeholder.name"));
        txtTelefono = UIFactory.crearInput(idioma.get("manufacturer.placeholder.phone"));
        txtCorreo = UIFactory.crearInput("planta@fabricante.com");
        txtCalle = UIFactory.crearInput(idioma.get("manufacturer.placeholder.street"));
        txtNoExt = UIFactory.crearInput("Ej. Km 12.5");
        txtNoInt = UIFactory.crearInput(idioma.get("manufacturer.placeholder.int"));
        txtCp = UIFactory.crearInput("Ej. 66350");
        txtColonia = UIFactory.crearInput(idioma.get("manufacturer.placeholder.colonia"));
        txtCiudad = UIFactory.crearInput(idioma.get("manufacturer.placeholder.city"));
        txtMunicipio = UIFactory.crearInput(idioma.get("manufacturer.placeholder.mun"));
        txtEstado = UIFactory.crearInput(idioma.get("manufacturer.placeholder.state"));
        txtPais = UIFactory.crearInput(idioma.get("manufacturer.placeholder.country"));
        txtRfc = UIFactory.crearInput("Ej. MIN701231T12");
        txtCurp = UIFactory.crearInput("Ej. ABCD701231HNLRSX05");

        cmbTipoPersona = new ComboBox<>();
        cmbTipoPersona.getItems().addAll(idioma.get("manufacturer.person.moral"), idioma.get("manufacturer.person.fisica"));
        cmbTipoPersona.getSelectionModel().select(0);
        cmbTipoPersona.setMaxWidth(Double.MAX_VALUE);
        cmbTipoPersona.setStyle(AppTheme.STYLE_INPUT);

        inputsContainer.getChildren().addAll(
                UIFactory.crearTituloSeccion(idioma.get("manufacturer.section.personal")),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.name"), txtNombre),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.phone"), txtTelefono),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.email"), txtCorreo),

                UIFactory.crearTituloSeccion(idioma.get("manufacturer.section.address")),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.street"), txtCalle),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.noext"), txtNoExt),
                        UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.noint"), txtNoInt),
                        UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.cp"), txtCp)),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.colonia"), txtColonia),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.city"), txtCiudad),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.mun"), txtMunicipio),
                new HBox(10,
                        UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.state"), txtEstado),
                        UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.country"), txtPais)),

                UIFactory.crearTituloSeccion(idioma.get("manufacturer.section.fiscal")),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.person_type"), cmbTipoPersona),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.rfc"), txtRfc),
                UIFactory.crearGrupoInput(idioma.get("manufacturer.lbl.curp"), txtCurp)
        );

        return inputsContainer;
    }

    /**
     * Configura las columnas de la tabla de fabricantes, incluyendo una columna de acciones con botones para editar y eliminar cada fabricante
     */
    @Override
    protected void configurarColumnasTabla() {
        Label lblVacio = new Label(idioma.get("manufacturer.table.empty"));
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaDatos.setPlaceholder(lblVacio);

        TableColumn<Fabricante, String> colId = UIFactory.crearColumna(idioma.get("manufacturer.col.id"), f -> String.valueOf(f.getIdFabricante()), 40);
        colId.setMaxWidth(40);
        
        TableColumn<Fabricante, String> colNombre = UIFactory.crearColumna(idioma.get("manufacturer.col.name"), Fabricante::getNombreFabricante, 150);
        TableColumn<Fabricante, String> colTel = UIFactory.crearColumna(idioma.get("manufacturer.col.phone"), Fabricante::getTelefonoFabricante, 100);
        TableColumn<Fabricante, String> colCorreo = UIFactory.crearColumna(idioma.get("manufacturer.col.email"), Fabricante::getCorreoFabricante, 150);
        
        TableColumn<Fabricante, String> colDireccion = UIFactory.crearColumna(idioma.get("manufacturer.col.address"), 
            f -> String.format("%s #%d, %s", f.getCalle(), f.getNoExtFabricante(), f.getColonia()), 0);

        TableColumn<Fabricante, Void> colAcciones = new TableColumn<>(idioma.get("manufacturer.col.actions"));
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
     * Limpia los campos del formulario específicos de Fabricante, dejando el formulario listo para ingresar un nuevo fabricante
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
     * Valida los campos del formulario y, si son correctos, construye un objeto Fabricante con los datos ingresados y lo guarda a través del servicio. Muestra mensajes de éxito o error según corresponda
     */
    @Override
    protected void registrarEntidad() {
        if (ValidationUtils.esVacio(txtNombre) || ValidationUtils.esVacio(txtTelefono) || ValidationUtils.esVacio(txtCorreo)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("manufacturer.msg.missing.title"), idioma.get("manufacturer.msg.missing.content"), stage);
            return;
        }

        if (!ValidationUtils.esEmailValido(txtCorreo.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("manufacturer.msg.invalid.title"), idioma.get("manufacturer.msg.invalid.email"), stage);
            return;
        }
        if (!ValidationUtils.esTelefonoValido(txtTelefono.getText().trim())) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, idioma.get("manufacturer.msg.invalid.title"), idioma.get("manufacturer.msg.invalid.phone"), stage);
            return;
        }

        Fabricante f = (entidadEnEdicion != null) ? entidadEnEdicion : new Fabricante();

        f.setNombreFabricante(txtNombre.getText().trim());
        f.setTelefonoFabricante(txtTelefono.getText().trim());
        f.setCorreoFabricante(txtCorreo.getText().trim());
        f.setRfcFabricante(ValidationUtils.esVacio(txtRfc) ? null : txtRfc.getText().trim().toUpperCase());
        f.setCurp(ValidationUtils.esVacio(txtCurp) ? null : txtCurp.getText().trim().toUpperCase());

        try {
            String cpStr = txtCp.getText().trim();
            f.setCpFabricante(cpStr.isEmpty() ? 0 : Integer.parseInt(cpStr));

            String noExtStr = txtNoExt.getText().trim();
            f.setNoExtFabricante(noExtStr.isEmpty() ? 0 : Integer.parseInt(noExtStr));
        } catch (NumberFormatException e) {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("manufacturer.msg.numeric.title"), idioma.get("manufacturer.msg.numeric.content"), stage);
            return;
        }

        f.setNoIntFabricante(ValidationUtils.esVacio(txtNoInt) ? null : txtNoInt.getText().trim());
        f.setCalle(txtCalle.getText().trim());
        f.setColonia(txtColonia.getText().trim());
        f.setCiudad(txtCiudad.getText().trim());
        f.setMunicipio(txtMunicipio.getText().trim());
        f.setEstado(txtEstado.getText().trim());
        f.setPais(txtPais.getText().trim());
        f.setEsPersonaFisica(cmbTipoPersona.getValue() != null && cmbTipoPersona.getValue().equals(idioma.get("manufacturer.person.fisica")));

        String res = fabricanteService.guardarFabricante(f);

        if (res.contains("exitosamente") || res.contains("guardad") || res.contains("successfully") || res.contains("saved")) {
            String accion = (entidadEnEdicion != null) ? idioma.get("manufacturer.msg.success.updated") : idioma.get("manufacturer.msg.success.saved");
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("manufacturer.msg.success.title"), accion, stage);
            limpiarFormulario();
            cargarDatos();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("manufacturer.msg.error.title"), res, stage);
        }
    }

    /**
     * Prepara el formulario para editar un fabricante existente, cargando sus datos en los campos correspondientes y cambiando el título y el texto del botón de guardar para reflejar que se está editando en lugar de creando un nuevo fabricante
     * @param f
     */
    private void prepararEdicion(Fabricante f) {
        this.entidadEnEdicion = f;
        lblTituloFormulario.setText(idioma.get("manufacturer.form.edit", f.getIdFabricante()));
        btnGuardar.setText(idioma.get("manufacturer.btn.update"));

        String styleBlue = "-fx-background-color: #2563EB; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;";
        btnGuardar.setStyle(styleBlue);
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

        cmbTipoPersona.getSelectionModel().select(f.isEsPersonaFisica() ? idioma.get("manufacturer.person.fisica") : idioma.get("manufacturer.person.moral"));
    }

    /**
     * Elimina un fabricante después de confirmar la acción con el usuario. Si el fabricante que se está eliminando es el mismo que se tiene cargado en el formulario para edición, limpia el formulario para evitar mostrar datos de un fabricante que ya no existe. Muestra mensajes de éxito o error según corresponda
     * @param f
     */
    private void eliminarEntidad(Fabricante f) {
        if (entidadEnEdicion != null && entidadEnEdicion.getIdFabricante().equals(f.getIdFabricante())) {
            limpiarFormulario();
        }

        if (dialogService.mostrarConfirmacion(idioma.get("manufacturer.msg.delete.title"), idioma.get("manufacturer.msg.delete.confirm", f.getNombreFabricante()), stage)) {
            if (fabricanteService.eliminarFabricante(f)) {
                cargarDatos();
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("manufacturer.msg.delete.title"), idioma.get("manufacturer.msg.delete.success"), stage);
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("manufacturer.msg.error.title"), idioma.get("manufacturer.msg.delete.error"), stage);
            }
        }
    }

    /**
     * Muestra un diálogo con los detalles completos de un fabricante, incluyendo su información personal, dirección y datos fiscales. El diálogo tiene un diseño limpio y organizado, con secciones claramente diferenciadas y un botón para cerrar el diálogo
     */
    @Override
    protected void mostrarDetalle(Fabricante f) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle(AppTheme.STYLE_DIALOG_BG + " -fx-background-radius: 12;");
        root.setMinWidth(500);
        root.setMaxWidth(500);

        Label lblTitulo = new Label(f.getNombreFabricante());
        lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");
        Label lblSub = new Label(f.isEsPersonaFisica() ? idioma.get("manufacturer.person.fisica") : idioma.get("manufacturer.person.moral"));
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);

        grid.add(UIFactory.crearDatoDetalle(idioma.get("manufacturer.detail.phone"), f.getTelefonoFabricante()), 0, 0);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("manufacturer.detail.email"), f.getCorreoFabricante()), 1, 0);

        String direccion = String.format("%s #%d%s", f.getCalle(), f.getNoExtFabricante(),
                (f.getNoIntFabricante() != null && !f.getNoIntFabricante().isEmpty() ? " Int " + f.getNoIntFabricante() : ""));
        grid.add(UIFactory.crearDatoDetalle(idioma.get("manufacturer.detail.address"), direccion), 0, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("manufacturer.detail.colonia_cp"), f.getColonia() + " C.P. " + f.getCpFabricante()), 1, 1);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("manufacturer.detail.city_mun"), f.getCiudad() + ", " + f.getMunicipio()), 0, 2);
        grid.add(UIFactory.crearDatoDetalle(idioma.get("manufacturer.detail.state_country"), f.getEstado() + ", " + f.getPais()), 1, 2);

        if (f.getCurp() != null && !f.getCurp().isEmpty())
            grid.add(UIFactory.crearDatoDetalle(idioma.get("manufacturer.detail.curp"), f.getCurp()), 0, 3);
        if (f.getRfcFabricante() != null && !f.getRfcFabricante().isEmpty())
            grid.add(UIFactory.crearDatoDetalle(idioma.get("manufacturer.detail.rfc"), f.getRfcFabricante()), 1, 3);

        Button btnCerrar = UIFactory.crearBotonSecundario(idioma.get("manufacturer.btn.close"));
        btnCerrar.setOnAction(e -> dialog.close());

        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, lblSub, new Separator(), grid, new Separator(), footer);
        dialogService.mostrarDialogoModal(dialog, root, stage);
    }
}