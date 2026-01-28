package com.mycompany.javafxapplication1;

import com.mycompany.javafxapplication1.models.FileChunk;
import com.mycompany.javafxapplication1.models.FileInfo;
import com.mycompany.javafxapplication1.services.FileManager;
import com.mycompany.javafxapplication1.services.LoadBalancer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

// Handles the following file operations - upload, download, delete
public class FileManagementController {
    @FXML
    private TextField usernameField;
    
    @FXML
    private TableView<FileInfo> filesTable;
    
    @FXML
    private TableColumn<FileInfo, String> filenameCol;
    
    @FXML
    private TableColumn<FileInfo, String> sizeCol;
    
    @FXML
    private TableColumn<FileInfo, Integer> chunksCol;
    
    @FXML
    private TableColumn<FileInfo, String> dateCol;
    
    @FXML
    private TableColumn<FileInfo, String> storageCol;
    
    @FXML
    private Label statusLabel;
    
    @FXML
    private ProgressBar progressBar;
    
    @FXML
    private Button uploadBtn;
    
    @FXML
    private Button downloadBtn;
    
    @FXML
    private Button deleteBtn;
    
    // Services
    private FileManager fileManager;
    private LoadBalancer loadBalancer;
    private String currentUsername;
    private int nextFileId = 1; // Counter for file IDs
    
    // Initialises when controller loads
    public void initialize() {
        // Creates services
        fileManager = new FileManager();
        loadBalancer = new LoadBalancer();
        
        // Set table columns
        filenameCol.setCellValueFactory(new PropertyValueFactory<>("filename"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        chunksCol.setCellValueFactory(new PropertyValueFactory<>("totalChunks"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));
        storageCol.setCellValueFactory(new PropertyValueFactory<>("storageLocation"));
        
        // Set status
        statusLabel.setText("Ready");
        progressBar.setProgress(0.0);
    }
    
    // Set username (from login screen)
    public void setUsername(String username) {
        this.currentUsername = username;
        usernameField.setText(username);
        loadUserFiles();
    }
    
    // Operation - Upload
    @FXML
    private void handleUpload(ActionEvent event) {
        try {
            // Allows user to choose a file
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Upload");
            Stage stage = (Stage) uploadBtn.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);
            
            if (selectedFile == null) {
                return; // User cancelled
            }
            
            statusLabel.setText("Uploading: " + selectedFile.getName());
            progressBar.setProgress(0.3);
            
            // Chunks the file
            System.out.println("Chunking file: " + selectedFile.getName());
            List<FileChunk> chunks = fileManager.chunkFile(selectedFile, nextFileId);
            progressBar.setProgress(0.5);
            
            // Uses load balancer to pick storage
            String storage = loadBalancer.getNextStorage();
            System.out.println("Selected storage: " + storage);
            
            // Saves chunks
            fileManager.saveChunks(chunks, storage);
            progressBar.setProgress(0.8);
            
            // Adds to table
            String fileSize = formatFileSize(selectedFile.length());
            String uploadDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
            FileInfo fileInfo = new FileInfo(nextFileId, selectedFile.getName(), fileSize, chunks.size(), uploadDate, storage);
            filesTable.getItems().add(fileInfo);
            
            nextFileId++;
            progressBar.setProgress(1.0);
            statusLabel.setText("Upload complete: " + selectedFile.getName());
            
            showAlert("Success", "File uploaded successfully!", Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Upload failed!");
            showAlert("Error", "Failed to upload file: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            // Resets progress bar after 2s
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> progressBar.setProgress(0.0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    // Operation - Download file
    @FXML
    private void handleDownload(ActionEvent event) {
        try {
            // Retrieves selected file from table
            FileInfo selected = filesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("No Selection", "Please select a file to download", Alert.AlertType.WARNING);
                return;
            }
            
            statusLabel.setText("Downloading: " + selected.getFilename());
            progressBar.setProgress(0.3);
            
            // Loads chunks from storage
            System.out.println("Loading chunks from: " + selected.getStorageLocation());
            List<FileChunk> chunks = fileManager.loadChunks(selected.getId(), selected.getTotalChunks(), selected.getStorageLocation());
            progressBar.setProgress(0.6);
            
            // Allows user choose where to save
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File As");
            fileChooser.setInitialFileName(selected.getFilename());
            Stage stage = (Stage) downloadBtn.getScene().getWindow();
            File saveFile = fileChooser.showSaveDialog(stage);
            
            if (saveFile == null) {
                statusLabel.setText("Download cancelled");
                progressBar.setProgress(0.0);
                return; // User cancelled
            }
            
            // Reconstructs file
            File reconstructed = fileManager.reconstructFile(chunks, saveFile.getAbsolutePath());
            progressBar.setProgress(1.0);
            statusLabel.setText("Download complete: " + selected.getFilename());
            
            showAlert("Success", "File downloaded to: " + saveFile.getAbsolutePath(), Alert.AlertType.INFORMATION);
            
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Download failed!");
            showAlert("Error", "Failed to download file: " + e.getMessage(), Alert.AlertType.ERROR);
        } finally {
            // Resets progress bar
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(() -> progressBar.setProgress(0.0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
    
    // Operation - Delete File
    @FXML
    private void handleDelete(ActionEvent event) {
        FileInfo selected = filesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a file to delete", Alert.AlertType.WARNING);
            return;
        }
        
        // Confirms deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + selected.getFilename() + "?");
        confirm.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Removes from table
            filesTable.getItems().remove(selected);
            statusLabel.setText("Deleted: " + selected.getFilename());
            
            // TODO: Delete actual chunk files from storage folder
            // TODO: Delete from database
        }
    }
    
    // Operation - Refresh file list
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadUserFiles();
        statusLabel.setText("List refreshed");
    }
    
    // Operation - Logout
    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("primary.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 640, 480);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Load user files
    private void loadUserFiles() {
        // Keeps files in tables - need to switch to SQL later 
        statusLabel.setText("Loaded files for: " + currentUsername);
    }
    
    // Format file size: bytes -> KB/MB
    private String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
    
    // Alert
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
