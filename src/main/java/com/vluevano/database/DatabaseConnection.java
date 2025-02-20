package com.vluevano.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DatabaseConnection {
    private static Connection connection = null;

    // Método para obtener la conexión
    public static Connection getConnection() throws SQLException, IOException {
        if (connection == null || connection.isClosed()) {
            // Cargar los valores del archivo config.properties
            Properties config = new Properties();
            try (FileInputStream fis = new FileInputStream("config.properties")) {
                config.load(fis);
            }

            // Obtener los valores del archivo
            String host = config.getProperty("db.host");
            String port = config.getProperty("db.port");
            String dbName = config.getProperty("db.name");
            String user = config.getProperty("db.user");
            String password = config.getProperty("db.password");

            // Crear la URL de conexión
            String url = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);

            // Establecer la conexión
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }
}
