# 🎫 Cancellation & Refund System - Complete Implementation

A production-ready Cancellation & Refund System for the Make My Trip Spring Boot application with automatic refund calculation, partial cancellation support, and real-time refund status tracking.

## ✨ Features

### 1. **Cancel Booking via Dashboard**
- Users can cancel confirmed bookings from their dashboard
- Real-time refund preview before confirming
- Automatic booking status update (CONFIRMED → CANCELLED/PARTIALLY_CANCELLED)

### 2. **Auto Refund Calculation**
- **24+ hours before travel**: 90% refund ✅
- **< 24 hours before travel**: 50% refund ✅
- Works for full and partial cancellations
- Instant calculation based on travel time

### 3. **Partial Cancellation**
- Cancel specific seats/rooms from a multi-unit booking
- Remaining units stay active
- Pro-rated refund calculation

### 4. **Cancellation Reason Tracking**
```
- Change of plans
- Found better price
- Medical reason
- Booking mistake
- Other
```

### 5. **Refund Status Tracker**
Real-time 4-stage progression:
```
Cancellation Requested → Refund Initiated → Processing → Refunded
```

---

## 📁 Project Structure

```
src/main/java/com/makemytrip/makemytrip/cancellation/
├── model/
│   ├── CancellationReason.java       ✅ Enum: reasons
│   ├── RefundStatus.java             ✅ Enum: stages
│   ├── BookingStatus.java            ✅ Enum: CONFIRMED, CANCELLED, PARTIALLY_CANCELLED
│   ├── Cancellation.java             ✅ Main entity
│   └── RefundTracker.java            ✅ Refund tracking
├── dto/
│   ├── CancellationRequestDTO.java   ✅ Frontend → Backend
│   ├── CancellationResponseDTO.java  ✅ Backend → Frontend
│   ├── CancellationPreviewDTO.java   ✅ Preview before confirm
│   └── RefundTrackerDTO.java         ✅ Refund status info
├── repository/
│   ├── CancellationRepository.java   ✅ MongoDB operations
│   └── RefundTrackerRepository.java  ✅ MongoDB operations
├── service/
│   ├── CancellationService.java      ✅ Business logic
│   └── RefundCalculationService.java ✅ Math engine
└── controller/
    └── CancellationController.java   ✅ 8 REST endpoints
```

---

## 🚀 Quick Start

### 1. No Configuration Needed!
The system uses MongoDB (already in your project). Collections are created automatically on first use.

### 2. API Endpoints Ready

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/cancellation/preview` | Show refund preview |
| POST | `/api/cancellation/cancel` | Process cancellation |
| GET | `/api/cancellation/refund-status/{id}` | Check refund status |
| GET | `/api/cancellation/user/{userId}/cancellations` | User's cancellations |
| GET | `/api/cancellation/{id}` | Cancellation details |
| PUT | `/api/cancellation/refund-status/{id}` | Update refund status (Admin) |
| GET | `/api/cancellation/reasons` | Dropdown: reasons |
| GET | `/api/cancellation/refund-statuses` | Dropdown: statuses |

### 3. Example Flow

```typescript
// User clicks "Cancel Booking"
const preview = await fetch(`/api/cancellation/preview?...`);

// Shows refund amount and policy
// User selects reason and confirms

const result = await fetch(`/api/cancellation/cancel?...`, {
  method: 'POST',
  body: JSON.stringify(cancellationDetails)
});

// Booking status updated, refund tracking started
// User sees confirmation + refund status
```

---

## 📊 Database Schema

### Cancellations Collection
```javascript
{
  "_id": ObjectId,
  "userId": "...",
  "bookingId": "...",
  "bookingType": "FLIGHT" | "HOTEL",
  "bookingStatus": "CONFIRMED" | "CANCELLED" | "PARTIALLY_CANCELLED",
  "reason": "CHANGE_OF_PLANS" | ... ,
  "totalQuantity": 2,
  "cancelledQuantity": 1,
  "remainingQuantity": 1,
  "originalPrice": 10000,
  "refundAmount": 9000,
  "refundPercentage": 90,
  "travelDateTime": ISODate(...),
  "cancellationRequestedAt": ISODate(...),
  "refundTrackerId": ObjectId,
  "partialCancellation": false
}
```

### Refund Trackers Collection
```javascript
{
  "_id": ObjectId,
  "cancellationId": ObjectId,
  "status": "CANCELLATION_REQUESTED" | "REFUND_INITIATED" | "PROCESSING" | "REFUNDED",
  "refundAmount": 9000,
  "createdAt": ISODate(...),
  "updatedAt": ISODate(...),
  "notes": "optional notes"
}
```

---

## 🔧 Integration with Dashboard

### Step 1: Add Cancel Button to Booking Card
```tsx
<button onClick={() => openCancellationDialog(booking)}>
  Cancel Booking
