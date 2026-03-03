# Cancellation System - Dashboard Integration Guide

## Quick Integration Steps

### 1. Update User Booking Model (Optional but Recommended)

To better track booking status, optionally add a status field to your existing `Users.Booking` inner class:

```java
public static class Booking {
    private String type;          // FLIGHT or HOTEL
    private String bookingId;
    private String date;
    private int quantity;         // seats or rooms
    private double totalPrice;
    private String status;        // NEW: CONFIRMED, CANCELLED, PARTIALLY_CANCELLED
    
    // Constructor
    publicBooking() {
        this.status = "CONFIRMED"; // default status
    }
    
    // Getters and Setters
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
```

### 2. Dashboard Component Flow

#### Step 1: Fetch User's Bookings from Dashboard
```typescript
// From existing booking dashboard
async loadUserBookings(userId: string) {
    const response = await fetch(`/api/users/${userId}`);
    const user = await response.json();
    return user.bookings; // List of bookings with type, bookingId, quantity, totalPrice, date
}
```

#### Step 2: Display Cancellation Option
For each booking, show a "Cancel Booking" button/link

#### Step 3: When User Clicks Cancel - Show Preview

```typescript
async function showCancellationPreview(booking) {
    const travelDate = new Date(booking.date); // Parse from booking
    
    const preview = await fetch(
        `/api/cancellation/preview?` +
        `bookingId=${booking.bookingId}&` +
        `bookingType=${booking.type}&` +
        `quantityToCancel=${booking.quantity}&` + // Default to full cancellation
        `totalQuantity=${booking.quantity}&` +
        `originalPrice=${booking.totalPrice}&` +
        `travelDateTimeString=${travelDate.toISOString()}`
    );
    
    const previewData = await preview.json();
    
    // Display to user:
    // - Current booking: {quantity} {booking.type}s for ₹{booking.totalPrice}
    // - Refund if cancelled: ₹{previewData.refundAmount}
    // - Refund policy: {previewData.refundPolicy}
    // - Hours until travel: {previewData.hoursUntilTravel}
}
```

#### Step 4: User Selects Cancellation Reason

Display dropdown with options from:
```typescript
async loadCancellationReasons() {
    const response = await fetch(`/api/cancellation/reasons`);
    return response.json();
    // Returns: { CHANGE_OF_PLANS: "...", FOUND_BETTER_PRICE: "...", ... }
}
```

#### Step 5: Handle Partial Cancellation (Optional)

If booking has multiple units (e.g., 4 rooms), allow user to select how many to cancel:
```typescript
function showPartialCancellationOption(booking) {
    if (booking.quantity > 1) {
        // Show slider or input: "Cancel how many of {quantity} units?"
        // Range: 1 to (quantity - 1)
        // If user selects less than all, update preview with partial amounts
    }
}
```

#### Step 6: Process Cancellation

```typescript
async function processCancellation(
    booking,
    selectedReason,
    quantityToCancel, // Default to booking.quantity for full cancellation
    userId
) {
    const travelDate = new Date(booking.date);
    
    const response = await fetch(
        `/api/cancellation/cancel?` +
        `totalQuantity=${booking.quantity}&` +
        `originalPrice=${booking.totalPrice}&` +
        `travelDateTimeString=${travelDate.toISOString()}`,
        {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-User-ID': userId // From auth token/session
            },
            body: JSON.stringify({
                bookingId: booking.bookingId,
                bookingType: booking.type,
                reason: selectedReason,
                quantityToCancel: quantityToCancel,
                additionalNotes: userNotes // Optional
            })
        }
    );
    
    const result = await response.json();
    
    if (result.success) {
        // Show success message
        console.log(result.message);
        
        // Update booking status in local state
        booking.status = result.newBookingStatus; // CANCELLED or PARTIALLY_CANCELLED
        
        // Show refund details
        console.log(`Refund Amount: ₹${result.refundAmount}`);
        console.log(`Refund Status: ${result.refundTracker.statusDisplay}`);
        
        // Optionally redirect to refund tracking page
        // navigate(`/dashboard/refund/${result.refundTrackerId}`);
    } else {
        // Show error message
        console.error(result.message);
    }
}
```

