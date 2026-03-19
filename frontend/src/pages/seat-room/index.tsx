import React, { useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { Armchair, Building2 } from "lucide-react";
import { getflight, gethotel } from "@/api";
import SeatMap from "@/components/Flights/SeatMap";
import RoomGrid from "@/components/Hotel/RoomGrid";
import Loader from "@/components/Loader";

export default function SeatRoomPage() {
  const user = useSelector((state: any) => state.user.user);
  const [mode, setMode] = useState<"flight" | "hotel">("flight");
  const [loading, setLoading] = useState(true);
  const [flights, setFlights] = useState<any[]>([]);
  const [hotels, setHotels] = useState<any[]>([]);
  const [selectedFlightId, setSelectedFlightId] = useState("");
  const [selectedHotelId, setSelectedHotelId] = useState("");

  useEffect(() => {
    const loadData = async () => {
      try {
        const [flightData, hotelData] = await Promise.all([getflight(), gethotel()]);
        const safeFlights = Array.isArray(flightData) ? flightData : [];
        const safeHotels = Array.isArray(hotelData) ? hotelData : [];

        setFlights(safeFlights);
        setHotels(safeHotels);
        if (safeFlights.length > 0) setSelectedFlightId(safeFlights[0].id);
        if (safeHotels.length > 0) setSelectedHotelId(safeHotels[0].id);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const selectedFlight = useMemo(
    () => flights.find((flight) => flight.id === selectedFlightId),
    [flights, selectedFlightId]
  );

  const selectedHotel = useMemo(
    () => hotels.find((hotel) => hotel.id === selectedHotelId),
    [hotels, selectedHotelId]
  );

  if (loading) return <Loader />;

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-8">
      <div className="max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900">Seat and Room Selection</h1>
        <p className="text-gray-600 mt-1 mb-6">
          Dedicated page for the seat-room backend module endpoints.
        </p>

        <div className="flex gap-3 mb-6">
          <button
            onClick={() => setMode("flight")}
            className={`px-4 py-2 rounded-lg border font-medium ${
              mode === "flight" ? "bg-red-600 text-white border-red-600" : "bg-white"
            }`}
          >
            <span className="inline-flex items-center gap-2">
              <Armchair className="w-4 h-4" />
              Flight Seats
            </span>
          </button>
          <button
            onClick={() => setMode("hotel")}
            className={`px-4 py-2 rounded-lg border font-medium ${
              mode === "hotel" ? "bg-red-600 text-white border-red-600" : "bg-white"
            }`}
          >
            <span className="inline-flex items-center gap-2">
              <Building2 className="w-4 h-4" />
              Hotel Rooms
            </span>
          </button>
        </div>

        {mode === "flight" ? (
          <div className="space-y-4">
            <div className="bg-white rounded-xl border p-4">
              <label className="block text-sm font-medium mb-2">Select Flight</label>
              <select
                value={selectedFlightId}
                onChange={(e) => setSelectedFlightId(e.target.value)}
                className="w-full border rounded-lg px-3 py-2"
              >
                {flights.map((flight) => (
                  <option key={flight.id} value={flight.id}>
                    {flight.flightName} ({flight.from} to {flight.to})
                  </option>
                ))}
              </select>
              {selectedFlight && (
                <p className="text-xs text-gray-500 mt-2">
                  Available seats: {selectedFlight.availableSeats}
                </p>
              )}
            </div>
            {selectedFlightId && <SeatMap flightId={selectedFlightId} userId={user?.id} />}
          </div>
        ) : (
          <div className="space-y-4">
            <div className="bg-white rounded-xl border p-4">
              <label className="block text-sm font-medium mb-2">Select Hotel</label>
              <select
                value={selectedHotelId}
                onChange={(e) => setSelectedHotelId(e.target.value)}
                className="w-full border rounded-lg px-3 py-2"
              >
                {hotels.map((hotel) => (
                  <option key={hotel.id} value={hotel.id}>
                    {hotel.hotelName} ({hotel.location})
                  </option>
                ))}
              </select>
              {selectedHotel && (
                <p className="text-xs text-gray-500 mt-2">
                  Available rooms: {selectedHotel.availableRooms}
                </p>
              )}
            </div>
            {selectedHotelId && <RoomGrid hotelId={selectedHotelId} userId={user?.id} />}
          </div>
        )}
      </div>
    </div>
  );
}
