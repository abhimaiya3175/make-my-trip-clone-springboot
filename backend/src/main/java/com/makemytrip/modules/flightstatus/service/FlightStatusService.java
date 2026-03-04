package com.makemytrip.modules.flightstatus.service;

import com.makemytrip.modules.flights.model.Flight;
import com.makemytrip.modules.flights.repository.FlightRepository;
import com.makemytrip.modules.flightstatus.model.FlightStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FlightStatusService {
    @Autowired
    private FlightRepository flightRepository;

    public FlightStatus getFlightStatus(String flightId) {
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        if (flightOpt.isPresent()) {
            Flight flight = flightOpt.get();
            FlightStatus status = new FlightStatus();
            status.setFlightId(flightId);
            status.setFlightName(flight.getFlightName());
            status.setDepartureTime(flight.getDepartureTime());
            status.setArrivalTime(flight.getArrivalTime());
            status.setStatus("ON_TIME");
            status.setDelayMinutes(0);
            return status;
        }
        return null;
    }
}
