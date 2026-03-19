package com.makemytrip.modules.cancellation.service;

import com.makemytrip.modules.cancellation.dto.*;
import com.makemytrip.modules.cancellation.model.*;
import com.makemytrip.modules.cancellation.repository.CancellationRepository;
import com.makemytrip.modules.cancellation.repository.RefundTrackerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service layer for handling cancellation and refund operations
 * Implements business logic for:
 * - Cancellation preview generation
 * - Booking cancellation (full and partial)
 * - Refund tracking
 */
@Service
@SuppressWarnings("null")
public class CancellationService {

    private static final Logger log = LoggerFactory.getLogger(CancellationService.class);

    @Autowired
    private CancellationRepository cancellationRepository;

    @Autowired
    private RefundTrackerRepository refundTrackerRepository;

    /**
     * Generate cancellation preview before user confirms
     * Shows refund amount, percentage, and policy
     * 
     * @param bookingId        Booking ID to cancel
     * @param bookingType      Type of booking (FLIGHT/HOTEL)
     * @param quantityToCancel Quantity to cancel (for partial cancellation)
     * @param totalQuantity    Total quantity in booking
     * @param originalPrice    Original booking price
     * @param travelDateTime   Travel date and time
     * @return CancellationPreviewDTO with all details
     */
    public CancellationPreviewDTO generateCancellationPreview(String bookingId,
            String bookingType,
            int quantityToCancel,
            int totalQuantity,
            double originalPrice,
            LocalDateTime travelDateTime) {

        LocalDateTime now = LocalDateTime.now();
        log.info("=== CANCELLATION PREVIEW ===");
        log.info("BookingId: {}, Type: {}, Cancel: {}/{}, Price: {}",
                bookingId, bookingType, quantityToCancel, totalQuantity, originalPrice);
        log.info("Travel DateTime: {}, Current DateTime: {}", travelDateTime, now);

        long hoursUntilTravel = RefundCalculationService.getHoursUntilTravel(now, travelDateTime);
        log.info("Hours until travel: {}", hoursUntilTravel);

        CancellationPreviewDTO preview = new CancellationPreviewDTO();
        preview.setBookingId(bookingId);
        preview.setBookingType(bookingType);
        preview.setTotalQuantity(totalQuantity);
        preview.setQuintityToCancel(quantityToCancel);
        preview.setRemainingQuantity(totalQuantity - quantityToCancel);
        preview.setOriginalPrice(originalPrice);
        preview.setTravelDate(travelDateTime.toString());

        // Calculate price per unit
        double pricePerUnit = originalPrice / totalQuantity;
        preview.setPricePerUnit(pricePerUnit);

        // Calculate cancellation price (price for units being cancelled)
        double cancellationPrice = pricePerUnit * quantityToCancel;
        preview.setCancellationPrice(cancellationPrice);

        // Calculate refund amount using consistent 'now'
        double refundAmount = RefundCalculationService.calculatePartialRefundAmount(
                originalPrice, totalQuantity, quantityToCancel, now, travelDateTime);
        preview.setRefundAmount(refundAmount);

        // Calculate refund percentage
        boolean eligibleFor90 = RefundCalculationService.isEligibleFor90PercentRefund(now, travelDateTime);
        double refundPercentage = eligibleFor90 ? 90.0 : 50.0;
        preview.setRefundPercentage(refundPercentage);

        // Set refund policy
        preview.setRefundPolicy(RefundCalculationService.getRefundPolicyDescription(now, travelDateTime));
        preview.setEligibleFor90Percent(eligibleFor90);

        preview.setHoursUntilTravel(hoursUntilTravel + " hours");

        log.info("Preview result: refundAmount={}, refundPercentage={}%, eligibleFor90={}",
                refundAmount, refundPercentage, eligibleFor90);
        log.info("=== END PREVIEW ===");

        return preview;
    }

