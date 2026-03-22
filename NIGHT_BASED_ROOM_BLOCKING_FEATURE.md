# Night-Based Room Blocking Feature

## Overview
This feature enables hotel room booking with automatic room blocking for the specified number of nights. When a guest books a room, it is automatically blocked for all nights from check-in to check-out date. Upon cancellation or completion of stay, the room is automatically unblocked and becomes available again.

## Feature Specifications

### 1. **Number of Nights Input**
- Users now specify the number of nights they want to stay when booking
- Minimum: 1 night
- Maximum: 365 nights
- Check-out date is automatically calculated as: Check-in Date + Number of Nights

### 2. **Room Blocking on Confirmation**
- When a booking is confirmed (payment processed), the room is blocked for all dates in the stay period
- Blocked dates: Check-in date (inclusive) to Check-out date (exclusive)
- Example: 3-night stay from Jan 1 to Jan 3 blocks dates: Jan 1, Jan 2, Jan 3

### 3. **Room Unblocking on Cancellation**
- When a booking is cancelled, all blocked dates are released
- Room becomes available for new bookings immediately
- Only the user who booked the room can cancel before the travel date

### 4. **Room Unblocking on Stay Completion**
- When the travel date passes, the booking is automatically marked as COMPLETED
- This effectively unblocks the room for future dates
- Past dates remain unblocked; future dates can be booked immediately

## Backend Changes

### Modified Files

#### 1. **Booking Model** - `Booking.java`
```java
// New fields added:
private int numberOfNights;      // Number of nights for hotel bookings
private LocalDate checkOutDate;  // Auto-calculated check-out date
```
- `setNumberOfNights()` automatically calculates `checkOutDate` as: `travelDate + numberOfNights`
- Getter methods provide access to both fields

#### 2. **CreateBookingRequest DTO** - `CreateBookingRequest.java`
```java
@Min(value = 0, message = "Number of nights must be at least 0")
private int numberOfNights;
```
- Accepts `numberOfNights` as input parameter
- Validated to ensure minimum value of 0 (though 0 is converted to 1 in service)

#### 3. **Room Model** - `Room.java`
```java
private List<LocalDate> blockedDates;        // Dates when room is blocked
private HashMap<String, String> dateBookingMap; // Map of date -> bookingId
```
- Tracks which dates are blocked for the room
- Maintains reference to which booking blocked each date (for accurate unblocking)

#### 4. **BookingService** - `BookingService.java`
**New methods:**
- `confirmPayment()`: Enhanced to block room dates when payment is confirmed
  ```java
  // For hotel bookings, blocks the room for the entire stay duration
  seatRoomService.blockRoomDates(booking.getEntityId(), booking.getTravelDate(), 
          booking.getCheckOutDate(), bookingId);
  ```

**Updated methods:**
- `createBooking()`: 
  - Validates `numberOfNights >= 1` for hotel bookings
  - Sets `numberOfNights` which auto-calculates `checkOutDate`
- `cancelBooking()`: 
  - Unblocks room dates when booking is cancelled
  - Calls `seatRoomService.unblockRoomDates()`

#### 5. **SeatRoomService** - `SeatRoomService.java`
**New methods:**
- `blockRoomDates(roomId, checkInDate, checkOutDate, bookingId)`:
  - Blocks all dates from check-in (inclusive) to check-out (exclusive)
  - Stores booking ID for each date for reference
  - Used when booking is confirmed

- `unblockRoomDates(roomId, checkInDate, checkOutDate, bookingId)`:
  - Unblocks all dates blocked by a specific booking
  - Only removes dates if they were blocked by the specified booking
  - Used when booking is cancelled

- `isRoomAvailableForDateRange(roomId, checkInDate, checkOutDate)`:
  - Checks if room is available for entire date range
  - Returns false if any date in range is blocked
  - Used for availability checking before booking

## Frontend Changes

### Modified Files

#### 1. **BookHotelPage** - `book-hotel/[id]/index.tsx`

