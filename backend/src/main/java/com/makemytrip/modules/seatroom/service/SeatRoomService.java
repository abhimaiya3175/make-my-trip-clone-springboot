package com.makemytrip.modules.seatroom.service;

import com.makemytrip.modules.seatroom.model.Seat;
import com.makemytrip.modules.seatroom.model.Room;
import com.makemytrip.modules.seatroom.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeatRoomService {
    @Autowired
    private SeatRepository seatRepository;

    public List<Seat> getSeatsByFlightId(String flightId) {
        return seatRepository.findByFlightId(flightId);
    }

    public List<Seat> getAvailableSeats(String flightId) {
        return seatRepository.findByFlightIdAndAvailable(flightId, true);
    }

    public Seat bookSeat(String seatId) {
        Seat seat = seatRepository.findById(seatId).orElse(null);
        if (seat != null && seat.isAvailable()) {
            seat.setAvailable(false);
            return seatRepository.save(seat);
        }
        return null;
    }
}
