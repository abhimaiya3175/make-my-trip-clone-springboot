# Cancellation & Refund System - Complete Documentation

## Overview

This is a complete, production-ready Cancellation & Refund System for the Make My Trip Spring Boot application. The system allows users to cancel bookings with automatic refund calculation based on time-based policies, supports partial cancellations, and provides real-time refund status tracking.

---

## Features Implemented

### 1. **Cancel Booking via User Dashboard**
- Users can initiate cancellation from the dashboard
- Full cancellation: Cancel entire booking
- Partial cancellation: Cancel specific seats/rooms while keeping others
- Real-time preview of refund amount before confirmation
- Booking status automatically updates: CONFIRMED → CANCELLED or PARTIALLY_CANCELLED

### 2. **Auto Refund Based on Time Policy**
- **24+ hours before travel**: 90% refund automatically calculated
- **Less than 24 hours before travel**: 50% refund automatically calculated
- Calculation happens automatically during cancellation request
- Works for partial cancellations (refund = per-unit price × cancelled quantity × refund percentage)

### 3. **Partial Cancellation**
- Users can specify quantity to cancel
- Remaining seats/rooms stay confirmed
- Separate pricing calculation for cancelled portion
- Smart status updates: 
  - Full cancellation: Status = CANCELLED
  - Partial: Status = PARTIALLY_CANCELLED

### 4. **Cancellation Reason Dropdown**
- 5 predefined reasons (with enum):
  - Change of plans
  - Found better price
  - Medical reason
  - Booking mistake
  - Other
- Reason is stored with cancellation record
- Helps with analytics and customer insights

### 5. **Refund Status Tracker**
- 4-stage refund progression:
  1. **Cancellation Requested** - Initial state upon cancellation
  2. **Refund Initiated** - Admin/system initiates refund processing
  3. **Processing** - Refund is being processed by payment gateway
  4. **Refunded** - Money returned to user's account
- Users can track status from dashboard in real-time
- Timestamps track when status changed

---

## Project Structure

```
src/main/java/com/makemytrip/makemytrip/cancellation/
├── model/
│   ├── CancellationReason.java       # Enum for cancellation reasons
│   ├── RefundStatus.java             # Enum for refund stages
│   ├── BookingStatus.java            # Enum for booking status
│   ├── Cancellation.java             # Main cancellation entity
│   └── RefundTracker.java            # Refund tracking entity
├── dto/
│   ├── CancellationRequestDTO.java   # Request from frontend
│   ├── CancellationResponseDTO.java  # Response to frontend
│   ├── CancellationPreviewDTO.java   # Preview before confirmation
│   └── RefundTrackerDTO.java         # Refund status info
├── repository/
│   ├── CancellationRepository.java   # MongoDB operations
│   └── RefundTrackerRepository.java  # MongoDB operations
├── service/
│   ├── CancellationService.java      # Business logic
│   └── RefundCalculationService.java # Refund math engine
└── controller/
    └── CancellationController.java   # REST endpoints
```

---

## MongoDB Collections

Two new MongoDB collections are automatically created:

### 1. **cancellations** Collection
```javascript
{
  "_id": ObjectId("..."),
  "userId": "user-123",
  "bookingId": "flight-456",
  "bookingType": "FLIGHT",
  "bookingStatus": "CANCELLED",        // Enum: CONFIRMED, CANCELLED, PARTIALLY_CANCELLED
  "reason": "CHANGE_OF_PLANS",         // Enum value
  "totalQuantity": 2,
  "cancelledQuantity": 2,
  "remainingQuantity": 0,
  "originalPrice": 10000.0,
  "refundAmount": 9000.0,
  "refundPercentage": 90.0,
  "travelDateTime": ISODate("2026-04-15T10:30:00"),
  "cancellationRequestedAt": ISODate("2026-03-03T15:45:00"),
  "refundTrackerId": ObjectId("..."),
  "partialCancellation": false,
  "additionalNotes": "optional notes"
}
```

