package com.makemytrip.modules.booking.model;

public enum PaymentStatus {
    PENDING,      // Payment not yet initiated
    PROCESSING,   // Payment in progress
    PAID,         // Payment successful
    FAILED,       // Payment failed
    REFUNDED      // Payment refunded after cancellation
}
