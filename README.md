# Distributed File Storage System

A distributed cloud-based file storage system with a load balancer, built using 
Java, JavaFX, Docker, and MySQL. Features role-based access, AES encryption, 
file chunking, and multiple scheduling algorithms.

## Tech Stack
- **Language:** Java (Maven)
- **GUI:** JavaFX
- **Database:** MySQL (Dockerised)
- **Infrastructure:** Docker & Docker Compose
- **Security:** AES encryption, PBKDF2 password hashing

## Features

### Core System
- User authentication — register and login with hashed passwords (PBKDF2 with salt)
- Role-based access — Admin and Standard user roles with permission enforcement
- File upload and download with automatic 1MB chunking and CRC32 integrity checking
- AES encryption of file chunks before storage, decryption on download
- Cross-session persistence — files remain after application restart

### Load Balancer
- Round Robin scheduling across multiple storage nodes
- Multiple scheduling algorithms supported
- Health checks — only healthy storage nodes receive traffic
- Artificial delay simulation to emulate real-world cloud latency
- Performance metrics tracking — upload/download times, storage distribution, efficiency reports

### Additional Features
- File sharing between users with read/write permission controls
- Terminal command interface within the application
- JavaFX GUI for full file management
- Docker Compose orchestration with persistent volumes

## Architecture
┌─────────────────────────────────┐
│     JavaFX GUI Application      │
├─────────────────────────────────┤
│         Load Balancer           │
│   (scheduling + health checks)  │
├───────────────┬─────────────────┤
│  File Server  │  File Server 2  │
│    Node 1     │                 │
├───────────────┴─────────────────┤
│         MySQL Database          │
│      (Docker containerised)     │
└─────────────────────────────────┘

## How to Run

**Prerequisites:**
- Java 20+
- Maven
- Docker Desktop

**Step 1 — Start the database:**
```bash
docker-compose up -d
```

**Step 2 — Build the project:**
```bash
mvn clean install
```

**Step 3 — Run the application:**
```bash
mvn javafx:run
```

**Default credentials:**
- Admin account is created on first run
- See `database-schema.sql` for database structure

## Project Structure
cloud-load-balancer/
├── app/
│   └── src/
│       ├── main/java/
│       │   ├── controllers/
│       │   ├── dao/
│       │   ├── models/
│       │   └── services/
│       └── resources/
├── database-schema.sql
└── README.md

## Development Timeline
Built across 10 days covering core file system, database integration, 
user roles, security, terminal interface, load balancer improvements, 
Docker integration, health checking, delay simulation, and performance metrics.