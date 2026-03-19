package com.makemytrip.modules.flightstatus.dto;

import com.makemytrip.modules.flightstatus.model.TimelineEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineResponse {
    private String flightId;
    private List<TimelineEvent> timeline;
}
