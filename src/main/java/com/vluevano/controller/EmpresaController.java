package com.vluevano.controller;

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
import com.vluevano.model.Empresa;

public class EmpresaController {

    // Método para registrar empresa desde formulario
    public void registrarEmpresa(Empresa empresa, List<Categoria> categorias) {

        if (empresa == null) {
            throw new IllegalArgumentException("El empresa no puede ser nulo.");
        }
        if (empresa.getNombreEmpresa() == null || empresa.getNombreEmpresa().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del empresa es obligatorio.");
        }
        if (!empresa.getRfcEmpresa().matches("[A-Za-z0-9]{13}")) {
            throw new IllegalArgumentException("El RFC debe contener 13 caracteres alfanuméricos.");
        }
        if (!empresa.getTelefonoEmpresa().matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 10 dígitos.");
        }
        if (!empresa.getCorreoEmpresa().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (String.valueOf(empresa.getCpEmpresa()).length() != 5) {
            throw new IllegalArgumentException("El código postal debe tener 5 dígitos.");
        }
        if (categorias == null) {
            categorias = new ArrayList<>();
        }

        String sqlEmpresa = "INSERT INTO empresa (nombreemp, cpEmpresa, noExtEmpresa, noIntEmpresa, rfcEmpresa, municipio, estado, calle, colonia, ciudad, pais, telefonoEmpresa, correoEmpresa, curpempresa, pfisicaempresa) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING idEmpresa";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmtEmpresa = conn.prepareStatement(sqlEmpresa,
                        Statement.RETURN_GENERATED_KEYS)) {

            stmtEmpresa.setString(1, empresa.getNombreEmpresa());
            stmtEmpresa.setInt(2, empresa.getCpEmpresa());
            stmtEmpresa.setInt(3, empresa.getNoExtEmpresa());
            stmtEmpresa.setInt(4, empresa.getNoIntEmpresa());
            stmtEmpresa.setString(5, empresa.getRfcEmpresa());
            stmtEmpresa.setString(6, empresa.getMunicipio());
            stmtEmpresa.setString(7, empresa.getEstado());
            stmtEmpresa.setString(8, empresa.getCalle());
            stmtEmpresa.setString(9, empresa.getColonia());
            stmtEmpresa.setString(10, empresa.getCiudad());
            stmtEmpresa.setString(11, empresa.getPais());
            stmtEmpresa.setString(12, empresa.getTelefonoEmpresa());
            stmtEmpresa.setString(13, empresa.getCorreoEmpresa());
            stmtEmpresa.setString(14, empresa.getCurp());
            stmtEmpresa.setBoolean(15, empresa.isEsPersonaFisica());
            stmtEmpresa.executeUpdate();

            ResultSet generatedKeys = stmtEmpresa.getGeneratedKeys();
            int idEmpresa = -1;
            if (generatedKeys.next()) {
                idEmpresa = generatedKeys.getInt(1);
            }

            for (Categoria categoria : categorias) {
                int idCategoria = obtenerOCrearCategoria(categoria);
                asociarEmpresaConCategoria(idEmpresa, idCategoria);
            }

            System.out.println("Empresa registrado exitosamente.");

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.out.println("Error al registrar empresa.");
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

    // Método para asociar empresa con categoría
    private void asociarEmpresaConCategoria(int idEmpresa, int idCategoria) throws SQLException, IOException {

        if (idEmpresa <= 0) {
            throw new IllegalArgumentException("El ID del empresa debe ser mayor a 0.");
        }
        if (idCategoria <= 0) {
            throw new IllegalArgumentException("El ID de la categoría debe ser mayor a 0.");
        }

        String sql = "INSERT INTO empresacategoria (idempresa, idcategoria) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idEmpresa);
            stmt.setInt(2, idCategoria);
            stmt.executeUpdate();
        }
    }

    // Método para modificar un empresa
    public void modificarEmpresa(Empresa empresa) {

        if (empresa == null) {
            throw new IllegalArgumentException("El empresa no puede ser nulo.");
        }
        if (empresa.getIdEmpresa() <= 0) {
            throw new IllegalArgumentException("El ID del empresa debe ser mayor a 0.");
        }
        if (empresa.getNombreEmpresa() == null || empresa.getNombreEmpresa().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del empresa es obligatorio.");
        }
        if (!empresa.getRfcEmpresa().matches("[A-Za-z0-9]{13}")) {
            throw new IllegalArgumentException("El RFC debe contener 13 caracteres alfanuméricos.");
        }
        if (!empresa.getTelefonoEmpresa().matches("\\d{10}")) {
            throw new IllegalArgumentException("El teléfono debe tener exactamente 10 dígitos.");
        }
        if (!empresa.getCorreoEmpresa().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("El correo electrónico no es válido.");
        }
        if (String.valueOf(empresa.getCpEmpresa()).length() != 5) {
            throw new IllegalArgumentException("El código postal debe tener 5 dígitos.");
        }

        String sql = "UPDATE empresa SET nombreemp = ?, cpempresa = ?, noExtEmpresa = ?, noIntEmpresa = ?, rfcEmpresa = ?, municipio = ?, estado = ?, calle = ?, colonia = ?, ciudad = ?, pais = ?, telefonoEmpresa = ?, correoempresa = ?, curpempresa = ?, pfisicaempresa = ? WHERE idempresa = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, empresa.getNombreEmpresa());
            stmt.setInt(2, empresa.getCpEmpresa());
            stmt.setInt(3, empresa.getNoExtEmpresa());
            stmt.setInt(4, empresa.getNoIntEmpresa());
            stmt.setString(5, empresa.getRfcEmpresa());
            stmt.setString(6, empresa.getMunicipio());
            stmt.setString(7, empresa.getEstado());
            stmt.setString(8, empresa.getCalle());
            stmt.setString(9, empresa.getColonia());
            stmt.setString(10, empresa.getCiudad());
            stmt.setString(11, empresa.getPais());
            stmt.setString(12, empresa.getTelefonoEmpresa());
            stmt.setString(13, empresa.getCorreoEmpresa());
            stmt.setString(14, empresa.getCurp());
            stmt.setBoolean(15, empresa.isEsPersonaFisica());
            stmt.setInt(16, empresa.getIdEmpresa());
            stmt.executeUpdate();

            System.out.println("Empresa actualizado correctamente.");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.out.println("Error al modificar empresa.");
        }
    }

