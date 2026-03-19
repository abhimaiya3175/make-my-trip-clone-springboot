# MakeMyTrip Clone - Full Stack Application

A full-stack travel booking application built with Spring Boot (Java 17) backend and Next.js frontend, featuring flight and hotel booking capabilities with advanced features.

## Implemented Features

- **Review & Rating System** — Star ratings, review CRUD, helpful votes, flagging, owner replies, URL-based photo attachments, moderation workflow
- **Live Flight Status** — Mock status provider with simulated delays, boarding, cancellation; auto-refresh polling (30s); in-app status change alert; timeline history
- **Seat / Room Selection** — Interactive seat map with FIRST/BUSINESS/ECONOMY classes; hotel room grid grouped by type; lock/select/confirm flow; premium surcharges; user preference persistence
- **Dynamic Pricing Engine** — Rule-based multipliers (weekend, holiday, last-minute, high demand, early bird); hourly price snapshots; price history charts via Recharts; 24-hour price freeze with countdown
- **AI-Powered Recommendations** — Basic collaborative filtering; user event tracking (VIEW/SEARCH/BOOK); feedback loop (LIKE/SAVE/NOT_INTERESTED); personalized explanations; mock data bootstrap
- **Security** — Spring Security filter chain, CORS policy, stateless sessions, BCrypt password encoding
- **Testing** — 42 backend tests passing (Mockito + JUnit 5)

## Feature Completion Snapshot (Updated: 15/03/2026)

- Cancellation & Refunds: done (cancel from dashboard, auto-refund policy, partial cancellation/refund, reason dropdown, refund tracker)
- Review & Rating System: mostly done
   remaining: true file upload pipeline for photos is not implemented (current flow uses image URLs)
- Live Flight Status (Mock API): mostly done
   remaining: browser/system push notifications are not implemented; estimated arrival updates are not implemented (estimated departure is shown)
- Seat/Room Selection: mostly done
   remaining: 3D room preview is not implemented
- Dynamic Pricing Engine: done
- AI Recommendations: done

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

Ensure `backend/src/main/resources/application.properties` has:
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
cd e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main\frontend

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

**Frontend (in frontend folder):**
```powershell
cd frontend
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
├── backend/                      # Spring Boot backend (Java 21)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/makemytrip/
│   │   │   │   ├── MakemytripApplication.java    # Main Spring Boot app
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java       # BCrypt password encoder
│   │   │   │   │   └── WebConfig.java            # CORS configuration
│   │   │   │   └── modules/                      # Feature-based modules
│   │   │   │       ├── auth/                     # Authentication & users
│   │   │   │       │   ├── controller/AuthController.java
│   │   │   │       │   ├── service/AuthService.java
│   │   │   │       │   ├── repository/UserRepository.java
│   │   │   │       │   ├── model/User.java
│   │   │   │       │   └── dto/                  # Login/Signup DTOs
│   │   │   │       ├── flights/                  # Flight management
│   │   │   │       │   ├── controller/FlightController.java
│   │   │   │       │   ├── service/FlightService.java
│   │   │   │       │   ├── repository/FlightRepository.java
│   │   │   │       │   └── model/Flight.java
│   │   │   │       ├── hotels/                   # Hotel management
│   │   │   │       ├── booking/                  # Booking management
│   │   │   │       ├── cancellation/             # Cancellation & refunds
│   │   │   │       ├── reviews/                  # Review system
│   │   │   │       ├── flightstatus/             # Flight tracking
│   │   │   │       ├── seatroom/                 # Seat/room selection
│   │   │   │       ├── pricing/                  # Dynamic pricing
│   │   │   │       └── recommendation/           # Recommendations
│   │   │   └── resources/
│   │   │       ├── application.properties        # Backend config
│   │   │       └── data.sql                      # Sample data (optional)
│   │   └── test/                 # Test files
│   └── target/                   # Build output (gitignored)
├── frontend/                     # Next.js frontend (React 19)
│   ├── src/
│   │   ├── components/          # React components (feature-based)
│   │   │   ├── auth/            # Login, Signup forms
│   │   │   ├── flights/         # Flight components
│   │   │   ├── hotels/          # Hotel components
│   │   │   ├── booking/         # Booking components
│   │   │   ├── cancellation/    # Refund components
│   │   │   ├── reviews/         # Review components
│   │   │   └── ui/              # Shadcn/UI components
│   │   ├── pages/               # Next.js pages (routing)
│   │   │   ├── auth/            # Login/Signup pages
│   │   │   ├── flights/         # Flight search & status
│   │   │   ├── hotels/          # Hotel search
│   │   │   ├── booking/         # Booking confirmation
│   │   │   └── profile/         # User profile
│   │   ├── services/            # API service layer
│   │   │   ├── authService.js   # Authentication APIs
│   │   │   ├── flightService.js # Flight APIs
│   │   │   ├── hotelService.js  # Hotel APIs
│   │   │   └── bookingService.js # Booking APIs
│   │   ├── hooks/               # Custom React hooks
│   │   ├── store/               # Redux store
│   │   └── styles/              # CSS styles
│   ├── public/                  # Static files
│   ├── package.json             # Frontend dependencies
│   └── start-frontend.ps1       # Frontend startup script
├── .mvn/                        # Maven wrapper
├── pom.xml                      # Maven configuration
├── mvnw / mvnw.cmd              # Maven wrapper scripts
├── Dockerfile                   # Docker build (Java 21)
├── start-backend.ps1            # Backend startup script
├── .env.example                 # Environment variables template
└── README.md                    # This file
```

