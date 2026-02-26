package com.vluevano.view;

import com.vluevano.service.MonedaService;
import com.vluevano.service.UsuarioService;
import com.vluevano.util.GestorIdioma;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MenuPrincipalScreen {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GestorIdioma idioma;

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
    @Autowired
    @Lazy
    private PerfilView perfilView;
    @Autowired
    @Lazy
    private VentaView ventaView;
    @Autowired
    @Lazy
    private CompraView compraView;
    @Autowired
    @Lazy
    private HistorialVentasView historialVentasView;
    @Autowired
    @Lazy
    private HistorialComprasView historialComprasView;
    @Autowired
    @Lazy
    private ConfiguracionView configuracionView;

    @Autowired
    private MonedaService monedaService;

    private Stage stage;
    private BorderPane rootLayout;
    private String usuarioActual;

    private Button btnFabMain;
    private Button btnFabVenta;
    private Button btnFabCompra;
    private VBox fabContainer;
    private boolean menuFabAbierto = false;
    private Region overlayOscuro;

    private static final String COLOR_PRIMARY = "#F97316";
    private static final String COLOR_SIDEBAR_BG = "#111827";
    private static final String COLOR_SIDEBAR_HOVER = "#1F2937";
    private static final String COLOR_TEXT_SIDEBAR = "#9CA3AF";
    private static final String COLOR_TEXT_SIDEBAR_ACTIVE = "#FFFFFF";
    private static final String COLOR_BG_DASHBOARD = "#F3F4F6";

    private static final String MENU_STYLES = "data:text/css,"
            + /* tu css aquí */ ".context-menu { -fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 4); -fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-padding: 4 0 4 0; } .menu-item { -fx-background-color: transparent; -fx-padding: 8 15 8 15; } .menu-item:focused { -fx-background-color: #F3F4F6; -fx-cursor: hand; } .menu-item .label { -fx-text-fill: #374151; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; -fx-font-weight: 600; }";

    /**
     * Muestra la pantalla principal del sistema
     * 
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        inicializarComponentes();

        Scene scene;
        if (stage.getScene() != null) {
            stage.getScene().setRoot(rootLayout);
            scene = stage.getScene();
        } else {
            scene = new Scene(rootLayout, 1200, 768);
            stage.setScene(scene);
        }

        if (!scene.getStylesheets().contains(MENU_STYLES)) {
            scene.getStylesheets().add(MENU_STYLES);
        }

        stage.setResizable(true);

        if (stage.getWidth() < 1000 || Double.isNaN(stage.getWidth())) {
            stage.setWidth(1200);
            stage.setHeight(768);
            stage.centerOnScreen();
            stage.setMaximized(true);
        }

        stage.setTitle("PriceStocker | " + idioma.get("menu.window.title"));
        stage.show();
    }

    /**
     * Inicializa todos los componentes de la interfaz, creando el layout principal,
     * la barra lateral, el header y la pantalla de bienvenida
     */
    private void inicializarComponentes() {
        StackPane rootStack = new StackPane();

        BorderPane mainLayout = new BorderPane();
        mainLayout.setLeft(crearSidebar());

        BorderPane contentPane = new BorderPane();
        contentPane.setTop(crearHeader());
        contentPane.setCenter(crearPantallaBienvenida());
        mainLayout.setCenter(contentPane);

        overlayOscuro = new Region();
        overlayOscuro.setStyle("-fx-background-color: rgba(0,0,0,0.4);");
        overlayOscuro.setVisible(false);
        overlayOscuro.setOnMouseClicked(e -> toggleFabMenu());

        crearBotonesFlotantes();

        rootStack.getChildren().addAll(mainLayout, overlayOscuro, fabContainer);
        StackPane.setAlignment(fabContainer, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(fabContainer, new Insets(0, 30, 30, 0));

        this.rootLayout = new BorderPane();
        this.rootLayout.setCenter(rootStack);
    }

    /**
     * Crea el header superior con la información del usuario y el menú contextual
     * para perfil, configuración y cerrar sesión
     * 
     * @return
     */
    private HBox crearHeader() {
        monedaService.inicializar();

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

        Label lblRol = new Label(idioma.get("menu.user.active"));
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

        HBox userSection = new HBox(15, userInfo, avatarContainer);
        userSection.setAlignment(Pos.CENTER_RIGHT);
        userSection.setCursor(Cursor.HAND);

        ContextMenu userMenu = new ContextMenu();

        MenuItem itemPerfil = new MenuItem(idioma.get("menu.user.profile"));
        itemPerfil.setOnAction(e -> perfilView.show(stage, usuarioActual));

        MenuItem itemConfig = new MenuItem(idioma.get("menu.user.config"));
        itemConfig.setOnAction(e -> configuracionView.show(stage, usuarioActual));

        SeparatorMenuItem sep = new SeparatorMenuItem();

        MenuItem itemLogout = new MenuItem(idioma.get("menu.user.logout"));
        itemLogout.setStyle("-fx-text-fill: #DC2626; -fx-font-weight: 700;");
        itemLogout.setOnAction(e -> cerrarSesion());

        userMenu.getItems().addAll(itemPerfil, itemConfig, sep, itemLogout);

        userSection.setOnMouseClicked(e -> {
            userMenu.show(userSection, Side.BOTTOM, 0, 5);
        });

        Tooltip tp = new Tooltip(idioma.get("menu.user.tooltip"));
        Tooltip.install(userSection, tp);

        header.getChildren().addAll(userSection);

        return header;
    }

    /**
     * Crea la barra lateral izquierda con el logo, las secciones y los botones para
     * acceder a cada módulo del sistema. También incluye la sección de
     * administración que solo se muestra si el usuario tiene permisos
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
        sidebar.getChildren().add(crearTituloSeccion(idioma.get("menu.section.operations")));
        sidebar.getChildren()
                .add(crearBotonMenu(idioma.get("menu.btn.products"), () -> productoView.show(stage, usuarioActual)));

        Region spacer1 = new Region();
        spacer1.setPrefHeight(10);
        sidebar.getChildren().add(spacer1);

        // HISTORIAL
        sidebar.getChildren().add(crearTituloSeccion(idioma.get("menu.section.history")));
        sidebar.getChildren()
                .add(crearBotonMenu(idioma.get("menu.btn.sales_history"),
                        () -> historialVentasView.show(stage, usuarioActual)));
        sidebar.getChildren()
                .add(crearBotonMenu(idioma.get("menu.btn.purchases_history"),
                        () -> historialComprasView.show(stage, usuarioActual)));

        Region spacer2 = new Region();
        spacer2.setPrefHeight(10);
        sidebar.getChildren().add(spacer2);

        // DIRECTORIO
        sidebar.getChildren().add(crearTituloSeccion(idioma.get("menu.section.directory")));
        sidebar.getChildren()
                .add(crearBotonMenu(idioma.get("menu.btn.suppliers"), () -> proveedorView.show(stage, usuarioActual)));
        sidebar.getChildren()
                .add(crearBotonMenu(idioma.get("menu.btn.clients"), () -> clienteView.show(stage, usuarioActual)));
        sidebar.getChildren().add(
                crearBotonMenu(idioma.get("menu.btn.manufacturers"), () -> fabricanteView.show(stage, usuarioActual)));
        sidebar.getChildren()
                .add(crearBotonMenu(idioma.get("menu.btn.competitors"), () -> empresaView.show(stage, usuarioActual)));
        sidebar.getChildren()
                .add(crearBotonMenu(idioma.get("menu.btn.service_providers"),
                        () -> prestadorServicioView.show(stage, usuarioActual)));

        // ADMIN
        if (usuarioService.tienePermiso(usuarioActual)) {
            Region spacerAdmin = new Region();
            spacerAdmin.setPrefHeight(10);
            sidebar.getChildren().add(spacerAdmin);
            sidebar.getChildren().add(crearTituloSeccion(idioma.get("menu.section.admin")));
            Button btnAdmin = crearBotonMenu(idioma.get("menu.btn.users"),
                    () -> usuarioView.show(stage, usuarioActual));
            sidebar.getChildren().add(btnAdmin);
        }
        return sidebar;
    }

    /**
     * Crea un label estilizado para usar como título de cada sección en la barra
     * lateral
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
     * Crea la pantalla de bienvenida que se muestra al iniciar sesión, con un
     * mensaje dinámico que incluye el nombre del usuario y un subtítulo motivador.
     * También se muestra el logo de la aplicación con un estilo moderno
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

        Label lblBienvenida = new Label(idioma.get("menu.welcome.title", usuarioActual));
        lblBienvenida.setStyle(
                "-fx-font-family: 'Segoe UI'; -fx-font-size: 24px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        Label lblSub = new Label(idioma.get("menu.welcome.subtitle"));
        lblSub.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-text-fill: #6B7280;");

        card.getChildren().addAll(imgLogo, lblBienvenida, lblSub);
        centro.getChildren().add(card);
        return centro;
    }

    /**
     * Crea un botón estilizado para usar en la barra lateral, con efectos de hover
     * y un indicador visual para el botón activo. El botón ejecuta la acción
     * proporcionada al hacer clic
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
     * Crea los botones flotantes para acceso rápido a las funciones de compra y
     * venta, con animaciones de aparición y desaparición, y un botón principal que
     * alterna su estado. Los botones secundarios solo se muestran cuando el menú
     * está abierto, y el fondo se oscurece para destacar la acción
     */
    private void crearBotonesFlotantes() {
        String styleMiniFab = "-fx-background-color: white; -fx-text-fill: #111827; -fx-font-weight: bold; -fx-background-radius: 50; -fx-min-width: 50; -fx-min-height: 50; -fx-max-width: 50; -fx-max-height: 50; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 6, 0, 0, 2); -fx-cursor: hand;";

        btnFabCompra = new Button("C");
        btnFabCompra.setTooltip(new Tooltip(idioma.get("menu.fab.purchase.tooltip")));
        btnFabCompra.setStyle(styleMiniFab);
        btnFabCompra.setVisible(false);
        btnFabCompra.setOnAction(e -> {
            toggleFabMenu();
            compraView.show(stage, usuarioActual);
        });

        btnFabVenta = new Button("V");
        btnFabVenta.setTooltip(new Tooltip(idioma.get("menu.fab.sale.tooltip")));
        btnFabVenta.setStyle(styleMiniFab);
        btnFabVenta.setVisible(false);
        btnFabVenta.setOnAction(e -> {
            toggleFabMenu();
            ventaView.show(stage, usuarioActual);
        });

        Label lblCompra = new Label(idioma.get("menu.fab.purchase.label"));
        lblCompra.setStyle(
                "-fx-text-fill: white; -fx-background-color: #111827; -fx-padding: 5 10 5 10; -fx-background-radius: 5;");
        lblCompra.setVisible(false);

        Label lblVenta = new Label(idioma.get("menu.fab.sale.label"));
        lblVenta.setStyle(
                "-fx-text-fill: white; -fx-background-color: #111827; -fx-padding: 5 10 5 10; -fx-background-radius: 5;");
        lblVenta.setVisible(false);

        HBox rowCompra = new HBox(10, lblCompra, btnFabCompra);
        rowCompra.setAlignment(Pos.CENTER_RIGHT);

        HBox rowVenta = new HBox(10, lblVenta, btnFabVenta);
        rowVenta.setAlignment(Pos.CENTER_RIGHT);

        btnFabMain = new Button("+");
        btnFabMain.setStyle("-fx-background-color: " + COLOR_PRIMARY
                + "; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-min-width: 60; -fx-min-height: 60; -fx-max-width: 60; -fx-max-height: 60; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 3); -fx-cursor: hand;");
        btnFabMain.setOnAction(e -> toggleFabMenu());

        fabContainer = new VBox(15, rowCompra, rowVenta, btnFabMain);
        fabContainer.setAlignment(Pos.BOTTOM_RIGHT);
        fabContainer.setPickOnBounds(false);
        rowCompra.setPickOnBounds(false);
        rowVenta.setPickOnBounds(false);
        fabContainer.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    /**
     * Alterna el estado del menú flotante, mostrando u ocultando los botones
     * secundarios y el overlay oscuro. También cambia el estilo del botón principal
     * para indicar si el menú está abierto o cerrado
     */
    private void toggleFabMenu() {
        menuFabAbierto = !menuFabAbierto;

        if (menuFabAbierto) {
            overlayOscuro.setVisible(true);
            mostrarMiniFab(btnFabVenta, ((HBox) btnFabVenta.getParent()).getChildren().get(0));
            mostrarMiniFab(btnFabCompra, ((HBox) btnFabCompra.getParent()).getChildren().get(0));

            btnFabMain.setText("-");
            btnFabMain.setStyle(
                    "-fx-background-color: #4B5563; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-min-width: 60; -fx-min-height: 60; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 3); -fx-cursor: hand;");

        } else {
            overlayOscuro.setVisible(false);
            ocultarMiniFab(btnFabVenta, ((HBox) btnFabVenta.getParent()).getChildren().get(0));
            ocultarMiniFab(btnFabCompra, ((HBox) btnFabCompra.getParent()).getChildren().get(0));

            btnFabMain.setText("+");
            btnFabMain.setStyle("-fx-background-color: " + COLOR_PRIMARY
                    + "; -fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold; -fx-background-radius: 50; -fx-min-width: 60; -fx-min-height: 60; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 3); -fx-cursor: hand;");
        }
    }

    /**
     * Muestra el botón flotante secundario con una animación de transición, y
     * también muestra la etiqueta asociada. El botón se mueve desde una posición
     * ligeramente desplazada hacia su posición final para dar un efecto de
     * aparición suave
     * 
     * @param btn
     * @param lbl
     */
    private void mostrarMiniFab(Button btn, javafx.scene.Node lbl) {
        btn.setVisible(true);
        lbl.setVisible(true);
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), btn);
        tt.setFromY(20);
        tt.setToY(0);
        tt.play();
    }

    /**
     * Oculta el botón flotante secundario y su etiqueta asociada, estableciendo su
     * visibilidad en false. Esto se utiliza para cerrar el menú flotante y ocultar
     * las opciones de compra y venta
     * 
     * @param btn
     * @param lbl
     */
    private void ocultarMiniFab(Button btn, javafx.scene.Node lbl) {
        btn.setVisible(false);
        lbl.setVisible(false);
    }

    /**
     * Cierra la sesión del usuario actual y muestra la pantalla de login. Esto se
     * llama desde el menú contextual del usuario en el header, y permite al usuario
     * salir de su cuenta y volver a la pantalla de inicio de sesión
     */
    private void cerrarSesion() {
        loginScreen.show(stage);
    }
}