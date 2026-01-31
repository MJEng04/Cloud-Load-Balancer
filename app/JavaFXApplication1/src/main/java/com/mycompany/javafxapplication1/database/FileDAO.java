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
}
