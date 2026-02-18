package com.vluevano.view;

import com.vluevano.service.DialogService;
import com.vluevano.service.MonedaService;
import com.vluevano.util.AppTheme;
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

@Component
public class ConfiguracionView {

    @Autowired
    private MonedaService monedaService;

    @Autowired
    private DialogService dialogService;

    @Autowired
    @Lazy
    private MenuPrincipalScreen menuPrincipalScreen;

    private Stage stage;


    private Label lblValorActual;
    private ComboBox<String> cmbMonedaDefault;
    private Button btnActualizarInternet;

    /**
     * Muestra la pantalla de configuración general del sistema
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        monedaService.inicializar();

        BorderPane root = new BorderPane();
        
        root.setTop(UIFactory.crearHeader("Configuración del Sistema", 
                "Ajustes globales, preferencias y tipos de cambio", 
                () -> menuPrincipalScreen.show(stage, usuarioActual)));

        VBox mainContainer = new VBox(25);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");
        mainContainer.setAlignment(Pos.TOP_CENTER);

        VBox cardMoneda = crearTarjetaMoneda();

        VBox cardPrefs = crearTarjetaPreferencias();

        mainContainer.getChildren().addAll(cardMoneda, cardPrefs);

        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + "; -fx-background: " + AppTheme.COLOR_BG_LIGHT + ";");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("PriceStocker | Configuración");
    }

    /**
     * Crea la tarjeta de Tipo de Cambio
     * @return
     */
    private VBox crearTarjetaMoneda() {
        VBox card = new VBox(15);
        card.setStyle(AppTheme.STYLE_CARD);
        card.setMaxWidth(600);
        card.setPadding(new Insets(25));

        Label lblTitulo = new Label("Tipo de Cambio (USD → MXN)");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        VBox valorBox = new VBox(5);
        valorBox.setAlignment(Pos.CENTER);
        
        Label lblSubtituloValor = new Label("Valor Actual");
        lblSubtituloValor.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 13px; -fx-font-weight: 600;");

        lblValorActual = new Label("$" + String.format("%.2f", monedaService.getTipoCambioActual()));
        lblValorActual.setStyle("-fx-font-size: 42px; -fx-font-weight: 800; -fx-text-fill: #059669;");
        
        valorBox.getChildren().addAll(lblSubtituloValor, lblValorActual);

        btnActualizarInternet = UIFactory.crearBotonSecundario("Actualizar ahora desde Internet");
        btnActualizarInternet.setMaxWidth(Double.MAX_VALUE);
        btnActualizarInternet.setOnAction(e -> actualizarDesdeInternet());
        card.getChildren().addAll(lblTitulo, valorBox, btnActualizarInternet);
        return card;
    }

    /**
     * Crea la tarjeta de preferencias generales
     * @return
     */
    private VBox crearTarjetaPreferencias() {
        VBox card = new VBox(15);
        card.setStyle(AppTheme.STYLE_CARD);
        card.setMaxWidth(600);
        card.setPadding(new Insets(25));

        Label lblTitulo = new Label("Preferencias Generales");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");

        cmbMonedaDefault = new ComboBox<>();
        cmbMonedaDefault.getItems().addAll("MXN", "USD");
        cmbMonedaDefault.setValue(monedaService.getMonedaPorDefecto());
        cmbMonedaDefault.setMaxWidth(Double.MAX_VALUE);
        cmbMonedaDefault.setStyle(AppTheme.STYLE_INPUT);

        Button btnGuardarPrefs = UIFactory.crearBotonPrimario("Guardar Preferencias");
        btnGuardarPrefs.setMaxWidth(Double.MAX_VALUE);
        btnGuardarPrefs.setOnAction(e -> {
            monedaService.setMonedaPorDefecto(cmbMonedaDefault.getValue());
            dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Guardado", "Preferencias actualizadas correctamente.", stage);
        });

        card.getChildren().addAll(
            lblTitulo, 
            UIFactory.crearGrupoInput("Moneda predeterminada al crear productos", cmbMonedaDefault),
            new Region(),
            btnGuardarPrefs
        );
        return card;
    }

    /**
     * Intenta actualizar el tipo de cambio desde una API pública y actualiza la interfaz según el resultado
     */
    private void actualizarDesdeInternet() {
        btnActualizarInternet.setText("Consultando API...");
        btnActualizarInternet.setDisable(true);
        
        new Thread(() -> {
            boolean exito = monedaService.actualizarDesdeInternet();
            
            Platform.runLater(() -> {
                btnActualizarInternet.setDisable(false);
                btnActualizarInternet.setText("Actualizar ahora desde Internet");
                
                if(exito) {
                    actualizarDisplayValor();
                    dialogService.mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Tipo de cambio actualizado correctamente.", stage);
                } else {
                    dialogService.mostrarAlerta(Alert.AlertType.ERROR, "Error de Conexión", "No se pudo conectar a la API. Verifica tu conexión a internet.", stage);
                }
            });
        }).start();
    }
    
    /**
     * Actualiza el label que muestra el valor actual del tipo de cambio
     */
    private void actualizarDisplayValor() {
        lblValorActual.setText("$" + String.format("%.2f", monedaService.getTipoCambioActual()));
    }
}