</button>
```

### Step 2: Show Preview
```typescript
const preview = await fetch(
  `/api/cancellation/preview?
    bookingId=${booking.id}&
    bookingType=${booking.type}&
    quantityToCancel=${quantity}&
    totalQuantity=${booking.quantity}&
    originalPrice=${booking.price}&
    travelDateTimeString=${booking.date}`
);
```

### Step 3: Display Refund Info
```tsx
<div className="refund-preview">
  <p>Refund: ₹{preview.refundAmount}</p>
  <p>Policy: {preview.refundPolicy}</p>
  <p>Hours until travel: {preview.hoursUntilTravel}</p>
</div>
```

### Step 4: Process Cancellation
```typescript
const result = await fetch(`/api/cancellation/cancel`, {
  method: 'POST',
  body: JSON.stringify({
    bookingId: booking.id,
    bookingType: booking.type,
    reason: selectedReason,
    quantityToCancel: quantity
  })
});
```

### Step 5: Track Refund Status
```tsx
<div className="refund-tracker">
  <p>Status: {result.refundTracker.statusDisplay}</p>
  <p>Amount: ₹{result.refundAmount}</p>
  <Timeline stages="CANCELLATION_REQUESTED → REFUND_INITIATED → PROCESSING → REFUNDED" />
</div>
```

---

## 🧪 Testing

### Option 1: cURL Commands
See `API_TESTING_GUIDE.md` for complete cURL examples

Quick test:
```bash
# Get cancellation reasons
curl http://localhost:8080/api/cancellation/reasons

# Generate preview
curl "http://localhost:8080/api/cancellation/preview?
bookingId=flight-123&
bookingType=FLIGHT&
quantityToCancel=1&
totalQuantity=2&
originalPrice=10000&
travelDateTimeString=2026-03-05T14:30:00"

# Process cancellation
curl -X POST http://localhost:8080/api/cancellation/cancel \
  -H "Content-Type: application/json" \
  -d '{"bookingId":"flight-123","bookingType":"FLIGHT","reason":"CHANGE_OF_PLANS","quantityToCancel":1}'
```

### Option 2: Postman Collection
Import the collection from `API_TESTING_GUIDE.md` and run requests

### Option 3: Automated Testing
```bash
node test-cancellation-api.js
```
See `API_TESTING_GUIDE.md` for Node.js script

---

## 📖 Documentation Files

| File | Purpose |
|------|---------|
| `CANCELLATION_SYSTEM_DOCUMENTATION.md` | Complete feature documentation |
| `DASHBOARD_INTEGRATION_GUIDE.md` | Frontend integration examples |
| `API_TESTING_GUIDE.md` | cURL + Postman + Testing |
| `README.md` | This file |

---

## 🎯 Refund Calculation Algorithm

```
IF (Travel Time - Now) >= 24 hours:
    Refund % = 90%
ELSE:
    Refund % = 50%

FOR FULL CANCELLATION:
    Refund = Original Price × Refund %

FOR PARTIAL CANCELLATION:
    Price Per Unit = Original Price / Total Quantity
    Cancellation Price = Price Per Unit × Units Being Cancelled
    Refund = Cancellation Price × Refund %