## 🏃 Getting Started

### Backend Setup (Spring Boot)

#### Step 1: Configure MongoDB Connection

Edit `backend/src/main/resources/application.properties`:

```properties
spring.application.name=makemytrip
server.port=8080

# MongoDB Configuration - UPDATE THIS!
spring.data.mongodb.uri=mongodb://localhost:27017/makemytrip
spring.data.mongodb.database=makemytrip

# CORS Configuration
spring.web.cors.allowed-origins=http://localhost:3000

# Logging (optional)
logging.level.com.makemytrip=INFO
```

**MongoDB Connection String Examples:**

> ⚠️ **SECURITY WARNING**: Never commit credentials or connection strings with passwords to Git!

Use environment variables instead:

- **Local MongoDB**: `mongodb://localhost:27017/makemytrip`
- **Docker MongoDB**: `mongodb://localhost:27017/makemytrip`
- **MongoDB Atlas (Cloud)**: Use environment variables - see `.env.example`

**Using Environment Variables (Recommended):**

1. Create `.env` file in project root (see `.env.example`):
```properties
MONGODB_URI=mongodb://localhost:27017/makemytrip
```

2. Reference in `backend/src/main/resources/application.properties`:
```properties
spring.data.mongodb.uri=${MONGODB_URI}
```

2. Set environment variable before running:
```powershell
$env:MONGODB_URI = "mongodb+srv://username:password@cluster.mongodb.net/makemytrip"
```

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
cd frontend
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

Edit `backend/src/main/resources/application.properties`:

```properties
# Application Name
spring.application.name=makemytrip

# Server Port
server.port=8080

# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/makemytrip
spring.data.mongodb.database=makemytrip

# CORS Configuration
spring.web.cors.allowed-origins=http://localhost:3000

# JWT Configuration (recommended: set via env var)
app.jwt.secret=${JWT_SECRET:change-this-to-a-long-random-secret}
app.jwt.expiration-ms=86400000

# Web Push (VAPID)
vapid.public.key=${VAPID_PUBLIC_KEY:replace-with-generated-vapid-public-key}
vapid.private.key=${VAPID_PRIVATE_KEY:replace-with-generated-vapid-private-key}
vapid.subject=${VAPID_SUBJECT:mailto:admin@example.com}

# Logging (optional)
logging.level.com.makemytrip=INFO
logging.level.org.springframework.data.mongodb=DEBUG

# Uploads Directory (Local storage for review photos)
app.upload.dir=${UPLOAD_DIR:./uploads/reviews/}
```

Set JWT secret from environment before starting backend:

```powershell
$env:JWT_SECRET="replace-with-a-strong-secret-at-least-32-characters"
```

```bash
export JWT_SECRET="replace-with-a-strong-secret-at-least-32-characters"
```

Generate VAPID keys (choose one):

```bash
npx web-push generate-vapid-keys
```

```bash
web-push generate-vapid-keys
```

Then set these environment variables before starting backend:

```powershell
$env:VAPID_PUBLIC_KEY="your-generated-public-key"
$env:VAPID_PRIVATE_KEY="your-generated-private-key"
$env:VAPID_SUBJECT="mailto:admin@example.com"
```

### Frontend Configuration

The frontend connects to the backend API at `http://localhost:8080`. If you change the backend port, update the API base URL in `frontend/src/utils/api.js`:

