package com.vluevano.view;

import com.vluevano.model.Usuario;
import com.vluevano.service.UsuarioService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.util.Optional;

@Component
public class UsuarioView {

    @Autowired
    private UsuarioService usuarioService;

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

    // COLORES Y ESTILOS
    private static final String COLOR_PRIMARY = "#F97316";
    private static final String COLOR_PRIMARY_HOVER = "#EA580C";
    private static final String COLOR_BG_LIGHT = "#F3F4F6";
    private static final String STYLE_INPUT = "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14px;";
    private static final String STYLE_DIALOG_BG = "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4);";

    /**
     * Muestra la vista de gestión de usuarios
     * 
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        BorderPane root = crearContenido();

        // Mantiene el tamaño si ya hay una escena
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
        root.setStyle("-fx-background-color: " + COLOR_BG_LIGHT + ";");

        // HEADER
        HBox header = new HBox(20);
        header.setPadding(new Insets(20, 40, 20, 40));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(
                "-fx-background-color: #111827; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label lblTitulo = new Label("Gestión de Usuarios");
        lblTitulo.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnVolver = new Button("Volver al Menú");
        crearBotonSecundario(btnVolver);
        btnVolver.setOnAction(e -> menuPrincipalScreen.show(stage, this.usuarioActual));

        header.getChildren().addAll(lblTitulo, spacer, btnVolver);
        root.setTop(header);

        // CONTENIDO CENTRAL
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
     * Crea el panel con la tabla de usuarios
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        tablaUsuarios = new TableView<>();
        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tablaUsuarios.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #E5E7EB; -fx-font-size: 14px;");

        // ID
        TableColumn<Usuario, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getIdUsuario()));
        colId.setResizable(false);
        colId.setMinWidth(50);
        colId.setMaxWidth(50);
        colId.setPrefWidth(50);

        // ACCIONES
        TableColumn<Usuario, Void> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setResizable(false);
        colAcciones.setMinWidth(180);
        colAcciones.setMaxWidth(180);
        colAcciones.setPrefWidth(180);

        // USUARIO
        TableColumn<Usuario, String> colNombre = new TableColumn<>("Usuario");
        colNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreUsuario()));

        // ROL
        TableColumn<Usuario, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isPermiso() ? "ADMINISTRADOR" : "ESTÁNDAR"));

        // Ajuste dinámico de anchos
        colNombre.prefWidthProperty().bind(
                tablaUsuarios.widthProperty().subtract(232).multiply(0.60) // 60% para Usuario
        );
        colRol.prefWidthProperty().bind(
                tablaUsuarios.widthProperty().subtract(232).multiply(0.40) // 40% para Rol
        );

        colRol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("ADMINISTRADOR")) {
                        setStyle("-fx-text-fill: " + COLOR_PRIMARY
                                + "; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                    } else {
                        setStyle("-fx-text-fill: #6B7280; -fx-alignment: CENTER-LEFT;");
                    }
                }
            }
        });

        // ACCIONES
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox pane = new HBox(8, btnEditar, btnEliminar);
            {
                pane.setAlignment(Pos.CENTER);
                btnEditar.setStyle(
                        "-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-text-fill: #374151; -fx-background-radius: 4; -fx-border-radius: 4; -fx-cursor: hand; -fx-font-size: 12px;");
                btnEditar.setOnAction(event -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    cambiarContraseña(u);
                });
                btnEliminar.setStyle(
                        "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-background-radius: 4; -fx-cursor: hand; -fx-font-size: 12px;");
                btnEliminar.setOnAction(event -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    eliminarUsuario(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tablaUsuarios.getColumns().addAll(colId, colNombre, colRol, colAcciones);
        VBox.setVgrow(tablaUsuarios, Priority.ALWAYS);

        box.getChildren().addAll(tablaUsuarios);
        return box;
    }

    /**
     * Crea el panel con el formulario de registro
     * 
     * @return
     */
    private VBox crearPanelFormulario() {
        VBox card = new VBox(20);
        card.setPadding(new Insets(30));
        card.setMinWidth(350);
        card.setMaxWidth(350);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        Label lblTitle = new Label("Nuevo Usuario");
        lblTitle.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        VBox inputs = new VBox(15);
        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre de usuario");
        txtNombre.setStyle(STYLE_INPUT);

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Contraseña");
        txtPassword.setStyle(STYLE_INPUT);

        chkAdmin = new CheckBox("Conceder permisos de Administrador");
        chkAdmin.setStyle("-fx-font-size: 13px; -fx-text-fill: #4B5563;");

        inputs.getChildren().addAll(new Label("Nombre"), txtNombre, new Label("Contraseña"), txtPassword, chkAdmin);

        Button btnGuardar = new Button("Registrar Usuario");
        crearBotonPrimario(btnGuardar);
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarUsuario());

