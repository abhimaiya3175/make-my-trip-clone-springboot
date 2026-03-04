import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import StarRating from "./StarRating";

interface Review {
  id: string;
  userId: string;
  userName?: string;
  bookingId: string;
  rating: number;
  comment: string;
  createdAt: string;
}

interface ReviewListProps {
  reviews: Review[];
}

const ReviewList: React.FC<ReviewListProps> = ({ reviews }) => {
  if (!reviews || reviews.length === 0) {
    return <p className="text-gray-500 text-sm">No reviews yet.</p>;
  }

  return (
    <div className="space-y-4">
      {reviews.map((review) => (
        <Card key={review.id}>
          <CardHeader className="pb-2">
            <div className="flex justify-between items-center">
              <CardTitle className="text-sm font-medium">
                {review.userName || "Anonymous"}
              </CardTitle>
              <span className="text-xs text-gray-400">
                {new Date(review.createdAt).toLocaleDateString()}
              </span>
            </div>
            <StarRating rating={review.rating} readonly />
          </CardHeader>
          <CardContent>
            <p className="text-sm text-gray-600">{review.comment}</p>
          </CardContent>
        </Card>
      ))}
    </div>
  );
};

export default ReviewList;
