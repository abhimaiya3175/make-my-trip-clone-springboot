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

    // Persist cancellation via cancellation module so it appears in /my/cancellations.
    const res = await api.post(
      `/api/cancellation/cancel`,
      {
        ...cancellationRequest,
        bookingId,
        reason,
      },
      {
        params: {
          totalQuantity,
          originalPrice,
          travelDateTimeString,
        },
      }
    );

    const response = unwrapApiResponse(res);
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
 * Get user's cancelled bookings from secured endpoint
 */
export const getUserCancellations = async () => {
  try {
    const res = await api.get(`/api/cancellation/my/cancellations`);
    return unwrapApiResponse(res);
  } catch (error) {
    console.error("[API] getUserCancellations FAILED", {
      status: error?.response?.status,
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
