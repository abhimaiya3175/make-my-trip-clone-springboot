package com.makemytrip.modules.reviews.repository;

import com.makemytrip.modules.reviews.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByBookingId(String bookingId);
    List<Review> findByUserId(String userId);
}
