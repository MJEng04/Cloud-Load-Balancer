# Distributed File Storage System

A distributed cloud-based file storage system with a load balancer, 
built using Java, JavaFX, Docker, and MySQL. Features role-based access, 
AES encryption, 1MB file chunking with CRC32 integrity checking, and 
multiple scheduling algorithms.

## Tech Stack
- **Language:** Java (Maven)
- **GUI:** JavaFX
- **Database:** MySQL (Dockerised)
- **Infrastructure:** Docker & Docker Compose
- **Security:** AES-256 encryption, PBKDF2 password hashing with salt
- **Build:** Maven

## Features

### File Management
- Upload and download files of any size
- Automatic 1MB file chunking with CRC32 integrity validation
- AES-256 encryption of all chunks before storage
- Decryption on download — transparent to the user
- Cross-session persistence — files survive application restarts

### Load Balancer
- Distributes file chunks evenly across multiple storage nodes
- Multiple scheduling algorithms — Round Robin, FCFS, Priority, 
  Shortest Job Next, Round Robin, and Multiple-Level Queues
- Health checks — automatically excludes unhealthy storage nodes
- Artificial delay simulation (30-90 seconds) to emulate real cloud latency
- Performance metrics — tracks upload/download times, storage distribution, 
  and load balancer efficiency

### User System
- Register and login with PBKDF2 hashed passwords and salt
- Role-based access — Admin and Standard users
- File sharing between users with read/write permission controls
- Access control lists enforced on all file operations

### Interface
- JavaFX GUI for full file management
- Built-in terminal command interface
- Supported commands: mv, cp, ls, mkdir, ps, whoami, tree, nano
- Log viewer and metrics dashboard

## Architecture
# Distributed File Storage System

A distributed cloud-based file storage system with a load balancer, 
built using Java, JavaFX, Docker, and MySQL. Features role-based access, 
AES encryption, 1MB file chunking with CRC32 integrity checking, and 
multiple scheduling algorithms.

## Tech Stack
- **Language:** Java (Maven)
- **GUI:** JavaFX
- **Database:** MySQL (Dockerised)
- **Infrastructure:** Docker & Docker Compose
- **Security:** AES-256 encryption, PBKDF2 password hashing with salt
- **Build:** Maven

## Features

### File Management
- Upload and download files of any size
- Automatic 1MB file chunking with CRC32 integrity validation
- AES-256 encryption of all chunks before storage
- Decryption on download — transparent to the user
- Cross-session persistence — files survive application restarts

### Load Balancer
- Distributes file chunks evenly across multiple storage nodes
- Multiple scheduling algorithms — Round Robin, FCFS, Priority, 
  Shortest Job Next, Round Robin, and Multiple-Level Queues
- Health checks — automatically excludes unhealthy storage nodes
- Artificial delay simulation (30-90 seconds) to emulate real cloud latency
- Performance metrics — tracks upload/download times, storage distribution, 
  and load balancer efficiency

### User System
- Register and login with PBKDF2 hashed passwords and salt
- Role-based access — Admin and Standard users
- File sharing between users with read/write permission controls
- Access control lists enforced on all file operations

### Interface
- JavaFX GUI for full file management
- Built-in terminal command interface
- Supported commands: mv, cp, ls, mkdir, ps, whoami, tree, nano
- Log viewer and metrics dashboard

## Architecture
┌─────────────────────────────────────┐
│        JavaFX GUI Application        │
│    (File Management + Terminal)      │
├─────────────────────────────────────┤
│           Load Balancer              │
│  (Scheduling + Health Checks)        │
├──────────────────┬──────────────────┤
│  File Storage    │  File Storage    │
│    Node 1        │    Node 2        │
├──────────────────┴──────────────────┤
│         MySQL Database               │
│      (Docker containerised)          │
└─────────────────────────────────────┘

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

See `docker_database.sql` for the full database schema.

## Project Structure
cloud-load-balancer/
├── app/
│   └── JavaFXApplication1/
│       ├── src/main/java/com/mycompany/javafxapplication1/
│       │   ├── database/      # DAO classes and DB connection
│       │   ├── models/        # User, File, Chunk entities
│       │   ├── services/      # Load balancer, encryption, file services
│       │   ├── FileManagementController.java
│       │   ├── TerminalController.java
│       │   ├── MetricsViewerController.java
│       │   └── App.java
│       ├── src/main/resources/  # FXML layout files
│       ├── docker-compose.yml
│       └── pom.xml
├── database-schema.sql
└── README.md

## Development Timeline
Built across 10 days — core file system, MySQL integration, user roles 
and file sharing, AES encryption and password hashing, terminal command 
interface, load balancer scheduling algorithms, Docker containerisation, 
health checking, artificial delay simulation, and performance metrics.


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

See `docker_database.sql` for the full database schema.

## Project Structure
cloud-load-balancer/
├── app/
│   └── JavaFXApplication1/
│       ├── src/main/java/com/mycompany/javafxapplication1/
│       │   ├── database/      # DAO classes and DB connection
│       │   ├── models/        # User, File, Chunk entities
│       │   ├── services/      # Load balancer, encryption, file services
│       │   ├── FileManagementController.java
│       │   ├── TerminalController.java
│       │   ├── MetricsViewerController.java
│       │   └── App.java
│       ├── src/main/resources/  # FXML layout files
│       ├── docker-compose.yml
│       └── pom.xml
├── database-schema.sql
└── README.md

## Development Timeline
Built across 10 days — core file system, MySQL integration, user roles 
and file sharing, AES encryption and password hashing, terminal command 
interface, load balancer scheduling algorithms, Docker containerisation, 
health checking, artificial delay simulation, and performance metrics.
