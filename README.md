# Load-Balancer-CWK
COMP20081 Systems Software CWK

Implementation and Validation of a Cloud Load Balancer

**Desc**
This project implements a distributed cloud-based file storage system with a load balancer,
developed as part of the COMP20081 Systems Software coursework.

**Key Features**
- User Authentication (Register/Login via MySQL)
- Role-based Users (Admin / Standard)
- File Upload & Download
- 1MB File Chunking with CRC32 integrity checking
- Round Robin Load Balancer across multiple storage nodes
- Persistent Storage using MySQL
- JavaFX GUI for file management
- Cross-session Persistence (files remain after restart)

**Technologies**
- Java (Maven)
- JavaFX
- Docker & Docker Compose
- MySQL
- SQLite

**Development Progress**
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

**Day 3 – User Roles & File Sharing (In Progress)**
- Introducing standard and admin user roles
- Adding file sharing between users
- Implementing read and write permissions
- Preparing admin features for user management

