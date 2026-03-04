package com.makemytrip.modules.booking.service;

import com.makemytrip.modules.auth.model.User;
import com.makemytrip.modules.auth.repository.UserRepository;
import com.makemytrip.modules.booking.model.Booking;
import com.makemytrip.modules.flights.model.Flight;
import com.makemytrip.modules.flights.repository.FlightRepository;
import com.makemytrip.modules.hotels.model.Hotel;
import com.makemytrip.modules.hotels.repository.HotelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class BookingService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private HotelRepository hotelRepository;

    public Booking bookFlight(String userId, String flightId, int seats, double price, String date) {
        Optional<User> usersOptional = userRepository.findById(userId);
        Optional<Flight> flightOptional = flightRepository.findById(flightId);
        if (usersOptional.isPresent() && flightOptional.isPresent()) {
            User user = usersOptional.get();
            Flight flight = flightOptional.get();
            if (flight.getAvailableSeats() >= seats) {
                flight.setAvailableSeats(flight.getAvailableSeats() - seats);
                flightRepository.save(flight);

                Booking booking = new Booking();
                booking.setType("Flight");
                booking.setBookingId(flightId);
                if (date != null && !date.isEmpty()) {
                    booking.setDate(date.length() > 10 ? date.substring(0, 10) : date);
                } else if (flight.getDepartureTime() != null && !flight.getDepartureTime().isEmpty()) {
                    String depDate = flight.getDepartureTime().length() > 10
                        ? flight.getDepartureTime().substring(0, 10)
                        : flight.getDepartureTime();
                    booking.setDate(depDate);
                } else {
                    booking.setDate(LocalDate.now().toString());
                }
                booking.setQuantity(seats);
                booking.setTotalPrice(price);
                user.getBookings().add(booking);
                userRepository.save(user);
                return booking;
            } else {
                throw new RuntimeException("Not enough seats available");
            }
        }
        throw new RuntimeException("User or flight not found");
    }

    public Booking bookhotel(String userId, String hotelId, int rooms, double price, String date) {
        Optional<User> usersOptional = userRepository.findById(userId);
        Optional<Hotel> hotelOptional = hotelRepository.findById(hotelId);
        if (usersOptional.isPresent() && hotelOptional.isPresent()) {
            User user = usersOptional.get();
            Hotel hotel = hotelOptional.get();
            if (hotel.getAvailableRooms() >= rooms) {
                hotel.setAvailableRooms(hotel.getAvailableRooms() - rooms);
                hotelRepository.save(hotel);

                Booking booking = new Booking();
                booking.setType("Hotel");
                booking.setBookingId(hotelId);
                if (date != null && !date.isEmpty()) {
                    booking.setDate(date.length() > 10 ? date.substring(0, 10) : date);
                } else {
                    booking.setDate(LocalDate.now().toString());
                }
                booking.setQuantity(rooms);
                booking.setTotalPrice(price);
                user.getBookings().add(booking);
                userRepository.save(user);
                return booking;
            } else {
                throw new RuntimeException("Not enough rooms available");
            }
        }
        throw new RuntimeException("User or hotel not found");
    }
}
