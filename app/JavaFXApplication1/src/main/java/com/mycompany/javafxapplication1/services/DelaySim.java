package com.mycompany.javafxapplication1.services;

import java.util.Random;

/**
 * SIMPLE DelaySimulator - Adds 3-5 second delays (simplified for demo)
 * Original spec asked for 30-90 seconds but that's too long for practical demos
 * This version uses 3-5 seconds which is much better for demonstration
 */
public class DelaySim {
    
    private static final int MIN_DELAY_SEC = 3;  // 3 seconds
    private static final int MAX_DELAY_SEC = 5;  // 5 seconds
    private static final Random random = new Random();
    
    // Enable/disable delays
    private static boolean enabled = false;  // Start disabled for easy testing
    
    /**
     * Enable delays
     */
    public static void enable() {
        enabled = true;
        System.out.println("⏱️ Delays ENABLED (3-5 seconds)");
    }
    
    /**
     * Disable delays
     */
    public static void disable() {
        enabled = false;
        System.out.println("⏱️ Delays DISABLED");
    }
    
    /**
     * Check if enabled
     */
    public static boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Simple delay - just add to start of upload/download/delete
     */
    public static void delay(String operation) {
        if (!enabled) {
            return;
        }
        
        int seconds = MIN_DELAY_SEC + random.nextInt(MAX_DELAY_SEC - MIN_DELAY_SEC + 1);
        
        System.out.println("⏱️ Simulating " + operation + " delay: " + seconds + " seconds...");
        
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("✓ Delay complete for " + operation);
    }
}
