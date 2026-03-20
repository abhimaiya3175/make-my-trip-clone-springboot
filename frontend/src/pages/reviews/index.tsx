import React, { useState, useEffect } from "react";
import { useRouter } from "next/router";
import { Star, ChevronRight } from "lucide-react";
import { useSelector } from "react-redux";
import ReviewForm from "@/components/reviews/ReviewForm";
import ReviewList from "@/components/reviews/ReviewList";
import Loader from "@/components/Loader";

interface EntityDetails {
  id: string;
  name: string;
  image?: string;
  rating?: number;
  reviewCount?: number;
}

const ReviewsPage = () => {
  const router = useRouter();
  const { entityType, entityId } = router.query;
  const normalizedEntityType = Array.isArray(entityType) ? entityType[0] : entityType;
  const normalizedEntityId = Array.isArray(entityId) ? entityId[0] : entityId;
  const hasEntityQuery = Boolean(normalizedEntityType && normalizedEntityId);
  const user = useSelector((state: any) => state.user.user);
  const currentUserId = user?.id || user?._id;
  const [entityDetails, setEntityDetails] = useState<EntityDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);

  useEffect(() => {
    if (!hasEntityQuery) {
      setLoading(false);
      setEntityDetails(null);
      return;
    }

    const fetchEntityDetails = async () => {
      setLoading(true);
      try {
        const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
        let endpoint = "";

        if (normalizedEntityType === "FLIGHT") {
          endpoint = `/api/flight/${normalizedEntityId}`;
        } else if (normalizedEntityType === "HOTEL") {
          endpoint = `/api/hotel/${normalizedEntityId}`;
        }

        const res = await fetch(`${API_BASE}${endpoint}`);
        if (res.ok) {
          const json = await res.json();
          const entity = json.data || json;

          setEntityDetails({
            id: entity.id || normalizedEntityId,
            name:
              normalizedEntityType === "FLIGHT"
                ? `${entity.from} → ${entity.to}`
                : entity.hotelName || entity.name,
            image:
              normalizedEntityType === "FLIGHT"
                ? "https://images.unsplash.com/photo-1578728657969-e34baf3722b9?auto=format&fit=crop&w=400"
                : entity.image ||
                  "https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=400",
            rating: entity.rating || 4.5,
            reviewCount: entity.reviewCount || 0,
          });
        }
      } catch (error) {
        console.error("Failed to fetch entity details:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchEntityDetails();
  }, [hasEntityQuery, normalizedEntityId, normalizedEntityType]);

  if (loading) {
    return <Loader />;
  }

  if (!hasEntityQuery) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-3xl mx-auto px-4 py-16">
          <div className="bg-white rounded-xl shadow-sm border p-8 text-center">
            <h1 className="text-2xl font-bold text-gray-900 mb-3">Reviews</h1>
            <p className="text-gray-600 mb-6">
              Open reviews from a hotel or flight details page, or browse entities first.
            </p>
            <div className="flex items-center justify-center gap-3">
              <button
                onClick={() => router.push("/hotels")}
                className="px-5 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700"
              >
                Browse Hotels
              </button>
              <button
                onClick={() => router.push("/flights")}
                className="px-5 py-2 rounded-lg bg-gray-100 text-gray-800 hover:bg-gray-200"
              >
                Browse Flights
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!entityDetails) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">
            Entity Not Found
          </h1>
          <p className="text-gray-600 mb-4">
            The {normalizedEntityType?.toLowerCase()} you're looking for doesn't exist.
          </p>
          <button
            onClick={() => router.back()}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Go Back
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Breadcrumb */}
      <div className="bg-white border-b">
        <div className="max-w-6xl mx-auto px-4 py-3">
          <div className="flex items-center space-x-2 text-sm">
            <a href="/" className="text-blue-600 hover:underline">
              Home
            </a>
            <ChevronRight className="w-4 h-4 text-gray-400" />
            <a
              href={
                normalizedEntityType === "FLIGHT"
                  ? "/flights"
                  : normalizedEntityType === "HOTEL"
                    ? "/hotels"
                    : "/"
              }
              className="text-blue-600 hover:underline"
            >
              {normalizedEntityType === "FLIGHT" ? "Flights" : "Hotels"}
            </a>
            <ChevronRight className="w-4 h-4 text-gray-400" />
            <span className="text-gray-600">{entityDetails.name}</span>
          </div>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Entity Card */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm p-6 sticky top-4">
              <img
                src={entityDetails.image}
                alt={entityDetails.name}
                className="w-full h-48 object-cover rounded-lg mb-4"
              />
              <h2 className="text-xl font-bold mb-3 line-clamp-2">
                {entityDetails.name}
              </h2>

              {/* Rating Summary */}
              <div className="mb-4 p-4 bg-blue-50 rounded-lg">
                <div className="flex items-center gap-2 mb-2">
                  <div className="flex items-center">
                    {[...Array(5)].map((_, i) => (
                      <Star
                        key={i}
                        className={`w-5 h-5 ${
                          i < Math.round(entityDetails.rating || 0)
                            ? "text-yellow-400 fill-current"
                            : "text-gray-300"
                        }`}
                      />
                    ))}
                  </div>
                  <span className="font-bold text-lg">
                    {entityDetails.rating?.toFixed(1)}
                  </span>
                </div>
                <p className="text-sm text-gray-600">
                  Based on {entityDetails.reviewCount} reviews
                </p>
              </div>

              {currentUserId && (
                <button
                  onClick={() => setShowForm(!showForm)}
                  className={`w-full py-2 rounded-lg font-semibold transition-colors ${
                    showForm
                      ? "bg-gray-200 text-gray-800 hover:bg-gray-300"
                      : "bg-blue-600 text-white hover:bg-blue-700"
                  }`}
                >
                  {showForm ? "Cancel" : "Write a Review"}
                </button>
              )}
            </div>
          </div>

          {/* Reviews Section */}
          <div className="lg:col-span-2">
            {/* Review Form */}
            {showForm && currentUserId && (
              <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
                <h3 className="text-lg font-bold mb-4">Share Your Experience</h3>
                <ReviewForm
                  entityType={normalizedEntityType as "FLIGHT" | "HOTEL"}
                  entityId={normalizedEntityId as string}
                  userId={currentUserId}
                  userName={user?.name || "Anonymous"}
                  onSuccess={() => {
                    setShowForm(false);
                  }}
                />
              </div>
            )}

            {/* Reviews List */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h3 className="text-lg font-bold mb-4">Customer Reviews</h3>
              <ReviewList
                entityType={normalizedEntityType as "FLIGHT" | "HOTEL"}
                entityId={normalizedEntityId as string}
                currentUserId={currentUserId}
                currentUserName={user?.name || "Anonymous"}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ReviewsPage;
