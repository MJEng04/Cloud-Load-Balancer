-- ==================================
-- COMP20081 - MySQL Database Script
-- ==================================

-- Create database
CREATE DATABASE IF NOT EXISTS n1264601cwk_db;
USE n1264601cwk_db;

-- ===============
-- Table 1: Users
-- ===============
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'user',  -- can be 'user' or 'admin'
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- Table 2: Files (metadata for uploaded files)
-- =============================================
CREATE TABLE IF NOT EXISTS files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    original_size BIGINT NOT NULL,  -- Size in bytes
    total_chunks INT NOT NULL,
    owner_username VARCHAR(100) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_username) REFERENCES users(username) ON DELETE CASCADE
);

-- ============================================
-- Table 3: Chunks (info for each file chunk)
-- ============================================
CREATE TABLE IF NOT EXISTS chunks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT NOT NULL,
    chunk_number INT NOT NULL,
    storage_location VARCHAR(50) NOT NULL,  -- 'storage1' or 'storage2'
    checksum VARCHAR(100),
    chunk_size INT NOT NULL,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    UNIQUE KEY unique_chunk (file_id, chunk_number)
);

-- =============================================
-- Table 4: File Permissions (for file sharing)
-- =============================================
CREATE TABLE IF NOT EXISTS file_permissions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    file_id INT NOT NULL,
    username VARCHAR(100) NOT NULL,
    can_read BOOLEAN DEFAULT TRUE,
    can_write BOOLEAN DEFAULT FALSE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    UNIQUE KEY unique_permission (file_id, username)
);

-- =====================================
-- test admin user (password: admin123)
-- =====================================
INSERT INTO users (username, password, role) 
VALUES ('admin', 'admin123', 'admin')
ON DUPLICATE KEY UPDATE username=username;

-- =======================================
-- test standard user (password: test123)
-- =======================================
INSERT INTO users (username, password, role) 
VALUES ('testuser', 'test123', 'standard')
ON DUPLICATE KEY UPDATE username=username;

-- ====================
-- Show created table
-- ====================
SHOW TABLES;
