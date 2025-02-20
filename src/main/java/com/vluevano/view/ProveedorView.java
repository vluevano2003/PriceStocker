package com.vluevano.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.vluevano.controller.ProveedorController;
import com.vluevano.database.DatabaseConnection;
import com.vluevano.model.Categoria;
import com.vluevano.model.Proveedor;

public class ProveedorView extends Application {

    private ProveedorController controller = new ProveedorController();

    private String usuarioActual;

    private TableView<Proveedor> tableView;

    public ProveedorView(String usuarioActual) {
        this.usuarioActual = usuarioActual; // Guardamos el usuario actual
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Módulo de Proveedores");

        // Crear menú
        MenuBar menuBar = new MenuBar();

        Menu menu = new Menu("Opciones");
        MenuItem registroItem = new MenuItem("Registrar Proveedor");
        MenuItem consultarItem = new MenuItem("Consultar Proveedores");
        MenuItem salirItem = new MenuItem("Salir");

        menu.getItems().addAll(registroItem, consultarItem, salirItem);
        menuBar.getMenus().add(menu);

        // Crear panel principal
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));

        // Funcionalidades del menú
        registroItem.setOnAction(e -> {
            try {
                showRegistroForm(vbox);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        consultarItem.setOnAction(e -> showConsultarForm(vbox));
        salirItem.setOnAction(e -> mostrarMenuPrincipal(primaryStage));

        // Layout principal
        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(vbox);

        showConsultarForm(vbox);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Registrar proveedores
    private void showRegistroForm(VBox vbox) throws IOException {
        vbox.getChildren().clear();

        // Asegurarse de que la ventana es lo suficientemente grande
        Stage stage = (Stage) vbox.getScene().getWindow();
        stage.setWidth(600);
        stage.setHeight(700);

        // Título del apartado
        Label titleLabel = new Label("Registrar Proveedor");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10px;");

        // Crear campos de texto con Labels
        Label nombreLabel = new Label("Nombre del proveedor:");
        TextField nombreField = new TextField();

        Label rfcLabel = new Label("RFC:");
        TextField rfcField = new TextField();

        Label telefonoLabel = new Label("Teléfono:");
        TextField telefonoField = new TextField();

        Label cpLabel = new Label("Código Postal:");
        TextField cpField = new TextField();

        Label noExtLabel = new Label("Número exterior:");
        TextField noExtField = new TextField();

        Label noIntLabel = new Label("Número interior:");
        TextField noIntField = new TextField();

        Label calleLabel = new Label("Calle:");
        TextField calleField = new TextField();

        Label coloniaLabel = new Label("Colonia:");
        TextField coloniaField = new TextField();

        Label ciudadLabel = new Label("Ciudad:");
        TextField ciudadField = new TextField();

        Label municipioLabel = new Label("Municipio:");
        TextField municipioField = new TextField();

        Label estadoLabel = new Label("Estado:");
        TextField estadoField = new TextField();

        Label paisLabel = new Label("País:");
        TextField paisField = new TextField();

        Label correoLabel = new Label("Correo electrónico:");
        TextField correoField = new TextField();

        Label curpLabel = new Label("CURP:");
        TextField curpField = new TextField();

        CheckBox personaFisicaCheck = new CheckBox("Es persona física");

        // Crear contenedor para las categorías
        HBox categoriaContainer = new HBox(10);

        // Crear un ComboBox inicial para la categoría
        ComboBox<String> categoriaComboBox = new ComboBox<>();
        cargarCategorias(categoriaComboBox); // Cargar categorías desde la base de datos
        categoriaComboBox.setPromptText("Selecciona categoría");

        // Añadir el ComboBox al contenedor
        categoriaContainer.getChildren().add(categoriaComboBox);

        // Crear botón para agregar más ComboBoxes
        Button agregarCategoriaButton = new Button("Agregar otra categoría");
        agregarCategoriaButton.setOnAction(e -> {
            ComboBox<String> nuevaCategoriaComboBox = new ComboBox<>();
            try {
                cargarCategorias(nuevaCategoriaComboBox);
            } catch (IOException e1) {
                e1.printStackTrace();
            } // Cargar categorías desde la base de datos
            nuevaCategoriaComboBox.setPromptText("Selecciona categoría");
            categoriaContainer.getChildren().add(nuevaCategoriaComboBox);
        });

        // Crear botón para registrar nueva categoría
        Button nuevaCategoriaButton = new Button("Registrar Nueva Categoría");
        nuevaCategoriaButton.setOnAction(e -> showRegistrarCategoriaForm(vbox, categoriaComboBox));

        // Contenedor para los botones de categoría (ahora alineados horizontalmente)
        HBox categoriaBotonesContainer = new HBox(10);
        categoriaBotonesContainer.getChildren().addAll(agregarCategoriaButton, nuevaCategoriaButton);

        // Botón para cargar archivo Excel
        Button cargarExcelButton = new Button("Cargar archivo Excel");
        cargarExcelButton.setOnAction(e -> cargarProveedoresDesdeExcel());

        // Crear botón para registrar
        Button registrarButton = new Button("Registrar");
        registrarButton.setOnAction(e -> {
            String nombre = nombreField.getText().trim();
            String rfc = rfcField.getText().trim();
            String telefono = telefonoField.getText().trim();
            String cp = cpField.getText().trim();
            String noExt = noExtField.getText().trim();
            String noInt = noIntField.getText().trim();
            String calle = calleField.getText().trim();
            String colonia = coloniaField.getText().trim();
            String ciudad = ciudadField.getText().trim();
            String municipio = municipioField.getText().trim();
            String estado = estadoField.getText().trim();
            String pais = paisField.getText().trim();
            String correo = correoField.getText().trim();
            String curp = curpField.getText().trim();
            boolean esPersonaFisica = personaFisicaCheck.isSelected();

            // Obtener todas las categorías seleccionadas
            List<String> categoriasSeleccionadas = new ArrayList<>();
            for (Node node : categoriaContainer.getChildren()) {
                if (node instanceof ComboBox) {
                    @SuppressWarnings("unchecked")
                    String categoria = ((ComboBox<String>) node).getValue();
                    if (categoria != null) {
                        categoriasSeleccionadas.add(categoria);
                    }
                }
            }

            if (!validarCampos(nombreField, rfcField, telefonoField, cpField, correoField, curpField)) {
                return;
            }

            // Registrar proveedor
            int cpInt = Integer.parseInt(cp);
            int noExtInt = Integer.parseInt(noExt);
            int noIntInt = noInt.isEmpty() ? 0 : Integer.parseInt(noInt);

            Proveedor proveedor = new Proveedor(0, nombre, cpInt, noExtInt, noIntInt, rfc, municipio, estado, calle,
                    colonia, ciudad, pais, telefono, correo, curp, esPersonaFisica);
            List<Categoria> categorias = new ArrayList<>();

            // Agregar las categorías seleccionadas
            for (String categoriaSeleccionada : categoriasSeleccionadas) {
                categorias.add(new Categoria(0, categoriaSeleccionada, "Descripción"));
            }

            controller.registrarProveedor(proveedor, categorias);
            showAlert(Alert.AlertType.INFORMATION, "Proveedor registrado con éxito.");
        });

        ScrollPane scrollPane = new ScrollPane();
        VBox formContainer = new VBox(10);
        formContainer.getChildren().addAll(
                titleLabel, nombreLabel, nombreField, rfcLabel, rfcField, telefonoLabel, telefonoField, cpLabel,
                cpField,
                noExtLabel, noExtField, noIntLabel, noIntField, calleLabel, calleField, coloniaLabel, coloniaField,
                ciudadLabel, ciudadField, municipioLabel, municipioField, estadoLabel, estadoField, paisLabel,
                paisField,
                correoLabel, correoField, curpLabel, curpField, personaFisicaCheck, categoriaContainer,
                categoriaBotonesContainer, registrarButton, cargarExcelButton);

        scrollPane.setContent(formContainer);
        scrollPane.setFitToHeight(true); // Ajusta la altura al contenido
        scrollPane.setFitToWidth(true); // Ajusta el ancho al contenido

        // Agregar el contenedor con scroll al VBox
        vbox.getChildren().add(scrollPane);
    }

    // Método para cargar las categorías desde la base de datos
    private void cargarCategorias(ComboBox<String> categoriaComboBox) throws IOException {
        categoriaComboBox.getItems().clear(); // Limpiar los items antes de agregar nuevos

        String sql = "SELECT nombreCategoria FROM categoria"; // Consulta para obtener categorías

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categoriaComboBox.getItems().add(rs.getString("nombreCategoria"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error al cargar categorías.");
        }
    }

    // Método para mostrar alerta
    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType, message);
        alert.show();
    }

    // Método para mostrar formulario de nueva categoría
    private void showRegistrarCategoriaForm(VBox vbox, ComboBox<String> categoriaComboBox) {
        Stage newStage = new Stage();
        newStage.setTitle("Registrar Nueva Categoría");

        // Crear campo de texto para el nombre de la categoría
        TextField categoriaField = new TextField();
        categoriaField.setPromptText("Nombre de la categoría");

        // Crear campo de texto para la descripción de la categoría
        TextField descripcionField = new TextField();
        descripcionField.setPromptText("Descripción de la categoría");

        // Crear botón para registrar la categoría
        Button registrarCategoriaButton = new Button("Registrar");
        registrarCategoriaButton.setOnAction(e -> {
            String categoria = categoriaField.getText().trim();
            String descripcion = descripcionField.getText().trim();

            // Validar que el nombre y la descripción no estén vacíos
            if (categoria.isEmpty() || descripcion.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Nombre y descripción de la categoría son obligatorios.");
                return;
            }

            // Lógica para registrar la nueva categoría en la base de datos
            String sql = "INSERT INTO categoria (nombrecategoria, desccategoria) VALUES (?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, categoria);
                stmt.setString(2, descripcion);
                stmt.executeUpdate();

                // Actualizar el ComboBox con la nueva categoría
                cargarCategorias(categoriaComboBox);

                showAlert(Alert.AlertType.INFORMATION, "Categoría registrada con éxito.");
                newStage.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error al registrar la categoría.");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        // Contenedor para los campos y el botón
        VBox newFormContainer = new VBox(10);
        newFormContainer.setPadding(new Insets(20)); // Añadir margen entre los elementos y los bordes
        newFormContainer.getChildren().addAll(
                new Label("Registrar Nueva Categoría"),
                categoriaField,
                descripcionField,
                registrarCategoriaButton);

        // Crear la escena con el contenedor y mostrar la ventana
        Scene newScene = new Scene(newFormContainer, 300, 200);
        newStage.setScene(newScene);
        newStage.show();
    }

    // Consultar proveedores
    @SuppressWarnings("unchecked")
    private void showConsultarForm(VBox vbox) {
        vbox.getChildren().clear();

        if (vbox.getScene() != null) {
            Stage stage = (Stage) vbox.getScene().getWindow();
            stage.setWidth(900);
            stage.setHeight(600);
        } else {
            System.err.println("El VBox aún no está en la escena.");
        }

        // Crear campo de búsqueda
        TextField filtroField = new TextField();
        filtroField.setPromptText("Introduce palabras clave para filtrar");

        // Crear el TableView
        tableView = new TableView<>();

        // Crear columnas para cada atributo del proveedor
        TableColumn<Proveedor, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));

        TableColumn<Proveedor, String> nombreColumn = new TableColumn<>("Nombre");
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombreProv"));

        TableColumn<Proveedor, String> rfcColumn = new TableColumn<>("RFC");
        rfcColumn.setCellValueFactory(new PropertyValueFactory<>("rfcProveedor"));

        TableColumn<Proveedor, String> telefonoColumn = new TableColumn<>("Teléfono");
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefonoProv"));

        TableColumn<Proveedor, Integer> cpColumn = new TableColumn<>("Código Postal");
        cpColumn.setCellValueFactory(new PropertyValueFactory<>("cpProveedor"));

        TableColumn<Proveedor, Integer> noExtColumn = new TableColumn<>("Número Exterior");
        noExtColumn.setCellValueFactory(new PropertyValueFactory<>("noExtProv"));

        TableColumn<Proveedor, Integer> noIntColumn = new TableColumn<>("Número Interior");
        noIntColumn.setCellValueFactory(new PropertyValueFactory<>("noIntProv"));

        TableColumn<Proveedor, String> calleColumn = new TableColumn<>("Calle");
        calleColumn.setCellValueFactory(new PropertyValueFactory<>("calle"));

        TableColumn<Proveedor, String> coloniaColumn = new TableColumn<>("Colonia");
        coloniaColumn.setCellValueFactory(new PropertyValueFactory<>("colonia"));

        TableColumn<Proveedor, String> ciudadColumn = new TableColumn<>("Ciudad");
        ciudadColumn.setCellValueFactory(new PropertyValueFactory<>("ciudad"));

        TableColumn<Proveedor, String> municipioColumn = new TableColumn<>("Municipio");
        municipioColumn.setCellValueFactory(new PropertyValueFactory<>("municipio"));

        TableColumn<Proveedor, String> estadoColumn = new TableColumn<>("Estado");
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<Proveedor, String> paisColumn = new TableColumn<>("País");
        paisColumn.setCellValueFactory(new PropertyValueFactory<>("pais"));

        TableColumn<Proveedor, String> correoColumn = new TableColumn<>("Correo");
        correoColumn.setCellValueFactory(new PropertyValueFactory<>("correoProv"));

        TableColumn<Proveedor, String> curpColumn = new TableColumn<>("CURP");
        curpColumn.setCellValueFactory(new PropertyValueFactory<>("curp"));

        TableColumn<Proveedor, String> personaFisicaColumn = new TableColumn<>("Es Persona Física");
        personaFisicaColumn.setCellValueFactory(new PropertyValueFactory<>("esPersonaFisica"));

        TableColumn<Proveedor, String> categoriasColumn = new TableColumn<>("Categorías");
        categoriasColumn.setCellValueFactory(new PropertyValueFactory<>("categoriasAsString"));
        categoriasColumn.setResizable(true);
        categoriasColumn.setPrefWidth(350);

        // Agregar las columnas al TableView
        tableView.getColumns().addAll(idColumn, nombreColumn, rfcColumn, telefonoColumn, cpColumn, noExtColumn,
                noIntColumn, calleColumn, coloniaColumn, ciudadColumn, municipioColumn, estadoColumn, paisColumn,
                correoColumn, curpColumn, personaFisicaColumn, categoriasColumn);

        List<Proveedor> proveedoresIniciales = controller.consultarTodosProveedores();

        tableView.getItems().setAll(proveedoresIniciales);

        // Crear un filtro para la búsqueda
        filtroField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Buscar proveedores con los filtros proporcionados
            List<Proveedor> proveedoresFiltrados = controller.buscarProveedores(newValue);

            // Actualizar la tabla con los proveedores filtrados
            tableView.getItems().setAll(proveedoresFiltrados);
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                mostrarVentanaEdicionProveedor(newValue);
            }
        });

        // Crear un ScrollPane para que la tabla sea desplazable si es necesario
        ScrollPane scrollPane = new ScrollPane(tableView);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);

        // Agregar el ScrollPane con la tabla al VBox
        vbox.getChildren().addAll(filtroField, scrollPane);
    }

    private void mostrarMenuPrincipal(Stage primaryStage) {
        // Crea una nueva instancia del MenuPrincipalScreen con el usuario actual
        MenuPrincipalScreen menuPrincipalScreen = new MenuPrincipalScreen(primaryStage, usuarioActual);
        menuPrincipalScreen.mostrarMenu(); // Mostrar el menú principal
    }

    public void mostrarVentanaEdicionProveedor(Proveedor proveedorSeleccionado) {
        // Crear los campos de texto para editar
        TextField nombreField = new TextField(proveedorSeleccionado.getNombreProv());
        TextField rfcField = new TextField(proveedorSeleccionado.getRfcProveedor());
        TextField telefonoField = new TextField(proveedorSeleccionado.getTelefonoProv());
        TextField cpField = new TextField(String.valueOf(proveedorSeleccionado.getCpProveedor()));
        TextField noExtField = new TextField(String.valueOf(proveedorSeleccionado.getNoExtProv()));
        TextField noIntField = new TextField(String.valueOf(proveedorSeleccionado.getNoIntProv()));
        TextField calleField = new TextField(proveedorSeleccionado.getCalle());
        TextField coloniaField = new TextField(proveedorSeleccionado.getColonia());
        TextField ciudadField = new TextField(proveedorSeleccionado.getCiudad());
        TextField municipioField = new TextField(proveedorSeleccionado.getMunicipio());
        TextField estadoField = new TextField(proveedorSeleccionado.getEstado());
        TextField paisField = new TextField(proveedorSeleccionado.getPais());
        TextField correoField = new TextField(proveedorSeleccionado.getCorreoProv());
        TextField curpField = new TextField(proveedorSeleccionado.getCurp());
        CheckBox personaFisicaCheck = new CheckBox();
        personaFisicaCheck.setSelected(proveedorSeleccionado.isEsPersonaFisica());

        // Crear botones de acción
        Button guardarButton = new Button("Guardar cambios");
        Button eliminarButton = new Button("Eliminar proveedor");

        // Evento para guardar cambios
        guardarButton.setOnAction(e -> {

            if (!validarCampos(nombreField, rfcField, telefonoField, cpField, correoField, curpField)) {
                return;
            }

            // Crear un nuevo proveedor con los datos editados
            Proveedor proveedorEditado = new Proveedor(
                    proveedorSeleccionado.getIdProveedor(), // ID no cambia
                    nombreField.getText(),
                    Integer.parseInt(cpField.getText()),
                    Integer.parseInt(noExtField.getText()),
                    Integer.parseInt(noIntField.getText()),
                    rfcField.getText(),
                    municipioField.getText(),
                    estadoField.getText(),
                    calleField.getText(),
                    coloniaField.getText(),
                    ciudadField.getText(),
                    paisField.getText(),
                    telefonoField.getText(),
                    correoField.getText(),
                    curpField.getText(),
                    personaFisicaCheck.isSelected());

            // Llamar al método del controlador para actualizar el proveedor
            controller.modificarProveedor(proveedorEditado);

            actualizarTablaProveedores(tableView);
            // Cerrar la ventana
            ((Stage) guardarButton.getScene().getWindow()).close();
        });

        // Evento para eliminar proveedor
        eliminarButton.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("¿Estás seguro de eliminar este proveedor?");
            alert.setContentText("Esta acción no se puede deshacer.");
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    // Eliminar proveedor en la base de datos
                    try {
                        controller.eliminarProveedor(proveedorSeleccionado.getIdProveedor());
                    } catch (SQLException | IOException e1) {
                        e1.printStackTrace();
                    }

                    // Actualizar la tabla
                    actualizarTablaProveedores(tableView);

                    // Cerrar la ventana
                    ((Stage) eliminarButton.getScene().getWindow()).close();
                }
            });
        });

        // Crear un VBox para organizar los campos y botones
        VBox vboxFormulario = new VBox(10);
        vboxFormulario.setPadding(new Insets(10)); // Añadir espaciado alrededor de los bordes

        // Agregar los campos de texto al VBox
        vboxFormulario.getChildren().addAll(
                new Label("Nombre:"), nombreField,
                new Label("RFC:"), rfcField,
                new Label("Teléfono:"), telefonoField,
                new Label("Código Postal:"), cpField,
                new Label("Número Exterior:"), noExtField,
                new Label("Número Interior:"), noIntField,
                new Label("Calle:"), calleField,
                new Label("Colonia:"), coloniaField,
                new Label("Ciudad:"), ciudadField,
                new Label("Municipio:"), municipioField,
                new Label("Estado:"), estadoField,
                new Label("País:"), paisField,
                new Label("Correo:"), correoField,
                new Label("CURP:"), curpField,
                new Label("Es Persona Física:"), personaFisicaCheck,
                guardarButton, eliminarButton);

        // Crear un ScrollPane para permitir desplazamiento
        ScrollPane scrollPane = new ScrollPane(vboxFormulario);
        scrollPane.setFitToWidth(true); // Ajustar el contenido al ancho del scroll
        scrollPane.setFitToHeight(true); // Ajustar el contenido al alto del scroll

        // Crear la escena con el ScrollPane
        Scene scene = new Scene(scrollPane, 400, 600);
        Stage ventanaEdicion = new Stage();
        ventanaEdicion.setTitle("Editar Proveedor");

        // Establecer un borde y relleno para darle formato a la ventana
        ventanaEdicion.setScene(scene);
        ventanaEdicion.setResizable(false); // Evitar que la ventana cambie de tamaño
        ventanaEdicion.show();
    }

    private void actualizarTablaProveedores(TableView<Proveedor> tableView) {
        List<Proveedor> proveedoresActualizados = controller.consultarTodosProveedores();
        tableView.getItems().setAll(proveedoresActualizados);
    }

    private boolean validarCampos(TextField nombre, TextField rfc, TextField telefono, TextField cp, TextField correo,
            TextField curp) {
        String mensajeError = "";

        if (nombre.getText().trim().isEmpty()) {
            mensajeError += "El nombre es obligatorio.\n";
        }
        if (!rfc.getText().matches("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$")) {
            mensajeError += "El RFC no tiene un formato válido.\n";
        }
        if (!telefono.getText().matches("^[0-9]{10}$")) {
            mensajeError += "El teléfono debe contener 10 dígitos.\n";
        }
        if (!cp.getText().matches("^[0-9]{5}$")) {
            mensajeError += "El código postal debe contener 5 dígitos.\n";
        }
        if (!correo.getText().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            mensajeError += "El correo no tiene un formato válido.\n";
        }
        if (!curp.getText().matches("[A-Z]{4}[0-9]{6}[A-Z]{6,7}[0-9]{1,2}")) {
            mensajeError += "El CURP no tiene un formato válido.\n";
        }

        if (!mensajeError.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Validación");
            alert.setHeaderText("Corrige los siguientes errores:");
            alert.setContentText(mensajeError);
            alert.show();
            return false;
        }
        return true;
    }

    private void cargarProveedoresDesdeExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                // Crear instancia del controlador
                ProveedorController controller = new ProveedorController();

                controller.registrarProveedorDesdeExcel(file);

                //showAlert(Alert.AlertType.INFORMATION, "Proveedores registrados desde Excel.");
            } catch (Exception e) {
                e.printStackTrace();
                //showAlert(Alert.AlertType.ERROR, "Error al procesar el archivo Excel: " + e.getMessage());
            }
        }
    }

}
