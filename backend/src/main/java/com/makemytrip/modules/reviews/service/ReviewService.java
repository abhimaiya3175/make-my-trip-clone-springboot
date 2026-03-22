package com.makemytrip.modules.reviews.service;

import com.makemytrip.modules.reviews.dto.CreateReviewRequest;
import com.makemytrip.modules.reviews.dto.FlagReviewRequest;
import com.makemytrip.modules.reviews.dto.ReplyReviewRequest;
import com.makemytrip.modules.reviews.model.*;
import com.makemytrip.modules.reviews.repository.ReviewRepository;
import com.makemytrip.common.api.PageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class ReviewService {
    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);
    private static final int MAX_PHOTOS = 5;
    private static final int MAX_TEXT_LENGTH = 2000;
    private static final int MAX_FLAG_COUNT_AUTO_HIDE = 3;

    @Autowired
    private ReviewRepository reviewRepository;

    public Review createReview(String userId, String userName, CreateReviewRequest request) {
        // Validation
        validateReview(request);

        // If the user already has a review for this entity, update it instead of failing.
        Optional<Review> existingReview = reviewRepository.findByUserIdAndEntityTypeAndEntityId(
            userId,
            request.getEntityType(),
            request.getEntityId()
        );

        if (existingReview.isPresent()) {
            Review review = existingReview.get();
            review.setUserName(userName);
            review.setRating(request.getRating());
            review.setText(request.getText());
            review.setPhotos(request.getPhotos() != null ? request.getPhotos() : List.of());
            review.setModerationStatus(ModerationStatus.ACTIVE);
            review.setUpdatedAt(LocalDateTime.now());

            log.info("Updating existing review for {} {} by user {}", request.getEntityType(), request.getEntityId(), userId);
            return reviewRepository.save(review);
        }
        
        Review review = new Review();
        review.setUserId(userId);
        review.setUserName(userName);
        review.setEntityId(request.getEntityId());
        review.setEntityType(request.getEntityType());
        review.setRating(request.getRating());
        review.setText(request.getText());
        review.setPhotos(request.getPhotos() != null ? request.getPhotos() : List.of());
        review.setModerationStatus(ModerationStatus.ACTIVE);
        
        log.info("Creating review for {} {} by user {}", request.getEntityType(), request.getEntityId(), userId);
        return reviewRepository.save(review);
    }

    public PageResponse<Review> getReviews(EntityType entityType, String entityId, String sortBy, int page, int size) {
        Sort sort = switch (sortBy.toLowerCase()) {
            case "helpful" -> Sort.by(Sort.Direction.DESC, "helpfulCount").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
        
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Review> reviewPage = reviewRepository.findByEntityTypeAndEntityIdAndModerationStatus(
            entityType, entityId, ModerationStatus.ACTIVE, pageable);
        
        return new PageResponse<>(
            reviewPage.getContent(),
            reviewPage.getTotalElements(),
            page,
            size,
            reviewPage.getTotalPages(),
            reviewPage.hasNext(),
            reviewPage.hasPrevious()
        );
    }

    public Review voteHelpful(String reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        if (review.getHelpfulVoters().contains(userId)) {
            // Unvote
            review.getHelpfulVoters().remove(userId);
            review.setHelpfulCount(review.getHelpfulCount() - 1);
            log.info("User {} removed helpful vote from review {}", userId, reviewId);
        } else {
            // Vote
            review.getHelpfulVoters().add(userId);
            review.setHelpfulCount(review.getHelpfulCount() + 1);
            log.info("User {} voted review {} as helpful", userId, reviewId);
        }
        
        review.setUpdatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public Review flagReview(String reviewId, String userId, FlagReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        review.setFlagged(true);
        review.setFlagCount(review.getFlagCount() + 1);
        review.getFlagReasons().add(request.getReason());
        review.setUpdatedAt(LocalDateTime.now());
        
        // Auto-hide if flagged too many times
        if (review.getFlagCount() >= MAX_FLAG_COUNT_AUTO_HIDE) {
            review.setModerationStatus(ModerationStatus.HIDDEN);
            log.warn("Review {} auto-hidden due to {} flags", reviewId, review.getFlagCount());
        } else {
            review.setModerationStatus(ModerationStatus.FLAGGED);
        }
        
        log.info("User {} flagged review {} for: {}", userId, reviewId, request.getReason());
        return reviewRepository.save(review);
    }

    public Review replyToReview(String reviewId, String userId, ReplyReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        ReviewReply reply = new ReviewReply(userId, request.getUserName(), request.getText(), request.isOwner());
        review.getReplies().add(reply);
        review.setUpdatedAt(LocalDateTime.now());
        
        log.info("User {} replied to review {}", userId, reviewId);
        return reviewRepository.save(review);
    }

    public Review updateReview(String reviewId, String userId, CreateReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User can only update their own review");
        }
        
        validateReview(request);
        
        review.setRating(request.getRating());
        review.setText(request.getText());
        if (request.getPhotos() != null) {
            review.setPhotos(request.getPhotos());
        }
        review.setUpdatedAt(LocalDateTime.now());
        
        log.info("User {} updated review {}", userId, reviewId);
        return reviewRepository.save(review);
    }

    // Legacy methods for backward compatibility
    public Review addReview(Review review) {
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());
        if (review.getModerationStatus() == null) {
            review.setModerationStatus(ModerationStatus.ACTIVE);
        }
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

    public void deleteReview(String id, String userId) {
        Review review = reviewRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new IllegalArgumentException("User can only delete their own review");
        }

        reviewRepository.deleteById(id);
    }

    // Validation helper
    private void validateReview(CreateReviewRequest request) {
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Review text is required");
        }
        
        if (request.getText().length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("Review text cannot exceed " + MAX_TEXT_LENGTH + " characters");
        }
        
        if (request.getPhotos() != null && request.getPhotos().size() > MAX_PHOTOS) {
            throw new IllegalArgumentException("Cannot upload more than " + MAX_PHOTOS + " photos");
        }
    }
}