**New state:**
```typescript
const [numberOfNights, setNumberOfNights] = useState(1);
```

**Updated booking calculation:**
```typescript
const totalPrice = (hotel?.pricePerNight || 0) * quantity * numberOfNights;
const totalTaxes = (hotelData?.room.taxes || 0) * quantity * numberOfNights;
const totalDiscounts = (hotelData?.room.discountedPrice || 0) * quantity * numberOfNights;
const grandTotal = totalPrice + totalTaxes - totalDiscounts;
```

**New UI Component:**
```typescript
{/* Number of Nights */}
<div className="space-y-2">
  <Label htmlFor="nights" className="flex items-center">
    <Home className="w-4 h-4 mr-2" />
    Number of Nights
  </Label>
  <Input
    id="nights"
    type="number"
    min="1"
    max="365"
    value={numberOfNights}
    onChange={(e) => setNumberOfNights(Math.max(1, Number.parseInt(e.target.value) || 1))}
    className="bg-white"
  />
</div>
```

**Enhanced booking flow:**
- Uses new `createBooking()` API endpoint from bookingService
- Passes `numberOfNights` to backend
- Automatically imports `createBooking` from API services

## API Endpoints

### Create Booking (POST /api/bookings)
**Request:**
```json
{
  "entityType": "HOTEL",
  "entityId": "hotel-id",
  "quantity": 1,
  "totalPrice": 5000,
  "travelDate": "2026-04-01",
  "numberOfNights": 3,
  "userName": "Guest Name"
}
```

**Response:**
```json
{
  "id": "booking-id",
  "userId": "user-id",
  "entityId": "hotel-id",
  "entityType": "HOTEL",
  "quantity": 1,
  "totalPrice": 5000,
  "travelDate": "2026-04-01",
  "checkOutDate": "2026-04-04",
  "numberOfNights": 3,
  "bookingStatus": "PENDING",
  "paymentStatus": "PENDING"
}
```

### Confirm Payment (POST /api/bookings/{bookingId}/confirm-payment)
- Triggers room blocking if it's a hotel booking
- Changes booking status to CONFIRMED
- Room becomes blocked for entire stay period

### Cancel Booking (POST /api/bookings/{bookingId}/cancel)
- Unblocks all dates associated with the booking
- Updates booking status to CANCELLED
- Refunds payment if applicable

## Data Flow

### 1. Booking Creation Flow
```
User Input (Hotel ID, Rooms, Nights, Dates)
    ↓
Frontend Validation
    ↓
API Call: POST /api/bookings (with numberOfNights)
    ↓
Backend: BookingService.createBooking()
  - Validates numberOfNights
  - Sets checkOutDate = travelDate + numberOfNights
  - Creates booking in PENDING status
    ↓
Response: Booking created in PENDING state (not yet blocked)
```

### 2. Payment Confirmation Flow
```
User Confirms Payment
    ↓
API Call: POST /api/bookings/{id}/confirm-payment
    ↓
Backend: BookingService.confirmPayment()
  - Updates payment status to PAID
  - Updates booking status to CONFIRMED
  - Calls SeatRoomService.blockRoomDates()
    ↓
SeatRoomService.blockRoomDates()
  - Adds all dates from checkIn to checkOut to blockedDates list
  - Maps each date to bookingId
  - Saves room with blocked dates
    ↓
Response: Booking confirmed, room blocked
```

### 3. Cancellation Flow
```
User Cancels Booking
    ↓
API Call: POST /api/bookings/{id}/cancel
    ↓
Backend: BookingService.cancelBooking()
  - Validates cancellation eligibility (before travel date)
  - Updates booking status to CANCELLED
  - Calls SeatRoomService.unblockRoomDates()
    ↓
SeatRoomService.unblockRoomDates()
  - Removes dates from blockedDates list
  - Removes entries from dateBookingMap
  - Saves room with updated dates
    ↓
Response: Booking cancelled, room unblocked
```

