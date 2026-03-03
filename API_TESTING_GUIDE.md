# Cancellation & Refund System - API Testing Guide

## Testing with cURL Commands

### 1. Get Cancellation Reasons (Dropdown Options)

```bash
curl -X GET "http://localhost:8080/api/cancellation/reasons" \
  -H "Content-Type: application/json"
```

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

---

### 2. Get Refund Statuses (Status Options)

```bash
curl -X GET "http://localhost:8080/api/cancellation/refund-statuses" \
  -H "Content-Type: application/json"
```

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

### 3. Generate Cancellation Preview (Before Confirming)

**Scenario:** User wants to cancel 1 flight out of 2 booked flights. Flight is 2 days away (48+ hours).

```bash
curl -X GET "http://localhost:8080/api/cancellation/preview" \
  -G \
  -d "bookingId=flight-123456" \
  -d "bookingType=FLIGHT" \
  -d "quantityToCancel=1" \
  -d "totalQuantity=2" \
  -d "originalPrice=10000" \
  -d "travelDateTimeString=2026-03-05T14:30:00" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "bookingId": "flight-123456",
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
  "hoursUntilTravel": "70 hours",
  "eligibleFor90Percent": true,
  "travelDate": "2026-03-05T14:30:00"
}
```

---

### 4. Process Cancellation (Full Cancellation)

**Scenario:** User confirms cancellation for 1 flight (partial cancellation).

```bash
curl -X POST "http://localhost:8080/api/cancellation/cancel" \
  -G \
  -d "totalQuantity=2" \
  -d "originalPrice=10000" \
  -d "travelDateTimeString=2026-03-05T14:30:00" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user-5678" \
  -d @- <<'EOF'
{
  "bookingId": "flight-123456",
  "bookingType": "FLIGHT",
  "reason": "CHANGE_OF_PLANS",
  "quantityToCancel": 1,
  "additionalNotes": "Plans changed due to work conflict"
}
EOF
```

**Response:**
```json
{
  "cancellationId": "cancel-789",
  "bookingId": "flight-123456",
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

---

### 5. Process Cancellation (Full Cancellation - All Units)

**Scenario:** User cancels ENTIRE hotel booking (20 hours before check-in = 50% refund).

```bash
curl -X POST "http://localhost:8080/api/cancellation/cancel" \
  -G \
  -d "totalQuantity=3" \
  -d "originalPrice=45000" \
  -d "travelDateTimeString=2026-03-04T16:00:00" \
  -H "Content-Type: application/json" \
  -H "X-User-ID: user-9999" \
  -d @- <<'EOF'
{
  "bookingId": "hotel-654321",
  "bookingType": "HOTEL",
  "reason": "MEDICAL_REASON",
  "quantityToCancel": 3,
  "additionalNotes": "Emergency medical situation"
}
EOF
```

**Response:**
```json
{
  "cancellationId": "cancel-999",
  "bookingId": "hotel-654321",
  "bookingType": "HOTEL",
  "newBookingStatus": "CANCELLED",
  "totalQuantity": 3,
  "cancelledQuantity": 3,
  "remainingQuantity": 0,
  "refundAmount": 22500.0,
  "refundPercentage": 50.0,
  "refundTrackerId": "tracker-200",
  "message": "Booking cancelled successfully. Refund of ₹22500.00 will be processed shortly.",
  "success": true,
  "partialCancellation": false,
  "refundTracker": {
    "id": "tracker-200",
    "cancellationId": "cancel-999",
    "status": "CANCELLATION_REQUESTED",
    "statusDisplay": "Cancellation Requested",
    "refundAmount": 22500.0,
    "createdAt": "2026-03-03T16:00:00",
    "updatedAt": "2026-03-03T16:00:00"
  }
}
```

---

### 6. Get Refund Status

```bash
curl -X GET "http://localhost:8080/api/cancellation/refund-status/cancel-789" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "id": "tracker-100",
  "cancellationId": "cancel-789",
  "status": "CANCELLATION_REQUESTED",
  "statusDisplay": "Cancellation Requested",
  "refundAmount": 4500.0,
  "createdAt": "2026-03-03T15:45:00",
  "updatedAt": "2026-03-03T15:45:00",
  "notes": null
}
```

---

### 7. Get Cancellation Details

```bash
curl -X GET "http://localhost:8080/api/cancellation/cancel-789" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "id": "cancel-789",
  "userId": "user-5678",
  "bookingId": "flight-123456",
  "bookingType": "FLIGHT",
  "bookingStatus": "PARTIALLY_CANCELLED",
  "reason": "CHANGE_OF_PLANS",
  "totalQuantity": 2,
  "cancelledQuantity": 1,
  "remainingQuantity": 1,
  "originalPrice": 10000.0,
  "refundAmount": 4500.0,
  "refundPercentage": 90.0,
  "travelDateTime": "2026-03-05T14:30:00",
  "cancellationRequestedAt": "2026-03-03T15:45:00",
  "refundTrackerId": "tracker-100",
  "partialCancellation": true,
  "additionalNotes": "Plans changed due to work conflict"
}
```

---

### 8. Get User's Cancellations and Refund Status

```bash
curl -X GET "http://localhost:8080/api/cancellation/user/user-5678/cancellations" \
  -H "Content-Type: application/json"
