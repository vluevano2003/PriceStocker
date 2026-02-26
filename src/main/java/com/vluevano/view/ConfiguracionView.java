package com.vluevano.view;

import com.vluevano.service.BackupService;
import com.vluevano.service.DialogService;
import com.vluevano.service.MonedaService;
import com.vluevano.service.SupabaseAuthService;
import com.vluevano.service.UsuarioService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.GestorIdioma;
import com.vluevano.util.UIFactory;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Locale;
import java.util.prefs.Preferences;

@Component
public class ConfiguracionView {

    @Autowired
    private MonedaService monedaService;

    @Autowired
    private DialogService dialogService;

    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    @Autowired
    private GestorIdioma idioma;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private SupabaseAuthService supabaseAuthService;

    @Autowired
    private BackupService backupService;
    
    private String tokenSupabase = null;

    private Stage stage;
    private String usuarioActual;

    private Label lblValorActual;
    private ComboBox<String> cmbMonedaDefault;
    private ComboBox<String> cmbIdiomaDefault;
    private Button btnActualizarInternet;

    private boolean esModoLogin = true;

    /**
     * Muestra la pantalla de configuración
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;
        monedaService.inicializar();

        BorderPane root = new BorderPane();

        root.setTop(UIFactory.crearHeader(
                idioma.get("config.header.title"),
                idioma.get("config.header.subtitle"),
                () -> menuPrincipalScreen.show(stage, usuarioActual)));

        HBox mainContainer = new HBox(40); 
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");
        mainContainer.setAlignment(Pos.TOP_CENTER);

        VBox leftColumn = new VBox(25);
        leftColumn.getChildren().addAll(crearTarjetaMoneda(), crearTarjetaPreferencias());

        mainContainer.getChildren().add(leftColumn);

        if (usuarioService.tienePermiso(usuarioActual)) {
            VBox rightColumn = new VBox(25);
            rightColumn.getChildren().add(crearTarjetaNube());
            mainContainer.getChildren().add(rightColumn);
        }

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + "; -fx-background: "
                + AppTheme.COLOR_BG_LIGHT + ";");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("PriceStocker | " + idioma.get("config.window.title"));
    }

    /**
     * Crea la tarjeta de tipo de cambio
     * @return
     */
    private VBox crearTarjetaMoneda() {
        VBox card = new VBox(15);
        card.setStyle(AppTheme.STYLE_CARD);
        card.setMaxWidth(600);
        card.setPadding(new Insets(25));

        Label lblTitulo = new Label(idioma.get("config.card.currency.title"));
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        VBox valorBox = new VBox(5);
        valorBox.setAlignment(Pos.CENTER);

        Label lblSubtituloValor = new Label(idioma.get("config.card.currency.current"));
        lblSubtituloValor.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-font-weight: 600;");

        lblValorActual = new Label("$" + String.format("%.2f", monedaService.getTipoCambioActual()));
        lblValorActual.setStyle("-fx-font-size: 42px; -fx-font-weight: 800; -fx-text-fill: #059669;");

        valorBox.getChildren().addAll(lblSubtituloValor, lblValorActual);

        btnActualizarInternet = UIFactory.crearBotonSecundario(idioma.get("config.btn.update_api"));
        btnActualizarInternet.setMaxWidth(Double.MAX_VALUE);
        btnActualizarInternet.setOnAction(e -> actualizarDesdeInternet());
        card.getChildren().addAll(lblTitulo, valorBox, btnActualizarInternet);
        return card;
    }

