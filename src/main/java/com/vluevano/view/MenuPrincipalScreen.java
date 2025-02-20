package com.vluevano.view;

import java.io.IOException;
import java.sql.SQLException;

import com.vluevano.controller.UsuarioController;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MenuPrincipalScreen {
    private Stage primaryStage;
    private String usuarioActual;

    public MenuPrincipalScreen(Stage primaryStage, String usuarioActual) {
        this.primaryStage = primaryStage;
        this.usuarioActual = usuarioActual;
    }

    public void mostrarMenu() {
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        primaryStage.setResizable(false);

        BorderPane root = new BorderPane();
        root.setPrefSize(800, 600);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

        MenuBar menuBar = new MenuBar();
        Menu menuArchivo = new Menu("Archivo");
        MenuItem cerrarSesion = new MenuItem("Cerrar Sesión");
        cerrarSesion.setOnAction(e -> mostrarPantallaInicio());
        menuArchivo.getItems().add(cerrarSesion);

        Menu menuProductos = new Menu("Productos");
        MenuItem abrirProductos = new MenuItem("Gestión de Productos");
        abrirProductos.setOnAction(e -> {
            try {
                new ProductoView(usuarioActual).start(primaryStage);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
        menuProductos.getItems().add(abrirProductos);

        Menu menuProveedores = new Menu("Proveedores");
        MenuItem abrirProveedores = new MenuItem("Gestión de Proveedores");
        abrirProveedores.setOnAction(e -> {
            new ProveedorView(usuarioActual).start(primaryStage);
        });
        menuProveedores.getItems().add(abrirProveedores);

        Menu menuClientes = new Menu("Clientes");
        MenuItem abrirClientes = new MenuItem("Gestión de Clientes");
        abrirClientes.setOnAction(e -> {
            new ClienteView(usuarioActual).start(primaryStage);
        });
        menuClientes.getItems().add(abrirClientes);

        Menu menuFabricantes = new Menu("Fabricantes");
        MenuItem abrirFabricantes = new MenuItem("Gestión de Fabricantes");
        abrirFabricantes.setOnAction(e -> {
            new FabricanteView(usuarioActual).start(primaryStage);
        });
        menuFabricantes.getItems().add(abrirFabricantes);

        Menu menuMercado = new Menu("Empresas del Mercado");
        MenuItem abrirEmpresas = new MenuItem("Gestión de Empresas");
        abrirEmpresas.setOnAction(e -> {
            new EmpresaView(usuarioActual).start(primaryStage);
        });
        menuMercado.getItems().add(abrirEmpresas);

        Menu menuPrestadores = new Menu("Prestadores de Servicios");
        MenuItem abrirPrestadores = new MenuItem("Gestión de Prestadores");
        abrirPrestadores.setOnAction(e -> {
            new PrestadorServicioView(usuarioActual).start(primaryStage);
        });
        menuPrestadores.getItems().add(abrirPrestadores);

        Menu menuUsuarios = new Menu("Usuarios");
        if (UsuarioController.tienePermiso(usuarioActual)) {
            MenuItem abrirUsuario = new MenuItem("Gestión de Usuarios");
            abrirUsuario.setOnAction(e -> {
                new UsuarioView(usuarioActual).start(primaryStage);
            });
            menuUsuarios.getItems().add(abrirUsuario);
        }

        menuBar.getMenus().addAll(menuArchivo, menuProductos, menuProveedores, menuClientes, menuFabricantes,
                menuMercado,
                menuPrestadores,
                menuUsuarios);

        VBox centro = new VBox(15);
        centro.setAlignment(Pos.CENTER);

        ImageView imgLogo = new ImageView(
                new Image(getClass().getResource("/images/PriceStockerLogo.png").toExternalForm()));
        imgLogo.setFitWidth(400);
        imgLogo.setPreserveRatio(true);

        Label lblBienvenida = new Label("Bienvenido, " + usuarioActual);
        lblBienvenida.setFont(new Font(20));

        centro.getChildren().addAll(imgLogo, lblBienvenida);
        root.setTop(menuBar);
        root.setCenter(centro);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Menú Principal - PriceStocker");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void mostrarPantallaInicio() {
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.start(primaryStage);
    }
}
