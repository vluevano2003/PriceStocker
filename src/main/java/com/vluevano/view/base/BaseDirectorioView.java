package com.vluevano.view.base;

import com.vluevano.service.DialogService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.GestorIdioma;
import com.vluevano.util.UIFactory;
import com.vluevano.view.MenuPrincipalScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * BaseDirectorioView es una clase abstracta que proporciona la estructura y lógica común para las vistas de directorio
 */
public abstract class BaseDirectorioView<T> {

    @Autowired protected DialogService dialogService;
    @Autowired @Lazy protected MenuPrincipalScreen menuPrincipalScreen;
    @Autowired protected GestorIdioma idioma;

    protected Stage stage;
    protected String usuarioActual;
    
    protected TableView<T> tablaDatos;
    protected TextField txtFiltro;
    protected VBox panelFormulario;
    protected Label lblTituloFormulario;
    protected Button btnGuardar;
    protected Label lblMensaje;
    
    protected T entidadEnEdicion = null;

    protected abstract String getTituloVentana();
    protected abstract String getTituloHeader();
    protected abstract String getSubtituloHeader();
    protected abstract String getPromptBusqueda();
    protected abstract String getTituloFormularioNuevo();
    
    protected abstract void configurarColumnasTabla();
    protected abstract VBox construirCamposFormulario();
    protected abstract void cargarDatos();
    protected abstract void buscarDatos(String valor);
    protected abstract void limpiarCamposEspecificos();
    protected abstract void registrarEntidad();
    protected abstract void mostrarDetalle(T entidad);

    private static final String MODERN_SCROLL_CSS = "data:text/css," +
        ".scroll-pane { -fx-background-color: transparent; -fx-background: transparent; }" +
        ".scroll-pane > .viewport { -fx-background-color: transparent; }" +
        ".scroll-bar:vertical { -fx-min-height: 14px; -fx-pref-height: 14px; -fx-max-height: 14px; -fx-background-color: #F3F4F6; -fx-background-radius: 7px; }" +
        ".scroll-bar:vertical .track { -fx-background-color: transparent; -fx-border-color: transparent; }" +
        ".scroll-bar:vertical .thumb { -fx-background-color: #D9DCE2; -fx-background-radius: 7px; -fx-background-insets: 2px; }" +
        ".scroll-bar:vertical:hover .thumb { -fx-background-color: #BEC4CE; }" + 
        ".scroll-bar:vertical .increment-button, .scroll-bar:vertical .decrement-button { -fx-padding: 0; -fx-pref-width: 0; }" +
        ".scroll-bar:vertical .increment-arrow, .scroll-bar:vertical .decrement-arrow { -fx-shape: null; -fx-padding: 0; }";

    /**
     * Muestra la vista en el escenario dado, configurando la estructura común y delegando a los métodos abstractos para la lógica específica de cada directorio
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        root.setTop(UIFactory.crearHeader(
                getTituloHeader(),
                getSubtituloHeader(),
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        HBox contenidoCentral = new HBox(30);
        contenidoCentral.setPadding(new Insets(30));

        VBox panelTabla = crearPanelTabla();
        HBox.setHgrow(panelTabla, Priority.ALWAYS);

        panelFormulario = crearPanelFormularioEstandar();
        panelFormulario.setMinWidth(420);
        panelFormulario.setMaxWidth(420);

        contenidoCentral.getChildren().addAll(panelTabla, panelFormulario);
        root.setCenter(contenidoCentral);

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1280, 800);
            stage.setScene(scene);
            stage.centerOnScreen();
        } else {
            scene.setRoot(root);
        }

        String css = getClass().getResource("/css/styles.css").toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }

        stage.setResizable(true);
        stage.setTitle("PriceStocker | " + getTituloVentana());
        stage.show();

        configurarColumnasTabla();
        cargarDatos();
    }

    /**
     * Crea el panel que contiene la tabla de datos y el campo de búsqueda, configurando el listener para filtrar los datos en tiempo real
     * @return
     */
    private VBox crearPanelTabla() {
        VBox box = new VBox(15);

        txtFiltro = UIFactory.crearInput(getPromptBusqueda());
        txtFiltro.setPrefWidth(300);
        txtFiltro.textProperty().addListener((obs, oldVal, newVal) -> buscarDatos(newVal));

        HBox topBar = new HBox(10, new Label(idioma.get("ui.search.label")), txtFiltro);
        topBar.setAlignment(Pos.CENTER_LEFT);

        tablaDatos = new TableView<>();
        tablaDatos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        
        tablaDatos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tablaDatos.getSelectionModel().getSelectedItem() != null) {
                mostrarDetalle(tablaDatos.getSelectionModel().getSelectedItem());
            }
        });

        VBox.setVgrow(tablaDatos, Priority.ALWAYS);
        box.getChildren().addAll(topBar, tablaDatos);
        return box;
    }

    /**
     * Crea el panel del formulario con un diseño estándar, incluyendo el título, un área para los campos específicos (que el hijo inyecta) y los botones de acción
     * @return
     */
    private VBox crearPanelFormularioEstandar() {
        VBox card = new VBox(0);
        card.setStyle(AppTheme.STYLE_CARD);

        lblTituloFormulario = new Label(getTituloFormularioNuevo());
        lblTituloFormulario.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        lblTituloFormulario.setPadding(new Insets(20));

        VBox inputsEspecificos = construirCamposFormulario();

        ScrollPane scrollPane = new ScrollPane(inputsEspecificos);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        scrollPane.getStylesheets().add(MODERN_SCROLL_CSS);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        VBox footer = new VBox(10);
        footer.setPadding(new Insets(20));

        btnGuardar = UIFactory.crearBotonPrimario(getTextoBotonGuardar());
        
        btnGuardar.setMaxWidth(Double.MAX_VALUE);
        btnGuardar.setOnAction(e -> registrarEntidad());

        Button btnLimpiar = UIFactory.crearBotonTexto(idioma.get("ui.btn.clear"));
        btnLimpiar.setMaxWidth(Double.MAX_VALUE);
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        lblMensaje = new Label();
        lblMensaje.setWrapText(true);

        footer.getChildren().addAll(btnGuardar, btnLimpiar, lblMensaje);
        card.getChildren().addAll(lblTituloFormulario, scrollPane, footer);
        return card;
    }

    /**
     * Limpia el formulario y resetea el estado de la vista para permitir ingresar una nueva entidad, delegando a los métodos abstractos para limpiar los campos específicos de cada directorio
     */
    protected void limpiarFormulario() {
        this.entidadEnEdicion = null;
        lblTituloFormulario.setText(getTituloFormularioNuevo());
        btnGuardar.setText(getTextoBotonGuardar());
        btnGuardar.setStyle("-fx-background-color: " + AppTheme.COLOR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 8; -fx-cursor: hand;");
        lblMensaje.setText("");
        limpiarCamposEspecificos();
    }

    /**
     * Obtiene el texto que se mostrará en el botón de guardar, permitiendo que las vistas hijas personalicen este texto según el contexto (por ejemplo, "Crear" vs "Actualizar")
     * @return
     */
    protected String getTextoBotonGuardar() {
        return idioma.get("ui.btn.save");
    }
}