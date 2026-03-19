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

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "seats")
@CompoundIndex(name = "flight_seat_idx", def = "{'flightId': 1, 'seatNumber': 1}", unique = true)
public class Seat {
    @Id
    private String id;

    private String flightId;
    private String seatNumber;   // e.g. "1A", "12F"
    private String row;          // e.g. "1", "12"
    private String column;       // e.g. "A", "F"
    private SeatClass seatClass; // ECONOMY, BUSINESS, FIRST
    private boolean available;
    private double basePrice;
    private double premiumSurcharge; // extra for window/exit-row/front

    // Lock fields for optimistic concurrency
    private String lockedByUserId;
    private LocalDateTime lockedUntil;

    @Version
    private Long version;

    public double getEffectivePrice() {
        return basePrice + premiumSurcharge;
    }

    public boolean isLocked() {
        return lockedByUserId != null && lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }
}
