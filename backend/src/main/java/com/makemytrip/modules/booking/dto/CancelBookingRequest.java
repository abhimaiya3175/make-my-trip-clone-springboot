package com.makemytrip.modules.booking.dto;

public class CancelBookingRequest {
    private String reason;

    public CancelBookingRequest() {}

    public CancelBookingRequest(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