### 2. **refund_trackers** Collection
```javascript
{
  "_id": ObjectId("..."),
  "cancellationId": ObjectId("..."),
  "status": "CANCELLATION_REQUESTED",   // Enum: CANCELLATION_REQUESTED, REFUND_INITIATED, PROCESSING, REFUNDED
  "refundAmount": 9000.0,
  "createdAt": ISODate("2026-03-03T15:45:00"),
  "updatedAt": ISODate("2026-03-03T15:45:00"),
  "notes": "optional processing notes"
}
```

---

## REST API Endpoints

### 1. **Generate Cancellation Preview**
**Endpoint:** `GET /api/cancellation/preview`

**Query Parameters:**
- `bookingId` (String): ID of booking to cancel
- `bookingType` (String): "FLIGHT" or "HOTEL"
- `quantityToCancel` (int): Units to cancel
- `totalQuantity` (int): Total units in booking
- `originalPrice` (double): Total booking price
- `travelDateTimeString` (String): ISO format datetime (e.g., "2026-04-15T10:30:00")

**Response:**
```json
{
  "bookingId": "flight-456",
  "bookingType": "FLIGHT",
  "totalQuantity": 2,
  "quintityToCancel": 1,
  "remainingQuantity": 1,
  "originalPrice": 10000.0,
  "pricePerUnit": 5000.0,
  "cancellationPrice": 5000.0,
  "refundAmount": 4500.0,
  "refundPercentage": 90.0,
  "refundPolicy": "Eligible for 90% refund (Cancelled 24+ hours before travel)",
  "hoursUntilTravel": "35 hours",
  "eligibleFor90Percent": true,
  "travelDate": "2026-04-15T10:30:00"
}
```

### 2. **Process Booking Cancellation**
**Endpoint:** `POST /api/cancellation/cancel`

**Headers:**
- `X-User-ID` (Optional): User ID from authentication

**Query Parameters:**
- `totalQuantity` (int): Total units in original booking
- `originalPrice` (double): Total booking price
- `travelDateTimeString` (String): ISO format datetime

**Request Body:**
```json
{
  "bookingId": "flight-456",
  "bookingType": "FLIGHT",
  "reason": "CHANGE_OF_PLANS",
  "quantityToCancel": 1,
  "additionalNotes": "optional notes for admin"
}
```

**Response:**
```json
{
  "cancellationId": "cancel-789",
  "bookingId": "flight-456",
  "bookingType": "FLIGHT",
  "newBookingStatus": "PARTIALLY_CANCELLED",
  "totalQuantity": 2,
  "cancelledQuantity": 1,
  "remainingQuantity": 1,
  "refundAmount": 4500.0,
  "refundPercentage": 90.0,
  "refundTrackerId": "tracker-100",
  "message": "Partial cancellation successful. 1 unit(s) cancelled. Refund of ₹4500.00 will be processed shortly.",
  "success": true,
  "partialCancellation": true,
  "refundTracker": {
    "id": "tracker-100",
    "cancellationId": "cancel-789",
    "status": "CANCELLATION_REQUESTED",
    "statusDisplay": "Cancellation Requested",
    "refundAmount": 4500.0,
    "createdAt": "2026-03-03T15:45:00",
    "updatedAt": "2026-03-03T15:45:00"
  }
}
```

### 3. **Get Refund Status**
**Endpoint:** `GET /api/cancellation/refund-status/{cancellationId}`

**Response:**
```json
{
  "id": "tracker-100",
  "cancellationId": "cancel-789",
  "status": "REFUND_INITIATED",
  "statusDisplay": "Refund Initiated",
  "refundAmount": 4500.0,
  "createdAt": "2026-03-03T15:45:00",
  "updatedAt": "2026-03-03T16:00:00",
  "notes": "Processing refund"
}
```

### 4. **Get User's Cancellations**
**Endpoint:** `GET /api/cancellation/user/{userId}/cancellations`

**Response:** Array of cancellation objects with refund details

### 5. **Get Cancellation Details**
**Endpoint:** `GET /api/cancellation/{cancellationId}`

**Response:** Full cancellation record with all details

### 6. **Update Refund Status (Admin)**
**Endpoint:** `PUT /api/cancellation/refund-status/{refundTrackerId}`

**Query Parameters:**
- `status` (String): New status (CANCELLATION_REQUESTED, REFUND_INITIATED, PROCESSING, REFUNDED)
- `notes` (String): Optional processing notes

