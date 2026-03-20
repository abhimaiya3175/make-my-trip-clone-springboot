import React, { useEffect } from "react";
import { useRouter } from "next/router";

export default function ReviewPage() {
  const router = useRouter();
  const { entityType, entityId } = router.query;

  useEffect(() => {
    if (!router.isReady) return;
    const target = entityType && entityId
      ? `/reviews?entityType=${encodeURIComponent(String(entityType))}&entityId=${encodeURIComponent(String(entityId))}`
      : "/reviews";
    router.replace(target);
  }, [entityId, entityType, router]);

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto p-6">
        <p className="text-gray-600">Redirecting to reviews...</p>
      </div>
    </div>
  );
}
