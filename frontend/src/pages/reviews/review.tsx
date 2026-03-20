import React from "react";
import { useRouter } from "next/router";
import ReviewFormComponent from "@/components/reviews/ReviewForm";

export default function ReviewPage() {
  const router = useRouter();
  const { entityType, entityId } = router.query;

  if (!entityType || !entityId) {
    return (
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-2xl mx-auto p-6">
          <p>Missing entityType or entityId query parameters.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto p-6">
        <h1 className="text-2xl font-bold mb-6">Write a Review</h1>
        <ReviewFormComponent
          entityType={entityType as 'FLIGHT' | 'HOTEL'}
          entityId={entityId as string}
        />
      </div>
    </div>
  );
}
