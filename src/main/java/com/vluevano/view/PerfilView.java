package com.vluevano.view;

import com.vluevano.service.DialogService;
import com.vluevano.service.UsuarioService;
import com.vluevano.util.AppTheme;
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

    private Stage stage;
    private String usuarioActual;
    
    private TextField txtNombre;
    private PasswordField txtPassword;
    private Label lblMensaje;

    /**
     * MOSTRAR PANTALLA DE PERFIL
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");
        
        root.setTop(UIFactory.crearHeader("Mi Perfil", "Administra tu información personal", 
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
        stage.setTitle("PriceStocker | Mi Perfil");
        stage.show();
    }

    /**
     * CREAR FORMULARIO DE PERFIL
     * @return
     */
    private VBox crearFormularioPerfil() {
        VBox card = new VBox(25);
        card.setMaxWidth(450);
        card.setPadding(new Insets(40));
        card.setStyle(AppTheme.STYLE_CARD);
        card.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label("Editar Datos de Cuenta");
        lblTitulo.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 20px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        Label lblInfo = new Label("Puedes modificar tu nombre de usuario o actualizar tu contraseña si así lo deseas.");
        lblInfo.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
        lblInfo.setWrapText(true);

        txtNombre = UIFactory.crearInput("Nombre de usuario");
        txtNombre.setText(usuarioActual);

        txtPassword = new PasswordField();
        txtPassword.setPromptText("Nueva Contraseña");
        txtPassword.setStyle(AppTheme.STYLE_INPUT);

        Button btnGuardar = UIFactory.crearBotonPrimario("Guardar Cambios");
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> guardarCambios());

        lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        card.getChildren().addAll(
            lblTitulo, lblInfo, 
            new Separator(),
            UIFactory.crearGrupoInput("Usuario", txtNombre),
            UIFactory.crearGrupoInput("Contraseña", txtPassword),
            new Region() {{ setPrefHeight(10); }},
            btnGuardar, 
            lblMensaje
        );
        return card;
    }

    /**
     * GUARDAR CAMBIOS EN EL PERFIL
     */
    private void guardarCambios() {
        String nuevoNombre = txtNombre.getText();
        String nuevaPass = txtPassword.getText();
        String resultado = usuarioService.actualizarPerfil(usuarioActual, nuevoNombre, nuevaPass);

        if (resultado.contains("exitosamente")) {
            lblMensaje.setTextFill(Color.GREEN);
            this.usuarioActual = nuevoNombre;

            txtPassword.clear(); 
            
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Perfil Actualizado", 
                "Tus datos han sido actualizados.", stage);
        } else {
            lblMensaje.setTextFill(Color.RED);
        }
        lblMensaje.setText(resultado);
    }
}