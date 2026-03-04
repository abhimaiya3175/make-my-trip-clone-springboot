package com.makemytrip.modules.seatroom.controller;

import com.makemytrip.modules.seatroom.model.Seat;
import com.makemytrip.modules.seatroom.service.SeatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@CrossOrigin(origins = "*")
public class SeatRoomController {
    @Autowired
    private SeatRoomService seatRoomService;

    @GetMapping("/flight/{flightId}")
    public ResponseEntity<List<Seat>> getSeatsByFlight(@PathVariable String flightId) {
        return ResponseEntity.ok(seatRoomService.getSeatsByFlightId(flightId));
    }

    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<List<Seat>> getAvailableSeats(@PathVariable String flightId) {
        return ResponseEntity.ok(seatRoomService.getAvailableSeats(flightId));
    }

    @PostMapping("/{seatId}/book")
    public ResponseEntity<Seat> bookSeat(@PathVariable String seatId) {
        Seat seat = seatRoomService.bookSeat(seatId);
        if (seat != null) {
            return ResponseEntity.ok(seat);
        }
        return ResponseEntity.badRequest().build();
    }
}