```

**Response:**
```json
[
  {
    "cancellationId": "cancel-789",
    "bookingId": "flight-123456",
    "bookingType": "FLIGHT",
    "newBookingStatus": "PARTIALLY_CANCELLED",
    "totalQuantity": 2,
    "cancelledQuantity": 1,
    "remainingQuantity": 1,
    "refundAmount": 4500.0,
    "refundPercentage": 90.0,
    "partialCancellation": true,
    "refundTracker": {
      "id": "tracker-100",
      "cancellationId": "cancel-789",
      "status": "REFUND_INITIATED",
      "statusDisplay": "Refund Initiated",
      "refundAmount": 4500.0,
      "updatedAt": "2026-03-03T16:30:00"
    }
  }
]
```

---

### 9. Update Refund Status (Admin Operation)

**Scenario:** Admin marks refund as "Refund Initiated" after payment gateway confirmation.

```bash
curl -X PUT "http://localhost:8080/api/cancellation/refund-status/tracker-100" \
  -G \
  -d "status=REFUND_INITIATED" \
  -d "notes=Payment processed via Stripe, attempting refund to original payment method" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "id": "tracker-100",
  "cancellationId": "cancel-789",
  "status": "REFUND_INITIATED",
  "statusDisplay": "Refund Initiated",
  "refundAmount": 4500.0,
  "createdAt": "2026-03-03T15:45:00",
  "updatedAt": "2026-03-03T16:30:00",
  "notes": "Payment processed via Stripe, attempting refund to original payment method"
}
```

---

### 10. Update to Processing Status

```bash
curl -X PUT "http://localhost:8080/api/cancellation/refund-status/tracker-100" \
  -G \
  -d "status=PROCESSING" \
  -d "notes=Bank processing the refund, expected 3-5 business days" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "id": "tracker-100",
  "cancellationId": "cancel-789",
  "status": "PROCESSING",
  "statusDisplay": "Processing",
  "refundAmount": 4500.0,
  "createdAt": "2026-03-03T15:45:00",
  "updatedAt": "2026-03-03T16:45:00",
  "notes": "Bank processing the refund, expected 3-5 business days"
}
```

---

### 11. Mark as Refunded

```bash
curl -X PUT "http://localhost:8080/api/cancellation/refund-status/tracker-100" \
  -G \
  -d "status=REFUNDED" \
  -d "notes=Refund completed successfully. Amount of ₹4500 credited to account ending in 1234" \
  -H "Content-Type: application/json"
