package com.vluevano.view;

import com.vluevano.model.Usuario;
import com.vluevano.service.UsuarioService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

@Component
public class LoginScreen {

    @Autowired private UsuarioService usuarioService;
    @Autowired @Lazy private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;
    private HBox rootLayout;

    /**
     * Muestra la pantalla de login
     * @param stage
     */
    public void show(Stage stage) {
        this.stage = stage;
        if (stage.isMaximized()) stage.setMaximized(false);
        stage.setWidth(900); stage.setHeight(600);
        stage.setResizable(false);
        stage.setTitle("PriceStocker | Acceso");
        try { stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/PriceStockerIcon.png"))); } catch (Exception ignored) {}

        inicializarInterfaz();
    }

    /**
     * Inicializa la interfaz de usuario
     */
    private void inicializarInterfaz() {
        rootLayout = new HBox();
        rootLayout.setStyle("-fx-background-color: white;");

        StackPane contenedorImagen = new StackPane();
        HBox.setHgrow(contenedorImagen, Priority.ALWAYS);
        try {
            Image bgImage = new Image(getClass().getResource("/images/bodega.jpg").toExternalForm());
            contenedorImagen.setBackground(new Background(new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, false, true))));
            Region overlay = new Region();
            overlay.setStyle("-fx-background-color: linear-gradient(to bottom right, rgba(0,0,0,0.8), rgba(0,0,0,0.4));");
            contenedorImagen.getChildren().add(overlay);
        } catch (Exception e) {
            contenedorImagen.setStyle("-fx-background-color: #111827;");
        }

        VBox card = new VBox(25);
        card.setMinWidth(450); card.setMaxWidth(450);
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
     * Construye la vista de bienvenida
     * @param card
     */
    private void buildBienvenida(VBox card) {
        ImageView imgLogo = new ImageView();
        try {
            imgLogo.setImage(new Image(getClass().getResourceAsStream("/images/PriceStockerLogo.png")));
            imgLogo.setFitWidth(180);
            imgLogo.setPreserveRatio(true);
        } catch (Exception ignored) { }

        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER);
        Label lblTitulo = new Label("Bienvenido");
        lblTitulo.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 32px; -fx-font-weight: 700; -fx-text-fill: " + AppTheme.COLOR_TEXT_MAIN + ";");
        Label lblSubtitulo = new Label("Gestión inteligente para tu inventario");
        lblSubtitulo.setStyle("-fx-font-size: 15px; -fx-text-fill: #6B7280;");
        textContainer.getChildren().addAll(lblTitulo, lblSubtitulo);

        Region spacerTop = new Region(); VBox.setVgrow(spacerTop, Priority.ALWAYS);
        Region spacerBottom = new Region(); VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        Button btnIniciar = UIFactory.crearBotonPrimario("Iniciar Sesión");
        btnIniciar.setPrefWidth(200);
        btnIniciar.setOnAction(e -> buildSeleccionUsuario(card));

        Button btnSalir = UIFactory.crearBotonSecundario("Salir");
        btnSalir.setPrefWidth(200);
        btnSalir.setOnAction(e -> stage.close());

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        if (imgLogo.getImage() != null) content.getChildren().add(imgLogo);
        content.getChildren().addAll(textContainer, btnIniciar, btnSalir);

        Label lblFooter = new Label("© 2026 Vluevano Systems");
        lblFooter.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        VBox footerBox = new VBox(lblFooter);
        footerBox.setAlignment(Pos.CENTER);

