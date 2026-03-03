package com.makemytrip.makemytrip.cancellation.model;

/**
 * Enum representing all possible cancellation reasons
 */
public enum CancellationReason {
    CHANGE_OF_PLANS("Change of plans"),
    FOUND_BETTER_PRICE("Found better price"),
    MEDICAL_REASON("Medical reason"),
    BOOKING_MISTAKE("Booking mistake"),
    OTHER("Other");

    private final String displayName;

    CancellationReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
