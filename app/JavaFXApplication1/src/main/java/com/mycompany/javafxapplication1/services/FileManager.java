package com.mycompany.javafxapplication1.services;

import com.mycompany.javafxapplication1.models.FileChunk;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

// Handles basic file operations e.g. Chunking, saving, loading, etc
public class FileManager {
    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB chunks
    private String baseStoragePath = "storage"; // Base folder for storage
    
    // Splits files into chunks then returns list of objects
    public List<FileChunk> chunkFile(File file, int fileId) throws IOException {
        List<FileChunk> chunks = new ArrayList<>();
        
        // Reads file
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[CHUNK_SIZE];
        int chunkNumber = 0;
        int bytesRead;
        
        // Reads file in chunks
        while ((bytesRead = fis.read(buffer)) > 0) {
            // Creates array of exact size
            byte[] chunkData = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunkData, 0, bytesRead);
            
            // Creates chunk object
            FileChunk chunk = new FileChunk(fileId, chunkNumber, chunkData);
            chunks.add(chunk);
            
            System.out.println("Created chunk " + chunkNumber + " - Size: " + bytesRead + " bytes");
            chunkNumber++;
        }
        
        fis.close();
        System.out.println("File split into " + chunks.size() + " chunks");
        return chunks;
    }
    
    // Saves chunk to disk
    public void saveChunks(List<FileChunk> chunks, String storageLocation) throws IOException {
        // Creates storage folder if it doesn't exist
        File storageDir = new File(baseStoragePath + File.separator + storageLocation);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        
        // Saves each chunk - to where?
        for (FileChunk chunk : chunks) {
            String filename = "file_" + chunk.getFileId() + "_chunk_" + chunk.getChunkNumber() + ".dat";
            File chunkFile = new File(storageDir, filename);
            
            FileOutputStream fos = new FileOutputStream(chunkFile);
            fos.write(chunk.getData());
            fos.close();
            
            chunk.setStorageLocation(storageLocation);
            System.out.println("Saved: " + filename + " to " + storageLocation);
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
            FileInputStream fis = new FileInputStream(chunkFile);
            byte[] data = new byte[(int) chunkFile.length()];
            fis.read(data);
            fis.close();
            
            // Creates chunk object
            FileChunk chunk = new FileChunk(fileId, i, data);
            chunk.setStorageLocation(storageLocation);
            chunks.add(chunk);
            
            System.out.println("Loaded: " + filename);
        }
        
        return chunks;
    }
    
    //Reconstruct file from chunks
    public File reconstructFile(List<FileChunk> chunks, String outputFilename) throws IOException {
        File outputFile = new File(outputFilename);
        FileOutputStream fos = new FileOutputStream(outputFile);
        
        // Sorts chunks by number
        chunks.sort((c1, c2) -> Integer.compare(c1.getChunkNumber(), c2.getChunkNumber()));
        
        // Writes all chunks to output file
        for (FileChunk chunk : chunks) {
            // Verifies integrity of chunk
            if (!chunk.verifyChecksum()) {
                throw new IOException("Chunk " + chunk.getChunkNumber() + " is corrupted!");
            }
            
            fos.write(chunk.getData());
            System.out.println("Written chunk " + chunk.getChunkNumber());
        }
        
        fos.close();
        System.out.println("File reconstructed: " + outputFilename);
        return outputFile;
    }
    
    // Simple test
    public static void main(String[] args) {
        try {
            FileManager fm = new FileManager();
            
            // Test -> Create test file
            File testFile = new File("test.txt");
            FileWriter writer = new FileWriter(testFile);
            writer.write("This is a test file for chunking\n");
            for (int i = 0; i < 10000; i++) {
                writer.write("Line " + i + ": Testing file chunking system...\n");
            }
            writer.close();
            
            System.out.println("\n==== TEST: Chunking File ====");
            List<FileChunk> chunks = fm.chunkFile(testFile, 1);
            
            System.out.println("\n==== TEST: Saving Chunks ====");
            fm.saveChunks(chunks, "storage1");
            
            System.out.println("\n==== TEST: Loading Chunks ====");
            List<FileChunk> loadedChunks = fm.loadChunks(1, chunks.size(), "storage1");
            
            System.out.println("\n==== TEST: Reconstructing File ====");
            File reconstructed = fm.reconstructFile(loadedChunks, "test_reconstructed.txt");
            
            System.out.println("\n=== SUCCESS! ===");
            System.out.println("Original size: " + testFile.length());
            System.out.println("Reconstructed size: " + reconstructed.length());
        
        // Catch block - prints error message
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
