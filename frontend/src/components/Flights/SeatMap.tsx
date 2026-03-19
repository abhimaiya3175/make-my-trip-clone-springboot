import React, { useState, useEffect, useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Button } from "../ui/button";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface SeatResponse {
  id: string;
  flightId: string;
  seatNumber: string;
  row: string;
  column: string;
  seatClass: "ECONOMY" | "BUSINESS" | "FIRST";
  available: boolean;
  basePrice: number;
  premiumSurcharge: number;
  effectivePrice: number;
  locked: boolean;
  lockedByMe: boolean;
}

interface SeatMapProps {
  flightId: string;
  userId?: string;
  onSeatSelect?: (seat: SeatResponse) => void;
  onSeatConfirm?: (seat: SeatResponse) => void;
}

const SeatMap: React.FC<SeatMapProps> = ({ flightId, userId, onSeatSelect, onSeatConfirm }) => {
  const [seats, setSeats] = useState<SeatResponse[]>([]);
  const [selectedSeatId, setSelectedSeatId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchSeats = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const url = `${API_BASE}/api/seatroom/seats/flight/${flightId}${userId ? `?userId=${userId}` : ""}`;
      const res = await fetch(url);
      if (res.ok) {
        const json = await res.json();
        setSeats(json.data || []);
      }
    } catch (err) {
      console.error("Failed to fetch seats:", err);
      setError("Failed to load seat map");
    } finally {
      setLoading(false);
    }
  }, [flightId, userId]);

  useEffect(() => {
    if (flightId) fetchSeats();
  }, [flightId, fetchSeats]);

  const handleSelect = async (seat: SeatResponse) => {
    if (!seat.available || seat.locked) return;
    if (!userId) {
      setSelectedSeatId(seat.id);
      onSeatSelect?.(seat);
      return;
    }

    // If another seat was locked by me, release it first
    const currentlyLocked = seats.find(s => s.lockedByMe && s.id !== seat.id);
    if (currentlyLocked) {
      await releaseSeat(currentlyLocked.id);
    }

    // Lock the new seat
    setActionLoading(true);
    try {
      const res = await fetch(`${API_BASE}/api/seatroom/seats/${seat.id}/lock`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId }),
      });
      const json = await res.json();
      if (res.ok) {
        setSelectedSeatId(seat.id);
        onSeatSelect?.(json.data);
        await fetchSeats();
      } else {
        setError(json.error?.message || "Failed to lock seat");
      }
    } catch (err) {
      setError("Failed to lock seat");
    } finally {
      setActionLoading(false);
    }
  };

  const releaseSeat = async (seatId: string) => {
    if (!userId) return;
    try {
      await fetch(`${API_BASE}/api/seatroom/seats/${seatId}/release`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId }),
      });
    } catch (err) {
      console.error("Failed to release seat:", err);
    }
  };

  const handleConfirm = async () => {
    if (!selectedSeatId || !userId) return;
    setActionLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/api/seatroom/seats/${selectedSeatId}/confirm`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId }),
      });
      const json = await res.json();
      if (res.ok) {
        onSeatConfirm?.(json.data);
        setSelectedSeatId(null);
        await fetchSeats();
      } else {
        setError(json.error?.message || "Failed to confirm seat");
      }
    } catch (err) {
      setError("Failed to confirm seat booking");
    } finally {
      setActionLoading(false);
    }
  };

  const getSeatColor = (seat: SeatResponse) => {
    if (seat.lockedByMe) return "bg-blue-500 text-white ring-2 ring-blue-300";
    if (!seat.available) return "bg-gray-400 cursor-not-allowed";
    if (seat.locked) return "bg-orange-300 cursor-not-allowed";
    switch (seat.seatClass) {
      case "FIRST": return "bg-yellow-100 hover:bg-yellow-200 cursor-pointer";
      case "BUSINESS": return "bg-purple-100 hover:bg-purple-200 cursor-pointer";
      default: return "bg-green-100 hover:bg-green-200 cursor-pointer";
    }
  };

  const selectedSeat = seats.find(s => s.id === selectedSeatId);

  if (loading) return <p className="text-center py-4">Loading seat map...</p>;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Select Your Seat</CardTitle>
      </CardHeader>
      <CardContent>
        {error && (
          <div className="bg-red-50 text-red-600 text-sm p-2 rounded mb-3">{error}</div>
        )}

        {seats.length > 0 ? (
          <>
            {/* Seat grid grouped by rows */}
            <div className="grid grid-cols-6 gap-1 max-w-md mx-auto">
              {seats.map((seat) => (
                <button
                  key={seat.id}
                  onClick={() => handleSelect(seat)}
                  className={`p-2 text-xs rounded ${getSeatColor(seat)} transition-colors`}
                  disabled={!seat.available || (seat.locked && !seat.lockedByMe) || actionLoading}
                  title={`${seat.seatNumber} - ${seat.seatClass} ₹${seat.effectivePrice}${seat.premiumSurcharge > 0 ? " (Premium)" : ""}${!seat.available ? " (Taken)" : seat.locked ? " (Held)" : ""}`}
                >
                  {seat.seatNumber}
                </button>
              ))}
            </div>

            {/* Legend */}
            <div className="flex flex-wrap gap-3 mt-4 text-xs justify-center">
              <span className="flex items-center gap-1"><span className="w-3 h-3 bg-green-100 border rounded" /> Economy</span>
              <span className="flex items-center gap-1"><span className="w-3 h-3 bg-purple-100 border rounded" /> Business</span>
              <span className="flex items-center gap-1"><span className="w-3 h-3 bg-yellow-100 border rounded" /> First</span>
              <span className="flex items-center gap-1"><span className="w-3 h-3 bg-gray-400 border rounded" /> Taken</span>
              <span className="flex items-center gap-1"><span className="w-3 h-3 bg-orange-300 border rounded" /> Held</span>
              <span className="flex items-center gap-1"><span className="w-3 h-3 bg-blue-500 border rounded" /> Selected</span>
            </div>

            {/* Selected seat summary + confirm */}
            {selectedSeat && (
              <div className="mt-4 p-3 bg-blue-50 rounded-lg">
                <div className="flex justify-between items-center">
                  <div>
                    <p className="font-semibold">{selectedSeat.seatNumber} — {selectedSeat.seatClass}</p>
                    <p className="text-sm text-gray-600">
                      Base: ₹{selectedSeat.basePrice}
                      {selectedSeat.premiumSurcharge > 0 && (
                        <span className="text-amber-600"> + ₹{selectedSeat.premiumSurcharge} premium</span>
                      )}
                    </p>
                    <p className="font-bold text-lg">₹{selectedSeat.effectivePrice}</p>
                  </div>
                  {userId && (
                    <Button onClick={handleConfirm} disabled={actionLoading}>
                      {actionLoading ? "Confirming..." : "Confirm Seat"}
                    </Button>
                  )}
                </div>
              </div>
            )}
          </>
        ) : (
          <p className="text-gray-500 text-center">No seat data available for this flight</p>
        )}
      </CardContent>
    </Card>
  );
};

export default SeatMap;
