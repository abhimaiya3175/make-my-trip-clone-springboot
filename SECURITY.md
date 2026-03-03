# Security Guidelines

## Overview

This document outlines the security practices and configurations for the Make My Trip Clone application.

## Credential Management

### ⚠️ CRITICAL: Never Commit Credentials

**Never** commit the following to version control:
- Database connection strings with passwords
- API keys or tokens
- Private keys or certificates
- AWS/Azure/GCP credentials
- JWT secrets
- Any other sensitive authentication data

### Environment Variables (Recommended)

Use environment variables to manage sensitive configuration:

```bash
# For macOS/Linux
export MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/makemytrip"
export SERVER_PORT=8080

# For Windows PowerShell
$env:MONGODB_URI="mongodb+srv://username:password@cluster.mongodb.net/makemytrip"
$env:SERVER_PORT=8080
```

### Local Development Setup

1. **Copy the template file:**
   ```bash
   cp .env.example .env
   cp makemytour/.env.example makemytour/.env.local
   ```

2. **Edit the `.env` file with your actual credentials:**
   ```ini
   MONGODB_URI=mongodb+srv://your-username:your-password@your-cluster.mongodb.net/makemytrip
   SERVER_PORT=8080
   CORS_ALLOWED_ORIGINS=http://localhost:3000
   ```

3. **Edit the `makemytour/.env.local` file:**
   ```bash
   NEXT_PUBLIC_BACKEND_URL=http://localhost:8080
   NEXT_PUBLIC_API_BASE_PATH=/api
   ```

4. **Load environment variables:**

   **For Backend (Spring Boot):**
   ```bash
   # Option 1: Set and run in same command
   export $(cat .env | xargs) && mvn spring-boot:run
   
   # Option 2: Use IDE run configuration to pass env vars
   # (IntelliJ IDEA: Run → Edit Configurations → Environment variables)
   ```

   **For Frontend (Next.js):**
   ```bash
   cd makemytour
   npm run dev
   # Next.js will automatically load .env.local variables
   ```

### Gitignore Configuration

The `.gitignore` file is configured to prevent committing sensitive files:

```
.env                      # Local environment variables
.env.local               # Local overrides
.env.*.local             # Environment-specific overrides
*.key                    # Private keys
*.pem                    # PEM certificates
application-secrets.properties  # Spring secrets file
```

## Database Security

### MongoDB Atlas Configuration

1. **Use Strong Passwords:**
   - Minimum 8 characters
   - Mix of uppercase, lowercase, numbers, and special characters
   - Never reuse passwords from other services

2. **IP Whitelist:**
   - In MongoDB Atlas, configure IP whitelist to restrict access
   - For local development: `0.0.0.0/0` (development only)
   - For production: Whitelist only your application servers

3. **Enable Authentication:**
   - Always use username/password authentication
   - Consider X.509 certificates for production

4. **Rotate Credentials Regularly:**
   - Change database passwords every 90 days
   - Rotate API keys when staff changes

### Connection String Format

**Local Development (No Authentication):**
```
mongodb://localhost:27017/makemytrip
```

**MongoDB Atlas (Recommended):**
```
mongodb+srv://username:password@cluster-name.mongodb.net/makemytrip?retryWrites=true&w=majority
```

## API Security

### CORS Configuration

Currently configured to allow all origins for development:
```java
@CrossOrigin(origins = "*")
```

**⚠️ For Production:**
Update to specific allowed origins:
```java
@CrossOrigin(origins = "https://yourdomain.com")
```

### Authentication

Recommended for production deployment:
- Implement JWT-based authentication
- Add role-based access control (RBAC)
- Use HTTPS for all endpoints
- Implement rate limiting
- Add request validation

See `src/main/java/com/makemytrip/makemytrip/config/SecurityConfig.java` for current configuration.

## GitHub Security

### Scanning for Secrets

GitHub automatically scans for exposed secrets. If detected:

1. **Immediately rotate/revoke the credential**
2. **Remove from git history:**
   ```bash
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch src/main/resources/application-secrets.properties" \
     --prune-empty --tag-name-filter cat -- --all
   
   git push origin --force --all
   ```

3. **Verify no secrets remain:**
   ```bash
   git log -p | grep -i "password\|secret\|api_key"
   ```

## Deployment Checklist

Before deploying to production:

- [ ] All credentials moved to environment variables
- [ ] `.gitignore` properly configured and committed
- [ ] No hardcoded secrets in code
- [ ] CORS whitelist updated to production domains
- [ ] HTTPS enabled on all endpoints
- [ ] Database credentials rotated
- [ ] Environment variables configured on deployment platform
- [ ] Security headers added to responses
- [ ] Rate limiting configured
- [ ] Input validation implemented
- [ ] Audit logging enabled
- [ ] Monitoring and alerting configured

## Reporting Security Issues

If you discover a security vulnerability:

1. **Do NOT** open a public GitHub issue
2. **Contact** the maintainers privately
3. **Provide** detailed reproduction steps
4. **Allow** 30 days for a fix before public disclosure

## References

- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
- [MongoDB Security Best Practices](https://docs.mongodb.com/manual/security/)
- [Spring Boot Security](https://spring.io/projects/spring-security)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [GitHub Secret Scanning](https://docs.github.com/en/code-security)

## Update History

- **2024-03-XX**: Added environment variable guidance, created .env.example templates, updated .gitignore
