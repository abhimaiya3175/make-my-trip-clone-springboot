# Quick Fix for "404 - getUserCancellations" Error

## The Problem
When you click on a booking's "Cancel Booking" button or try to view cancellations, you get:
```
AxiosError: Request failed with status code 404
```

## The Root Cause
The `userId` is **undefined** or **null**, so the API calls the wrong URL:
```
❌ http://localhost:8080/api/cancellation/user/undefined/cancellations
✅ http://localhost:8080/api/cancellation/user/[actual-user-id]/cancellations
```

## The Solution (In Order)

### 1. **RESTART EVERYTHING** (Most Important!)
```powershell
# Stop backend: Press Ctrl+C in the Java/backend terminal
# Stop frontend: Press Ctrl+C in the Node/frontend terminal

# Wait 5 seconds

# Start backend
cd "e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main"
.\start-backend.ps1

# In a NEW terminal, start frontend
cd "e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main\makemytour"
.\start-frontend.ps1
```

Wait for both to fully start. You should see **no red errors** in either console.

---

### 2. **Clear Browser Storage**
```javascript
// Open browser console (F12)
// Find "Console" tab
// Paste this and press Enter:

localStorage.clear();
location.reload();
```

This clears cached login data that might have the old format.

---

### 3. **Log In Again**
1. Go to [http://localhost:3000](http://localhost:3000)
2. Sign up or log in
3. **DO NOT CLOSE THE BROWSER CONSOLE** (F12) - keep it open!

---

### 4. **Go to Profile & Check Console**

Open the **Console tab (F12)** and look for these logs **immediately after login**:

#### ✅ **If You See This - YOU'RE FIXED:**
```
[Profile] user.id: 507f1f77bcf86cd799439011  user._id: 507f1f77bcf86cd799439011
[API] getUserCancellations request {userId: "507f1f77bcf86cd799439011", ...}
[API] getUserCancellations SUCCESS. Response: [...]
```

**→ You're done! Cancellations should work now.**

---

#### ❌ **If You See This - Still Broken:**
```
[Profile] user.id: undefined  user._id: 507f1f77bcf86cd799439011
```

**→ The server is STILL sending `_id` instead of `id`**

**FIX:** 
1. Did you restart the backend? (Check that you see "Started Application" in the terminal)
2. Did the backend actually recompile? (Look for "BUILD SUCCESS" in the terminal output)
3. Try `.\mvnw.cmd clean compile` to force a fresh build

---

#### ❌ **If You See This - Login Broken:**
```
[Profile] user is null/undefined
[Profile] ERROR: Neither user.id nor user._id exists!
```

**→ Login is broken, not the cancellation code**

**FIX:**
1. Check if /user/login endpoint is working
2. Check Spring Boot password encoding
3. Review UserController logs

---

## What to Share If Still Broken

If it's still not working after restarting:

1. **Browser Console Output** (copy-paste from F12 > Console):
   - Any `[Profile]` logs
   - Any `[API]` logs
   - Any red error messages

2. **Spring Boot Console Output**:
   - Any `[GET_USER_CANCELLATIONS]` logs
   - Any red error messages

3. **Screenshot of:**
   - The profile page (showing the error)
   - The "user object" you logged in with

---

## Verification Checklist

- [ ] Backend fully restarted? (**"Started Application"** message visible?)
- [ ] Frontend fully restarted? (No build errors?)
- [ ] Cleared localStorage?
- [ ] Logged in fresh?
- [ ] Browser console open (F12)?
- [ ] Clicked Profile to trigger the logs?

If all ✅, you should be good. If not, share the console logs!
