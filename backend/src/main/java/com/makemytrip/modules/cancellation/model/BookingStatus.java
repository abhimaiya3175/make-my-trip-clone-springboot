package com.makemytrip.modules.cancellation.model;

/**
 * Enum representing booking status
 */
public enum BookingStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    PARTIALLY_CANCELLED("Partially Cancelled");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
