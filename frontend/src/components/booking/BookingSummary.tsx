import React from "react";

interface BookingSummaryProps {
  booking: any;
  type: "flight" | "hotel";
}

const BookingSummary: React.FC<BookingSummaryProps> = ({ booking, type }) => {
  return (
    <div className="border rounded-lg p-4 bg-white shadow-sm">
      <h3 className="font-semibold text-lg mb-2">Booking Summary</h3>
      <div className="space-y-2 text-sm">
        <p><span className="text-gray-500">Type:</span> {type}</p>
        <p><span className="text-gray-500">Booking ID:</span> {booking?.bookingId}</p>
        <p><span className="text-gray-500">Date:</span> {booking?.date}</p>
        <p><span className="text-gray-500">Quantity:</span> {booking?.quantity}</p>
        <p><span className="text-gray-500">Total Price:</span> ₹{booking?.totalPrice}</p>
      </div>
    </div>
  );
};

export default BookingSummary;
