import React from "react";

interface ReviewFormProps {
  bookingId?: string;
  onSubmit?: (review: { rating: number; comment: string }) => void;
}

const ReviewForm: React.FC<ReviewFormProps> = ({ bookingId, onSubmit }) => {
  return (
    <div className="p-4 border rounded-lg">
      <h3 className="font-semibold text-lg mb-2">Write a Review</h3>
      <p className="text-gray-500 text-sm">Review form for booking: {bookingId}</p>
      {/* Review form will be implemented with star rating and comment */}
    </div>
  );
};

export default ReviewForm;
