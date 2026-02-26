package com.vluevano.view;

import com.vluevano.model.Usuario;
import com.vluevano.service.UsuarioService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import com.vluevano.view.base.BaseDirectorioView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioView extends BaseDirectorioView<Usuario> {

    @Autowired
    private UsuarioService usuarioService;

    private TextField txtNombre;
    private PasswordField txtPassword;
    private CheckBox chkAdmin;

    @Override
    protected String getTituloVentana() {
        return idioma.get("users.window.title");
    }

    @Override
    protected String getTituloHeader() {
        return idioma.get("users.header.title");
    }

    @Override
    protected String getSubtituloHeader() {
        return idioma.get("users.header.subtitle");
    }

    @Override
    protected String getPromptBusqueda() {
        return idioma.get("users.placeholder.user"); 
    }

    @Override
    protected String getTituloFormularioNuevo() {
        return idioma.get("users.form.new");
    }

    @Override
    protected String getTextoBotonGuardar() {
        return idioma.get("users.btn.register");
    }

    /**
     * Cargamos todos los usuarios, incluyendo los inactivos, para que el admin pueda gestionarlos (reactivar o eliminar definitivamente). La distinción visual se hace en la tabla, donde los inactivos se muestran en gris y con la opción de reactivar.
     */
    @Override
    protected void cargarDatos() {
        tablaDatos.getItems().setAll(usuarioService.consultarUsuarios());
    }

    /**
     * Consultamos todos los usuarios y luego filtramos en memoria. Esto permite mostrar también los inactivos en los resultados de búsqueda, con su distinción visual correspondiente.
     */
    @Override
    protected void buscarDatos(String valor) {
        List<Usuario> todos = usuarioService.consultarUsuarios();
        if (valor == null || valor.trim().isEmpty()) {
            tablaDatos.getItems().setAll(todos);
        } else {
            String lower = valor.toLowerCase();
            List<Usuario> filtrados = todos.stream()
                .filter(u -> u.getNombreUsuario().toLowerCase().contains(lower))
                .toList();
            tablaDatos.getItems().setAll(filtrados);
        }
    }

    /**
     * Creamos el formulario de registro con campos para nombre de usuario, contraseña y un checkbox para asignar permisos de administrador. La validación se realiza en el servicio, donde también se maneja la lógica de reactivación si el usuario ya existe pero está inactivo.
     */
    @Override
    protected VBox construirCamposFormulario() {
        VBox inputsContainer = new VBox(15);
        inputsContainer.setPadding(new Insets(20));

        txtNombre = UIFactory.crearInput(idioma.get("users.placeholder.user"));
        txtPassword = new PasswordField();
        txtPassword.setPromptText(idioma.get("users.placeholder.pass"));
        txtPassword.setStyle(AppTheme.STYLE_INPUT);

        chkAdmin = new CheckBox(idioma.get("users.chk.admin"));
        chkAdmin.setStyle("-fx-font-size: 13px; -fx-text-fill: #4B5563;");

        inputsContainer.getChildren().addAll(
                UIFactory.crearGrupoInput(idioma.get("users.lbl.user"), txtNombre),
                UIFactory.crearGrupoInput(idioma.get("users.lbl.pass"), txtPassword),
                chkAdmin
        );

        return inputsContainer;
    }

    /**
     * Crea las columnas de la tabla, incluyendo una columna de acciones con botones para cambiar permisos y eliminar (desactivar) usuarios. Los usuarios inactivos se muestran en gris y con un botón para reactivarlos.
     */
    @Override
    protected void configurarColumnasTabla() {
        Label lblVacio = new Label(idioma.get("users.table.empty"));
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaDatos.setPlaceholder(lblVacio);

        tablaDatos.setRowFactory(tv -> new TableRow<Usuario>() {
            @Override
            protected void updateItem(Usuario item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.getActivo()) {
                    setStyle("-fx-background-color: #F3F4F6; -fx-opacity: 0.6;");
                } else {
                    setStyle("");
                }
            }
        });

        TableColumn<Usuario, String> colId = UIFactory.crearColumna(idioma.get("users.col.id"), u -> String.valueOf(u.getIdUsuario()), 50);
        colId.setMaxWidth(50);
        
        TableColumn<Usuario, String> colNombre = UIFactory.crearColumna(idioma.get("users.col.user"), Usuario::getNombreUsuario, 0);
        
        TableColumn<Usuario, String> colRol = UIFactory.crearColumna(idioma.get("users.col.role"), 
            u -> u.isPermiso() ? idioma.get("users.role.admin") : idioma.get("users.role.standard"), 0);

        colRol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Usuario u = getTableView().getItems().get(getIndex());
                    if (u != null && !u.getActivo()) {
                        setStyle("-fx-text-fill: #6B7280; -fx-font-style: italic;");
                        setText("DESACTIVADO");
                    } else if (item.equals(idioma.get("users.role.admin"))) {
                        setStyle("-fx-text-fill: " + AppTheme.COLOR_PRIMARY + "; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #6B7280;");
                    }
                }
            }
        });

        TableColumn<Usuario, Void> colAcciones = new TableColumn<>(idioma.get("users.col.actions"));
        colAcciones.setMinWidth(220);
        colAcciones.setMaxWidth(220);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnPermiso = new Button();
            private final Button btnEliminar = UIFactory.crearBotonTablaEliminar(() -> eliminarUsuario(getTableView().getItems().get(getIndex())));
            private final Button btnReactivar = new Button("Reactivar");
            
            {
                btnPermiso.setStyle("-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 4; -fx-text-fill: #374151; -fx-font-size: 11px; -fx-cursor: hand;");
                btnPermiso.setOnAction(e -> togglePermiso(getTableView().getItems().get(getIndex())));

                btnReactivar.setStyle("-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-cursor: hand; -fx-font-size: 11px; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4;");
                btnReactivar.setOnAction(e -> reactivarUsuario(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableView().getItems().get(getIndex()) == null) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    if (!u.getActivo()) {
                        HBox pane = new HBox(8, btnReactivar);
                        pane.setAlignment(Pos.CENTER);
                        setGraphic(pane);
                    } else {
                        if (u.isPermiso()) {
                            btnPermiso.setText(idioma.get("users.btn.revoke_admin"));
                        } else {
                            btnPermiso.setText(idioma.get("users.btn.make_admin"));
                        }
                        HBox pane = new HBox(8, btnPermiso, btnEliminar);
                        pane.setAlignment(Pos.CENTER);
                        setGraphic(pane);
                    }
                }
            }
        });

        tablaDatos.getColumns().addAll(List.of(colId, colNombre, colRol, colAcciones));
    }

    /**
     * Limpiamos los campos del formulario después de registrar un usuario o al cancelar. Esto asegura que el formulario esté listo para un nuevo registro sin datos residuales.
     */
    @Override
    protected void limpiarCamposEspecificos() {
        txtNombre.clear();
        txtPassword.clear();
        chkAdmin.setSelected(false);
    }

    /**
     * Registramos un nuevo usuario utilizando el servicio. El servicio se encarga de validar los datos, manejar la lógica de reactivación si el usuario ya existe pero está inactivo, y retornar un mensaje adecuado según el resultado de la operación. Dependiendo del mensaje, mostramos una alerta de éxito o error.
     */
    @Override
    protected void registrarEntidad() {
        Usuario u = new Usuario();
        u.setNombreUsuario(txtNombre.getText());
        u.setContrasenaUsuario(txtPassword.getText());
        u.setPermiso(chkAdmin.isSelected());

        String resultado = usuarioService.registrarUsuario(u);
        if (resultado.contains("exitosamente") || resultado.contains("guardad") || resultado.contains("successfully") || resultado.contains("saved")) {
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("product.msg.success.title"), resultado, stage);
            limpiarFormulario();
            cargarDatos();
        } else {
            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("product.msg.error.title"), resultado, stage);
        }
    }

    /**
     * Este método no se utiliza en esta vista, ya que no tenemos una funcionalidad de "detalle" para los usuarios. Sin embargo, lo dejamos implementado para cumplir con la estructura de la clase base y evitar errores. Si en el futuro se decide agregar una vista de detalle para los usuarios, aquí sería donde se implementaría la lógica para mostrar esa información.
     */
    @Override
    protected void mostrarDetalle(Usuario entidad) {
    }

    /**
     * Este método se encarga de cambiar el permiso de un usuario entre administrador y estándar. Si el usuario es el mismo que el actual, se muestra una alerta de advertencia para evitar que un admin se quite sus propios permisos. Para otros usuarios, se muestra una confirmación antes de realizar el cambio, y luego se actualiza la tabla con los nuevos datos.
     * @param u
     */
    private void togglePermiso(Usuario u) {
        if (u.getNombreUsuario().equals(this.usuarioActual)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING,
                    idioma.get("users.msg.denied.title"),
                    idioma.get("users.msg.denied.self_revoke"), stage);
            return;
        }
        boolean nuevoEstado = !u.isPermiso();
        String accion = nuevoEstado ? idioma.get("users.msg.confirm.grant") : idioma.get("users.msg.confirm.revoke");

        if (dialogService.mostrarConfirmacion(
                idioma.get("users.msg.confirm.role_change.title"),
                idioma.get("users.msg.confirm.role_change.content", accion, u.getNombreUsuario()),
                stage)) {
            usuarioService.actualizarPermiso(u.getIdUsuario(), nuevoEstado);
            cargarDatos();
        }
    }

    /**
     * Este método se encarga de eliminar (desactivar) un usuario. Si el usuario a eliminar es el mismo que el actual, se muestra una alerta de advertencia para evitar que un admin se elimine a sí mismo. Para otros usuarios, se muestra una confirmación antes de realizar la eliminación, y luego se actualiza la tabla con los nuevos datos. La eliminación en este caso no borra el registro de la base de datos, sino que lo marca como inactivo, permitiendo su reactivación futura si es necesario.
     * @param user
     */
    private void eliminarUsuario(Usuario user) {
        if (user.getNombreUsuario().equals(this.usuarioActual)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING,
                    idioma.get("users.msg.denied.title"),
                    idioma.get("users.msg.denied.self_delete"), stage);
            return;
        }

        if (dialogService.mostrarConfirmacion(
                idioma.get("users.msg.delete.title"),
                idioma.get("users.msg.delete.confirm", user.getNombreUsuario()),
                stage)) {
            if (usuarioService.eliminarUsuario(user.getIdUsuario())) {
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("users.msg.delete.title"), "Usuario desactivado.", stage);
                cargarDatos();
            } else {
                dialogService.mostrarAlerta(Alert.AlertType.ERROR,
                        idioma.get("users.msg.error.title"),
                        idioma.get("users.msg.error.delete"), stage);
            }
        }
    }

    /**
     * Este método se encarga de reactivar un usuario que ha sido previamente desactivado. Se muestra una confirmación antes de realizar la reactivación, y luego se actualiza la tabla con los nuevos datos. La reactivación simplemente marca al usuario como activo nuevamente, permitiéndole acceder al sistema con su nombre de usuario y contraseña anteriores.
     * @param user
     */
    private void reactivarUsuario(Usuario user) {
        if (dialogService.mostrarConfirmacion(
                "Reactivar Usuario",
                "¿Deseas reactivar al usuario " + user.getNombreUsuario() + "?",
                stage)) {
            if (usuarioService.reactivarUsuario(user.getIdUsuario())) {
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Usuario reactivado exitosamente.", stage);
                cargarDatos();
            }
        }
    }
}