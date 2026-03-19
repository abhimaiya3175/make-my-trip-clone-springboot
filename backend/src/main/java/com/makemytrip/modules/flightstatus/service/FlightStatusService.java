package com.makemytrip.modules.flightstatus.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.makemytrip.modules.flightstatus.dto.FlightStatusResponse;
import com.makemytrip.modules.flightstatus.dto.TimelineResponse;
import com.makemytrip.modules.flightstatus.model.FlightStatus;
import com.makemytrip.modules.flightstatus.model.FlightStatusEnum;
import com.makemytrip.modules.flightstatus.repository.FlightStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FlightStatusService {

    private final FlightStatusRepository repository;
    private final Random random = new Random();

    private static final List<String> DELAY_REASONS = Arrays.asList(
            "Air traffic congestion",
            "Weather conditions",
            "Technical maintenance",
            "Crew scheduling",
            "Late aircraft arrival");

    @PostConstruct
    public void loadMockData() {
        if (repository.count() > 0) {
            log.info("Flight status data already loaded, skipping mock data init");
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());

            ClassPathResource resource = new ClassPathResource("mock/flight-status.mock.json");
            List<FlightStatus> mockFlights = mapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<List<FlightStatus>>() {
                    });

            repository.saveAll(mockFlights);
            log.info("Loaded {} mock flight statuses", mockFlights.size());
        } catch (IOException e) {
            log.error("Failed to load mock flight status data", e);
        }
    }

    @Scheduled(fixedRate = 45000) // Every 45 seconds
    public void simulateLiveUpdates() {
        List<FlightStatus> allFlights = repository.findAll();
        if (allFlights.isEmpty())
            return;

        // Randomly mutate 1-2 flights
        int numUpdates = random.nextInt(2) + 1;
        for (int i = 0; i < numUpdates && i < allFlights.size(); i++) {
            FlightStatus flight = allFlights.get(random.nextInt(allFlights.size()));
            mutateFlightStatus(flight);
            repository.save(flight);
            log.debug("Updated status for flight {}: {}", flight.getFlightId(), flight.getStatus());
        }
    }

    private void mutateFlightStatus(FlightStatus flight) {
        // Skip if already landed or cancelled
        if (flight.getStatus() == FlightStatusEnum.LANDED ||
                flight.getStatus() == FlightStatusEnum.CANCELLED) {
            return;
        }

        int action = random.nextInt(100);

        if (action < 20 && flight.getStatus() == FlightStatusEnum.ON_TIME) {
            // 20% chance: add delay
            int delayMins = (random.nextInt(6) + 1) * 15; // 15-90 mins
            flight.setStatus(FlightStatusEnum.DELAYED);
            flight.setDelayMinutes(delayMins);
            flight.setDelayReason(DELAY_REASONS.get(random.nextInt(DELAY_REASONS.size())));
            flight.setEstimatedDeparture(flight.getScheduledDeparture().plusMinutes(delayMins));
            flight.addTimelineEvent("DELAY_ANNOUNCED",
                    String.format("Delayed by %d minutes - %s", delayMins, flight.getDelayReason()));
        } else if (action < 40 && flight.getStatus() == FlightStatusEnum.DELAYED) {
            // 20% chance: recover from delay
            flight.setStatus(FlightStatusEnum.ON_TIME);
            flight.setDelayMinutes(0);
            flight.setDelayReason(null);
            flight.setEstimatedDeparture(flight.getScheduledDeparture());
            flight.addTimelineEvent("DELAY_CLEARED", "Delay cleared, back on schedule");
        } else if (action < 60 && flight.getStatus() != FlightStatusEnum.BOARDING) {
            // 20% chance: start boarding
            flight.setStatus(FlightStatusEnum.BOARDING);
            flight.addTimelineEvent("BOARDING_STARTED",
                    String.format("Boarding started at Gate %d", random.nextInt(20) + 1));
        } else if (action < 70) {
            // 10% chance: increase delay
            if (flight.getDelayMinutes() != null && flight.getDelayMinutes() > 0) {
                int additionalDelay = 15;
                flight.setDelayMinutes(flight.getDelayMinutes() + additionalDelay);
                flight.setEstimatedDeparture(flight.getEstimatedDeparture().plusMinutes(additionalDelay));
                flight.addTimelineEvent("DELAY_EXTENDED",
                        String.format("Delay extended by %d minutes", additionalDelay));
            }
        }
    }

    public FlightStatusResponse getStatus(String flightId) {
        FlightStatus flight = repository.findByFlightId(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + flightId));

        return mapToResponse(flight);
    }

    public TimelineResponse getTimeline(String flightId) {
        FlightStatus flight = repository.findByFlightId(flightId)
                .orElseThrow(() -> new RuntimeException("Flight not found: " + flightId));

        return TimelineResponse.builder()
                .flightId(flight.getFlightId())
                .timeline(flight.getTimeline())
                .build();
    }

    public Page<FlightStatusResponse> listAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::mapToResponse);
    }

    private FlightStatusResponse mapToResponse(FlightStatus flight) {
        return FlightStatusResponse.builder()
                .flightId(flight.getFlightId())
                .airline(flight.getAirline())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .scheduledDeparture(flight.getScheduledDeparture())
                .estimatedDeparture(flight.getEstimatedDeparture())
                .status(flight.getStatus())
                .delayMinutes(flight.getDelayMinutes())
                .delayReason(flight.getDelayReason())
                .lastUpdated(flight.getLastUpdated())
                .build();
    }
}
