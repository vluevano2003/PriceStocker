package com.vluevano.view;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.List;

import com.vluevano.controller.UsuarioController;
import com.vluevano.model.Usuario;

public class LoginScreen extends Application {
    private Stage primaryStage;
    private String usuarioActual;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.getIcons().add(new Image(getClass().getResource("/images/PriceStockerIcon.png").toExternalForm()));
        mostrarPantallaInicio();
    }

    private void centrarPantalla(Stage stage) {
        stage.setX((Screen.getPrimary().getVisualBounds().getWidth() - stage.getWidth()) / 2);
        stage.setY((Screen.getPrimary().getVisualBounds().getHeight() - stage.getHeight()) / 2);
    }

    private void mostrarPantallaInicio() {
        primaryStage.setWidth(450);
        primaryStage.setHeight(550);
        primaryStage.setResizable(false);

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 350, 450);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

        ImageView imgLogo = new ImageView(
                new Image(getClass().getResource("/images/PriceStockerLogo.png").toExternalForm()));
        imgLogo.setFitWidth(300);
        imgLogo.setPreserveRatio(true);

        Label lblTitulo = new Label("Sistema de Inventario");
        lblTitulo.getStyleClass().add("titulo");

        Button btnIniciar = new Button("Iniciar sesión");
        Button btnCerrar = new Button("Cerrar");

        btnIniciar.setOnAction(e -> mostrarSeleccionUsuario());
        btnCerrar.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(imgLogo, lblTitulo, btnIniciar, btnCerrar);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Inicio de Sesión");
        primaryStage.setResizable(false);
        primaryStage.show();
        centrarPantalla(primaryStage);
    }

    private void mostrarSeleccionUsuario() {
        primaryStage.setWidth(450);
        primaryStage.setHeight(550);
        primaryStage.setResizable(false);

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 350, 450);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

        Label lblTitulo = new Label("Selecciona tu usuario");
        lblTitulo.getStyleClass().add("titulo");

        ListView<String> listaUsuarios = new ListView<>();
        List<Usuario> usuarios = UsuarioController.consultarUsuarios();

        for (Usuario usuario : usuarios) {
            listaUsuarios.getItems().add(usuario.getNombreUsuario());
        }

        Button btnSeleccionar = new Button("Seleccionar");
        Button btnRetroceder = new Button();
        ImageView imgRetroceder = new ImageView(
                new Image(getClass().getResource("/images/flecha.png").toExternalForm()));
        imgRetroceder.setFitHeight(40);
        imgRetroceder.setFitWidth(40);
        btnRetroceder.setGraphic(imgRetroceder);
        btnRetroceder.setStyle("-fx-background-color: transparent;");

        btnSeleccionar.setOnAction(e -> {
            String nombreUsuario = listaUsuarios.getSelectionModel().getSelectedItem();
            if (nombreUsuario != null) {
                mostrarPantallaContrasena(nombreUsuario);
            }
        });
        btnRetroceder.setOnAction(e -> mostrarPantallaInicio());

        HBox header = new HBox(10);
        header.setAlignment(Pos.TOP_LEFT);
        header.getChildren().add(btnRetroceder);

        root.getChildren().addAll(header, lblTitulo, listaUsuarios, btnSeleccionar);
        primaryStage.setScene(scene);
        centrarPantalla(primaryStage);
    }

    private void mostrarPantallaContrasena(String nombreUsuario) {
        primaryStage.setWidth(450);
        primaryStage.setHeight(550);
        primaryStage.setResizable(false);

        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 350, 450);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

        Label lblTitulo = new Label("Ingresa tu contraseña");
        lblTitulo.getStyleClass().add("titulo");

        PasswordField txtContrasena = new PasswordField();
        txtContrasena.setPromptText("Contraseña");

        Button btnIngresar = new Button("Ingresar");
        Button btnRetroceder = new Button();
        ImageView imgRetroceder = new ImageView(
                new Image(getClass().getResource("/images/flecha.png").toExternalForm()));
        imgRetroceder.setFitHeight(40);
        imgRetroceder.setFitWidth(40);
        btnRetroceder.setGraphic(imgRetroceder);
        btnRetroceder.setStyle("-fx-background-color: transparent;");

        btnIngresar.setOnAction(e -> {
            if (UsuarioController.iniciarSesion(nombreUsuario, txtContrasena.getText())) {
                usuarioActual = nombreUsuario;
                MenuPrincipalScreen menuPrincipalScreen = new MenuPrincipalScreen(primaryStage, usuarioActual);
                menuPrincipalScreen.mostrarMenu();
            } else {
                Alert alerta = new Alert(Alert.AlertType.ERROR, "Contraseña incorrecta");
                alerta.getDialogPane().setPrefSize(250, 100);
                alerta.show();
            }
        });
        btnRetroceder.setOnAction(e -> mostrarSeleccionUsuario());

        HBox header = new HBox(10);
        header.setAlignment(Pos.TOP_LEFT);
        header.getChildren().add(btnRetroceder);

        root.getChildren().addAll(header, lblTitulo, txtContrasena, btnIngresar);
        primaryStage.setScene(scene);
        centrarPantalla(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
