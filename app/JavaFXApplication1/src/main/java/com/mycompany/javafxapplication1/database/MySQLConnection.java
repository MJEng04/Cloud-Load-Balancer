package com.mycompany.javafxapplication1.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


// MySQL database connection - connects to n1264601cwk_db database
public class MySQLConnection {
    // Connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/n1264601cwk_db";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    
    // Connection timeout
    private static final int TIMEOUT = 30;
    
    // Connection to database
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        // Loads MySQL driver
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // Creates and returns connection
        Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        System.out.println("Connected to MySQL database!");
        return conn;
    }
    
    // Tests connection - check is MySQL is working
    public static void testConnection() {
        try {
            Connection conn = getConnection();
            System.out.println("MySQL connection test successful!");
            System.out.println("   Database: n1264601cwk_db");
            System.out.println("   Status: Connected");
            conn.close();
        } catch (Exception e) {
            System.err.println("MySQL connection test failed!");
            System.err.println("   Error: " + e.getMessage());
        }
    }
    
    // Method to test connection
    public static void main(String[] args) {
        System.out.println("Testing MySQL connection...\n");
        testConnection();
    }
}
