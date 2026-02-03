package com.mycompany.javafxapplication1.services;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;


// Handles encryption for file chunks
public class EncryptionService {
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 16;
    
    private SecretKey secretKey;
    
    
    // Generates new encryption key
    public EncryptionService() {
        try {
            this.secretKey = generateKey();
            System.out.println("Encryption service initialised");
        } catch (Exception e) {
            System.err.println("Failed to initialise encryption: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    // Constructor - has existing key
    public EncryptionService(String base64Key) {
        try {
            byte[] decodedKey = Base64.getDecoder().decode(base64Key);
            this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
            System.out.println("Encryption service initialise with existing key");
        } catch (Exception e) {
            System.err.println("Failed to load encryption key: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    // Encrypts data - returns IV (initialisation vector) + encrypted data
    public byte[] encrypt(byte[] data) throws Exception {
        // Generates random IV
        byte[] iv = generateIV();
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Initialises cipher
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        
        // Encrypts data
        byte[] encryptedData = cipher.doFinal(data);
        
        // Combines IV + encrypted data
        byte[] combined = new byte[IV_SIZE + encryptedData.length];
        System.arraycopy(iv, 0, combined, 0, IV_SIZE);
        System.arraycopy(encryptedData, 0, combined, IV_SIZE, encryptedData.length);
        
        return combined;
    }
    

    // Decrypts data - anticipates IV + encrypted data
    public byte[] decrypt(byte[] combined) throws Exception {
        // Extracts IV
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        
        // Extractz encrypted data
        byte[] encryptedData = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, IV_SIZE, encryptedData, 0, encryptedData.length);
        
        // Initialises cipher
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        
        // Decrypts data
        return cipher.doFinal(encryptedData);
    }
    

    // Generates key
    private SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }
    

    // Generates random IV
    private byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
    

    // Retrieves encryption key as  base-64 string -> for storage
    public String getKeyAsString() {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}
