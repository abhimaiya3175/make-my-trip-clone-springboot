import React, { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/router";
import { Plane, MapPin, Clock } from "lucide-react";
import { getflight } from "@/api";
import Loader from "@/components/Loader";

interface Flight {
  id: string;
  flightName: string;
  from: string;
  to: string;
  departureTime: string;
  arrivalTime: string;
  price: number;
  availableSeats: number;
}

export default function FlightsPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [flights, setFlights] = useState<Flight[]>([]);
  const [fromFilter, setFromFilter] = useState("");
  const [toFilter, setToFilter] = useState("");

  useEffect(() => {
    const loadFlights = async () => {
      try {
        const data = await getflight();
        setFlights(Array.isArray(data) ? data : []);
      } finally {
        setLoading(false);
      }
    };

    loadFlights();
  }, []);

  const filteredFlights = useMemo(() => {
    return flights.filter((flight) => {
      const fromOk = fromFilter
        ? flight.from.toLowerCase().includes(fromFilter.toLowerCase())
        : true;
      const toOk = toFilter
        ? flight.to.toLowerCase().includes(toFilter.toLowerCase())
        : true;
      return fromOk && toOk;
    });
  }, [flights, fromFilter, toFilter]);

  if (loading) return <Loader />;

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-8">
      <div className="max-w-6xl mx-auto">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Flights</h1>
          <p className="text-gray-600 mt-1">Separate page for flight backend endpoints.</p>
        </div>

        <div className="bg-white rounded-xl shadow-sm border p-4 mb-6 grid grid-cols-1 md:grid-cols-2 gap-3">
          <input
            type="text"
            placeholder="Filter by source city"
            value={fromFilter}
            onChange={(e) => setFromFilter(e.target.value)}
            className="w-full border rounded-lg px-3 py-2"
          />
          <input
            type="text"
            placeholder="Filter by destination city"
            value={toFilter}
            onChange={(e) => setToFilter(e.target.value)}
            className="w-full border rounded-lg px-3 py-2"
          />
        </div>

        {filteredFlights.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm border p-8 text-center text-gray-500">
            No flights found.
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {filteredFlights.map((flight) => (
              <div key={flight.id} className="bg-white rounded-xl shadow-sm border p-5">
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="text-lg font-semibold flex items-center gap-2">
                      <Plane className="w-5 h-5 text-red-500" />
                      {flight.flightName}
                    </h2>
                    <p className="text-sm text-gray-500">ID: {flight.id}</p>
                  </div>
                  <p className="text-xl font-bold text-blue-700">Rs. {flight.price}</p>
                </div>

                <div className="mt-4 space-y-2 text-sm text-gray-700">
                  <p className="flex items-center gap-2">
                    <MapPin className="w-4 h-4" />
                    {flight.from} to {flight.to}
                  </p>
                  <p className="flex items-center gap-2">
                    <Clock className="w-4 h-4" />
                    Departure: {new Date(flight.departureTime).toLocaleString()}
                  </p>
                  <p className="flex items-center gap-2">
                    <Clock className="w-4 h-4" />
                    Arrival: {new Date(flight.arrivalTime).toLocaleString()}
                  </p>
                  <p>Available Seats: {flight.availableSeats}</p>
                </div>

                <button
                  onClick={() => router.push(`/book-flight/${flight.id}`)}
                  className="mt-5 w-full bg-red-600 hover:bg-red-700 text-white font-medium py-2 rounded-lg"
                >
                  Book This Flight
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