    public List<Empresa> buscarEmpresas(String filtro) {

        if (filtro == null || filtro.trim().isEmpty()) {
            filtro = "1=1"; // Devolver todos los empresas
        }

        List<Empresa> empresas = new ArrayList<>();
        String[] filtros = filtro.split(","); // Separar por comas

        // Crear condiciones dinámicas para la búsqueda de empresas
        StringBuilder sql = new StringBuilder("SELECT p.* FROM empresa p WHERE ");
        List<String> condiciones = new ArrayList<>();

        // Condiciones de búsqueda por cada filtro
        for (String palabra : filtros) {
            palabra = palabra.trim();

            if (palabra.matches("\\d+")) { // Filtrar por ID
                condiciones.add("p.idEmpresa = ?");
            } else if (palabra.matches("[a-zA-Z]+")) { // Filtrar por categoría
                condiciones.add(
                        "p.idEmpresa IN (SELECT pc.idEmpresa FROM empresacategoria pc INNER JOIN categoria c ON pc.idCategoria = c.idCategoria WHERE c.nombreCategoria ILIKE ?)");
            } else if (palabra.matches("\\d{10}")) { // Filtrar por teléfono
                condiciones.add("p.telefonoEmpresa = ?");
            } else if (palabra.matches("[A-Za-z0-9]{13}")) { // Filtrar por RFC
                condiciones.add("p.rfcEmpresa = ?");
            } else {
                // Filtrar por nombre, estado, municipio
                condiciones.add(
                        "p.nombreemp ILIKE ? OR p.estado ILIKE ? OR p.municipio ILIKE ? OR p.rfcEmpresa ILIKE ?");
            }
        }

        // Si no hay filtros, traer todos los empresas
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
                Empresa empresa = new Empresa(
                        rs.getInt("idEmpresa"),
                        rs.getString("nombreEmp"),
                        rs.getInt("cpEmpresa"),
                        rs.getInt("noExtEmpresa"),
                        rs.getInt("noIntEmpresa"),
                        rs.getString("rfcEmpresa"),
                        rs.getString("municipio"),
                        rs.getString("estado"),
                        rs.getString("calle"),
                        rs.getString("colonia"),
                        rs.getString("ciudad"),
                        rs.getString("pais"),
                        rs.getString("telefonoEmpresa"),
                        rs.getString("correoEmpresa"),
                        rs.getString("curpempresa"),
                        rs.getBoolean("pfisicaempresa"));

                // Inicializar lista de categorías vacía
                List<Categoria> categorias = new ArrayList<>();

                // Consulta para obtener las categorías asociadas al empresa
                String categoriaSql = """
                        SELECT c.idCategoria, c.nombreCategoria
                        FROM categoria c
                        INNER JOIN empresacategoria pc ON c.idCategoria = pc.idCategoria
                        WHERE pc.idEmpresa = ?""";

                try (PreparedStatement stmtCategorias = conn.prepareStatement(categoriaSql)) {
                    stmtCategorias.setInt(1, empresa.getIdEmpresa());
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
                    empresa.setCategorias(categorias);
                }

                // Agregar empresa a la lista
                empresas.add(empresa);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return empresas;
    }

