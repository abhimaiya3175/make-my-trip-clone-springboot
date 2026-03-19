package com.makemytrip.modules.booking.repository;

import com.makemytrip.modules.booking.model.Booking;
import com.makemytrip.modules.booking.model.BookingStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends MongoRepository<Booking, String> {
    List<Booking> findByUserId(String userId);
    List<Booking> findByUserIdAndBookingStatus(String userId, BookingStatus status);
    List<Booking> findByEntityIdAndEntityType(String entityId, com.makemytrip.modules.booking.model.EntityType entityType);
}
