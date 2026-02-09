package com.mycompany.javafxapplication1.services;

import java.io.File;
import java.util.*;


// Health Checks - Monitors health of storage containers -> ensures load balancer only sends traffic to healthy locations
public class HealthCheck {
    
    private Map<String, StorageHealth> storageHealth;
    private long lastCheckTime;
    private static final long CHECK_INTERVAL = 30000; //  Basically 30 seconds
    

    // Storage Health Status
    public static class StorageHealth {
        public String storageName;
        public boolean isHealthy;
        public String lastError;
        public long lastCheckTime;
        public long totalSpace;
        public long usableSpace;
        public int fileCount;
        
        public StorageHealth(String storageName) {
            this.storageName = storageName;
            this.isHealthy = false;
            this.lastError = "Not checked yet";
            this.lastCheckTime = 0;
        }
    }
    

    // Constructor
    public HealthCheck() {
        storageHealth = new HashMap<>();
        lastCheckTime = 0;
    }
    

    // Registers storage location to check health
    public void registerStorage(String storageName) {
        if (!storageHealth.containsKey(storageName)) {
            storageHealth.put(storageName, new StorageHealth(storageName));
            System.out.println("Registered storage for health checks: " + storageName);
        }
    }
    

    // Checks health of all registered storage nodes
    public void checkAllStorage() {
        System.out.println("--- Commencing health checks ---");
        
        for (String storageName : storageHealth.keySet()) {
            checkStorage(storageName);
        }
        
        lastCheckTime = System.currentTimeMillis();
        System.out.println("--- Health check complete ---\n");
    }
    

    // Checks health of specific storage location
    private void checkStorage(String storageName) {
        StorageHealth health = storageHealth.get(storageName);
        
        try {
            // Checks if storage directory exists
            File storageDir = new File("storage" + File.separator + storageName);
            
            if (!storageDir.exists()) {
                health.isHealthy = false;
                health.lastError = "Storage directory does not exist";
                System.out.println(storageName + ": Directory not found");
                return;
            }
            
            // Checks if directory accessible
            if (!storageDir.canRead() || !storageDir.canWrite()) {
                health.isHealthy = false;
                health.lastError = "Storage directory not accessible";
                System.out.println(storageName + ": No read/write permission");
                return;
            }
            
            // Checks available space
            health.totalSpace = storageDir.getTotalSpace();
            health.usableSpace = storageDir.getUsableSpace();
            
            if (health.usableSpace < 10 * 1024 * 1024) { // Less than 10MB
                health.isHealthy = false;
                health.lastError = "Low disk space (< 10MB available)";
                System.out.println(storageName + ": Low disk space");
                return;
            }
            
            // Counts files
            File[] files = storageDir.listFiles();
            health.fileCount = (files != null) ? files.length : 0;
            
            // When all checks passed
            health.isHealthy = true;
            health.lastError = null;
            health.lastCheckTime = System.currentTimeMillis();
            
            System.out.println(storageName + ": Success - " + health.fileCount + " files, " + 
                             formatBytes(health.usableSpace) + " available");
            
        } catch (Exception e) {
            health.isHealthy = false;
            health.lastError = "Error checking storage: " + e.getMessage();
            System.out.println(storageName + ": Error - " + e.getMessage());
        }
    }
    
    // Checks if health check needed
    public boolean needsHealthCheck() {
        return (System.currentTimeMillis() - lastCheckTime) > CHECK_INTERVAL;
    }
    

    // Retrieves health storage nodes
    public List<String> getHealthyStorage() {
        // Runs health check if needed
        if (needsHealthCheck()) {
            checkAllStorage();
        }
        
        List<String> healthy = new ArrayList<>();
        
        for (Map.Entry<String, StorageHealth> entry : storageHealth.entrySet()) {
            if (entry.getValue().isHealthy) {
                healthy.add(entry.getKey());
            }
        }
        
        return healthy;
    }
    

    // Checks if specific storage is healthy
    public boolean isStorageHealthy(String storageName) {
        StorageHealth health = storageHealth.get(storageName);
        
        if (health == null) {
            return false;
        }
        
        // Runs health check if needed
        if (needsHealthCheck()) {
            checkStorage(storageName);
        }
        
        return health.isHealthy;
    }
    

    // Retrieves health statusof all storages
    public Map<String, StorageHealth> getAllHealthStatus() {
        // Runs health check if needed
        if (needsHealthCheck()) {
            checkAllStorage();
        }
        
        return new HashMap<>(storageHealth);
    }
    

    // Retrieves health report as a string
    public String getHealthReport() {
        checkAllStorage();
        
        StringBuilder report = new StringBuilder();
        report.append("--- STORAGE HEALTH REPORT ---\n\n");
        
        for (Map.Entry<String, StorageHealth> entry : storageHealth.entrySet()) {
            StorageHealth health = entry.getValue();
            
            report.append(String.format("Storage: %s\n", health.storageName));
            report.append(String.format("  Status: %s\n", health.isHealthy ? "Healthy" : "Unhealthy"));
            
            if (!health.isHealthy && health.lastError != null) {
                report.append(String.format("  Error: %s\n", health.lastError));
            }
            
            if (health.isHealthy) {
                report.append(String.format("  Files: %d\n", health.fileCount));
                report.append(String.format("  Available: %s / %s\n", 
                    formatBytes(health.usableSpace), 
                    formatBytes(health.totalSpace)));
            }
            
            report.append("\n");
        }
        
        return report.toString();
    }
    

    // Format file size: bytes -> KB/MB
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