    /**
     * Crea la tarjeta de preferencias (moneda por defecto e idioma)
     * @return
     */
    private VBox crearTarjetaPreferencias() {
        VBox card = new VBox(15);
        card.setStyle(AppTheme.STYLE_CARD);
        card.setMaxWidth(600);
        card.setPadding(new Insets(25));

        Label lblTitulo = new Label(idioma.get("config.card.prefs.title"));
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        cmbMonedaDefault = new ComboBox<>();
        cmbMonedaDefault.getItems().addAll("MXN", "USD");
        cmbMonedaDefault.setValue(monedaService.getMonedaPorDefecto());
        cmbMonedaDefault.setMaxWidth(Double.MAX_VALUE);
        cmbMonedaDefault.setStyle(AppTheme.STYLE_INPUT);

        cmbIdiomaDefault = new ComboBox<>();
        cmbIdiomaDefault.getItems().addAll("Español", "English");

        if (idioma.getLocaleActual().getLanguage().equals("es")) {
            cmbIdiomaDefault.setValue("Español");
        } else {
            cmbIdiomaDefault.setValue("English");
        }
        cmbIdiomaDefault.setMaxWidth(Double.MAX_VALUE);
        cmbIdiomaDefault.setStyle(AppTheme.STYLE_INPUT);

        Button btnGuardarPrefs = UIFactory.crearBotonPrimario(idioma.get("config.btn.save_prefs"));
        btnGuardarPrefs.setMaxWidth(Double.MAX_VALUE);
        btnGuardarPrefs.setOnAction(e -> {
            monedaService.setMonedaPorDefecto(cmbMonedaDefault.getValue());

            boolean cambioIdioma = false;
            String seleccion = cmbIdiomaDefault.getValue();
            if (seleccion.equals("Español") && !idioma.getLocaleActual().getLanguage().equals("es")) {
                idioma.setIdioma(new Locale("es"));
                cambioIdioma = true;
            } else if (seleccion.equals("English") && !idioma.getLocaleActual().getLanguage().equals("en")) {
                idioma.setIdioma(new Locale("en"));
                cambioIdioma = true;
            }

            if (cambioIdioma) {
                show(stage, usuarioActual);
            }
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION,
                    idioma.get("config.msg.saved.title"),
                    idioma.get("config.msg.saved.content"),
                    stage);
        });

