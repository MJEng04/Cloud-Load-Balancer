package com.mycompany.javafxapplication1.database;

import com.mycompany.javafxapplication1.models.FileChunk;
import com.mycompany.javafxapplication1.models.FileInfo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Data Access Object for File operations - saves and loads files from MySQL
public class FileDAO {
    
    // Save file to database after uploading
    public int saveFile(String filename, long fileSize, int totalChunks, String username) {
        String sql = "INSERT INTO files (filename, original_size, total_chunks, owner_username) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, filename);
            stmt.setLong(2, fileSize);
            stmt.setInt(3, totalChunks);
            stmt.setString(4, username);
            
            stmt.executeUpdate();
            
            // Retrieves file ID
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int fileId = rs.getInt(1);
                System.out.println("File saved to database: " + filename + " (ID: " + fileId + ")");
                return fileId;
            }
            
        } catch (Exception e) {
            System.err.println("Error saving file to database: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1; // Signals Error
    }
    
    // Saves chunk info to database
    public void saveChunk(int fileId, int chunkNumber, String storageLocation, String checksum, int chunkSize) {
        String sql = "INSERT INTO chunks (file_id, chunk_number, storage_location, checksum, chunk_size) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            stmt.setInt(2, chunkNumber);
            stmt.setString(3, storageLocation);
            stmt.setString(4, checksum);
            stmt.setInt(5, chunkSize);
            
            stmt.executeUpdate();
            System.out.println("Chunk saved: file_" + fileId + "_chunk_" + chunkNumber);
            
        } catch (Exception e) {
            System.err.println("Error saving chunk: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Update total chunks for file
    public void updateTotalChunks(int fileId, int totalChunks) {
        String sql = "UPDATE files SET total_chunks = ? WHERE id = ?";

        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, totalChunks);
            stmt.setInt(2, fileId);
            stmt.executeUpdate();

            System.out.println("Updated total chunks for file is" + fileId + ": " + totalChunks);

        } catch (Exception e) {
            System.err.println("Error updating chunks: " + e.getMessage());
        }
    }
    
    // Retrieves files for user
    public List<FileInfo> getUserFiles(String username) {
        List<FileInfo> files = new ArrayList<>();
        String sql = "SELECT id, filename, original_size, total_chunks, upload_date FROM files WHERE owner_username = ? ORDER BY upload_date DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String filename = rs.getString("filename");
                long size = rs.getLong("original_size");
                int chunks = rs.getInt("total_chunks");
                Timestamp uploadDate = rs.getTimestamp("upload_date");
                
                // Retrieves storage location from first chunk
                String storage = getFileStorageLocation(id);
                
                // Formats file size
                String sizeStr = formatFileSize(size);
                
                // Formats date
                String dateStr = uploadDate.toString().substring(0, 16); 
                
                FileInfo fileInfo = new FileInfo(id, filename, sizeStr, chunks, dateStr, storage);
                files.add(fileInfo);
            }
            
            System.out.println("Loaded " + files.size() + " files for " + username);
            
        } catch (Exception e) {
            System.err.println("Error loading files: " + e.getMessage());
            e.printStackTrace();
        }
        
        return files;
    }
    
    // Retrieves files shared with user
    public List<FileInfo> getSharedFiles(String username) {
        List<FileInfo> files = new ArrayList<>();
        String sql = "SELECT f.id, f.filename, f.original_size, f.total_chunks, f.upload_date, " +
                     "fp.can_read, fp.can_write, f.owner_username " +
                     "FROM files f " +
                     "JOIN file_permissions fp ON f.id = fp.file_id " +
                     "WHERE fp.username = ? " +
                     "ORDER BY f.upload_date DESC";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String filename = rs.getString("filename");
                long size = rs.getLong("original_size");
                int chunks = rs.getInt("total_chunks");
                Timestamp uploadDate = rs.getTimestamp("upload_date");
                String owner = rs.getString("owner_username");
                
                String storage = getFileStorageLocation(id);
                String sizeStr = formatFileSize(size);
                String dateStr = uploadDate.toString().substring(0, 16);
                
                // Adds "Shared by: owner" information to filename
                FileInfo fileInfo = new FileInfo(id, filename + " (by: " + owner + ")", sizeStr, chunks, dateStr, storage);
                files.add(fileInfo);
            }
            
            System.out.println("Loaded " + files.size() + " shared files for user: " + username);
            
        } catch (Exception e) {
            System.err.println("Error loading shared files: " + e.getMessage());
            e.printStackTrace();
        }
        
        return files;
    }
    
    
    // Allows users to share file with other users
    public boolean shareFile(int fileId, String username, boolean canRead, boolean canWrite) {
        String sql = "INSERT INTO file_permissions (file_id, username, can_read, can_write) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE can_read = ?, can_write = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            stmt.setString(2, username);
            stmt.setBoolean(3, canRead);
            stmt.setBoolean(4, canWrite);
            stmt.setBoolean(5, canRead);
            stmt.setBoolean(6, canWrite);
            
            stmt.executeUpdate();
            System.out.println("File " + fileId + " shared with " + username + " (read: " + canRead + ", write: " + canWrite + ")");
            return true;
            
        } catch (Exception e) {
            System.err.println("Error sharing file: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
   
    // Checks if user has permission to access file
    public boolean canAccessFile(int fileId, String username) {
        // Check if user owns the file
        String ownerSql = "SELECT owner_username FROM files WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(ownerSql)) {
            
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getString("owner_username").equals(username)) {
                return true; // Meaning owner has full access
            }
            
        } catch (Exception e) {
            System.err.println("Error checking ownership of file: " + e.getMessage());
        }
        
        // Checks if user has shared access of file
        String permSql = "SELECT can_read FROM file_permissions WHERE file_id = ? AND username = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(permSql)) {
            
            stmt.setInt(1, fileId);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("can_read");
            }
            
        } catch (Exception e) {
            System.err.println("Error checking permissions: " + e.getMessage());
        }
        
        return false; // No access to file
    }
    

    // Checks if user can modify a file
    public boolean canModifyFile(int fileId, String username) {
        // Check if user owns the file
        String ownerSql = "SELECT owner_username FROM files WHERE id = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(ownerSql)) {
            
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getString("owner_username").equals(username)) {
                return true; // Meaning owner can modify file 
            }
            
        } catch (Exception e) {
            System.err.println("Error checking ownership: " + e.getMessage());
        }
        
        // Checks if user has write permission
        String permSql = "SELECT can_write FROM file_permissions WHERE file_id = ? AND username = ?";
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(permSql)) {
            
            stmt.setInt(1, fileId);
            stmt.setString(2, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getBoolean("can_write");
            }
            
        } catch (Exception e) {
            System.err.println("Error checking write permission: " + e.getMessage());
        }
        
        return false; // Meaning no write access
    }
    
    // Retrieves storage location for file
    private String getFileStorageLocation(int fileId) {
        String sql = "SELECT storage_location FROM chunks WHERE file_id = ? LIMIT 1";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("storage_location");
            }
            
        } catch (Exception e) {
            System.err.println("Error retrieving storage location: " + e.getMessage());
        }
        
        return "unknown";
    }
    
    // Retrieves chunk information for file
    public List<ChunkInfo> getFileChunks(int fileId) {
        List<ChunkInfo> chunks = new ArrayList<>();
        String sql = "SELECT chunk_number, storage_location, checksum FROM chunks WHERE file_id = ? ORDER BY chunk_number";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int chunkNum = rs.getInt("chunk_number");
                String storage = rs.getString("storage_location");
                String checksum = rs.getString("checksum");
                
                chunks.add(new ChunkInfo(chunkNum, storage, checksum));
            }
            
        } catch (Exception e) {
            System.err.println("Error loading chunks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return chunks;
    }
    
    // Deletes file and chunk from database
    public void deleteFile(int fileId) {
        String sql = "DELETE FROM files WHERE id = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, fileId);
            stmt.executeUpdate();
            
            System.out.println("File(s) deleted from database (ID: " + fileId + ")");
            
        } catch (Exception e) {
            System.err.println("Error deleting file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Formats file size
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    // Holds chunk info
    public static class ChunkInfo {
        public int chunkNumber;
        public String storageLocation;
        public String checksum;
        
        public ChunkInfo(int chunkNumber, String storageLocation, String checksum) {
            this.chunkNumber = chunkNumber;
            this.storageLocation = storageLocation;
            this.checksum = checksum;
        }
    }
   
}
