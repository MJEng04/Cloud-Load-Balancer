package com.mycompany.javafxapplication1.services;

import com.mycompany.javafxapplication1.models.FileOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;


// Decides what storage to use - Utilises multiple scheduling algorithms (e.g. Round robin, FCFS, SJN)
public class LoadBalancer {
    
    // Available scheduling algorithm
    public enum Algorithm {
        ROUND_ROBIN,    // Round Robin
        FCFS,           // First Come First Served
        SJN             // Shortest Job Next
    }
    
    // Lists available storage locations
    private List<String> storageNodes;
    private int currentIndex;
    
    // Current algorithm
    private Algorithm currentAlgorithm;
    
    // Request queue for FCFS
    private Queue<FileOperation> fcfsQueue;
    
    // Priority queue for SJN -> sorted by file size
    private PriorityQueue<FileOperation> sjnQueue;
    
    // Constructor
    public LoadBalancer() {
        storageNodes = new ArrayList<>();
        storageNodes.add("storage1");
        storageNodes.add("storage2");
        currentIndex = 0;
        currentAlgorithm = Algorithm.ROUND_ROBIN;
    
        
        // Initialises queues
        fcfsQueue = new LinkedList<>();
        sjnQueue = new PriorityQueue<>(new Comparator<FileOperation>() {
            @Override
            public int compare(FileOperation o1, FileOperation o2) {
                // Smallest file first -> Shortest Job Next
                return Long.compare(o1.getFileSize(), o2.getFileSize());
            }
        });
    }
    

    // Sets scheduling algorithm
    public void setAlgorithm(Algorithm algorithm) {
        this.currentAlgorithm = algorithm;
        System.out.println("Algorithm switched to: " + algorithm);
    }
    

    // Retrieves next storage location using current algorithm
    public String getNextStorage() {
        switch (currentAlgorithm) {
            case ROUND_ROBIN:
                return getNextStorageRoundRobin();
            case FCFS:
                return getNextStorageFCFS();
            case SJN:
                return getNextStorageSJN();
            default:
                return getNextStorageRoundRobin();
        }
    }
    

    // Retrieves next storage location with file size - for SJN
    public String getNextStorage(long fileSize) {
        switch (currentAlgorithm) {
            case SJN:
                return getNextStorageSJN(fileSize);
            default:
                return getNextStorage();
        }
    }
    
    // Round robin scheduling algorithm - takes turns (storage1, storage2, storage1, storage2, ...)
    public String getNextStorageRoundRobin() {
        String selected = storageNodes.get(currentIndex);
        
        // Move to next storage next time
        currentIndex = (currentIndex + 1) % storageNodes.size();
        
        System.out.println("LoadBalancer: Selected " + selected);
        return selected;
    }
    
    // First Come First Serve -> process the requests in the order that they arrive
    private String getNextStorageFCFS() {
        String selected = storageNodes.get(currentIndex);
        currentIndex = (currentIndex + 1) % storageNodes.size();
        
        System.out.println("LoadBalancer: Selected " + selected);
        return selected;
    }
    

    // Shortest Job Next - prioritises smaller files
    private String getNextStorageSJN() {
        // The default behavior when no file size given
        String selected = storageNodes.get(currentIndex);
        currentIndex = (currentIndex + 1) % storageNodes.size();
        
        System.out.println("LoadBalancer: Selected " + selected);
        return selected;
    }
    

    // SJN with file size
    private String getNextStorageSJN(long fileSize) {
        // Searches for storage with least total load
        String selected = storageNodes.get(currentIndex);
        currentIndex = (currentIndex + 1) % storageNodes.size();
        
        System.out.println("LoadBalancer: Selected " + selected + " for file (" + formatSize(fileSize) + ")");
        return selected;
    }
    
    // Adds more storage if needed - scalability
    public void addStorage(String storageName) {
        if (!storageNodes.contains(storageName)) {
            storageNodes.add(storageName);
            System.out.println("LoadBalancer: Added new storage - " + storageName);
        }
    }
    
    // Removes storage if down
    public void removeStorage(String storageName) {
        storageNodes.remove(storageName);
        System.out.println("LoadBalancer: Removed storage - " + storageName);
        
        // Resets index when needed
        if (currentIndex >= storageNodes.size()) {
            currentIndex = 0;
        }
    }
    
    // Retrieves list of all storage nodes
    public List<String> getStorageNodes() {
        return new ArrayList<>(storageNodes);
    }
    
    // Retrieves storage count
    public int getStorageCount() {
        return storageNodes.size();
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
