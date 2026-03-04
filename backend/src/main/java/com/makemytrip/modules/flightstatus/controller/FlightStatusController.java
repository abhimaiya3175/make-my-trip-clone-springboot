package com.makemytrip.modules.flightstatus.controller;

import com.makemytrip.modules.flightstatus.model.FlightStatus;
import com.makemytrip.modules.flightstatus.service.FlightStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flight-status")
@CrossOrigin(origins = "*")
public class FlightStatusController {
    @Autowired
    private FlightStatusService flightStatusService;

    @GetMapping("/{flightId}")
    public ResponseEntity<FlightStatus> getFlightStatus(@PathVariable String flightId) {
        FlightStatus status = flightStatusService.getFlightStatus(flightId);
        if (status != null) {
            return ResponseEntity.ok(status);
        }
        return ResponseEntity.notFound().build();
    }
}