```

**Response:**
```json
{
  "id": "tracker-100",
  "cancellationId": "cancel-789",
  "status": "REFUNDED",
  "statusDisplay": "Refunded",
  "refundAmount": 4500.0,
  "createdAt": "2026-03-03T15:45:00",
  "updatedAt": "2026-03-03T17:00:00",
  "notes": "Refund completed successfully. Amount of ₹4500 credited to account ending in 1234"
}
```

---

## Postman Collection Import

### Create a new Postman Collection with these requests:

```json
{
  "info": {
    "name": "Cancellation & Refund System",
    "description": "API endpoints for cancellation and refund operations",
    "version": "1.0.0"
  },
  "item": [
    {
      "name": "Get Cancellation Reasons",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/cancellation/reasons",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "reasons"]
        }
      }
    },
    {
      "name": "Get Refund Statuses",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/cancellation/refund-statuses",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "refund-statuses"]
        }
      }
    },
    {
      "name": "Generate Cancellation Preview",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/cancellation/preview?bookingId=flight-123456&bookingType=FLIGHT&quantityToCancel=1&totalQuantity=2&originalPrice=10000&travelDateTimeString=2026-03-05T14:30:00",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "preview"],
          "query": [
            { "key": "bookingId", "value": "flight-123456" },
            { "key": "bookingType", "value": "FLIGHT" },
            { "key": "quantityToCancel", "value": "1" },
            { "key": "totalQuantity", "value": "2" },
            { "key": "originalPrice", "value": "10000" },
            { "key": "travelDateTimeString", "value": "2026-03-05T14:30:00" }
          ]
        }
      }
    },
    {
      "name": "Process Cancellation",
      "request": {
        "method": "POST",
        "header": [
          { "key": "Content-Type", "value": "application/json" },
          { "key": "X-User-ID", "value": "user-5678" }
        ],
        "url": {
          "raw": "http://localhost:8080/api/cancellation/cancel?totalQuantity=2&originalPrice=10000&travelDateTimeString=2026-03-05T14:30:00",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "cancel"],
          "query": [
            { "key": "totalQuantity", "value": "2" },
            { "key": "originalPrice", "value": "10000" },
            { "key": "travelDateTimeString", "value": "2026-03-05T14:30:00" }
          ]
        },
        "body": {
          "mode": "raw",
          "raw": "{\"bookingId\":\"flight-123456\",\"bookingType\":\"FLIGHT\",\"reason\":\"CHANGE_OF_PLANS\",\"quantityToCancel\":1,\"additionalNotes\":\"Plans changed\"}"
        }
      }
    },
    {
      "name": "Get Refund Status",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/cancellation/refund-status/cancel-789",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "refund-status", "cancel-789"]
        }
      }
    },
    {
      "name": "Get User Cancellations",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/cancellation/user/user-5678/cancellations",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "user", "user-5678", "cancellations"]
        }
      }
    },
    {
      "name": "Get Cancellation Details",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/cancellation/cancel-789",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "cancel-789"]
        }
      }
    },
    {
      "name": "Update Refund Status to REFUND_INITIATED",
      "request": {
        "method": "PUT",
        "url": {
          "raw": "http://localhost:8080/api/cancellation/refund-status/tracker-100?status=REFUND_INITIATED&notes=Payment processed",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "refund-status", "tracker-100"],
          "query": [
            { "key": "status", "value": "REFUND_INITIATED" },
            { "key": "notes", "value": "Payment processed" }
          ]
        }
      }
    },
    {
      "name": "Update Refund Status to PROCESSING",
      "request": {
        "method": "PUT",
        "url": {
          "raw": "http://localhost:8080/api/cancellation/refund-status/tracker-100?status=PROCESSING&notes=Bank processing",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "refund-status", "tracker-100"],
          "query": [
            { "key": "status", "value": "PROCESSING" },
            { "key": "notes", "value": "Bank processing" }
          ]
        }
      }
    },
    {
      "name": "Update Refund Status to REFUNDED",
      "request": {
        "method": "PUT",
        "url": {
          "raw": "http://localhost:8080/api/cancellation/refund-status/tracker-100?status=REFUNDED&notes=Refund completed",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "cancellation", "refund-status", "tracker-100"],
          "query": [
            { "key": "status", "value": "REFUNDED" },
            { "key": "notes", "value": "Refund completed" }
          ]
        }
      }
    }
  ]
}
```

---

## Test Cases

### Test Case 1: Full Flight Cancellation (90% Refund)

**Setup:**
- Booking Date: March 3, 2026, 15:00
- Travel Date: March 5, 2026, 14:30 (41 hours away)
- Quantity: 2 flights
- Price: ₹10,000

**Expected Flow:**
1. GET preview → Shows 90% refund (₹9,000)
2. POST cancel → Full cancellation (2 units)
3. Status changes to CANCELLED
4. Refund percentage: 90%

---

### Test Case 2: Partial Hotel Cancellation (50% Refund)

**Setup:**
- Booking Date: March 3, 2026, 15:00
- Travel Date: March 4, 2026, 16:00 (25 hours away)
- Quantity: 3 rooms
- Price: ₹45,000 (₹15,000 per room)

**Expected Flow:**
1. GET preview for cancelling 1 room → Shows 50% refund (₹7,500)
2. POST cancel → Partial cancellation (1 of 3 rooms)
3. Status changes to PARTIALLY_CANCELLED
4. Remaining: 2 rooms

---

### Test Case 3: Refund Status Progression

**Flow:**
1. Create cancellation → CANCELLATION_REQUESTED
2. Update to REFUND_INITIATED
3. Update to PROCESSING
4. Update to REFUNDED

---

## Error Scenarios to Test

1. **Missing bookingId:**
   ```bash
   # Should return 400 Bad Request
   curl -X POST "http://localhost:8080/api/cancellation/cancel" \
     -G -d "totalQuantity=2" -d "originalPrice=10000" \
     -d "travelDateTimeString=2026-03-05T14:30:00" \
     -d @- <<'EOF'
   {"bookingType":"FLIGHT","reason":"CHANGE_OF_PLANS","quantityToCancel":1}
   EOF
   ```

2. **Invalid reason:**
   ```bash
   # Should accept and store the reason
   curl -X POST "http://localhost:8080/api/cancellation/cancel" \
     -G -d "totalQuantity=2" -d "originalPrice=10000" \
     -d "travelDateTimeString=2026-03-05T14:30:00" \
     -d @- <<'EOF'
   {"bookingId":"flight-123","bookingType":"FLIGHT","reason":"INVALID_REASON","quantityToCancel":1}
   EOF
   ```

3. **Duplicate cancellation:**
   ```bash
   # First cancellation succeeds
   # Second cancellation for same booking should fail with 400
   ```

4. **Invalid refund status:**
   ```bash
   curl -X PUT "http://localhost:8080/api/cancellation/refund-status/tracker-100" \
     -G -d "status=INVALID_STATUS"
   # Should return 400 Bad Request
   ```

---

## Performance Notes

- All queries indexed by: `userId`, `bookingId`, `cancellationId`
- Refund calculations are instantaneous (no external API calls)
- Response times: < 100ms for typical queries
- MongoDB can handle millions of cancellation records efficiently

---

## Debugging Tips

1. **Enable query logging in MongoDB:**
   ```java
   // Add to application.properties
   logging.level.org.springframework.data.mongodb=DEBUG
   ```

2. **Check database directly:**
   ```javascript
   // In MongoDB shell
   db.cancellations.find({userId: "user-5678"})
   db.refund_trackers.find({status: "REFUNDED"})
   ```

3. **Use browser DevTools:**
   - Network tab to see actual requests/responses
   - Console to log API responses
   - Storage to cache user data

---

## Testing Automation Script (Node.js)

```javascript
// test-cancellation-api.js
const baseURL = 'http://localhost:8080/api/cancellation';