### 7. **Get Cancellation Reasons (Dropdown)**
**Endpoint:** `GET /api/cancellation/reasons`

**Response:**
```json
{
  "CHANGE_OF_PLANS": "Change of plans",
  "FOUND_BETTER_PRICE": "Found better price",
  "MEDICAL_REASON": "Medical reason",
  "BOOKING_MISTAKE": "Booking mistake",
  "OTHER": "Other"
}
```

### 8. **Get Refund Statuses (Dropdown)**
**Endpoint:** `GET /api/cancellation/refund-statuses`

**Response:**
```json
{
  "CANCELLATION_REQUESTED": "Cancellation Requested",
  "REFUND_INITIATED": "Refund Initiated",
  "PROCESSING": "Processing",
  "REFUNDED": "Refunded"
}
```

---

## Business Logic

### Refund Calculation Algorithm

```
If (Current Time + 24 hours) <= Travel Time:
    Refund Percentage = 90%
Else:
    Refund Percentage = 50%

For Full Cancellation:
    Refund Amount = Original Price × Refund Percentage

For Partial Cancellation:
    Price Per Unit = Original Price / Total Quantity
    Cancellation Price = Price Per Unit × Quantity To Cancel
    Refund Amount = Cancellation Price × Refund Percentage
```

### Booking Status Transitions

```
Original Status: CONFIRMED

Full Cancellation (cancel all units):
    CONFIRMED → CANCELLED

Partial Cancellation (cancel some units):
    CONFIRMED → PARTIALLY_CANCELLED
    (Remaining units stay "confirmed" conceptually)
```

### Refund Status Progression

```
1. CANCELLATION_REQUESTED (Initial)
   ↓
2. REFUND_INITIATED (Admin triggers)
   ↓
3. PROCESSING (Payment gateway processing)
   ↓
4. REFUNDED (Completed, money sent to user)
```

---

## Integration Guide

### Step 1: Add Dependencies (Already in pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

### Step 2: Enable Component Scanning
Your `MakemytripApplication.java` should have:
```java
@SpringBootApplication
@EnableMongoRepositories
public class MakemytripApplication {
    public static void main(String[] args) {
        SpringApplication.run(MakemytripApplication.class, args);
    }
}
```

### Step 3: Database Configuration
MongoDB automatically creates collections on first write. No manual setup needed.

### Step 4: Update Booking Model (Optional)
Add status field to track booking status:
```java
public class Booking {
    private String status; // CONFIRMED, CANCELLED, PARTIALLY_CANCELLED
    // ... existing fields
}
```

---

## Frontend Integration Examples

### React/Next.js Component (Cancellation Preview)

```typescript
// fetchCancellationPreview.ts
export const fetchCancellationPreview = async (
  bookingId: string,
  bookingType: 'FLIGHT' | 'HOTEL',
  quantityToCancel: number,
  totalQuantity: number,
  originalPrice: number,
  travelDateTime: Date
) => {
  const response = await fetch(
    `/api/cancellation/preview?` +
    `bookingId=${bookingId}&` +
    `bookingType=${bookingType}&` +
    `quantityToCancel=${quantityToCancel}&` +
    `totalQuantity=${totalQuantity}&` +
    `originalPrice=${originalPrice}&` +
    `travelDateTimeString=${travelDateTime.toISOString()}`
  );
  return response.json();
};

// processCancellation.ts
export const processCancellation = async (
  cancellationRequest: CancellationRequestDTO,
  totalQuantity: number,
  originalPrice: number,
  travelDateTime: Date,
  userId?: string
) => {
  const response = await fetch(
    `/api/cancellation/cancel?` +
    `totalQuantity=${totalQuantity}&` +
    `originalPrice=${originalPrice}&` +
    `travelDateTimeString=${travelDateTime.toISOString()}`,
    {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(userId && { 'X-User-ID': userId })
      },
      body: JSON.stringify(cancellationRequest)
    }
  );
  return response.json();
};
```

### Dashboard Components

