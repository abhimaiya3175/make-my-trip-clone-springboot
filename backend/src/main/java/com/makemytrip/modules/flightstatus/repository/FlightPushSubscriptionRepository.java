package com.makemytrip.modules.flightstatus.repository;

import com.makemytrip.modules.flightstatus.model.FlightPushSubscription;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface FlightPushSubscriptionRepository extends MongoRepository<FlightPushSubscription, String> {
    List<FlightPushSubscription> findByFlightId(String flightId);
    Optional<FlightPushSubscription> findByEndpointAndFlightId(String endpoint, String flightId);
}
