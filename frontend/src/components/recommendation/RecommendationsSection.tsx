import React, { useState, useEffect, useCallback } from "react";
import RecommendationCard from "./RecommendationCard";
import api from "@/utils/api";

interface Recommendation {
  id: string;
  userId: string;
  itemId: string;
  itemType: "FLIGHT" | "HOTEL";
  score: number;
  reason: string;
  tags?: string[];
  createdAt: string;
}

interface RecommendationsSectionProps {
  userId: string;
  itemType?: "FLIGHT" | "HOTEL";
  title?: string;
}

const RecommendationsSection: React.FC<RecommendationsSectionProps> = ({
  userId,
  itemType,
  title = "Recommended for You",
}) => {
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchRecommendations = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const params = itemType ? { itemType } : {};
      const res = await api.get(`/api/recommendations/user/${userId}`, { params });
      setRecommendations(res.data?.data || []);
    } catch {
      setError("Could not load recommendations.");
    } finally {
      setLoading(false);
    }
  }, [userId, itemType]);

  useEffect(() => {
    if (userId) fetchRecommendations();
  }, [userId, fetchRecommendations]);

  if (loading) {
    return (
      <div className="py-8 text-center text-gray-400">
        Loading recommendations...
      </div>
    );
  }

  if (error) {
    return <div className="py-8 text-center text-red-500 text-sm">{error}</div>;
  }

  if (recommendations.length === 0) {
    return (
      <div className="py-8 text-center text-gray-400 text-sm">
        No recommendations yet. Browse flights and hotels to get personalized suggestions!
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <h2 className="text-xl font-bold">{title}</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {recommendations.map((rec) => (
          <RecommendationCard
            key={rec.id}
            recommendation={rec}
            userId={userId}
            onFeedback={fetchRecommendations}
          />
        ))}
      </div>
    </div>
  );
};

export default RecommendationsSection;
