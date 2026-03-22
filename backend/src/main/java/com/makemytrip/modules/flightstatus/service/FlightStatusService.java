package com.makemytrip.modules.flightstatus.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.makemytrip.modules.flightstatus.dto.FlightStatusResponse;
import com.makemytrip.modules.flightstatus.dto.PushSubscriptionRequest;
import com.makemytrip.modules.flightstatus.dto.TimelineResponse;
import com.makemytrip.modules.flightstatus.model.FlightPushSubscription;
import com.makemytrip.modules.flightstatus.model.FlightStatus;
import com.makemytrip.modules.flightstatus.model.FlightStatusEnum;
import com.makemytrip.modules.flightstatus.repository.FlightPushSubscriptionRepository;
import com.makemytrip.modules.flightstatus.repository.FlightStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class FlightStatusService {

    private final FlightStatusRepository repository;
    private final FlightPushSubscriptionRepository pushSubscriptionRepository;
    private final Random random = new Random();
    private static final int DEFAULT_DURATION_MINUTES = 120;
    private final Map<String, String> lastKnownStatusSnapshot = new ConcurrentHashMap<>();

    @Value("${vapid.public.key}")
    private String vapidPublicKey;

    @Value("${vapid.private.key}")
    private String vapidPrivateKey;

    @Value("${vapid.subject:mailto:admin@example.com}")
    private String vapidSubject;

    private static final List<String> DELAY_REASONS = Arrays.asList(
            "Air traffic congestion",
            "Weather conditions",
            "Technical maintenance",
            "Crew scheduling",
            "Late aircraft arrival");

    @PostConstruct
    public void loadMockData() {
        if (repository.count() == 0) {
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
        } else {
            log.info("Flight status data already loaded, skipping mock data init");
        }

        refreshStatusSnapshot();
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
        ensureArrivalTimes(flight);

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
            flight.setEstimatedArrival(flight.getScheduledArrival().plusMinutes(delayMins));
            flight.addTimelineEvent("DELAY_ANNOUNCED",
                    String.format("Delayed by %d minutes - %s", delayMins, flight.getDelayReason()));
        } else if (action < 40 && flight.getStatus() == FlightStatusEnum.DELAYED) {
            // 20% chance: recover from delay
            flight.setStatus(FlightStatusEnum.ON_TIME);
            flight.setDelayMinutes(0);
            flight.setDelayReason(null);
            flight.setEstimatedDeparture(flight.getScheduledDeparture());
            flight.setEstimatedArrival(flight.getScheduledArrival());
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
                flight.setEstimatedArrival(flight.getEstimatedArrival().plusMinutes(additionalDelay));
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

    public List<FlightStatusResponse> listAllLive() {
        return repository.findAll().stream()
                .map(this::mapToResponse)
                .sorted(Comparator.comparing(FlightStatusResponse::getFlightId))
                .toList();
    }

    public void savePushSubscription(PushSubscriptionRequest request) {
        FlightPushSubscription subscription = pushSubscriptionRepository
                .findByEndpointAndFlightId(request.getEndpoint(), request.getFlightId())
                .orElseGet(FlightPushSubscription::new);

        LocalDateTime now = LocalDateTime.now();
        if (subscription.getCreatedAt() == null) {
            subscription.setCreatedAt(now);
        }
        subscription.setUpdatedAt(now);
        subscription.setFlightId(request.getFlightId());
        subscription.setEndpoint(request.getEndpoint());
        subscription.setP256dh(request.getKeys().getP256dh());
        subscription.setAuth(request.getKeys().getAuth());
        pushSubscriptionRepository.save(subscription);
    }

    public String getVapidPublicKey() {
        return vapidPublicKey;
    }

    @Scheduled(fixedRate = 60000) // Every 60 seconds
    public void sendPushNotificationsForStatusChanges() {
        List<FlightStatus> flights = repository.findAll();
        if (flights.isEmpty()) {
            return;
        }

        for (FlightStatus flight : flights) {
            String currentSnapshot = buildNotificationSnapshot(flight);
            String previousSnapshot = lastKnownStatusSnapshot.get(flight.getFlightId());

            if (previousSnapshot == null) {
                lastKnownStatusSnapshot.put(flight.getFlightId(), currentSnapshot);
                continue;
            }

            if (!currentSnapshot.equals(previousSnapshot)) {
                notifySubscribers(flight, previousSnapshot);
                lastKnownStatusSnapshot.put(flight.getFlightId(), currentSnapshot);
            }
        }
    }

    private FlightStatusResponse mapToResponse(FlightStatus flight) {
        ensureArrivalTimes(flight);

        return FlightStatusResponse.builder()
                .flightId(flight.getFlightId())
                .airline(flight.getAirline())
                .origin(flight.getOrigin())
                .destination(flight.getDestination())
                .scheduledDeparture(flight.getScheduledDeparture())
                .estimatedDeparture(flight.getEstimatedDeparture())
                .scheduledArrival(flight.getScheduledArrival())
                .estimatedArrival(flight.getEstimatedArrival())
                .status(flight.getStatus())
                .statusMessage(buildStatusMessage(flight))
                .delayMinutes(flight.getDelayMinutes())
                .delayReason(flight.getDelayReason())
                .arrivalDelayMinutes(calculateArrivalDelayMinutes(flight))
                .estimatedArrivalUpdate(buildEstimatedArrivalUpdate(flight))
                .lastUpdated(flight.getLastUpdated())
                .build();
    }

    private void notifySubscribers(FlightStatus flight, String previousSnapshot) {
        List<FlightPushSubscription> subscriptions = pushSubscriptionRepository.findByFlightId(flight.getFlightId());
        if (subscriptions.isEmpty()) {
            return;
        }

        String payload = buildPushPayload(flight, previousSnapshot);
        PushService pushService;
        try {
            pushService = new PushService()
                    .setPublicKey(vapidPublicKey)
                    .setPrivateKey(vapidPrivateKey)
                    .setSubject(vapidSubject);
        } catch (Exception ex) {
            log.warn("Unable to initialize Web Push service. Check VAPID key configuration.", ex);
            return;
        }

        for (FlightPushSubscription subscription : subscriptions) {
            try {
                Notification notification = new Notification(
                        subscription.getEndpoint(),
                        subscription.getP256dh(),
                        subscription.getAuth(),
                        payload.getBytes(StandardCharsets.UTF_8)
                );
                pushService.send(notification);
            } catch (Exception ex) {
                log.warn("Failed to send push notification for flight {} to endpoint {}", flight.getFlightId(), subscription.getEndpoint(), ex);
            }
        }
    }

    private String buildPushPayload(FlightStatus flight, String previousSnapshot) {
        String previousStatus = previousSnapshot == null
                ? "UNKNOWN"
                : previousSnapshot.split("\\|", 2)[0];
        String title = String.format("%s %s status update", flight.getAirline(), flight.getFlightId());
        String body = String.format("Status changed from %s to %s. %s",
                previousStatus,
                flight.getStatus(),
                buildEstimatedArrivalUpdate(flight));

        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(Map.of(
                    "title", title,
                    "body", body,
                    "flightId", flight.getFlightId(),
                    "status", flight.getStatus().name(),
                    "statusMessage", buildStatusMessage(flight),
                    "delayReason", flight.getDelayReason() == null ? "" : flight.getDelayReason(),
                    "estimatedArrivalUpdate", buildEstimatedArrivalUpdate(flight),
                    "lastUpdated", flight.getLastUpdated() == null ? "" : flight.getLastUpdated().toString()
            ));
        } catch (Exception ex) {
            log.warn("Failed to serialize push payload for flight {}", flight.getFlightId(), ex);
            return "{\"title\":\"Flight status update\",\"body\":\"Flight status has changed\"}";
        }
    }

    private void refreshStatusSnapshot() {
        lastKnownStatusSnapshot.clear();
        repository.findAll().forEach(flight -> lastKnownStatusSnapshot.put(flight.getFlightId(), buildNotificationSnapshot(flight)));
    }

    private void ensureArrivalTimes(FlightStatus flight) {
        if (flight.getScheduledDeparture() == null) {
            return;
        }

        if (flight.getScheduledArrival() == null) {
            flight.setScheduledArrival(flight.getScheduledDeparture().plusMinutes(DEFAULT_DURATION_MINUTES));
        }

        if (flight.getEstimatedDeparture() == null) {
            int delay = flight.getDelayMinutes() == null ? 0 : flight.getDelayMinutes();
            flight.setEstimatedDeparture(flight.getScheduledDeparture().plusMinutes(delay));
        }

        if (flight.getEstimatedArrival() == null) {
            int delay = flight.getDelayMinutes() == null ? 0 : flight.getDelayMinutes();
            flight.setEstimatedArrival(flight.getScheduledArrival().plusMinutes(delay));
        }
    }

    private String buildNotificationSnapshot(FlightStatus flight) {
        Integer delay = flight.getDelayMinutes() == null ? 0 : flight.getDelayMinutes();
        String reason = flight.getDelayReason() == null ? "" : flight.getDelayReason();
        String eta = flight.getEstimatedArrival() == null ? "" : flight.getEstimatedArrival().toString();
        return String.format("%s|%d|%s|%s", flight.getStatus(), delay, reason, eta);
    }

    private String buildStatusMessage(FlightStatus flight) {
        if (flight.getStatus() == FlightStatusEnum.DELAYED) {
            int delayMinutes = flight.getDelayMinutes() == null ? 0 : flight.getDelayMinutes();
            if (delayMinutes >= 60 && delayMinutes % 60 == 0) {
                return String.format("Delayed by %dh", delayMinutes / 60);
            }
            return String.format("Delayed by %d min", delayMinutes);
        }
        return flight.getStatus().name().replace("_", " ").toLowerCase(Locale.ENGLISH);
    }

    private Integer calculateArrivalDelayMinutes(FlightStatus flight) {
        if (flight.getEstimatedArrival() == null || flight.getScheduledArrival() == null) {
            return 0;
        }
        return (int) java.time.Duration.between(flight.getScheduledArrival(), flight.getEstimatedArrival()).toMinutes();
    }

    private String buildEstimatedArrivalUpdate(FlightStatus flight) {
        Integer arrivalDelayMinutes = calculateArrivalDelayMinutes(flight);
        if (arrivalDelayMinutes == null || arrivalDelayMinutes <= 0) {
            return "Estimated arrival remains on schedule";
        }
        if (arrivalDelayMinutes >= 60 && arrivalDelayMinutes % 60 == 0) {
            return String.format("Estimated arrival moved by %dh", arrivalDelayMinutes / 60);
        }
        return String.format("Estimated arrival moved by %d minutes", arrivalDelayMinutes);
    }
}
