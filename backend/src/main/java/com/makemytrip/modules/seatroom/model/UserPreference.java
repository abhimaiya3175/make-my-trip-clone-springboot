package com.makemytrip.modules.seatroom.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_preferences")
public class UserPreference {
    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    // Seat preferences
    private SeatClass preferredSeatClass;
    private String preferredSeatPosition; // "WINDOW", "AISLE", "MIDDLE"

    // Room preferences
    private RoomType preferredRoomType;
    private Integer preferredMaxOccupancy;
}