#### Step 7: Display Refund Status in Dashboard

```typescript
async function loadRefundStatus(userId: string) {
    const response = await fetch(`/api/cancellation/user/${userId}/cancellations`);
    const cancellations = await response.json();
    
    // Display a "My Cancellations" section showing:
    cancellations.forEach(cancellation => {
        console.log({
            bookingId: cancellation.bookingId,
            bookingType: cancellation.bookingType,
            cancelledQuantity: cancellation.cancelledQuantity,
            refundAmount: `₹${cancellation.refundAmount}`,
            refundPercentage: `${cancellation.refundPercentage}%`,
            refundStatus: cancellation.refundTracker?.statusDisplay,
            lastUpdated: cancellation.refundTracker?.updatedAt
        });
    });
}
```

---

## UI Component Examples

### Cancellation Confirmation Dialog

```typescript
// CancellationDialog.tsx

export function CancellationDialog({ booking, onClose, onConfirm }) {
    const [selectedReason, setSelectedReason] = useState<string>('');
    const [quantityToCancel, setQuantityToCancel] = useState(booking.quantity);
    const [preview, setPreview] = useState(null);
    const [loading, setLoading] = useState(false);
    const [reasons, setReasons] = useState([]);

    // Load cancellation reasons
    useEffect(() => {
        fetch('/api/cancellation/reasons')
            .then(r => r.json())
            .then(setReasons);
    }, []);

    // Load cancellation preview when inputs change
    useEffect(() => {
        if (selectedReason) {
            setLoading(true);
            const travelDate = new Date(booking.date);
            fetch(
                `/api/cancellation/preview?` +
                `bookingId=${booking.bookingId}&` +
                `bookingType=${booking.type}&` +
                `quantityToCancel=${quantityToCancel}&` +
                `totalQuantity=${booking.quantity}&` +
                `originalPrice=${booking.totalPrice}&` +
                `travelDateTimeString=${travelDate.toISOString()}`
            )
                .then(r => r.json())
                .then(setPreview)
                .finally(() => setLoading(false));
        }
    }, [selectedReason, quantityToCancel]);

    const handleConfirm = async () => {
        setLoading(true);
        const travelDate = new Date(booking.date);
        const response = await fetch(
            `/api/cancellation/cancel?` +
            `totalQuantity=${booking.quantity}&` +
            `originalPrice=${booking.totalPrice}&` +
            `travelDateTimeString=${travelDate.toISOString()}`,
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    bookingId: booking.bookingId,
                    bookingType: booking.type,
                    reason: selectedReason,
                    quantityToCancel: quantityToCancel
                })
            }
        );
        
        const result = await response.json();
        setLoading(false);
        
        if (result.success) {
            onConfirm(result);
        }
    };

    return (
        <div className="dialog">
            <h2>Cancel {booking.type} Booking</h2>
            
            {/* Booking Details */}
            <div className="booking-info">
                <p>Booking ID: {booking.bookingId}</p>
                <p>Type: {booking.type}</p>
                <p>Units: {booking.quantity}</p>
                <p>Total Price: ₹{booking.totalPrice}</p>
                <p>Travel Date: {booking.date}</p>
            </div>

            {/* Quantity Selection (if multiple units) */}
            {booking.quantity > 1 && (
                <div className="quantity-select">
                    <label>Cancel how many {booking.type}s?</label>
                    <input
                        type="range"
                        min="1"
                        max={booking.quantity}
                        value={quantityToCancel}
                        onChange={(e) => setQuantityToCancel(parseInt(e.target.value))}
                    />
                    <span>{quantityToCancel} of {booking.quantity}</span>
                </div>
            )}

            {/* Cancellation Reason Dropdown */}
            <div className="reason-select">
                <label>Select Cancellation Reason *</label>
                <select
                    value={selectedReason}
                    onChange={(e) => setSelectedReason(e.target.value)}
                    required
                >
                    <option value="">-- Select a reason --</option>
                    {Object.entries(reasons).map(([key, value]) => (
                        <option key={key} value={key}>
                            {value}
                        </option>
                    ))}
                </select>
            </div>

            {/* Refund Preview */}
            {preview && (
                <div className="refund-preview">
                    <h3>Refund Preview</h3>
                    <p className="policy-label">{preview.refundPolicy}</p>
                    
                    <div className="preview-details">
                        <div className="row">
                            <span>Units to Cancel:</span>
                            <strong>{preview.quintityToCancel}</strong>
                        </div>
                        <div className="row">
                            <span>Price per Unit:</span>
                            <strong>₹{preview.pricePerUnit.toFixed(2)}</strong>
                        </div>
                        <div className="row">
                            <span>Cancellation Price:</span>
                            <strong>₹{preview.cancellationPrice.toFixed(2)}</strong>
                        </div>
                        <div className="row highlight">
                            <span>Refund ({preview.refundPercentage}%):</span>
                            <strong style={{ color: '#4CAF50' }}>
                                ₹{preview.refundAmount.toFixed(2)}
                            </strong>
                        </div>
                        <div className="row">
                            <span>Hours Until Travel:</span>
                            <strong>{preview.hoursUntilTravel}</strong>
                        </div>
                    </div>
                </div>
            )}

            {/* Action Buttons */}
            <div className="actions">
                <button
                    onClick={onClose}
                    disabled={loading}
                    className="btn-secondary"
                >
                    Cancel
                </button>
                <button
                    onClick={handleConfirm}
                    disabled={!selectedReason || loading}
                    className="btn-primary"
                >
                    {loading ? 'Processing...' : 'Confirm Cancellation'}
                </button>
            </div>
        </div>
    );
}
```

