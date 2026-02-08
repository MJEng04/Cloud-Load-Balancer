package com.mycompany.javafxapplication1;


import com.mycompany.javafxapplication1.services.SystemLogger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;


// Displays system logs
public class LogViewerController {
    
    @FXML
    private TextArea logTextArea;
    
    @FXML
    private ComboBox<String> filterCombo;
    
    @FXML
    private Label statsLabel;
    
   
    public void initialize() {
        // Sets default filter
        filterCombo.setValue("Last 100 Lines");
        
        // Loads logs
        loadLogs();
        
        // Adds filter
        filterCombo.setOnAction(event -> loadLogs());
    }
    
    
    // Loads and displays logs based on filter chosen
    private void loadLogs() {
        String filter = filterCombo.getValue();
        String logs = "";
        
        switch (filter) {
            case "All Logs":
                logs = SystemLogger.getAllLogs();
                break;
            case "Last 50 Lines":
                logs = SystemLogger.getRecentLogs(50);
                break;
            case "Last 100 Lines":
                logs = SystemLogger.getRecentLogs(100);
                break;
            case "User Actions Only":
                logs = filterLogs("USER:");
                break;
            case "System Events Only":
                logs = filterLogs("SYSTEM");
                break;
            case "Security Events Only":
                logs = filterLogs("SECURITY");
                break;
            case "Errors Only":
                logs = filterLogs("ERROR");
                break;
        }
        
        logTextArea.setText(logs);
        
        // Updates statistics
        int lineCount = logs.isEmpty() ? 0 : logs.split("\n").length;
        statsLabel.setText("Total logs: " + lineCount);
        
        // Scrolls to bottom
        logTextArea.setScrollTop(Double.MAX_VALUE);
    }
    

    // Filters logs by keyword
    private String filterLogs(String keyword) {
        String allLogs = SystemLogger.getAllLogs();
        StringBuilder filtered = new StringBuilder();
        
        for (String line : allLogs.split("\n")) {
            if (line.contains(keyword)) {
                filtered.append(line).append("\n");
            }
        }
        
        return filtered.toString();
    }
    

    // Refreshes logs
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadLogs();
    }
    

    // Clears logs
    @FXML
    private void handleClear(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Logs");
        alert.setHeaderText("Clear all logs?");
        alert.setContentText("Action can't be undone.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            SystemLogger.clearLogs();
            loadLogs();
        }
    }
}
