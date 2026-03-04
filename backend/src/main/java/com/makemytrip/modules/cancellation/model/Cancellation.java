package com.makemytrip.modules.cancellation.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Cancellation entity - stores cancellation request details
 */
@Document(collection = "cancellations")
public class Cancellation {
    @Id
    private String id;
    private String userId;
    private String bookingId;
    private String bookingType; // FLIGHT or HOTEL
    private BookingStatus bookingStatus;
    private CancellationReason reason;
    private int totalQuantity; // total seats/rooms in original booking
    private int cancelledQuantity; // quantity being cancelled
    private int remainingQuantity; // quantity remaining after cancellation
    private double originalPrice;
    private double refundAmount;
    private double refundPercentage;
    private LocalDateTime travelDate;
    private LocalDateTime cancellationRequestedAt;
    private LocalDateTime travelDateTime;
    private String refundTrackerId;
    private boolean partialCancellation;
    private String additionalNotes;

    public Cancellation() {
        this.cancellationRequestedAt = LocalDateTime.now();
        this.bookingStatus = BookingStatus.CONFIRMED;
    }

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

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingType() {
        return bookingType;
    }

    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public CancellationReason getReason() {
        return reason;
    }

    public void setReason(CancellationReason reason) {
        this.reason = reason;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public int getCancelledQuantity() {
        return cancelledQuantity;
    }

    public void setCancelledQuantity(int cancelledQuantity) {
        this.cancelledQuantity = cancelledQuantity;
    }

    public int getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(int remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public double getRefundPercentage() {
        return refundPercentage;
    }

    public void setRefundPercentage(double refundPercentage) {
        this.refundPercentage = refundPercentage;
    }

    public LocalDateTime getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDateTime travelDate) {
        this.travelDate = travelDate;
    }

    public LocalDateTime getCancellationRequestedAt() {
        return cancellationRequestedAt;
    }

    public void setCancellationRequestedAt(LocalDateTime cancellationRequestedAt) {
        this.cancellationRequestedAt = cancellationRequestedAt;
    }

    public LocalDateTime getTravelDateTime() {
        return travelDateTime;
    }

    public void setTravelDateTime(LocalDateTime travelDateTime) {
        this.travelDateTime = travelDateTime;
    }

    public String getRefundTrackerId() {
        return refundTrackerId;
    }

    public void setRefundTrackerId(String refundTrackerId) {
        this.refundTrackerId = refundTrackerId;
    }

    public boolean isPartialCancellation() {
        return partialCancellation;
    }

    public void setPartialCancellation(boolean partialCancellation) {
        this.partialCancellation = partialCancellation;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
}
