package com.mycompany.javafxapplication1.services;

import java.util.*;


// Peformance Metrics - Tracks system performance - monitors load balancer and storage statistics
public class PerformanceMetrics {
    
    //  Singleton instance
    private static PerformanceMetrics instance;
    
    // Metrics data
    private Map<String, Integer> storageUsage;  // Files per storage
    private Map<String, Long> storageBytesUsed; // Bytes per storage
    private List<Long> uploadTimes;  // Upload time in milliseconds
    private List<Long> downloadTimes;  // Download time in milliseconds
    private int totalUploads;
    private int totalDownloads;
    private int totalDeletes;
    private long startTime;
    

    // Constructor
    private PerformanceMetrics() {
        storageUsage = new HashMap<>();
        storageBytesUsed = new HashMap<>();
        uploadTimes = new ArrayList<>();
        downloadTimes = new ArrayList<>();
        totalUploads = 0;
        totalDownloads = 0;
        totalDeletes = 0;
        startTime = System.currentTimeMillis();
        
        // Initialise storage locations
        storageUsage.put("storage1", 0);
        storageUsage.put("storage2", 0);
        storageBytesUsed.put("storage1", 0L);
        storageBytesUsed.put("storage2", 0L);
    }
    

    // Retrieves singleton instance
    public static synchronized PerformanceMetrics getInstance() {
        if (instance == null) {
            instance = new PerformanceMetrics();
        }
        return instance;
    }
    
    
    // Records file uploads
    public void recordUpload(String storage, long fileSize, long timeMs) {
        totalUploads++;
        uploadTimes.add(timeMs);
        
        storageUsage.put(storage, storageUsage.getOrDefault(storage, 0) + 1);
        storageBytesUsed.put(storage, storageBytesUsed.getOrDefault(storage, 0L) + fileSize);
        
        System.out.println("Upload recorded - " + storage + " (" + formatSize(fileSize) + " in " + timeMs + "ms)");
    }
    
    // Records file downloads
    public void recordDownload(long timeMs) {
        totalDownloads++;
        downloadTimes.add(timeMs);
        
        System.out.println("Download recorded (" + timeMs + "ms)");
    }
    
    // Records file deletions
    public void recordDelete(String storage, long fileSize) {
        totalDeletes++;
        
        storageUsage.put(storage, Math.max(0, storageUsage.getOrDefault(storage, 0) - 1));
        storageBytesUsed.put(storage, Math.max(0, storageBytesUsed.getOrDefault(storage, 0L) - fileSize));
        
        System.out.println("Delete recorded - " + storage);
    }
    
    // Retrieves total uploads
    public int getTotalUploads() {
        return totalUploads;
    }
   
    // Retrieves total downloads
    public int getTotalDownloads() {
        return totalDownloads;
    }
   
    // Retrieves total deletes
    public int getTotalDeletes() {
        return totalDeletes;
    }
    
    // Retrieves average upload time
    public long getAverageUploadTime() {
        if (uploadTimes.isEmpty()) return 0;
        long sum = 0;
        for (long time : uploadTimes) {
            sum += time;
        }
        return sum / uploadTimes.size();
    }
    
    // Retrieves average download time
    public long getAverageDownloadTime() {
        if (downloadTimes.isEmpty()) return 0;
        long sum = 0;
        for (long time : downloadTimes) {
            sum += time;
        }
        return sum / downloadTimes.size();
    }
    

    // Retrieves storage usage -> file count
    public Map<String, Integer> getStorageUsage() {
        return new HashMap<>(storageUsage);
    }

    // Retrieves storage bytes used 
    public Map<String, Long> getStorageBytesUsed() {
        return new HashMap<>(storageBytesUsed);
    }
    

    // Retrieves system uptime in seconds
    public long getUptimeSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000;
    }
    

    // Retrieves load balancer efficiency - shows how evenly distributed they are
    public double getLoadBalancerEfficiency() {
        if (totalUploads == 0) return 100.0;
        
        int storage1Count = storageUsage.getOrDefault("storage1", 0);
        int storage2Count = storageUsage.getOrDefault("storage2", 0);
        
        // Perfect balance = 50/50 split
        int total = storage1Count + storage2Count;
        if (total == 0) return 100.0;
        
        double storage1Pct = (storage1Count * 100.0) / total;
        double storage2Pct = (storage2Count * 100.0) / total;
        
        // Efficiency = 100% - deviation from 50/50
        double deviation = Math.abs(storage1Pct - 50.0) + Math.abs(storage2Pct - 50.0);
        return Math.max(0, 100.0 - deviation);
    }
    

    // Retrieves metrics report
    public String getMetricsReport() {
        StringBuilder report = new StringBuilder();
        report.append("--- PERFORMANCE METRICS ---\n\n");
        
        // Shows System uptime
        long uptime = getUptimeSeconds();
        report.append(String.format("System Uptime: %d seconds (%.1f minutes)\n\n", 
            uptime, uptime / 60.0));
        
        // Shows File operations
        report.append("File Operations:\n");
        report.append(String.format("  Uploads: %d\n", totalUploads));
        report.append(String.format("  Downloads: %d\n", totalDownloads));
        report.append(String.format("  Deletes: %d\n", totalDeletes));
        report.append(String.format("  Total Operations: %d\n\n", 
            totalUploads + totalDownloads + totalDeletes));
        
        // Shows Performance times
        report.append("Average Times:\n");
        report.append(String.format("  Upload: %.2f seconds\n", getAverageUploadTime() / 1000.0));
        report.append(String.format("  Download: %.2f seconds\n\n", getAverageDownloadTime() / 1000.0));
        
        // Shows Storage distribution
        report.append("Storage Distribution:\n");
        for (Map.Entry<String, Integer> entry : storageUsage.entrySet()) {
            String storage = entry.getKey();
            int fileCount = entry.getValue();
            long bytesUsed = storageBytesUsed.getOrDefault(storage, 0L);
            
            report.append(String.format("  %s: %d files (%s)\n", 
                storage, fileCount, formatSize(bytesUsed)));
        }
        report.append("\n");
        
        // Shows Load balancer efficiency
        report.append(String.format("Load Balancer Efficiency: %.1f%%\n", getLoadBalancerEfficiency()));
        
        return report.toString();
    }
    

    // Resets metrics
    public void reset() {
        storageUsage.clear();
        storageBytesUsed.clear();
        uploadTimes.clear();
        downloadTimes.clear();
        totalUploads = 0;
        totalDownloads = 0;
        totalDeletes = 0;
        startTime = System.currentTimeMillis();
        
        storageUsage.put("storage1", 0);
        storageUsage.put("storage2", 0);
        storageBytesUsed.put("storage1", 0L);
        storageBytesUsed.put("storage2", 0L);
        
        System.out.println("Metrics reset");
    }
    
    // Formats file size
    private String formatSize(long bytes) {
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

