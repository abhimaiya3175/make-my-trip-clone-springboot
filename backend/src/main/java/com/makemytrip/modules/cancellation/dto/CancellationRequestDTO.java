package com.makemytrip.modules.cancellation.dto;

import com.makemytrip.modules.cancellation.model.CancellationReason;

/**
 * DTO for cancellation request from frontend
 */
public class CancellationRequestDTO {
    private String bookingId;
    private String bookingType; // FLIGHT or HOTEL
    private CancellationReason reason;
    private int quantityToCancel; // for partial cancellation
    private String additionalNotes;

    // Constructors
    public CancellationRequestDTO() {}

    public CancellationRequestDTO(String bookingId, String bookingType, 
                                 CancellationReason reason, int quantityToCancel) {
        this.bookingId = bookingId;
        this.bookingType = bookingType;
        this.reason = reason;
        this.quantityToCancel = quantityToCancel;
    }

    // Getters and Setters
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

    public CancellationReason getReason() {
        return reason;
    }

    public void setReason(CancellationReason reason) {
        this.reason = reason;
    }

    public int getQuantityToCancel() {
        return quantityToCancel;
    }

    public void setQuantityToCancel(int quantityToCancel) {
        this.quantityToCancel = quantityToCancel;
    }

    public String getAdditionalNotes() {
        return additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }
}
