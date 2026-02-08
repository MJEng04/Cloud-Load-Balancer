package com.mycompany.javafxapplication1.services;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// Logs user actions and system events - written to system.log file as well for debugging
public class SystemLogger {
    
    private static final String LOG_FILE = "logs/system.log";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // All log levels
    public enum LogLevel {
        INFO,
        WARNING,
        ERROR,
        SECURITY,
        SYSTEM
    }
    
   
    // Initialises log -> creates log direectory if needed
    static {
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
        } catch (Exception e) {
            System.err.println("Failed to create logs directory: " + e.getMessage());
        }
    }
    

    // Logs message with appropiate level
    public static void log(LogLevel level, String message) {
        String timestamp = dateFormat.format(new Date());
        String logEntry = String.format("[%s] [%s] %s%n", timestamp, level, message);
        
        // Writes to file
        try (FileWriter fw = new FileWriter(LOG_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            out.print(logEntry);
            
        } catch (IOException e) {
            System.err.println("Failed to write to log: " + e.getMessage());
        }
        
        // Prints to console
        System.out.print(logEntry);
    }
    

    // Logs user action
    public static void logUserAction(String username, String action) {
        log(LogLevel.INFO, "USER: " + username + " - " + action);
    }
    
    
    // Logs system event
    public static void logSystemEvent(String event) {
        log(LogLevel.SYSTEM, event);
    }
    
    // Logs security event
    public static void logSecurity(String event) {
        log(LogLevel.SECURITY, event);
    }
    
    // Logs error
    public static void logError(String error) {
        log(LogLevel.ERROR, error);
    }
    
    // Logs warning
    public static void logWarning(String warning) {
        log(LogLevel.WARNING, warning);
    }
    
    
    // Retrieves all logs as strings
    public static String getAllLogs() {
        StringBuilder logs = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                logs.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            return "No logs found.";
        } catch (IOException e) {
            return "Error reading logs: " + e.getMessage();
        }
        
        return logs.toString();
    }
    

    // Retrieves recent logs
    public static String getRecentLogs(int numLines) {
        StringBuilder logs = new StringBuilder();
        
        try (BufferedReader br = new BufferedReader(new FileReader(LOG_FILE))) {
            // Reads all logs
            java.util.List<String> allLines = new java.util.ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                allLines.add(line);
            }
            
            int start = Math.max(0, allLines.size() - numLines);
            for (int i = start; i < allLines.size(); i++) {
                logs.append(allLines.get(i)).append("\n");
            }
            
        } catch (FileNotFoundException e) {
            return "No logs found.";
        } catch (IOException e) {
            return "Error reading logs: " + e.getMessage();
        }
        
        return logs.toString();
    }
    

    // Clears logs
    public static void clearLogs() {
        try (PrintWriter writer = new PrintWriter(LOG_FILE)) {
            writer.print("");
            log(LogLevel.SYSTEM, "Logs cleared");
        } catch (IOException e) {
            System.err.println("Failed to clear logs: " + e.getMessage());
        }
    }
    
    // Logs file uploads
    public static void logFileUpload(String username, String filename, long fileSize, int chunks) {
        logUserAction(username, String.format("UPLOAD: %s (Size: %d bytes, Chunks: %d)", 
            filename, fileSize, chunks));
    }
    
    
    // Logs file download
    public static void logFileDownload(String username, String filename) {
        logUserAction(username, "DOWNLOAD: " + filename);
    }
    

    // Logs file deletions
    public static void logFileDelete(String username, String filename) {
        logUserAction(username, "DELETE: " + filename);
    }
    

    // Logs file shares
    public static void logFileShare(String ownerUsername, String filename, String sharedWith, String permissions) {
        logUserAction(ownerUsername, String.format("SHARE: %s with %s (%s)", 
            filename, sharedWith, permissions));
    }
    

    // Logs logins
    public static void logLogin(String username, boolean success) {
        if (success) {
            logSecurity("LOGIN SUCCESS: " + username);
        } else {
            logSecurity("LOGIN ERROR: " + username);
        }
    }
    

    // Logs registrations
    public static void logRegistration(String username) {
        logSecurity("USER REGISTERED: " + username);
    }
    

    // Logs encryptions
    public static void logEncryption(int fileId, int chunkNumber) {
        logSystemEvent(String.format("ENCRYPTION: file_%d_chunk_%d", fileId, chunkNumber));
    }
    
    // Logs decryptions
    public static void logDecryption(int fileId, int chunkNumber) {
        logSystemEvent(String.format("DECRYPTION: file_%d_chunk_%d", fileId, chunkNumber));
    }
    

    // Logs load balancer actions
    public static void logLoadBalancer(String algorithm, String storageSelected) {
        logSystemEvent(String.format("LOAD BALANCER [%s]: Selected %s", algorithm, storageSelected));
    }
}
