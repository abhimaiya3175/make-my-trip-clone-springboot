package com.makemytrip.modules.flightstatus.repository;

import com.makemytrip.modules.flightstatus.model.FlightStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlightStatusRepository extends MongoRepository<FlightStatus, String> {
    Optional<FlightStatus> findByFlightId(String flightId);
    boolean existsByFlightId(String flightId);
}
