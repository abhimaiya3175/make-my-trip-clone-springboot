import React, { useState, useEffect, useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Button } from "../ui/button";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

const buildAuthHeaders = () => {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("authToken");
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }
  return headers;
};

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
  requiredSeats?: number;
  onSeatSelect?: (seats: SeatResponse[]) => void;
  onSeatConfirm?: (seats: SeatResponse[]) => void;
}

const SeatMap: React.FC<SeatMapProps> = ({ flightId, userId, requiredSeats = 1, onSeatSelect, onSeatConfirm }) => {
  const [seats, setSeats] = useState<SeatResponse[]>([]);
  const [selectedSeatIds, setSelectedSeatIds] = useState<string[]>([]);
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
    
    // Toggle seat selection
    const isAlreadySelected = selectedSeatIds.includes(seat.id);
    
    if (isAlreadySelected) {
      // Deselect and release
      if (userId) {
        await releaseSeat(seat.id);
      }
      setSelectedSeatIds(selectedSeatIds.filter(id => id !== seat.id));
    } else {
      // Can we add more seats?
      if (selectedSeatIds.length >= requiredSeats) {
        setError(`You can only select ${requiredSeats} seat(s).`);
        return;
      }

      // Select new seat
      setActionLoading(true);
      try {
        const res = await fetch(`${API_BASE}/api/seatroom/seats/${seat.id}/lock`, {
          method: "POST",
          headers: buildAuthHeaders(),
          body: JSON.stringify({ userId }),
        });
        const json = await res.json();
        if (res.ok) {
          setSelectedSeatIds([...selectedSeatIds, seat.id]);
          onSeatSelect?.([...selectedSeatIds.map(id => seats.find(s => s.id === id)).filter(Boolean) as SeatResponse[], json.data]);
          await fetchSeats();
        } else {
          if (res.status === 401) {
            setError("Please log in again to lock seats.");
          } else {
            setError(json.error?.message || "Failed to lock seat");
          }
        }
      } catch (err) {
        setError("Failed to lock seat");
      } finally {
        setActionLoading(false);
      }
    }
  };

  const releaseSeat = async (seatId: string) => {
    if (!userId) return;
    try {
      await fetch(`${API_BASE}/api/seatroom/seats/${seatId}/release`, {
        method: "POST",
        headers: buildAuthHeaders(),
        body: JSON.stringify({ userId }),
      });
    } catch (err) {
      console.error("Failed to release seat:", err);
    }
  };

  const handleConfirm = async () => {
    if (selectedSeatIds.length === 0 || selectedSeatIds.length !== requiredSeats || !userId) return;
    
    setActionLoading(true);
    setError(null);
    try {
      // Confirm all selected seats
      const confirmResponses = await Promise.all(
        selectedSeatIds.map(seatId =>
          fetch(`${API_BASE}/api/seatroom/seats/${seatId}/confirm`, {
            method: "POST",
            headers: buildAuthHeaders(),
            body: JSON.stringify({ userId }),
          })
        )
      );

      const allSuccessful = confirmResponses.every(res => res.ok);
      
      if (allSuccessful) {
        const confirmedSeats = selectedSeatIds
          .map(id => seats.find(s => s.id === id))
          .filter(Boolean) as SeatResponse[];
        
        onSeatConfirm?.(confirmedSeats);
        setSelectedSeatIds([]);
        await fetchSeats();
      } else {
        const failedResponse = confirmResponses.find(res => !res.ok);
        if (failedResponse?.status === 401) {
          setError("Please log in again to confirm seats.");
        } else {
          setError("Failed to confirm one or more seats");
        }
      }
    } catch (err) {
      setError("Failed to confirm seat bookings");
    } finally {
      setActionLoading(false);
    }
  };

  const getSeatColor = (seat: SeatResponse) => {
    if (selectedSeatIds.includes(seat.id)) return "bg-blue-500 text-white ring-2 ring-blue-300";
    if (!seat.available) return "bg-gray-400 cursor-not-allowed";
    if (seat.locked) return "bg-orange-300 cursor-not-allowed";
    switch (seat.seatClass) {
      case "FIRST": return "bg-yellow-100 hover:bg-yellow-200 cursor-pointer";
      case "BUSINESS": return "bg-purple-100 hover:bg-purple-200 cursor-pointer";
      default: return "bg-green-100 hover:bg-green-200 cursor-pointer";
    }
  };

  const selectedSeats = selectedSeatIds
    .map(id => seats.find(s => s.id === id))
    .filter(Boolean) as SeatResponse[];
  
  const isComplete = selectedSeatIds.length === requiredSeats;

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
            <div className="max-h-[45vh] overflow-y-auto pr-1 rounded-md border border-gray-100 bg-white">
              <div className="grid grid-cols-6 gap-1 max-w-md mx-auto p-2">
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

              {/* Sticky legend while scrolling seat grid */}
              <div className="sticky bottom-0 border-t bg-white/95 px-2 py-2 backdrop-blur-sm">
                <div className="flex flex-wrap gap-3 text-xs justify-center">
                  <span className="flex items-center gap-1"><span className="w-3 h-3 bg-green-100 border rounded" /> Economy</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 bg-purple-100 border rounded" /> Business</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 bg-yellow-100 border rounded" /> First</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 bg-gray-400 border rounded" /> Taken</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 bg-orange-300 border rounded" /> Held</span>
                  <span className="flex items-center gap-1"><span className="w-3 h-3 bg-blue-500 border rounded" /> Selected</span>
                </div>
              </div>
            </div>

            {/* Selected seat summary + confirm */}
            {selectedSeats.length > 0 && (
              <div className="mt-4 p-3 bg-blue-50 rounded-lg">
                <div className="flex justify-between items-start">
                  <div className="flex-1">
                    <p className="font-semibold mb-2">
                      Selected Seats ({selectedSeats.length}/{requiredSeats})
                    </p>
                    <div className="space-y-2">
                      {selectedSeats.map((seat) => (
                        <div key={seat.id} className="flex justify-between text-sm">
                          <span>{seat.seatNumber} ({seat.seatClass})</span>
                          <span className="font-medium">₹{seat.effectivePrice}</span>
                        </div>
                      ))}
                    </div>
                    <div className="border-t mt-2 pt-2 font-bold">
                      <div className="flex justify-between">
                        <span>Total Seat Charges:</span>
                        <span>₹{selectedSeats.reduce((sum, s) => sum + s.effectivePrice, 0)}</span>
                      </div>
                    </div>
                  </div>
                  {userId && (
                    <Button 
                      onClick={handleConfirm} 
                      disabled={actionLoading || !isComplete}
                      className="ml-4 flex-shrink-0"
                    >
                      {actionLoading ? "Confirming..." : isComplete ? "Confirm Seats" : `${requiredSeats - selectedSeats.length} more`}
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
