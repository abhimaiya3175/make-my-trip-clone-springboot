package com.makemytrip.modules.hotels.controller;

import com.makemytrip.common.api.ApiError;
import com.makemytrip.common.api.ApiResponse;
import com.makemytrip.modules.hotels.model.Hotel;
import com.makemytrip.modules.hotels.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class HotelController {
    @Autowired
    private HotelService hotelService;

    @GetMapping("/hotel")
    public ResponseEntity<List<Hotel>> getallhotel() {
        List<Hotel> hotels = hotelService.getAllHotels();
        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/hotel/{id}")
    public ResponseEntity<?> getHotelById(@PathVariable String id) {
        return hotelService.getHotelById(id)
            .map(h -> ResponseEntity.ok(
                ApiResponse.ok(h, UUID.randomUUID().toString())))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(
                    new ApiError("NOT_FOUND", "Hotel not found", null),
                    UUID.randomUUID().toString())));
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
