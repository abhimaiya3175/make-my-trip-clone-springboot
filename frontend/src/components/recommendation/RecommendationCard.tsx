import React from "react";

interface RecommendationCardProps {
  recommendation?: any;
}

const RecommendationCard: React.FC<RecommendationCardProps> = ({ recommendation }) => {
  return (
    <div className="border rounded-lg p-4 bg-white shadow-sm hover:shadow-md transition-shadow">
      <h3 className="font-semibold">{recommendation?.itemType || "Recommendation"}</h3>
      <p className="text-gray-500 text-sm">{recommendation?.reason || "Based on your preferences"}</p>
      <p className="text-sm mt-2">Score: {recommendation?.score || "N/A"}</p>
    </div>
  );
};

export default RecommendationCard;
