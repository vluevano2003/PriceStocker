package com.vluevano.view;

import com.vluevano.controller.UsuarioController;
import com.vluevano.model.Usuario;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UsuarioView extends Application {

    private final String usuarioActual;
    private TableView<Usuario> tablaUsuarios;

    public UsuarioView(String usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Módulo de Usuarios");

        MenuBar menuBar = new MenuBar();
        Menu menu = new Menu("Opciones");
        MenuItem salirItem = new MenuItem("Salir");

        menu.getItems().addAll(salirItem);
        menuBar.getMenus().add(menu);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        salirItem.setOnAction(e -> mostrarMenuPrincipal(primaryStage));

        // Crear formulario y tabla en la misma ventana
        showFormularioYTabla(vbox);

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(vbox);

        primaryStage.setScene(new Scene(root, 900, 600));
        primaryStage.show();
    }

    private void showFormularioYTabla(VBox vbox) {
        HBox hbox = new HBox(20); // Usamos un HBox para organizar la tabla a la izquierda y el formulario a la derecha
        hbox.setPadding(new Insets(20));

        // Sección de tabla de usuarios
        VBox tablaBox = new VBox(10);
        Label lblTabla = new Label("Usuarios Registrados");
        lblTabla.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        tablaBox.getChildren().add(lblTabla);
        tablaBox.getChildren().add(crearTablaUsuarios());

        // Sección de formulario de registro
        VBox formularioBox = new VBox(10);
        Label lblFormulario = new Label("Formulario de Registro de Usuario");
        lblFormulario.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        formularioBox.getChildren().add(lblFormulario);
        formularioBox.getChildren().add(crearFormularioRegistro());

        hbox.getChildren().addAll(tablaBox, formularioBox);

        vbox.getChildren().add(hbox);
    }

    private GridPane crearFormularioRegistro() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        Label lblUsuario = new Label("Usuario:");
        TextField txtUsuario = new TextField();
        Label lblContrasena = new Label("Contraseña:");
        PasswordField txtContrasena = new PasswordField();
        CheckBox chkPermiso = new CheckBox("Es Administrador");
        Button btnRegistrar = new Button("Registrar");
        Label lblMensaje = new Label();

        btnRegistrar.setOnAction(e -> {
            String nombreUsuario = txtUsuario.getText();
            String contrasenaUsuario = txtContrasena.getText();
            StringBuilder mensaje = new StringBuilder();

            // Creación de usuario
            Usuario usuario = new Usuario(0, nombreUsuario, contrasenaUsuario, chkPermiso.isSelected());

            // Llamar al método del controlador para registrar el usuario
            if (UsuarioController.registrarUsuario(usuario, mensaje)) {
                mostrarAlerta("Éxito", mensaje.toString());
                actualizarTablaUsuarios(); // Si el registro fue exitoso
            } else {
                mostrarAlerta("Error", mensaje.toString()); // Mostrar el error
            }
        });

        grid.add(lblUsuario, 0, 0);
        grid.add(txtUsuario, 1, 0);
        grid.add(lblContrasena, 0, 1);
        grid.add(txtContrasena, 1, 1);
        grid.add(chkPermiso, 1, 2);
        grid.add(btnRegistrar, 1, 3);
        grid.add(lblMensaje, 1, 4);

        return grid;
    }

    @SuppressWarnings("unchecked")
    private VBox crearTablaUsuarios() {
        VBox vbox = new VBox();

        tablaUsuarios = new TableView<>();
        TableColumn<Usuario, Integer> colId = new TableColumn<>("ID");
        TableColumn<Usuario, String> colNombre = new TableColumn<>("Nombre");
        TableColumn<Usuario, Boolean> colPermiso = new TableColumn<>("Administrador");

        colId.setCellValueFactory(cellData -> cellData.getValue().idUsuarioProperty().asObject());
        colNombre.setCellValueFactory(cellData -> cellData.getValue().nombreUsuarioProperty());
        colPermiso.setCellValueFactory(cellData -> cellData.getValue().permisoProperty().asObject());

        tablaUsuarios.getColumns().addAll(colId, colNombre, colPermiso);
        actualizarTablaUsuarios();

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(tablaUsuarios);
        scrollPane.setFitToWidth(true); // Ajusta el tamaño de la tabla al ancho del contenedor
        scrollPane.setFitToHeight(true); // Ajusta el tamaño de la tabla al alto del contenedor

        vbox.getChildren().add(scrollPane);

        tablaUsuarios.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !tablaUsuarios.getSelectionModel().isEmpty()) {
                mostrarOpcionesUsuario(tablaUsuarios.getSelectionModel().getSelectedItem());
            }
        });

        return vbox;
    }

    private void mostrarOpcionesUsuario(Usuario usuario) {
        Stage opcionesStage = new Stage();
        opcionesStage.initModality(Modality.APPLICATION_MODAL);
        opcionesStage.setTitle("Opciones de Usuario");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(15));
        grid.setVgap(10);
        grid.setHgap(10);

        Label lblContrasena = new Label("Nueva Contraseña:");
        PasswordField txtNuevaContrasena = new PasswordField();
        Button btnCambiar = new Button("Cambiar Contraseña");
        Button btnEliminar = new Button("Eliminar Usuario");

        btnCambiar.setOnAction(e -> {
            String nuevaContrasena = txtNuevaContrasena.getText();
            if (nuevaContrasena.length() < 6) {
                mostrarAlerta("Error", "La contraseña debe tener al menos 6 caracteres.");
                return;
            }

            if (UsuarioController.cambiarContrasena(usuario.getIdUsuario(), nuevaContrasena)) {
                mostrarAlerta("Éxito", "Contraseña actualizada correctamente.");
                actualizarTablaUsuarios();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar la contraseña.");
            }
        });

        btnEliminar.setOnAction(e -> {
            if (mostrarConfirmacion("Eliminar Usuario", "¿Seguro que quieres eliminar este usuario?")) {
                if (UsuarioController.eliminarUsuario(usuario.getIdUsuario())) {
                    mostrarAlerta("Éxito", "Usuario eliminado");
                    actualizarTablaUsuarios();
                    opcionesStage.close();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el usuario");
                }
            }
        });

        grid.add(lblContrasena, 0, 0);
        grid.add(txtNuevaContrasena, 1, 0);
        grid.add(btnCambiar, 1, 1);
        grid.add(btnEliminar, 1, 2);

        opcionesStage.setScene(new Scene(grid, 400, 250));
        opcionesStage.show();
    }

    private void actualizarTablaUsuarios() {
        tablaUsuarios.getItems().setAll(UsuarioController.consultarUsuarios());
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private void mostrarMenuPrincipal(Stage primaryStage) {
        new MenuPrincipalScreen(primaryStage, usuarioActual).mostrarMenu();
    }
}
