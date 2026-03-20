import api, { unwrapApiResponse } from "@/utils/api";

const requireAuthToken = () => {
  if (typeof window === "undefined" || !localStorage) {
    return;
  }
  const token = localStorage.getItem("authToken");
  if (!token) {
    throw new Error("Authentication required. Please login again.");
  }
};

// ========== Legacy Booking Endpoints ==========

export const handleflightbooking = async (userId, flightId, seats, price, date, seatNumbers = []) => {
  try {
    let url = `/api/bookings/flight?userId=${userId}&flightId=${flightId}&seats=${seats}&price=${price}`;
    if (date) {
      const dateStr = typeof date === 'string' ? date.substring(0, 10) : date;
      url += `&date=${dateStr}`;
    }
    if (Array.isArray(seatNumbers) && seatNumbers.length > 0) {
      url += `&seatNumbers=${encodeURIComponent(seatNumbers.join(","))}`;
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
    requireAuthToken();
    const res = await api.get(`/api/bookings`);
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
    requireAuthToken();
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
    requireAuthToken();
    const res = await api.post(`/api/bookings/${bookingId}/cancel`, { reason });
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
    requireAuthToken();
    const res = await api.post(`/api/bookings/${bookingId}/review-submitted`, {});
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
    requireAuthToken();
    const res = await api.post(`/api/bookings`, bookingData);
    return unwrapApiResponse(res);
  } catch (error) {
    console.error('Create booking error:', error);
    throw error;
  }
};
