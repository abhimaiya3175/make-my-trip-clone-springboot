package com.makemytrip.modules.flightstatus;

import com.makemytrip.modules.flightstatus.dto.FlightStatusResponse;
import com.makemytrip.modules.flightstatus.dto.TimelineResponse;
import com.makemytrip.modules.flightstatus.model.FlightStatus;
import com.makemytrip.modules.flightstatus.model.FlightStatusEnum;
import com.makemytrip.modules.flightstatus.model.TimelineEvent;
import com.makemytrip.modules.flightstatus.repository.FlightStatusRepository;
import com.makemytrip.modules.flightstatus.service.FlightStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightStatusServiceTest {

    @Mock private FlightStatusRepository repository;
    @InjectMocks private FlightStatusService service;

    private FlightStatus sampleFlight() {
        return FlightStatus.builder()
                .id("id-1")
                .flightId("6E-2024")
                .airline("IndiGo")
                .origin("BLR")
                .destination("DEL")
                .scheduledDeparture(LocalDateTime.of(2026, 3, 10, 8, 0))
                .scheduledArrival(LocalDateTime.of(2026, 3, 10, 10, 0))
                .estimatedDeparture(LocalDateTime.of(2026, 3, 10, 8, 0))
                .estimatedArrival(LocalDateTime.of(2026, 3, 10, 10, 0))
                .status(FlightStatusEnum.ON_TIME)
                .delayMinutes(0)
                .lastUpdated(LocalDateTime.now())
                .timeline(List.of(
                        TimelineEvent.builder()
                                .timestamp(LocalDateTime.now())
                                .event("SCHEDULED")
                                .detail("Flight scheduled")
                                .build()
                ))
                .build();
    }

    @Test
    void getStatus_returnsResponse() {
        FlightStatus fs = sampleFlight();
        when(repository.findByFlightId("6E-2024")).thenReturn(Optional.of(fs));

        FlightStatusResponse result = service.getStatus("6E-2024");

        assertThat(result.getFlightId()).isEqualTo("6E-2024");
        assertThat(result.getAirline()).isEqualTo("IndiGo");
        assertThat(result.getStatus()).isEqualTo(FlightStatusEnum.ON_TIME);
        assertThat(result.getOrigin()).isEqualTo("BLR");
        assertThat(result.getDestination()).isEqualTo("DEL");
    }

    @Test
    void getStatus_notFound_throwsException() {
        when(repository.findByFlightId("NONEXIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatus("NONEXIST"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getTimeline_returnsEvents() {
        FlightStatus fs = sampleFlight();
        when(repository.findByFlightId("6E-2024")).thenReturn(Optional.of(fs));

        TimelineResponse result = service.getTimeline("6E-2024");

        assertThat(result.getFlightId()).isEqualTo("6E-2024");
        assertThat(result.getTimeline()).isNotEmpty();
        assertThat(result.getTimeline().get(0).getEvent()).isEqualTo("SCHEDULED");
    }

    @Test
    void getTimeline_notFound_throwsException() {
        when(repository.findByFlightId("GONE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getTimeline("GONE"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void getStatus_delayedFlight_returnsDelayInfo() {
        FlightStatus fs = sampleFlight();
        fs.setStatus(FlightStatusEnum.DELAYED);
        fs.setDelayMinutes(45);
        fs.setDelayReason("Weather conditions");
        fs.setEstimatedDeparture(fs.getScheduledDeparture().plusMinutes(45));
        fs.setEstimatedArrival(fs.getScheduledArrival().plusMinutes(45));
        when(repository.findByFlightId("6E-2024")).thenReturn(Optional.of(fs));

        FlightStatusResponse result = service.getStatus("6E-2024");

        assertThat(result.getStatus()).isEqualTo(FlightStatusEnum.DELAYED);
        assertThat(result.getDelayMinutes()).isEqualTo(45);
        assertThat(result.getDelayReason()).isEqualTo("Weather conditions");
        assertThat(result.getEstimatedArrival())
            .isEqualTo(result.getScheduledArrival().plusMinutes(result.getDelayMinutes()));
    }
}
