package com.makemytrip.modules.hotels.controller;

import com.makemytrip.modules.hotels.model.Hotel;
import com.makemytrip.modules.hotels.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HotelController {
    @Autowired
    private HotelService hotelService;

    @GetMapping("/hotel")
    public ResponseEntity<List<Hotel>> getallhotel() {
        List<Hotel> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    @PostMapping("/admin/hotel")
    public ResponseEntity<Hotel> addhotel(@RequestBody Hotel hotel) {
        Hotel savedHotel = hotelService.addHotel(hotel);
        return ResponseEntity.ok(savedHotel);
    }

    @PutMapping("/admin/hotel/{id}")
    public ResponseEntity<Hotel> editHotel(@PathVariable String id, @RequestBody Hotel updatedHotel) {
        Hotel hotel = hotelService.editHotel(id, updatedHotel);
        if (hotel != null) {
            return ResponseEntity.ok(hotel);
        }
        return ResponseEntity.notFound().build();
    }
}
