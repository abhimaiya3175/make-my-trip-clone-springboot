package com.makemytrip.makemytrip.cancellation.model;

/**
 * Enum representing refund status tracking stages
 */
public enum RefundStatus {
    CANCELLATION_REQUESTED("Cancellation Requested"),
    REFUND_INITIATED("Refund Initiated"),
    PROCESSING("Processing"),
    REFUNDED("Refunded");

    private final String displayName;

    RefundStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
