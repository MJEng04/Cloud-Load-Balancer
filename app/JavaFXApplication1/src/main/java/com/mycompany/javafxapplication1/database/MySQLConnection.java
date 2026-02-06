package com.mycompany.javafxapplication1.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


// MySQL database connection - connects to n1264601cwk_db database
public class MySQLConnection {
    // Connection details -> Database configuration
    private static final String DB_HOST = System.getenv("MYSQL_HOST") != null ? 
        System.getenv("MYSQL_HOST") : "localhost";
    private static final String DB_PORT = System.getenv("MYSQL_PORT") != null ? 
        System.getenv("MYSQL_PORT") : "3307";
    private static final String DB_NAME = System.getenv("MYSQL_DATABASE") != null ? 
        System.getenv("MYSQL_DATABASE") : "n1264601_dkrdb";
    private static final String DB_USER = System.getenv("MYSQL_USER") != null ? 
        System.getenv("MYSQL_USER") : "n1264601";
    private static final String DB_PASSWORD = System.getenv("MYSQL_PASSWORD") != null ? 
        System.getenv("MYSQL_PASSWORD") : "student123";
    
    // Builds the connection URL
    private static final String DB_URL = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;
    
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
            System.out.println("   Database: n1264601_dkrdb");
            System.out.println("   Status: Connected");
            conn.close();
        } catch (Exception e) {
            System.err.println("MySQL connection test failed!");
            System.err.println("   Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Method to test connection
    public static void main(String[] args) {
        System.out.println("Testing MySQL connection\n");
        testConnection();
    }
}
