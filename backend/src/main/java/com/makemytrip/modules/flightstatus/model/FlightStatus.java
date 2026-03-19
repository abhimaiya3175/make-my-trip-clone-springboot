package com.makemytrip.modules.flightstatus.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "flight_status")
public class FlightStatus {
    @Id
    private String id;
    @Indexed(unique = true)
    private String flightId;
    private String airline;
    private String origin;
    private String destination;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime estimatedDeparture;
    private FlightStatusEnum status;
    private Integer delayMinutes;
    private String delayReason;
    private LocalDateTime lastUpdated;
    
    @Builder.Default
    private List<TimelineEvent> timeline = new ArrayList<>();
    
    public void addTimelineEvent(String event, String detail) {
        if (timeline == null) {
            timeline = new ArrayList<>();
        }
        timeline.add(TimelineEvent.builder()
            .timestamp(LocalDateTime.now())
            .event(event)
            .detail(detail)
            .build());
        this.lastUpdated = LocalDateTime.now();
    }
}
