import React, { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Button } from "../ui/button";

interface Seat {
  id: string;
  row: string;
  column: string;
  type: string; // "economy" | "business" | "first"
  available: boolean;
  price?: number;
}

interface SeatMapProps {
  flightId: string;
  onSeatSelect?: (seat: Seat) => void;
}

const SeatMap: React.FC<SeatMapProps> = ({ flightId, onSeatSelect }) => {
  const [seats, setSeats] = useState<Seat[]>([]);
  const [selectedSeat, setSelectedSeat] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchSeats = async () => {
      setLoading(true);
      try {
        const res = await fetch(`http://localhost:8080/api/seats/flight/${flightId}`);
        if (res.ok) {
          const data = await res.json();
          setSeats(data);
        }
      } catch (err) {
        console.error("Failed to fetch seats:", err);
      } finally {
        setLoading(false);
      }
    };

    if (flightId) fetchSeats();
  }, [flightId]);

  const handleSelect = (seat: Seat) => {
    if (!seat.available) return;
    setSelectedSeat(seat.id);
    onSeatSelect?.(seat);
  };

  const getSeatColor = (seat: Seat) => {
    if (seat.id === selectedSeat) return "bg-blue-500 text-white";
    if (!seat.available) return "bg-gray-400 cursor-not-allowed";
    switch (seat.type) {
      case "first": return "bg-yellow-100 hover:bg-yellow-200";
      case "business": return "bg-purple-100 hover:bg-purple-200";
      default: return "bg-green-100 hover:bg-green-200";
    }
  };

  if (loading) return <p className="text-center py-4">Loading seat map...</p>;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Seat Map</CardTitle>
      </CardHeader>
      <CardContent>
        {seats.length > 0 ? (
          <div className="grid grid-cols-6 gap-1 max-w-md mx-auto">
            {seats.map((seat) => (
              <button
                key={seat.id}
                onClick={() => handleSelect(seat)}
                className={`p-2 text-xs rounded ${getSeatColor(seat)} transition-colors`}
                disabled={!seat.available}
                title={`${seat.row}${seat.column} - ${seat.type} ${seat.available ? `₹${seat.price}` : "(Taken)"}`}
              >
                {seat.row}{seat.column}
              </button>
            ))}
          </div>
        ) : (
          <p className="text-gray-500 text-center">No seat data available for this flight</p>
        )}
        <div className="flex gap-4 mt-4 text-xs justify-center">
          <span className="flex items-center gap-1"><span className="w-3 h-3 bg-green-100 border rounded" /> Economy</span>
          <span className="flex items-center gap-1"><span className="w-3 h-3 bg-purple-100 border rounded" /> Business</span>
          <span className="flex items-center gap-1"><span className="w-3 h-3 bg-yellow-100 border rounded" /> First</span>
          <span className="flex items-center gap-1"><span className="w-3 h-3 bg-gray-400 border rounded" /> Taken</span>
        </div>
      </CardContent>
    </Card>
  );
};

export default SeatMap;
