package com.makemytrip.modules.reviews.repository;

import com.makemytrip.modules.reviews.model.Review;
import com.makemytrip.modules.reviews.model.EntityType;
import com.makemytrip.modules.reviews.model.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<Review, String> {
    // Legacy methods
    List<Review> findByBookingId(String bookingId);
    List<Review> findByUserId(String userId);
    
    // New entity-based methods
    Page<Review> findByEntityTypeAndEntityIdAndModerationStatus(
        EntityType entityType, String entityId, ModerationStatus status, Pageable pageable);
    
    List<Review> findByEntityTypeAndEntityId(EntityType entityType, String entityId);

    Optional<Review> findByUserIdAndEntityTypeAndEntityId(String userId, EntityType entityType, String entityId);
    
    boolean existsByUserIdAndEntityTypeAndEntityId(String userId, EntityType entityType, String entityId);
    
    List<Review> findByModerationStatus(ModerationStatus status);
}

