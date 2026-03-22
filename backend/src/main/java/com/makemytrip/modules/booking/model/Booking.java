package com.makemytrip.modules.booking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "bookings")
public class Booking {
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    private String userName;
    
    @Indexed
    private String entityId; // flightId or hotelId
    
    @Indexed
    private EntityType entityType; // FLIGHT or HOTEL
    
    private int quantity; // number of seats or rooms
    private List<String> seatNumbers; // selected flight seats (if applicable)
    private double totalPrice;
    
    // Date fields
    private LocalDate bookingDate;   // When booking was created
    private LocalDate travelDate;    // When travel/stay begins (check-in date for hotels)
    private int numberOfNights;      // Number of nights for hotel bookings
    private LocalDate checkOutDate;  // Auto-calculated check-out date (travelDate + numberOfNights)
    
    // Status fields
    @Indexed
    private BookingStatus bookingStatus;
    private PaymentStatus paymentStatus;
    
    // Flags
    private boolean cancellationAllowed;
    private boolean reviewSubmitted;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Cancellation details
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    
    // Constructor
    public Booking() {
        this.bookingDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.bookingStatus = BookingStatus.PENDING;
        this.paymentStatus = PaymentStatus.PENDING;
        this.cancellationAllowed = true;
        this.reviewSubmitted = false;
    }
    
    // Legacy compatibility fields (for old embedded bookings)
    private String type; // "Flight" or "Hotel"
    private String bookingId; // legacy entityId
    private String date; // legacy travelDate

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public List<String> getSeatNumbers() {
        return seatNumbers;
    }

    public void setSeatNumbers(List<String> seatNumbers) {
        this.seatNumbers = seatNumbers;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public int getNumberOfNights() {
        return numberOfNights;
    }

    public void setNumberOfNights(int numberOfNights) {
        this.numberOfNights = numberOfNights;
        // Auto-calculate checkout date
        if (this.travelDate != null && numberOfNights > 0) {
            this.checkOutDate = this.travelDate.plusDays(numberOfNights);
        }
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public boolean isCancellationAllowed() {
        return cancellationAllowed;
    }

    public void setCancellationAllowed(boolean cancellationAllowed) {
        this.cancellationAllowed = cancellationAllowed;
    }

    public boolean isReviewSubmitted() {
        return reviewSubmitted;
    }

    public void setReviewSubmitted(boolean reviewSubmitted) {
        this.reviewSubmitted = reviewSubmitted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    // Legacy getters/setters for backward compatibility
    public String getType() {
        if (type != null) return type;
        return entityType != null ? entityType.name() : null;
    }

    public void setType(String type) {
        this.type = type;
        if (type != null) {
            this.entityType = type.equalsIgnoreCase("Flight") ? EntityType.FLIGHT : EntityType.HOTEL;
        }
    }

    public String getBookingId() {
        return bookingId != null ? bookingId : entityId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
        this.entityId = bookingId;
    }

    public String getDate() {
        if (date != null) return date;
        return travelDate != null ? travelDate.toString() : null;
    }

    public void setDate(String date) {
        this.date = date;
        if (date != null && !date.isEmpty()) {
            try {
                this.travelDate = LocalDate.parse(date.substring(0, Math.min(10, date.length())));
            } catch (Exception e) {
                // Handle parse error
            }
        }
    }
    
    // Helper method to check if booking is before travel date
    public boolean isBeforeTravelDate() {
        return travelDate != null && LocalDate.now().isBefore(travelDate);
    }
    
    // Helper method to check if booking is on or after travel date
    public boolean isOnOrAfterTravelDate() {
        return travelDate != null && !LocalDate.now().isBefore(travelDate);
    }
}