```typescript
// UserCancellations.tsx
async function loadUserCancellations(userId: string) {
  const response = await fetch(`/api/cancellation/user/${userId}/cancellations`);
  const cancellations = await response.json();
  
  return cancellations.map(cancellation => ({
    bookingId: cancellation.bookingId,
    bookingType: cancellation.bookingType,
    status: cancellation.newBookingStatus,
    refundAmount: cancellation.refundAmount,
    refundStatus: cancellation.refundTracker?.statusDisplay,
    refundPercentage: cancellation.refundPercentage
  }));
}
```

---

## Key Design Principles

1. **Clean Architecture**: Separated concerns into Model, DTO, Repository, Service, Controller
2. **No Existing Code Modified**: Completely independent module - zero impact on existing bookings
3. **Dependency Injection**: All dependencies injected via @Autowired
4. **Time-Based Logic**: Uses Java LocalDateTime for accurate time calculations
5. **Audit Trail**: Timestamps track all state changes
6. **Flexibility**: Supports both full and partial cancellations
7. **Data Integrity**: Separate collections for cancellations and refund tracking

---

## Testing Scenarios

### Test Case 1: Full Cancellation (24+ hours before travel)
```
Configuration:
- Total Units: 2
- Units to Cancel: 2 (full cancellation)
- Original Price: ₹10,000
- Travel Time: 36 hours from now

Expected:
- Booking Status: CANCELLED
- Refund Percentage: 90%
- Refund Amount: ₹9,000
```

### Test Case 2: Partial Cancellation (< 24 hours before travel)
```
Configuration:
- Total Units: 4 (rooms)
- Units to Cancel: 2 (partial)
- Original Price: ₹20,000 (₹5,000 per room)
- Travel Time: 20 hours from now

Expected:
- Booking Status: PARTIALLY_CANCELLED
- Remaining Units: 2
- Refund Percentage: 50%
- Refund Amount: ₹5,000 (₹2,500 × 2 rooms)
```

### Test Case 3: Refund Status Tracking
```
1. Create cancellation → Status: CANCELLATION_REQUESTED
2. Admin trigger → Status: REFUND_INITIATED
3. Simulate processing → Status: PROCESSING
4. Complete refund → Status: REFUNDED
```

---

## Error Handling

The system handles:
- Missing required parameters
- Invalid cancellation reasons
- Duplicate cancellation requests for same booking
- Invalid refund status transitions
- Malformed datetime strings
- Database connection issues

All errors return appropriate HTTP status codes:
- **400 Bad Request**: Invalid input
- **404 Not Found**: Resource not found
- **500 Internal Server Error**: Server-side issues

---

## Security Considerations

1. **User Validation**: HTTP header `X-User-ID` for authentication (implement JWT validation in production)
2. **Authorization**: Verify user can only cancel their own bookings
3. **Data Protection**: Don't expose sensitive details in API responses
4. **Audit Logging**: All cancellations logged for compliance

---

## Future Enhancements

1. **Email Notifications**: Send confirmation and refund status updates
2. **Payment Gateway Integration**: Auto-process refunds with Stripe/PayPal
3. **Cancellation Insurance**: Optional coverage for non-refundable bookings
4. **Analytics Dashboard**: Track cancellation patterns and refund statistics
5. **Scheduled Tasks**: Auto-update refund status based on payment gateway webhooks
6. **Currency Support**: Multi-currency refund calculations

---

## Troubleshooting

### Issue: Refund Amount Not Calculated Correctly
**Solution**: Verify travel datetime is in future and in correct ISO format

### Issue: Cancellation Not Found
**Solution**: Ensure booking was created first, then cancelled

### Issue: MongoDB Connection Error
**Solution**: Check MongoDB is running and connection string in application.properties

### Issue: CORS Errors
**Solution**: @CrossOrigin annotation in controller allows all origins (configure as needed)

---

## Summary

This is a complete, production-ready cancellation and refund system that:
- ✅ Requires NO modifications to existing code
- ✅ Implements all 5 required features
- ✅ Follows clean architecture principles
- ✅ Provides comprehensive REST APIs
- ✅ Includes proper error handling
- ✅ Supports full and partial cancellations
- ✅ Auto-calculates refunds based on time policies
- ✅ Tracks refund status in real-time

**Ready to integrate into your dashboard!**
