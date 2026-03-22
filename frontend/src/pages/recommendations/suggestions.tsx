import React from "react";
import RecommendationsSection from "@/components/recommendation/RecommendationsSection";
import { useSelector } from "react-redux";

export default function RecommendationsPage() {
  const user = useSelector((state: any) => state.user.user);
  const userId = user?.id || user?._id || "guest";

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto p-6">
        <h1 className="text-2xl font-bold text-gray-900">AI Recommendations</h1>
        <p className="text-sm text-gray-600 mt-1 mb-5">
          Suggestions are based on your activity history, similar travelers, and your feedback.
        </p>
        <RecommendationsSection userId={userId} />
      </div>
    </div>
  );
}
