package com.vluevano.view;

import com.vluevano.model.Usuario;
import com.vluevano.service.UsuarioService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.GestorIdioma;
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
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.List;
import java.util.Locale;

@Component
public class LoginScreen {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    @Autowired
    private GestorIdioma idioma;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    private Stage stage;
    private HBox rootLayout;

    /**
     * Muestra la pantalla de login
     * 
     * @param stage
     */
    public void show(Stage stage) {
        this.stage = stage;
        if (stage.isMaximized())
            stage.setMaximized(false);
        stage.setWidth(900);
        stage.setHeight(600);
        stage.setResizable(false);

        stage.setTitle("PriceStocker | " + idioma.get("login.window.title"));

        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/PriceStockerIcon.png")));
        } catch (Exception ignored) {
        }

        inicializarInterfaz();
    }

    /**
     * Inicializa toda la interfaz de la pantalla de login, creando el diseño base
     * con la imagen y el card
     */
    private void inicializarInterfaz() {
        rootLayout = new HBox();
        rootLayout.setStyle("-fx-background-color: white;");

        StackPane contenedorImagen = new StackPane();
        HBox.setHgrow(contenedorImagen, Priority.ALWAYS);
        try {
            Image bgImage = new Image(getClass().getResource("/images/bodega.jpg").toExternalForm());
            contenedorImagen.setBackground(
                    new Background(new BackgroundImage(bgImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER, new BackgroundSize(100, 100, true, true, false, true))));
            Region overlay = new Region();
            overlay.setStyle(
                    "-fx-background-color: linear-gradient(to bottom right, rgba(0,0,0,0.8), rgba(0,0,0,0.4));");
            contenedorImagen.getChildren().add(overlay);
        } catch (Exception e) {
            contenedorImagen.setStyle("-fx-background-color: #111827;");
        }

        VBox card = new VBox(25);
        card.setMinWidth(450);
        card.setMaxWidth(450);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(50));
        card.setStyle("-fx-background-color: white;");

        buildBienvenida(card);
        rootLayout.getChildren().addAll(contenedorImagen, card);

        StackPane mainContainer = new StackPane(rootLayout);

        Button btnIdioma = buildBotonIdioma();
        StackPane.setAlignment(btnIdioma, Pos.TOP_RIGHT);
        StackPane.setMargin(btnIdioma, new Insets(20));

        mainContainer.getChildren().add(btnIdioma);

        Scene scene = new Scene(mainContainer, 900, 600);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
        animarEntrada(card);
    }

    /**
     * Construye el botón de cambio de idioma con estilo de píldora y funcionalidad
     * para cambiar el idioma de la aplicación
     * 
     * @return
     */
    private Button buildBotonIdioma() {
        boolean isEs = idioma.getLocaleActual().getLanguage().equals("es");

        String textoBtn = isEs ? "🌐 ES" : "🌐 EN";
        Button btn = new Button(textoBtn);

        String normalStyle = "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-weight: bold; -fx-text-fill: #374151;";
        String hoverStyle = "-fx-background-color: #F3F4F6; -fx-border-color: #D1D5DB; -fx-border-radius: 20; -fx-background-radius: 20; -fx-padding: 8 15; -fx-cursor: hand; -fx-font-weight: bold; -fx-text-fill: #111827;";

        btn.setStyle(normalStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));

        btn.setOnAction(e -> {
            Locale nuevoIdioma = isEs ? new Locale("en") : new Locale("es");
            idioma.setIdioma(nuevoIdioma);

            stage.setTitle("PriceStocker | " + idioma.get("login.window.title"));

            inicializarInterfaz();
        });

        return btn;
    }

    /**
     * Construye la vista de bienvenida con el logo, título, subtítulo y botones
     * para iniciar o salir de la aplicación
     * 
     * @param card
     */
    private void buildBienvenida(VBox card) {
        ImageView imgLogo = new ImageView();
        try {
            imgLogo.setImage(new Image(getClass().getResourceAsStream("/images/PriceStockerLogo.png")));
            imgLogo.setFitWidth(180);
            imgLogo.setPreserveRatio(true);
        } catch (Exception ignored) {
        }

        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER);

        Label lblTitulo = new Label(idioma.get("login.welcome.title"));
        lblTitulo.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 32px; -fx-font-weight: 700; -fx-text-fill: "
                + AppTheme.COLOR_TEXT_MAIN + ";");

        Label lblSubtitulo = new Label(idioma.get("login.welcome.subtitle"));
        lblSubtitulo.setStyle("-fx-font-size: 15px; -fx-text-fill: #6B7280;");
        textContainer.getChildren().addAll(lblTitulo, lblSubtitulo);

        Region spacerTop = new Region();
        VBox.setVgrow(spacerTop, Priority.ALWAYS);
        Region spacerBottom = new Region();
        VBox.setVgrow(spacerBottom, Priority.ALWAYS);

        Button btnIniciar = UIFactory.crearBotonPrimario(idioma.get("login.btn.start"));
        btnIniciar.setPrefWidth(200);
        btnIniciar.setOnAction(e -> buildSeleccionUsuario(card));

        Button btnSalir = UIFactory.crearBotonSecundario(idioma.get("login.btn.exit"));
        btnSalir.setPrefWidth(200);
        btnSalir.setOnAction(e -> stage.close());

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        if (imgLogo.getImage() != null)
            content.getChildren().add(imgLogo);
        content.getChildren().addAll(textContainer, btnIniciar, btnSalir);

        String appVersion = (buildProperties != null) ? buildProperties.getVersion() : "Dev";
        Label lblFooter = new Label("© " + Year.now() + " vluevano_2003 | v" + appVersion);

        lblFooter.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 12px;");
        VBox footerBox = new VBox(lblFooter);
        footerBox.setAlignment(Pos.CENTER);

        card.getChildren().clear();
        card.getChildren().addAll(spacerTop, content, spacerBottom, footerBox);
    }

    /**
     * Construye la vista de selección de usuario, mostrando una lista de usuarios
     * disponibles y permitiendo seleccionar uno para iniciar sesión
     * 
     * @param card
     */
    private void buildSeleccionUsuario(VBox card) {
        animarSalida(card, () -> {
            card.getChildren().clear();
            card.setAlignment(Pos.CENTER_LEFT);

            Label lblSel = new Label(idioma.get("login.select.account"));
            lblSel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 26px; -fx-font-weight: 700; -fx-text-fill: "
                    + AppTheme.COLOR_TEXT_MAIN + ";");

            ListView<Usuario> listaUsuarios = new ListView<>();
            
            List<Usuario> usuariosActivos = usuarioService.consultarUsuarios().stream()
                    .filter(Usuario::getActivo)
                    .toList();
            listaUsuarios.getItems().addAll(usuariosActivos);
            
            listaUsuarios.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
            listaUsuarios.setPrefHeight(280);

            listaUsuarios.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Usuario item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                        setStyle("-fx-background-color: transparent;");
                    } else {
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
                            root.setStyle("-fx-background-color: #FFF7ED; -fx-border-color: " + AppTheme.COLOR_PRIMARY
                                    + "; -fx-border-radius: 8;");
                            circle.setFill(Color.web(AppTheme.COLOR_PRIMARY));
                            initial.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
                        } else {
                            root.setStyle(
                                    "-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 8;");
                            circle.setFill(Color.web("#F3F4F6"));
                            initial.setStyle("-fx-font-weight: bold; -fx-text-fill: #9CA3AF;");
                        }
                        setGraphic(root);
                    }
                }
            });

            Button btnContinuar = UIFactory.crearBotonPrimario(idioma.get("login.btn.continue"));
            btnContinuar.setMaxWidth(Double.MAX_VALUE);
            btnContinuar.setDisable(true);
            listaUsuarios.getSelectionModel().selectedItemProperty().addListener((o, old, newVal) -> {
                btnContinuar.setDisable(newVal == null);
                listaUsuarios.refresh();
            });
            btnContinuar.setOnAction(e -> buildVistaPassword(card,
                    listaUsuarios.getSelectionModel().getSelectedItem().getNombreUsuario()));

            Button btnVolver = UIFactory.crearBotonTexto("← " + idioma.get("login.btn.back"));
            btnVolver.setOnAction(e -> animacionInicio(card));

            card.getChildren().addAll(lblSel, listaUsuarios, btnContinuar, btnVolver);
            animarEntrada(card);
        });
    }

    /**
     * Construye la vista de ingreso de contraseña, mostrando el nombre del usuario
     * seleccionado y un campo para ingresar la contraseña, con validación al
     * intentar iniciar sesión
     * 
     * @param card
     * @param usuario
     */
    private void buildVistaPassword(VBox card, String usuario) {
        animarSalida(card, () -> {
            card.getChildren().clear();

            Label lblUser = new Label(idioma.get("login.hello.user", usuario));
            lblUser.setStyle(
                    "-fx-font-size: 28px; -fx-font-weight: 700; -fx-text-fill: " + AppTheme.COLOR_TEXT_MAIN + ";");

            PasswordField txtPass = new PasswordField();
            txtPass.setPromptText("••••••••");
            txtPass.setStyle(AppTheme.STYLE_INPUT);
            txtPass.setPrefHeight(50);

            Label lblError = new Label();
            lblError.setTextFill(Color.web(AppTheme.COLOR_ERROR));

            Button btnLogin = UIFactory.crearBotonPrimario(idioma.get("login.btn.access"));
            btnLogin.setMaxWidth(Double.MAX_VALUE);

            Runnable loginAction = () -> {
                btnLogin.setDisable(true);
                if (usuarioService.iniciarSesion(usuario, txtPass.getText()))
                    menuPrincipalScreen.show(stage, usuario);
                else {
                    lblError.setText(idioma.get("login.error.password"));
                    animarError(txtPass);
                    btnLogin.setDisable(false);
                }
            };

            btnLogin.setOnAction(e -> loginAction.run());
            txtPass.setOnAction(e -> loginAction.run());

            Button btnVolver = UIFactory.crearBotonTexto(idioma.get("login.btn.notme", usuario));
            btnVolver.setOnAction(e -> buildSeleccionUsuario(card));

            card.getChildren().addAll(lblUser, new Label(idioma.get("login.enter.password")), new Region(), txtPass,
                    lblError,
                    btnLogin, btnVolver);
            animarEntrada(card);
            Platform.runLater(txtPass::requestFocus);
        });
    }

    /**
     * Realiza una animación de salida en el card, ejecutando una acción al
     * finalizar la animación para construir la siguiente vista, y luego realiza una
     * animación de entrada para mostrar la nueva vista
     * 
     * @param card
     */
    private void animacionInicio(VBox card) {
        animarSalida(card, () -> {
            buildBienvenida(card);
            animarEntrada(card);
        });
    }

    /**
     * Realiza una animación de entrada combinando un fade-in y un slide-in desde la
     * derecha para el nodo especificado, creando una transición suave al mostrar
     * nuevos elementos en la interfaz
     * 
     * @param node
     */
    private void animarEntrada(Node node) {
        FadeTransition ft = new FadeTransition(Duration.millis(500), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        TranslateTransition tt = new TranslateTransition(Duration.millis(500), node);
        tt.setFromX(20);
        tt.setToX(0);
        new ParallelTransition(ft, tt).play();
    }

    /**
     * Realiza una animación de salida combinando un fade-out y un slide-out hacia
     * la izquierda para el nodo especificado, ejecutando una acción al finalizar la
     * animación para construir la siguiente vista o realizar cualquier otra tarea
     * necesaria antes de mostrar nuevos elementos en la interfaz
     * 
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
     * Realiza una animación de error aplicando un efecto de shake al nodo
     * especificado, moviéndolo rápidamente hacia los lados para indicar visualmente
     * que ha ocurrido un error, como una contraseña incorrecta, y llamar la
     * atención del usuario sobre el campo que requiere corrección
     * 
     * @param node
     */
    private void animarError(Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setByX(10);
        tt.setCycleCount(5);
        tt.setAutoReverse(true);
        tt.play();
    }
}