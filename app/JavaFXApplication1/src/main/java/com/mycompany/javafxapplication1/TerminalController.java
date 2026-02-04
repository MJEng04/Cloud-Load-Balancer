package com.mycompany.javafxapplication1;

import com.mycompany.javafxapplication1.database.FileDAO;
import com.mycompany.javafxapplication1.database.UserDAO;
import com.mycompany.javafxapplication1.models.FileInfo;
import com.mycompany.javafxapplication1.models.FileChunk;
import com.mycompany.javafxapplication1.services.FileManager;
import com.mycompany.javafxapplication1.services.LoadBalancer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

// Command-line interface for file operations
public class TerminalController {
    
    @FXML
    private TextArea terminalOutput;
    
    @FXML
    private TextField commandInput;
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private Button executeBtn;
    private String currentUsername;
    private String currentDirectory = "/";
    private FileDAO fileDAO;
    private UserDAO userDAO;
    private FileManager fileManager;
    private LoadBalancer loadBalancer;
    
    @FXML
    private void quickCommand(ActionEvent event) {
        Button btn = (Button) event.getSource();
        commandInput.setText(btn.getText());
        handleExecute(null);
    }

    
    
    // Initialises terminal
    public void initialize() {
        fileDAO = new FileDAO();
        userDAO = new UserDAO();
        fileManager = new FileManager();
        loadBalancer = new LoadBalancer();
        
        // Sets terminal styles
        terminalOutput.setEditable(false);
        terminalOutput.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px; -fx-background-color: #1e1e1e; -fx-text-fill: #00ff00;");
        commandInput.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        
        // Handles "Enter key" command input
        commandInput.setOnAction(event -> handleExecute(null));
        
        printWelcome();
    }
    

    // Sets username taken from login
    public void setUsername(String username) {
        this.currentUsername = username;
        usernameField.setText(username);
        appendOutput("Logged in: " + username + "\n");
        appendOutput("Type 'help' for commands\n\n");
    }
    

    // Handles command execution
    @FXML
    private void handleExecute(ActionEvent event) {
        String command = commandInput.getText().trim();
        
        if (command.isEmpty()) {
            return;
        }
        
        // Shows command in terminal
        appendOutput(currentUsername + "@cloud:" + currentDirectory + "$ " + command + "\n");
        
        // Executes command
        executeCommand(command);
        
        // Clears input
        commandInput.clear();
        
        // Bonus - Scroll to bottom
        terminalOutput.setScrollTop(Double.MAX_VALUE);
    }
    

    // Executes command
    private void executeCommand(String commandLine) {
        String[] parts = commandLine.trim().split("\\s+");
        String command = parts[0].toLowerCase();
        
        try {
            switch (command) {
                case "help":
                    showHelp();
                    break;
                    
                case "ls":
                case "list":
                    listFiles();
                    break;
                    
                case "download":
                    if (parts.length < 2) {
                        appendOutput("Error: Usage: download <filename>\n\n");
                    } else {
                        downloadFile(parts[1]);
                    }
                    break;
                    
                case "upload":
                    uploadFile();
                    break;
                    
                case "share":
                    if (parts.length < 4) {
                        appendOutput("Error: Usage: share <filename> <username> <read|write|both>\n\n");
                    } else {
                        shareFile(parts[1], parts[2], parts[3]);
                    }
                    break;
                    
                case "rm":
                case "delete":
                    if (parts.length < 2) {
                        appendOutput("Error: Usage: rm <filename>\n\n");
                    } else {
                        deleteFile(parts[1]);
                    }
                    break;
                    
                case "shared":
                    listSharedFiles();
                    break;
                    
                case "pwd":
                    appendOutput(currentDirectory + "\n\n");
                    break;
                    
                case "clear":
                    clearTerminal();
                    break;
                   
                case "exit":
                case "logout":
                    handleLogout();
                    break;
                    
                default:
                    appendOutput("Error: Unknown command '" + command + "'. Type 'help' for commands.\n\n");
            }
        } catch (Exception e) {
            appendOutput("Error: " + e.getMessage() + "\n\n");
            e.printStackTrace();
        }
    }
    

