package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String DB_URL = "jdbc:sqlite:C:/Users/Alexis/Documents/MODULS POBLENOU/0485 - PROGRAMACION/SCRUM/AppTaxiMaven/src/main/java/data/apptaxi.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public static void main(String[] args) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()){
            System.out.println("Connectad to SQlite database!");
        }catch (SQLException e){
            System.out.println("Connected failed: " + e.getMessage());
        }
    }
}
