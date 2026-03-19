package com.makemytrip.modules.seatroom.repository;

import com.makemytrip.modules.seatroom.model.Seat;
import com.makemytrip.modules.seatroom.model.SeatClass;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends MongoRepository<Seat, String> {
    List<Seat> findByFlightId(String flightId);
    List<Seat> findByFlightIdAndAvailable(String flightId, boolean available);
    List<Seat> findByFlightIdAndSeatClass(String flightId, SeatClass seatClass);
    Optional<Seat> findByFlightIdAndSeatNumber(String flightId, String seatNumber);
    boolean existsByFlightId(String flightId);
}
