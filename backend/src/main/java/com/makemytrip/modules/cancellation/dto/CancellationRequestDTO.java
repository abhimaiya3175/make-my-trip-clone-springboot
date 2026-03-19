package com.makemytrip.modules.cancellation.dto;

import com.makemytrip.modules.cancellation.model.CancellationReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for cancellation request from frontend
 */
public class CancellationRequestDTO {
    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    @NotBlank(message = "Booking type is required")
    @Pattern(regexp = "(?i)FLIGHT|HOTEL", message = "Booking type must be FLIGHT or HOTEL")
    private String bookingType; // FLIGHT or HOTEL

    @NotNull(message = "Cancellation reason is required")
    private CancellationReason reason;

    @Min(value = 1, message = "Quantity to cancel must be at least 1")
    private int quantityToCancel; // for partial cancellation

    @Size(max = 1000, message = "Additional notes cannot exceed 1000 characters")
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
