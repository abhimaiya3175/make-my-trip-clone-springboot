package com.makemytrip.modules.cancellation.controller;

import com.makemytrip.modules.cancellation.dto.*;
import com.makemytrip.modules.cancellation.model.RefundStatus;
import com.makemytrip.modules.cancellation.service.CancellationService;
import com.makemytrip.modules.cancellation.service.RefundCalculationService;
import com.makemytrip.security.AuthContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Cancellation and Refund operations
 */
@RestController
@RequestMapping("/api/cancellation")
public class CancellationController {

    private static final Logger log = LoggerFactory.getLogger(CancellationController.class);

    @Autowired
    private CancellationService cancellationService;

    /**
     * Generate cancellation preview before user confirms
     * 
     * @param bookingId Booking ID
     * @param bookingType Type of booking (FLIGHT/HOTEL)
     * @param quantityToCancel Quantity to cancel
     * @param totalQuantity Total quantity in booking
     * @param originalPrice Original booking price
     * @param travelDateTimeString Travel date/time as ISO string
     * @return CancellationPreviewDTO
     */
    @GetMapping("/preview")
    public ResponseEntity<?> generateCancellationPreview(
            @RequestParam String bookingId,
            @RequestParam String bookingType,
            @RequestParam int quantityToCancel,
            @RequestParam int totalQuantity,
            @RequestParam double originalPrice,
            @RequestParam String travelDateTimeString) {

        try {
            log.info("[PREVIEW] bookingId={}, type={}, cancel={}/{}, price={}, travelDate={}", 
                bookingId, bookingType, quantityToCancel, totalQuantity, originalPrice, travelDateTimeString);
            
            LocalDateTime travelDateTime = RefundCalculationService.parseTravelDate(travelDateTimeString);
            log.info("[PREVIEW] Parsed travelDateTime: {}", travelDateTime);
            
            CancellationPreviewDTO preview = cancellationService.generateCancellationPreview(
                bookingId, bookingType, quantityToCancel, totalQuantity, originalPrice, travelDateTime
            );
            log.info("[PREVIEW] Success: refund={}, percentage={}%", preview.getRefundAmount(), preview.getRefundPercentage());
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            log.error("[PREVIEW] Error: {}", e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to generate preview: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Process booking cancellation
     * Requires authentication - userId should come from JWT token
     * 
     * @param userId User ID (from authentication)
     * @param request CancellationRequestDTO with cancellation details
     * @param totalQuantity Total quantity in original booking
     * @param originalPrice Original booking price
     * @param travelDateTimeString Travel date/time as ISO string
     * @return CancellationResponseDTO with confirmation
     */
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelBooking(
            Authentication authentication,
            @Valid @RequestBody CancellationRequestDTO request,
            @RequestParam int totalQuantity,
            @RequestParam double originalPrice,
            @RequestParam String travelDateTimeString) {

        try {
            String userId = AuthContext.userId(authentication);
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("Authentication is required"));
            }

            log.info("[CANCEL] userId={}, bookingId={}, type={}, reason={}", 
                userId, request.getBookingId(), request.getBookingType(), request.getReason());
            log.info("[CANCEL] quantityToCancel={}, totalQuantity={}, originalPrice={}, travelDate={}", 
                request.getQuantityToCancel(), totalQuantity, originalPrice, travelDateTimeString);

            // Validate request
            if (request.getBookingId() == null || request.getBookingId().isEmpty()) {
                log.warn("[CANCEL] Missing booking ID");
                return ResponseEntity.badRequest().body(errorResponse("Booking ID is required"));
            }
            if (request.getReason() == null) {
                log.warn("[CANCEL] Missing cancellation reason");
                return ResponseEntity.badRequest().body(errorResponse("Cancellation reason is required"));
            }
            if (request.getQuantityToCancel() <= 0) {
                log.warn("[CANCEL] Invalid quantity: {}", request.getQuantityToCancel());
                return ResponseEntity.badRequest().body(errorResponse("Invalid quantity to cancel"));
            }

            // Check if booking already has a cancellation
            if (cancellationService.hasExistingCancellation(request.getBookingId())) {
                log.warn("[CANCEL] Cancellation already exists for bookingId={}", request.getBookingId());
                return ResponseEntity.badRequest().body(errorResponse("Cancellation already exists for this booking"));
            }

            LocalDateTime travelDateTime = RefundCalculationService.parseTravelDate(travelDateTimeString);
            log.info("[CANCEL] Parsed travelDateTime: {}", travelDateTime);

            CancellationResponseDTO response = cancellationService.cancelBooking(
                userId, request, totalQuantity, originalPrice, travelDateTime
            );

            if (response.isSuccess()) {
                log.info("[CANCEL] Success! cancellationId={}, refund={}", response.getCancellationId(), response.getRefundAmount());
                return ResponseEntity.ok(response);
            } else {
                log.error("[CANCEL] Failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }

        } catch (Exception e) {
            log.error("[CANCEL] Exception: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error processing cancellation: " + e.getMessage()));
        }
    }

    /**
     * Get refund status for a cancellation
     * 
     * @param cancellationId Cancellation ID
     * @return RefundTrackerDTO with current refund status
     */
    @GetMapping("/refund-status/{cancellationId}")
    public ResponseEntity<?> getRefundStatus(@PathVariable String cancellationId) {
        try {
            Optional<RefundTrackerDTO> tracker = cancellationService.getRefundStatus(cancellationId);
            if (tracker.isPresent()) {
                return ResponseEntity.ok(tracker.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorResponse("Refund tracker not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching refund status: " + e.getMessage()));
        }
    }

    /**
     * Get user's cancellations with refund status
     * Requires authentication - userId from JWT token
     * 
     * @param userId User ID (from authentication)
     * @return List of cancellations with refund details
     */
    @GetMapping("/my/cancellations")
    public ResponseEntity<?> getUserCancellations(Authentication authentication) {
        try {
            String userId = AuthContext.userId(authentication);
            if (userId == null || userId.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse("Authentication is required"));
            }

            log.info("[GET_USER_CANCELLATIONS] START userId={}, type={}, isEmpty={}", 
                userId, userId == null ? "null" : userId.getClass().getSimpleName(), 
                userId == null ? "N/A" : userId.isEmpty());
            
            List<CancellationResponseDTO> cancellations = 
                cancellationService.getUserCancellationsWithRefundStatus(userId);
            
            log.info("[GET_USER_CANCELLATIONS] SUCCESS: Found {} cancellation(s) for userId={}", cancellations.size(), userId);
            
            if (cancellations.isEmpty()) {
                return ResponseEntity.ok(new java.util.ArrayList<>());
            }
            
            return ResponseEntity.ok(cancellations);
        } catch (Exception e) {
            log.error("[GET_USER_CANCELLATIONS] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching user cancellations: " + e.getMessage()));
        }
    }

    /**
     * Get cancellation details
     * 
     * @param cancellationId Cancellation ID
     * @return Cancellation details
     */
    @GetMapping("/{cancellationId}")
    public ResponseEntity<?> getCancellationDetails(@PathVariable String cancellationId) {
        try {
            Optional<?> cancellation = cancellationService.getCancellationDetails(cancellationId);
            if (cancellation.isPresent()) {
                return ResponseEntity.ok(cancellation.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorResponse("Cancellation not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching cancellation details: " + e.getMessage()));
        }
    }

    /**
     * Update refund status (Admin only)
     * Used for internal refund processing
     * 
     * @param refundTrackerId Refund tracker ID
     * @param status New refund status
     * @param notes Optional notes
     * @return Updated RefundTrackerDTO
     */
    @PutMapping("/refund-status/{refundTrackerId}")
    public ResponseEntity<?> updateRefundStatus(
            @PathVariable String refundTrackerId,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {

        try {
            RefundStatus newStatus = RefundStatus.valueOf(status.toUpperCase());
            Optional<RefundTrackerDTO> updated = cancellationService.updateRefundStatus(
                refundTrackerId, newStatus, notes
            );

            if (updated.isPresent()) {
                return ResponseEntity.ok(updated.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(errorResponse("Refund tracker not found"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(errorResponse("Invalid refund status"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error updating refund status: " + e.getMessage()));
        }
    }

    /**
     * Get cancellation reasons (for dropdown in frontend)
     * 
     * @return List of available cancellation reasons
     */
    @GetMapping("/reasons")
    public ResponseEntity<?> getCancellationReasons() {
        try {
            Map<String, String> reasons = new HashMap<>();
            reasons.put("CHANGE_OF_PLANS", "Change of plans");
            reasons.put("FOUND_BETTER_PRICE", "Found better price");
            reasons.put("MEDICAL_REASON", "Medical reason");
            reasons.put("BOOKING_MISTAKE", "Booking mistake");
            reasons.put("OTHER", "Other");
            
            return ResponseEntity.ok(reasons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching cancellation reasons: " + e.getMessage()));
        }
    }

    /**
     * Get refund status options (for dropdown in frontend)
     * 
     * @return List of refund status stages
     */
    @GetMapping("/refund-statuses")
    public ResponseEntity<?> getRefundStatuses() {
        try {
            Map<String, String> statuses = new HashMap<>();
            statuses.put("CANCELLATION_REQUESTED", "Cancellation Requested");
            statuses.put("REFUND_INITIATED", "Refund Initiated");
            statuses.put("PROCESSING", "Processing");
            statuses.put("REFUNDED", "Refunded");
            
            return ResponseEntity.ok(statuses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse("Error fetching refund statuses: " + e.getMessage()));
        }
    }

    /**
     * Helper method to create error response
     */
    private Map<String, String> errorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        error.put("timestamp", LocalDateTime.now().toString());
        return error;
    }
}
