package com.makemytrip.modules.reviews.controller;

import com.makemytrip.common.api.ApiError;
import com.makemytrip.common.api.ApiHeaders;
import com.makemytrip.common.api.ApiResponse;
import com.makemytrip.common.api.PageResponse;
import com.makemytrip.modules.reviews.dto.CreateReviewRequest;
import com.makemytrip.modules.reviews.dto.FlagReviewRequest;
import com.makemytrip.modules.reviews.dto.ReplyReviewRequest;
import com.makemytrip.modules.reviews.model.EntityType;
import com.makemytrip.modules.reviews.model.Review;
import com.makemytrip.modules.reviews.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    /**
     * Create a new review
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(
            @RequestHeader(name = ApiHeaders.USER_ID, required = false) String userId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            @RequestHeader(name = "X-User-Name", required = false, defaultValue = "Anonymous") String userName,
            @RequestBody CreateReviewRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "User ID is required", null), reqId));
        }
        
        try {
            Review review = reviewService.createReview(userId, userName, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(review, reqId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Get paginated reviews for an entity with sorting
     * GET /api/reviews?entityType=FLIGHT&entityId=FL123&sortBy=helpful&page=1&size=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Review>>> getReviews(
            @RequestParam EntityType entityType,
            @RequestParam String entityId,
            @RequestParam(defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        try {
            PageResponse<Review> reviews = reviewService.getReviews(entityType, entityId, sortBy, page, size);
            return ResponseEntity.ok(ApiResponse.ok(reviews, reqId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Get a single review by ID
     * GET /api/reviews/{reviewId}
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Review>> getReviewById(
            @PathVariable String reviewId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        return reviewService.getReviewById(reviewId)
            .map(review -> ResponseEntity.ok(ApiResponse.ok(review, reqId)))
            .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new ApiError("NOT_FOUND", "Review not found", null), reqId)));
    }

    /**
     * Update an existing review
     * PUT /api/reviews/{reviewId}
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Review>> updateReview(
            @PathVariable String reviewId,
            @RequestHeader(name = ApiHeaders.USER_ID, required = false) String userId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            @RequestBody CreateReviewRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "User ID is required", null), reqId));
        }
        
        try {
            Review review = reviewService.updateReview(reviewId, userId, request);
            return ResponseEntity.ok(ApiResponse.ok(review, reqId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Vote a review as helpful
     * PUT /api/reviews/{reviewId}/helpful
     */
    @PutMapping("/{reviewId}/helpful")
    public ResponseEntity<ApiResponse<Review>> voteHelpful(
            @PathVariable String reviewId,
            @RequestHeader(name = ApiHeaders.USER_ID, required = false) String userId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "User ID is required", null), reqId));
        }
        
        try {
            Review review = reviewService.voteHelpful(reviewId, userId);
            return ResponseEntity.ok(ApiResponse.ok(review, reqId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Flag a review as inappropriate
     * POST /api/reviews/{reviewId}/flag
     */
    @PostMapping("/{reviewId}/flag")
    public ResponseEntity<ApiResponse<Review>> flagReview(
            @PathVariable String reviewId,
            @RequestHeader(name = ApiHeaders.USER_ID, required = false) String userId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            @RequestBody FlagReviewRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "User ID is required", null), reqId));
        }
        
        try {
            Review review = reviewService.flagReview(reviewId, userId, request);
            return ResponseEntity.ok(ApiResponse.ok(review, reqId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Reply to a review
     * POST /api/reviews/{reviewId}/reply
     */
    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ApiResponse<Review>> replyToReview(
            @PathVariable String reviewId,
            @RequestHeader(name = ApiHeaders.USER_ID, required = false) String userId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            @RequestBody ReplyReviewRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "User ID is required", null), reqId));
        }
        
        try {
            Review review = reviewService.replyToReview(reviewId, userId, request);
            return ResponseEntity.ok(ApiResponse.ok(review, reqId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Delete a review
     * DELETE /api/reviews/{reviewId}
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @PathVariable String reviewId,
            @RequestHeader(name = ApiHeaders.USER_ID, required = false) String userId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "User ID is required", null), reqId));
        }
        
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok(ApiResponse.ok(null, reqId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    // ======== Legacy Endpoints (for backward compatibility) ========

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByBookingId(
            @PathVariable String bookingId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        List<Review> reviews = reviewService.getReviewsByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.ok(reviews, reqId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<Review>>> getReviewsByUserId(
            @PathVariable String userId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        List<Review> reviews = reviewService.getReviewsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.ok(reviews, reqId));
    }
}
