package com.makemytrip.modules.booking.controller;

import com.makemytrip.common.api.ApiError;
import com.makemytrip.common.api.ApiHeaders;
import com.makemytrip.common.api.ApiResponse;
import com.makemytrip.modules.booking.dto.BookingPermissions;
import com.makemytrip.modules.booking.dto.CancelBookingRequest;
import com.makemytrip.modules.booking.dto.CreateBookingRequest;
import com.makemytrip.modules.booking.model.Booking;
import com.makemytrip.modules.booking.service.BookingService;
import com.makemytrip.security.AuthContext;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;

    /**
     * Create a new booking
     * POST /api/bookings
     */
    @PostMapping
    public ResponseEntity<ApiResponse<Booking>> createBooking(
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication,
            @Valid @RequestBody CreateBookingRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
        }
        
        try {
            request.setUserId(userId);
            if (request.getUserName() == null || request.getUserName().isBlank()) {
                request.setUserName(AuthContext.userName(authentication));
            }
            Booking booking = bookingService.createBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(booking, reqId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Get booking by ID
     * GET /api/bookings/{bookingId}
     */
    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponse<Booking>> getBooking(
            @PathVariable String bookingId,
            Authentication authentication,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
        }

        java.util.Optional<Booking> bookingOpt = bookingService.getBookingById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new ApiError("NOT_FOUND", "Booking not found", null), reqId));
        }

        Booking booking = bookingOpt.get();
        boolean owner = userId.equals(booking.getUserId());
        boolean admin = AuthContext.hasRole(authentication, "ADMIN");
        if (!owner && !admin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(new ApiError("FORBIDDEN", "Access denied", null), reqId));
        }

        return ResponseEntity.ok(ApiResponse.ok(booking, reqId));
    }

    /**
     * Get all bookings for current user
     * GET /api/bookings
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Booking>>> getUserBookings(
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication) {

        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();

        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
        }
        
        List<Booking> bookings = bookingService.getUserBookings(userId);
        return ResponseEntity.ok(ApiResponse.ok(bookings, reqId));
    }

    /**
     * Get booking permissions (what actions are allowed)
     * GET /api/bookings/{bookingId}/permissions
     * 
     * Example response:
     * {
     *   "canPay": true,
     *   "canCancel": true,
     *   "canModify": true,
     *   "canReview": false,
     *   "message": "Booking active. You can make changes or cancel."
     * }
     */
    @GetMapping("/{bookingId}/permissions")
    public ResponseEntity<ApiResponse<BookingPermissions>> getBookingPermissions(
            @PathVariable String bookingId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        try {
            BookingPermissions permissions = bookingService.getBookingPermissions(bookingId);
            return ResponseEntity.ok(ApiResponse.ok(permissions, reqId));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Cancel a booking
     * POST /api/bookings/{bookingId}/cancel
     * 
     * Logic:
     * - Only allowed BEFORE travel date
     * - Sets bookingStatus = CANCELLED
     * - Sets cancellationAllowed = false
     * - Updates paymentStatus to REFUNDED if paid
     */
    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponse<Booking>> cancelBooking(
            @PathVariable String bookingId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication,
            @RequestBody(required = false) CancelBookingRequest request) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
        }
        
        try {
            String reason = (request != null && request.getReason() != null) 
                    ? request.getReason() 
                    : "User requested cancellation";
            
            Booking booking = bookingService.cancelBooking(bookingId, userId, reason);
            return ResponseEntity.ok(ApiResponse.ok(booking, reqId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new ApiError("NOT_FOUND", e.getMessage(), null), reqId));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Mark review as submitted for a booking
     * POST /api/bookings/{bookingId}/review-submitted
     * 
     * Logic:
     * - Only allowed ON or AFTER travel date
     * - Sets reviewSubmitted = true
     * - Called automatically after user submits a review
     */
    @PostMapping("/{bookingId}/review-submitted")
    public ResponseEntity<ApiResponse<Booking>> markReviewSubmitted(
            @PathVariable String bookingId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId,
            Authentication authentication) {

        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();

        String userId = AuthContext.userId(authentication);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail(new ApiError("UNAUTHORIZED", "Authentication is required", null), reqId));
        }
        
        try {
            Booking booking = bookingService.markReviewSubmitted(bookingId, userId);
            return ResponseEntity.ok(ApiResponse.ok(booking, reqId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new ApiError("NOT_FOUND", e.getMessage(), null), reqId));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    /**
     * Confirm payment for a booking
     * POST /api/bookings/{bookingId}/confirm-payment
     */
    @PostMapping("/{bookingId}/confirm-payment")
    public ResponseEntity<ApiResponse<Booking>> confirmPayment(
            @PathVariable String bookingId,
            @RequestHeader(name = ApiHeaders.REQUEST_ID, required = false) String requestId) {
        
        String reqId = requestId != null ? requestId : UUID.randomUUID().toString();
        
        try {
            Booking booking = bookingService.confirmPayment(bookingId);
            return ResponseEntity.ok(ApiResponse.ok(booking, reqId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new ApiError("NOT_FOUND", e.getMessage(), null), reqId));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                .body(ApiResponse.fail(new ApiError("BAD_REQUEST", e.getMessage(), null), reqId));
        }
    }

    // ========== Legacy Endpoints (backward compatibility) ==========

    @PostMapping("/flight")
    public Booking bookFlight(
            @RequestParam String userId, 
            @RequestParam String flightId, 
            @RequestParam int seats, 
            @RequestParam double price, 
            @RequestParam(required = false) String date) {
        return bookingService.bookFlight(userId, flightId, seats, price, date);
    }

    @PostMapping("/hotel")
    public Booking bookhotel(
            @RequestParam String userId, 
            @RequestParam String hotelId, 
            @RequestParam int rooms, 
            @RequestParam double price, 
            @RequestParam(required = false) String date) {
        return bookingService.bookhotel(userId, hotelId, rooms, price, date);
    }
}
