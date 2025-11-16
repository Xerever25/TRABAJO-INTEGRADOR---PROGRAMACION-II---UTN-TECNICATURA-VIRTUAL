/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DatabaseConnection {

    private static Connection connection = null;

    private DatabaseConnection() {
        // Evita que se creen instancias de esta clase
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Cargar propiedades
                Properties props = new Properties();
                InputStream input = DatabaseConnection.class.getClassLoader().getResourceAsStream("db.properties");
                if (input == null) {
                    throw new IOException("No se encontr� el archivo db.properties");
                }
                props.load(input);

                // Leer par�metros
                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String password = props.getProperty("db.password");

                // Crear conexi�n
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("? Conexi�n establecida correctamente con la base de datos.");

            } catch (IOException | SQLException e) {
                System.err.println("? Error al conectar con la base de datos: " + e.getMessage());
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("? Conexi�n cerrada correctamente.");
            } catch (SQLException e) {
                System.err.println("? Error al cerrar la conexi�n: " + e.getMessage());
            }
        }
    }
}