        card.getChildren().clear();
        card.getChildren().addAll(spacerTop, content, spacerBottom, footerBox);
    }

    /**
     *  Construye la vista de selección de usuario
     * @param card
     */
    private void buildSeleccionUsuario(VBox card) {
        animarSalida(card, () -> {
            card.getChildren().clear();
            card.setAlignment(Pos.CENTER_LEFT);

            Label lblSel = new Label("Selecciona tu cuenta");
            lblSel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: " + AppTheme.COLOR_TEXT_MAIN + ";");
            
            ListView<Usuario> listaUsuarios = new ListView<>();
            listaUsuarios.getItems().addAll(usuarioService.consultarUsuarios());
            listaUsuarios.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
            listaUsuarios.setPrefHeight(280);
            
            listaUsuarios.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Usuario item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setGraphic(null); setStyle("-fx-background-color: transparent;"); }
                    else {
                        // Lógica de iniciales
                        String letra = "";
                        if (item.getNombreUsuario() != null && !item.getNombreUsuario().isEmpty()) {
                            letra = item.getNombreUsuario().substring(0, 1).toUpperCase();
                        }
                        
                        Label initial = new Label(letra);
                        Circle circle = new Circle(18);
                        
                        Label name = new Label(item.getNombreUsuario());
                        name.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #374151;");
                        
                        StackPane avatar = new StackPane(circle, initial);
                        
                        HBox root = new HBox(12, avatar, name);
                        root.setPadding(new Insets(10));
                        root.setAlignment(Pos.CENTER_LEFT);
                        
                        if (isSelected()) {
                            root.setStyle("-fx-background-color: #FFF7ED; -fx-border-color: " + AppTheme.COLOR_PRIMARY + "; -fx-border-radius: 8;");
                            circle.setFill(Color.web(AppTheme.COLOR_PRIMARY));
                            initial.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                        } else {
                            root.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8;");
                            circle.setFill(Color.web("#F3F4F6"));
                            initial.setStyle("-fx-font-weight: bold; -fx-text-fill: #9CA3AF;");
                        }
                        setGraphic(root);
                    }
                }
            });

            Button btnContinuar = UIFactory.crearBotonPrimario("Continuar");
            btnContinuar.setMaxWidth(Double.MAX_VALUE);
            btnContinuar.setDisable(true);
            listaUsuarios.getSelectionModel().selectedItemProperty().addListener((o, old, newVal) -> {
                btnContinuar.setDisable(newVal == null);
                listaUsuarios.refresh();
            });
            btnContinuar.setOnAction(e -> buildVistaPassword(card, listaUsuarios.getSelectionModel().getSelectedItem().getNombreUsuario()));

            Button btnVolver = UIFactory.crearBotonTexto("← Volver atrás");
            btnVolver.setOnAction(e -> animacionInicio(card));

            card.getChildren().addAll(lblSel, listaUsuarios, btnContinuar, btnVolver);
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
            
            Label lblUser = new Label("Hola, " + usuario);
            lblUser.setStyle("-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: " + AppTheme.COLOR_TEXT_MAIN + ";");
            
            PasswordField txtPass = new PasswordField();
            txtPass.setPromptText("••••••••");
            txtPass.setStyle(AppTheme.STYLE_INPUT);
            txtPass.setPrefHeight(50);
            
            Label lblError = new Label();
            lblError.setTextFill(Color.web(AppTheme.COLOR_ERROR));

            Button btnLogin = UIFactory.crearBotonPrimario("Acceder al Dashboard");
            btnLogin.setMaxWidth(Double.MAX_VALUE);
            
            Runnable loginAction = () -> {
                btnLogin.setDisable(true);
                if (usuarioService.iniciarSesion(usuario, txtPass.getText())) menuPrincipalScreen.show(stage, usuario);
                else {
                    lblError.setText("La contraseña es incorrecta.");
                    animarError(txtPass);
                    btnLogin.setDisable(false);
                }
            };
            
            btnLogin.setOnAction(e -> loginAction.run());
            txtPass.setOnAction(e -> loginAction.run());

            Button btnVolver = UIFactory.crearBotonTexto("No soy " + usuario);
            btnVolver.setOnAction(e -> buildSeleccionUsuario(card));

            card.getChildren().addAll(lblUser, new Label("Ingresa tu contraseña"), new Region(), txtPass, lblError, btnLogin, btnVolver);
            animarEntrada(card);
            Platform.runLater(txtPass::requestFocus);
        });
    }

    /**
     * Animación de inicio
     * @param card
     */
    private void animacionInicio(VBox card) {
        animarSalida(card, () -> { buildBienvenida(card); animarEntrada(card); });
    }

    /**
     * Animación de entrada
     * @param node
     */
    private void animarEntrada(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        ft.setFromValue(0.0); ft.setToValue(1.0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), node);
        tt.setFromX(20); tt.setToX(0);
        new ParallelTransition(ft, tt).play();
    }

    /**
     * Animación de salida
     * @param node
     * @param onFinished
     */
    private void animarSalida(Node node, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> onFinished.run());
        ft.play();
    }

    /**
     * Animación de error
     * @param node
     */
    private void animarError(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setByX(10); tt.setCycleCount(5); tt.setAutoReverse(true);
        tt.play();
    }
}