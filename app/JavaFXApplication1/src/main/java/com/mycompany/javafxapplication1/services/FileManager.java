package com.mycompany.javafxapplication1.services;

import com.mycompany.javafxapplication1.models.FileChunk;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

// Handles basic file operations e.g. Chunking, saving, loading w/ encryption, etc
public class FileManager {
    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB chunks
    private String baseStoragePath = "storage"; // Base folder for storage
    private EncryptionService encryptionService;
    
     // Initialises encryption
    public FileManager() {
        this.encryptionService = new EncryptionService();
    }
    
    // Splits files into chunks then returns list of objects
    public List<FileChunk> chunkFile(File file, int fileId) throws IOException {
        List<FileChunk> chunks = new ArrayList<>();
        
        // Reads file
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int chunkNumber = 0;
            int bytesRead;
            
            // Reads file in chunks
            while ((bytesRead = fis.read(buffer)) != -1) { // Returns -1 when theres nothing left to read
                // Creates array of exact size
                byte[] chunkData = new byte[bytesRead];
                System.arraycopy(buffer, 0, chunkData, 0, bytesRead);
                
                // Calculates checksum before encryption
                String checksum = calculateChecksum(chunkData);
                
                // Encrypts the chunk data
                byte[] encryptedData = null;
                try {
                    encryptedData = encryptionService.encrypt(chunkData);
                    System.out.println("Encrypted chunk " + chunkNumber + " - Original: " + chunkData.length + " bytes, Encrypted: " + encryptedData.length + " bytes");
                } catch (Exception e) {
                    System.err.println("Encryption failed for chunk " + chunkNumber + ": " + e.getMessage());
                    throw new IOException("Encryption failed", e);
                }

                // Creates chunk object
                FileChunk chunk = new FileChunk(fileId, chunkNumber, encryptedData, checksum);
                chunks.add(chunk);
                System.out.println("Created chunk " + chunkNumber + " - Size: " + bytesRead + " bytes");
                chunkNumber++;
            }
          
        }

        System.out.println("File split into " + chunks.size() + " chunks");
        return chunks;
    }
    
    // Saves chunk to disk -> now to storage folder
    public void saveChunks(List<FileChunk> chunks, String storageLocation) throws IOException {
        // Creates storage folder if it doesn't exist
        File storageDir = new File(baseStoragePath + File.separator + storageLocation);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        // Saves each chunk - to storage location
        for (FileChunk chunk : chunks) {
            String filename = "file_" + chunk.getFileId() + "_chunk_" + chunk.getChunkNumber() + ".dat";
            File chunkFile = new File(storageDir, filename);

            try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
                fos.write(chunk.getData());  
            }
            

            chunk.setStorageLocation(storageLocation);
            System.out.println("Saved: " + filename + " to " + storageLocation + " (" + chunk.getData().length + " bytes)");
        }
    }
    
    //Loads chunk from disk
    public List<FileChunk> loadChunks(int fileId, int totalChunks, String storageLocation) throws IOException {
        List<FileChunk> chunks = new ArrayList<>();
        File storageDir = new File(baseStoragePath + File.separator + storageLocation);
       
        // Loads each chunk
        for (int i = 0; i < totalChunks; i++) {
            String filename = "file_" + fileId + "_chunk_" + i + ".dat";
            File chunkFile = new File(storageDir, filename);
            
            if (!chunkFile.exists()) {
                throw new IOException("Chunk file not found: " + filename);
            }
            
            // Reads chunk data
            byte[] encryptedData;
            try (FileInputStream fis = new FileInputStream(chunkFile)) {
                encryptedData = fis.readAllBytes();
            }
            
            // Decrypts the chunk
            byte[] decryptedData = null;
            try {
                decryptedData = encryptionService.decrypt(encryptedData);
                System.out.println("Decrypted chunk " + i + " - Encrypted: " + encryptedData.length + " bytes, Decrypted: " + decryptedData.length + " bytes"); // Rewrite
            } catch (Exception e) {
                System.err.println("Decryption failed for chunk " + i + ": " + e.getMessage());
                throw new IOException("Decryption failed", e);
            }

            // Calculate checksum of decrypted data
            String checksum = calculateChecksum(decryptedData);
            
            // Creates chunk object
            FileChunk chunk = new FileChunk(fileId, i, decryptedData, checksum);
            chunks.add(chunk);
            
            System.out.println("Loaded: " + filename);
        }
        
        System.out.println("Loaded + decrypted " + chunks.size() + " chunks located from: " + storageLocation);
        return chunks;
    }
    
    //Reconstruct file from chunks
    public File reconstructFile(List<FileChunk> chunks, String outputFilename) throws IOException {
        File outputFile = new File(outputFilename);
        
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            // Writes all chunks to output file
            for (FileChunk chunk : chunks) {
                // Verifies checksum
                String calculatedChecksum = calculateChecksum(chunk.getData());
                if (!calculatedChecksum.equals(chunk.getChecksum())) {
                    System.err.println("Warning: Checksum mismatch for chunk " + chunk.getChunkNumber());
                }
                
                fos.write(chunk.getData());
                System.out.println("Written chunk " + chunk.getChunkNumber());
            }

        }

        System.out.println("File reconstructed: " + outputFilename + " (" + outputFile.length() + " bytes)");
        return outputFile;
    }
    
    // Calculate checksum
    private String calculateChecksum(byte[] data) {
        CRC32 crc = new CRC32();
        crc.update(data);
        return Long.toHexString(crc.getValue());
    }
     
}