/*package com.vluevano.controller;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.vluevano.database.DatabaseConnection;
import com.vluevano.model.Categoria;
import com.vluevano.model.Cliente;

public class ClienteController {

    // Método para registrar cliente desde formulario
    public void registrarCliente(Cliente cliente, List<Categoria> categorias) {

        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (cliente.getNombreCliente() == null || cliente.getNombreCliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        if (!cliente.getRfcCliente().matches("[A-Za-z0-9]{13}")) {
            throw new IllegalArgumentException("El RFC debe contener 13 caracteres alfanuméricos.");
        }
        if (!cliente.getTelefonoCliente().matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 10 dígitos.");
        }
        if (!cliente.getCorreoCliente().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (String.valueOf(cliente.getCpCliente()).length() != 5) {
            throw new IllegalArgumentException("El código postal debe tener 5 dígitos.");
        }
        if (categorias == null) {
            categorias = new ArrayList<>();
        }

        String sqlCliente = "INSERT INTO cliente (nombrecliente, nombrefcliente, cpcliente, noExtCliente, noIntCliente, rfcCliente, municipio, estado, calle, colonia, ciudad, pais, telefonoCliente, correoCliente, curpCliente, pfisicaCliente) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING idCliente";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmtCliente = conn.prepareStatement(sqlCliente,
                        Statement.RETURN_GENERATED_KEYS)) {

            stmtCliente.setString(1, cliente.getNombreCliente());
            stmtCliente.setString(2, cliente.getNombreFiscal());
            stmtCliente.setInt(3, cliente.getCpCliente());
            stmtCliente.setInt(4, cliente.getNoExtCliente());
            stmtCliente.setInt(5, cliente.getNoIntCliente());
            stmtCliente.setString(6, cliente.getRfcCliente());
            stmtCliente.setString(7, cliente.getMunicipio());
            stmtCliente.setString(8, cliente.getEstado());
            stmtCliente.setString(9, cliente.getCalle());
            stmtCliente.setString(10, cliente.getColonia());
            stmtCliente.setString(11, cliente.getCiudad());
            stmtCliente.setString(12, cliente.getPais());
            stmtCliente.setString(13, cliente.getTelefonoCliente());
            stmtCliente.setString(14, cliente.getCorreoCliente());
            stmtCliente.setString(15, cliente.getCurp());
            stmtCliente.setBoolean(16, cliente.isEsPersonaFisica());
            stmtCliente.executeUpdate();

            ResultSet generatedKeys = stmtCliente.getGeneratedKeys();
            int idCliente = -1;
            if (generatedKeys.next()) {
                idCliente = generatedKeys.getInt(1);
            }

            for (Categoria categoria : categorias) {
                int idCategoria = obtenerOCrearCategoria(categoria);
                asociarClienteConCategoria(idCliente, idCategoria);
            }

            System.out.println("Cliente registrado exitosamente.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.out.println("Error al registrar cliente.");
        }
    }

    // Método para obtener o registrar una categoría
    private int obtenerOCrearCategoria(Categoria categoria) throws SQLException, IOException {

        if (categoria == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        if (categoria.getNombreCategoria() == null || categoria.getNombreCategoria().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }
        if (categoria.getDescripcionCategoria() == null) {
            categoria.setDescripcionCategoria("Sin descripción");
        }

        String sqlObtenerId = "SELECT idCategoria FROM categoria WHERE nombreCategoria = ?";
        String sqlInsertCategoria = "INSERT INTO categoria (nombrecategoria, desccategoria) VALUES (?, ?) ON CONFLICT (nombrecategoria) DO NOTHING RETURNING idcategoria";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmtObtenerId = conn.prepareStatement(sqlObtenerId);
                PreparedStatement stmtInsertCategoria = conn.prepareStatement(sqlInsertCategoria)) {

            // Verificar si la categoría ya existe
            stmtObtenerId.setString(1, categoria.getNombreCategoria());
            ResultSet rs = stmtObtenerId.executeQuery();
            if (rs.next()) {
                return rs.getInt("idCategoria");
            }

            // Si no existe, insertarla
            stmtInsertCategoria.setString(1, categoria.getNombreCategoria());
            stmtInsertCategoria.setString(2, categoria.getDescripcionCategoria());
            ResultSet generatedKeys = stmtInsertCategoria.executeQuery();
            if (generatedKeys.next()) {
                return generatedKeys.getInt("idCategoria");
            }
        }
        return -1;
    }

    // Método para asociar cliente con categoría
    private void asociarClienteConCategoria(int idCliente, int idCategoria) throws SQLException, IOException {
        String sql = "INSERT INTO clientecategoria (idcliente, idcategoria) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCliente);
            stmt.setInt(2, idCategoria);
            stmt.executeUpdate();
        }
    }

    // Método para modificar un cliente
    public void modificarCliente(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo.");
        }
        if (cliente.getIdCliente() <= 0) {
            throw new IllegalArgumentException("El ID del cliente debe ser mayor a 0.");
        }
        if (cliente.getNombreCliente() == null || cliente.getNombreCliente().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es obligatorio.");
        }
        if (!cliente.getRfcCliente().matches("[A-Za-z0-9]{13}")) {
            throw new IllegalArgumentException("El RFC debe contener 13 caracteres alfanuméricos.");
        }
        if (!cliente.getTelefonoCliente().matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 10 dígitos.");
        }
        if (!cliente.getCorreoCliente().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (String.valueOf(cliente.getCpCliente()).length() != 5) {
            throw new IllegalArgumentException("El código postal debe tener 5 dígitos.");
        }

        String sql = "UPDATE cliente SET nombrecliente = ?, nombrefcliente = ?, cpcliente = ?, noExtCliente = ?, noIntCliente = ?, rfcCliente = ?, municipio = ?, estado = ?, calle = ?, colonia = ?, ciudad = ?, pais = ?, telefonoCliente = ?, correocliente = ?, curpCliente = ?, pfisicaCliente = ? WHERE idcliente = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getNombreCliente());
            stmt.setString(2, cliente.getNombreFiscal());
            stmt.setInt(3, cliente.getCpCliente());
            stmt.setInt(4, cliente.getNoExtCliente());
            stmt.setInt(5, cliente.getNoIntCliente());
            stmt.setString(6, cliente.getRfcCliente());
            stmt.setString(7, cliente.getMunicipio());
            stmt.setString(8, cliente.getEstado());
            stmt.setString(9, cliente.getCalle());
            stmt.setString(10, cliente.getColonia());
            stmt.setString(11, cliente.getCiudad());
            stmt.setString(12, cliente.getPais());
            stmt.setString(13, cliente.getTelefonoCliente());
            stmt.setString(14, cliente.getCorreoCliente());
            stmt.setString(15, cliente.getCurp());
            stmt.setBoolean(16, cliente.isEsPersonaFisica());
            stmt.setInt(17, cliente.getIdCliente());
            stmt.executeUpdate();

            System.out.println("Cliente actualizado correctamente.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.out.println("Error al modificar cliente.");
        }
    }

    public List<Cliente> buscarClientes(String filtro) {

        if (filtro == null || filtro.trim().isEmpty()) {
            filtro = "1=1";
        }

        List<Cliente> clientes = new ArrayList<>();
        String[] filtros = filtro.split(","); // Separar por comas

        // Crear condiciones dinámicas para la búsqueda de clientes
        StringBuilder sql = new StringBuilder("SELECT cl.* FROM cliente cl WHERE ");
        List<String> condiciones = new ArrayList<>();

        // Condiciones de búsqueda por cada filtro
        for (String palabra : filtros) {
            palabra = palabra.trim();

            if (palabra.matches("\\d+")) { // Filtrar por ID
                condiciones.add("cl.idCliente = ?");
            } else if (palabra.matches("[a-zA-Z]+")) { // Filtrar por categoría
                condiciones.add(
                        "cl.idCliente IN (SELECT cl.idCliente FROM clientecategoria ca INNER JOIN categoria c ON ca.idCategoria = c.idCategoria WHERE c.nombreCategoria ILIKE ?)");
            } else if (palabra.matches("\\d{10}")) { // Filtrar por teléfono
                condiciones.add("cl.telefonoCliente = ?");
            } else if (palabra.matches("[A-Za-z0-9]{13}")) { // Filtrar por RFC
                condiciones.add("cl.rfcCliente = ?");
            } else {
                // Filtrar por nombre, estado, municipio
                condiciones.add(
                        "cl.nombreCliente ILIKE ? OR cl.estado ILIKE ? OR cl.municipio ILIKE ? OR cl.rfcCliente ILIKE ?");
            }
        }

        // Si no hay filtros, traer todos los clientes
        if (condiciones.isEmpty()) {
            sql.append("1=1"); // Agregar condición que siempre es verdadera
        } else {
            // Unir las condiciones con 'OR' si hay filtros
            sql.append(String.join(" OR ", condiciones));
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            for (String palabra : filtros) {
                palabra = palabra.trim();

                if (palabra.matches("\\d+")) {
                    stmt.setInt(index++, Integer.parseInt(palabra)); // Filtrar por ID
                } else if (palabra.matches("[a-zA-Z]+")) {
                    stmt.setString(index++, "%" + palabra + "%"); // Filtrar por categoría
                } else if (palabra.matches("\\d{10}")) {
                    stmt.setString(index++, palabra); // Filtrar por teléfono
                } else if (palabra.matches("[A-Za-z0-9]{13}")) {
                    stmt.setString(index++, palabra); // Filtrar por RFC
                } else {
                    // Filtrar por nombre, estado, municipio
                    stmt.setString(index++, "%" + palabra + "%");
                    stmt.setString(index++, "%" + palabra + "%");
                    stmt.setString(index++, "%" + palabra + "%");
                    stmt.setString(index++, "%" + palabra + "%");
                }
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Cliente cliente = new Cliente(
                        rs.getInt("idCliente"),
                        rs.getString("nombreCliente"),
                        rs.getString("nombrefCliente"),
                        rs.getInt("cpCliente"),
                        rs.getInt("noExtCliente"),
                        rs.getInt("noIntCliente"),
                        rs.getString("rfcCliente"),
                        rs.getString("municipio"),
                        rs.getString("estado"),
                        rs.getString("calle"),
                        rs.getString("colonia"),
                        rs.getString("ciudad"),
                        rs.getString("pais"),
                        rs.getString("telefonoCliente"),
                        rs.getString("correoCliente"),
                        rs.getString("curpCliente"),
                        rs.getBoolean("pfisicaCliente"));

                // Inicializar lista de categorías vacía
                List<Categoria> categorias = new ArrayList<>();

                // Consulta para obtener las categorías asociadas al clientes
                String categoriaSql = """
                        SELECT c.idCategoria, c.nombreCategoria
                        FROM categoria c
                        INNER JOIN clientecategoria ca ON c.idCategoria = ca.idCategoria
                        WHERE ca.idCliente = ?""";

                try (PreparedStatement stmtCategorias = conn.prepareStatement(categoriaSql)) {
                    stmtCategorias.setInt(1, cliente.getIdCliente());
                    ResultSet rsCategorias = stmtCategorias.executeQuery();

                    while (rsCategorias.next()) {
                        int idCat = rsCategorias.getInt("idCategoria");
                        String nombreCat = rsCategorias.getString("nombreCategoria");
                        Categoria categoria = new Categoria(idCat, nombreCat);
                        categorias.add(categoria);
                    }
                }

                // Asignar categorías solo si se encontraron
                if (!categorias.isEmpty()) {
                    cliente.setCategorias(categorias);
                }

                // Agregar clientes a la lista
                clientes.add(cliente);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return clientes;
    }

    // Método para eliminar clientees
    public void eliminarCliente(int idCliente) {

        if (idCliente <= 0) {
            throw new IllegalArgumentException("El ID del cliente debe ser mayor a 0.");
        }

        String sqlEliminarCategoria = "DELETE FROM clientecategoria WHERE idcliente = ?";
        String sqlEliminarCliente = "DELETE FROM cliente WHERE idCliente = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Iniciar una transacción
            conn.setAutoCommit(false);

            try (PreparedStatement stmtCategoria = conn.prepareStatement(sqlEliminarCategoria);
                    PreparedStatement stmtCliente = conn.prepareStatement(sqlEliminarCliente)) {

                // Eliminar las categorías asociadas con el clientes
                stmtCategoria.setInt(1, idCliente);
                stmtCategoria.executeUpdate();

                // Eliminar el clientes
                stmtCliente.setInt(1, idCliente);
                stmtCliente.executeUpdate();

                // Si todo va bien, confirmar la transacción
                conn.commit();
                System.out.println("Cliente y su categoría eliminados exitosamente.");
            } catch (SQLException e) {
                // Si ocurre un error, deshacer la transacción
                conn.rollback();
                e.printStackTrace();
                System.out.println("Error al eliminar cliente y su categoría.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.out.println("Error al eliminar cliente.");
        }
    }

    // Método para consultar todos los clientes
    public List<Cliente> consultarTodosClientes() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            Map<Integer, Cliente> clientesMap = new HashMap<>(); // Usar un mapa para evitar duplicados

            while (rs.next()) {
                // Crear cliente
                Cliente cliente = new Cliente(
                        rs.getInt("idCliente"),
                        rs.getString("nombreCliente"),
                        rs.getString("nombrefCliente"),
                        rs.getInt("cpCliente"),
                        rs.getInt("noExtCliente"),
                        rs.getInt("noIntCliente"),
                        rs.getString("rfcCliente"),
                        rs.getString("municipio"),
                        rs.getString("estado"),
                        rs.getString("calle"),
                        rs.getString("colonia"),
                        rs.getString("ciudad"),
                        rs.getString("pais"),
                        rs.getString("telefonoCliente"),
                        rs.getString("correoCliente"),
                        rs.getString("curpCliente"),
                        rs.getBoolean("pfisicaCliente"));

                // Si el cliente ya está en el mapa, usamos el existente y agregamos las
                // categorías
                Cliente existingCliente = clientesMap.get(cliente.getIdCliente());
                if (existingCliente == null) {
                    clientesMap.put(cliente.getIdCliente(), cliente);
                } else {
                    cliente = existingCliente; // Usar el cliente existente
                }

                // Consulta para obtener las categorías asociadas al cliente
                String categoriaSql = """
                        SELECT c.idCategoria, c.nombreCategoria
                        FROM categoria c
                        INNER JOIN clientecategoria ca ON c.idCategoria = ca.idCategoria
                        WHERE ca.idCliente = ?""";

                try (PreparedStatement stmtCategorias = conn.prepareStatement(categoriaSql)) {
                    stmtCategorias.setInt(1, cliente.getIdCliente());
                    ResultSet rsCategorias = stmtCategorias.executeQuery();

                    List<Categoria> categorias = new ArrayList<>();
                    while (rsCategorias.next()) {
                        int idCat = rsCategorias.getInt("idCategoria");
                        String nombreCat = rsCategorias.getString("nombreCategoria");
                        categorias.add(new Categoria(idCat, nombreCat));
                    }

                    if (!categorias.isEmpty()) {
                        cliente.setCategorias(categorias); // Usar lista de categorías
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            // Convertir el mapa a una lista de clientes sin duplicados
            clientes.addAll(clientesMap.values());

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return clientes;
    }

    // Método para registrar clientes desde archivo Excel
    public void registrarClientesDesdeExcel(File excelFile) {
        StringBuilder errores = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() <= 1) {
                showAlert(Alert.AlertType.WARNING, "El archivo Excel está vacío o solo tiene encabezados.");
                return;
            }

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                try {
                    // Usar el método obtenerValorCelda para obtener los valores de las celdas
                    String nombre = getStringCellValue(row.getCell(0));
                    String nombrefiscal = getStringCellValue(row.getCell(1));
                    String cpStr = getStringCellValue(row.getCell(2));
                    String noExtStr = getStringCellValue(row.getCell(3));
                    String noIntStr = getStringCellValue(row.getCell(4));
                    String rfc = getStringCellValue(row.getCell(5));
                    String municipio = getStringCellValue(row.getCell(6));
                    String estado = getStringCellValue(row.getCell(7));
                    String calle = getStringCellValue(row.getCell(8));
                    String colonia = getStringCellValue(row.getCell(9));
                    String ciudad = getStringCellValue(row.getCell(10));
                    String pais = getStringCellValue(row.getCell(11));
                    String telefono = getStringCellValue(row.getCell(12));
                    String correo = getStringCellValue(row.getCell(13));
                    String curp = getStringCellValue(row.getCell(14));
                    String esFisicaStr = (getStringCellValue(row.getCell(15)));

                    // Validaciones previas
                    if (nombre.isEmpty() || cpStr.isEmpty() || rfc.isEmpty() || telefono.isEmpty()
                            || correo.isEmpty()) {
                        errores.append("Fila ").append(row.getRowNum()).append(": Faltan datos obligatorios.\n");
                    }
                    if (!cpStr.matches("\\d{5}")) {
                        errores.append("Fila ").append(row.getRowNum()).append(": Código Postal inválido.\n");
                    }
                    if (!noExtStr.matches("\\d*")) {
                        errores.append("Fila ").append(row.getRowNum()).append(": Número exterior inválido.\n");
                    }
                    if (!noIntStr.matches("\\d*")) {
                        errores.append("Fila ").append(row.getRowNum()).append(": Número interior inválido.\n");
                    }
                    if (!rfc.matches("[A-Za-z0-9]{13}")) {
                        errores.append("Fila ").append(row.getRowNum()).append(": RFC inválido.\n");
                    }
                    if (!telefono.matches("\\d{10}")) {
                        errores.append("Fila ").append(row.getRowNum()).append(": Teléfono inválido.\n");
                    }
                    if (!correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                        errores.append("Fila ").append(row.getRowNum()).append(": Correo electrónico inválido.\n");
                    }
                    if (!curp.isEmpty() && !curp.matches("[A-Z0-9]{18}")) {
                        errores.append("Fila ").append(row.getRowNum()).append(": CURP inválida.\n");
                    }

                    // Si hubo errores, continuar con la siguiente fila
                    if (errores.length() > 0) {
                        continue;
                    }

                    // Convertir valores numéricos
                    int cp = Integer.parseInt(cpStr);
                    int noExt = noExtStr.isEmpty() ? 0 : Integer.parseInt(noExtStr);
                    int noInt = noIntStr.isEmpty() ? 0 : Integer.parseInt(noIntStr);

                    // Validación de esFisica
                    boolean esFisica = Boolean.parseBoolean(esFisicaStr);

                    Cliente cliente = new Cliente(0, nombre, nombrefiscal, cp, noExt,
                            noInt, rfc, municipio, estado, calle, colonia, ciudad, pais, telefono,
                            correo, curp, esFisica);

                    List<Categoria> categorias = new ArrayList<>();
                    for (int i = 16; i < row.getLastCellNum(); i++) {
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (cell != null && cell.getCellType() == CellType.STRING) {
                            String categoriaNombre = getStringCellValue(cell).trim();
                            if (!categoriaNombre.isEmpty()) {
                                // Directamente se agrega la categoría sin obtener el ID
                                categorias.add(new Categoria(0, categoriaNombre, "Descripción de " + categoriaNombre));
                            }
                        }
                    }

                    // Ahora solo llamas al método registrarCliente una sola vez
                    registrarCliente(cliente, categorias);

                } catch (Exception e) {
                    errores.append("Error procesando fila ").append(row.getRowNum()).append(": ").append(e.getMessage())
                            .append("\n");
                }
            }

            if (errores.length() > 0) {
                showAlert(Alert.AlertType.ERROR, errores.toString());
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Clientes registrados desde Excel.");
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error al leer el archivo Excel: " + e.getMessage());
        }
    }

    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setContentText(message);
        alert.show();
    }

}*/
