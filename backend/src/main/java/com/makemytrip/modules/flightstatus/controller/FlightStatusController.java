package com.makemytrip.modules.flightstatus.controller;

import com.makemytrip.modules.flightstatus.dto.FlightStatusResponse;
import com.makemytrip.modules.flightstatus.dto.PushSubscriptionRequest;
import com.makemytrip.modules.flightstatus.dto.TimelineResponse;
import com.makemytrip.modules.flightstatus.service.FlightStatusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/flight-status")
@RequiredArgsConstructor
public class FlightStatusController {
    
    private final FlightStatusService service;
    
    @GetMapping("/{flightId}")
    public ResponseEntity<?> getFlightStatus(@PathVariable String flightId) {
        try {
            FlightStatusResponse response = service.getStatus(flightId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/{flightId}/timeline")
    public ResponseEntity<?> getTimeline(@PathVariable String flightId) {
        try {
            TimelineResponse response = service.getTimeline(flightId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping
    public ResponseEntity<Page<FlightStatusResponse>> listAllStatuses(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<FlightStatusResponse> response = service.listAll(PageRequest.of(page, size));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/live")
    public ResponseEntity<List<FlightStatusResponse>> listAllLiveStatuses() {
        return ResponseEntity.ok(service.listAllLive());
    }

    @PostMapping("/subscribe")
    public ResponseEntity<Map<String, String>> subscribe(@Valid @RequestBody PushSubscriptionRequest request) {
        service.savePushSubscription(request);
        return ResponseEntity.ok(Map.of("message", "Subscription saved"));
    }

    @GetMapping("/vapid-public-key")
    public ResponseEntity<Map<String, String>> getVapidPublicKey() {
        return ResponseEntity.ok(Map.of("publicKey", service.getVapidPublicKey()));
    }
    
    // Simple error response class
    private record ErrorResponse(String message) {}
}