```javascript
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080", // Update this if backend port changes
});

export default api;
```

## 🔌 API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/user/signup` | Register new user |
| POST | `/user/login` | User login |
| GET | `/user/email` | Get user by email |
| POST | `/user/edit` | Edit user profile |

### Flight Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/flight` | Get all flights |
| POST | `/admin/flight` | Add new flight (admin) |
| PUT | `/admin/flight/:id` | Update flight (admin) |

### Hotel Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/hotel` | Get all hotels |
| POST | `/admin/hotel` | Add new hotel (admin) |
| PUT | `/admin/hotel/:id` | Update hotel (admin) |

### Booking Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/booking/flight` | Book a flight |
| POST | `/api/booking/hotel` | Book a hotel |
| GET | `/api/booking/user/:userId` | Get user bookings |

### Cancellation & Refund Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/cancellation/request` | Request booking cancellation |
| GET | `/api/cancellation/user/:userId` | Get user cancellations |
| GET | `/api/cancellation/refund/:bookingId` | Get refund tracker |

### Review & Rating Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/reviews` | Create a review |
| GET | `/api/reviews?entityType=&entityId=` | List reviews (sorted) |
| PUT | `/api/reviews/:id` | Update a review |
| DELETE | `/api/reviews/:id` | Delete a review |
| POST | `/api/reviews/:id/helpful` | Vote helpful |
| POST | `/api/reviews/:id/flag` | Flag inappropriate |
| POST | `/api/reviews/:id/reply` | Reply to review |

### Live Flight Status Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/flight-status/:flightId` | Get current flight status |
| GET | `/api/flight-status/:flightId/timeline` | Get status timeline history |
| GET | `/api/flight-status` | List all flight statuses (paginated) |
| GET | `/api/flight-status/stream/:flightId` | SSE stream for live updates |

### Seat / Room Selection Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/seatroom/seats/flight/:flightId` | Get all seats for a flight |
| GET | `/api/seatroom/seats/flight/:flightId/available` | Get available seats |
| POST | `/api/seatroom/seats/:seatId/lock` | Lock a seat (10-min hold) |
| POST | `/api/seatroom/seats/:seatId/release` | Release seat lock |
| POST | `/api/seatroom/seats/:seatId/confirm` | Confirm seat booking |
| GET | `/api/seatroom/rooms/hotel/:hotelId` | Get all rooms for a hotel |
| GET | `/api/seatroom/rooms/hotel/:hotelId/available` | Get available rooms |
| POST | `/api/seatroom/rooms/:roomId/lock` | Lock a room (10-min hold) |
| POST | `/api/seatroom/rooms/:roomId/release` | Release room lock |
| POST | `/api/seatroom/rooms/:roomId/confirm` | Confirm room booking |
| GET | `/api/seatroom/preferences/:userId` | Get user seat/room preferences |
| PUT | `/api/seatroom/preferences` | Save user preferences |

### Dynamic Pricing Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/pricing/:entityType/:entityId?userId=` | Get current dynamic price |
| GET | `/api/pricing/:entityType/:entityId/history?days=7` | Get price history |
| POST | `/api/pricing/freeze` | Freeze current price (24h) |
| GET | `/api/pricing/freeze/user/:userId` | Get user's active freezes |

### AI Recommendation Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/recommendations/user/:userId?itemType=` | Get personalized recommendations |
| GET | `/api/recommendations/similar/:itemType/:itemId` | Get similar items |
| POST | `/api/recommendations/events` | Record user event (VIEW/SEARCH/BOOK) |
| POST | `/api/recommendations/feedback` | Submit feedback (LIKE/SAVE/NOT_INTERESTED) |
| GET | `/api/recommendations/:itemId/explain?userId=` | Get recommendation explanation |

### Admin Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/users` | Get all users |

## 🎬 Feature Demo Steps

> Ensure both backend (`:8080`) and frontend (`:3000`) are running before trying these demos.

### 1. Review & Rating System

1. Sign up / log in at `http://localhost:3000`.
2. Book a flight or hotel so you have a booking to review.
3. Navigate to **http://localhost:3000/reviews/review**.
4. Select the entity type (FLIGHT or HOTEL) and entity ID.
5. Choose a star rating (1-5), write a review, optionally add photo URLs.
6. Click **Submit** — the review appears in the review list.
7. Other users can vote "Helpful", flag inappropriate reviews, or reply.

### 2. Live Flight Status Tracker

