package com.makemytrip.modules.flights.controller;

import com.makemytrip.common.api.ApiError;
import com.makemytrip.common.api.ApiResponse;
import com.makemytrip.modules.flights.model.Flight;
import com.makemytrip.modules.flights.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class FlightController {
    @Autowired
    private FlightService flightService;

    @GetMapping("/")
    public String home() {
        return "✅ It's running on port 8080!";
    }

    @GetMapping("/flight")
    public ResponseEntity<List<Flight>> getallflights() {
        List<Flight> flights = flightService.getAllFlights();
        return ResponseEntity.ok(flights);
    }

        @GetMapping("/flight/{id}")
        public ResponseEntity<?> getFlightById(@PathVariable String id) {
        return flightService.getFlightById(id)
            .map(f -> ResponseEntity.ok(
                ApiResponse.ok(f, UUID.randomUUID().toString())))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(
                    new ApiError("NOT_FOUND", "Flight not found", null),
                    UUID.randomUUID().toString())));
        }

    @PostMapping("/admin/flight")
    public ResponseEntity<Flight> addflight(@RequestBody Flight flight) {
        Flight savedFlight = flightService.addFlight(flight);
        return ResponseEntity.ok(savedFlight);
    }

    @PutMapping("/admin/flight/{id}")
    public ResponseEntity<Flight> editflight(@PathVariable String id, @RequestBody Flight updatedFlight) {
        Flight flight = flightService.editFlight(id, updatedFlight);
        if (flight != null) {
            return ResponseEntity.ok(flight);
        }
        return ResponseEntity.notFound().build();
    }
}
