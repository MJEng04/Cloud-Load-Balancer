package com.mycompany.javafxapplication1.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


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

    // Retrieves all users - for admin panel
    public List<UserInfo> getAllUsers() {
        List<UserInfo> users = new ArrayList<>();
        String sql = "SELECT username, role, created_at FROM users ORDER BY created_at DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String username = rs.getString("username");
                String role = rs.getString("role");
                Timestamp created = rs.getTimestamp("created_at");
                
                String createdStr = created.toString().substring(0, 16);
                users.add(new UserInfo(username, role, createdStr));
            }
            
            System.out.println("Loaded " + users.size() + " users");
            
        } catch (Exception e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
        
        return users;
    }
    

    // Admin Only - Delete User
    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE username = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("User deleted: " + username);
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    
    // Admin Only - Promote user to admin
    public boolean promoteToAdmin(String username) {
        String sql = "UPDATE users SET role = 'admin' WHERE username = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("User promoted to admin: " + username);
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Error promoting user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    

    // Admin Only - Demoting admin to user
    public boolean demoteToStandard(String username) {
        String sql = "UPDATE users SET role = 'standard' WHERE username = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("User demoted to standard: " + username);
                return true;
            }
            
        } catch (Exception e) {
            System.err.println("Error demoting user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    

    // Retrieves list of all usernames
    public List<String> getAllUsernames() {
        List<String> usernames = new ArrayList<>();
        String sql = "SELECT username FROM users ORDER BY username";
        
        try (Connection conn = MySQLConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usernames.add(rs.getString("username"));
            }
            
        } catch (Exception e) {
            System.err.println("Error loading usernames: " + e.getMessage());
        }
        
        return usernames;
    }
    
    
    // Holds user info
    public static class UserInfo {
        public String username;
        public String role;
        public String createdAt;
        
        public UserInfo(String username, String role, String createdAt) {
            this.username = username;
            this.role = role;
            this.createdAt = createdAt;
        }
        
        @Override
        public String toString() {
            return username + " (" + role + ")";
        }
    }
}