    // Displays help section
    private void showHelp() {
        appendOutput(" ------------ Cloud File Management System - User Terminal Help ------------ \n");
        appendOutput(" ---------------------- Commands ----------------------\n");
        appendOutput("  ls, list  - List files\n");
        appendOutput("  download <file>  - Download file\n");
        appendOutput("  upload - Upload file\n");
        appendOutput("  share <file> <user> <permission> - Share file (read | write | readwrite)\n");
        appendOutput("  rm <file> - Delete file\n");
        appendOutput("  shared - List files shared with you\n");
        appendOutput("  pwd - Show current directory\n");
        appendOutput("  clear - Clear terminal screen\n");
        appendOutput("  help - Show help\n");
        appendOutput("  exit, logout - Return to login screen\n\n");
    }
    
    // Command - List File
    private void listFiles() {
        List<FileInfo> files = fileDAO.getUserFiles(currentUsername);
        
        if (files.isEmpty()) {
            appendOutput("No files found.\n\n");
            return;
        }
        
        appendOutput("Your Files (" + files.size() + " total):\n");
        appendOutput("--------------------------------------------\n");
        
        for (FileInfo file : files) {
            appendOutput(String.format(" %-30s %10s  %2d chunks  %s\n",
                file.getFilename(),
                file.getSize(),
                file.getTotalChunks(),
                file.getUploadDate()
            ));
        }
        
        appendOutput("\n");
    }
    
    // Command - List shared files
    private void listSharedFiles() {
        List<FileInfo> files = fileDAO.getSharedFiles(currentUsername);
        
        if (files.isEmpty()) {
            appendOutput("No shared files found.\n\n");
            return;
        }
        
        appendOutput("Files Shared With You (" + files.size() + " total):\n");
        appendOutput("--------------------------------------------\n");
        
        for (FileInfo file : files) {
            appendOutput(String.format("  %-30s %10s  %2d chunks  %s\n",
                file.getFilename(),
                file.getSize(),
                file.getTotalChunks(),
                file.getUploadDate()
            ));
        }
        
        appendOutput("\n");
    }
    

    // Downloads file
    private void downloadFile(String filename) {
        try {
            // Searches for file by name
            FileInfo fileToDownload = null;
            List<FileInfo> files = fileDAO.getUserFiles(currentUsername);
            
            for (FileInfo file : files) {
                if (file.getFilename().equals(filename)) {
                    fileToDownload = file;
                    break;
                }
            }
            
            if (fileToDownload == null) {
                appendOutput("Error: File '" + filename + "' not found.\n\n");
                return;
            }
            
            // Check permission
            if (!fileDAO.canAccessFile(fileToDownload.getId(), currentUsername)) {
                appendOutput("Error: Access denied for '" + filename + "'\n\n");
                return;
            }
            
            // Let user choose save location
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File As");
            fileChooser.setInitialFileName(filename);
            Stage stage = (Stage) terminalOutput.getScene().getWindow();
            File saveFile = fileChooser.showSaveDialog(stage);
            
            if (saveFile == null) {
                appendOutput("Download cancelled.\n\n");
                return;
            }
            
            appendOutput("Downloading " + filename + "\n");
            
            // Retrieves chunks
            List<FileDAO.ChunkInfo> chunkInfos = fileDAO.getFileChunks(fileToDownload.getId());
            String storage = chunkInfos.get(0).storageLocation;
            
            // Loads and reconstructs
            List<FileChunk> chunks = fileManager.loadChunks(fileToDownload.getId(), fileToDownload.getTotalChunks(), storage);
            File reconstructed = fileManager.reconstructFile(chunks, saveFile.getAbsolutePath());
            
            appendOutput("File successfully downloaded '" + filename + "' to " + saveFile.getAbsolutePath() + "\n\n");
            
        } catch (Exception e) {
            appendOutput("Error downloading file: " + e.getMessage() + "\n\n");
            e.printStackTrace();
        }
    }
    

