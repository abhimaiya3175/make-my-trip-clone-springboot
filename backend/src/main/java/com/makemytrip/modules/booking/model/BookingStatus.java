package com.makemytrip.modules.booking.model;

public enum BookingStatus {
    PENDING,      // Booking created but not confirmed
    CONFIRMED,    // Payment successful, booking confirmed
    COMPLETED,    // Travel date has passed
    CANCELLED     // Booking cancelled by user or system
}
