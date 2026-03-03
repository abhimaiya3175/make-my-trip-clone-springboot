package com.makemytrip.makemytrip.cancellation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Service for calculating refund amounts based on policy
 * Refund Policy:
 * - Less than 24 hours before travel: 50% refund
 * - 24 hours or more before travel: 90% refund
 * - Travel date has passed: 50% refund (minimum)
 */
public class RefundCalculationService {

    private static final Logger log = LoggerFactory.getLogger(RefundCalculationService.class);

    private static final double REFUND_PERCENTAGE_LESS_THAN_24H = 0.50;
    private static final double REFUND_PERCENTAGE_24H_OR_MORE = 0.90;
    private static final long HOURS_THRESHOLD = 24;

    /**
     * Calculate refund amount based on cancellation time and travel time
     */
    public static double calculateRefundAmount(double originalPrice, 
                                               LocalDateTime cancellationTime, 
                                               LocalDateTime travelTime) {
        double refundPercentage = calculateRefundPercentage(cancellationTime, travelTime);
        double result = Math.round(originalPrice * refundPercentage * 100.0) / 100.0;
        log.info("[REFUND_CALC] calculateRefundAmount: price={}, percentage={}, result={}", 
            originalPrice, refundPercentage, result);
        return result;
    }

    /**
     * Calculate refund percentage based on cancellation time and travel time
     * Uses Math.max to prevent negative time calculations
     * 
     * @return Refund percentage (0.50 or 0.90)
     */
    public static double calculateRefundPercentage(LocalDateTime cancellationTime, 
                                                   LocalDateTime travelTime) {
        long rawHours = ChronoUnit.HOURS.between(cancellationTime, travelTime);
        long hoursUntilTravel = Math.max(0, rawHours);
        
        log.info("[REFUND_CALC] calculateRefundPercentage: cancellationTime={}, travelTime={}, rawHours={}, clampedHours={}", 
            cancellationTime, travelTime, rawHours, hoursUntilTravel);
        
        if (hoursUntilTravel >= HOURS_THRESHOLD) {
            log.info("[REFUND_CALC] >= 24 hours -> 90% refund");
            return REFUND_PERCENTAGE_24H_OR_MORE;
        } else {
            log.info("[REFUND_CALC] < 24 hours -> 50% refund");
            return REFUND_PERCENTAGE_LESS_THAN_24H;
        }
    }

    /**
     * Calculate refund for partial cancellation
     */
    public static double calculatePartialRefundAmount(double originalPrice,
                                                      int totalQuantity,
                                                      int quantityToCancel,
                                                      LocalDateTime cancellationTime,
                                                      LocalDateTime travelTime) {
        double pricePerUnit = originalPrice / totalQuantity;
        double cancellationPrice = pricePerUnit * quantityToCancel;
        double refundPercentage = calculateRefundPercentage(cancellationTime, travelTime);
        double result = Math.round(cancellationPrice * refundPercentage * 100.0) / 100.0;
        log.info("[REFUND_CALC] calculatePartialRefund: pricePerUnit={}, cancellationPrice={}, refund%={}, result={}", 
            pricePerUnit, cancellationPrice, refundPercentage, result);
        return result;
    }

    /**
     * Get hours until travel (clamped to 0 minimum to prevent negative values)
     */
    public static long getHoursUntilTravel(LocalDateTime cancellationTime, 
                                          LocalDateTime travelTime) {
        long raw = ChronoUnit.HOURS.between(cancellationTime, travelTime);
        long clamped = Math.max(0, raw);
        log.info("[REFUND_CALC] getHoursUntilTravel: raw={}, clamped={}", raw, clamped);
        return clamped;
    }

    /**
     * Check if eligible for 90% refund
     */
    public static boolean isEligibleFor90PercentRefund(LocalDateTime cancellationTime, 
                                                       LocalDateTime travelTime) {
        long hoursUntilTravel = getHoursUntilTravel(cancellationTime, travelTime);
        boolean eligible = hoursUntilTravel >= HOURS_THRESHOLD;
        log.info("[REFUND_CALC] isEligibleFor90%: hours={}, eligible={}", hoursUntilTravel, eligible);
        return eligible;
    }

    /**
     * Parse a date string (YYYY-MM-DD) into LocalDateTime at end of day (23:59:59)
     * This ensures time-based refund calculations work correctly with date-only strings
     */
    public static LocalDateTime parseTravelDate(String dateString) {
        log.info("[REFUND_CALC] parseTravelDate input: '{}'", dateString);
        if (dateString == null || dateString.isEmpty()) {
            LocalDateTime fallback = LocalDateTime.now().plusDays(2);
            log.warn("[REFUND_CALC] Empty date string, using fallback: {}", fallback);
            return fallback;
        }
        try {
            // If it already has time component, parse directly
            if (dateString.contains("T")) {
                LocalDateTime parsed = LocalDateTime.parse(dateString);
                log.info("[REFUND_CALC] Parsed datetime with T: {}", parsed);
                return parsed;
            }
            // Parse as date and set to end of day
            LocalDate date = LocalDate.parse(dateString);
            LocalDateTime result = date.atTime(23, 59, 59);
            log.info("[REFUND_CALC] Parsed date-only, set to end of day: {}", result);
            return result;
        } catch (Exception e) {
            LocalDateTime fallback = LocalDateTime.now().plusDays(2);
            log.error("[REFUND_CALC] Failed to parse '{}', using fallback: {}. Error: {}", dateString, fallback, e.getMessage());
            return fallback;
        }
    }

    /**
     * Get refund policy description
     */
    public static String getRefundPolicyDescription(LocalDateTime cancellationTime, 
                                                    LocalDateTime travelTime) {
        long hoursUntilTravel = ChronoUnit.HOURS.between(cancellationTime, travelTime);
        
        if (hoursUntilTravel < 0) {
            return "Travel date has passed - 50% refund applicable";
        } else if (hoursUntilTravel >= HOURS_THRESHOLD) {
            return "Eligible for 90% refund (Cancelled 24+ hours before travel)";
        } else {
            return "Eligible for 50% refund (Cancelled less than 24 hours before travel)";
        }
    }
}
