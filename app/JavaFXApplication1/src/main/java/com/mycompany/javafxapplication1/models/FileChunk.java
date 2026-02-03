package com.mycompany.javafxapplication1.models;

// Represents single piece of larger file
public class FileChunk {
    private int fileId;           // Identifies which file chunk belong to
    private int chunkNumber;      // Identifies what piece it is (0,1,2,...)
    private byte[] data;          // Actual data (1MB max)
    private String storageLocation; // storage1 or storage2
    private String checksum;      // To verify that the data isn't corrupted
    
    // Constructor
    public FileChunk(int fileId, int chunkNumber, byte[] data) {
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.data = data;
        this.checksum = calculateChecksum();
    }
    
    // Constructor w/ checksum - for encryption
    public FileChunk(int fileId, int chunkNumber, byte[] data, String checksum) {
        this.fileId = fileId;
        this.chunkNumber = chunkNumber;
        this.data = data;
        this.checksum = checksum;
    }
    
    
    // Calculate checksum
    private String calculateChecksum() {
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(data);
        return String.valueOf(crc.getValue());
    }
    
    // Verify if data is good
    public boolean verifyChecksum() {
        return checksum.equals(calculateChecksum());
    }
    
   // Retrievers and Setters
    public int getFileId() {
        return fileId;
    }
    
    public int getChunkNumber() {
        return chunkNumber;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public String getStorageLocation() {
        return storageLocation;
    }
    
    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }
    
    public String getChecksum() {
        return checksum;
    }
    
    public int getSize() {
        return data.length;
    }
    
    
    // Makes object output readable 
    @Override
    public String toString() {
        return "Chunk{" +
                "file=" + fileId +
                ", num=" + chunkNumber +
                ", size=" + data.length +
                ", storage='" + storageLocation + '\'' +
                '}';
    }
    
}
