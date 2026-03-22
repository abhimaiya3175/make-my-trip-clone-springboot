package com.makemytrip.modules.seatroom.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "rooms")
@CompoundIndex(name = "hotel_room_idx", def = "{'hotelId': 1, 'roomNumber': 1}", unique = true)
public class Room {
    @Id
    private String id;

    private String hotelId;
    private String roomNumber;
    private RoomType roomType;       // STANDARD, DELUXE, SUITE, PENTHOUSE
    private boolean available;
    private double pricePerNight;
    private int maxOccupancy;
    private List<String> amenities;  // e.g. ["WiFi", "Mini Bar", "Balcony"]
    private List<String> images;     // URLs for room preview carousel
    private boolean isPanorama;      // true = treat first image as 360-degree equirectangular

    // Lock fields for optimistic concurrency
    private String lockedByUserId;
    private LocalDateTime lockedUntil;

    // Booking blocking fields (for night-based blocking)
    private List<LocalDate> blockedDates;        // Dates when room is blocked
    private HashMap<String, String> dateBookingMap; // Map of date -> bookingId for reference
    
    @Version
    private Long version;

    public boolean isLocked() {
        return lockedByUserId != null && lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
}
