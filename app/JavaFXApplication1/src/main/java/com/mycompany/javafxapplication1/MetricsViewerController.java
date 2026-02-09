package com.mycompany.javafxapplication1;

import com.mycompany.javafxapplication1.services.PerformanceMetrics;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import java.util.Map;

/**
 * MetricsViewerController - Displays performance metrics
 */
public class MetricsViewerController {
    
    @FXML
    private Label uploadsLabel;
    
    @FXML
    private Label downloadsLabel;
    
    @FXML
    private Label deletesLabel;
    
    @FXML
    private Label avgUploadLabel;
    
    @FXML
    private Label avgDownloadLabel;
    
    @FXML
    private Label uptimeLabel;
    
    @FXML
    private Label efficiencyLabel;
    
    @FXML
    private PieChart storageChart;
    
    @FXML
    private TextArea reportArea;
    
    private PerformanceMetrics metrics;
    
    /**
     * Initialize
     */
    public void initialize() {
        metrics = PerformanceMetrics.getInstance();
        loadMetrics();
    }
    
    /**
     * Load and display metrics
     */
    private void loadMetrics() {
        // Update labels
        uploadsLabel.setText(String.valueOf(metrics.getTotalUploads()));
        downloadsLabel.setText(String.valueOf(metrics.getTotalDownloads()));
        deletesLabel.setText(String.valueOf(metrics.getTotalDeletes()));
        
        avgUploadLabel.setText(String.format("%.2f s", metrics.getAverageUploadTime() / 1000.0));
        avgDownloadLabel.setText(String.format("%.2f s", metrics.getAverageDownloadTime() / 1000.0));
        
        long uptime = metrics.getUptimeSeconds();
        uptimeLabel.setText(String.format("%d seconds (%.1f minutes)", uptime, uptime / 60.0));
        
        efficiencyLabel.setText(String.format("%.1f%%", metrics.getLoadBalancerEfficiency()));
        
        // Update pie chart
        storageChart.getData().clear();
        Map<String, Integer> usage = metrics.getStorageUsage();
        
        for (Map.Entry<String, Integer> entry : usage.entrySet()) {
            String storage = entry.getKey();
            int fileCount = entry.getValue();
            
            if (fileCount > 0) {
                PieChart.Data slice = new PieChart.Data(storage + " (" + fileCount + " files)", fileCount);
                storageChart.getData().add(slice);
            }
        }
        
        // If no data, show placeholder
        if (storageChart.getData().isEmpty()) {
            PieChart.Data placeholder = new PieChart.Data("No data yet", 1);
            storageChart.getData().add(placeholder);
        }
        
        // Update detailed report
        reportArea.setText(metrics.getMetricsReport());
    }
    
    /**
     * Refresh metrics
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        loadMetrics();
    }
    
    /**
     * Reset all metrics
     */
    @FXML
    private void handleReset(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Reset Metrics");
        confirm.setHeaderText("Reset all performance metrics?");
        confirm.setContentText("This will clear all recorded statistics.");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            metrics.reset();
            loadMetrics();
        }
    }
}
