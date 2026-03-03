# 🚀 How to Run MakeMyTrip Clone Locally - Complete Guide

This guide provides step-by-step instructions to run the MakeMyTrip clone application on your Windows machine.

## ✅ What You Need

- ✅ JDK 21 (installed at: `C:\Users\abhim\.jdk\jdk-21.0.8`)
- ✅ Node.js and npm
- ✅ MongoDB (Docker recommended)
- ✅ Git (optional)

---

## 📝 First Time Setup

### Step 1: Enable PowerShell Scripts

Open PowerShell as Administrator and run:

```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
```

This allows npm and other scripts to run.

### Step 2: Start MongoDB

**Recommended: Using Docker**
```powershell
docker run -d -p 27017:27017 --name mongodb mongo:latest
```

**Alternative: Local MongoDB**
```powershell
net start MongoDB
```

**Verify MongoDB is running:**
```powershell
Test-NetConnection localhost -Port 27017
```
Should return `TcpTestSucceeded: True`

### Step 3: Verify MongoDB Configuration

Check that `src/main/resources/application.properties` contains:
```properties
spring.data.mongodb.uri=mongodb://localhost:27017/makemytrip
```

✅ This is already configured correctly!

### Step 4: Install Frontend Dependencies

```powershell
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main\makemytour
npm install
```

Wait for installation to complete (206 packages).

---

## 🎯 Running the Application

### Method 1: Using Startup Scripts (Easiest)

**Terminal 1 - Start Backend:**
```powershell
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main
.\start-backend.ps1
```

**Terminal 2 - Start Frontend:**
```powershell
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main\makemytour
.\start-frontend.ps1
```

### Method 2: Manual Commands

**Terminal 1 - Backend:**
```powershell
# Navigate to project root
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main

# Set Java 21 environment
$env:JAVA_HOME = "C:\Users\abhim\.jdk\jdk-21.0.8"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Build (first time or after code changes)
.\mvnw.cmd clean package -DskipTests

# Run the application
& "C:\Users\abhim\.jdk\jdk-21.0.8\bin\java.exe" -jar target\makemytrip-0.0.1-SNAPSHOT.jar
```

Wait for this message:
```
Started MakemytripApplication in X.XXX seconds
```

**Terminal 2 - Frontend:**
```powershell
# Navigate to frontend folder
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main\makemytour

# Start dev server
npm run dev
```

Wait for:
```
✓ Ready in X ms
○ Local: http://localhost:3000
```

---

## 🌐 Access the Application

Open your browser and go to:

### **http://localhost:3000**

That's it! The application is now running.

---

## 🔍 Verify Everything is Working

Run this command to check all services:

```powershell
Write-Host "MongoDB (27017):" -NoNewline; (Test-NetConnection localhost -Port 27017 -WarningAction SilentlyContinue).TcpTestSucceeded
Write-Host "Backend (8080):" -NoNewline; (Test-NetConnection localhost -Port 8080 -WarningAction SilentlyContinue).TcpTestSucceeded
Write-Host "Frontend (3000):" -NoNewline; (Test-NetConnection localhost -Port 3000 -WarningAction SilentlyContinue).TcpTestSucceeded
```

All should return `True`.

---

## 🛑 Stopping the Application

Press `Ctrl+C` in each terminal window where the application is running.

To stop MongoDB Docker container:
```powershell
docker stop mongodb
```

---

## 🐛 Common Issues & Fixes

### Issue 1: "Unsupported class file version 65.0"

**Problem:** Java 17 is being used instead of Java 21

**Fix:**
```powershell
$env:JAVA_HOME = "C:\Users\abhim\.jdk\jdk-21.0.8"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version  # Should show 21.0.8
```

### Issue 2: "npm.ps1 cannot be loaded"

**Problem:** PowerShell script execution is disabled

**Fix:**
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
```

### Issue 3: "Connection refused to mongodb://localhost:27017"

**Problem:** MongoDB is not running

**Fix:**
```powershell
# If using Docker:
docker start mongodb

# If using local MongoDB:
net start MongoDB

# Check if it's running:
Test-NetConnection localhost -Port 27017
```

### Issue 4: Port 8080 or 3000 Already in Use

**Find and kill the process:**

```powershell
# For port 8080 (backend):
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# For port 3000 (frontend):
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

### Issue 5: Frontend Can't Connect to Backend

**Solution:**
1. Ensure backend is running on port 8080
2. Check browser console for errors
3. Verify MongoDB is running and connected

---

## 📊 Service URLs

| Service | URL | Status Check |
|---------|-----|--------------|
| Frontend | http://localhost:3000 | Main application UI |
| Backend API | http://localhost:8080 | Spring Boot REST API |
| MongoDB | localhost:27017 | Database |

---

## 🔄 Rebuilding After Code Changes

**Backend:**
```powershell
# Navigate to project root
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main

# Set Java 21
$env:JAVA_HOME = "C:\Users\abhim\.jdk\jdk-21.0.8"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Rebuild
.\mvnw.cmd clean package -DskipTests
```

**Frontend:**
Frontend changes are automatically detected and hot-reloaded by Next.js.

---

## 📝 Daily Workflow

1. **Start MongoDB**
   ```powershell
   docker start mongodb
   # or: net start MongoDB
   ```

2. **Start Backend** (in one terminal)
   ```powershell
   cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main
   .\start-backend.ps1
   ```

3. **Start Frontend** (in another terminal)
   ```powershell
   cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main\makemytour
   .\start-frontend.ps1
   ```

4. **Open Browser**
   - Go to: http://localhost:3000

5. **When Done**
   - Press `Ctrl+C` in both terminals
   - Optionally: `docker stop mongodb`

---

## 🎓 Additional Resources

- **README.md** - Comprehensive project documentation
- **Troubleshooting** - See README.md "Troubleshooting" section
- **API Documentation** - See README.md "API Endpoints" section
- **Upgrade Documentation** - `.github/java-upgrade/20260303070004/`

---

**Happy Coding! 🚀**

*Last updated: March 3, 2026*
