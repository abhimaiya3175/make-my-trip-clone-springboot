package com.makemytrip.modules.flights.controller;

import com.makemytrip.modules.flights.model.Flight;
import com.makemytrip.modules.flights.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
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