        Button btnLimpiar = new Button("Limpiar Formulario");
        crearBotonTexto(btnLimpiar);
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        card.getChildren().addAll(lblTitle, inputs, btnGuardar, btnLimpiar, lblMensaje);
        return card;
    }

    /**
     * Carga los usuarios desde el servicio
     */
    private void cargarUsuarios() {
        tablaUsuarios.getItems().setAll(usuarioService.consultarUsuarios());
    }

    /**
     * Registra un nuevo usuario
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
     * Elimina un usuario
     * 
     * @param user
     */
    private void eliminarUsuario(Usuario user) {
        if (user.getNombreUsuario().equals(this.usuarioActual)) {
            mostrarAlertaPersonalizada(Alert.AlertType.WARNING, "Acción no permitida",
                    "No puedes eliminar tu propia cuenta mientras estás en sesión.");
            return;
        }

        boolean confirmado = mostrarAlertaConfirmacion("Confirmar eliminación",
                "¿Estás seguro que deseas eliminar al usuario " + user.getNombreUsuario() + "?");

        if (confirmado) {
            if (usuarioService.eliminarUsuario(user.getIdUsuario())) {
                cargarUsuarios();
            } else {
                mostrarAlertaPersonalizada(Alert.AlertType.ERROR, "Error", "No se pudo eliminar el usuario.");
            }
        }
    }

    /**
     * Cambia la contraseña de un usuario
     * 
     * @param user
     */
    private void cambiarContraseña(Usuario user) {
        Optional<String> newPass = mostrarPopUpCambiarContraseña(user);

        newPass.ifPresent(pass -> {
            if (pass.length() < 6) {
                mostrarAlertaPersonalizada(Alert.AlertType.ERROR, "Contraseña insegura",
                        "La contraseña debe tener al menos 6 caracteres.");
            } else {
                if (usuarioService.cambiarContrasena(user.getIdUsuario(), pass)) {
                    mostrarAlertaPersonalizada(Alert.AlertType.INFORMATION, "Éxito",
                            "Contraseña actualizada correctamente.");
                }
            }
        });
    }

    /**
     * Pop up para cambiar la contraseña
     * 
     * @param user
     * @return
     */
    private Optional<String> mostrarPopUpCambiarContraseña(Usuario user) {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(stage);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle(STYLE_DIALOG_BG + " -fx-background-radius: 12; -fx-border-radius: 12;");
        root.setMinWidth(400);

        Label lblTitulo = new Label("Cambiar Contraseña");
        lblTitulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label lblSub = new Label("Usuario: " + user.getNombreUsuario());
        lblSub.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 14px;");

        PasswordField txtNewPass = new PasswordField();
        txtNewPass.setPromptText("Nueva contraseña");
        txtNewPass.setStyle(STYLE_INPUT);

        HBox botones = new HBox(15);
        botones.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = new Button("Cancelar");
        crearBotonSecundario(btnCancelar);

        Button btnConfirmar = new Button("Actualizar");
        crearBotonPrimario(btnConfirmar);

        final String[] resultado = new String[1];

        btnCancelar.setOnAction(e -> dialogStage.close());
        btnConfirmar.setOnAction(e -> {
            resultado[0] = txtNewPass.getText();
            dialogStage.close();
        });

        botones.getChildren().addAll(btnCancelar, btnConfirmar);
        root.getChildren().addAll(lblTitulo, lblSub, txtNewPass, botones);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

        return Optional.ofNullable(resultado[0]);
    }

    /**
     * Mostrar alerta
     * 
     * @param tipo
     * @param titulo
     * @param mensaje
     */
    private void mostrarAlertaPersonalizada(Alert.AlertType tipo, String titulo, String mensaje) {
        Stage alertStage = new Stage();
        alertStage.initOwner(stage);
        alertStage.initModality(Modality.WINDOW_MODAL);
        alertStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle(STYLE_DIALOG_BG + " -fx-background-radius: 12; -fx-border-radius: 12;");
        root.setMinWidth(350);
        root.setMaxWidth(350);

        String colorTitulo = tipo == Alert.AlertType.ERROR ? "#DC2626"
                : (tipo == Alert.AlertType.WARNING ? "#D97706" : COLOR_PRIMARY);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + colorTitulo + ";");

        Label lblMsg = new Label(mensaje);
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");

        HBox botones = new HBox();
        botones.setAlignment(Pos.CENTER_RIGHT);

        Button btnOk = new Button("Entendido");
        if (tipo == Alert.AlertType.ERROR) {
            btnOk.setStyle(
                    "-fx-background-color: #FEE2E2; -fx-text-fill: #DC2626; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        } else {
            crearBotonPrimario(btnOk);
        }
        btnOk.setOnAction(e -> alertStage.close());

        botones.getChildren().add(btnOk);
        root.getChildren().addAll(lblTitulo, lblMsg, botones);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        alertStage.setScene(scene);
        alertStage.showAndWait();
    }

    /**
     * Mostrar alerta de confirmación
     * 
     * @param titulo
     * @param mensaje
     * @return
     */
    private boolean mostrarAlertaConfirmacion(String titulo, String mensaje) {
        Stage alertStage = new Stage();
        alertStage.initOwner(stage);
        alertStage.initModality(Modality.WINDOW_MODAL);
        alertStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setStyle(STYLE_DIALOG_BG + " -fx-background-radius: 12; -fx-border-radius: 12;");
        root.setMinWidth(380);

        Label lblTitulo = new Label(titulo);
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label lblMsg = new Label(mensaje);
        lblMsg.setWrapText(true);
        lblMsg.setStyle("-fx-text-fill: #374151; -fx-font-size: 14px;");

        HBox botones = new HBox(15);
        botones.setAlignment(Pos.CENTER_RIGHT);

        Button btnNo = new Button("Cancelar");
        crearBotonSecundario(btnNo);

        Button btnSi = new Button("Confirmar");
        btnSi.setStyle(
                "-fx-background-color: #DC2626; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");

        final boolean[] respuesta = { false };

        btnNo.setOnAction(e -> {
            respuesta[0] = false;
            alertStage.close();
        });

        btnSi.setOnAction(e -> {
            respuesta[0] = true;
            alertStage.close();
        });

        botones.getChildren().addAll(btnNo, btnSi);
        root.getChildren().addAll(lblTitulo, lblMsg, botones);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        alertStage.setScene(scene);
        alertStage.showAndWait();

        return respuesta[0];
    }

    /**
     * Limpia el formulario de registro
     */
    private void limpiarFormulario() {
        txtNombre.clear();
        txtPassword.clear();
        chkAdmin.setSelected(false);
        lblMensaje.setText("");
    }

    /**
     * Botón primario
     * 
     * @param btn
     */
    private void crearBotonPrimario(Button btn) {
        btn.setPrefHeight(40);
        btn.setStyle("-fx-background-color: " + COLOR_PRIMARY
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + COLOR_PRIMARY_HOVER
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + COLOR_PRIMARY
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;"));
    }

    /**
     * Botón secundario
     * 
     * @param btn
     */
    private void crearBotonSecundario(Button btn) {
        btn.setPrefHeight(35);
        btn.setStyle(
                "-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: #F9FAFB; -fx-border-color: #9CA3AF; -fx-text-fill: #111827; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: white; -fx-border-color: #D1D5DB; -fx-text-fill: #374151; -fx-font-weight: 600; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand;"));
    }

    /**
     * Botón de texto
     * 
     * @param btn
     */
    private void crearBotonTexto(Button btn) {
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_PRIMARY
                + "; -fx-cursor: hand; -fx-underline: true;"));
        btn.setOnMouseExited(
                e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-cursor: hand;"));
    }
}