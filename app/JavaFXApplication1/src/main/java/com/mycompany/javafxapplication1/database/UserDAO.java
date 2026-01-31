package com.mycompany.javafxapplication1.database;

import java.sql.*;
import java.security.MessageDigest;
import java.util.Base64;


// Handles user authentication with MySQL - class for login and register
public class UserDAO {
    
    // Register user in MySQL
    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'standard')";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, password); // Needs hashing at some point
            
            stmt.executeUpdate();
            System.out.println("User registered in MySQL: " + username);
            return true;
            
        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("Username already exists: " + username);
            } else {
                System.err.println("Error registering user: " + e.getMessage());
                e.printStackTrace();
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    
    // Validate user login
    public boolean validateUser(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                if (storedPassword.equals(password)) {
                    System.out.println("Login successful: " + username);
                    return true;
                } else {
                    System.out.println("Invalid password for: " + username);
                    return false;
                }
            } else {
                System.out.println("User not found: " + username);
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Error validating user: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Checks if user exists
    public boolean userExists(String username) {
        String sql = "SELECT username FROM users WHERE username = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next();
            
        } catch (Exception e) {
            System.err.println("Error checking user: " + e.getMessage());
            return false;
        }
    }
    

    // Retrieves user role (admin or user)
    public String getUserRole(String username) {
        String sql = "SELECT role FROM users WHERE username = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("role");
            }
            
        } catch (Exception e) {
            System.err.println("Error getting role: " + e.getMessage());
        }
        
        return "user";
    }
}