    // Uploads file
    private void uploadFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select File to Upload");
            Stage stage = (Stage) terminalOutput.getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);
            
            if (selectedFile == null) {
                appendOutput("Upload cancelled.\n\n");
                return;
            }
            
            appendOutput("Uploading " + selectedFile.getName() + "\n");
            
            // Saves to database
            int fileId = fileDAO.saveFile(selectedFile.getName(), selectedFile.length(), 0, currentUsername);
            
            if (fileId == -1) {
                throw new Exception("Failed to save file to database");
            }
            
            // Chunks file
            List<FileChunk> chunks = fileManager.chunkFile(selectedFile, fileId);
            fileDAO.updateTotalChunks(fileId, chunks.size());
            
            // Retrieves storage location
            String storage = loadBalancer.getNextStorage();
            
            // Saves chunks
            fileManager.saveChunks(chunks, storage);
            
            // Saves chunk info
            for (FileChunk chunk : chunks) {
                fileDAO.saveChunk(fileId, chunk.getChunkNumber(), storage, chunk.getChecksum(), chunk.getSize());
            }
            
            appendOutput("Successfully uploaded '" + selectedFile.getName() + "'\n\n");
            
        } catch (Exception e) {
            appendOutput("Error uploading file: " + e.getMessage() + "\n\n");
            e.printStackTrace();
        }
    }
    
    // Shares file
    private void shareFile(String filename, String targetUser, String permissions) {
        try {
            // Searches for file
            FileInfo fileToShare = null;
            List<FileInfo> files = fileDAO.getUserFiles(currentUsername);
            
            for (FileInfo file : files) {
                if (file.getFilename().equals(filename)) {
                    fileToShare = file;
                    break;
                }
            }
            
            if (fileToShare == null) {
                appendOutput("Error: File '" + filename + "' not found.\n\n");
                return;
            }
            
            // Checks if user exists
            if (!userDAO.userExists(targetUser)) {
                appendOutput("Error: User '" + targetUser + "' does not exist.\n\n");
                return;
            }
            
            // Retrieves Permission Data
            boolean canRead = permissions.equalsIgnoreCase("read") || permissions.equalsIgnoreCase("both");
            boolean canWrite = permissions.equalsIgnoreCase("write") || permissions.equalsIgnoreCase("both");
            
            if (!canRead && !canWrite) {
                appendOutput("Error: Invalid permissions \n\n");
                return;
            }
            
            // Shares file
            if (fileDAO.shareFile(fileToShare.getId(), targetUser, canRead, canWrite)) {
                String perms = canRead && canWrite ? "read+write" : (canRead ? "read-only" : "write-only");
                appendOutput("Shared '" + filename + "' with " + targetUser + " (" + perms + ")\n\n");
            } else {
                appendOutput("Error: Failed to share file.\n\n");
            }
            
        } catch (Exception e) {
            appendOutput("Error sharing file: " + e.getMessage() + "\n\n");
            e.printStackTrace();
        }
    }
    

    // Deletes file
    private void deleteFile(String filename) {
        try {
            // Searches for file
            FileInfo fileToDelete = null;
            List<FileInfo> files = fileDAO.getUserFiles(currentUsername);
            
            for (FileInfo file : files) {
                if (file.getFilename().equals(filename)) {
                    fileToDelete = file;
                    break;
                }
            }
            
            if (fileToDelete == null) {
                appendOutput("Error: File '" + filename + "' not found.\n\n");
                return;
            }
            
            // Checks permission
            if (!fileDAO.canModifyFile(fileToDelete.getId(), currentUsername)) {
                appendOutput("Error: Permission denied. Can't delete file.\n\n");
                return;
            }
            
            // Deletes file
            fileDAO.deleteFile(fileToDelete.getId());
            appendOutput("Deleted '" + filename + "'\n\n");
            
        } catch (Exception e) {
            appendOutput("Error deleting file: " + e.getMessage() + "\n\n");
            e.printStackTrace();
        }
    }
    

    // Clears terminal
    private void clearTerminal() {
        terminalOutput.clear();
        printWelcome();
        appendOutput("Logged in: " + currentUsername + "\n");
        appendOutput("Type 'help' for commands\n\n");
    }
    

    // Handles logout
    private void handleLogout() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader();
            loader.setLocation(getClass().getResource("primary.fxml"));
            javafx.scene.Parent root = loader.load();
            
            Stage stage = (Stage) terminalOutput.getScene().getWindow();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 640, 480);
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    // Prints welcome message
    private void printWelcome() {
        appendOutput("------------ Cloud File Management System - User Terminal Interface ---------\n");
    }
    

    // Minor function - append text to terminal output
    private void appendOutput(String text) {
        terminalOutput.appendText(text);
    }
    

    // Formats file size
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
