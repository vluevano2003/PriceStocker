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
import com.vluevano.model.Cliente;
import com.vluevano.model.Empresa;
import com.vluevano.model.Fabricante;
import com.vluevano.model.Precio;
import com.vluevano.model.PrestadorServicio;
import com.vluevano.model.Producto;
import com.vluevano.model.Proveedor;
import com.vluevano.model.Servicio;

public class ProductoController {

    public void registrarProducto(Producto producto, List<Categoria> categorias, List<Empresa> empresas,
            List<Proveedor> proveedores, List<Fabricante> fabricantes, List<Cliente> clientes,
            List<Servicio> servicios,
            List<Double> preciosEmpresas, List<Double> preciosProveedores, List<Double> preciosFabricantes,
            List<Double> preciosClientes, String moneda)
            throws IOException {
        String sql = "INSERT INTO producto (nombreProducto, fichaproducto, alternoproducto, existenciaproducto) VALUES (?, ?, ?, ?) RETURNING idProducto";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, producto.getNombreProducto());
            stmt.setString(2, producto.getFichaProducto());
            stmt.setString(3, producto.getAlternoProducto());
            stmt.setInt(4, producto.getExistenciaProducto());
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int idProducto = generatedKeys.getInt(1);
                asociarProductoConEntidades(idProducto, categorias, empresas, proveedores, fabricantes, clientes,
                        servicios, preciosEmpresas, preciosProveedores, preciosFabricantes, preciosClientes, moneda);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void asociarProductoConEntidades(int idProducto, List<Categoria> categorias, List<Empresa> empresas,
            List<Proveedor> proveedores, List<Fabricante> fabricantes, List<Cliente> clientes,
            List<Servicio> servicios,
            List<Double> preciosEmpresas, List<Double> preciosProveedores, List<Double> preciosFabricantes,
            List<Double> preciosClientes, String moneda)
            throws SQLException, IOException {

        int index = 0;
        for (Categoria categoria : categorias) {
            asociarProductoConCategoria(idProducto, categoria.getIdCategoria());
        }

        for (Empresa empresa : empresas) {
            asociarProductoConEmpresa(idProducto, empresa.getIdEmpresa(), preciosEmpresas.get(index), moneda);
            index++;
        }

        index = 0;
        for (Proveedor proveedor : proveedores) {
            asociarProductoConProveedor(idProducto, proveedor.getIdProveedor(), preciosProveedores.get(index), moneda);
            index++;
        }

        index = 0;
        for (Fabricante fabricante : fabricantes) {
            asociarProductoConFabricante(idProducto, fabricante.getIdFabricante(), preciosFabricantes.get(index),
                    moneda);
            index++;
        }

        index = 0;
        for (Cliente cliente : clientes) {
            asociarProductoConCliente(idProducto, cliente.getIdCliente(), preciosClientes.get(index), moneda);
            index++;
        }

        for (Servicio servicio : servicios) {
            asociarProductoConServicio(idProducto, servicio.getIdServicio(), moneda);
        }
    }

