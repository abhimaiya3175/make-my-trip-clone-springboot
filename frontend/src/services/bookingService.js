import api, { unwrapApiResponse } from "@/utils/api";

// ========== Legacy Booking Endpoints ==========

export const handleflightbooking = async (userId, flightId, seats, price, date) => {
  try {
    let url = `/api/bookings/flight?userId=${userId}&flightId=${flightId}&seats=${seats}&price=${price}`;
    if (date) {
      const dateStr = typeof date === 'string' ? date.substring(0, 10) : date;
      url += `&date=${dateStr}`;
    }
    const res = await api.post(url);
    return res.data;
  } catch (error) {
    console.log(error);
  }
};

export const handlehotelbooking = async (userId, hotelId, rooms, price, date) => {
  try {
    let url = `/api/bookings/hotel?userId=${userId}&hotelId=${hotelId}&rooms=${rooms}&price=${price}`;
    if (date) {
      const dateStr = typeof date === 'string' ? date.substring(0, 10) : date;
      url += `&date=${dateStr}`;
    }
    const res = await api.post(url);
    return res.data;
  } catch (error) {
    console.log(error);
  }
};

// ========== New Booking API Endpoints ==========

/**
 * Get all bookings for the current user
 */
export const getUserBookings = async (userId) => {
  try {
    const res = await api.get(`/api/bookings`, {
      headers: {
        'X-User-ID': userId
      }
    });
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Get user bookings error:', error);
    throw error;
  }
};

/**
 * Get a specific booking by ID
 */
export const getBookingById = async (bookingId) => {
  try {
    const res = await api.get(`/api/bookings/${bookingId}`);
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Get booking error:', error);
    throw error;
  }
};

/**
 * Check what actions are allowed for a booking (permissions)
 * Returns: { canPay, canCancel, canModify, canReview, message }
 */
export const getBookingPermissions = async (bookingId) => {
  try {
    const res = await api.get(`/api/bookings/${bookingId}/permissions`);
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Get booking permissions error:', error);
    throw error;
  }
};

/**
 * Cancel a booking
 * Only allowed before travel date
 */
export const cancelBooking = async (bookingId, userId, reason) => {
  try {
    const res = await api.post(
      `/api/bookings/${bookingId}/cancel`,
      { reason },
      {
        headers: {
          'X-User-ID': userId
        }
      }
    );
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Cancel booking error:', error);
    throw error;
  }
};

/**
 * Mark review as submitted for a booking
 * Only allowed after travel date
 */
export const markReviewSubmitted = async (bookingId, userId) => {
  try {
    const res = await api.post(
      `/api/bookings/${bookingId}/review-submitted`,
      {},
      {
        headers: {
          'X-User-ID': userId
        }
      }
    );
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Mark review submitted error:', error);
    throw error;
  }
};

/**
 * Confirm payment for a booking
 */
export const confirmPayment = async (bookingId) => {
  try {
    const res = await api.post(`/api/bookings/${bookingId}/confirm-payment`, {});
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Confirm payment error:', error);
    throw error;
  }
};

/**
 * Create a new booking
 */
export const createBooking = async (bookingData) => {
  try {
    const res = await api.post(
      `/api/bookings`,
      bookingData,
      {
        headers: {
          'X-User-ID': bookingData.userId
        }
      }
    );
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Create booking error:', error);
    throw error;
  }
};
