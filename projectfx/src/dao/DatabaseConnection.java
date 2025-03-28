package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // MySQL connection parameters (Updated to use a consistent format)
    private static final String URL = "jdbc:mysql://localhost:3306/elearningplatform";
    private static final String USER = "root"; // Replace with your MySQL username
    private static final String PASSWORD = "ronish"; // Replace with your MySQL password

    // Method to establish connection with the database
    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create a connection to the database
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection established successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Database connection failed!");
            e.printStackTrace();
        }
        return connection;
    }
}
