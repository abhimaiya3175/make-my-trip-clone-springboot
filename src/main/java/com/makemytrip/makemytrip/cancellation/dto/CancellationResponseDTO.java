package com.makemytrip.makemytrip.cancellation.dto;

import com.makemytrip.makemytrip.cancellation.model.BookingStatus;

/**
 * DTO for cancellation response - sent to frontend after successful cancellation
 */
public class CancellationResponseDTO {
    private String cancellationId;
    private String bookingId;
    private String bookingType;
    private BookingStatus newBookingStatus;
    private int totalQuantity;
    private int cancelledQuantity;
    private int remainingQuantity;
    private double refundAmount;
    private double refundPercentage;
    private String refundTrackerId;
    private String message;
    private boolean success;
    private boolean partialCancellation;
    private RefundTrackerDTO refundTracker;

    // Constructors
    public CancellationResponseDTO() {
        this.success = true;
    }

    public CancellationResponseDTO(String cancellationId, String bookingId, 
                                  String bookingType, BookingStatus newBookingStatus,
                                  double refundAmount) {
        this();
        this.cancellationId = cancellationId;
        this.bookingId = bookingId;
        this.bookingType = bookingType;
        this.newBookingStatus = newBookingStatus;
        this.refundAmount = refundAmount;
    }

    // Getters and Setters
    public String getCancellationId() {
        return cancellationId;
    }

    public void setCancellationId(String cancellationId) {
        this.cancellationId = cancellationId;
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

    public BookingStatus getNewBookingStatus() {
        return newBookingStatus;
    }

    public void setNewBookingStatus(BookingStatus newBookingStatus) {
        this.newBookingStatus = newBookingStatus;
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

    public String getRefundTrackerId() {
        return refundTrackerId;
    }

    public void setRefundTrackerId(String refundTrackerId) {
        this.refundTrackerId = refundTrackerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isPartialCancellation() {
        return partialCancellation;
    }

    public void setPartialCancellation(boolean partialCancellation) {
        this.partialCancellation = partialCancellation;
    }

    public RefundTrackerDTO getRefundTracker() {
        return refundTracker;
    }

    public void setRefundTracker(RefundTrackerDTO refundTracker) {
        this.refundTracker = refundTracker;
    }
}
