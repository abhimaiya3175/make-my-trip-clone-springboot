package com.makemytrip.modules.flights.repository;

import com.makemytrip.modules.flights.model.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FlightRepository extends MongoRepository<Flight, String> {
}