### Refund Status Widget

```typescript
// RefundStatusWidget.tsx

export function RefundStatusWidget({ userId }) {
    const [cancellations, setCancellations] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        setLoading(true);
        fetch(`/api/cancellation/user/${userId}/cancellations`)
            .then(r => r.json())
            .then(setCancellations)
            .finally(() => setLoading(false));
    }, [userId]);

    const getStatusColor = (status: string) => {
        switch (status) {
            case 'CANCELLATION_REQUESTED': return '#FFC107'; // Orange
            case 'REFUND_INITIATED': return '#2196F3';      // Blue
            case 'PROCESSING': return '#FF9800';             // Orange
            case 'REFUNDED': return '#4CAF50';               // Green
            default: return '#999';
        }
    };

    if (loading) return <div>Loading cancellations...</div>;

    if (cancellations.length === 0) {
        return <div className="empty-state">No cancellations</div>;
    }

    return (
        <div className="refund-status-widget">
            <h3>My Cancellations & Refunds</h3>
            
            {cancellations.map((cancellation) => (
                <div key={cancellation.cancellationId} className="cancellation-card">
                    <div className="header">
                        <span className="booking-id">
                            {cancellation.bookingType} - {cancellation.bookingId}
                        </span>
                        <span className="status-badge">
                            {cancellation.newBookingStatus}
                        </span>
                    </div>

                    <div className="details">
                        {cancellation.partialCancellation ? (
                            <p>
                                Cancelled {cancellation.cancelledQuantity} of{' '}
                                {cancellation.totalQuantity} units
                            </p>
                        ) : (
                            <p>Full cancellation</p>
                        )}
                        <p className="refund-amount">
                            Refund: ₹{cancellation.refundAmount.toFixed(2)}{' '}
                            <span className="percentage">
                                ({cancellation.refundPercentage}%)
                            </span>
                        </p>
                    </div>

                    {cancellation.refundTracker && (
                        <div className="refund-status">
                            <div className="status-line">
                                <span>Status:</span>
                                <span
                                    style={{
                                        color: getStatusColor(
                                            cancellation.refundTracker.status
                                        )
                                    }}
                                >
                                    {cancellation.refundTracker.statusDisplay}
                                </span>
                            </div>
                            <div className="status-timeline">
                                <div className="step completed">
                                    <span className="checkmark">✓</span>
                                    <span>Requested</span>
                                </div>
                                <div
                                    className={`step ${
                                        [
                                            'REFUND_INITIATED',
                                            'PROCESSING',
                                            'REFUNDED'
                                        ].includes(cancellation.refundTracker.status)
                                            ? 'completed'
                                            : ''
                                    }`}
                                >
                                    <span className="checkmark">✓</span>
                                    <span>Initiated</span>
                                </div>
                                <div
                                    className={`step ${
                                        [
                                            'PROCESSING',
                                            'REFUNDED'
                                        ].includes(cancellation.refundTracker.status)
                                            ? 'completed'
                                            : ''
                                    }`}
                                >
                                    <span className="checkmark">✓</span>
                                    <span>Processing</span>
                                </div>
                                <div
                                    className={`step ${
                                        cancellation.refundTracker.status ===
                                        'REFUNDED'
                                            ? 'completed'
                                            : ''
                                    }`}
                                >
                                    <span className="checkmark">✓</span>
                                    <span>Refunded</span>
                                </div>
                            </div>
                            <div className="last-updated">
                                Last updated:{' '}
                                {new Date(
                                    cancellation.refundTracker.updatedAt
                                ).toLocaleDateString()}
                            </div>
                        </div>
                    )}
                </div>
            ))}
        </div>
    );
}
```

