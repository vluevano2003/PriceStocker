package com.vluevano.view.base;

import com.vluevano.service.DialogService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.GestorIdioma;
import com.vluevano.util.UIFactory;
import com.vluevano.view.MenuPrincipalScreen;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

public abstract class BaseHistorialView<T> {

    @Autowired protected DialogService dialogService;
    @Autowired @Lazy protected MenuPrincipalScreen menuPrincipalScreen;
    @Autowired protected GestorIdioma idioma;

    protected Stage stage;
    protected String usuarioActual;

    protected TableView<T> tablaDatos;
    protected TextField txtBusqueda;
    protected DatePicker dpInicio;
    protected DatePicker dpFin;

    private Label lblPlaceholderDefault;
    private Label lblPlaceholderFiltro;

    protected abstract String getTituloVentana();
    protected abstract String getTituloHeader();
    protected abstract String getSubtituloHeader();
    protected abstract String getPromptBusqueda();
    protected abstract String getTxtTablaVacia();
    protected abstract String getTxtExportarError();
    protected abstract String getTxtExportarVacio();
    protected abstract String getNombreArchivoPdf();
    
    protected abstract void configurarColumnasTabla();
    protected abstract List<T> obtenerDatos(LocalDate inicio, LocalDate fin, String busqueda);
    protected abstract void generarReportePdf(File file, List<T> datos, String rangoFechas) throws Exception;

    /**
     * Muestra la vista en el escenario dado, configurando la estructura común y delegando a los métodos abstractos para la lógica específica de cada historial
     * @param stage
     * @param usuarioActual
     */
    public void show(Stage stage, String usuarioActual) {
        this.stage = stage;
        this.usuarioActual = usuarioActual;

        BorderPane root = new BorderPane();
        root.setTop(UIFactory.crearHeader(
                getTituloHeader(),
                getSubtituloHeader(),
                () -> menuPrincipalScreen.show(stage, this.usuarioActual)));

        root.setCenter(crearContenidoPrincipal());

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1200, 768);
            stage.setScene(scene);
            stage.centerOnScreen();
        } else {
            scene.setRoot(root);
        }

        String css = getClass().getResource("/css/styles.css").toExternalForm();
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }

        stage.setTitle("PriceStocker | " + getTituloVentana());
        stage.show();
    }

    /**
     * Crea el contenido principal de la vista, incluyendo los filtros y la tabla. Este método es común para todos los historiales, pero delega la configuración de columnas y la obtención de datos a los métodos abstractos
     * @return
     */
    private VBox crearContenidoPrincipal() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setStyle("-fx-background-color: " + AppTheme.COLOR_BG_LIGHT + ";");

        inicializarPlaceholders();

        HBox filtros = new HBox(15);
        filtros.setStyle(AppTheme.STYLE_CARD);
        filtros.setPadding(new Insets(20));
        filtros.setAlignment(Pos.CENTER_LEFT);

        txtBusqueda = UIFactory.crearInput(getPromptBusqueda());
        txtBusqueda.setPrefWidth(260);

        dpInicio = new DatePicker(LocalDate.now().minusDays(30));
        dpFin = new DatePicker(LocalDate.now());
        Button btnBuscar = UIFactory.crearBotonPrimario(idioma.get("history.btn.filter"));

        txtBusqueda.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltros());
        btnBuscar.setOnAction(e -> aplicarFiltros());

        Button btnReporte = new Button(idioma.get("history.btn.export_pdf"));
        btnReporte.setStyle("-fx-background-color: #1E7145; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15; -fx-background-radius: 6;");
        btnReporte.setOnAction(e -> exportarPdf());

        filtros.getChildren().addAll(
                new Label(idioma.get("history.lbl.search")), txtBusqueda,
                new Label(idioma.get("history.lbl.from")), dpInicio,
                new Label(idioma.get("history.lbl.to")), dpFin,
                btnBuscar,
                new Region(),
                btnReporte);
        HBox.setHgrow(filtros.getChildren().get(7), Priority.ALWAYS);

        tablaDatos = new TableView<>();
        tablaDatos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        VBox.setVgrow(tablaDatos, Priority.ALWAYS);
        
        configurarColumnasTabla();

        layout.getChildren().addAll(filtros, tablaDatos);
        
        aplicarFiltros();
        
        return layout;
    }

    /**
     * Inicializa los placeholders para la tabla, uno para cuando no hay datos y otro para cuando el filtro de búsqueda no arroja resultados. Esto permite mostrar mensajes más amigables al usuario dependiendo del contexto
     */
    private void inicializarPlaceholders() {
        String estiloBase = "-fx-text-fill: #9CA3AF; -fx-font-size: 14px; -fx-font-weight: 500;";
        lblPlaceholderDefault = new Label(getTxtTablaVacia());
        lblPlaceholderDefault.setStyle(estiloBase);

        lblPlaceholderFiltro = new Label(idioma.get("history.table.no_results"));
        lblPlaceholderFiltro.setStyle(estiloBase);
    }

    /**
     * Aplica los filtros de fecha y búsqueda para obtener los datos correspondientes y actualizar la tabla. Si el campo de búsqueda no está vacío, se muestra un placeholder indicando que no hay resultados, en lugar del mensaje genérico de tabla vacía
     */
    private void aplicarFiltros() {
        if (dpInicio.getValue() == null || dpFin.getValue() == null) return;
        
        boolean esBusquedaTexto = !txtBusqueda.getText().trim().isEmpty();
        tablaDatos.setPlaceholder(esBusquedaTexto ? lblPlaceholderFiltro : lblPlaceholderDefault);

        List<T> datos = obtenerDatos(dpInicio.getValue(), dpFin.getValue(), txtBusqueda.getText().trim().toLowerCase());
        tablaDatos.setItems(FXCollections.observableArrayList(datos));
    }

    /**
     * Permite exportar los datos actualmente filtrados a un archivo PDF. Si la tabla está vacía, muestra una alerta indicando que no hay datos para exportar. Si ocurre un error durante la generación del PDF, muestra una alerta con el mensaje de error correspondiente
     */
    private void exportarPdf() {
        if (tablaDatos.getItems().isEmpty()) {
            dialogService.mostrarAlerta(Alert.AlertType.WARNING,
                    idioma.get("history.msg.empty_table.title"),
                    getTxtExportarVacio(),
                    stage);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(idioma.get("history.purchase.fc.title"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        
        String defaultFileName = idioma.getLocaleActual().getLanguage().equals("es")
                ? "Reporte_" + getNombreArchivoPdf() + "_"
                : "Report_" + getNombreArchivoPdf() + "_";
        
        fileChooser.setInitialFileName(defaultFileName + LocalDate.now() + ".pdf");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                String rango = idioma.get("history.report.range", dpInicio.getValue().toString(), dpFin.getValue().toString());
                generarReportePdf(file, tablaDatos.getItems(), rango);
                
                dialogService.mostrarAlerta(Alert.AlertType.INFORMATION,
                        idioma.get("history.msg.report_generated.title"),
                        idioma.get("history.msg.report_generated.content"), stage);
            } catch (Exception ex) {
                ex.printStackTrace();
                dialogService.mostrarAlerta(Alert.AlertType.ERROR,
                        idioma.get("history.msg.report_error.title"),
                        getTxtExportarError() + " " + ex.getMessage(), stage);
            }
        }
    }
}