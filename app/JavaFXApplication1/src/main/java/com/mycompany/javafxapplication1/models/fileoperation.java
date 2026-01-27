/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javafxapplication1.models;

/**
 *
 * @author marti
 */
public class fileoperation {
    private int id;
    private String username;
    private String filename;
    private String operation; // either "upload" or "download"
    private String status;    // either "waiting", "running", "completed"
    private long timestamp;
    
    // constructor
    public fileoperation (int id, String username, String filename, String operation){
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
    
    // toString for debugging
    @Override
    public String toString() {
        return "fileoperation{" +
                "id=" + id +
                ", user='" + username + '\'' +
                ", file='" + filename + '\'' +
                ", operation='" + operation + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
    
}
