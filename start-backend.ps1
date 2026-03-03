# MakeMyTrip Backend Startup Script
# This script sets up Java 21 environment and starts the Spring Boot application

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "  MakeMyTrip Backend - Starting..." -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Set Java 21 environment
$env:JAVA_HOME = "C:\Users\abhim\.jdk\jdk-21.0.8"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# Verify Java version
Write-Host "Java Version:" -ForegroundColor Yellow
& "$env:JAVA_HOME\bin\java.exe" -version

# Check if JAR exists
if (-not (Test-Path "target\makemytrip-0.0.1-SNAPSHOT.jar")) {
    Write-Host "`nJAR file not found. Building project..." -ForegroundColor Yellow
    .\mvnw.cmd clean package -DskipTests
}

# Check MongoDB connection
Write-Host "`nChecking MongoDB connection..." -ForegroundColor Yellow
$mongoRunning = (Test-NetConnection localhost -Port 27017 -WarningAction SilentlyContinue).TcpTestSucceeded
if (-not $mongoRunning) {
    Write-Host "WARNING: MongoDB is not running on port 27017!" -ForegroundColor Red
    Write-Host "Start MongoDB first: docker run -d -p 27017:27017 --name mongodb mongo:latest" -ForegroundColor Yellow
    Write-Host "`nDo you want to continue anyway? (Press Ctrl+C to cancel)" -ForegroundColor Yellow
    Start-Sleep -Seconds 3
}

# Start the application
Write-Host "`nStarting Spring Boot application..." -ForegroundColor Green
Write-Host "Backend will be available at: http://localhost:8080`n" -ForegroundColor Green
& "$env:JAVA_HOME\bin\java.exe" -jar target\makemytrip-0.0.1-SNAPSHOT.jar
