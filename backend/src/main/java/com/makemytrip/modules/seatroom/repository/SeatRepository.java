package com.makemytrip.modules.seatroom.repository;

import com.makemytrip.modules.seatroom.model.Seat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SeatRepository extends MongoRepository<Seat, String> {
    List<Seat> findByFlightId(String flightId);
    List<Seat> findByFlightIdAndAvailable(String flightId, boolean available);
}
