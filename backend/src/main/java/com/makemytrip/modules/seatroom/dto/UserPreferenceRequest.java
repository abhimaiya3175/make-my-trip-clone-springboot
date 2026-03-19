package com.makemytrip.modules.seatroom.dto;

import com.makemytrip.modules.seatroom.model.RoomType;
import com.makemytrip.modules.seatroom.model.SeatClass;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserPreferenceRequest {
    @NotBlank(message = "userId is required")
    private String userId;

    private SeatClass preferredSeatClass;
    private String preferredSeatPosition;
    private RoomType preferredRoomType;
    private Integer preferredMaxOccupancy;
}
