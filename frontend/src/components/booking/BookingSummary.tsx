import React from "react";

interface BookingSummaryProps {
  booking: any | null;
  type: "flight" | "hotel";
  isLoading?: boolean;
}

const BookingSummary: React.FC<BookingSummaryProps> = ({ booking, type, isLoading = false }) => {
  if (isLoading || !booking) {
    return (
      <div className="border rounded-lg p-4 bg-white shadow-sm">
        <h3 className="font-semibold text-lg mb-2">Booking Summary</h3>
        <p className="text-sm text-gray-600">Loading booking details...</p>
      </div>
    );
  }

  const bookingIdentifier = booking?.id || booking?._id || booking?.bookingId;
  const bookingDate = booking?.bookingDate || booking?.date;
  const status = booking?.bookingStatus || "PENDING";

  return (
    <div className="border rounded-lg p-4 bg-white shadow-sm">
      <h3 className="font-semibold text-lg mb-2">Booking Summary</h3>
      <div className="space-y-2 text-sm">
        <p><span className="text-gray-500">Type:</span> {type}</p>
        <p><span className="text-gray-500">Booking ID:</span> {bookingIdentifier || "N/A"}</p>
        <p><span className="text-gray-500">Entity ID:</span> {booking?.entityId || booking?.bookingId || "N/A"}</p>
        <p><span className="text-gray-500">Status:</span> {status}</p>
        <p><span className="text-gray-500">Date:</span> {bookingDate || "N/A"}</p>
        <p><span className="text-gray-500">Quantity:</span> {booking?.quantity ?? "N/A"}</p>
        <p><span className="text-gray-500">Total Price:</span> ₹{booking?.totalPrice ?? 0}</p>
      </div>
    </div>
  );
};

export default BookingSummary;
