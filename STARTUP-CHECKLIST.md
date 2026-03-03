# ✅ MakeMyTrip Clone - Startup Checklist

## Before You Start (First Time Setup)

- [ ] JDK 21 installed at: `C:\Users\abhim\.jdk\jdk-21.0.8`
- [ ] Node.js and npm installed
- [ ] Docker Desktop installed (for MongoDB)
- [ ] PowerShell execution policy enabled:
  ```powershell
  Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
  ```
- [ ] Frontend dependencies installed:
  ```powershell
  cd makemytour
  npm install
  ```

---

## Every Time You Run (Quick Checklist)

### ✅ Step 1: Start MongoDB
```powershell
docker start mongodb
# or: docker run -d -p 27017:27017 --name mongodb mongo:latest (first time)
```
- [ ] MongoDB running on port 27017

### ✅ Step 2: Start Backend (Terminal 1)
```powershell
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main
.\start-backend.ps1
```
Wait for: `Started MakemytripApplication in X.XXX seconds`
- [ ] Backend running on port 8080

### ✅ Step 3: Start Frontend (Terminal 2)
```powershell
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main\makemytour
.\start-frontend.ps1
```
Wait for: `✓ Ready in X ms`
- [ ] Frontend running on port 3000

### ✅ Step 4: Access Application
Open browser: **http://localhost:3000**
- [ ] Application loads successfully

---

## Quick Verification

Run in PowerShell to check all services:
```powershell
Test-NetConnection localhost -Port 27017  # MongoDB
Test-NetConnection localhost -Port 8080   # Backend
Test-NetConnection localhost -Port 3000   # Frontend
```

All should return: `TcpTestSucceeded : True`

---

## Common Commands

### Check What's Running
```powershell
# MongoDB
Test-NetConnection localhost -Port 27017

# Backend
Test-NetConnection localhost -Port 8080

# Frontend
Test-NetConnection localhost -Port 3000
```

### Stop Services
- **Backend/Frontend**: Press `Ctrl+C` in respective terminals
- **MongoDB**: `docker stop mongodb`

### Restart Services
```powershell
# Restart MongoDB
docker restart mongodb

# Restart backend - press Ctrl+C, then:
.\start-backend.ps1

# Restart frontend - press Ctrl+C, then:
.\start-frontend.ps1
```

### Rebuild Backend (after code changes)
```powershell
$env:JAVA_HOME = "C:\Users\abhim\.jdk\jdk-21.0.8"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\mvnw.cmd clean package -DskipTests
```

### View Logs
- **Backend**: Check terminal where `start-backend.ps1` is running
- **Frontend**: Check terminal where `start-frontend.ps1` is running
- **Browser Console**: F12 → Console tab

---

## Troubleshooting Quick Fixes

### Error: "Unsupported class file version 65.0"
```powershell
$env:JAVA_HOME = "C:\Users\abhim\.jdk\jdk-21.0.8"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
```

### Error: "npm.ps1 cannot be loaded"
```powershell
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser -Force
```

### Error: "Connection refused to mongodb"
```powershell
docker start mongodb
# or: net start MongoDB
```

### Port Already in Use
```powershell
# Find process using port 8080:
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Find process using port 3000:
netstat -ano | findstr :3000
taskkill /PID <PID> /F
```

---

## URL Reference

| Service | URL | Description |
|---------|-----|-------------|
| **Frontend** | http://localhost:3000 | Main application |
| **Backend API** | http://localhost:8080 | REST API endpoints |
| **MongoDB** | localhost:27017 | Database |

---

## File Reference

| File | Purpose |
|------|---------|
| `HOW-TO-RUN.md` | Complete setup guide |
| `README.md` | Full documentation |
| `start-backend.ps1` | Backend startup script |
| `makemytour/start-frontend.ps1` | Frontend startup script |
| `src/main/resources/application.properties` | Backend configuration |

---

**Print this checklist for quick reference!**

*Last updated: March 3, 2026*
