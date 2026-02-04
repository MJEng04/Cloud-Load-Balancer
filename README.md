# Load-Balancer-CWK
COMP20081 Systems Software CWK

Implementation and Validation of a Cloud Load Balancer

# Desc
This project implements a distributed cloud-based file storage system with a load balancer,
developed as part of the COMP20081 Systems Software coursework.

# Key Features
- User Authentication (Register/Login via MySQL)
- Role-based Users (Admin / Standard)
- File Upload & Download
- 1MB File Chunking with CRC32 integrity checking
- Round Robin Load Balancer across multiple storage nodes
- Persistent Storage using MySQL
- JavaFX GUI for file management
- Cross-session Persistence (files remain after restart)

# Technologies
- Java (Maven)
- JavaFX
- Docker & Docker Compose
- MySQL
- SQLite

#  Development Progress
**Day 1 – Core File System**
- Implemented file upload and download functionality
- Files
    - split into smaller chunks for storage
    - distributed evenly across multiple storage folders
    - can be reconstructed correctly after download

**Day 2 – Database & GUI Integration**
- Connected the application to a MySQL database
- User login and registration now use MySQL
- File information is saved to the database and persists after restart
- Integrated database-backed file management into the JavaFX GUI

**Day 3 – User Roles & File Sharing**
- Added file sharing between users
- Added read and write access controls
- Added a share dialog to select users and permissions
- Added a toggle to switch between My Files and Shared Files
- System checks user permissions before allowing file actions
- Updated FileDAO to support file sharing
- Updated UserDAO to support user management
- Applied access checks in FileManagementController

**Day 4 – Security & Cleanup**
- Added password hashing using PBKDF2 (with salt)
- Implemented AES encryption for file chunks before storage
- Files now stored encrypted on disk and decrypted on download
- Removed old SQLite database code
- Updated app startup to use MySQL only
- Fixed NetBeans crashes caused by multiple database initialisations

**Day 5 – Terminal Command Interface**
- Introduced a terminal command interface within the application
- Added command validation for user input
- Implemented file management commands
- Added formatted output for file listings
- Added help command to display supported commands
- Added error messages for invalid commands and permissions

**Day 6 – Load Balancer Improvements**
- Updated the load balancer to support multiple scheduling algorithms
- Updated chunk distribution to work across all algorithms