    // Método para eliminar empresa
    public void eliminarEmpresa(int idEmpresa) throws SQLException, IOException {

        if (idEmpresa <= 0) {
            throw new IllegalArgumentException("El ID del empresa debe ser mayor a 0.");
        }

        // Verificar si el empresa existe antes de eliminarlo
        String sqlVerificar = "SELECT COUNT(*) FROM empresa WHERE idEmpresa = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlVerificar)) {
            stmt.setInt(1, idEmpresa);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                throw new IllegalArgumentException("El empresa con ID " + idEmpresa + " no existe.");
            }
        }

        String sqlEliminarCategoria = "DELETE FROM empresacategoria WHERE idempresa = ?";
        String sqlEliminarEmpresa = "DELETE FROM empresa WHERE idEmpresa = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Iniciar una transacción
            conn.setAutoCommit(false);

            try (PreparedStatement stmtCategoria = conn.prepareStatement(sqlEliminarCategoria);
                    PreparedStatement stmtEmpresa = conn.prepareStatement(sqlEliminarEmpresa)) {

                // Eliminar las categorías asociadas con el empresa
                stmtCategoria.setInt(1, idEmpresa);
                stmtCategoria.executeUpdate();

                // Eliminar el empresa
                stmtEmpresa.setInt(1, idEmpresa);
                stmtEmpresa.executeUpdate();

                // Si todo va bien, confirmar la transacción
                conn.commit();
                System.out.println("Empresa y su categoría eliminados exitosamente.");
            } catch (SQLException e) {
                // Si ocurre un error, deshacer la transacción
                conn.rollback();
                e.printStackTrace();
                System.out.println("Error al eliminar empresa y su categoría.");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            System.out.println("Error al eliminar empresa.");
        }
    }

    // Método para consultar todos los empresas
    public List<Empresa> consultarTodosEmpresas() {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT * FROM empresa";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            Map<Integer, Empresa> empresasMap = new HashMap<>(); // Usar un mapa para evitar duplicados

            while (rs.next()) {
                // Crear empresa
                Empresa empresa = new Empresa(
                        rs.getInt("idEmpresa"),
                        rs.getString("nombreEmp"),
                        rs.getInt("cpEmpresa"),
                        rs.getInt("noExtEmpresa"),
                        rs.getInt("noIntEmpresa"),
                        rs.getString("rfcEmpresa"),
                        rs.getString("municipio"),
                        rs.getString("estado"),
                        rs.getString("calle"),
                        rs.getString("colonia"),
                        rs.getString("ciudad"),
                        rs.getString("pais"),
                        rs.getString("telefonoEmpresa"),
                        rs.getString("correoEmpresa"),
                        rs.getString("curpempresa"),
                        rs.getBoolean("pfisicaempresa"));

                // Si el empresa ya está en el mapa, usamos el existente y agregamos las
                // categorías
                Empresa existingEmpresa = empresasMap.get(empresa.getIdEmpresa());
                if (existingEmpresa == null) {
                    empresasMap.put(empresa.getIdEmpresa(), empresa);
                } else {
                    empresa = existingEmpresa; // Usar el empresa existente
                }

                // Consulta para obtener las categorías asociadas al empresa
                String categoriaSql = """
                        SELECT c.idCategoria, c.nombreCategoria
                        FROM categoria c
                        INNER JOIN empresacategoria pc ON c.idCategoria = pc.idCategoria
                        WHERE pc.idEmpresa = ?""";

                try (PreparedStatement stmtCategorias = conn.prepareStatement(categoriaSql)) {
                    stmtCategorias.setInt(1, empresa.getIdEmpresa());
                    ResultSet rsCategorias = stmtCategorias.executeQuery();

                    List<Categoria> categorias = new ArrayList<>();
                    while (rsCategorias.next()) {
                        int idCat = rsCategorias.getInt("idCategoria");
                        String nombreCat = rsCategorias.getString("nombreCategoria");
                        categorias.add(new Categoria(idCat, nombreCat));
                    }

                    if (!categorias.isEmpty()) {
                        empresa.setCategorias(categorias); // Usar lista de categorías
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }

            // Convertir el mapa a una lista de empresas sin duplicados
            empresas.addAll(empresasMap.values());

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }

        return empresas;
    }

    public void registrarEmpresaDesdeExcel(File excelFile) {
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
                    // Obtener valores de celdas con validaciones
                    String nombre = getStringCellValue(row.getCell(0)).trim();
                    String cpStr = getStringCellValue(row.getCell(1)).trim();
                    String noExtStr = getStringCellValue(row.getCell(2)).trim();
                    String noIntStr = getStringCellValue(row.getCell(3)).trim();
                    String rfc = getStringCellValue(row.getCell(4)).trim();
                    String municipio = getStringCellValue(row.getCell(5)).trim();
                    String estado = getStringCellValue(row.getCell(6)).trim();
                    String calle = getStringCellValue(row.getCell(7)).trim();
                    String colonia = getStringCellValue(row.getCell(8)).trim();
                    String ciudad = getStringCellValue(row.getCell(9)).trim();
                    String pais = getStringCellValue(row.getCell(10)).trim();
                    String telefono = getStringCellValue(row.getCell(11)).trim();
                    String correo = getStringCellValue(row.getCell(12)).trim();
                    String curp = getStringCellValue(row.getCell(13)).trim();
                    String esFisicaStr = getStringCellValue(row.getCell(14)).trim();

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
                    boolean esFisica = esFisicaStr.equalsIgnoreCase("true");

                    Empresa empresa = new Empresa(0, nombre, cp, noExt, noInt, rfc, municipio, estado, calle,
                            colonia, ciudad, pais, telefono, correo, curp, esFisica);

                    // Procesar categorías
                    List<Categoria> categorias = new ArrayList<>();
                    for (int i = 15; i < row.getLastCellNum(); i++) {
                        Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                        if (cell != null && cell.getCellType() == CellType.STRING) {
                            String categoriaNombre = getStringCellValue(cell).trim();
                            if (!categoriaNombre.isEmpty()) {
                                categorias.add(new Categoria(0, categoriaNombre, "Descripción de " + categoriaNombre));
                            }
                        }
                    }

                    // Registrar empresa con categorías
                    registrarEmpresa(empresa, categorias);

                } catch (Exception e) {
                    errores.append("Error procesando fila ").append(row.getRowNum()).append(": ").append(e.getMessage())
                            .append("\n");
                }
            }

            if (errores.length() > 0) {
                showAlert(Alert.AlertType.ERROR, errores.toString());
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Empresas registrados desde Excel.");
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

}
