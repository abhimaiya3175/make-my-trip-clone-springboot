package com.makemytrip.modules.cancellation.repository;

import com.makemytrip.modules.cancellation.model.Cancellation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Cancellation entity
 */
@Repository
public interface CancellationRepository extends MongoRepository<Cancellation, String> {
    
    // Find cancellations by user ID
    List<Cancellation> findByUserId(String userId);
    
    // Find cancellation by booking ID
    Optional<Cancellation> findByBookingId(String bookingId);
    
    // Find cancellations by user ID and booking type
    List<Cancellation> findByUserIdAndBookingType(String userId, String bookingType);
    
    // Check if cancellation exists for a booking
    boolean existsByBookingId(String bookingId);
}
