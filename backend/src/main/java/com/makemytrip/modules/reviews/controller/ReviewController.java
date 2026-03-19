package com.makemytrip.modules.reviews.controller;

import com.makemytrip.common.api.ApiError;
import com.makemytrip.common.api.ApiHeaders;
import com.makemytrip.common.api.ApiResponse;
import com.makemytrip.common.api.PageResponse;
import com.makemytrip.modules.auth.model.User;
import com.makemytrip.modules.auth.service.AuthService;
import com.makemytrip.modules.reviews.dto.CreateReviewRequest;
import com.makemytrip.modules.reviews.dto.FlagReviewRequest;
import com.makemytrip.modules.reviews.dto.ReplyReviewRequest;
import com.makemytrip.modules.reviews.model.EntityType;
import com.makemytrip.modules.reviews.model.Review;
import com.makemytrip.modules.reviews.service.ReviewService;
import com.makemytrip.security.AuthContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;

    @Autowired
    private AuthService authService;

    @Value("${app.upload.dir:./uploads/reviews/}")
    private String uploadDir;

    /**
     * Create a new review
     * POST /api/reviews
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication,
            @Valid @RequestBody CreateReviewRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
        }

        String userName = AuthContext.userName(authentication);
        if (userName == null || userName.isBlank()) {
            User user = authService.getUserById(userId);
            userName = user != null
                ? ((user.getFirstName() == null ? "" : user.getFirstName()) + " "
                + (user.getLastName() == null ? "" : user.getLastName())).trim()
                : "Anonymous";
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
     * Upload a review photo
     * POST /api/reviews/upload-photo
     */
    @PostMapping("/upload-photo")
    public ResponseEntity<ApiResponse<String>> uploadPhoto(
            @RequestParam("photo") MultipartFile photo,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        // Validate size max 5MB
        if (photo.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("VALIDATION_FAILED", "File size exceeds 5MB limit", null), reqId));
        }

        // Validate type jpeg/png/webp
        String contentType = photo.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp"))) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("VALIDATION_FAILED", "Only JPEG, PNG, and WEBP formats are allowed", null), reqId));
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = photo.getOriginalFilename();
            String cleanName = originalName != null && !originalName.isBlank() ? StringUtils.cleanPath(originalName) : "image.jpg";
            String filename = UUID.randomUUID().toString() + "_" + cleanName;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = "/uploads/reviews/" + filename;
            return ResponseEntity.ok(ApiResponse.ok(fileUrl, reqId));
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(ApiResponse.fail(new ApiError("INTERNAL_SERVER_ERROR", "Failed to upload file", null), reqId));
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
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication,
            @Valid @RequestBody CreateReviewRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
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
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
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
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication,
            @RequestBody FlagReviewRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
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
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication,
            @RequestBody ReplyReviewRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
        }

        if (request.getUserName() == null || request.getUserName().isBlank()) {
            request.setUserName(AuthContext.userName(authentication));
        }
        request.setOwner(AuthContext.hasRole(authentication, "ADMIN"));
        
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
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
        }
        
        try {
            reviewService.deleteReview(reviewId, userId);
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
