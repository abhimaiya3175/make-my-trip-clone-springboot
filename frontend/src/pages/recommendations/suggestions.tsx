import React from "react";
import RecommendationsSection from "@/components/recommendation/RecommendationsSection";
import { useSelector } from "react-redux";

export default function RecommendationsPage() {
  const user = useSelector((state: any) => state.user);
  const userId = user?.id || user?._id || "guest";

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto p-6">
        <RecommendationsSection userId={userId} />
      </div>
    </div>
  );
}