    private void asociarProductoConCategoria(int idProducto, int idCategoria) throws SQLException, IOException {
        String checkSql = "SELECT 1 FROM productocategoria WHERE idProducto = ? AND idCategoria = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, idProducto);
            checkStmt.setInt(2, idCategoria);

            ResultSet resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                // Si no existe la relación, insertar el nuevo registro
                String insertSql = "INSERT INTO productocategoria (idProducto, idCategoria) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, idProducto);
                    insertStmt.setInt(2, idCategoria);
                    insertStmt.executeUpdate();
                }
            } else {
                // Si ya existe la relación, actualizarla (si es necesario)
                String updateSql = "UPDATE productocategoria SET idCategoria = ? WHERE idProducto = ? AND idCategoria != ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, idCategoria);
                    updateStmt.setInt(2, idProducto);
                    updateStmt.setInt(3, idCategoria);
                    updateStmt.executeUpdate();
                }
            }
        }
    }

    private void asociarProductoConEmpresa(int idProducto, int idEmpresa, double precio, String moneda)
            throws SQLException, IOException {
        String checkSql = "SELECT 1 FROM productoempresa WHERE idProducto = ? AND idEmpresa = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, idProducto);
            checkStmt.setInt(2, idEmpresa);

            ResultSet resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                // Si no existe la relación, insertar el nuevo registro
                String insertSql = "INSERT INTO productoempresa (idProducto, idEmpresa, costomercado, monedamercado) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, idProducto);
                    insertStmt.setInt(2, idEmpresa);
                    insertStmt.setDouble(3, precio);
                    insertStmt.setString(4, moneda);
                    insertStmt.executeUpdate();
                }
            } else {
                // Si ya existe la relación, actualizarla
                String updateSql = "UPDATE productoempresa SET costomercado = ?, monedamercado = ? WHERE idProducto = ? AND idEmpresa = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, precio);
                    updateStmt.setString(2, moneda);
                    updateStmt.setInt(3, idProducto);
                    updateStmt.setInt(4, idEmpresa);
                    updateStmt.executeUpdate();
                }
            }
        }
    }

    private void asociarProductoConProveedor(int idProducto, int idProveedor, double precio, String moneda)
            throws SQLException, IOException {
        String checkSql = "SELECT 1 FROM productoproveedor WHERE idProducto = ? AND idProveedor = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, idProducto);
            checkStmt.setInt(2, idProveedor);

            ResultSet resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                // Si no existe la relación, insertar el nuevo registro
                String insertSql = "INSERT INTO productoproveedor (idProducto, idProveedor, costocompraprov, monedacompraprov) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, idProducto);
                    insertStmt.setInt(2, idProveedor);
                    insertStmt.setDouble(3, precio);
                    insertStmt.setString(4, moneda);
                    insertStmt.executeUpdate();
                }
            } else {
                // Si ya existe la relación, actualizarla
                String updateSql = "UPDATE productoproveedor SET costocompraprov = ?, monedacompraprov = ? WHERE idProducto = ? AND idProveedor = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, precio);
                    updateStmt.setString(2, moneda);
                    updateStmt.setInt(3, idProducto);
                    updateStmt.setInt(4, idProveedor);
                    updateStmt.executeUpdate();
                }
            }
        }
    }

    private void asociarProductoConFabricante(int idProducto, int idFabricante, double precio, String moneda)
            throws SQLException, IOException {
        String checkSql = "SELECT 1 FROM productofabricante WHERE idProducto = ? AND idFabricante = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, idProducto);
            checkStmt.setInt(2, idFabricante);

            ResultSet resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                // Si no existe la relación, insertar el nuevo registro
                String insertSql = "INSERT INTO productofabricante (idProducto, idFabricante, costocomprafab, monedacomprafab) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, idProducto);
                    insertStmt.setInt(2, idFabricante);
                    insertStmt.setDouble(3, precio);
                    insertStmt.setString(4, moneda);
                    insertStmt.executeUpdate();
                }
            } else {
                // Si ya existe la relación, actualizarla
                String updateSql = "UPDATE productofabricante SET costocomprafab = ?, monedacomprafab = ? WHERE idProducto = ? AND idFabricante = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, precio);
                    updateStmt.setString(2, moneda);
                    updateStmt.setInt(3, idProducto);
                    updateStmt.setInt(4, idFabricante);
                    updateStmt.executeUpdate();
                }
            }
        }
    }

    private void asociarProductoConCliente(int idProducto, int idCliente, double precio, String moneda)
            throws SQLException, IOException {
        String checkSql = "SELECT 1 FROM productocliente WHERE idProducto = ? AND idCliente = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, idProducto);
            checkStmt.setInt(2, idCliente);

            ResultSet resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                // Si no existe la relación, insertar el nuevo registro
                String insertSql = "INSERT INTO productocliente (idProducto, idCliente, costoventa, monedaventa) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, idProducto);
                    insertStmt.setInt(2, idCliente);
                    insertStmt.setDouble(3, precio);
                    insertStmt.setString(4, moneda);
                    insertStmt.executeUpdate();
                }
            } else {
                // Si ya existe la relación, actualizarla
                String updateSql = "UPDATE productocliente SET costoventa = ?, monedaventa = ? WHERE idProducto = ? AND idCliente = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDouble(1, precio);
                    updateStmt.setString(2, moneda);
                    updateStmt.setInt(3, idProducto);
                    updateStmt.setInt(4, idCliente);
                    updateStmt.executeUpdate();
                }
            }
        }
    }

    private void asociarProductoConServicio(int idProducto, int idServicio, String moneda)
            throws SQLException, IOException {
        String checkSql = "SELECT 1 FROM productoservicio WHERE idProducto = ? AND idServicio = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, idProducto);
            checkStmt.setInt(2, idServicio);

            ResultSet resultSet = checkStmt.executeQuery();

            if (!resultSet.next()) {
                // Si no existe la relación, insertar el nuevo registro
                String insertSql = "INSERT INTO productoservicio (idProducto, idServicio) VALUES (?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setInt(1, idProducto);
                    insertStmt.setInt(2, idServicio);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    public List<Producto> consultarProductos() throws IOException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.idProducto, p.nombreProducto, p.fichaProducto, p.alternoProducto, p.existenciaProducto, "
                +
                "c.idCategoria, c.nombreCategoria, " +
                "e.idEmpresa, e.nombreEmp, " +
                "pr.idProveedor, pr.nombreProv, " +
                "f.idFabricante, f.nombreFabricante, " +
                "cl.idCliente, cl.nombreCliente, " +
                "s.idServicio, s.descripcionServicio " +
                "FROM producto p " +
                "LEFT JOIN productocategoria pc ON p.idProducto = pc.idProducto " +
                "LEFT JOIN categoria c ON pc.idCategoria = c.idCategoria " +
                "LEFT JOIN productoempresa pe ON p.idProducto = pe.idProducto " +
                "LEFT JOIN empresa e ON pe.idEmpresa = e.idEmpresa " +
                "LEFT JOIN productoproveedor pp ON p.idProducto = pp.idProducto " +
                "LEFT JOIN proveedor pr ON pp.idProveedor = pr.idProveedor " +
                "LEFT JOIN productofabricante pf ON p.idProducto = pf.idProducto " +
                "LEFT JOIN fabricante f ON pf.idFabricante = f.idFabricante " +
                "LEFT JOIN productocliente pc2 ON p.idProducto = pc2.idProducto " +
                "LEFT JOIN cliente cl ON pc2.idCliente = cl.idCliente " +
                "LEFT JOIN productoservicio ps ON p.idProducto = ps.idProducto " +
                "LEFT JOIN servicio s ON ps.idServicio = s.idServicio";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            Map<Integer, Producto> productoMap = new HashMap<>();

            while (rs.next()) {
                int idProducto = rs.getInt("idProducto");

                // Si el producto aún no está en el mapa, crear uno nuevo
                if (!productoMap.containsKey(idProducto)) {
                    Producto producto = new Producto(
                            idProducto,
                            rs.getString("nombreProducto"),
                            rs.getString("fichaProducto"),
                            rs.getString("alternoProducto"),
                            rs.getInt("existenciaProducto"));

                    productoMap.put(idProducto, producto);
                }

                Producto producto = productoMap.get(idProducto);

                // Añadir las categorías
                int idCategoria = rs.getInt("idCategoria");
                if (idCategoria > 0) {
                    producto.getCategorias().add(new Categoria(idCategoria, rs.getString("nombreCategoria")));
                }

                // Añadir las empresas
                int idEmpresa = rs.getInt("idEmpresa");
                if (idEmpresa > 0) {
                    producto.getEmpresas().add(new Empresa(idEmpresa, rs.getString("nombreEmp")));
                }

                // Añadir los proveedores
                int idProveedor = rs.getInt("idProveedor");
                if (idProveedor > 0) {
                    producto.getProveedores().add(new Proveedor(idProveedor, rs.getString("nombreProv")));
                }

                // Añadir los fabricantes
                int idFabricante = rs.getInt("idFabricante");
                if (idFabricante > 0) {
                    producto.getFabricantes().add(new Fabricante(idFabricante, rs.getString("nombreFabricante")));
                }

                // Añadir los clientes
                int idCliente = rs.getInt("idCliente");
                if (idCliente > 0) {
                    producto.getClientes().add(new Cliente(idCliente, rs.getString("nombreCliente")));
                }

                // Añadir los servicios
                int idServicio = rs.getInt("idServicio");
                if (idServicio > 0) {
                    producto.getServicios().add(new Servicio(idServicio, rs.getString("descripcionServicio")));
                }
            }

            productos.addAll(productoMap.values());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productos;
    }

    public List<Categoria> obtenerCategorias() throws IOException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categoria";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categorias.add(new Categoria(
                        rs.getInt("idCategoria"),
                        rs.getString("nombreCategoria")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categorias;
    }

    public List<Empresa> obtenerEmpresas() throws IOException {
        List<Empresa> empresas = new ArrayList<>();
        String sql = "SELECT * FROM empresa";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                empresas.add(new Empresa(
                        rs.getInt("idEmpresa"),
                        rs.getString("nombreEmp")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return empresas;
    }

    public List<Proveedor> obtenerProveedores() throws IOException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM proveedor";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                proveedores.add(new Proveedor(
                        rs.getInt("idProveedor"),
                        rs.getString("nombreProv")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return proveedores;
    }

    public List<Fabricante> obtenerFabricantes() throws IOException {
        List<Fabricante> fabricantes = new ArrayList<>();
        String sql = "SELECT * FROM fabricante";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                fabricantes.add(new Fabricante(
                        rs.getInt("idFabricante"),
                        rs.getString("nombreFabricante")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fabricantes;
    }

    public List<Cliente> obtenerClientes() throws IOException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM cliente";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                clientes.add(new Cliente(
                        rs.getInt("idCliente"),
                        rs.getString("nombreCliente")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return clientes;
    }

    public List<Servicio> obtenerServicios() throws IOException {
        List<Servicio> servicios = new ArrayList<>();
        String sql = "SELECT * FROM servicio";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                servicios.add(new Servicio(
                        rs.getInt("idServicio"),
                        rs.getString("descripcionServicio")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return servicios;
    }

    public List<Producto> filtrarProductos(String filtro) throws IOException {
        List<Producto> productos = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT p.idProducto, p.nombreProducto, p.fichaProducto, p.alternoProducto, p.existenciaProducto, "
                        + "c.idCategoria, c.nombreCategoria, "
                        + "e.idEmpresa, e.nombreEmp, "
                        + "pr.idProveedor, pr.nombreProv, "
                        + "f.idFabricante, f.nombreFabricante, "
                        + "cl.idCliente, cl.nombreCliente, "
                        + "s.idServicio, s.descripcionServicio "
                        + "FROM producto p "
                        + "LEFT JOIN productocategoria pc ON p.idProducto = pc.idProducto "
                        + "LEFT JOIN categoria c ON pc.idCategoria = c.idCategoria "
                        + "LEFT JOIN productoempresa pe ON p.idProducto = pe.idProducto "
                        + "LEFT JOIN empresa e ON pe.idEmpresa = e.idEmpresa "
                        + "LEFT JOIN productoproveedor pp ON p.idProducto = pp.idProducto "
                        + "LEFT JOIN proveedor pr ON pp.idProveedor = pr.idProveedor "
                        + "LEFT JOIN productofabricante pf ON p.idProducto = pf.idProducto "
                        + "LEFT JOIN fabricante f ON pf.idFabricante = f.idFabricante "
                        + "LEFT JOIN productocliente pc2 ON p.idProducto = pc2.idProducto "
                        + "LEFT JOIN cliente cl ON pc2.idCliente = cl.idCliente "
                        + "LEFT JOIN productoservicio ps ON p.idProducto = ps.idProducto "
                        + "LEFT JOIN servicio s ON ps.idServicio = s.idServicio "
                        + "WHERE 1=1 ");

        List<String> filtros = new ArrayList<>();
        if (filtro != null && !filtro.trim().isEmpty()) {
            String[] terms = filtro.split(",");
            for (String term : terms) {
                term = term.trim();
                if (!term.isEmpty()) {
                    filtros.add(term);
                }
            }
        }

        if (!filtros.isEmpty()) {
            sql.append(" AND (");

            List<String> conditions = new ArrayList<>();
            for (int i = 0; i < filtros.size(); i++) {
                conditions.add("(p.nombreProducto LIKE ? OR p.fichaProducto LIKE ? OR p.alternoProducto LIKE ? "
                        + "OR c.nombreCategoria LIKE ? OR pr.nombreProv LIKE ? OR e.nombreEmp LIKE ? "
                        + "OR f.nombreFabricante LIKE ? OR cl.nombreCliente LIKE ? OR s.descripcionServicio LIKE ?)");
            }

            sql.append(String.join(" AND ", conditions));
            sql.append(")");
        }

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (!filtros.isEmpty()) {
                int paramIndex = 1;
                for (String term : filtros) {
                    String filtroLike = "%" + term + "%";
                    for (int i = 0; i < 9; i++) {
                        stmt.setString(paramIndex++, filtroLike);
                    }
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                Map<Integer, Producto> productoMap = new HashMap<>();

                while (rs.next()) {
                    int idProducto = rs.getInt("idProducto");

                    if (!productoMap.containsKey(idProducto)) {
                        Producto producto = new Producto(
                                idProducto,
                                rs.getString("nombreProducto"),
                                rs.getString("fichaProducto"),
                                rs.getString("alternoProducto"),
                                rs.getInt("existenciaProducto"));
                        productoMap.put(idProducto, producto);
                    }

                    Producto producto = productoMap.get(idProducto);
                    int idCategoria = rs.getInt("idCategoria");
                    if (idCategoria > 0) {
                        producto.getCategorias().add(new Categoria(idCategoria, rs.getString("nombreCategoria")));
                    }

                    int idEmpresa = rs.getInt("idEmpresa");
                    if (idEmpresa > 0) {
                        producto.getEmpresas().add(new Empresa(idEmpresa, rs.getString("nombreEmp")));
                    }

                    int idProveedor = rs.getInt("idProveedor");
                    if (idProveedor > 0) {
                        producto.getProveedores().add(new Proveedor(idProveedor, rs.getString("nombreProv")));
                    }

                    int idFabricante = rs.getInt("idFabricante");
                    if (idFabricante > 0) {
                        producto.getFabricantes().add(new Fabricante(idFabricante, rs.getString("nombreFabricante")));
                    }

                    int idCliente = rs.getInt("idCliente");
                    if (idCliente > 0) {
                        producto.getClientes().add(new Cliente(idCliente, rs.getString("nombreCliente")));
                    }

                    int idServicio = rs.getInt("idServicio");
                    if (idServicio > 0) {
                        producto.getServicios().add(new Servicio(idServicio, rs.getString("descripcionServicio")));
                    }
                }

                productos.addAll(productoMap.values());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return productos;
    }

    public void actualizarProducto(Producto producto) throws IOException {
        String sql = "UPDATE producto SET nombreProducto = ?, fichaProducto = ?, alternoProducto = ?, existenciaProducto = ? WHERE idProducto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, producto.getNombreProducto());
            stmt.setString(2, producto.getFichaProducto());
            stmt.setString(3, producto.getAlternoProducto());
            stmt.setInt(4, producto.getExistenciaProducto());
            stmt.setInt(5, producto.getIdProducto());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void eliminarProducto(int idProducto) throws IOException {
        String[] tablasIntermedias = { "ProductoProveedor", "ProductoCategoria", "ProductoFabricante",
                "ProductoEmpresa", "ProductoCliente", "ProductoServicio" };

        try (Connection conn = DatabaseConnection.getConnection()) {
            for (String tabla : tablasIntermedias) {
                String sql = "DELETE FROM " + tabla + " WHERE idProducto = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, idProducto);
                    stmt.executeUpdate();
                }
            }
            String sqlProducto = "DELETE FROM producto WHERE idProducto = ?";
            try (PreparedStatement stmtProducto = conn.prepareStatement(sqlProducto)) {
                stmtProducto.setInt(1, idProducto);
                stmtProducto.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cargarPreciosProducto(Producto producto) throws IOException {
        String sql = "SELECT p.idproveedor, p.nombreProv, pp.costoCompraProv, pp.monedaCompraProv " +
                "FROM ProductoProveedor pp " +
                "JOIN Proveedor p ON pp.idproveedor = p.idProveedor " +
                "WHERE pp.idproducto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, producto.getIdProducto());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Proveedor proveedor = new Proveedor(rs.getInt("idproveedor"), rs.getString("nombreProv"));
                Precio precio = new Precio(rs.getDouble("costoCompraProv"), rs.getString("monedaCompraProv"));
                producto.getPreciosPorProveedor().put(proveedor, precio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cargarFabricantesProducto(Producto producto) throws IOException {
        String sql = "SELECT p.idFabricante, p.nombreFabricante, pp.costoCompraFab, pp.monedaCompraFab " +
                "FROM ProductoFabricante pp " +
                "JOIN Fabricante p ON pp.idFabricante = p.idFabricante " +
                "WHERE pp.idproducto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, producto.getIdProducto());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Fabricante fabricante = new Fabricante(rs.getInt("idFabricante"), rs.getString("nombreFabricante"));
                Precio precio = new Precio(rs.getDouble("costoCompraFab"), rs.getString("monedaCompraFab"));
                producto.getPreciosPorFabricante().put(fabricante, precio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cargarClientesProducto(Producto producto) throws IOException {
        String sql = "SELECT p.idCliente, p.nombreCliente, pp.costoVenta, pp.monedaVenta " +
                "FROM ProductoCliente pp " +
                "JOIN Cliente p ON pp.idCliente = p.idCliente " +
                "WHERE pp.idproducto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, producto.getIdProducto());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cliente cliente = new Cliente(rs.getInt("idCliente"), rs.getString("nombreCliente"));
                Precio precio = new Precio(rs.getDouble("costoVenta"), rs.getString("monedaVenta"));
                producto.getPreciosPorCliente().put(cliente, precio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cargarEmpresasProducto(Producto producto) throws IOException {
        String sql = "SELECT p.idEmpresa, p.nombreEmp, pp.costoMercado, pp.monedaMercado " +
                "FROM ProductoEmpresa pp " +
                "JOIN Empresa p ON pp.idEmpresa = p.idEmpresa " +
                "WHERE pp.idproducto = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, producto.getIdProducto());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Empresa empresa = new Empresa(rs.getInt("idEmpresa"), rs.getString("nombreEmp"));
                Precio precio = new Precio(rs.getDouble("costoMercado"), rs.getString("monedaMercado"));
                producto.getPreciosPorEmpresa().put(empresa, precio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void registrarProductosDesdeExcel(File excelFile) {
        try (FileInputStream fis = new FileInputStream(excelFile);
                Workbook workbook = new XSSFWorkbook(fis)) {
    
            Sheet sheet = workbook.getSheetAt(0);
    
            if (sheet.getPhysicalNumberOfRows() == 0) {
                showAlert("Error", "El archivo Excel está vacío.", Alert.AlertType.ERROR);
                return;
            }
    
            boolean productosRegistrados = false;
            boolean errorMostrado = false; // Bandera para controlar la alerta
    
            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;
    
                try {
                    String nombreProducto = getStringCellValue(row.getCell(0));
                    String ficha = getStringCellValue(row.getCell(1));
                    String alterno = getStringCellValue(row.getCell(2));
                    String existencia = getStringCellValue(row.getCell(3));
                    String moneda = getStringCellValue(row.getCell(4));
    
                    // Listas para almacenar las entidades relacionadas
                    List<String> categorias = new ArrayList<>();
                    List<String> proveedores = new ArrayList<>();
                    List<String> fabricantes = new ArrayList<>();
                    List<String> clientes = new ArrayList<>();
                    List<String> empresas = new ArrayList<>();
                    List<String> servicios = new ArrayList<>();
                    List<String> prestadoresServicio = new ArrayList<>();
                    List<Double> preciosProveedores = new ArrayList<>();
                    List<Double> preciosFabricantes = new ArrayList<>();
                    List<Double> preciosClientes = new ArrayList<>();
                    List<Double> preciosEmpresas = new ArrayList<>();
                    List<Double> costosServicios = new ArrayList<>();
    
                    int columnBase = 5;
                    int increment = 12;
    
                    while (true) {
                        boolean foundData = false;
    
                        for (int offset = 0; offset < increment; offset++) {
                            int currentColumn = columnBase + offset;
                            Cell cell = row.getCell(currentColumn);
    
                            if (cell == null || cell.getCellType() == CellType.BLANK)
                                continue;
    
                            foundData = true;
    
                            if (cell.getCellType() == CellType.STRING) {
                                String valor = cell.getStringCellValue();
                                switch (offset) {
                                    case 0:
                                        categorias.add(valor);
                                        break;
                                    case 1:
                                        proveedores.add(valor);
                                        break;
                                    case 3:
                                        fabricantes.add(valor);
                                        break;
                                    case 5:
                                        clientes.add(valor);
                                        break;
                                    case 7:
                                        empresas.add(valor);
                                        break;
                                    case 9:
                                        servicios.add(valor);
                                        break;
                                    case 10:
                                        prestadoresServicio.add(valor);
                                        break;
                                }
                            } else if (cell.getCellType() == CellType.NUMERIC) {
                                double valorNumerico = cell.getNumericCellValue();
                                switch (offset) {
                                    case 2:
                                        preciosProveedores.add(valorNumerico);
                                        break;
                                    case 4:
                                        preciosFabricantes.add(valorNumerico);
                                        break;
                                    case 6:
                                        preciosClientes.add(valorNumerico);
                                        break;
                                    case 8:
                                        preciosEmpresas.add(valorNumerico);
                                        break;
                                    case 11:
                                        costosServicios.add(valorNumerico);
                                        break;
                                }
                            }
                        }
    
                        // Si no se encontraron datos en este bloque, terminamos
                        if (!foundData)
                            break;
    
                        // Pasamos al siguiente bloque de 12 columnas
                        columnBase += increment;
                    }
    
                    // Registrar las entidades en la base de datos
                    List<Categoria> categoriasObj = new ArrayList<>();
                    for (String categoriaNombre : categorias) {
                        int idCategoria = verificarYRegistrar("categoria", "idCategoria", "nombreCategoria", categoriaNombre);
                        categoriasObj.add(new Categoria(idCategoria, categoriaNombre));
                    }
    
                    List<Proveedor> proveedoresObj = new ArrayList<>();
                    for (String proveedorNombre : proveedores) {
                        int idProveedor = verificarYRegistrar("proveedor", "idProveedor", "nombreProv", proveedorNombre);
                        proveedoresObj.add(new Proveedor(idProveedor, proveedorNombre));
                    }
    
                    List<Fabricante> fabricantesObj = new ArrayList<>();
                    for (String fabricanteNombre : fabricantes) {
                        int idFabricante = verificarYRegistrar("fabricante", "idFabricante", "nombreFabricante", fabricanteNombre);
                        fabricantesObj.add(new Fabricante(idFabricante, fabricanteNombre));
                    }
    
                    List<Cliente> clientesObj = new ArrayList<>();
                    for (String clienteNombre : clientes) {
                        int idCliente = verificarYRegistrar("cliente", "idCliente", "nombreCliente", clienteNombre);
                        clientesObj.add(new Cliente(idCliente, clienteNombre));
                    }
    
                    List<Empresa> empresasObj = new ArrayList<>();
                    for (String empresaNombre : empresas) {
                        int idEmpresa = verificarYRegistrar("empresa", "idEmpresa", "nombreEmp", empresaNombre);
                        empresasObj.add(new Empresa(idEmpresa, empresaNombre));
                    }
    
                    List<Servicio> serviciosObj = new ArrayList<>();
                    List<PrestadorServicio> prestadoresServicioObj = new ArrayList<>();
                    for (int i = 0; i < servicios.size(); i++) {
                        String servicioNombre = servicios.get(i);
                        String prestadorNombre = prestadoresServicio.get(i);
                        double costoServicio = costosServicios.get(i);
    
                        // Registrar prestador y servicio
                        int idPrestador = verificarYRegistrarPrestador(prestadorNombre);
    
                        if (idPrestador != -1) {
                            int idServicio = verificarYRegistrarServicio(idPrestador, servicioNombre, costoServicio, moneda);
                            prestadoresServicioObj.add(new PrestadorServicio(idPrestador, prestadorNombre));
                            serviciosObj.add(new Servicio(idServicio, servicioNombre, costoServicio));
                        }
                    }
    
                    // Crear y registrar el producto
                    Producto producto = new Producto(0, nombreProducto, ficha, alterno, Integer.parseInt(existencia));
                    registrarProducto(producto, categoriasObj, empresasObj, proveedoresObj, fabricantesObj, clientesObj,
                            serviciosObj, preciosEmpresas, preciosProveedores, preciosFabricantes, preciosClientes, moneda);
    
                    productosRegistrados = true; // Al menos un producto fue registrado
    
                } catch (IndexOutOfBoundsException e) {
                    // Mostrar solo una vez el error
                    if (!errorMostrado) {
                        showAlert("Error", "El archivo Excel tiene una estructura incorrecta. Asegúrese de seguir el formato requerido.", Alert.AlertType.ERROR);
                        errorMostrado = true;
                        continue;
                    }
                    e.printStackTrace();
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    showAlert("Error", "Hubo un error al registrar el producto", Alert.AlertType.ERROR);
                }
            }
    
            // Al finalizar, mostrar mensaje dependiendo si se registraron productos
            if (productosRegistrados) {
                showAlert("Éxito", "Los productos fueron registrados exitosamente.", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Error", "No se registraron productos.", Alert.AlertType.ERROR);
            }
    
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "No se pudo leer el archivo Excel.", Alert.AlertType.ERROR);
        }
    }
    

    // Método para mostrar las alertas
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private int verificarYRegistrar(String tabla, String campoId, String campoNombre, String nombre)
            throws SQLException, IOException {
        // Primero, verificamos si el nombre ya existe en la base de datos.
        String consulta = "SELECT " + campoId + " FROM " + tabla + " WHERE " + campoNombre + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(consulta)) {
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // Si ya existe, actualizamos el valor
                String actualizacion = "UPDATE " + tabla + " SET " + campoNombre + " = ? WHERE " + campoId + " = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(actualizacion)) {
                    updateStmt.setString(1, nombre); // Actualizamos el nombre
                    updateStmt.setInt(2, rs.getInt(campoId)); // Usamos el ID encontrado
                    updateStmt.executeUpdate();
                }
                // Retornamos el ID ya existente
                return rs.getInt(campoId);
            }
        }

        // Si no existe, insertamos el nuevo valor
        String insercion = "INSERT INTO " + tabla + " (" + campoNombre + ") VALUES (?) RETURNING " + campoId;
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(insercion)) {
            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(campoId);
            }
        }

        return -1; // Si no se encuentra o inserta el valor
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

    // Método para verificar y registrar el prestador en la tabla prestador_servicio
    private int verificarYRegistrarPrestador(String prestadorNombre) throws SQLException, IOException {
        // Verificar si el prestador ya existe
        String consulta = "SELECT idPrestador FROM prestadorservicio WHERE nombrePrestador = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(consulta)) {
            stmt.setString(1, prestadorNombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("idPrestador"); // Retorna el ID si ya existe
            }
        }

        // Si no existe, insertamos el nuevo prestador
        String insercion = "INSERT INTO prestadorservicio (nombrePrestador) VALUES (?) RETURNING idPrestador";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(insercion)) {
            stmt.setString(1, prestadorNombre);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("idPrestador");
            }
        }

        return -1; // Si no se encuentra o inserta el valor
    }

    // Método para verificar y registrar el servicio en la tabla servicio
    private int verificarYRegistrarServicio(int idPrestador, String descripcionServicio, double costoServicio,
            String moneda) throws SQLException, IOException {
        // Verificar si el servicio ya existe
        String consulta = "SELECT idServicio FROM servicio WHERE descripcionServicio = ? AND idPrestador = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(consulta)) {
            stmt.setString(1, descripcionServicio);
            stmt.setInt(2, idPrestador);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("idServicio"); // Retorna el ID si ya existe
            }
        }

        // Si no existe, insertamos el nuevo servicio
        String insercion = "INSERT INTO servicio (idPrestador, descripcionServicio, costoServicio, monedaServicio) VALUES (?, ?, ?, ?) RETURNING idServicio";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(insercion)) {
            stmt.setInt(1, idPrestador);
            stmt.setString(2, descripcionServicio);
            stmt.setDouble(3, costoServicio);
            stmt.setString(4, moneda);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("idServicio");
            }
        }

        return -1; // Si no se encuentra o inserta el valor
    }

}