    /**
     * Cancel a booking (full or partial cancellation)
     * Creates Cancellation and RefundTracker records
     * 
     * @param userId         User ID requesting cancellation
     * @param request        CancellationRequestDTO with cancellation details
     * @param totalQuantity  Total quantity in original booking
     * @param originalPrice  Original booking price
     * @param travelDateTime Travel date/time
     * @return CancellationResponseDTO with confirmation details
     */
    public CancellationResponseDTO cancelBooking(String userId,
            CancellationRequestDTO request,
            int totalQuantity,
            double originalPrice,
            LocalDateTime travelDateTime) {

        CancellationResponseDTO response = new CancellationResponseDTO();

        try {
            // Capture current time ONCE for consistency
            LocalDateTime now = LocalDateTime.now();

            log.info("=== CANCEL BOOKING ===");
            log.info("UserId: {}, BookingId: {}, Type: {}", userId, request.getBookingId(), request.getBookingType());
            log.info("Reason: {}, QuantityToCancel: {}, TotalQuantity: {}", request.getReason(),
                    request.getQuantityToCancel(), totalQuantity);
            log.info("OriginalPrice: {}, TravelDateTime: {}, CurrentTime: {}", originalPrice, travelDateTime, now);

            // Create Cancellation record
            Cancellation cancellation = new Cancellation();
            cancellation.setUserId(userId);
            cancellation.setBookingId(request.getBookingId());
            cancellation.setBookingType(request.getBookingType());
            cancellation.setReason(request.getReason());
            cancellation.setAdditionalNotes(request.getAdditionalNotes());
            cancellation.setTravelDateTime(travelDateTime);
            cancellation.setCancellationRequestedAt(now);
            cancellation.setTotalQuantity(totalQuantity);
            cancellation.setOriginalPrice(originalPrice);

            // Handle full or partial cancellation
            int quantityToCancel = request.getQuantityToCancel();
            if (quantityToCancel >= totalQuantity) {
                // Full cancellation
                cancellation.setCancelledQuantity(totalQuantity);
                cancellation.setRemainingQuantity(0);
                cancellation.setPartialCancellation(false);
                cancellation.setBookingStatus(BookingStatus.CANCELLED);
                log.info("Full cancellation: {} units", totalQuantity);
            } else {
                // Partial cancellation
                cancellation.setCancelledQuantity(quantityToCancel);
                cancellation.setRemainingQuantity(totalQuantity - quantityToCancel);
                cancellation.setPartialCancellation(true);
                cancellation.setBookingStatus(BookingStatus.PARTIALLY_CANCELLED);
                log.info("Partial cancellation: {} of {} units", quantityToCancel, totalQuantity);
            }

            // Calculate refund amount using consistent 'now'
            double refundAmount = RefundCalculationService.calculatePartialRefundAmount(
                    originalPrice, totalQuantity, quantityToCancel, now, travelDateTime);
            cancellation.setRefundAmount(refundAmount);
            log.info("Calculated refund amount: {}", refundAmount);

            // Calculate refund percentage using consistent 'now'
            double refundPercentage = RefundCalculationService.calculateRefundPercentage(
                    now, travelDateTime);
            cancellation.setRefundPercentage(refundPercentage * 100);
            log.info("Refund percentage: {}% (raw: {})", refundPercentage * 100, refundPercentage);

            long hoursUntilTravel = RefundCalculationService.getHoursUntilTravel(now, travelDateTime);
            log.info("Hours until travel: {}", hoursUntilTravel);

            // Save cancellation record
            Cancellation savedCancellation = cancellationRepository.save(cancellation);
            log.info("Saved cancellation with ID: {}", savedCancellation.getId());

            // Create RefundTracker with REFUND_INITIATED status (auto-advance from
            // CANCELLATION_REQUESTED)
            RefundTracker refundTracker = new RefundTracker(savedCancellation.getId(), refundAmount);
            refundTracker.setStatus(RefundStatus.REFUND_INITIATED);
            refundTracker.setNotes("Refund initiated automatically upon cancellation confirmation");
            RefundTracker savedTracker = refundTrackerRepository.save(refundTracker);
            log.info("Saved RefundTracker with ID: {}, Status: {}", savedTracker.getId(), savedTracker.getStatus());

            // Link refund tracker to cancellation
            savedCancellation.setRefundTrackerId(savedTracker.getId());
            cancellationRepository.save(savedCancellation);

            // Build response
            response.setCancellationId(savedCancellation.getId());
            response.setBookingId(request.getBookingId());
            response.setBookingType(request.getBookingType());
            response.setNewBookingStatus(cancellation.getBookingStatus());
            response.setTotalQuantity(totalQuantity);
            response.setCancelledQuantity(quantityToCancel);
            response.setRemainingQuantity(cancellation.getRemainingQuantity());
            response.setRefundAmount(refundAmount);
            response.setRefundPercentage(refundPercentage * 100);
            response.setRefundTrackerId(savedTracker.getId());
            response.setPartialCancellation(cancellation.isPartialCancellation());
            response.setSuccess(true);
            response.setMessage(buildSuccessMessage(quantityToCancel, totalQuantity, refundAmount));

            // Add refund tracker info to response
            RefundTrackerDTO trackerDTO = new RefundTrackerDTO();
            trackerDTO.setId(savedTracker.getId());
            trackerDTO.setCancellationId(savedCancellation.getId());
            trackerDTO.setStatus(savedTracker.getStatus());
            trackerDTO.setStatusDisplay(savedTracker.getStatus().getDisplayName());
            trackerDTO.setRefundAmount(savedTracker.getRefundAmount());
            trackerDTO.setUpdatedAt(savedTracker.getUpdatedAt());
            response.setRefundTracker(trackerDTO);

            log.info("Cancellation successful! RefundAmount: {}, Status: {}", refundAmount, savedTracker.getStatus());
            log.info("=== END CANCEL BOOKING ===");

            return response;

        } catch (Exception e) {
            log.error("=== CANCELLATION ERROR ===");
            log.error("Error processing cancellation for booking {}: {}", request.getBookingId(), e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Error processing cancellation: " + e.getMessage());
            return response;
        }
    }

    /**
     * Get cancellation details by cancellation ID
     * 
     * @param cancellationId Cancellation ID
     * @return Cancellation details if found
     */
    public Optional<Cancellation> getCancellationDetails(String cancellationId) {
        return cancellationRepository.findById(cancellationId);
    }

    /**
     * Get refund status by cancellation ID
     * 
     * @param cancellationId Cancellation ID
     * @return RefundTrackerDTO with current refund status
     */
    public Optional<RefundTrackerDTO> getRefundStatus(String cancellationId) {
        Optional<RefundTracker> tracker = refundTrackerRepository.findByCancellationId(cancellationId);

        if (tracker.isPresent()) {
            RefundTracker rt = tracker.get();
            RefundTrackerDTO dto = new RefundTrackerDTO();
            dto.setId(rt.getId());
            dto.setCancellationId(rt.getCancellationId());
            dto.setStatus(rt.getStatus());
            dto.setStatusDisplay(rt.getStatus().getDisplayName());
            dto.setRefundAmount(rt.getRefundAmount());
            dto.setCreatedAt(rt.getCreatedAt());
            dto.setUpdatedAt(rt.getUpdatedAt());
            dto.setNotes(rt.getNotes());
            return Optional.of(dto);
        }

        return Optional.empty();
    }

    /**
     * Get all cancellations for a user
     * 
     * @param userId User ID
     * @return List of user's cancellations
     */
    public List<Cancellation> getUserCancellations(String userId) {
        return cancellationRepository.findByUserId(userId);
    }

    /**
     * Get all cancellations and their refund statuses for a user
     * 
     * @param userId User ID
     * @return List of DTOs with cancellation and refund details
     */
    public List<CancellationResponseDTO> getUserCancellationsWithRefundStatus(String userId) {
        log.info("Fetching cancellations for userId: {}", userId);
        List<Cancellation> cancellations = cancellationRepository.findByUserId(userId);
        log.info("Found {} cancellation(s) for user {}", cancellations.size(), userId);

        return cancellations.stream().map(cancellation -> {
            CancellationResponseDTO response = new CancellationResponseDTO();
            response.setCancellationId(cancellation.getId());
            response.setBookingId(cancellation.getBookingId());
            response.setBookingType(cancellation.getBookingType());
            response.setNewBookingStatus(cancellation.getBookingStatus());
            response.setTotalQuantity(cancellation.getTotalQuantity());
            response.setCancelledQuantity(cancellation.getCancelledQuantity());
            response.setRemainingQuantity(cancellation.getRemainingQuantity());
            response.setRefundAmount(cancellation.getRefundAmount());
            response.setRefundPercentage(cancellation.getRefundPercentage());
            response.setPartialCancellation(cancellation.isPartialCancellation());

            // Get refund status
            Optional<RefundTracker> tracker = refundTrackerRepository.findByCancellationId(cancellation.getId());
            if (tracker.isPresent()) {
                RefundTracker rt = tracker.get();
                RefundTrackerDTO trackerDTO = new RefundTrackerDTO();
                trackerDTO.setId(rt.getId());
                trackerDTO.setStatus(rt.getStatus());
                trackerDTO.setStatusDisplay(rt.getStatus().getDisplayName());
                trackerDTO.setRefundAmount(rt.getRefundAmount());
                trackerDTO.setUpdatedAt(rt.getUpdatedAt());
                response.setRefundTracker(trackerDTO);
            }

            return response;
        }).collect(Collectors.toList());
    }

    /**
     * Update refund status (for admin operations)
     * 
     * @param refundTrackerId Refund tracker ID
     * @param newStatus       New refund status
     * @param notes           Optional notes
     * @return Updated RefundTrackerDTO
     */
    public Optional<RefundTrackerDTO> updateRefundStatus(String refundTrackerId,
            RefundStatus newStatus,
            String notes) {
        log.info("Updating refund status: trackerId={}, newStatus={}, notes={}", refundTrackerId, newStatus, notes);
        Optional<RefundTracker> tracker = refundTrackerRepository.findById(refundTrackerId);

        if (tracker.isPresent()) {
            RefundTracker rt = tracker.get();
            rt.setStatus(newStatus);
            if (notes != null && !notes.isEmpty()) {
                rt.setNotes(notes);
            }
            rt.setUpdatedAt(LocalDateTime.now());

            RefundTracker updated = refundTrackerRepository.save(rt);

            RefundTrackerDTO dto = new RefundTrackerDTO();
            dto.setId(updated.getId());
            dto.setCancellationId(updated.getCancellationId());
            dto.setStatus(updated.getStatus());
            dto.setStatusDisplay(updated.getStatus().getDisplayName());
            dto.setRefundAmount(updated.getRefundAmount());
            dto.setUpdatedAt(updated.getUpdatedAt());
            dto.setNotes(updated.getNotes());

            return Optional.of(dto);
        }

        return Optional.empty();
    }

    /**
     * Check if booking already has a cancellation request
     * 
     * @param bookingId Booking ID
     * @return true if cancellation exists, false otherwise
     */
    public boolean hasExistingCancellation(String bookingId) {
        return cancellationRepository.existsByBookingId(bookingId);
    }

    /**
     * Build success message for cancellation response
     */
    private String buildSuccessMessage(int cancelledQuantity, int totalQuantity, double refundAmount) {
        if (cancelledQuantity >= totalQuantity) {
            return String.format("Booking cancelled successfully. Refund of ₹%.2f will be processed shortly.",
                    refundAmount);
        } else {
            return String.format(
                    "Partial cancellation successful. %d unit(s) cancelled. Refund of ₹%.2f will be processed shortly.",
                    cancelledQuantity, refundAmount);
        }
    }
}
