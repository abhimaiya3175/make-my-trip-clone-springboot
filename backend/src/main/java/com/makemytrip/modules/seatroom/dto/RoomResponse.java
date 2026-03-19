package com.makemytrip.modules.seatroom.dto;

import com.makemytrip.modules.seatroom.model.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomResponse {
    private String id;
    private String hotelId;
    private String roomNumber;
    private RoomType roomType;
    private boolean available;
    private double pricePerNight;
    private int maxOccupancy;
    private List<String> amenities;
    private List<String> images;
    private boolean isPanorama;
    private boolean locked;
    private boolean lockedByMe;
}
