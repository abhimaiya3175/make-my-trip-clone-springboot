/**
 * Backward-compatible re-exports from feature-specific services.
 * Import directly from @/services/* for new code.
 */
export { login, signup, getuserbyemail, editprofile } from "@/services/authService";
export { getflight, addflight, editflight, getFlightById } from "@/services/flightService";
export { gethotel, addhotel, edithotel, getHotelById } from "@/services/hotelService";
export { handleflightbooking, handlehotelbooking, getUserBookings, getBookingById, cancelBooking } from "@/services/bookingService";
export {
  getCancellationReasons,
  getRefundStatuses,
  getCancellationPreview,
  processCancellation,
  getRefundStatus,
  getUserCancellations,
  getCancellationDetails,
} from "@/services/refundService";

