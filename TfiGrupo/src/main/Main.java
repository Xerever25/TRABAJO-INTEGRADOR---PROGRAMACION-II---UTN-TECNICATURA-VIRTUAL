package main;

public class Main {
    public static void main(String[] args) {
        // Cargar el driver de MySQL (buena práctica, aunque JDBC 4+ es automático)
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el driver de MySQL (mysql-connector-j).");
            return;
        }

        System.out.println("Iniciando aplicación...");
        AppMenu menu = new AppMenu();
        menu.iniciar();
    }
}