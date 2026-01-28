package com.mycompany.javafxapplication1.models;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

// Represents file in system - for showing files in TableView
public class FileInfo {
    private SimpleIntegerProperty id;
    private SimpleStringProperty filename;
    private SimpleStringProperty size;
    private SimpleIntegerProperty totalChunks;
    private SimpleStringProperty uploadDate;
    private SimpleStringProperty storageLocation;
    
    // Constructor
    public FileInfo(int id, String filename, String size, int totalChunks, String uploadDate, String storageLocation) {
        this.id = new SimpleIntegerProperty(id);
        this.filename = new SimpleStringProperty(filename);
        this.size = new SimpleStringProperty(size);
        this.totalChunks = new SimpleIntegerProperty(totalChunks);
        this.uploadDate = new SimpleStringProperty(uploadDate);
        this.storageLocation = new SimpleStringProperty(storageLocation);
    }
    
    // Retrievers
    public int getId() {
        return id.get();
    }
    
    public String getFilename() {
        return filename.get();
    }
    
    public String getSize() {
        return size.get();
    }
    
    public int getTotalChunks() {
        return totalChunks.get();
    }
    
    public String getUploadDate() {
        return uploadDate.get();
    }
    
    public String getStorageLocation() {
        return storageLocation.get();
    }
    
    // Retrieves property
    public SimpleStringProperty filenameProperty() {
        return filename;
    }
    
    public SimpleStringProperty sizeProperty() {
        return size;
    }
    
    public SimpleIntegerProperty totalChunksProperty() {
        return totalChunks;
    }
    
    public SimpleStringProperty uploadDateProperty() {
        return uploadDate;
    }
    
    public SimpleStringProperty storageLocationProperty() {
        return storageLocation;
    }
}
