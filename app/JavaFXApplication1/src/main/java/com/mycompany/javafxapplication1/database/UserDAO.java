package com.mycompany.javafxapplication1.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.util.Arrays;


// Handles user authentication with MySQL - class for login and register
public class UserDAO {
    
    // Parameters for password hashing -> controls strength of hashing
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 30;
    
    // Register user in MySQL
    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, 'standard')";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            
            stmt.executeUpdate();
            System.out.println("User registered: " + username);
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
                String storedHash = rs.getString("password");
                
                if (verifyPassword(password, storedHash)) {
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
    
    // Hashing Password
    private String hashPassword(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        // Generates random salt -> ensures unique and secure key generation
        byte[] salt = generateSalt();
        
        // Hashes password
        byte[] hash = pbkdf2(password.toCharArray(), salt);
        
        // Return salt and hash -> both encoded
        return Base64.getEncoder().encodeToString(salt) + "$" + Base64.getEncoder().encodeToString(hash);
    }
    

    // Verifies password with stored hash value
    private boolean verifyPassword(String password, String storedHash) {
        try {
            // Splits stored hash into salt + hash
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] hash = Base64.getDecoder().decode(parts[1]);
            
            // Hashes provided password with salt
            byte[] testHash = pbkdf2(password.toCharArray(), salt);
            
            // Compares hashes
            return Arrays.equals(hash, testHash);
            
        } catch (Exception e) {
            System.err.println("Error verifying password: " + e.getMessage());
            return false;
        }
    }
    

    // Generates random salt value -> used for added security
    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
    

    // Derives PBKDF2 key
    private byte[] pbkdf2(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);
        
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
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
