package com.vluevano.view;

import com.vluevano.model.Usuario;
import com.vluevano.service.DialogService;
import com.vluevano.service.UsuarioService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class UsuarioView {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private DialogService dialogService;
    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private String usuarioActual;
    private TableView<Usuario> tablaUsuarios;
    private TextField txtNombre;
    private PasswordField txtPassword;
    private CheckBox chkAdmin;
    private Label lblMensaje;

    /**
     * Muestra la pantalla de gestión de usuarios
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
            Scene scene = new Scene(root, 1200, 768);
            stage.setScene(scene);
            stage.centerOnScreen();
        }
        stage.setResizable(true);
        stage.setTitle("PriceStocker | Gestión de Usuarios");
        stage.show();
        cargarUsuarios();
    }

    /**
     * Crea el contenido principal de la vista
     * 
     * @return
     */
    private BorderPane crearContenido() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader("Gestión de Usuarios", "Administra los accesos, roles y contraseñas del sistema", () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        HBox contenidoCentral = new HBox(30);
        contenidoCentral.setPadding(new Insets(30));

        VBox panelTabla = crearPanelTabla();
        HBox.setHgrow(panelTabla, Priority.ALWAYS);

        VBox panelFormulario = crearPanelFormulario();

        contenidoCentral.getChildren().addAll(panelTabla, panelFormulario);
        root.setCenter(contenidoCentral);

        return root;
    }

    /**
     * Crea el panel de la tabla de usuarios
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);
        tablaUsuarios = new TableView<>();
        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaUsuarios.setStyle("-fx-base: #111827; -fx-control-inner-background: white; -fx-background-color: white; -fx-table-cell-border-color: #E5E7EB; -fx-table-header-border-color: #E5E7EB; -fx-border-color: #E5E7EB; -fx-font-size: 13px;");

        Label lblVacio = new Label("No hay usuarios registrados aún.");
        lblVacio.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;");
        tablaUsuarios.setPlaceholder(lblVacio);

        TableColumn<Usuario, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getIdUsuario()));
        colId.setMinWidth(50);
        colId.setMaxWidth(50);

        TableColumn<Usuario, String> colNombre = new TableColumn<>("Usuario");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreUsuario()));

        TableColumn<Usuario, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isPermiso() ? "ADMINISTRADOR" : "ESTÁNDAR"));
        colRol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("ADMINISTRADOR"))
                        setStyle("-fx-text-fill: " + AppTheme.COLOR_PRIMARY + "; -fx-font-weight: bold;");
                    else
                        setStyle("-fx-text-fill: #6B7280;");
                }
            }
        });

        TableColumn<Usuario, Void> colAcciones = new TableColumn<>("Gestión");
        colAcciones.setMinWidth(220);
        colAcciones.setCellFactory(param -> new TableCell<>() {
            
            private final Button btnPermiso = new Button();
            private final Button btnEliminar = UIFactory.crearBotonTablaEliminar(() -> eliminarUsuario(getTableView().getItems().get(getIndex())));
            {
                btnPermiso.setStyle("-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 4; -fx-text-fill: #374151; -fx-font-size: 11px; -fx-cursor: hand;");
                btnPermiso.setOnAction(e -> togglePermiso(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    if (u.isPermiso()) {
                        btnPermiso.setText("Revocar Admin");
                    } else {
                        btnPermiso.setText("Hacer Admin");
                    }
                    HBox pane = new HBox(8, btnPermiso, btnEliminar);
                    pane.setAlignment(Pos.CENTER);
                    setGraphic(pane);
                }
            }
        });
        tablaUsuarios.getColumns().addAll(colId, colNombre, colRol, colAcciones);
        VBox.setVgrow(tablaUsuarios, Priority.ALWAYS);
        box.getChildren().add(tablaUsuarios);
        return box;
    }

    /**
     * Cambia el permiso de administrador de un usuario
     * @param u
     */
    private void togglePermiso(Usuario u) {
        if (u.getNombreUsuario().equals(this.usuarioActual)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Acción denegada", "No puedes quitarte tus propios permisos de administrador.", stage);
            return;
        }
        boolean nuevoEstado = !u.isPermiso();
        String accion = nuevoEstado ? "dar permisos de Administrador" : "quitar permisos de Administrador";
        
        if (dialogService.mostrarConfirmacion("Confirmar cambio de rol", "¿Estás seguro de " + accion + " al usuario " + u.getNombreUsuario() + "?", stage)) {
            usuarioService.actualizarPermiso(u.getIdUsuario(), nuevoEstado);
            cargarUsuarios();
        }
    }

    /**
     * Crea el panel del formulario de registro de usuarios
     * 
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setMinWidth(350);
        card.setMaxWidth(350);
        card.setStyle(AppTheme.STYLE_CARD);

        Label lblTitle = new Label("Nuevo Usuario");
        lblTitle.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        txtNombre = UIFactory.crearInput("Nombre de usuario");
        txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        txtPassword.setStyle(AppTheme.STYLE_INPUT);

        chkAdmin = new CheckBox("Conceder permisos de Administrador");
        chkAdmin.setStyle("-fx-font-size: 13px; -fx-text-fill: #4B5563;");

        VBox inputs = new VBox(15,
                UIFactory.crearGrupoInput("Nombre", txtNombre),
                UIFactory.crearGrupoInput("Contraseña", txtPassword),
                chkAdmin);

        Button btnGuardar = UIFactory.crearBotonPrimario("Registrar Usuario");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarUsuario());

        Button btnLimpiar = UIFactory.crearBotonTexto("Limpiar Formulario");
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        card.getChildren().addAll(lblTitle, inputs, btnGuardar, btnLimpiar, lblMensaje);
        return card;
    }

    /**
     * Carga los usuarios en la tabla desde el servicio
     */
    private void cargarUsuarios() {
        tablaUsuarios.getItems().setAll(usuarioService.consultarUsuarios());
    }

    /**
     * Registra un nuevo usuario usando los datos del formulario
     */
    private void registrarUsuario() {
        Usuario u = new Usuario();
        u.setNombreUsuario(txtNombre.getText());
        u.setContrasenaUsuario(txtPassword.getText());
        u.setPermiso(chkAdmin.isSelected());

        String resultado = usuarioService.registrarUsuario(u);
        if (resultado.contains("exitosamente")) {
            lblMensaje.setTextFill(Color.GREEN);
            limpiarFormulario();
            cargarUsuarios();
        } else {
            lblMensaje.setTextFill(Color.RED);
        }
        lblMensaje.setText(resultado);
    }

    /**
     * Elimina un usuario después de confirmar la acción
     * 
     * @param user
     */
    private void eliminarUsuario(Usuario user) {
        if (user.getNombreUsuario().equals(this.usuarioActual)) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING, "Acción no permitida",
                    "No puedes eliminar tu propia cuenta en sesión.", stage);
            return;
        }

        if (dialogService.mostrarConfirmacion("Confirmar eliminación", "¿Eliminar a " + user.getNombreUsuario() + "?",
                stage)) {
            if (usuarioService.eliminarUsuario(user.getIdUsuario()))
                cargarUsuarios();
            else
                dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el usuario.", stage);
        }
    }

    /**
     * Limpia los campos del formulario
     */
    private void limpiarFormulario() {
        txtNombre.clear();
        txtPassword.clear();
        chkAdmin.setSelected(false);
        lblMensaje.setText("");
    }
}