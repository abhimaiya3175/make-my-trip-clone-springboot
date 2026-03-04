package com.makemytrip.modules.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.makemytrip.modules.booking.model.Booking;
import com.makemytrip.modules.booking.service.BookingService;

@RestController
@RequestMapping("/booking")
@CrossOrigin(origins = "*")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    @PostMapping("/flight")
    public Booking bookFlight(@RequestParam String userId, @RequestParam String flightId, @RequestParam int seats, @RequestParam double price, @RequestParam(required = false) String date) {
        return bookingService.bookFlight(userId, flightId, seats, price, date);
    }

    @PostMapping("/hotel")
    public Booking bookhotel(@RequestParam String userId, @RequestParam String hotelId, @RequestParam int rooms, @RequestParam double price, @RequestParam(required = false) String date) {
        return bookingService.bookhotel(userId, hotelId, rooms, price, date);
    }
}