        card.getChildren().addAll(
                lblTitulo,
                UIFactory.crearGrupoInput(idioma.get("config.lbl.default_currency"), cmbMonedaDefault),
                UIFactory.crearGrupoInput(idioma.get("config.lbl.language"), cmbIdiomaDefault),
                new Region(),
                btnGuardarPrefs);
        return card;
    }

    /**
     * Crea la tarjeta de integración con la nube (Supabase)
     * @return
     */
    private VBox crearTarjetaNube() {
        VBox card = new VBox(15);
        card.setStyle(AppTheme.STYLE_CARD);
        card.setPrefWidth(350);
        card.setPadding(new Insets(25));

        Label lblTitulo = new Label(idioma.get("config.card.cloud.title"));
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        
        Label lblInfo = new Label(idioma.get("config.card.cloud.info"));
        lblInfo.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-wrap-text: true;");

        Preferences prefs = Preferences.userNodeForPackage(ConfiguracionView.class);
        boolean autoBackupActivado = prefs.getBoolean("AUTO_BACKUP", false);
        String savedEmail = prefs.get("SUPA_EMAIL", "");
        String savedPass = prefs.get("SUPA_PASS", "");

        TextField txtEmail = new TextField(savedEmail);
        txtEmail.setPromptText(idioma.get("config.placeholder.email"));
        txtEmail.setStyle(AppTheme.STYLE_INPUT);

        PasswordField txtPass = new PasswordField();
        txtPass.setText(savedPass);
        txtPass.setPromptText(idioma.get("config.placeholder.pass"));
        txtPass.setStyle(AppTheme.STYLE_INPUT);

        Button btnAccionFormulario = UIFactory.crearBotonPrimario(idioma.get("config.btn.login"));
        btnAccionFormulario.setMaxWidth(Double.MAX_VALUE);

        Label lblToggleFormulario = new Label(idioma.get("config.link.register"));
        lblToggleFormulario.setStyle("-fx-text-fill: #2563EB; -fx-underline: true; -fx-cursor: hand;");

        VBox loginBox = new VBox(10);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.getChildren().addAll(
            UIFactory.crearGrupoInput(idioma.get("config.lbl.email_supa"), txtEmail),
            UIFactory.crearGrupoInput(idioma.get("config.lbl.pass_supa"), txtPass),
            new Region(),
            btnAccionFormulario,
            lblToggleFormulario
        );

        VBox panelControlBox = new VBox(15);
        panelControlBox.setVisible(false);
        panelControlBox.setManaged(false);

        CheckBox chkAutoBackup = new CheckBox(idioma.get("config.chk.autobackup"));
        chkAutoBackup.setSelected(autoBackupActivado);
        chkAutoBackup.setStyle("-fx-text-fill: #111827;");

        Button btnRespaldar = UIFactory.crearBotonPrimario(idioma.get("config.btn.backup_up"));
        btnRespaldar.setMaxWidth(Double.MAX_VALUE);
        btnRespaldar.setStyle("-fx-background-color: #059669; -fx-text-fill: white; -fx-font-weight: bold;");

        Button btnDescargar = UIFactory.crearBotonSecundario(idioma.get("config.btn.backup_down"));
        btnDescargar.setMaxWidth(Double.MAX_VALUE);
        
        Button btnCerrarSesion = new Button(idioma.get("config.btn.cloud_logout"));
        btnCerrarSesion.setStyle("-fx-background-color: transparent; -fx-text-fill: #DC2626; -fx-cursor: hand; -fx-underline: true;");
        btnCerrarSesion.setMaxWidth(Double.MAX_VALUE);

        panelControlBox.getChildren().addAll(chkAutoBackup, btnRespaldar, btnDescargar, btnCerrarSesion);

        lblToggleFormulario.setOnMouseClicked(e -> {
            esModoLogin = !esModoLogin;
            if(esModoLogin){
                btnAccionFormulario.setText(idioma.get("config.btn.login"));
                lblToggleFormulario.setText(idioma.get("config.link.register"));
            } else {
                btnAccionFormulario.setText(idioma.get("config.btn.create_account"));
                lblToggleFormulario.setText(idioma.get("config.link.login"));
            }
        });

        btnAccionFormulario.setOnAction(e -> {
            String correo = txtEmail.getText();
            String pass = txtPass.getText();
            
            btnAccionFormulario.setDisable(true);
            btnAccionFormulario.setText(idioma.get("config.btn.connecting"));

            new Thread(() -> {
                if (!esModoLogin) {
                    boolean registrado = supabaseAuthService.registrarUsuario(correo, pass);
                    Platform.runLater(() -> {
                        if (registrado) {
                            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("client.msg.success.title"), idioma.get("config.msg.cloud.created"), stage);
                            esModoLogin = true;
                            btnAccionFormulario.fire(); 
                        } else {
                            btnAccionFormulario.setText(idioma.get("config.btn.create_account"));
                            btnAccionFormulario.setDisable(false);
                            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("client.msg.error.title"), idioma.get("config.msg.cloud.register_err"), stage);
                        }
                    });
                } else {
                    String token = supabaseAuthService.iniciarSesion(correo, pass);
                    Platform.runLater(() -> {
                        btnAccionFormulario.setText(idioma.get("config.btn.login"));
                        btnAccionFormulario.setDisable(false);

                        if (token != null) {
                            this.tokenSupabase = token;
                            prefs.put("SUPA_EMAIL", correo);
                            prefs.put("SUPA_PASS", pass);

                            loginBox.setVisible(false); loginBox.setManaged(false);
                            panelControlBox.setVisible(true); panelControlBox.setManaged(true);
                            lblInfo.setText(idioma.get("config.lbl.connected_as", correo));
                            lblInfo.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                        } else {
                            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("client.msg.error.title"), idioma.get("config.msg.cloud.bad_creds"), stage);
                        }
                    });
                }
            }).start();
        });

        chkAutoBackup.setOnAction(e -> prefs.putBoolean("AUTO_BACKUP", chkAutoBackup.isSelected()));

        btnRespaldar.setOnAction(e -> {
            btnRespaldar.setText(idioma.get("config.btn.uploading")); 
            btnRespaldar.setDisable(true);
            new Thread(() -> {
                File zip = backupService.generarRespaldoLocal();
                boolean exito = zip != null && backupService.subirRespaldoNube(zip, tokenSupabase, txtEmail.getText());
                Platform.runLater(() -> {
                    btnRespaldar.setDisable(false); 
                    btnRespaldar.setText(idioma.get("config.btn.backup_up"));
                    if (exito) dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("client.msg.success.title"), idioma.get("config.msg.cloud.sync_ok"), stage);
                    else dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("client.msg.error.title"), idioma.get("config.msg.cloud.sync_err"), stage);
                });
            }).start();
        });

        btnDescargar.setOnAction(e -> {
            boolean confirmado = dialogService.mostrarConfirmacion(
                idioma.get("config.msg.cloud.restore_title"), 
                idioma.get("config.msg.cloud.restore_confirm"), 
                stage
            );

            if (confirmado) {
                btnDescargar.setText(idioma.get("config.btn.restoring")); 
                btnDescargar.setDisable(true);
                
                new Thread(() -> {
                    File descargado = backupService.descargarRespaldoNube(tokenSupabase, txtEmail.getText());
                    if (descargado != null) {
                        boolean aplicado = backupService.aplicarRespaldoAutomatico(descargado);
                        
                        Platform.runLater(() -> {
                            if (aplicado) {
                                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, idioma.get("client.msg.success.title"), idioma.get("config.msg.cloud.restore_ok"), stage);
                                System.exit(0); 
                            } else {
                                btnDescargar.setDisable(false); 
                                btnDescargar.setText(idioma.get("config.btn.backup_down"));
                                dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("config.msg.cloud.fatal"), idioma.get("config.msg.cloud.restore_err"), stage);
                            }
                        });
                    } else {
                        Platform.runLater(() -> {
                            btnDescargar.setDisable(false); 
                            btnDescargar.setText(idioma.get("config.btn.backup_down"));
                            dialogService.mostrarAlerta(Alert.AlertType.ERROR, idioma.get("client.msg.error.title"), idioma.get("config.msg.cloud.no_backup"), stage);
                        });
                    }
                }).start();
            }
        });

        btnCerrarSesion.setOnAction(e -> {
            this.tokenSupabase = null;
            prefs.remove("SUPA_EMAIL");
            prefs.remove("SUPA_PASS");
            txtEmail.clear();
            txtPass.clear();
            
            panelControlBox.setVisible(false); panelControlBox.setManaged(false);
            loginBox.setVisible(true); loginBox.setManaged(true);
            lblInfo.setText(idioma.get("config.card.cloud.info"));
            lblInfo.setStyle("-fx-text-fill: #6B7280;");
        });

        if (!savedEmail.isEmpty() && !savedPass.isEmpty()) {
            Platform.runLater(() -> btnAccionFormulario.fire());
        }

        card.getChildren().addAll(lblTitulo, lblInfo, new Region(), loginBox, panelControlBox);
        return card;
    }

    /**
     * Actualiza el tipo de cambio desde la API de internet
     */
    private void actualizarDesdeInternet() {
        btnActualizarInternet.setText(idioma.get("config.btn.api_loading"));
        btnActualizarInternet.setDisable(true);

        new Thread(() -> {
            boolean exito = monedaService.actualizarDesdeInternet();

            Platform.runLater(() -> {
                btnActualizarInternet.setDisable(false);
                btnActualizarInternet.setText(idioma.get("config.btn.update_api"));

                if (exito) {
                    actualizarDisplayValor();
                    dialogService.mostrarAlerta(Alert.AlertType.INFORMATION,
                            idioma.get("config.msg.api.success.title"),
                            idioma.get("config.msg.api.success.content"),
                            stage);
                } else {
                    dialogService.mostrarAlerta(Alert.AlertType.ERROR,
                            idioma.get("config.msg.api.error.title"),
                            idioma.get("config.msg.api.error.content"),
                            stage);
                }
            });
        }).start();
    }

    /**
     * Actualiza el valor del tipo de cambio mostrado en la interfaz
     */
    private void actualizarDisplayValor() {
        lblValorActual.setText("$" + String.format("%.2f", monedaService.getTipoCambioActual()));
    }
}