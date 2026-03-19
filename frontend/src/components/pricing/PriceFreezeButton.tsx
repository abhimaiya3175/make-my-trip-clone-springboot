import React, { useState, useEffect, useRef, useCallback } from "react";
import { Button } from "../ui/button";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface PriceFreezeButtonProps {
  entityId: string;
  entityType: "FLIGHT" | "HOTEL";
  userId?: string;
  currentPrice: number;
}

interface FreezeInfo {
  id: string;
  frozenPrice: number;
  expiresAt: string;
  active: boolean;
}

const PriceFreezeButton: React.FC<PriceFreezeButtonProps> = ({
  entityId,
  entityType,
  userId,
  currentPrice,
}) => {
  const [freeze, setFreeze] = useState<FreezeInfo | null>(null);
  const [countdown, setCountdown] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  const checkExistingFreeze = useCallback(async () => {
    if (!userId) return;
    try {
      const res = await fetch(`${API_BASE}/api/pricing/freeze/user/${userId}`);
      if (res.ok) {
        const json = await res.json();
        const freezes: FreezeInfo[] = json.data || [];
        const match = freezes.find(
          (f) => f.active && f.frozenPrice > 0
        );
        // find the one matching this entity
        const entityFreeze = freezes.find(
          (f) => f.active
        );
        if (entityFreeze) {
          setFreeze(entityFreeze);
        }
      }
    } catch {
      // Ignore - no freeze found
    }
  }, [userId]);

  useEffect(() => {
    checkExistingFreeze();
  }, [checkExistingFreeze]);

  // Countdown timer
  useEffect(() => {
    if (!freeze?.expiresAt) return;

    const updateCountdown = () => {
      const expiresMs = new Date(freeze.expiresAt).getTime();
      const nowMs = Date.now();
      const diff = expiresMs - nowMs;

      if (diff <= 0) {
        setCountdown("Expired");
        setFreeze(null);
        if (timerRef.current) clearInterval(timerRef.current);
        return;
      }

      const hours = Math.floor(diff / 3600000);
      const mins = Math.floor((diff % 3600000) / 60000);
      const secs = Math.floor((diff % 60000) / 1000);
      setCountdown(`${hours}h ${mins}m ${secs}s`);
    };

    updateCountdown();
    timerRef.current = setInterval(updateCountdown, 1000);
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, [freeze?.expiresAt]);

  const handleFreeze = async () => {
    if (!userId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/api/pricing/freeze`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId, entityId, entityType }),
      });
      const json = await res.json();
      if (res.ok) {
        setFreeze(json.data);
      } else {
        setError(json.error?.message || "Failed to freeze price");
      }
    } catch {
      setError("Failed to freeze price");
    } finally {
      setLoading(false);
    }
  };

  if (!userId) return null;

  return (
    <div className="mt-3">
      {freeze ? (
        <div className="flex items-center gap-3 bg-blue-50 border border-blue-200 rounded-lg p-3">
          <div className="flex-1">
            <p className="text-sm font-medium text-blue-800">
              🔒 Price Frozen at ₹{freeze.frozenPrice}
            </p>
            <p className="text-xs text-blue-600">Expires in {countdown}</p>
          </div>
          {currentPrice > freeze.frozenPrice && (
            <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded-full font-medium">
              Saving ₹{(currentPrice - freeze.frozenPrice).toFixed(0)}
            </span>
          )}
        </div>
      ) : (
        <>
          <Button
            variant="outline"
            size="sm"
            onClick={handleFreeze}
            disabled={loading}
            className="w-full"
          >
            {loading ? "Freezing..." : `🔒 Freeze Price at ₹${currentPrice} for 24h`}
          </Button>
          {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
        </>
      )}
    </div>
  );
};

export default PriceFreezeButton;
