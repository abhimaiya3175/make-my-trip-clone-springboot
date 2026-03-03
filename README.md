# MakeMyTrip Clone - Full Stack Application

A full-stack travel booking application built with Spring Boot (Java 21) backend and Next.js frontend, featuring flight and hotel booking capabilities.

## 📋 Table of Contents

- [⚡ Quick Start Guide](#-quick-start-guide)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Backend Setup (Spring Boot)](#backend-setup-spring-boot)
  - [Frontend Setup (Next.js)](#frontend-setup-nextjs)
- [Configuration](#configuration)
- [API Endpoints](#api-endpoints)
- [Troubleshooting](#troubleshooting)
- [Development](#development)

---

## ⚡ Quick Start Guide

> 📖 **For detailed step-by-step instructions, see [HOW-TO-RUN.md](HOW-TO-RUN.md)**

**Follow these steps to run the application locally (Windows):**

### Prerequisites Check
- ✅ JDK 21 installed at: `C:\Users\abhim\.jdk\jdk-21.0.8`
- ✅ Node.js and npm installed
- ✅ MongoDB running (Docker or local)

### Step-by-Step Instructions

**1. Enable PowerShell Script Execution** (First time only)
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
```

**2. Start MongoDB** (Choose one option)

**Option A - Using Docker (Recommended):**
```powershell
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

**Option B - Local MongoDB Service:**
```powershell
net start MongoDB
```

**3. Configure MongoDB Connection**

Ensure `src/main/resources/application.properties` has:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/makemytrip
```

**4. Start Backend** (First PowerShell window)

```powershell
# Navigate to project root
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main

# Set Java 21 environment
$env:JAVA_HOME = "C:\Users\abhim\.jdk\jdk-21.0.8"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Build the project (first time or after code changes)
.\mvnw.cmd clean package -DskipTests

# Run the application
& "C:\Users\abhim\.jdk\jdk-21.0.8\bin\java.exe" -jar target\makemytrip-0.0.1-SNAPSHOT.jar
```

✅ **Backend Running:** http://localhost:8080

**5. Start Frontend** (New PowerShell window)

```powershell
# Navigate to frontend directory
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main\makemytour

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

✅ **Frontend Running:** http://localhost:3000

**6. Access Application**

Open your browser: **http://localhost:3000**

---

### 🎯 Even Easier: Use Startup Scripts

We've created convenient startup scripts for you:

**Backend:**
```powershell
.\start-backend.ps1
```

**Frontend (in makemytour folder):**
```powershell
.\start-frontend.ps1
```

These scripts automatically:
- Set up Java 21 environment
- Check dependencies
- Verify MongoDB connection
- Start the respective service

---

### Verify Everything is Running

```powershell
# Check all services
Test-NetConnection localhost -Port 8080  # Backend
Test-NetConnection localhost -Port 3000  # Frontend
Test-NetConnection localhost -Port 27017 # MongoDB
```

### Stop the Application

Press `Ctrl+C` in each PowerShell window to stop the servers.

---

## 🚀 Technology Stack

### Backend
- **Java**: 21 (LTS)
- **Spring Boot**: 3.4.1
- **Spring Security**: 6.4.2
- **Spring Data MongoDB**: 3.4.1
- **Maven**: 3.9.9 (Maven Wrapper included)
- **Lombok**: 1.18.36

### Frontend
- **Next.js**: Latest (Pages Router)
- **React**: Latest
- **TypeScript**: Latest
- **Tailwind CSS**: For styling

### Database
- **MongoDB**: Latest

## 📦 Prerequisites

Before running this application locally, ensure you have the following installed:

1. **Java Development Kit (JDK) 21**
   - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/#java21) or [OpenJDK](https://adoptium.net/)
   - Verify installation: `java -version` (should show version 21.x.x)

2. **Node.js** (v18 or higher) and **npm**
   - Download from [nodejs.org](https://nodejs.org/)
   - Verify installation: `node -v` and `npm -v`

3. **MongoDB**
   - **Option A - Local Installation**: Download from [mongodb.com](https://www.mongodb.com/try/download/community)
   - **Option B - Docker**: `docker run -d -p 27017:27017 --name mongodb mongo:latest`
   - **Option C - MongoDB Atlas**: Use free cloud database from [mongodb.com/cloud/atlas](https://www.mongodb.com/cloud/atlas)

4. **Git** (optional, for version control)

## 📁 Project Structure

```
make-my-trip-clone-springboot-main/
├── src/                          # Spring Boot backend source code
│   ├── main/
│   │   ├── java/com/makemytrip/makemytrip/
│   │   │   ├── MakemytripApplication.java    # Main Spring Boot application
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java       # Security configuration
│   │   │   ├── controllers/
│   │   │   │   ├── AdminController.java      # Admin endpoints
│   │   │   │   ├── BookingController.java    # Booking management
│   │   │   │   ├── RootController.java       # Root/public endpoints
│   │   │   │   └── UserController.java       # User management
│   │   │   ├── models/
│   │   │   │   ├── Flight.java               # Flight entity
│   │   │   │   ├── Hotel.java                # Hotel entity
│   │   │   │   └── Users.java                # User entity
│   │   │   ├── repositories/
│   │   │   │   ├── FlightRepository.java     # Flight data access
│   │   │   │   ├── HotelRepository.java      # Hotel data access
│   │   │   │   └── UserRepository.java       # User data access
│   │   │   └── services/
│   │   │       ├── BookingService.java       # Booking business logic
│   │   │       └── UserServices.java         # User business logic
│   │   └── resources/
│   │       └── application.properties        # Backend configuration
│   └── test/                     # Test files
├── makemytour/                   # Next.js frontend application
│   ├── src/
│   │   ├── components/          # React components
│   │   ├── pages/               # Next.js pages
│   │   └── styles/              # CSS styles
│   ├── public/                  # Static files
│   └── package.json             # Frontend dependencies
├── pom.xml                      # Maven configuration
├── mvnw / mvnw.cmd              # Maven Wrapper scripts
└── README.md                    # This file
```

## 🏃 Getting Started

### Backend Setup (Spring Boot)

#### Step 1: Configure MongoDB Connection

Edit `src/main/resources/application.properties`:

```properties
spring.application.name=makemytrip
server.port=8080

# MongoDB Configuration - UPDATE THIS!
spring.data.mongodb.uri=mongodb://localhost:27017/makemytrip
spring.data.mongodb.database=makemytrip

# Security (disabled for development)
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

**MongoDB Connection String Examples:**

- **Local MongoDB**: `mongodb://localhost:27017/makemytrip`
- **MongoDB with Authentication**: `mongodb://username:password@localhost:27017/makemytrip`
- **MongoDB Atlas (Cloud)**: `mongodb+srv://username:password@cluster.mongodb.net/makemytrip`
- **Docker MongoDB**: `mongodb://localhost:27017/makemytrip`

#### Step 2: Set Java 21 Environment

**Windows (PowerShell):**
```powershell
# Set JAVA_HOME to JDK 21
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"  # Adjust path as needed

# Verify Java version
java -version
```

**Linux/Mac (Bash):**
```bash
# Set JAVA_HOME to JDK 21
export JAVA_HOME=/path/to/jdk-21  # Adjust path as needed

# Verify Java version
java -version
```

#### Step 3: Build the Backend

```bash
# Clean and build the project
./mvnw clean install

# Or on Windows
mvnw.cmd clean install
```

#### Step 4: Run the Backend

**Option A - Using Maven Wrapper (Recommended):**
```bash
# Linux/Mac
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

**Option B - Using JAR file:**
```bash
# Build JAR
./mvnw clean package

# Run JAR
java -jar target/makemytrip-0.0.1-SNAPSHOT.jar
```

**Option C - Using IDE:**
- Open project in IntelliJ IDEA / Eclipse / VS Code
- Run `MakemytripApplication.java` main class
- Ensure IDE is configured to use JDK 21

The backend server will start at: **http://localhost:8080**

#### Step 5: Verify Backend is Running

Open your browser or use curl:
```bash
curl http://localhost:8080
```

### Frontend Setup (Next.js)

#### Step 1: Navigate to Frontend Directory

```bash
cd makemytour
```

#### Step 2: Install Dependencies

```bash
npm install
# or
yarn install
# or
pnpm install
```

#### Step 3: Run the Development Server

```bash
npm run dev
# or
yarn dev
# or
pnpm dev
```

The frontend will start at: **http://localhost:3000**

#### Step 4: Access the Application

Open [http://localhost:3000](http://localhost:3000) in your browser to see the application.

## ⚙️ Configuration

### Backend Configuration

Edit `src/main/resources/application.properties`:

```properties
# Application Name
spring.application.name=makemytrip

# Server Port
server.port=8080

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/makemytrip
spring.data.mongodb.database=makemytrip

# Security Configuration
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration

# Logging (optional)
logging.level.com.makemytrip=DEBUG
logging.level.org.springframework.data.mongodb=DEBUG
```

### Frontend Configuration

The frontend connects to the backend API. If you change the backend port, update the API base URL in the frontend configuration.

## 🔌 API Endpoints

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Root endpoint |
| GET | `/api/flights` | Get all flights |
| GET | `/api/hotels` | Get all hotels |

### User Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users/register` | Register new user |
| POST | `/api/users/login` | User login |
| GET | `/api/users/profile` | Get user profile |

### Booking Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings/flight` | Book a flight |
| POST | `/api/bookings/hotel` | Book a hotel |
| GET | `/api/bookings/user/:userId` | Get user bookings |

### Admin Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/admin/flight` | Add new flight |
| POST | `/api/admin/hotel` | Add new hotel |
| PUT | `/api/admin/flight/:id` | Update flight |
| PUT | `/api/admin/hotel/:id` | Update hotel |
| DELETE | `/api/admin/flight/:id` | Delete flight |
| DELETE | `/api/admin/hotel/:id` | Delete hotel |

## 🐛 Troubleshooting

### Backend Issues

**1. MongoDB Connection Error**

```
Error: Connection refused to mongodb://localhost:27017
```

**Solution:**
- Ensure MongoDB is running: `mongod --version`
- Start MongoDB service:
  ```bash
  # Linux
  sudo systemctl start mongod
  
  # Mac
  brew services start mongodb-community
  
  # Windows
  net start MongoDB
  
  # Docker
  docker start mongodb
  ```
- Verify connection string in `application.properties`

**2. Port 8080 Already in Use**

```
Error: Port 8080 is already in use
```

**Solution:**
- Change port in `application.properties`: `server.port=8081`
- Or kill the process using port 8080:
  ```bash
  # Windows
  netstat -ano | findstr :8080
  taskkill /PID <PID> /F
  
  # Linux/Mac
  lsof -i :8080
  kill -9 <PID>
  ```

**3. Java Version Mismatch**

```
Error: Unsupported class file major version 65
Error: UnsupportedClassVersionError ... class file version 65.0 ... only recognizes class file versions up to 61.0
```

**Solution:**

This means Maven/Java is using an older version (e.g., Java 17) instead of Java 21.

**Windows Fix:**
```powershell
# Set JAVA_HOME and PATH to use JDK 21
$env:JAVA_HOME = "C:\Users\abhim\.jdk\jdk-21.0.8"  # Adjust to your JDK 21 path
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify Java version
java -version  # Should show version 21.x.x

# Clean rebuild
.\mvnw.cmd clean package -DskipTests

# Run with explicit Java 21 path
& "$env:JAVA_HOME\bin\java.exe" -jar target\makemytrip-0.0.1-SNAPSHOT.jar
```

**Linux/Mac Fix:**
```bash
# Set JAVA_HOME to JDK 21
export JAVA_HOME=/path/to/jdk-21
export PATH=$JAVA_HOME/bin:$PATH

# Verify and rebuild
java -version
./mvnw clean package -DskipTests
```

**Permanent Fix (Windows):**
- Add `JAVA_HOME` to System Environment Variables
- Set it to: `C:\Users\abhim\.jdk\jdk-21.0.8`
- Add `%JAVA_HOME%\bin` to PATH
- Restart your terminal/IDE

**4. Maven Build Fails**

```
Error: Could not resolve dependencies
```

**Solution:**
```bash
# Clear Maven cache
./mvnw dependency:purge-local-repository

# Rebuild
./mvnw clean install -U
```

### Frontend Issues

**1. PowerShell Script Execution Blocked**

```
Error: File ... npm.ps1 cannot be loaded because running scripts is disabled on this system
SecurityError: UnauthorizedAccess
```

**Solution:**
```powershell
# Allow script execution for current user
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force

# Verify it worked
Get-ExecutionPolicy -Scope CurrentUser  # Should show "RemoteSigned"

# Now npm commands will work
npm install
```

**2. Module Not Found Errors**

```
Error: Cannot find module 'next'
```

**Solution:**
```bash
# Delete node_modules and package-lock.json
rm -rf node_modules package-lock.json

# Reinstall dependencies
npm install
```

**3. Port 3000 Already in Use**

**Solution:**
```bash
# Windows
netstat -ano | findstr :3000
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :3000
kill -9 <PID>

# Or use different port
PORT=3001 npm run dev
```

**4. API Connection Issues**

**Solution:**
- Ensure backend is running on http://localhost:8080
- Check browser console for CORS errors
- Verify API endpoint URLs in frontend code

### Database Issues

**1. Clear MongoDB Data**

```bash
# Connect to MongoDB
mongosh

# Switch to database
use makemytrip

# Drop all collections
db.dropDatabase()
```

**2. View MongoDB Data**

```bash
# Connect to MongoDB
mongosh

# Switch to database
use makemytrip

# View collections
show collections

# View documents
db.flights.find()
db.hotels.find()
db.users.find()
```

## 🛠️ Development

### Startup Scripts (Optional)

**Create `start-backend.ps1` in project root:**
```powershell
# Set Java 21 environment
$env:JAVA_HOME = "C:\Users\abhim\.jdk\jdk-21.0.8"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Run the application
Write-Host "Starting MakeMyTrip Backend (Java 21)..." -ForegroundColor Green
& "$env:JAVA_HOME\bin\java.exe" -jar target\makemytrip-0.0.1-SNAPSHOT.jar
```

**Create `start-frontend.ps1` in makemytour folder:**
```powershell
Write-Host "Starting MakeMyTrip Frontend..." -ForegroundColor Green
npm run dev
```

**Usage:**
```powershell
# Terminal 1: Start backend
.\start-backend.ps1

# Terminal 2: Start frontend
cd makemytour
.\start-frontend.ps1
```

### Running Tests

**Backend Tests:**
```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=MakemytripApplicationTests
```

**Frontend Tests:**
```bash
cd makemytour
npm run test
```

### Building for Production

**Backend:**
```bash
# Build JAR file
./mvnw clean package -DskipTests

# JAR will be created at: target/makemytrip-0.0.1-SNAPSHOT.jar
```

**Frontend:**
```bash
cd makemytour
npm run build
npm start
```

### Code Formatting

**Backend:**
- Use IDE formatter (IntelliJ IDEA / Eclipse)
- Lombok will generate getters/setters automatically

**Frontend:**
```bash
cd makemytour
npm run lint
```

## 📝 Additional Notes

- **Security**: Security is currently disabled for development. Enable it for production by removing the exclude configuration.
- **CORS**: Configure CORS in `SecurityConfig.java` if frontend and backend are on different domains.
- **Environment Variables**: Use environment variables for sensitive data (MongoDB credentials, API keys).
- **Java 21 Features**: This project is upgraded to Java 21 LTS for improved performance and modern language features.

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is open source and available for educational purposes.

## 🆘 Need Help?

If you encounter any issues not covered in the troubleshooting section:

1. Check MongoDB connection and ensure database is running
2. Verify Java 21 is properly installed and configured
3. Ensure all dependencies are installed (`mvnw clean install` and `npm install`)
4. Check application logs for detailed error messages
5. Review upgrade documentation in `.github/java-upgrade/20260303070004/`

---

**Happy Coding! 🚀**