---

## CSS Styling (Tailwind/Custom)

```css
.cancellation-dialog {
    max-width: 600px;
    padding: 2rem;
    border-radius: 8px;
    background: white;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.booking-info,
.reason-select,
.quantity-select {
    margin: 1.5rem 0;
    padding: 1rem;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    background: #f9f9f9;
}

.refund-preview {
    background: #e8f5e9;
    border-left: 4px solid #4CAF50;
    padding: 1.5rem;
    border-radius: 4px;
    margin: 1.5rem 0;
}

.preview-details .row {
    display: flex;
    justify-content: space-between;
    padding: 0.75rem 0;
    border-bottom: 1px solid #c8e6c9;
}

.preview-details .row.highlight {
    font-size: 1.1rem;
    font-weight: bold;
    border-bottom: none;
}

.refund-status-widget .cancellation-card {
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 1.5rem;
    margin-bottom: 1rem;
    background: white;
}

.status-timeline {
    display: flex;
    justify-content: space-between;
    margin: 1rem 0;
    padding: 1rem 0;
}

.status-timeline .step {
    flex: 1;
    text-align: center;
    opacity: 0.5;
    transition: opacity 0.3s;
}

.status-timeline .step.completed {
    opacity: 1;
}

.status-timeline .checkmark {
    display: block;
    font-size: 1.5rem;
    color: #4CAF50;
    margin-bottom: 0.5rem;
}
```

---

## Workflow Summary

```
User Dashboard
    ↓
Shows Booking with "Cancel" button
    ↓
Click "Cancel" → Open Cancellation Dialog
    ↓
Select Reason → Fetch Preview
    ↓
Review Refund Amount & Policy
    ↓
Optionally Adjust Quantity (for partial cancellation)
    ↓
Confirm Cancellation → API Call
    ↓
Cancellation Created → RefundTracker Created (Status: CANCELLATION_REQUESTED)
    ↓
Show Success Message with Refund Details
    ↓
Update UI to show booking as CANCELLED/PARTIALLY_CANCELLED
    ↓
Display in "My Cancellations" section with real-time refund status updates
```

---

## Key Points

✅ **No modification** to existing Users/Booking models required (but optional status field recommended)
✅ **Complete separation** of concerns - cancellation system is independent
✅ **All data** stored in new MongoDB collections
✅ **Real-time** refund status tracking
✅ **Automatic** refund calculation based on time policy
✅ **Full & partial** cancellation support
✅ **RESTful APIs** ready for frontend consumption
✅ **Error handling** and validation included

**Ready to integrate into your dashboard!**
