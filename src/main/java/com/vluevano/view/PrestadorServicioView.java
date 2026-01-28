/*package com.vluevano.view;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vluevano.controller.PrestadorServicioController;
import com.vluevano.model.PrestadorServicio;
import com.vluevano.model.Ruta;
import com.vluevano.model.Servicio;

public class PrestadorServicioView extends Application {

    private PrestadorServicioController controller;
    private TableView<PrestadorServicio> tablePrestadores;
    private TextField txtNombre, txtMunicipio, txtEstado, txtCp, txtNoExt, txtNoInt, txtRfc, txtCalle, txtColonia,
            txtCiudad, txtPais, txtTelefono, txtCorreo, txtCurp;
    private Button btnRegistrar, btnModificar, btnEliminar, btnImportarExcel;
    private CheckBox chkEsPersonaFisica;
    private FileChooser fileChooser;
    private BorderPane root;

    private ListView<String> listViewServicios;
    private ListView<String> listViewRutas;

    private String usuarioActual;

    // Atributos para los paneles
    private VBox panelFormulario;
    private VBox panelTabla;

    // Para guardar el prestador seleccionado
    private PrestadorServicio prestadorSeleccionado;

    private List<PrestadorServicio> prestadoresOriginales;

    public PrestadorServicioView(String usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    private VBox crearPanelFormulario() {
        panelFormulario = new VBox(10);
        panelFormulario.setStyle("-fx-padding: 10;");

        // Título
        Label lblTitulo = new Label("Registrar Prestador de Servicios");
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Campos de formulario
        txtNombre = new TextField();
        txtNombre.setPromptText("Nombre del prestador");
        txtRfc = new TextField();
        txtRfc.setPromptText("RFC");
        txtTelefono = new TextField();
        txtTelefono.setPromptText("Teléfono");
        txtCp = new TextField();
        txtCp.setPromptText("Código Postal");
        txtNoExt = new TextField();
        txtNoExt.setPromptText("Número Ext.");
        txtNoInt = new TextField();
        txtNoInt.setPromptText("Número Int.");
        txtCalle = new TextField();
        txtCalle.setPromptText("Calle");
        txtColonia = new TextField();
        txtColonia.setPromptText("Colonia");
        txtCiudad = new TextField();
        txtCiudad.setPromptText("Ciudad");
        txtMunicipio = new TextField();
        txtMunicipio.setPromptText("Municipio");
        txtEstado = new TextField();
        txtEstado.setPromptText("Estado");
        txtPais = new TextField();
        txtPais.setPromptText("País");
        txtCorreo = new TextField();
        txtCorreo.setPromptText("Correo");
        txtCurp = new TextField();
        txtCurp.setPromptText("CURP");

        chkEsPersonaFisica = new CheckBox("Es Persona Física");

        // Sección para agregar servicios
        Label lblServicios = new Label("Servicios:");
        listViewServicios = new ListView<>();
        listViewServicios.setMaxHeight(100); // Ajusta el tamaño de la lista
        TextField txtDescripcionServicio = new TextField();
        txtDescripcionServicio.setPromptText("Descripción del servicio");
        TextField txtCostoServicio = new TextField();
        txtCostoServicio.setPromptText("Costo del servicio");

        // ComboBox para seleccionar moneda
        ComboBox<String> comboMonedaServicio = new ComboBox<>();
        comboMonedaServicio.getItems().addAll("MXN", "USD");
        comboMonedaServicio.setPromptText("Moneda");

        Button btnAgregarServicio = new Button("Agregar Servicio");
        btnAgregarServicio.setOnAction(e -> {
            if (txtDescripcionServicio.getText().isEmpty() || txtCostoServicio.getText().isEmpty()
                    || comboMonedaServicio.getValue() == null) {
                showAlert("Error", "Todos los campos del servicio deben estar completos.");
            } else {
                String servicio = txtDescripcionServicio.getText() + " - " + txtCostoServicio.getText() + " "
                        + comboMonedaServicio.getValue();
                listViewServicios.getItems().add(servicio);
                txtDescripcionServicio.clear();
                txtCostoServicio.clear();
                comboMonedaServicio.getSelectionModel().clearSelection(); // Limpia la selección
            }
        });

        Button btnEliminarServicio = new Button("Eliminar Servicio");
        btnEliminarServicio.setOnAction(e -> {
            int selectedIndex = listViewServicios.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                listViewServicios.getItems().remove(selectedIndex);
            }
        });

        VBox panelServicios = new VBox(5, lblServicios, txtDescripcionServicio, txtCostoServicio, comboMonedaServicio,
                btnAgregarServicio, listViewServicios, btnEliminarServicio);

        // Sección para agregar rutas
        Label lblRutas = new Label("Rutas:");
        listViewRutas = new ListView<>();
        listViewRutas.setMaxHeight(100); // Ajusta el tamaño de la lista
        TextField txtSalidaRuta = new TextField();
        txtSalidaRuta.setPromptText("Punto de salida");
        TextField txtDestinoRuta = new TextField();
        txtDestinoRuta.setPromptText("Destino");
        Button btnAgregarRuta = new Button("Agregar Ruta");
        btnAgregarRuta.setOnAction(e -> {
            if (txtSalidaRuta.getText().isEmpty() || txtDestinoRuta.getText().isEmpty()) {
                showAlert("Error", "La ruta debe contener un punto de salida y destino.");
            } else {
                String ruta = txtSalidaRuta.getText() + " → " + txtDestinoRuta.getText();
                listViewRutas.getItems().add(ruta);
                txtSalidaRuta.clear();
                txtDestinoRuta.clear();
            }
        });

        Button btnEliminarRuta = new Button("Eliminar Ruta");
        btnEliminarRuta.setOnAction(e -> {
            int selectedIndex = listViewRutas.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0) {
                listViewRutas.getItems().remove(selectedIndex);
            }
        });

        VBox panelRutas = new VBox(5, lblRutas, txtSalidaRuta, txtDestinoRuta, btnAgregarRuta, listViewRutas,
                btnEliminarRuta);

        btnRegistrar = new Button("Registrar");
        btnRegistrar.setOnAction(e -> registrarPrestador());

        btnImportarExcel = new Button("Importar Excel");
        btnImportarExcel.setOnAction(e -> importarExcel());

        // Validación de formulario antes de registrar
        btnRegistrar.setOnAction(e -> {
            if (txtNombre.getText().isEmpty() || txtRfc.getText().isEmpty() || txtTelefono.getText().isEmpty()
                    || txtCp.getText().isEmpty() || txtCorreo.getText().isEmpty() || txtCurp.getText().isEmpty()
                    || txtCalle.getText().isEmpty() || txtColonia.getText().isEmpty() || txtCiudad.getText().isEmpty()
                    || txtMunicipio.getText().isEmpty() || txtEstado.getText().isEmpty() || txtPais.getText().isEmpty()
                    || listViewServicios.getItems().isEmpty() || listViewRutas.getItems().isEmpty()) {
                showAlert("Error", "Los campos obligatorios no deben estar vacíos.");
            } else if (!txtTelefono.getText().matches("\\d+")) {
                showAlert("Error", "El teléfono debe ser numérico.");
            } else if (!txtCp.getText().matches("\\d+")) {
                showAlert("Error", "El Código Postal debe ser numérico.");
            } else if (!txtCorreo.getText().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                showAlert("Error", "El correo debe ser válido.");
            } else if (!txtRfc.getText().matches("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$")) {
                showAlert("Error", "El RFC debe tener entre 12 y 13 caracteres.");
            } else if (!txtCurp.getText().matches("[A-Z]{4}[0-9]{6}[A-Z]{6,7}[0-9]{1,2}")) {
                showAlert("Error", "El CURP debe tener 18 caracteres y seguir el formato oficial.");
            }
            else {
                registrarPrestador(); // Si las validaciones pasan
            }
        });

        panelFormulario.getChildren().addAll(lblTitulo, txtNombre, txtMunicipio, txtEstado, txtCp, txtNoExt, txtNoInt,
                txtRfc,
                txtCalle, txtColonia, txtCiudad, txtPais, txtTelefono, txtCorreo, txtCurp, chkEsPersonaFisica,
                panelServicios, panelRutas, btnRegistrar, btnImportarExcel);

        ScrollPane scrollPane = new ScrollPane(panelFormulario);
        scrollPane.setFitToWidth(true);

        return new VBox(scrollPane);
    }

    // Método para mostrar alertas
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void registrarPrestador() {
        PrestadorServicio prestador = new PrestadorServicio();
        prestador.setNombrePrestador(txtNombre.getText());
        prestador.setMunicipio(txtMunicipio.getText());
        prestador.setEstado(txtEstado.getText());
        prestador.setCpPrestador(Integer.parseInt(txtCp.getText()));
        prestador.setNoExtPrestador(Integer.parseInt(txtNoExt.getText()));
        prestador.setNoIntPrestador(Integer.parseInt(txtNoInt.getText()));
        prestador.setRfcPrestador(txtRfc.getText());
        prestador.setCalle(txtCalle.getText());
        prestador.setColonia(txtColonia.getText());
        prestador.setCiudad(txtCiudad.getText());
        prestador.setPais(txtPais.getText());
        prestador.setTelefonoPrestador(txtTelefono.getText());
        prestador.setCorreoPrestador(txtCorreo.getText());
        prestador.setCurp(txtCurp.getText());
        prestador.setEsPersonaFisica(chkEsPersonaFisica.isSelected());

        for (String servicioStr : listViewServicios.getItems()) {
            String[] servicioParts = servicioStr.split(" - ");
            if (servicioParts.length == 2) {
                String descripcion = servicioParts[0];
                String[] costoParts = servicioParts[1].split(" ");
                if (costoParts.length == 2) {
                    double costo = Double.parseDouble(costoParts[0]);
                    String moneda = costoParts[1];
                    Servicio servicio = new Servicio(0, descripcion, costo, moneda);
                    prestador.getServicios().add(servicio);
                }
            }
        }

        for (String rutaStr : listViewRutas.getItems()) {
            String[] rutaParts = rutaStr.split(" → ");
            if (rutaParts.length == 2) {
                String salida = rutaParts[0];
                String destino = rutaParts[1];
                Ruta ruta = new Ruta(0, salida, destino);
                prestador.getRutas().add(ruta);
            }
        }

        boolean registrado = controller.registrarPrestadorServicio(prestador);
        if (registrado) {
            mostrarMensaje("Prestador registrado con éxito.");
            actualizarTabla(""); // Actualizamos la tabla
        } else {
            mostrarMensaje("Error al registrar el prestador.");
        }
    }

    private void actualizarTabla(String filtro) {
        // Usamos el filtro para consultar prestadores desde el controlador
        tablePrestadores.getItems().setAll(controller.consultarPrestadores());
    }

    @SuppressWarnings("unchecked")
    private VBox crearPanelTabla() {
        // Panel de tabla
        panelTabla = new VBox(10);
        panelTabla.setStyle("-fx-padding: 10;");

        // Campo de texto para ingresar filtros
        TextField filtroField = new TextField();
        filtroField.setPromptText("Ingresa filtros (ID, nombre, teléfono, RFC, etc.) separados por comas");

        filtroField.textProperty().addListener((observable, oldValue, newValue) -> {
            String filtro = filtroField.getText();
            List<PrestadorServicio> prestadoresFiltrados = controller.buscarPrestadoresServicio(filtro);
            actualizarTablaFiltro(prestadoresFiltrados); // Actualiza la tabla con los resultados
        });

        // Tabla de prestadores de servicios
        tablePrestadores = new TableView<>();

        // Consultar la lista original de prestadores solo una vez
        prestadoresOriginales = controller.consultarPrestadores();
        tablePrestadores.getItems().setAll(prestadoresOriginales);

        filtroField.textProperty().addListener((observable, oldValue, newValue) -> {
            List<PrestadorServicio> filtrados = newValue.isEmpty()
                    ? prestadoresOriginales
                    : controller.buscarPrestadoresServicio(newValue);

            tablePrestadores.getItems().setAll(filtrados);
        });

        TableColumn<PrestadorServicio, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idPrestador"));

        TableColumn<PrestadorServicio, String> nombreColumn = new TableColumn<>("Nombre");
        nombreColumn.setCellValueFactory(new PropertyValueFactory<>("nombrePrestador"));

        TableColumn<PrestadorServicio, String> rfcColumn = new TableColumn<>("RFC");
        rfcColumn.setCellValueFactory(new PropertyValueFactory<>("rfcPrestador"));

        TableColumn<PrestadorServicio, String> telefonoColumn = new TableColumn<>("Teléfono");
        telefonoColumn.setCellValueFactory(new PropertyValueFactory<>("telefonoPrestador"));

        TableColumn<PrestadorServicio, Integer> cpColumn = new TableColumn<>("Código Postal");
        cpColumn.setCellValueFactory(new PropertyValueFactory<>("cpPrestador"));

        TableColumn<PrestadorServicio, Integer> noExtColumn = new TableColumn<>("Número Exterior");
        noExtColumn.setCellValueFactory(new PropertyValueFactory<>("noExtPrestador"));

        TableColumn<PrestadorServicio, Integer> noIntColumn = new TableColumn<>("Número Interior");
        noIntColumn.setCellValueFactory(new PropertyValueFactory<>("noIntPrestador"));

        TableColumn<PrestadorServicio, String> calleColumn = new TableColumn<>("Calle");
        calleColumn.setCellValueFactory(new PropertyValueFactory<>("calle"));

        TableColumn<PrestadorServicio, String> coloniaColumn = new TableColumn<>("Colonia");
        coloniaColumn.setCellValueFactory(new PropertyValueFactory<>("colonia"));

        TableColumn<PrestadorServicio, String> ciudadColumn = new TableColumn<>("Ciudad");
        ciudadColumn.setCellValueFactory(new PropertyValueFactory<>("ciudad"));

        TableColumn<PrestadorServicio, String> municipioColumn = new TableColumn<>("Municipio");
        municipioColumn.setCellValueFactory(new PropertyValueFactory<>("municipio"));

        TableColumn<PrestadorServicio, String> estadoColumn = new TableColumn<>("Estado");
        estadoColumn.setCellValueFactory(new PropertyValueFactory<>("estado"));

        TableColumn<PrestadorServicio, String> paisColumn = new TableColumn<>("País");
        paisColumn.setCellValueFactory(new PropertyValueFactory<>("pais"));

        TableColumn<PrestadorServicio, String> correoColumn = new TableColumn<>("Correo");
        correoColumn.setCellValueFactory(new PropertyValueFactory<>("correoPrestador"));

        TableColumn<PrestadorServicio, String> curpColumn = new TableColumn<>("CURP");
        curpColumn.setCellValueFactory(new PropertyValueFactory<>("curp"));

        TableColumn<PrestadorServicio, String> personaFisicaColumn = new TableColumn<>("Es Persona Física");
        personaFisicaColumn.setCellValueFactory(new PropertyValueFactory<>("esPersonaFisica"));

        // Nueva columna para los servicios
        TableColumn<PrestadorServicio, String> serviciosColumn = new TableColumn<>("Servicios");
        serviciosColumn.setCellValueFactory(cellData -> {
            List<Servicio> servicios = cellData.getValue().getServicios();
            return new SimpleStringProperty(servicios != null && !servicios.isEmpty()
                    ? servicios.stream().map(Servicio::getDescripcionServicio).collect(Collectors.joining(", "))
                    : "Sin servicios");
        });

        TableColumn<PrestadorServicio, String> rutasColumn = new TableColumn<>("Rutas");
        rutasColumn.setCellValueFactory(cellData -> {
            List<Ruta> rutas = cellData.getValue().getRutas();
            return new SimpleStringProperty(rutas != null && !rutas.isEmpty()
                    ? rutas.stream().map(ruta -> ruta.getSalida() + " - " + ruta.getDestino())
                            .collect(Collectors.joining(", "))
                    : "Sin rutas");
        });

        // Agregar las columnas al TableView
        tablePrestadores.getColumns().addAll(idColumn, nombreColumn, rfcColumn, telefonoColumn, cpColumn, noExtColumn,
                noIntColumn, calleColumn, coloniaColumn, ciudadColumn, municipioColumn, estadoColumn, paisColumn,
                correoColumn, curpColumn, personaFisicaColumn, serviciosColumn, rutasColumn);

        // Inicializar la tabla con los prestadores
        List<PrestadorServicio> prestadoresIniciales = controller.consultarPrestadores();
        tablePrestadores.getItems().setAll(prestadoresIniciales);

        // Configuración para seleccionar un prestador de la tabla
        tablePrestadores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            prestadorSeleccionado = newSelection; // Se guarda el prestador seleccionado
            btnModificar.setDisable(newSelection == null); // Habilitar/deshabilitar botones
            btnEliminar.setDisable(newSelection == null);
        });

        // Botones de acción
        HBox buttons = new HBox(10);
        btnModificar = new Button("Modificar");
        btnModificar.setOnAction(e -> modificarPrestador());
        btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> eliminarPrestador());

        // Deshabilitar botones si no hay selección
        btnModificar.setDisable(true);
        btnEliminar.setDisable(true);

        buttons.getChildren().addAll(btnModificar, btnEliminar);

        // Agregar la tabla en un ScrollPane
        ScrollPane scrollTable = new ScrollPane(tablePrestadores);
        scrollTable.setFitToWidth(true);
        scrollTable.setFitToHeight(true);

        panelTabla.getChildren().addAll(filtroField, scrollTable, buttons);

        return new VBox(panelTabla);
    }

    // Método para actualizar la tabla con los resultados filtrados
    private void actualizarTablaFiltro(List<PrestadorServicio> prestadoresFiltrados) {
        ObservableList<PrestadorServicio> observablePrestadores = FXCollections
                .observableArrayList(prestadoresFiltrados);
        tablePrestadores.setItems(observablePrestadores);
    }

    private void abrirVentanaModificar(PrestadorServicio prestadorSeleccionado) {
        if (prestadorSeleccionado == null) {
            mostrarMensaje("Selecciona un prestador para modificar.");
            return;
        }

        Stage ventanaModificacion = new Stage();
        ventanaModificacion.initModality(Modality.APPLICATION_MODAL);
        ventanaModificacion.setTitle("Modificar Prestador");

        // Campos de entrada
        TextField nombreField = new TextField(prestadorSeleccionado.getNombrePrestador());
        TextField rfcField = new TextField(prestadorSeleccionado.getRfcPrestador());
        TextField telefonoField = new TextField(prestadorSeleccionado.getTelefonoPrestador());
        TextField cpField = new TextField(String.valueOf(prestadorSeleccionado.getCpPrestador()));
        TextField noExtField = new TextField(String.valueOf(prestadorSeleccionado.getNoExtPrestador()));
        TextField noIntField = new TextField(String.valueOf(prestadorSeleccionado.getNoIntPrestador()));
        TextField calleField = new TextField(prestadorSeleccionado.getCalle());
        TextField coloniaField = new TextField(prestadorSeleccionado.getColonia());
        TextField ciudadField = new TextField(prestadorSeleccionado.getCiudad());
        TextField municipioField = new TextField(prestadorSeleccionado.getMunicipio());
        TextField estadoField = new TextField(prestadorSeleccionado.getEstado());
        TextField paisField = new TextField(prestadorSeleccionado.getPais());
        TextField correoField = new TextField(prestadorSeleccionado.getCorreoPrestador());
        TextField curpField = new TextField(prestadorSeleccionado.getCurp());
        CheckBox personaFisicaCheck = new CheckBox();
        personaFisicaCheck.setSelected(prestadorSeleccionado.isEsPersonaFisica());

        Button btnGuardar = new Button("Guardar Cambios");
        btnGuardar.setOnAction(e -> {

            // Validar que los campos no estén vacíos o nulos
            if ((nombreField.getText() == null || nombreField.getText().trim().isEmpty()) ||
                    (rfcField.getText() == null || rfcField.getText().trim().isEmpty()) ||
                    (telefonoField.getText() == null || telefonoField.getText().trim().isEmpty()) ||
                    (cpField.getText() == null || cpField.getText().trim().isEmpty()) ||
                    (calleField.getText() == null || calleField.getText().trim().isEmpty()) ||
                    (municipioField.getText() == null || municipioField.getText().trim().isEmpty()) ||
                    (estadoField.getText() == null || estadoField.getText().trim().isEmpty()) ||
                    (paisField.getText() == null || paisField.getText().trim().isEmpty()) ||
                    (correoField.getText() == null || correoField.getText().trim().isEmpty())) {

                mostrarMensaje("Todos los campos obligatorios deben estar llenos.");
                return;
            }
            if (!rfcField.getText().matches("^[A-ZÑ&]{3,4}[0-9]{6}[A-Z0-9]{3}$")) {
                mostrarMensaje("RFC inválido. Debe tener entre 12 y 13 caracteres.");
                return;
            }
            if (!curpField.getText().matches("^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[0-9A-Z]{2}$")) {
                mostrarMensaje("CURP inválido. Debe tener 18 caracteres y seguir el formato oficial.");
                return;
            }
            if (!telefonoField.getText().matches("^[0-9]{10}$")) {
                mostrarMensaje("Teléfono inválido. Debe contener 10 dígitos.");
                return;
            }
            if (!correoField.getText().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$")) {
                mostrarMensaje("Correo inválido. Debe seguir el formato ejemplo@dominio.com.");
                return;
            }

            prestadorSeleccionado.setNombrePrestador(nombreField.getText());
            prestadorSeleccionado.setRfcPrestador(rfcField.getText());
            prestadorSeleccionado.setTelefonoPrestador(telefonoField.getText());
            prestadorSeleccionado.setCpPrestador(Integer.parseInt(cpField.getText()));
            prestadorSeleccionado.setNoExtPrestador(Integer.parseInt(noExtField.getText()));
            prestadorSeleccionado.setNoIntPrestador(Integer.parseInt(noIntField.getText()));
            prestadorSeleccionado.setCalle(calleField.getText());
            prestadorSeleccionado.setColonia(coloniaField.getText());
            prestadorSeleccionado.setCiudad(ciudadField.getText());
            prestadorSeleccionado.setMunicipio(municipioField.getText());
            prestadorSeleccionado.setEstado(estadoField.getText());
            prestadorSeleccionado.setPais(paisField.getText());
            prestadorSeleccionado.setCorreoPrestador(correoField.getText());
            prestadorSeleccionado.setCurp(curpField.getText());
            prestadorSeleccionado.setEsPersonaFisica(personaFisicaCheck.isSelected());

            boolean modificado = controller.modificarPrestadorServicio(prestadorSeleccionado);
            if (modificado) {
                mostrarMensaje("Prestador modificado con éxito.");
                actualizarTabla(""); // Actualizar tabla en la vista principal
                ventanaModificacion.close();
            } else {
                mostrarMensaje("Error al modificar el prestador.");
            }
        });

        VBox layout = new VBox(10, new Label("Nombre:"), nombreField,
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
                new Label("Es Persona Física:"), personaFisicaCheck, btnGuardar);

        layout.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(layout);
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

    // Método para llamar la ventana cuando el usuario seleccione modificar
    private void modificarPrestador() {
        PrestadorServicio prestadorSeleccionado = tablePrestadores.getSelectionModel().getSelectedItem();
        abrirVentanaModificar(prestadorSeleccionado);
    }

    private void eliminarPrestador() {
        if (prestadorSeleccionado != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar eliminación");
            alert.setHeaderText("Eliminar Prestador");
            alert.setContentText("¿Estás seguro de que deseas eliminar al prestador "
                    + prestadorSeleccionado.getNombrePrestador() + "?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                boolean eliminado = controller.eliminarPrestadorServicio(prestadorSeleccionado.getIdPrestador());
                if (eliminado) {
                    mostrarMensaje("Prestador eliminado con éxito.");
                    actualizarTabla(""); // Actualizamos la tabla
                } else {
                    mostrarMensaje("Error al eliminar el prestador.");
                }
            }
        } else {
            mostrarMensaje("Selecciona un prestador para eliminar.");
        }
    }

    private void importarExcel() {
        fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos Excel", "*.xlsx"));
        File archivo = fileChooser.showOpenDialog(null);
        if (archivo != null) {
            controller.importarPrestadoresDesdeExcel(archivo);
            actualizarTabla(""); // Actualizamos la tabla
        }
    }

    private void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            controller = new PrestadorServicioController();

            // Layout principal
            root = new BorderPane();
            root.setStyle("-fx-padding: 10;");

            // Crear la barra de menú pasando el primaryStage
            MenuBar menuBar = crearMenuBar(primaryStage);
            root.setTop(menuBar);

            // Inicializamos con el formulario de registro
            root.setCenter(crearPanelFormulario());

            // Cambiar a la tabla de prestadores después de la inicialización del formulario
            root.setCenter(crearPanelTabla());
            actualizarTabla(""); // Ahora la tabla está inicializada

            Scene scene = new Scene(root, 800, 600);
            primaryStage.setTitle("Gestión de Prestadores de Servicio");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MenuBar crearMenuBar(Stage primaryStage) {
        Menu menuVista = new Menu("Opciones");

        MenuItem itemFormulario = new MenuItem("Registrar Prestador");
        itemFormulario.setOnAction(e -> root.setCenter(crearPanelFormulario()));

        MenuItem itemTabla = new MenuItem("Consultar Prestadores");
        itemTabla.setOnAction(e -> root.setCenter(crearPanelTabla()));

        MenuItem salirItem = new MenuItem("Salir");
        salirItem.setOnAction(e -> mostrarMenuPrincipal(primaryStage));

        menuVista.getItems().addAll(itemFormulario, itemTabla, salirItem);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().add(menuVista);
        return menuBar;
    }

    private void mostrarMenuPrincipal(Stage primaryStage) {
        // Crea una nueva instancia del MenuPrincipalScreen con el usuario actual
        MenuPrincipalScreen menuPrincipalScreen = new MenuPrincipalScreen(primaryStage, usuarioActual);
        menuPrincipalScreen.mostrarMenu(); // Mostrar el menú principal
    }

    public static void main(String[] args) {
        launch(args);
    }
}
*/