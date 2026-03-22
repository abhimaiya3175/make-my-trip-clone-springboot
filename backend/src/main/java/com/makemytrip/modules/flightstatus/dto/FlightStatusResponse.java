package com.makemytrip.modules.flightstatus.dto;

import com.makemytrip.modules.flightstatus.model.FlightStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightStatusResponse {
    private String flightId;
    private String airline;
    private String origin;
    private String destination;
    private LocalDateTime scheduledDeparture;
    private LocalDateTime estimatedDeparture;
    private LocalDateTime scheduledArrival;
    private LocalDateTime estimatedArrival;
    private FlightStatusEnum status;
    private String statusMessage;
    private Integer delayMinutes;
    private String delayReason;
    private Integer arrivalDelayMinutes;
    private String estimatedArrivalUpdate;
    private LocalDateTime lastUpdated;
}