```

### Examples

**Example 1: Flight cancelled 48 hours before travel**
- Original: ₹10,000 (2 flights)
- Cancel: 1 flight
- Calculation: (10,000 ÷ 2) × 1 × 0.90 = ₹4,500 (90% refund)

**Example 2: Hotel cancelled 20 hours before check-in**
- Original: ₹45,000 (3 rooms)
- Cancel: 2 rooms
- Calculation: (45,000 ÷ 3) × 2 × 0.50 = ₹15,000 (50% refund)

---

## 🔐 Security

- ✅ User-specific cancellations (via X-User-ID header)
- ✅ Input validation on all endpoints
- ✅ MongoDB injection protection (ORM-based queries)
- ✅ Error handling (no sensitive data exposure)
- ✅ Audit trail (timestamps on all operations)

**For production:**
- Add JWT authentication to `/cancel` endpoint
- Verify X-User-ID matches authenticated user
- Add rate limiting to prevent abuse
- Log all cancellation operations

---

## 🚨 Error Handling

All errors return appropriate HTTP status codes:

```
400 Bad Request     - Invalid input (missing bookingId, invalid reason)
404 Not Found       - Cancellation or refund tracker not found
500 Server Error    - Database or system issues
```

Error response format:
```json
{
  "error": "Error message here",
  "timestamp": "2026-03-03T15:45:00"
}
```

---

## 📈 What's NOT Modified

✅ **Zero impact** on existing code:
- No changes to User model
- No changes to Booking model
- No changes to Flight/Hotel controllers
- No changes to authentication
- No changes to payment processing

Everything is in `src/main/java/com/makemytrip/makemytrip/cancellation/`

---

## 🎁 Bonus Features Ready

- ✅ Full REST API with 8 endpoints
- ✅ Automatic MongoDB collections
- ✅ Dependency injection setup
- ✅ Enum-based status tracking
- ✅ DateTime calculations
- ✅ Partial cancellation math
- ✅ Audit timestamps
- ✅ Error responses

**Future enhancements (easy to add):**
- Email notifications on cancellation
- Payment gateway integration
- Cancellation insurance options
- Analytics dashboard
- Scheduled refund updates
- Multi-currency support

---

## 📋 Refund Status Workflow

```
User clicks "Cancel Booking"
          ↓
System shows PREVIEW with refund amount
          ↓
User selects REASON and confirms
          ↓
System creates CANCELLATION record
System creates REFUNDTRACKER (Status: CANCELLATION_REQUESTED)
Booking status updated to CANCELLED/PARTIALLY_CANCELLED
          ↓
[ADMIN/WEBHOOK]
          ↓
Status → REFUND_INITIATED (Admin confirms refund to be sent)
          ↓
Status → PROCESSING (Payment gateway processing)
          ↓
Status → REFUNDED (Money sent to user's account)
          ↓
User sees "Refunded" in dashboard
```

---

## 🤝 Integration Checklist

- [ ] Read `CANCELLATION_SYSTEM_DOCUMENTATION.md`
- [ ] Review API endpoints in `CANCELLATION_SYSTEM_DOCUMENTATION.md`
- [ ] Test endpoints using `API_TESTING_GUIDE.md`
- [ ] Follow frontend integration in `DASHBOARD_INTEGRATION_GUIDE.md`
- [ ] Add "Cancel Booking" button to booking card
- [ ] Call `/api/cancellation/preview` to show refund amount
- [ ] Call `/api/cancellation/cancel` to process cancellation
- [ ] Call `/api/cancellation/user/{userId}/cancellations` to show refund status
- [ ] Update booking UI to show status changes
- [ ] Test full and partial cancellations
- [ ] Test 24-hour refund policy boundary

---

## 📞 Support

**Issues?**
1. Check `CANCELLATION_SYSTEM_DOCUMENTATION.md` Troubleshooting section
2. Verify travel datetime is in correct ISO format
3. Check MongoDB is running
4. Review error response for details

**Testing?**
1. Use cURL commands from `API_TESTING_GUIDE.md`
2. Import Postman collection
3. Run Node.js test script

---

## 📝 Summary

| Aspect | Status |
|--------|--------|
| Full Cancellation | ✅ Implemented |
| Partial Cancellation | ✅ Implemented |
| Auto Refund (90%) | ✅ Implemented |
| Auto Refund (50%) | ✅ Implemented |
| Reason Tracking | ✅ Implemented |
| Refund Status Tracker | ✅ Implemented |
| REST APIs (8 endpoints) | ✅ Implemented |
| MongoDB Collections | ✅ Auto-created |
| Error Handling | ✅ Implemented |
| Existing Code Untouched | ✅ Verified |

---

## 🎉 You're Ready!

Everything is implemented and ready to integrate into your dashboard.

**Next Step:** Follow the steps in `DASHBOARD_INTEGRATION_GUIDE.md` to add cancellation functionality to your UI.

**Questions?** Review the detailed documentation files or test the APIs using the provided cURL commands.

---

**Version:** 1.0.0  
**Created:** March 3, 2026  
**Status:** Production Ready ✅
