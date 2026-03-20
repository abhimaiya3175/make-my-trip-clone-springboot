package com.makemytrip.modules.flights.service;

import com.makemytrip.modules.flights.model.Flight;
import com.makemytrip.modules.flights.repository.FlightRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class FlightService {
    @Autowired
    private FlightRepository flightRepository;

    @PostConstruct
    public void seedUpcomingFlightsIfMissing() {
        List<Flight> existingFlights = flightRepository.findAll();
        long upcomingFlightsCount = existingFlights.stream()
                .map(Flight::getDepartureTime)
            .filter(this::isTodayOrFutureDeparture)
            .count();

        if (upcomingFlightsCount >= 36) {
            return;
        }

        LocalDate baseDate = LocalDate.now().plusDays(1);
        List<Flight> upcomingFlights = List.of(
                createFlight("Air India AI-101", "Delhi", "Bangalore", baseDate, "08:00", "10:30", 5000, 42),
                createFlight("IndiGo 6E-212", "Delhi", "Mumbai", baseDate, "09:15", "11:20", 4600, 38),
                createFlight("Vistara UK-933", "Bangalore", "Delhi", baseDate.plusDays(1), "07:40", "10:25", 5400, 30),
                createFlight("SpiceJet SG-451", "Mumbai", "Bangalore", baseDate.plusDays(1), "14:00", "15:45", 4300, 26),
                createFlight("Air India AI-302", "Delhi", "Kolkata", baseDate.plusDays(2), "06:30", "08:40", 5200, 34),
                createFlight("IndiGo 6E-778", "Kolkata", "Delhi", baseDate.plusDays(2), "19:10", "21:25", 5100, 29),
                createFlight("Vistara UK-514", "Bangalore", "Mumbai", baseDate.plusDays(3), "11:20", "13:00", 4700, 31),
            createFlight("Air India AI-709", "Mumbai", "Delhi", baseDate.plusDays(3), "17:30", "19:45", 4950, 35),
            createFlight("Akasa QP-142", "Delhi", "Goa", baseDate.plusDays(4), "06:20", "08:50", 5200, 33),
            createFlight("IndiGo 6E-931", "Goa", "Delhi", baseDate.plusDays(4), "21:10", "23:35", 5100, 27),
            createFlight("Vistara UK-645", "Mumbai", "Kolkata", baseDate.plusDays(5), "10:05", "12:35", 5600, 36),
            createFlight("Air India AI-558", "Kolkata", "Mumbai", baseDate.plusDays(5), "16:40", "19:10", 5550, 28),
            createFlight("SpiceJet SG-288", "Bangalore", "Hyderabad", baseDate.plusDays(1), "12:15", "13:30", 3200, 41),
            createFlight("IndiGo 6E-564", "Hyderabad", "Bangalore", baseDate.plusDays(2), "18:00", "19:10", 3300, 39),
            createFlight("Air India AI-411", "Delhi", "Chennai", baseDate.plusDays(6), "07:55", "10:40", 6100, 32),
            createFlight("Vistara UK-872", "Chennai", "Delhi", baseDate.plusDays(6), "19:30", "22:20", 6050, 30),
            createFlight("Akasa QP-301", "Mumbai", "Ahmedabad", baseDate.plusDays(2), "08:35", "09:50", 3000, 45),
            createFlight("IndiGo 6E-447", "Ahmedabad", "Mumbai", baseDate.plusDays(3), "20:10", "21:25", 3050, 40),
            createFlight("Air India AI-226", "Delhi", "Pune", baseDate.plusDays(5), "13:25", "15:35", 4800, 37),
            createFlight("SpiceJet SG-612", "Pune", "Delhi", baseDate.plusDays(5), "18:45", "20:55", 4700, 34),
            createFlight("IndiGo 6E-104", "Delhi", "Jaipur", baseDate.plusDays(1), "06:10", "07:15", 2600, 46),
            createFlight("Air India AI-557", "Jaipur", "Delhi", baseDate.plusDays(1), "21:20", "22:25", 2750, 44),
            createFlight("Vistara UK-321", "Mumbai", "Goa", baseDate.plusDays(2), "09:45", "11:00", 3600, 40),
            createFlight("Akasa QP-809", "Goa", "Mumbai", baseDate.plusDays(2), "17:25", "18:40", 3550, 39),
            createFlight("Air India AI-904", "Bangalore", "Chennai", baseDate.plusDays(4), "07:15", "08:20", 2900, 43),
            createFlight("IndiGo 6E-612", "Chennai", "Bangalore", baseDate.plusDays(4), "20:35", "21:40", 3000, 41),
            createFlight("SpiceJet SG-731", "Hyderabad", "Mumbai", baseDate.plusDays(3), "08:20", "09:50", 4100, 35),
            createFlight("Vistara UK-742", "Mumbai", "Hyderabad", baseDate.plusDays(3), "19:10", "20:35", 4200, 33),
            createFlight("Air India AI-118", "Delhi", "Lucknow", baseDate.plusDays(6), "10:30", "11:45", 3100, 42),
            createFlight("IndiGo 6E-221", "Lucknow", "Delhi", baseDate.plusDays(6), "16:25", "17:40", 3150, 40),
            createFlight("Akasa QP-655", "Pune", "Bangalore", baseDate.plusDays(7), "06:50", "08:20", 3900, 36),
            createFlight("Air India AI-441", "Bangalore", "Pune", baseDate.plusDays(7), "20:10", "21:40", 3950, 35),
            createFlight("IndiGo 6E-878", "Kolkata", "Chennai", baseDate.plusDays(8), "11:55", "14:15", 5200, 29),
            createFlight("Vistara UK-889", "Chennai", "Kolkata", baseDate.plusDays(8), "18:05", "20:20", 5250, 28),
            createFlight("SpiceJet SG-147", "Delhi", "Ahmedabad", baseDate.plusDays(9), "05:40", "07:15", 3400, 38),
            createFlight("Air India AI-248", "Ahmedabad", "Delhi", baseDate.plusDays(9), "21:00", "22:35", 3450, 37)
        );

        flightRepository.saveAll(upcomingFlights);
    }

    private boolean isTodayOrFutureDeparture(String departureTime) {
        if (departureTime == null || departureTime.length() < 10) {
            return false;
        }

        try {
            LocalDate departureDate = LocalDate.parse(departureTime.substring(0, 10));
            return !departureDate.isBefore(LocalDate.now());
        } catch (Exception ex) {
            return false;
        }
    }

    private Flight createFlight(String flightName,
                                String from,
                                String to,
                                LocalDate date,
                                String departure,
                                String arrival,
                                double price,
                                int seats) {
        Flight flight = new Flight();
        flight.setFlightName(flightName);
        flight.setFrom(from);
        flight.setTo(to);
        flight.setDepartureTime(date + "T" + departure + ":00");
        flight.setArrivalTime(date + "T" + arrival + ":00");
        flight.setPrice(price);
        flight.setAvailableSeats(seats);
        return flight;
    }

    public List<Flight> getAllFlights() {
        return flightRepository.findAll();
    }

    public Optional<Flight> getFlightById(String id) {
        return flightRepository.findById(id);
    }

    public Flight addFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public Flight editFlight(String id, Flight updatedFlight) {
        Optional<Flight> flightOptional = flightRepository.findById(id);
        if (flightOptional.isPresent()) {
            Flight flight = flightOptional.get();
            flight.setFlightName(updatedFlight.getFlightName());
            flight.setFrom(updatedFlight.getFrom());
            flight.setTo(updatedFlight.getTo());
            flight.setDepartureTime(updatedFlight.getDepartureTime());
            flight.setArrivalTime(updatedFlight.getArrivalTime());
            flight.setPrice(updatedFlight.getPrice());
            flight.setAvailableSeats(updatedFlight.getAvailableSeats());
            return flightRepository.save(flight);
        }
        return null;
    }
}
