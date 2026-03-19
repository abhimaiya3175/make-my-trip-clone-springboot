import React, { useState, useEffect } from "react";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface PriceInfo {
  entityId: string;
  entityType: string;
  basePrice: number;
  finalPrice: number;
  totalMultiplier: number;
  appliedRules: string[];
  frozenPrice?: number;
  freezeExpiresAt?: string;
}

interface DynamicPriceBadgeProps {
  entityId: string;
  entityType: "FLIGHT" | "HOTEL";
  userId?: string;
  className?: string;
}

const DynamicPriceBadge: React.FC<DynamicPriceBadgeProps> = ({
  entityId,
  entityType,
  userId,
  className = "",
}) => {
  const [price, setPrice] = useState<PriceInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPrice = async () => {
      setLoading(true);
      setError(null);
      try {
        const url = `${API_BASE}/api/pricing/${entityType}/${entityId}${userId ? `?userId=${userId}` : ""}`;
        const res = await fetch(url);
        if (res.ok) {
          const json = await res.json();
          setPrice(json.data);
        } else {
          setError("Price unavailable");
        }
      } catch (err) {
        setError("Price unavailable");
      } finally {
        setLoading(false);
      }
    };
    if (entityId) fetchPrice();
  }, [entityId, entityType, userId]);

  if (loading) return <span className={`text-xs text-gray-400 ${className}`}>Loading price...</span>;
  if (error) return <span className={`text-xs text-gray-400 ${className}`}>{error}</span>;
  if (!price) return null;

  const hasIncrease = price.totalMultiplier > 1;
  const hasDiscount = price.totalMultiplier < 1;
  const effectivePrice = price.frozenPrice ?? price.finalPrice;

  return (
    <div className={`inline-flex flex-col ${className}`}>
      <div className="flex items-baseline gap-2">
        <span className="text-xl font-bold">₹{effectivePrice.toFixed(0)}</span>
        {price.basePrice !== price.finalPrice && (
          <span className="text-sm text-gray-400 line-through">₹{price.basePrice.toFixed(0)}</span>
        )}
        {hasIncrease && (
          <span className="text-xs bg-red-100 text-red-700 px-1.5 py-0.5 rounded-full">
            +{((price.totalMultiplier - 1) * 100).toFixed(0)}%
          </span>
        )}
        {hasDiscount && (
          <span className="text-xs bg-green-100 text-green-700 px-1.5 py-0.5 rounded-full">
            {((1 - price.totalMultiplier) * 100).toFixed(0)}% off
          </span>
        )}
      </div>
      {price.appliedRules.length > 0 && (
        <p className="text-[10px] text-gray-500 mt-0.5">
          {price.appliedRules.join(" · ")}
        </p>
      )}
      {price.frozenPrice && (
        <p className="text-[10px] text-blue-600 mt-0.5">🔒 Frozen price</p>
      )}
    </div>
  );
};

export default DynamicPriceBadge;
