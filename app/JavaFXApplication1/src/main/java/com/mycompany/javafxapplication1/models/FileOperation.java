package com.mycompany.javafxapplication1.models;

// Represents file operations (upload/download)
public class FileOperation {
    private int id;
    private String username;
    private String filename;
    private String operation; // either "upload" or "download"
    private String status;    // either "waiting", "running", "completed"
    private long timestamp;
    
    // Constructor
    public FileOperation (int id, String username, String filename, String operation){
        this.id = id;
        this.username = username;
        this.filename = filename;
        this.operation = operation;
        this.status = "waiting";
        this.timestamp = System.currentTimeMillis();    
    }
    
    // Retrievers and Setters
        public int getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getFilename() {
        return filename;
    }
    
    public String getOperation() {
        return operation;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    // Makes object output readable 
    @Override
    public String toString() {
        return "FileOperation{" +
                "id=" + id +
                ", user='" + username + '\'' +
                ", file='" + filename + '\'' +
                ", operation='" + operation + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
    
}
