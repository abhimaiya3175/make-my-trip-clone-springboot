# MakeMyTrip Frontend Startup Script
# This script starts the Next.js development server

Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host "  MakeMyTrip Frontend - Starting..." -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Check if node_modules exists
if (-not (Test-Path "node_modules")) {
    Write-Host "Dependencies not installed. Running npm install..." -ForegroundColor Yellow
    npm install
}

# Check if backend is running
Write-Host "`nChecking backend connection..." -ForegroundColor Yellow
$backendRunning = (Test-NetConnection localhost -Port 8080 -WarningAction SilentlyContinue).TcpTestSucceeded
if (-not $backendRunning) {
    Write-Host "WARNING: Backend is not running on port 8080!" -ForegroundColor Red
    Write-Host "Start the backend first in another terminal window." -ForegroundColor Yellow
    Write-Host "`nContinuing frontend startup anyway...`n" -ForegroundColor Yellow
    Start-Sleep -Seconds 2
}

# Start the development server
Write-Host "Starting Next.js development server..." -ForegroundColor Green
Write-Host "Frontend will be available at: http://localhost:3000`n" -ForegroundColor Green
npm run dev
