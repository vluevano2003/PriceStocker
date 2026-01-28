package com.vluevano.view;

import com.vluevano.service.UsuarioService;
import com.vluevano.model.Usuario;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoginScreen {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private HBox rootLayout;

    // COLORES Y ESTILOS
    private static final String COLOR_PRIMARY = "#F97316";
    private static final String COLOR_PRIMARY_HOVER = "#EA580C";
    private static final String COLOR_BG_INPUT = "#FFFFFF";
    private static final String COLOR_BORDER_INPUT = "#E5E7EB";
    private static final String COLOR_TEXT_MAIN = "#111827";
    private static final String COLOR_TEXT_SECONDARY = "#6B7280";

    private static final String FONT_TITLE = "-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 32px; -fx-font-weight: 700; -fx-text-fill: "
            + COLOR_TEXT_MAIN + ";";
    private static final String FONT_SUBTITLE = "-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 15px; -fx-text-fill: "
            + COLOR_TEXT_SECONDARY + ";";
    private static final String STYLE_INPUT_NORMAL = "-fx-background-color: " + COLOR_BG_INPUT + "; -fx-border-color: "
            + COLOR_BORDER_INPUT
            + "; -fx-border-width: 1.5; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 14; -fx-font-size: 14px; -fx-text-fill: #374151;";
    private static final String STYLE_INPUT_FOCUS = "-fx-background-color: #FFFFFF; -fx-border-color: " + COLOR_PRIMARY
            + "; -fx-border-width: 1.5; -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 14; -fx-font-size: 14px; -fx-text-fill: #374151;";

    /**
     * Muestra la ventana de Login
     * @param stage
     */
    public void show(Stage stage) {
        this.stage = stage;
        
        // Ajustar tamaño inicial
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        }

        stage.setWidth(900);
        stage.setHeight(600);
        stage.setResizable(false);
        configurarVentana();
        inicializarInterfaz();
    }

    /**
     * Configura propiedades de la ventana
     */
    private void configurarVentana() {
        stage.setTitle("PriceStocker | Acceso");
        stage.setResizable(false);
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/PriceStockerIcon.png")));
        } catch (Exception ignored) {
        }
    }

    /**
     * Inicializa los componentes de la interfaz
     */
    private void inicializarInterfaz() {
        rootLayout = new HBox();
        rootLayout.setStyle("-fx-background-color: white;");

        // Sección izquierda
        StackPane contenedorImagen = new StackPane();
        HBox.setHgrow(contenedorImagen, Priority.ALWAYS);

        try {
            Image bgImage = new Image(getClass().getResource("/images/bodega.jpg").toExternalForm());
            BackgroundImage bg = new BackgroundImage(bgImage,
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(100, 100, true, true, false, true)); // Cover
            contenedorImagen.setBackground(new Background(bg));

            Region overlay = new Region();
            overlay.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, rgba(0,0,0,0.8), rgba(0,0,0,0.4));");
            contenedorImagen.getChildren().add(overlay);
        } catch (Exception e) {
            contenedorImagen.setStyle("-fx-background-color: #111827;");
        }

        // Sección derecha
        VBox card = new VBox(25);
        card.setMinWidth(450);
        card.setMaxWidth(450);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(50));
        card.setStyle("-fx-background-color: white;");

        buildBienvenida(card);

        rootLayout.getChildren().addAll(contenedorImagen, card);

        Scene scene = new Scene(rootLayout, 900, 600);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();

        animarEntrada(card);
    }

    /**
     * Construye la pantalla de bienvenida
     * @param card
     */
    private void buildBienvenida(VBox card) {
        ImageView imgLogo = new ImageView();
        try {
            imgLogo.setImage(new Image(getClass().getResourceAsStream("/images/PriceStockerLogo.png")));
            imgLogo.setFitWidth(180);
            imgLogo.setPreserveRatio(true);
        } catch (Exception e) {
            Label logoText = new Label("PRICESTOCKER");
            logoText.setStyle("-fx-font-weight: 900; -fx-font-size: 24px; -fx-text-fill: " + COLOR_PRIMARY + ";");
            if (!card.getChildren().contains(logoText))
                card.getChildren().add(logoText);
        }

        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER);
        Label lblTitulo = new Label("Bienvenido");
        lblTitulo.setStyle(FONT_TITLE);
        Label lblSubtitulo = new Label("Gestión inteligente para tu inventario");
        lblSubtitulo.setStyle(FONT_SUBTITLE);
        textContainer.getChildren().addAll(lblTitulo, lblSubtitulo);

        Region spacerTop = new Region();
        VBox.setVgrow(spacerTop, Priority.ALWAYS);

        Button btnIniciar = crearBotonPrimario("Iniciar Sesión", false);
        btnIniciar.setOnAction(e -> buildSeleccionUsuario(card));

        Button btnSalir = crearBotonSecundario("Salir");
        btnSalir.setOnAction(e -> stage.close());

        Region spacerBottom = new Region();
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        Label copyright = new Label("© 2026 Vluevano Systems");
        copyright.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        VBox footer = new VBox(copyright);
        footer.setAlignment(Pos.CENTER);

        VBox contentCenter = new VBox(30);
        contentCenter.setAlignment(Pos.CENTER);
        if (imgLogo.getImage() != null)
            contentCenter.getChildren().add(imgLogo);
        contentCenter.getChildren().addAll(textContainer, btnIniciar, btnSalir);

        card.getChildren().clear();
        card.getChildren().addAll(spacerTop, contentCenter, spacerBottom, footer);
        card.setAlignment(Pos.CENTER_LEFT);
    }

    /**
     * Construye la pantalla de selección de usuario
     * @param card
     */
    private void buildSeleccionUsuario(VBox card) {
        animarSalida(card, () -> {
            card.getChildren().clear();
            card.setAlignment(Pos.CENTER_LEFT);

            Label lblSel = new Label("Selecciona tu cuenta");
            lblSel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: "
                    + COLOR_TEXT_MAIN + ";");

            Label lblSub = new Label("Elige un perfil para continuar");
            lblSub.setStyle(FONT_SUBTITLE);

            ListView<Usuario> listaUsuarios = new ListView<>();
            List<Usuario> usuarios = usuarioService.consultarUsuarios();
            listaUsuarios.getItems().addAll(usuarios);

            listaUsuarios.setStyle(
                    "-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-padding: 0;");
            listaUsuarios.setPrefHeight(280);

            listaUsuarios.setCellFactory(lv -> new ListCell<Usuario>() {
                private final HBox root = new HBox(15);
                private final Label nameLabel = new Label();
                private final Circle avatar = new Circle(18, Color.web("#F3F4F6"));
                private final Label initial = new Label();
                private final StackPane avatarContainer = new StackPane(avatar, initial);

                {
                    root.setAlignment(Pos.CENTER_LEFT);
                    root.setPadding(new Insets(10));
                    root.setStyle(
                            "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-background-radius: 8;");
                    nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #374151;");
                    initial.setStyle("-fx-font-weight: bold; -fx-text-fill: #9CA3AF;");
                    root.getChildren().addAll(avatarContainer, nameLabel);
                }

                @Override
                protected void updateItem(Usuario item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
                        nameLabel.setText(item.getNombreUsuario());
                        String letra = item.getNombreUsuario().substring(0, 1).toUpperCase();
                        initial.setText(letra);

                        if (isSelected()) {
                            root.setStyle("-fx-background-color: #FFF7ED; -fx-border-color: " + COLOR_PRIMARY
                                    + "; -fx-border-radius: 8; -fx-background-radius: 8;");
                            avatar.setFill(Color.web(COLOR_PRIMARY));
                            initial.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                        } else {
                            root.setStyle(
                                    "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-background-radius: 8;");
                            avatar.setFill(Color.web("#F3F4F6"));
                            initial.setStyle("-fx-text-fill: #9CA3AF; -fx-font-weight: bold;");
                        }
                        setGraphic(root);
                        setStyle("-fx-background-color: transparent; -fx-padding: 4 0 4 0;");
                    }
                }
            });

            Button btnContinuar = crearBotonPrimario("Continuar", true);
            btnContinuar.setDisable(true);

            listaUsuarios.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                btnContinuar.setDisable(newVal == null);
                listaUsuarios.refresh();
            });

            btnContinuar.setOnAction(e -> {
                Usuario seleccionado = listaUsuarios.getSelectionModel().getSelectedItem();
                if (seleccionado != null)
                    buildVistaPassword(card, seleccionado.getNombreUsuario());
            });

            Button btnVolver = crearBotonTexto("← Volver atrás");
            btnVolver.setOnAction(e -> animacionInicio(card));

            VBox container = new VBox(15);
            container.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().addAll(lblSel, lblSub, listaUsuarios, btnContinuar, btnVolver);

            card.getChildren().add(container);
            animarEntrada(card);
        });
    }

    /**
     * Construye la vista de ingreso de contraseña
     * @param card
     * @param usuario
     */
    private void buildVistaPassword(VBox card, String usuario) {
        animarSalida(card, () -> {
            card.getChildren().clear();

            VBox header = new VBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            Label lblUser = new Label("Hola, " + usuario);
            lblUser.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: "
                    + COLOR_TEXT_MAIN + ";");

            Label lblInstr = new Label("Por favor ingresa tu contraseña.");
            lblInstr.setStyle(FONT_SUBTITLE);
            header.getChildren().addAll(lblUser, lblInstr);

            PasswordField txtPass = new PasswordField();
            txtPass.setPromptText("••••••••");
            txtPass.setStyle(STYLE_INPUT_NORMAL);
            txtPass.setPrefHeight(50);

            DropShadow glow = new DropShadow();
            glow.setColor(Color.web(COLOR_PRIMARY, 0.25));
            glow.setWidth(15);
            glow.setHeight(15);
            glow.setSpread(0.1);

            txtPass.focusedProperty().addListener((obs, oldVal, newVal) -> {
                txtPass.setStyle(newVal ? STYLE_INPUT_FOCUS : STYLE_INPUT_NORMAL);
                txtPass.setEffect(newVal ? glow : null);
            });

            Label lblError = new Label();
            lblError.setTextFill(Color.web("#DC2626"));
            lblError.setStyle("-fx-font-size: 13px; -fx-font-weight: 600;");
            lblError.setMinHeight(20);

            Button btnLogin = crearBotonPrimario("Acceder al Dashboard", true);

            Runnable loginAction = () -> {
                btnLogin.setDisable(true);
                btnLogin.setText("Verificando...");

                if (usuarioService.iniciarSesion(usuario, txtPass.getText())) {
                    menuPrincipalScreen.show(stage, usuario);
                } else {
                    lblError.setText("La contraseña es incorrecta.");
                    animarError(txtPass);
                    btnLogin.setDisable(false);
                    btnLogin.setText("Acceder al Dashboard");
                }
            };

            btnLogin.setOnAction(e -> loginAction.run());
            txtPass.setOnAction(e -> loginAction.run());

            Button btnVolver = crearBotonTexto("No soy " + usuario);
            btnVolver.setOnAction(e -> buildSeleccionUsuario(card));

            Region spacer = new Region();
            spacer.setPrefHeight(20);

            VBox form = new VBox(20);
            form.setAlignment(Pos.CENTER_LEFT);
            form.getChildren().addAll(header, spacer, txtPass, lblError, btnLogin, btnVolver);

            card.getChildren().add(form);
            animarEntrada(card);
            Platform.runLater(txtPass::requestFocus);
        });
    }

    /**
     * Animación de transición de regreso a la pantalla de inicio
     * @param card
     */
    private void animacionInicio(VBox card) {
        animarSalida(card, () -> {
            buildBienvenida(card);
            animarEntrada(card);
        });
    }

    /**
     * Botón primario
     * @param texto
     * @param isFullWidth
     * @return
     */
    private Button crearBotonPrimario(String texto, boolean isFullWidth) {
        Button btn = new Button(texto);
        if (isFullWidth)
            btn.setMaxWidth(Double.MAX_VALUE);
        else
            btn.setPrefWidth(200);

        btn.setPrefHeight(50);
        String styleBase = "-fx-background-color: " + COLOR_PRIMARY
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 10; -fx-font-size: 15px; -fx-cursor: hand;";
        String styleHover = "-fx-background-color: " + COLOR_PRIMARY_HOVER
                + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 10; -fx-font-size: 15px; -fx-cursor: hand;";

        btn.setStyle(styleBase);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(249, 115, 22, 0.4));
        shadow.setOffsetY(4);
        shadow.setRadius(10);
        btn.setEffect(shadow);

        btn.setOnMouseEntered(e -> {
            btn.setStyle(styleHover);
            shadow.setOffsetY(6);
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(styleBase);
            shadow.setOffsetY(4);
        });
        return btn;
    }

    /**
     * Botón secundario
     * @param texto
     * @return
     */
    private Button crearBotonSecundario(String texto) {
        Button btn = new Button(texto);
        btn.setPrefWidth(200);
        btn.setPrefHeight(50);
        String styleBase = "-fx-background-color: transparent; -fx-border-color: #D1D5DB; -fx-border-width: 1.5; -fx-text-fill: #4B5563; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-weight: 600; -fx-font-size: 14px; -fx-cursor: hand;";
        String styleHover = "-fx-background-color: #F3F4F6; -fx-border-color: #9CA3AF; -fx-border-width: 1.5; -fx-text-fill: #111827; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-weight: 600; -fx-font-size: 14px; -fx-cursor: hand;";
        btn.setStyle(styleBase);
        btn.setOnMouseEntered(e -> btn.setStyle(styleHover));
        btn.setOnMouseExited(e -> btn.setStyle(styleBase));
        return btn;
    }

    /**
     * Botón de texto simple
     * @param texto
     * @return
     */
    private Button crearBotonTexto(String texto) {
        Button btn = new Button(texto);
        btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-size: 14px; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + COLOR_PRIMARY
                + "; -fx-font-size: 14px; -fx-cursor: hand; -fx-underline: true;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #6B7280; -fx-font-size: 14px; -fx-cursor: hand;"));
        return btn;
    }

    /**
     * Animación de entrada
     * @param node
     */
    private void animarEntrada(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), node);
        tt.setFromX(20);
        tt.setToX(0);
        ParallelTransition pt = new ParallelTransition(ft, tt);
        pt.play();
    }

    /**
     * Animación de salida
     * @param node
     * @param onFinished
     */
    private void animarSalida(Node node, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> onFinished.run());
        ft.play();
    }

    /**
     * Animación de error (temblor)
     * @param node
     */
    private void animarError(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setByX(10);
        tt.setCycleCount(5);
        tt.setAutoReverse(true);
        String originalStyle = node.getStyle();
        node.setStyle(originalStyle + "-fx-border-color: #DC2626;");
        tt.setOnFinished(e -> node.setStyle(originalStyle));
        tt.play();
    }
}