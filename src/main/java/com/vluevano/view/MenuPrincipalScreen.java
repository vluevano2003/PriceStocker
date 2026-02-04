package com.vluevano.view;

import com.vluevano.service.UsuarioService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MenuPrincipalScreen {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    @Lazy
    private LoginScreen loginScreen;

    @Autowired
    @Lazy
    private UsuarioView usuarioView;

    @Autowired
    @Lazy
    private ProveedorView proveedorView;

    @Autowired
    @Lazy
    private ClienteView clienteView;

    @Autowired
    @Lazy
    private FabricanteView fabricanteView;

    @Autowired
    @Lazy
    private EmpresaView empresaView;

    @Autowired
    @Lazy
    private PrestadorServicioView prestadorServicioView;

    @Autowired
    @Lazy
    private ProductoView productoView;

    private Stage stage;
    private BorderPane rootLayout;
    private String usuarioActual;

    // Colores y estilos
    private static final String COLOR_PRIMARY = "#F97316";
    private static final String COLOR_SIDEBAR_BG = "#111827";
    private static final String COLOR_SIDEBAR_HOVER = "#1F2937";
    private static final String COLOR_TEXT_SIDEBAR = "#9CA3AF";
    private static final String COLOR_TEXT_SIDEBAR_ACTIVE = "#FFFFFF";
    private static final String COLOR_BG_DASHBOARD = "#F3F4F6";

    /**
     * Muestra la pantalla principal del menú
     * 
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        inicializarComponentes();

        if (stage.getScene() != null) {
            stage.getScene().setRoot(rootLayout);
        } else {
            Scene scene = new Scene(rootLayout, 1200, 768);
            stage.setScene(scene);
        }

        stage.setResizable(true);

        // Ajustar tamaño si es muy pequeño
        if (stage.getWidth() < 1000 || Double.isNaN(stage.getWidth())) {
            stage.setWidth(1200);
            stage.setHeight(768);
            stage.centerOnScreen();
            stage.setMaximized(true);
        }
        stage.setTitle("PriceStocker | Panel de Control");
        stage.show();
    }

    /**
     * Inicializa los componentes principales de la interfaz
     */
    private void inicializarComponentes() {
        rootLayout = new BorderPane();

        VBox sidebar = crearSidebar();
        rootLayout.setLeft(sidebar);

        BorderPane contentPane = new BorderPane();
        contentPane.setTop(crearHeader());
        contentPane.setCenter(crearPantallaBienvenida());

        rootLayout.setCenter(contentPane);
    }

    /**
     * Crea el header superior con información del usuario
     * 
     * @return
     */
    private HBox crearHeader() {
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 30, 15, 30));
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setStyle("-fx-background-color: white;");

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.05));
        shadow.setRadius(10);
        shadow.setOffsetY(2);
        header.setEffect(shadow);

        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_RIGHT);

        Label lblUsuario = new Label(usuarioActual);
        lblUsuario.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-weight: 700; -fx-font-size: 14px; -fx-text-fill: #111827;");

        Label lblRol = new Label("Usuario Activo");
        lblRol.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 11px; -fx-text-fill: #6B7280;");

        userInfo.getChildren().addAll(lblUsuario, lblRol);

        StackPane avatarContainer = new StackPane();
        ImageView avatarImg = new ImageView();
        try {
            avatarImg.setImage(new Image(getClass().getResourceAsStream("/images/PriceStockerIcon.png")));
            avatarImg.setFitWidth(40);
            avatarImg.setFitHeight(40);

            Circle clip = new Circle(20, 20, 20);
            avatarImg.setClip(clip);
        } catch (Exception e) {
            Circle bg = new Circle(20, Color.web(COLOR_PRIMARY));
            Label inicial = new Label(usuarioActual.substring(0, 1).toUpperCase());
            inicial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            avatarContainer.getChildren().addAll(bg, inicial);
        }
        if (avatarImg.getImage() != null)
            avatarContainer.getChildren().add(avatarImg);

        Circle border = new Circle(20);
        border.setFill(Color.TRANSPARENT);
        border.setStroke(Color.web("#E5E7EB"));
        border.setStrokeWidth(1);
        avatarContainer.getChildren().add(border);

        header.getChildren().addAll(userInfo, avatarContainer);
        return header;
    }

    /**
     * Crea la barra lateral de navegación
     * 
     * @return
     */
    private VBox crearSidebar() {
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(25, 15, 25, 15));
        sidebar.setPrefWidth(260);
        sidebar.setStyle("-fx-background-color: " + COLOR_SIDEBAR_BG + ";");

        HBox logoContainer = new HBox(10);
        logoContainer.setAlignment(Pos.CENTER_LEFT);
        logoContainer.setPadding(new Insets(0, 0, 30, 10));

        Label lblLogo = new Label("PRICESTOCKER");
        lblLogo.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-weight: 900; -fx-font-size: 18px; -fx-text-fill: white;");
        Label lblPunto = new Label(".");
        lblPunto.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-weight: 900; -fx-font-size: 24px; -fx-text-fill: "
                + COLOR_PRIMARY + ";");

        logoContainer.getChildren().addAll(lblLogo, lblPunto);
        sidebar.getChildren().add(logoContainer);

        // OPERACIONES
        sidebar.getChildren().add(crearTituloSeccion("OPERACIONES"));

        sidebar.getChildren().add(crearBotonMenu("Gestión de Productos", () -> {
            productoView.show(stage, usuarioActual);
        }));

        Region spacer1 = new Region();
        spacer1.setPrefHeight(15);
        sidebar.getChildren().add(spacer1);

        // DIRECTORIO
        sidebar.getChildren().add(crearTituloSeccion("DIRECTORIO"));
        sidebar.getChildren().add(crearBotonMenu("Proveedores", () -> { proveedorView.show(stage, usuarioActual);}));
        sidebar.getChildren().add(crearBotonMenu("Clientes", () -> clienteView.show(stage, usuarioActual)));
        sidebar.getChildren().add(crearBotonMenu("Fabricantes", () -> fabricanteView.show(stage, usuarioActual)));
        sidebar.getChildren().add(crearBotonMenu("Empresas Mercado", () -> empresaView.show(stage, usuarioActual)));
        sidebar.getChildren()
                .add(crearBotonMenu("Prestadores Servicio", () -> prestadorServicioView.show(stage, usuarioActual)));

        // ADMIN
        if (usuarioService.tienePermiso(usuarioActual)) {
            Region spacerAdmin = new Region();
            spacerAdmin.setPrefHeight(15);
            sidebar.getChildren().add(spacerAdmin);
            sidebar.getChildren().add(crearTituloSeccion("ADMINISTRACIÓN"));
            Button btnAdmin = crearBotonMenu("Gestión de Usuarios", () -> {
                usuarioView.show(stage, usuarioActual);
            });
            sidebar.getChildren().add(btnAdmin);
        }

        // FOOTER
        Region spacerBottom = new Region();
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        Separator sep = new Separator();
        sep.setOpacity(0.1);
        sep.setPadding(new Insets(10, 0, 10, 0));

        Button btnCerrar = crearBotonCerrarSesion();
        HBox logoutContainer = new HBox(btnCerrar);
        logoutContainer.setAlignment(Pos.CENTER);
        sidebar.getChildren().addAll(spacerBottom, sep, logoutContainer);
        return sidebar;
    }

    /**
     * Crea un título de sección en la barra lateral
     * 
     * @param texto
     * @return
     */
    private Label crearTituloSeccion(String texto) {
        Label lbl = new Label(texto);
        lbl.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: #4B5563; -fx-padding: 0 0 5 10;");
        return lbl;
    }

    /**
     * Crea la pantalla de bienvenida en el centro
     * 
     * @return
     */
    private StackPane crearPantallaBienvenida() {
        StackPane centro = new StackPane();
        centro.setStyle("-fx-background-color: " + COLOR_BG_DASHBOARD + ";");

        VBox contenido = new VBox(25);
        contenido.setAlignment(Pos.CENTER);

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setMaxSize(500, 300);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 4);");

        ImageView imgLogo = new ImageView();
        try {
            imgLogo.setImage(new Image(getClass().getResourceAsStream("/images/PriceStockerLogo.png")));
        } catch (Exception e) {
        }
        imgLogo.setFitWidth(200);
        imgLogo.setPreserveRatio(true);
        imgLogo.setOpacity(0.8);

        Label lblBienvenida = new Label("¡Bienvenido, " + usuarioActual + "!");
        lblBienvenida.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        Label lblSub = new Label("Selecciona una opción del menú para comenzar a trabajar.");
        lblSub.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-text-fill: #6B7280;");

        card.getChildren().addAll(imgLogo, lblBienvenida, lblSub);
        centro.getChildren().add(card);
        return centro;
    }

    /**
     * Crea un botón de menú lateral
     * 
     * @param texto
     * @param accion
     * @return
     */
    private Button crearBotonMenu(String texto, Runnable accion) {
        Button btn = new Button(texto);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 15));

        String styleBase = "-fx-background-color: transparent; -fx-text-fill: " + COLOR_TEXT_SIDEBAR
                + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 6;";
        String styleHover = "-fx-background-color: " + COLOR_SIDEBAR_HOVER + "; -fx-text-fill: "
                + COLOR_TEXT_SIDEBAR_ACTIVE
                + "; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 6;";

        btn.setStyle(styleBase);

        Rectangle indicator = new Rectangle(3, 16, Color.web(COLOR_PRIMARY));
        indicator.setVisible(false);
        btn.setGraphic(indicator);
        btn.setGraphicTextGap(10);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(styleHover);
            indicator.setVisible(true);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(styleBase);
            indicator.setVisible(false);
        });

        btn.setOnAction(e -> accion.run());
        return btn;
    }

    /**
     * Crea el botón de cerrar sesión
     * 
     * @return
     */
    private Button crearBotonCerrarSesion() {
        Button btn = new Button("Cerrar Sesión");
        btn.setPrefWidth(120);
        btn.setAlignment(Pos.CENTER);
        btn.setPadding(new Insets(6, 10, 6, 10));

        String styleBase = "-fx-background-color: #EB1414; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand;";
        String styleHover = "-fx-background-color: #C01111; -fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: 600; -fx-cursor: hand; -fx-background-radius: 6;";

        btn.setStyle(styleBase);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleBase));

        btn.setOnAction(e -> cerrarSesion());
        return btn;
    }

    /**
     * Cerrar sesión y volver a la pantalla de login
     */
    private void cerrarSesion() {
        loginScreen.show(stage);
    }
}