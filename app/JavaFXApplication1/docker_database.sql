-- Database for Docker 


CREATE DATABASE IF NOT EXISTS n1264601_dkrdb;
USE n1264601_dkrdb;

-- ========
-- Users --
-- ========
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'standard') DEFAULT 'standard',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========
-- Files --
-- ========
CREATE TABLE IF NOT EXISTS files (
    id INT AUTO_INCREMENT PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_size BIGINT NOT NULL,
    total_chunks INT NOT NULL,
    owner_username VARCHAR(50) NOT NULL,
    upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (owner_username) REFERENCES users(username) ON DELETE CASCADE,
    INDEX idx_owner (owner_username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========
-- Chunks --
-- =========
CREATE TABLE IF NOT EXISTS chunks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_id INT NOT NULL,
    chunk_number INT NOT NULL,
    storage_location VARCHAR(50) NOT NULL,
    checksum VARCHAR(255) NOT NULL,
    chunk_size INT NOT NULL,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    INDEX idx_file_chunk (file_id, chunk_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================
-- File Permissions --
-- ===================
CREATE TABLE IF NOT EXISTS file_permissions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    file_id INT NOT NULL,
    username VARCHAR(50) NOT NULL,
    can_read BOOLEAN DEFAULT FALSE,
    can_write BOOLEAN DEFAULT FALSE,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE,
    UNIQUE KEY unique_file_user (file_id, username),
    INDEX idx_user_perms (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

