package com.mycompany.javafxapplication1.services;

import com.mycompany.javafxapplication1.models.FileOperation;
import java.util.ArrayList;
import java.util.List;


// Decides what storage to use - Utilises Round Robin algorithm
public class LoadBalancer {
   // Lists available storage locations
    private List<String> storageNodes;
    private int currentIndex;
    
    // Constructor
    public LoadBalancer() {
        storageNodes = new ArrayList<>();
        storageNodes.add("storage1");
        storageNodes.add("storage2");
        currentIndex = 0;
    }
    
    // Round robin scheduling algorithm - takes turns (storage1, storage2, storage1, storage2, ...)
    public String getNextStorage() {
        String selected = storageNodes.get(currentIndex);
        
        // Move to next storage next time
        currentIndex = (currentIndex + 1) % storageNodes.size();
        
        System.out.println("LoadBalancer: Selected " + selected);
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
    
    // Test
    public static void main(String[] args) {
        LoadBalancer lb = new LoadBalancer();
        
        // Test Round Robin
        System.out.println("Testing Round Robin algorithm:");
        for (int i = 0; i < 6; i++) {
            String storage = lb.getNextStorage();
            System.out.println("Request " + i + " -> " + storage);
        }
        
        // Output: storage1, storage2, storage1, storage2, storage1, storage2
    } 
}
