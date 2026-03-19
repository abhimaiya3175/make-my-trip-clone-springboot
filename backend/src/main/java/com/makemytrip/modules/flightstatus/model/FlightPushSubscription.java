package com.makemytrip.modules.flightstatus.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "flight_push_subscriptions")
public class FlightPushSubscription {
    @Id
    private String id;

    @Indexed
    private String flightId;

    @Indexed
    private String endpoint;

    private String p256dh;
    private String auth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
