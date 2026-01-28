/*package com.vluevano.view;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.vluevano.controller.ProductoController;
import com.vluevano.model.Categoria;
import com.vluevano.model.Cliente;
import com.vluevano.model.Empresa;
import com.vluevano.model.Fabricante;
import com.vluevano.model.Producto;
import com.vluevano.model.Proveedor;
import com.vluevano.model.Servicio;

public class ProductoView extends Application {
    private final ProductoController productoController = new ProductoController();
    private String usuarioActual;
    private VBox formularioRegistro;
    private VBox consultaProductos;
    private TableView<Producto> productoTable;
    private VBox root;

    public ProductoView(String usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, SQLException {
        primaryStage.setTitle("Gestión de Productos");

        MenuBar menuBar = new MenuBar();
        Menu menuVista = new Menu("Opciones");
        MenuItem registrarItem = new MenuItem("Registrar producto");
        MenuItem consultarItem = new MenuItem("Consultar productos");
        MenuItem salirItem = new MenuItem("Salir");

        registrarItem.setOnAction(e -> mostrarFormularioRegistro());
        consultarItem.setOnAction(e -> {
            try {
                mostrarConsultaProductos();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
        salirItem.setOnAction(e -> mostrarMenuPrincipal(primaryStage));

        menuVista.getItems().addAll(registrarItem, consultarItem, salirItem);
        menuBar.getMenus().add(menuVista);

        formularioRegistro = crearFormularioRegistro();
        consultaProductos = crearConsultaProductos();

        root = new VBox(10, menuBar, consultaProductos);
        root.setPadding(new Insets(10));
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox crearFormularioRegistro() throws IOException {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        Label titulo = new Label("Registrar Producto");
        titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        TextField nombreField = new TextField();
        nombreField.setStyle("-fx-margin: 5px;");

        TextField fichaField = new TextField();
        fichaField.setStyle("-fx-margin: 5px;");

        TextField alternoField = new TextField();
        alternoField.setStyle("-fx-margin: 5px;");

        TextField existenciaField = new TextField();
        existenciaField.setStyle("-fx-margin: 5px;");

        ComboBox<String> monedaCombo = new ComboBox<>();
        monedaCombo.getItems().addAll("USD", "MXN");
        monedaCombo.setStyle("-fx-margin: 5px;");

        List<ComboBox<Categoria>> categoriaList = new ArrayList<>();
        List<ComboBox<Empresa>> empresaList = new ArrayList<>();
        List<TextField> precioEmpresaList = new ArrayList<>();
        List<ComboBox<Proveedor>> proveedorList = new ArrayList<>();
        List<TextField> precioProveedorList = new ArrayList<>();
        List<ComboBox<Fabricante>> fabricanteList = new ArrayList<>();
        List<TextField> precioFabricanteList = new ArrayList<>();
        List<ComboBox<Cliente>> clienteList = new ArrayList<>();
        List<TextField> precioClienteList = new ArrayList<>();
        List<ComboBox<Servicio>> servicios = new ArrayList<>();

        categoriaList.add(new ComboBox<>(FXCollections.observableList(productoController.obtenerCategorias())));
        empresaList.add(new ComboBox<>(FXCollections.observableList(productoController.obtenerEmpresas())));
        proveedorList.add(new ComboBox<>(FXCollections.observableList(productoController.obtenerProveedores())));
        fabricanteList.add(new ComboBox<>(FXCollections.observableList(productoController.obtenerFabricantes())));
        clienteList.add(new ComboBox<>(FXCollections.observableList(productoController.obtenerClientes())));
        servicios.add(new ComboBox<>(FXCollections.observableList(productoController.obtenerServicios())));

        // Botones con colores
        Button agregarCategoriaBtn = new Button("Agregar Categoría");
        agregarCategoriaBtn.setId("agregarCategoriaBtn");
        agregarCategoriaBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // Verde
        agregarCategoriaBtn.setOnAction(e -> {
            try {
                ComboBox<Categoria> categoriaCombo = new ComboBox<>(
                        FXCollections.observableList(productoController.obtenerCategorias()));
                categoriaList.add(categoriaCombo);
                layout.getChildren().addAll(new Separator(), new Label("Categoría:"), categoriaCombo);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        Button agregarEmpresaBtn = new Button("Agregar Empresa");
        agregarEmpresaBtn.setId("agregarEmpresaBtn");
        agregarEmpresaBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;"); // Azul
        agregarEmpresaBtn.setOnAction(e -> {
            try {
                ComboBox<Empresa> empresaCombo = new ComboBox<>(
                        FXCollections.observableList(productoController.obtenerEmpresas()));
                TextField precioEmpresa = new TextField();
                empresaList.add(empresaCombo);
                precioEmpresaList.add(precioEmpresa);
                layout.getChildren().addAll(new Separator(), new Label("Empresa:"), empresaCombo,
                        new Label("Precio Empresa:"), precioEmpresa);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        Button agregarProveedorBtn = new Button("Agregar Proveedor");
        agregarProveedorBtn.setId("agregarProveedorBtn");
        agregarProveedorBtn.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white;"); // Naranja
        agregarProveedorBtn.setOnAction(e -> {
            try {
                ComboBox<Proveedor> proveedorCombo = new ComboBox<>(
                        FXCollections.observableList(productoController.obtenerProveedores()));
                TextField precioProveedor = new TextField();
                proveedorList.add(proveedorCombo);
                precioProveedorList.add(precioProveedor);
                layout.getChildren().addAll(new Separator(), new Label("Proveedor:"), proveedorCombo,
                        new Label("Precio Proveedor:"), precioProveedor);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        Button agregarFabricanteBtn = new Button("Agregar Fabricante");
        agregarFabricanteBtn.setId("agregarFabricanteBtn");
        agregarFabricanteBtn.setStyle("-fx-background-color: #FFC107; -fx-text-fill: white;"); // Amarillo
        agregarFabricanteBtn.setOnAction(e -> {
            try {
                ComboBox<Fabricante> fabricanteCombo = new ComboBox<>(
                        FXCollections.observableList(productoController.obtenerFabricantes()));
                TextField precioFabricante = new TextField();
                fabricanteList.add(fabricanteCombo);
                precioFabricanteList.add(precioFabricante);
                layout.getChildren().addAll(new Separator(), new Label("Fabricante:"), fabricanteCombo,
                        new Label("Precio Fabricante:"), precioFabricante);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        Button agregarClienteBtn = new Button("Agregar Cliente");
        agregarClienteBtn.setId("agregarClienteBtn");
        agregarClienteBtn.setStyle("-fx-background-color: #673AB7; -fx-text-fill: white;"); // Púrpura
        agregarClienteBtn.setOnAction(e -> {
            try {
                ComboBox<Cliente> clienteCombo = new ComboBox<>(
                        FXCollections.observableList(productoController.obtenerClientes()));
                TextField precioCliente = new TextField();
                clienteList.add(clienteCombo);
                precioClienteList.add(precioCliente);
                layout.getChildren().addAll(new Separator(), new Label("Cliente:"), clienteCombo,
                        new Label("Precio Cliente:"), precioCliente);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        Button agregarServicioBtn = new Button("Agregar Servicio");
        agregarServicioBtn.setId("agregarServicioBtn");
        agregarServicioBtn.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white;"); // Violeta
        agregarServicioBtn.setOnAction(e -> {
            try {
                ComboBox<Servicio> servicioCombo = new ComboBox<>(
                        FXCollections.observableList(productoController.obtenerServicios()));
                servicios.add(servicioCombo);
                layout.getChildren().addAll(new Separator(), new Label("Servicio:"), servicioCombo);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });

        Button registrarBtn = new Button("Registrar");
        registrarBtn.setId("registrarBtn");
        registrarBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;"); // Verde
        registrarBtn.setOnAction(e -> {
            try {
                // Validar campos de texto
                if (nombreField.getText().isEmpty() || fichaField.getText().isEmpty()
                        || alternoField.getText().isEmpty() || existenciaField.getText().isEmpty()) {
                    showAlert("Error", "Todos los campos son obligatorios.", Alert.AlertType.ERROR);
                    return; // Salir si algún campo está vacío
                }

                // Validar existencia (debe ser un número entero)
                int existencia;
                try {
                    existencia = Integer.parseInt(existenciaField.getText());
                } catch (NumberFormatException ex) {
                    showAlert("Error", "La existencia debe ser un número entero.", Alert.AlertType.ERROR);
                    return;
                }

                // Validar que al menos una categoría sea seleccionada
                if (categoriaList.stream().allMatch(c -> c.getValue() == null)) {
                    showAlert("Error", "Debe seleccionar al menos una categoría.", Alert.AlertType.ERROR);
                    return;
                }

                // Validar que se haya seleccionado una moneda
                if (monedaCombo.getValue() == null) {
                    showAlert("Error", "Debe seleccionar una moneda.", Alert.AlertType.ERROR);
                    return;
                }

                // Crear el producto con los datos validados
                Producto nuevoProducto = new Producto(0, nombreField.getText(), fichaField.getText(),
                        alternoField.getText(), existencia);

                // Recoger las listas seleccionadas
                List<Categoria> categoriasSeleccionadas = categoriaList.stream().map(ComboBox::getValue)
                        .collect(Collectors.toList());
                List<Empresa> empresasSeleccionadas = empresaList.stream().map(ComboBox::getValue)
                        .filter(Objects::nonNull).collect(Collectors.toList());

                List<Double> preciosEmpresas = precioEmpresaList.stream().map(tf -> Double.parseDouble(tf.getText()))
                        .collect(Collectors.toList());

                List<Proveedor> proveedoresSeleccionados = proveedorList.stream().map(ComboBox::getValue)
                        .filter(Objects::nonNull).collect(Collectors.toList());
                List<Double> preciosProveedores = precioProveedorList.stream()
                        .map(tf -> Double.parseDouble(tf.getText())).collect(Collectors.toList());

                List<Fabricante> fabricantesSeleccionados = fabricanteList.stream().map(ComboBox::getValue)
                        .filter(Objects::nonNull).collect(Collectors.toList());
                List<Double> preciosFabricantes = precioFabricanteList.stream()
                        .map(tf -> Double.parseDouble(tf.getText())).collect(Collectors.toList());

                List<Cliente> clientesSeleccionados = clienteList.stream().map(ComboBox::getValue)
                        .filter(Objects::nonNull).collect(Collectors.toList());
                List<Double> preciosClientes = precioClienteList.stream().map(tf -> Double.parseDouble(tf.getText()))
                        .collect(Collectors.toList());

                List<Servicio> serviciosSeleccionados = servicios.stream().map(ComboBox::getValue)
                        .filter(Objects::nonNull).collect(Collectors.toList());

                String moneda = monedaCombo.getValue();

                // Registrar el producto en el controlador
                productoController.registrarProducto(nuevoProducto, categoriasSeleccionadas, empresasSeleccionadas,
                        proveedoresSeleccionados, fabricantesSeleccionados, clientesSeleccionados,
                        serviciosSeleccionados, preciosEmpresas, preciosProveedores, preciosFabricantes,
                        preciosClientes, moneda);

                // Mensaje de éxito
                showAlert("Éxito", "Producto registrado correctamente", Alert.AlertType.INFORMATION);

                // Limpiar el formulario y crear uno nuevo
                layout.getChildren().clear();
                VBox nuevoFormulario = crearFormularioRegistro();
                nuevoFormulario.setPadding(new Insets(-21));
                layout.getChildren().add(nuevoFormulario);

            } catch (Exception ex) {
                showAlert("Error", "Error al registrar el producto", Alert.AlertType.ERROR);
                System.out.println(ex.getMessage());
            }
        });

        Button cargarExcelBtn = new Button("Cargar desde Excel");
        cargarExcelBtn.setId("cargarExcelBtn");
        cargarExcelBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;"); // Naranja
        cargarExcelBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Excel Files", "*.xls", "*.xlsx"));
            File selectedFile = fileChooser.showOpenDialog(layout.getScene().getWindow());

            if (selectedFile != null) {
                productoController.registrarProductosDesdeExcel(selectedFile);
            }
        });

        HBox botonera = new HBox(10);
        botonera.getChildren().addAll(agregarCategoriaBtn, agregarEmpresaBtn, agregarProveedorBtn, agregarFabricanteBtn,
                agregarClienteBtn, agregarServicioBtn);
        botonera.setStyle("-fx-alignment: center;");

        layout.getChildren().addAll(titulo, new Label("Nombre:"), nombreField, new Label("Ficha:"), fichaField,
                new Label("Alterno:"), alternoField, new Label("Existencia:"), existenciaField, new Label("Moneda:"),
                monedaCombo, new Label("Categoría:"), categoriaList.get(0), botonera, registrarBtn, cargarExcelBtn);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToWidth(true);
        return new VBox(scrollPane);
    }

    @SuppressWarnings("unchecked")
    private VBox crearConsultaProductos() throws SQLException {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-border-color: black; -fx-border-width: 1px; -fx-border-radius: 5px;");

        // Crear el campo de texto para el filtro
        TextField filtroField = new TextField();
        filtroField.setPromptText(
                "Filtrar por ID, nombre, ficha, alterno, categoría, proveedor, empresa, fabricante, cliente, prestador de servicio");

        // Crear la tabla de productos
        productoTable = new TableView<>();

        // Definir las columnas
        TableColumn<Producto, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Producto, String> nombreColumn = new TableColumn<>("Nombre");
        TableColumn<Producto, String> fichaColumn = new TableColumn<>("Ficha");
        TableColumn<Producto, String> alternoColumn = new TableColumn<>("Alterno");
        TableColumn<Producto, Integer> existenciaColumn = new TableColumn<>("Existencia");
        TableColumn<Producto, String> categoriasColumn = new TableColumn<>("Categorías");
        TableColumn<Producto, String> proveedoresColumn = new TableColumn<>("Proveedores");
        TableColumn<Producto, String> empresasColumn = new TableColumn<>("Empresas");
        TableColumn<Producto, String> clientesColumn = new TableColumn<>("Clientes");
        TableColumn<Producto, String> fabricantesColumn = new TableColumn<>("Fabricantes");
        TableColumn<Producto, String> serviciosColumn = new TableColumn<>("Servicios");

        // Establecer las celdas de las columnas
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProductoProperty().asObject());
        nombreColumn.setCellValueFactory(cellData -> cellData.getValue().nombreProductoProperty());
        fichaColumn.setCellValueFactory(cellData -> cellData.getValue().fichaProductoProperty());
        alternoColumn.setCellValueFactory(cellData -> cellData.getValue().alternoProductoProperty());
        existenciaColumn.setCellValueFactory(cellData -> cellData.getValue().existenciaProductoProperty().asObject());

        // Columnas adicionales para mostrar los datos relacionados
        categoriasColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategoriasString()));
        proveedoresColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getProveedoresString()));
        empresasColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmpresasString()));
        clientesColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClientesString()));
        fabricantesColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFabricantesString()));
        serviciosColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getServiciosString()));

        // Agregar todas las columnas a la tabla
        productoTable.getColumns().addAll(idColumn, nombreColumn, fichaColumn, alternoColumn, existenciaColumn,
                categoriasColumn, proveedoresColumn, empresasColumn, clientesColumn,
                fabricantesColumn, serviciosColumn);

        // Cargar los productos inicialmente
        cargarProductos();

        // Filtrar productos cuando se escribe en el campo de texto
        filtroField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                productoTable.getItems().setAll(productoController.filtrarProductos(newValue));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        productoTable.setRowFactory(tv -> {
            TableRow<Producto> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Producto productoSeleccionado = row.getItem();
                    try {
                        abrirVentanaEdicion(productoSeleccionado);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return row;
        });

        // Agregar el campo de texto y la tabla al layout
        layout.getChildren().addAll(filtroField, productoTable);

        return layout;
    }

    private void abrirVentanaEdicion(Producto producto) throws IOException {
        productoController.cargarPreciosProducto(producto);
        productoController.cargarFabricantesProducto(producto);
        productoController.cargarClientesProducto(producto);
        productoController.cargarEmpresasProducto(producto);

        Stage ventanaEdicion = new Stage();
        ventanaEdicion.setTitle("Editar Producto");

        GridPane layout = new GridPane();
        layout.setPadding(new Insets(20));
        layout.setHgap(10);
        layout.setVgap(10);

        TextField nombreField = new TextField(producto.getNombreProducto());
        TextField fichaField = new TextField(producto.getFichaProducto());
        TextField alternoField = new TextField(producto.getAlternoProducto());
        TextField existenciaField = new TextField(String.valueOf(producto.getExistenciaProducto()));

        TextArea infoRelacionada = new TextArea(obtenerInformacionRelacionada(producto));
        infoRelacionada.setEditable(false);

        // Validación de datos antes de actualizar
        Button btnActualizar = new Button("Actualizar");
        btnActualizar.setOnAction(e -> {
            String nombre = nombreField.getText();
            String ficha = fichaField.getText();
            String alterno = alternoField.getText();
            String existenciaTexto = existenciaField.getText();

            // Validación de campos vacíos
            if (nombre.isEmpty() || ficha.isEmpty() || alterno.isEmpty() || existenciaTexto.isEmpty()) {
                showAlert("Error", "Todos los campos deben estar completos", Alert.AlertType.ERROR);
                return;
            }

            // Validación de existencia (debe ser un número entero positivo)
            int existencia;
            try {
                existencia = Integer.parseInt(existenciaTexto);
                if (existencia < 0) {
                    showAlert("Error", "La existencia debe ser un número entero positivo", Alert.AlertType.ERROR);
                    return;
                }
            } catch (NumberFormatException ex) {
                showAlert("Error", "La existencia debe ser un número válido", Alert.AlertType.ERROR);
                return;
            }

            // Actualizar producto
            producto.setNombreProducto(nombre);
            producto.setFichaProducto(ficha);
            producto.setAlternoProducto(alterno);
            producto.setExistenciaProducto(existencia);

            try {
                productoController.actualizarProducto(producto);
                cargarProductos(); // Cargar la lista actualizada
                ventanaEdicion.close();
            } catch (IOException | SQLException ex) {
                showAlert("Error", "No se pudo actualizar el producto", Alert.AlertType.ERROR);
                ex.printStackTrace();
            }
        });

        // Confirmación antes de eliminar
        Button btnEliminar = new Button("Eliminar");
        btnEliminar.setOnAction(e -> {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Estás seguro de que quieres eliminar este producto?");
            confirmacion.setTitle("Confirmar eliminación");
            confirmacion.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        productoController.eliminarProducto(producto.getIdProducto());
                        cargarProductos(); // Actualizar la lista
                        ventanaEdicion.close();
                    } catch (IOException | SQLException ex) {
                        showAlert("Error", "No se pudo eliminar el producto", Alert.AlertType.ERROR);
                        ex.printStackTrace();
                    }
                }
            });
        });

        HBox botones = new HBox(10, btnActualizar, btnEliminar);
        botones.setAlignment(Pos.CENTER);

        layout.add(new Label("Nombre:"), 0, 0);
        layout.add(nombreField, 1, 0);
        layout.add(new Label("Ficha:"), 0, 1);
        layout.add(fichaField, 1, 1);
        layout.add(new Label("Alterno:"), 0, 2);
        layout.add(alternoField, 1, 2);
        layout.add(new Label("Existencia:"), 0, 3);
        layout.add(existenciaField, 1, 3);
        layout.add(new Label("Información Relacionada:"), 0, 4, 2, 1);
        layout.add(infoRelacionada, 0, 5, 2, 1);
        layout.add(botones, 0, 6, 2, 1);

        Scene escena = new Scene(layout, 400, 350);
        ventanaEdicion.setScene(escena);
        ventanaEdicion.show();
    }

    private String obtenerInformacionRelacionada(Producto producto) {
        StringBuilder info = new StringBuilder();

        info.append("Proveedores:\n");
        producto.getPreciosPorProveedor()
                .forEach((proveedor, precio) -> info.append(proveedor.getNombreProv()).append(" - Precio: ")
                        .append(precio.getMonto()).append(" ")
                        .append(precio.getMoneda()).append("\n"));

        info.append("\nClientes:\n");
        producto.getPreciosPorCliente()
                .forEach((cliente, precio) -> info.append(cliente.getNombreCliente()).append(" - Precio: ")
                        .append(precio.getMonto()).append(" ")
                        .append(precio.getMoneda()).append("\n"));

        info.append("\nEmpresas:\n");
        producto.getPreciosPorEmpresa()
                .forEach((empresa, precio) -> info.append(empresa.getNombreEmpresa()).append(" - Precio: ")
                        .append(precio.getMonto()).append(" ")
                        .append(precio.getMoneda()).append("\n"));

        info.append("\nFabricantes:\n");
        producto.getPreciosPorFabricante()
                .forEach((fabricante, precio) -> info.append(fabricante.getNombreFabricante()).append(" - Precio: ")
                        .append(precio.getMonto()).append(" ")
                        .append(precio.getMoneda()).append("\n"));

        return info.toString();
    }

    private void cargarProductos() throws SQLException {
        try {
            ProductoController productoController = new ProductoController();
            List<Producto> productos = productoController.consultarProductos();

            // Agregar los productos con sus datos relacionados a la tabla
            for (Producto producto : productos) {
                productoTable.getItems().add(producto);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mostrarFormularioRegistro() {
        root.getChildren().set(1, formularioRegistro);
    }

    private void mostrarConsultaProductos() throws SQLException {
        cargarProductos();
        root.getChildren().set(1, consultaProductos);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void mostrarMenuPrincipal(Stage primaryStage) {
        MenuPrincipalScreen menuPrincipalScreen = new MenuPrincipalScreen(primaryStage, usuarioActual);
        menuPrincipalScreen.mostrarMenu();
    }
}
*/