import React, { useState } from "react";
import { Card, CardContent } from "../ui/card";
import { Button } from "../ui/button";
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

interface RecommendationCardProps {
  recommendation: Recommendation;
  userId: string;
  onFeedback?: () => void;
}

const RecommendationCard: React.FC<RecommendationCardProps> = ({
  recommendation,
  userId,
  onFeedback,
}) => {
  const [explanation, setExplanation] = useState<string | null>(null);
  const [loadingExplain, setLoadingExplain] = useState(false);
  const [feedbackSent, setFeedbackSent] = useState<string | null>(null);
  const [feedbackSubmitting, setFeedbackSubmitting] = useState(false);
  const [feedbackError, setFeedbackError] = useState<string | null>(null);
  const [showWhyTooltip, setShowWhyTooltip] = useState(false);

  const fetchExplanation = async () => {
    if (explanation) {
      setShowWhyTooltip((prev) => !prev);
      return;
    }
    setLoadingExplain(true);
    try {
      if (userId && userId !== "guest") {
        await api.post("/api/recommendations/events", {
          userId,
          eventType: "CLICK",
          entityId: recommendation.itemId,
          entityType: recommendation.itemType,
          metadata: "action:why_this",
        });
      }
      const res = await api.get(
        `/api/recommendations/${recommendation.itemId}/explain`,
        { params: { userId } }
      );
      setExplanation(res.data?.data?.explanation || "No details available.");
      setShowWhyTooltip(true);
    } catch {
      setExplanation("Could not load explanation.");
      setShowWhyTooltip(true);
    } finally {
      setLoadingExplain(false);
    }
  };

  const sendFeedback = async (type: "LIKE" | "SAVE" | "NOT_INTERESTED") => {
    if (feedbackSubmitting) {
      return;
    }

    const previousFeedback = feedbackSent;
    setFeedbackError(null);
    setFeedbackSubmitting(true);
    // Optimistic UI: acknowledge click immediately.
    setFeedbackSent(type);

    try {
      await api.post(`/api/recommendations/feedback`, {
          userId,
          itemId: recommendation.itemId,
          itemType: recommendation.itemType,
          feedbackType: type,
      });
      await onFeedback?.();
    } catch {
      setFeedbackSent(previousFeedback);
      setFeedbackError("Could not save feedback. Please try again.");
    } finally {
      setFeedbackSubmitting(false);
    }
  };

  const scorePercent = Math.round(recommendation.score * 100);
  const isHotel = recommendation.itemType === "HOTEL";

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardContent className="p-4">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-lg">{isHotel ? "🏨" : "✈️"}</span>
              <span className="font-semibold text-sm">
                {recommendation.itemId}
              </span>
              <span
                className={`text-xs px-2 py-0.5 rounded-full ${
                  isHotel
                    ? "bg-green-100 text-green-700"
                    : "bg-blue-100 text-blue-700"
                }`}
              >
                {recommendation.itemType}
              </span>
            </div>

            <p className="text-gray-600 text-sm mb-2">{recommendation.reason}</p>

            {recommendation.tags && recommendation.tags.length > 0 && (
              <div className="flex gap-1 flex-wrap mb-2">
                {recommendation.tags.map((tag) => (
                  <span
                    key={tag}
                    className="text-xs bg-gray-100 text-gray-600 px-2 py-0.5 rounded"
                  >
                    #{tag}
                  </span>
                ))}
              </div>
            )}

            <div className="flex items-center gap-3">
              <span className="text-xs text-gray-400">
                Match: {scorePercent}%
              </span>
              <div className="w-20 h-1.5 bg-gray-200 rounded-full overflow-hidden">
                <div
                  className="h-full rounded-full"
                  style={{
                    width: `${scorePercent}%`,
                    backgroundColor:
                      scorePercent >= 85
                        ? "#22c55e"
                        : scorePercent >= 70
                        ? "#eab308"
                        : "#f97316",
                  }}
                />
              </div>
            </div>
          </div>
        </div>

        {/* Explanation tooltip */}
        <div className="relative mt-2 inline-block">
          <button
            onClick={fetchExplanation}
            className="text-xs text-blue-600 hover:underline"
            disabled={loadingExplain}
            aria-describedby={`why-tooltip-${recommendation.id}`}
          >
            {loadingExplain ? "Loading..." : "Why this recommendation?"}
          </button>
          {showWhyTooltip && explanation && (
            <div
              id={`why-tooltip-${recommendation.id}`}
              role="tooltip"
              className="absolute left-0 z-10 mt-2 w-72 rounded-md border border-gray-200 bg-white p-3 text-xs text-gray-600 shadow-lg"
            >
              <p>{explanation}</p>
              <button
                onClick={() => setShowWhyTooltip(false)}
                className="mt-2 text-[11px] text-blue-600 hover:underline"
              >
                Close
              </button>
            </div>
          )}
        </div>

        {/* Feedback buttons */}
        <div className="flex gap-2 mt-3 border-t pt-2">
          {feedbackSent ? (
            <span className="text-xs text-green-600">
              {feedbackSubmitting ? "Saving..." : "✓ "}
              {feedbackSent === "LIKE" ? "Liked" : feedbackSent === "SAVE" ? "Saved" : "Hidden"}
            </span>
          ) : (
            <>
              <Button
                variant="ghost"
                size="sm"
                className="text-xs h-7"
                disabled={feedbackSubmitting}
                onClick={() => sendFeedback("LIKE")}
              >
                👍 Like
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className="text-xs h-7"
                disabled={feedbackSubmitting}
                onClick={() => sendFeedback("SAVE")}
              >
                🔖 Save
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className="text-xs h-7 text-gray-400"
                disabled={feedbackSubmitting}
                onClick={() => sendFeedback("NOT_INTERESTED")}
              >
                ✕ Not interested
              </Button>
            </>
          )}
        </div>
        {feedbackError && (
          <div className="mt-2 text-xs text-red-500">{feedbackError}</div>
        )}
      </CardContent>
    </Card>
  );
};

export default RecommendationCard;
