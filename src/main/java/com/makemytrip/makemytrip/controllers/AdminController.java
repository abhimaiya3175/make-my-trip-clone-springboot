package com.makemytrip.makemytrip.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.makemytrip.makemytrip.models.Users;
import com.makemytrip.makemytrip.models.Flight;
import com.makemytrip.makemytrip.models.Hotel;
import com.makemytrip.makemytrip.repositories.UserRepository;
import com.makemytrip.makemytrip.repositories.FlightRepository;
import com.makemytrip.makemytrip.repositories.HotelRepository;
import java.util.List;
import java.util.Optional;
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping("/users")
    public ResponseEntity<List<Users>> getallusers(){
        List<Users> users=userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    @PostMapping("/flight")
    public ResponseEntity<Flight> addflight(@RequestBody Flight flight){
        Flight savedFlight = flightRepository.save(flight);
        return ResponseEntity.ok(savedFlight);
    }

    @PostMapping("/hotel")
    public ResponseEntity<Hotel> addhotel(@RequestBody Hotel hotel){
        Hotel savedHotel = hotelRepository.save(hotel);
        return ResponseEntity.ok(savedHotel);
    }
    @PutMapping("flight/{id}")
    public ResponseEntity<Flight> editflight(@PathVariable String id, @RequestBody Flight updatedFlight){
        Optional<Flight> flightOptional = flightRepository.findById(id);
        if(flightOptional.isPresent()){
            Flight flight = flightOptional.orElseThrow();
            flight.setFlightName(updatedFlight.getFlightName());
            flight.setFrom(updatedFlight.getFrom());
            flight.setTo(updatedFlight.getTo());
            flight.setDepartureTime(updatedFlight.getDepartureTime());
            flight.setArrivalTime(updatedFlight.getArrivalTime());
            flight.setPrice(updatedFlight.getPrice());
            flight.setAvailableSeats(updatedFlight.getAvailableSeats());
            flightRepository.save(flight);
            return  ResponseEntity.ok(flight);
        }
        return ResponseEntity.notFound().build();
    }
    @PutMapping("hotel/{id}")
    public ResponseEntity<Hotel> editHotel (@PathVariable String id, @RequestBody Hotel updatedHotel){
        Optional<Hotel> hotelOptional = hotelRepository.findById(id);
        if(hotelOptional.isPresent()){
            Hotel hotel = hotelOptional.orElseThrow();
            hotel.sethotelName(updatedHotel.gethotelName());
            hotel.setLocation(updatedHotel.getLocation());
            hotel.setAvailableRooms(updatedHotel.getAvailableRooms());
            hotel.setPricePerNight(updatedHotel.getPricePerNight());
            hotel.setamenities((updatedHotel.getamenities()));
            hotelRepository.save(hotel);
            return ResponseEntity.ok(hotel);
            }
        return ResponseEntity.notFound().build();
    }

}