1. Go to **http://localhost:3000/flight-status**.
2. Enter a flight number (e.g. `AI101`) in the search box and click **Track Flight**.
3. The status card displays: airline, route, scheduled/estimated departure, and a coloured badge (ON_TIME / DELAYED / BOARDING / LANDED / CANCELLED).
4. Status auto-refreshes every 45 seconds — if the status changes a toast alert appears.
5. Click **View Timeline** to see the full status history.

### 3. Seat / Room Selection

1. From the home page search for flights or hotels.
2. Click **Book** on a flight → the booking page loads with an interactive **seat map**.
   - Green = available, Red = taken, Orange = locked by another user.
   - Click a seat to lock it (held for 10 minutes).
   - Click **Confirm** to finalize.
3. Click **Book** on a hotel → the booking page loads with a **room grid** grouped by type (Deluxe / Suite / Standard).
   - Room locking and confirmation work the same way.
4. Your seat/room preferences are remembered for future bookings.

### 4. Dynamic Pricing & Price Freeze

1. On any flight/hotel booking page a **price badge** shows the current dynamic price with multiplier factors (weekend, high demand, etc.).
2. Click the **Price History** tab to see a Recharts line chart of price changes over the last 7 days.
3. Click **Freeze Price** to lock the current price for 24 hours — a countdown timer appears.
4. The frozen price persists on page refresh; when the timer expires the badge reverts to the live price.

### 5. AI-Powered Recommendations

1. Log in and browse / search / book a few flights and hotels to generate activity events.
2. Navigate to **http://localhost:3000/recommendations/suggestions**.
3. Personalized recommendations appear as cards with scores, explanations, and prices.
4. Click **Like** or **Save** to boost similar suggestions, or **Not Interested** to suppress them.
5. Click the **Why?** tooltip to see a plain-language explanation of why the item was recommended.

---

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

### Running Tests

```powershell
# Run all backend unit tests (41 tests)
.\mvnw.cmd test

# Run specific test classes
.\mvnw.cmd test -Dtest="FlightStatusServiceTest,SeatRoomServiceTest,PricingServiceTest,RecommendationServiceTest"
```

Tests cover:
- **FlightStatusServiceTest** (5 tests) — status lookup, not-found handling, delay info
- **SeatRoomServiceTest** (15 tests) — lock/release/confirm for seats and rooms, conflict detection, optimistic locking, preferences
- **PricingServiceTest** (11 tests) — base price, demand multipliers, entity type filtering, freeze lifecycle, history
- **RecommendationServiceTest** (10 tests) — direct recommendations, collaborative filtering, feedback upsert, explanations

**Frontend Unit Tests (27 tests):**
```powershell
cd frontend
npm test              # Run all Jest tests
npm run test:watch    # Watch mode
npm run test:coverage # With coverage report
```

Frontend tests cover: ErrorBoundary, FlightStatusTracker, SeatMap, PriceFreezeButton, RecommendationsSection.

**Cypress E2E Tests (5 specs):**
```powershell
cd frontend
npx cypress install   # First-time binary download
npm run cypress:open   # Interactive runner
npm run cypress:run    # Headless CI runner
```

E2E specs: reviews, flight-status, seat-selection, price-freeze, recommendations.

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

**Create `start-frontend.ps1` in frontend folder:**
```powershell
Write-Host "Starting MakeMyTrip Frontend..." -ForegroundColor Green
npm run dev
```

**Usage:**
```powershell
# Terminal 1: Start backend
.\start-backend.ps1

# Terminal 2: Start frontend
cd frontend
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
cd frontend
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
cd frontend
npm run build
npm start
```

### Code Formatting

**Backend:**
- Use IDE formatter (IntelliJ IDEA / Eclipse)
- Lombok will generate getters/setters automatically

**Frontend:**
```bash
cd frontend
npm run lint
```

## 📝 Additional Notes

- **Architecture**: Project uses feature-based modular architecture for better code organization and maintainability.
- **Package Structure**: Backend package is `com.makemytrip.modules.*` with separate modules for auth, flights, hotels, booking, cancellation, etc.
- **Security**: BCrypt password encoding configured in `SecurityConfig.java`. CORS configured in `WebConfig.java`.
- **Environment Variables**: Use `.env` file for sensitive data (MongoDB credentials, API keys). See `.env.example`.
- **Java 21**: This project uses Java 21 LTS for improved performance and modern language features.
- **Services Layer**: Frontend uses a services layer (`frontend/src/services/`) for clean API separation.

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
