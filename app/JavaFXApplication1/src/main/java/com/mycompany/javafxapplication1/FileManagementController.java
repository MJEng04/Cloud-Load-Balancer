package com.mycompany.javafxapplication1;

import com.mycompany.javafxapplication1.models.FileChunk;
import com.mycompany.javafxapplication1.models.FileInfo;
import com.mycompany.javafxapplication1.services.FileManager;
import com.mycompany.javafxapplication1.services.LoadBalancer;
import com.mycompany.javafxapplication1.database.FileDAO;
import com.mycompany.javafxapplication1.database.UserDAO;
import com.mycompany.javafxapplication1.services.SystemLogger;
import com.mycompany.javafxapplication1.services.DelaySim;
import com.mycompany.javafxapplication1.services.PerformanceMetrics;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.concurrent.Task;

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
    
    @FXML
    private Button shareBtn; 
    
    @FXML
    private Button viewSharedBtn;
    
    @FXML
    private Button terminalBtn;
    
    @FXML
    private Button viewLogsBtn;
    
    @FXML
    private CheckBox delayCheckbox;
    
    @FXML
    private Button metricsBtn;
    
    // Services
    private FileManager fileManager;
    private LoadBalancer loadBalancer;
    private FileDAO fileDAO;  
    private UserDAO userDAO;
    private PerformanceMetrics metrics;
    private String currentUsername;
    private boolean viewingOwnFiles = true;
   
    
    // Initialises when controller loads
    public void initialize() {
        // Creates services
        fileManager = new FileManager();
        loadBalancer = new LoadBalancer();
        fileDAO = new FileDAO();
        userDAO = new UserDAO();
        metrics = PerformanceMetrics.getInstance();
        
        // Set table columns
        filenameCol.setCellValueFactory(new PropertyValueFactory<>("filename"));
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        chunksCol.setCellValueFactory(new PropertyValueFactory<>("totalChunks"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("uploadDate"));
        storageCol.setCellValueFactory(new PropertyValueFactory<>("storageLocation"));
        
        // Set status
        statusLabel.setText("Ready");
        progressBar.setProgress(0.0);
        
        // Toggle delay
        if (delayCheckbox != null) {
            delayCheckbox.setOnAction(e -> {
                if (delayCheckbox.isSelected()) {
                    DelaySim.enable();
                } else {
                    DelaySim.disable();
                }
            });
        }
    }
    
    // Set username -> from login screen
    public void setUsername(String username) {
        this.currentUsername = username;
        usernameField.setText(username);
        loadUserFiles(); 
    }
    
    // Operation - Upload
    @FXML
    private void handleUpload(ActionEvent event) {
        
        // Start timer
        long startTime = System.currentTimeMillis();
        
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
            DelaySim.delay("upload");
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
            SystemLogger.logFileUpload(currentUsername, selectedFile.getName(), 
                selectedFile.length(), chunks.size());
            
            // Records upload metrics
            long uploadTime = System.currentTimeMillis() - startTime;
            metrics.recordUpload(storage, selectedFile.length(), uploadTime);
            System.out.println("Recorded: " + storage + ", " + uploadTime + "ms");
            
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
        long startTime = System.currentTimeMillis();  // Starts timing
        
        try {
            // Retrieves selected file from table
            FileInfo selected = filesTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("None Selected", "Please select a file to download", Alert.AlertType.WARNING);
                return;
            }
            
            statusLabel.setText("Downloading: " + selected.getFilename());
            DelaySim.delay("download");
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
            SystemLogger.logFileDownload(currentUsername, selected.getFilename());
            
            // Record download metrics
            long downloadTime = System.currentTimeMillis() - startTime;
            metrics.recordDownload(downloadTime);
            System.out.println("Recorded: " + downloadTime + "ms");
            
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
    
    // Operation - Delete File (w/ permission checks)
    @FXML
    private void handleDelete(ActionEvent event) {
        FileInfo selected = filesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("None Selected", "Please select a file to delete", Alert.AlertType.WARNING);
            return;
        }
        
        if (!fileDAO.canModifyFile(selected.getId(), currentUsername)) {
            showAlert("Denied", "Missing required permission to delete file.", Alert.AlertType.ERROR);
            return;
        }
        
        // Confirms deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete " + selected.getFilename() + "?");
        confirm.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        
        DelaySim.delay("delete");
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                
                // Retrieves storage info prior to deletion
                List<FileDAO.ChunkInfo> chunkInfos = fileDAO.getFileChunks(selected.getId());
                String storage = chunkInfos.isEmpty() ? "unknown" : chunkInfos.get(0).storageLocation;
                
                // Delete from database
                fileDAO.deleteFile(selected.getId());
                
                
                // Reloads file list from database
                loadUserFiles();
                
                statusLabel.setText("Deleted: " + selected.getFilename());
                SystemLogger.logFileDelete(currentUsername, selected.getFilename());
                
                 // Records delete metrics
                metrics.recordDelete(storage, 0);
                System.out.println("Recorded: " + storage);
                
                showAlert("Success", "File deleted successfully", Alert.AlertType.INFORMATION);
               
                
                
            } catch (Exception e) {
                e.printStackTrace();
                statusLabel.setText("Delete failed");
                showAlert("Error", "Failed to delete file: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    // Operation - Share Files
    @FXML
    private void handleShare(ActionEvent event) {
        FileInfo selected = filesTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("None Selected", "Please select a file to share", Alert.AlertType.WARNING);
            return;
        }
        
        // Tells Users only owners can share
        if (!viewingOwnFiles) {
            showAlert("Can't Share", "Can only share files you own.", Alert.AlertType.WARNING);
            return;
        }
        
        showShareDialog(selected);
    }
    
    // Switches between My Files and Shared Files
    @FXML
    private void handleViewShared(ActionEvent event) {
        viewingOwnFiles = !viewingOwnFiles;
        
        if (viewingOwnFiles) {
            loadUserFiles();
            viewSharedBtn.setText("View Shared Files");
            statusLabel.setText("Viewing your files");
        } else {
            loadSharedFiles();
            viewSharedBtn.setText("View My Files");
            statusLabel.setText("Viewing shared files");
        }
    }
    
    // Shows dialog
    private void showShareDialog(FileInfo file) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Share File");
        dialog.setHeaderText("Share: " + file.getFilename());
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        List<String> usernames = userDAO.getAllUsernames();
        usernames.remove(currentUsername);
        
        if (usernames.isEmpty()) {
            showAlert("No Users", "No other users to share with.", Alert.AlertType.INFORMATION);
            return;
        }
        
        ComboBox<String> userCombo = new ComboBox<>();
        userCombo.getItems().addAll(usernames);
        userCombo.setPromptText("Select user...");
        
        CheckBox readCheck = new CheckBox("Can Read (download)");
        readCheck.setSelected(true);
        
        CheckBox writeCheck = new CheckBox("Can Write (delete)");
        
        grid.add(new Label("Share with:"), 0, 0);
        grid.add(userCombo, 1, 0);
        grid.add(readCheck, 1, 1);
        grid.add(writeCheck, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        Optional<ButtonType> result = dialog.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String selectedUser = userCombo.getValue();
            
            if (selectedUser == null) {
                showAlert("No User", "Please select a user.", Alert.AlertType.WARNING);
                return;
            }
            
            boolean canRead = readCheck.isSelected();
            boolean canWrite = writeCheck.isSelected();
            
            if (!canRead && !canWrite) {
                showAlert("No Permissions", "Select at least one permission.", Alert.AlertType.WARNING);
                return;
            }
            
            if (fileDAO.shareFile(file.getId(), selectedUser, canRead, canWrite)) {
                String perms = canRead && canWrite ? "read+write" : (canRead ? "read-only" : "write-only");
                showAlert("Success", "Shared with " + selectedUser + "\nPermissions: " + perms, Alert.AlertType.INFORMATION);
                SystemLogger.logFileShare(currentUsername, file.getFilename(), selectedUser, perms);
            } else {
                showAlert("Error", "Failed to share file.", Alert.AlertType.ERROR);
            }
        }
    }
    
    // Loads shared files
    private void loadSharedFiles() {
        try {
            System.out.println("Loading shared files for: " + currentUsername);
            
            List<FileInfo> files = fileDAO.getSharedFiles(currentUsername);
            ObservableList<FileInfo> fileList = FXCollections.observableArrayList(files);
            filesTable.setItems(fileList);
            
            System.out.println("Loaded " + files.size() + " shared files");
            
        } catch (Exception e) {
            System.err.println("Error loading shared files: " + e.getMessage());
            e.printStackTrace();
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
    
    // Opens Terminal
    @FXML
    private void handleTerminal(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("terminal.fxml"));
            Parent root = loader.load();

            // Sets username in terminal
            TerminalController controller = loader.getController();
            controller.setUsername(currentUsername);

            // Shows terminal window
            Stage terminalStage = new Stage();
            Scene scene = new Scene(root, 850, 650);
            terminalStage.setScene(scene);
            terminalStage.setTitle("Terminal - " + currentUsername);
            terminalStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Opens View Logs
    @FXML
    private void handleViewLogs(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("logviewer.fxml"));
            Parent root = loader.load();

            Stage logStage = new Stage();
            Scene scene = new Scene(root, 850, 650);
            logStage.setScene(scene);
            logStage.setTitle("System Logs");
            logStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleViewMetrics(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("metricsviewer.fxml"));
            Parent root = loader.load();

            Stage metricsStage = new Stage();
            Scene scene = new Scene(root, 700, 600);
            metricsStage.setScene(scene);
            metricsStage.setTitle("Performance Metrics");
            metricsStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open metrics viewer: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
        }
    }
}
