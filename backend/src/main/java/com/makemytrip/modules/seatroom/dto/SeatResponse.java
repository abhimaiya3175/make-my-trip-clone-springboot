package com.makemytrip.modules.seatroom.dto;

import com.makemytrip.modules.seatroom.model.SeatClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatResponse {
    private String id;
    private String flightId;
    private String seatNumber;
    private String row;
    private String column;
    private SeatClass seatClass;
    private boolean available;
    private double basePrice;
    private double premiumSurcharge;
    private double effectivePrice;
    private boolean locked;         // locked by another user
    private boolean lockedByMe;     // locked by requesting user
}
