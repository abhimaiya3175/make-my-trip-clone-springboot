# Environment Variable Setup Guide

## Overview

This guide explains how to configure your development environment using environment variables instead of hardcoding credentials in your source code.

## Files Created

- **`.env.example`** - Template for backend (Spring Boot) environment variables
- **`makemytour/.env.example`** - Template for frontend (Next.js) environment variables  
- **`SECURITY.md`** - Comprehensive security best practices guide

## Quick Setup

### Option 1: Using .env Files (Recommended)

#### Backend (Spring Boot)

1. **Create `.env` file** in the project root:
   ```bash
   cp .env.example .env
   ```

2. **Edit `.env`** with your actual values:
   ```properties
   MONGODB_URI=mongodb://localhost:27017/makemytrip
   SERVER_PORT=8080
   CORS_ALLOWED_ORIGINS=http://localhost:3000
   ```

3. **Load and run**:
   ```bash
   # For Linux/macOS
   export $(cat .env | xargs) && mvn spring-boot:run
   
   # For Windows PowerShell
   Get-Content .env | foreach { 
       $name, $value = $_.split('='); 
       [Environment]::SetEnvironmentVariable($name, $value) 
   }
   mvn spring-boot:run
   ```

#### Frontend (Next.js)

1. **Create `makemytour/.env.local`** file:
   ```bash
   cp makemytour/.env.example makemytour/.env.local
   ```

2. **Edit `makemytour/.env.local`**:
   ```bash
   NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
   NEXT_PUBLIC_API_BASE_PATH=/api
   ```

3. **Run** (Next.js automatically reads .env.local):
   ```bash
   cd makemytour
   npm run dev
   ```

### Option 2: Using IDE Run Configurations (IntelliJ IDEA)

1. **Open Run → Edit Configurations**
2. **Select your Spring Boot run configuration**
3. **Add Environment variables** in the "Environment variables" field:
   ```
   MONGODB_URI=mongodb://localhost:27017/makemytrip;SERVER_PORT=8080;CORS_ALLOWED_ORIGINS=http://localhost:3000
   ```
4. **Click OK and run** - The application will load these variables automatically

### Option 3: System Environment Variables

#### Windows PowerShell

```powershell
# Set variables
$env:MONGODB_URI = "mongodb://localhost:27017/makemytrip"
$env:SERVER_PORT = "8080"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:3000"

# Verify
Get-ChildItem env:MONGODB_URI, env:SERVER_PORT, env:CORS_ALLOWED_ORIGINS

# Run Spring Boot
mvn spring-boot:run
```

#### Linux/macOS

```bash
# Set variables
export MONGODB_URI="mongodb://localhost:27017/makemytrip"
export SERVER_PORT="8080"
export CORS_ALLOWED_ORIGINS="http://localhost:3000"

# Verify
echo $MONGODB_URI $SERVER_PORT $CORS_ALLOWED_ORIGINS

# Run Spring Boot
mvn spring-boot:run
```

## Configuration Reference

### Backend (Spring Boot)

The following properties are configurable via environment variables in `src/main/resources/application.properties`:

| Environment Variable | Property Name | Default Value | Description |
|---|---|---|---|
| `MONGODB_URI` | `spring.data.mongodb.uri` | `mongodb://localhost:27017/makemytrip` | MongoDB connection string |
| `SPRING_APPLICATION_NAME` | `spring.application.name` | `makemytrip` | Application name |
| `SERVER_PORT` | `server.port` | `8080` | Server port |
| `CORS_ALLOWED_ORIGINS` | (SecurityConfig) | `*` | CORS allowed origins (update for production) |
| `LOGGING_LEVEL_COM_MAKEMYTRIP` | `logging.level.com.makemytrip` | `INFO` | Logging level for app code |

### Frontend (Next.js)

The following environment variables are used in the frontend:

| Variable | Location | Example Value | Description |
|---|---|---|---|
| `NEXT_PUBLIC_BACKEND_URL` | `makemytour/.env.local` | `http://localhost:8080` | Backend API base URL |
| `NEXT_PUBLIC_API_BASE_PATH` | `makemytour/.env.local` | `/api` | API base path (if needed) |
| `NEXT_PUBLIC_APP_NAME` | `makemytour/.env.local` | `Make My Trip Clone` | Application display name |

## MongoDB Atlas Configuration

### Generate Connection String

1. Go to [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create/Select your cluster
3. Click "Connect" → "Drivers" → "Node.js"
4. Copy the connection string, which looks like:
   ```
   mongodb+srv://username:password@cluster-name.mongodb.net/dbname
   ```

### Update Environment Variable

```bash
# Update your .env file
MONGODB_URI=mongodb+srv://your-username:your-secure-password@cluster0.xxxxx.mongodb.net/makemytrip
```

⚠️ **Security Note**: 
- Replace `your-username` with your actual MongoDB username
- Replace `your-secure-password` with your actual password
- Never commit `.env` to Git (it's in `.gitignore`)
- Use strong passwords (12+ characters with mixed case, numbers, symbols)

## Security Checklist

Before deploying to production:

- [ ] All `.env` files are in `.gitignore`
- [ ] Credentials are rotated/changed from development credentials
- [ ] Database passwords are strong (12+ characters)
- [ ] IP whitelist configured on MongoDB Atlas
- [ ] CORS updated to specific domains (not `*`)
- [ ] HTTPS enabled on all production URLs
- [ ] Environment variables set on deployment platform
- [ ] No `.env` files committed to Git

## Troubleshooting

### Variables Not Loading

**Issue**: Environment variables not recognized by Spring Boot

**Solutions**:
1. Restart your IDE/terminal after setting variables
2. Use `mvn clean package` to rebuild
3. Verify variable exists: `echo $MONGODB_URI` (Linux/macOS) or `Get-ChildItem env:MONGODB_URI` (Windows)
4. Check `application.properties` syntax: should be `${VARIABLE_NAME:defaultValue}`

### MongoDB Connection Failed

**Issue**: Connection string not working

**Check**:
1. Verify MongoDB is running: `Test-NetConnection localhost -Port 27017` (Windows)
2. Verify credentials are correct in `.env`
3. For Atlas: Ensure IP address is whitelisted
4. Check connection string format in `.env`

### Frontend Cannot Reach Backend

**Issue**: Frontend shows 404 or CORS errors

**Check**:
1. Verify `NEXT_PUBLIC_BACKEND_URL` in `makemytour/.env.local`
2. Ensure backend is running on the configured port
3. Check browser console for actual error messages
4. Verify CORS is enabled on backend controllers

## Additional Resources

- [Spring Boot External Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
- [Next.js Environment Variables](https://nextjs.org/docs/basic-features/environment-variables)
- [MongoDB Atlas Documentation](https://docs.atlas.mongodb.com/)
- [See SECURITY.md for comprehensive security guidelines](./SECURITY.md)

## Support

For issues or questions:

1. Check the [Troubleshooting](#troubleshooting) section above
2. Review [SECURITY.md](./SECURITY.md) for security-related issues
3. Check application logs for detailed error messages
4. See [README.md](./README.md) for general setup and running instructions
