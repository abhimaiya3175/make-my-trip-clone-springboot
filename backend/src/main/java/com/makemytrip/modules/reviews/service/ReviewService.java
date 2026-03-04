package com.makemytrip.modules.reviews.service;

import com.makemytrip.modules.reviews.model.Review;
import com.makemytrip.modules.reviews.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public Review addReview(Review review) {
        review.setCreatedAt(LocalDateTime.now().toString());
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByBookingId(String bookingId) {
        return reviewRepository.findByBookingId(bookingId);
    }

    public List<Review> getReviewsByUserId(String userId) {
        return reviewRepository.findByUserId(userId);
    }

    public Optional<Review> getReviewById(String id) {
        return reviewRepository.findById(id);
    }

    public void deleteReview(String id) {
        reviewRepository.deleteById(id);
    }
}