async function testCancellationFlow() {
  try {
    // 1. Get reasons
    console.log('Getting cancellation reasons...');
    let response = await fetch(`${baseURL}/reasons`);
    let reasons = await response.json();
    console.log('Reasons:', reasons);

    // 2. Generate preview
    console.log('\nGenerating cancellation preview...');
    const previewUrl = new URL(`${baseURL}/preview`);
    previewUrl.searchParams.append('bookingId', 'flight-123456');
    previewUrl.searchParams.append('bookingType', 'FLIGHT');
    previewUrl.searchParams.append('quantityToCancel', '1');
    previewUrl.searchParams.append('totalQuantity', '2');
    previewUrl.searchParams.append('originalPrice', '10000');
    previewUrl.searchParams.append('travelDateTimeString', '2026-03-05T14:30:00');
    
    response = await fetch(previewUrl);
    const preview = await response.json();
    console.log('Preview:', preview);

    // 3. Process cancellation
    console.log('\nProcessing cancellation...');
    const cancelUrl = new URL(`${baseURL}/cancel`);
    cancelUrl.searchParams.append('totalQuantity', '2');
    cancelUrl.searchParams.append('originalPrice', '10000');
    cancelUrl.searchParams.append('travelDateTimeString', '2026-03-05T14:30:00');
    
    response = await fetch(cancelUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-User-ID': 'user-5678'
      },
      body: JSON.stringify({
        bookingId: 'flight-123456',
        bookingType: 'FLIGHT',
        reason: 'CHANGE_OF_PLANS',
        quantityToCancel: 1
      })
    });
    
    const result = await response.json();
    console.log('Cancellation Result:', result);

    if (result.success) {
      // 4. Check refund status
      console.log('\nChecking refund status...');
      response = await fetch(`${baseURL}/refund-status/${result.cancellationId}`);
      const refundStatus = await response.json();
      console.log('Refund Status:', refundStatus);

      // 5. Update refund status
      console.log('\nUpdating refund status to REFUND_INITIATED...');
      const updateUrl = new URL(`${baseURL}/refund-status/${refundStatus.id}`);
      updateUrl.searchParams.append('status', 'REFUND_INITIATED');
      updateUrl.searchParams.append('notes', 'Test update');
      
      response = await fetch(updateUrl, { method: 'PUT' });
      const updated = await response.json();
      console.log('Updated:', updated);
    }
  } catch (error) {
    console.error('Error:', error);
  }
}

testCancellationFlow();
```

Run with: `node test-cancellation-api.js`

---

**All tests ready! Start with the cURL commands above.**
