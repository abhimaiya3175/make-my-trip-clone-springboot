import React from "react";
import RecommendationCard from "@/components/recommendation/RecommendationCard";
import Navbar from "@/components/Navbar";

export default function RecommendationsPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-6xl mx-auto p-6">
        <h1 className="text-2xl font-bold mb-6">Recommendations</h1>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <RecommendationCard recommendation={{ itemType: "Flight", reason: "Popular route", score: 95 }} />
          <RecommendationCard recommendation={{ itemType: "Hotel", reason: "Highly rated", score: 90 }} />
        </div>
      </div>
    </div>
  );
}
