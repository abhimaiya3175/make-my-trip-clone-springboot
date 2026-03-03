# Debugging 404 Error: "getUserCancellations"

## Quick Summary
The **404 error** means the backend cannot find the endpoint, which happens when `userId` is **undefined, null, or empty**.

---

## Step-by-Step Debugging

### **STEP 1: Restart Both Servers**
The backend needs to restart to pick up the `@JsonProperty("id")` change:

1. **Kill Spring Boot** (if running)
   ```powershell
   # In the backend terminal, press Ctrl+C
   ```

2. **Kill Node Frontend** (if running)
   ```powershell
   # In the frontend terminal, press Ctrl+C
   ```

3. **Start backend:**
   ```powershell
   cd "e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main"
   .\start-backend.ps1
   ```

4. **Start frontend** (in a NEW terminal):
   ```powershell
   cd "e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main\makemytour"
   .\start-frontend.ps1
   ```

Wait for both to start completely (you'll see no errors in the console).

---

### **STEP 2: Open Browser DevTools (F12)**

1. Press **F12** to open Developer Tools
2. Go to **Console** tab
3. **Keep this open** while testing

---

### **STEP 3: Log in to the App**

1. Go to `http://localhost:3000` (or your app URL)
2. Click **Sign Up** button (or login if you have an account)
3. **DO NOT navigate away yet** - keep DevTools open!

---

### **STEP 4: Watch Console Output During Login**

**During/After login, you should see in the console:**

```
[Store] User set: {_id: "...", firstName: "...", email: "...", ...}
```

OR (after our fix):

```
[Store] User set: {id: "...", firstName: "...", email: "...", ...}
```

✅ **If you see `id:` field** → The @JsonProperty fix worked!
❌ **If you see only `_id:` field** → The backend didn't pick up the compile changes yet

---

### **STEP 5: Navigate to Profile & Check Logs**

1. Click on **Profile** in the navbar (top right avatar)
2. **Immediately look at the browser console** - you should see:

```
[Profile] useEffect triggered. user object: {...full object...}
[Profile] user.id: (some-id-value)  user._id: (some-id-value)
[Profile] Fetching cancellations for userId: (the-actual-id) type: string
[API] getUserCancellations request {
    userId: "...",
    userIdType: "string",
    fullUrl: "http://localhost:8080/api/cancellation/user/.../cancellations",
    backendUrl: "http://localhost:8080"
}
```

---

### **What Each Log Tells You**

| Log Message | What It Means |
|------------|---------------|
| `user.id: undefined` | ❌ Fix didn't work - backend still sending `_id` instead of `id` |
| `user.id: (some-value)` | ✅ Fix worked - backend serialized correctly |
| `userId: undefined` | ❌ Neither `user.id` nor `user._id` exist - login broken |
| `fullUrl: ...user/undefined/...` | ❌ userId is undefined - won't find endpoint |
| `fullUrl: ...user/actual-id/...` | ✅ Correct URL being called |

---

### **STEP 6: Spring Boot Console**

Look at the **Spring Boot terminal output** for these logs:

```
[GET_USER_CANCELLATIONS] START userId=..., type=String, isEmpty=false
[GET_USER_CANCELLATIONS] SUCCESS: Found 0 cancellation(s) for userId=...
```

OR ERROR:

```
[GET_USER_CANCELLATIONS] ERROR: userId is null or empty!
```

---

## Which Scenario Are You Seeing?

### **Scenario A: Works Fine ✅**
**Browser Console Shows:**
```
[API] getUserCancellations request {userId: "507f1f77bcf86cd799439011", ...}
[API] getUserCancellations SUCCESS. Response: [...]
```

**Spring Boot Console Shows:**
```
[GET_USER_CANCELLATIONS] SUCCESS: Found 3 cancellation(s) for userId=507f1f77bcf86cd799439011
```

→ **FIXED!** Cancellations should display.

---

### **Scenario B: 404 Still Happening ❌**
**Browser Console Shows:**
```
[API] getUserCancellations request {userId: undefined, ...}
[API] getUserCancellations FAILED {
    userId: undefined,
    status: 404,
    url: "http://localhost:8080/api/cancellation/user/undefined/cancellations"
}
```

→ **ROOT CAUSE: userId is undefined**

**Check:**
1. Is the server restarted?
2. Is login working?
3. Run this in browser console:
   ```javascript
   // Check what the app has stored for the user
   const stored = JSON.parse(localStorage.getItem('user'));
   console.log("Stored user:", stored);
   console.log("Has 'id' field:", !!stored?.id);
   console.log("Has '_id' field:", !!stored?._id);
   ```

---

### **Scenario C: Backend Returns 400 (Bad Request) ⚠️**
**Spring Boot Console Shows:**
```
[GET_USER_CANCELLATIONS] ERROR: userId is null or empty!
```

→ **FIX NEEDED:** The backend received an empty string. The API function's validation will catch this BEFORE making the request.

---

## The Complete Data Flow

```
1. User Logs In
   ↓
2. Backend Returns User Object (should have "id" field now)
   ↓
3. Frontend Stores in Redux/LocalStorage
   ↓
4. Profile Page OnMount reads user.id
   ↓
5. API function constructs URL: /api/cancellation/user/{userId}/cancellations
   ↓
6. Backend receives request and looks up cancellations by userId
   ↓
7. Returns cancellations (or empty array) with 200 OK
```

**If ANY step fails → 404 error**

---

## Quick Checklist

- [ ] Backend restarted (see "Compiling..." then "Started Application")?
- [ ] Frontend restarted?
- [ ] Logged in successfully?
- [ ] Opened Profile page?
- [ ] Checked browser console for `[API]` logs?
- [ ] Checked Spring Boot console for `[GET_USER_CANCELLATIONS]` logs?
- [ ] Do you see `userId:` value in the logs (not `undefined`)?

---

## If Still Stuck

Copy-paste these from your **browser console** and **Spring Boot console** and share them:

**Browser Console (after clicking Profile):**
```
(Select all with Ctrl+A and copy the [Profile] and [API] logs)
```

**Spring Boot Console:**
```
(Find [GET_USER_CANCELLATIONS] logs and copy them)
```

---

## Common Fixes

### Fix 1: Clear Browser Cache
```javascript
// In browser console, type:
localStorage.clear();
location.reload();
// Then log in again
```

### Fix 2: Restart Everything
```powershell
# Stop both servers (Ctrl+C)
# Clear Maven cache
.\mvnw.cmd clean

# Restart backend
.\start-backend.ps1

# In another terminal, restart frontend
.\start-frontend.ps1
```

### Fix 3: Check Backend is Actually Compiled
```powershell
cd "e:\ProJect\make-my-trip-clone-springboot-main\make-my-trip-clone-springboot-main"
.\mvnw.cmd compile -q
# Should complete with no errors
```
