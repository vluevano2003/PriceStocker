package com.vluevano.view;

import com.vluevano.service.DialogService;
import com.vluevano.service.UsuarioService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.GestorIdioma;
import com.vluevano.util.UIFactory;
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
public class PerfilView {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private DialogService dialogService;
    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;
    @Autowired
    private GestorIdioma idioma;

    private Stage stage;
    private String usuarioActual;
    
    private TextField txtNombre;
    private PasswordField txtPassword;
    private Label lblMensaje;

    /**
     * Muestra la pantalla de perfil del usuario
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");
        
        root.setTop(UIFactory.crearHeader(
            idioma.get("profile.header.title"), 
            idioma.get("profile.header.subtitle"), 
            () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        VBox contenido = crearFormularioPerfil();
        StackPane centro = new StackPane(contenido);
        centro.setPadding(new Insets(40));
        root.setCenter(centro);

        if (stage.getScene() != null) {
            stage.getScene().setRoot(root);
        } else {
            Scene scene = new Scene(root, 1200, 768);
            stage.setScene(scene);
        }
        stage.setTitle("PriceStocker | " + idioma.get("profile.window.title"));
        stage.show();
    }

    /**
     * Crea el formulario para editar el perfil del usuario
     * @return
     */
    private VBox crearFormularioPerfil() {
        VBox card = new VBox(25);
        card.setMaxWidth(450);
        card.setPadding(new Insets(40));
        card.setStyle(AppTheme.STYLE_CARD);
        card.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label(idioma.get("profile.card.title"));
        lblTitulo.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        Label lblInfo = new Label(idioma.get("profile.card.info"));
        lblInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
        lblInfo.setWrapText(true);

        txtNombre = UIFactory.crearInput(idioma.get("profile.placeholder.user"));
        txtNombre.setText(usuarioActual);

        txtPassword = new PasswordField();
        txtPassword.setPromptText(idioma.get("profile.placeholder.pass"));
        txtPassword.setStyle(AppTheme.STYLE_INPUT);

        Button btnGuardar = UIFactory.crearBotonPrimario(idioma.get("profile.btn.save"));
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> guardarCambios());

        lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        card.getChildren().addAll(
            lblTitulo, lblInfo, 
            new Separator(),
            UIFactory.crearGrupoInput(idioma.get("profile.lbl.user"), txtNombre),
            UIFactory.crearGrupoInput(idioma.get("profile.lbl.pass"), txtPassword),
            new Region() {{ setPrefHeight(10); }},
            btnGuardar, 
            lblMensaje
        );
        return card;
    }

    /**
     * Lógica para guardar los cambios del perfil del usuario
     */
    private void guardarCambios() {
        String nuevoNombre = txtNombre.getText();
        String nuevaPass = txtPassword.getText();
        String resultado = usuarioService.actualizarPerfil(usuarioActual, nuevoNombre, nuevaPass);

        if (resultado.contains("exitosamente")) {
            lblMensaje.setTextFill(Color.GREEN);
            this.usuarioActual = nuevoNombre;

            txtPassword.clear(); 
            
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, 
                idioma.get("profile.msg.success.title"), 
                idioma.get("profile.msg.success.content"), stage);
        } else {
            lblMensaje.setTextFill(Color.RED);
        }
        lblMensaje.setText(resultado);
    }
}