import React from "react";
import ReviewFormComponent from "@/components/reviews/ReviewForm";
import Navbar from "@/components/Navbar";

export default function ReviewPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-2xl mx-auto p-6">
        <h1 className="text-2xl font-bold mb-6">Write a Review</h1>
        <ReviewFormComponent />
      </div>
    </div>
  );
}
