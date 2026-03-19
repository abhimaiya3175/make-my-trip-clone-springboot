import api, { unwrapApiResponse } from "@/utils/api";
import * as bookingService from "@/services/bookingService";

// ========== Legacy Cancellation Endpoints (Deprecated) ==========

export const getCancellationReasons = async () => {
  try {
    const res = await api.get(`/api/cancellation/reasons`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

export const getRefundStatuses = async () => {
  try {
    const res = await api.get(`/api/cancellation/refund-statuses`);
    return res.data;
  } catch (error) {
    console.log(error);
    throw error;
  }
};

// ========== New Booking-Based Cancellation Endpoints ==========

/**
 * Get cancellation preview with refund calculation
 * Uses the new booking API - simplified parameters
 */
export const getCancellationPreview = async (
  bookingId, bookingType, quantityToCancel, totalQuantity, originalPrice, travelDateTimeString
) => {
  try {
    console.log("[API] getCancellationPreview request:", {
      bookingId, bookingType, quantityToCancel, totalQuantity, originalPrice, travelDateTimeString,
    });
    
    // Get booking to check permissions and calculate refund
    const booking = await bookingService.getBookingById(bookingId);
    const permissions = await bookingService.getBookingPermissions(bookingId);
    
    if (!permissions?.canCancel) {
      throw new Error("This booking cannot be cancelled (travel date has passed or already cancelled)");
    }
    
    // Calculate refund percentage based on days before travel
    const now = new Date();
    const travelDate = new Date(booking.travelDate);
    const daysUntilTravel = Math.ceil((travelDate - now) / (1000 * 60 * 60 * 24));
    
    let refundPercentage = 0;
    if (daysUntilTravel > 7) refundPercentage = 100; // Full refund if > 7 days
    else if (daysUntilTravel > 3) refundPercentage = 75;  // 75% if 3-7 days
    else if (daysUntilTravel > 0) refundPercentage = 50;  // 50% if 0-3 days
    else refundPercentage = 0; // No refund on or after travel date
    
    const refundAmount = (originalPrice * refundPercentage) / 100;
    
    const preview = {
      bookingId,
      bookingType,
      quantityToCancel,
      totalQuantity,
      refundPercentage,
      refundAmount,
      originalPrice,
      newBookingStatus: quantityToCancel === totalQuantity ? "CANCELLED" : "PARTIALLY_CANCELLED",
    };
    
    console.log("[API] getCancellationPreview response:", preview);
    return preview;
  } catch (error) {
    console.error("[API] getCancellationPreview error:", error?.response?.data || error);
    throw error;
  }
};

/**
 * Process cancellation using new booking API
 * Cancels a booking and returns refund information
 */
export const processCancellation = async (
  userId, cancellationRequest, totalQuantity, originalPrice, travelDateTimeString
) => {
  try {
    console.log("[API] processCancellation request:", {
      userId, cancellationRequest, totalQuantity, originalPrice, travelDateTimeString,
    });
    
    const { bookingId, reason } = cancellationRequest;
    
    // Call new cancelBooking endpoint
    const result = await bookingService.cancelBooking(bookingId, userId, reason);
    
    // Transform response to match expected format
    const response = {
      success: true,
      bookingId,
      newBookingStatus: "CANCELLED",
      refundAmount: originalPrice,
      refundPercentage: 100,
      refundStatus: "INITIATED",
      message: "Booking cancelled successfully. Refund will be processed within 5-7 business days.",
      data: result,
    };
    
    console.log("[API] processCancellation response:", response);
    return response;
  } catch (error) {
    console.error("[API] processCancellation error:", error?.response?.data || error);
    throw error;
  }
};

/**
 * Get refund status (legacy - kept for backward compatibility)
 * Now returns booking status instead
 */
export const getRefundStatus = async (bookingId) => {
  try {
    const booking = await bookingService.getBookingById(bookingId);
    return {
      bookingId,
      status: booking.bookingStatus,
      paymentStatus: booking.paymentStatus,
      refundInitiatedDate: booking.bookingDate,
      refundExpectedDate: new Date(new Date().getTime() + 7 * 24 * 60 * 60 * 1000).toISOString(),
    };
  } catch (error) {
    console.log(error);
    throw error;
  }
};

/**
 * Get user's cancelled bookings
 * Filters bookings by status=CANCELLED
 */
export const getUserCancellations = async (userId) => {
  try {
    if (!userId) {
      console.error("[API] getUserCancellations called with empty userId!", { userId, type: typeof userId });
      throw new Error("userId is required");
    }
    
    console.log("[API] getUserCancellations request", {
      userId, userIdType: typeof userId,
    });
    
    // Get all user bookings
    const bookings = await bookingService.getUserBookings(userId);
    
    // Transform cancelled bookings to match old cancellation format
    const cancellations = (bookings || [])
      .filter(b => b.bookingStatus === "CANCELLED")
      .map((booking) => ({
        cancellationId: booking.id,
        bookingId: booking.id,
        userId: booking.userId,
        bookingType: booking.entityType,
        newBookingStatus: "CANCELLED",
        cancelledQuantity: 1,
        totalQuantity: 1,
        partialCancellation: false,
        refundPercentage: 100,
        refundAmount: booking.price || 0,
        originalPrice: booking.price || 0,
        refundStatus: booking.paymentStatus === "REFUNDED" ? "COMPLETED" : "PROCESSING",
        cancellationDate: booking.bookingDate,
        travelDate: booking.travelDate,
      }));
    
    console.log("[API] getUserCancellations SUCCESS. Response:", cancellations);
    return cancellations;
  } catch (error) {
    console.error("[API] getUserCancellations FAILED", {
      userId, status: error?.response?.status,
      statusText: error?.response?.statusText, data: error?.response?.data, message: error.message,
    });
    throw error;
  }
};

/**
 * Get cancellation details (legacy - kept for backward compatibility)
 */
export const getCancellationDetails = async (bookingId) => {
  try {
    const booking = await bookingService.getBookingById(bookingId);
    const permissions = await bookingService.getBookingPermissions(bookingId);
    
    return {
      cancellationId: booking.id,
      bookingId: booking.id,
      bookingType: booking.entityType,
      bookingStatus: booking.bookingStatus,
      canCancel: permissions?.canCancel,
      canReview: permissions?.canReview,
      permissionsMessage: permissions?.message,
      booking,
      permissions,
    };
  } catch (error) {
    console.log(error);
    throw error;
  }
};
