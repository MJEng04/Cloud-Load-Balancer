package com.mycompany.javafxapplication1;

import com.mycompany.javafxapplication1.models.FileChunk;
import com.mycompany.javafxapplication1.models.FileInfo;
import com.mycompany.javafxapplication1.services.FileManager;
import com.mycompany.javafxapplication1.services.LoadBalancer;
import com.mycompany.javafxapplication1.database.FileDAO;

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
    private FileDAO fileDAO;  
    private String currentUsername;
   
    
    // Initialises when controller loads
    public void initialize() {
        // Creates services
        fileManager = new FileManager();
        loadBalancer = new LoadBalancer();
        fileDAO = new FileDAO();  // 
        
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
                return;
            }
            
            statusLabel.setText("Uploading: " + selectedFile.getName());
            progressBar.setProgress(0.2);
            
            // Saves to database to get file ID
            System.out.println("Saving file info to database");
            int fileId = fileDAO.saveFile(
                selectedFile.getName(), 
                selectedFile.length(), 
                0,
                currentUsername
            );
            
            if (fileId == -1) {
                throw new Exception("Failed to save file to database");
            }
            
            progressBar.setProgress(0.4);
            
            // Chunks file using database ID
            System.out.println("Chunking file: " + selectedFile.getName());
            List<FileChunk> chunks = fileManager.chunkFile(selectedFile, fileId);
            
            // Update database with total chunks
            fileDAO.updateTotalChunks(fileId, chunks.size());
            
            progressBar.setProgress(0.6);
            
            // Uses load balancer to pick storage
            String storage = loadBalancer.getNextStorage();
            System.out.println("Selected storage: " + storage);
            
            // Saves chunks to disk
            fileManager.saveChunks(chunks, storage);
            progressBar.setProgress(0.8);
            
            // Save chunk info to database
            for (FileChunk chunk : chunks) {
                fileDAO.saveChunk(
                    fileId,
                    chunk.getChunkNumber(),
                    storage,
                    chunk.getChecksum(),
                    chunk.getSize()
                );
            }
            
            progressBar.setProgress(0.9);
            
            // Reloads file list from database
            loadUserFiles();
            
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
                    javafx.application.Platform.runLater(() -> {
                        progressBar.setProgress(0.0);
                        statusLabel.setText("Ready");
                    });
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
            
            // Retrieves chunk info from database
            List<FileDAO.ChunkInfo> chunkInfos = fileDAO.getFileChunks(selected.getId());
            
            if (chunkInfos.isEmpty()) {
                throw new Exception("No chunks found in database for this file");
            }
            
            // Find which storage has the chunks
            String storage = chunkInfos.get(0).storageLocation;
            
            progressBar.setProgress(0.5);
            
            // Loads chunks from storage
            System.out.println("Loading chunks from: " + storage);
            List<FileChunk> chunks = fileManager.loadChunks(selected.getId(), selected.getTotalChunks(), storage);
            progressBar.setProgress(0.7);
            
            // Allows user choose where to save
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File As");
            fileChooser.setInitialFileName(selected.getFilename());
            Stage stage = (Stage) downloadBtn.getScene().getWindow();
            File saveFile = fileChooser.showSaveDialog(stage);
            
            if (saveFile == null) {
                statusLabel.setText("Download cancelled");
                progressBar.setProgress(0.0);
                return;
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
                    javafx.application.Platform.runLater(() -> {
                        progressBar.setProgress(0.0);
                        statusLabel.setText("Ready");
                    });
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
            try {
                // Delete from database
                fileDAO.deleteFile(selected.getId());
                
                // Reloads file list from database
                loadUserFiles();
                
                statusLabel.setText("Deleted: " + selected.getFilename());
                showAlert("Success", "File deleted successfully!", Alert.AlertType.INFORMATION);
                
                
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Delete failed!");
                showAlert("Error", "Failed to delete file: " + e.getMessage(), Alert.AlertType.ERROR);
            }
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
    
    // Load user files - from MySQL database
    private void loadUserFiles() {
        try {
            System.out.println("Loading files for user: " + currentUsername);
            
            // Retrieves files from database
            List<FileInfo> files = fileDAO.getUserFiles(currentUsername);
            
            // Updates table with database data
            ObservableList<FileInfo> fileList = FXCollections.observableArrayList(files);
            filesTable.setItems(fileList);
            
            statusLabel.setText("Loaded " + files.size() + " file(s) for: " + currentUsername);
            System.out.println("Successfully loaded " + files.size() + " files from database");
            
        } catch (Exception e) {
            System.err.println("Error loading files: " + e.getMessage());
            e.printStackTrace();
            statusLabel.setText("Error loading files");
        }
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