### 4. Stay Completion Flow
```
Travel Date Passes
    ↓
Backend: BookingService.updateBookingBasedOnDate() (called on each booking fetch)
  - Marks booking as COMPLETED (automatic, no explicit unblock call needed)
  - Room dates naturally release over time as past dates don't affect future availability
```

## Database Schema Changes

### Bookings Collection
```json
{
  "_id": "booking-id",
  "userId": "user-id",
  "entityId": "hotel-id",
  "entityType": "HOTEL",
  "quantity": 1,
  "travelDate": "2026-04-01",
  "numberOfNights": 3,
  "checkOutDate": "2026-04-04",
  "bookingStatus": "CONFIRMED",
  "paymentStatus": "PAID",
  "createdAt": "2026-03-21T10:00:00Z",
  "updatedAt": "2026-03-21T10:05:00Z"
}
```

### Rooms Collection
```json
{
  "_id": "room-id",
  "hotelId": "hotel-id",
  "roomNumber": "101",
  "roomType": "STANDARD",
  "available": true,
  "blockedDates": ["2026-04-01", "2026-04-02", "2026-04-03"],
  "dateBookingMap": {
    "2026-04-01": "booking-id",
    "2026-04-02": "booking-id",
    "2026-04-03": "booking-id"
  }
}
```

## Edge Cases & Validations

### 1. **Number of Nights Validation**
- Minimum: 1 night (enforced at frontend and backend)
- Maximum: 365 nights (UI limit, not enforced at backend)
- Invalid values revert to 1

### 2. **Multiple Room Bookings**
- Each room is blocked/unblocked independently
- If booking 2 rooms, each room is blocked for the same dates
- Each room tracks its own blocked dates

### 3. **Overlapping Bookings**
- If Room A is booked for Jan 1-3, a new booking for Jan 2-4 is rejected
- Availability check fails because Jan 2 and 3 are already blocked
- User must choose a different room or dates

### 4. **Partial Unblocking**
- Unblocking only removes dates blocked by the specific booking
- If two bookings overlap (shouldn't happen but handles gracefully), each booking only removes its own dates

### 5. **Cancelled Booking Re-booking**
- When booking is cancelled, dates are immediately available
- Room can be re-booked for same dates by another user immediately

## Testing Strategy

### Backend Unit Tests
1. **BookingService Tests:**
   - `createBooking()` with valid numberOfNights
   - `createBooking()` with invalid numberOfNights (should fail for hotels)
   - `confirmPayment()` triggers room blocking
   - `cancelBooking()` triggers room unblocking

2. **SeatRoomService Tests:**
   - `blockRoomDates()` correctly adds all dates to blockedDates
   - `unblockRoomDates()` correctly removes only specified booking's dates
   - `isRoomAvailableForDateRange()` returns false if any date is blocked

### Frontend Tests
1. **BookHotelPage Component:**
   - NumberOfNights input renders correctly
   - Price calculation includes nights multiplier
   - CreateBooking API call includes numberOfNights

### Integration Tests
1. End-to-end booking flow with night-based blocking
2. Cancellation and room unblocking
3. Multiple bookings for different date ranges

## Deployment Considerations

1. **Data Migration:** Existing bookings won't have `numberOfNights` or `checkOutDate` - default to treat as single-night bookings
2. **Backward Compatibility:** Legacy hotel booking endpoints continue to work
3. **Database Indexing:** Consider indexing on `blockedDates` and `dateBookingMap` for performance
4. **Concurrency:** Room locking mechanism prevents race conditions during booking

## Future Enhancements

1. **Availability Calendar:** Display blocked dates on hotel page before booking
2. **Dynamic Pricing:** Adjust price based on length of stay (longer stays = discounts)
3. **Bulk Room Blocking:** Block multiple rooms at once for group bookings
4. **Blackout Dates:** Admin can set unavailable dates for maintenance, events
5. **Early Check-in/Late Check-out:** Handle special requests for partial-day extensions
6. **Room Swaps:** Allow changing rooms without cancelling if available
