package com.makemytrip.modules.recommendation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_events")
@CompoundIndex(name = "user_event_idx", def = "{'userId': 1, 'eventType': 1, 'createdAt': -1}")
public class UserEvent {
    @Id
    private String id;

    @Indexed
    private String userId;
    private String eventType;    // "VIEW", "SEARCH", "BOOK", "WISHLIST", "CLICK"
    private String entityId;     // flightId or hotelId
    private String entityType;   // "FLIGHT" or "HOTEL"
    private String metadata;     // JSON metadata (search query, etc.)
    private LocalDateTime createdAt;
}
