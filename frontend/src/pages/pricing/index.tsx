import React, { useEffect, useMemo, useState } from "react";
import { useSelector } from "react-redux";
import { getflight, gethotel } from "@/api";
import Loader from "@/components/Loader";
import PriceHistoryChart from "@/components/pricing/PriceHistoryChart";
import PriceFreezeButton from "@/components/pricing/PriceFreezeButton";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

type EntityType = "FLIGHT" | "HOTEL";

interface PriceData {
  finalPrice: number;
  basePrice: number;
  multiplier: number;
}

export default function PricingPage() {
  const user = useSelector((state: any) => state.user.user);
  const [loading, setLoading] = useState(true);
  const [entityType, setEntityType] = useState<EntityType>("FLIGHT");
  const [flights, setFlights] = useState<any[]>([]);
  const [hotels, setHotels] = useState<any[]>([]);
  const [selectedEntityId, setSelectedEntityId] = useState("");
  const [priceData, setPriceData] = useState<PriceData | null>(null);
  const [priceLoading, setPriceLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadData = async () => {
      try {
        const [flightData, hotelData] = await Promise.all([getflight(), gethotel()]);
        const safeFlights = Array.isArray(flightData) ? flightData : [];
        const safeHotels = Array.isArray(hotelData) ? hotelData : [];

        setFlights(safeFlights);
        setHotels(safeHotels);
        if (safeFlights.length > 0) setSelectedEntityId(safeFlights[0].id);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  const options = useMemo(() => {
    if (entityType === "FLIGHT") {
      return flights.map((flight) => ({
        id: flight.id,
        label: `${flight.flightName} (${flight.from} to ${flight.to})`,
      }));
    }
    return hotels.map((hotel) => ({
      id: hotel.id,
      label: `${hotel.hotelName} (${hotel.location})`,
    }));
  }, [entityType, flights, hotels]);

  useEffect(() => {
    if (entityType === "FLIGHT") {
      setSelectedEntityId(flights[0]?.id || "");
      return;
    }
    setSelectedEntityId(hotels[0]?.id || "");
  }, [entityType, flights, hotels]);

  useEffect(() => {
    const fetchPrice = async () => {
      if (!selectedEntityId) return;
      setPriceLoading(true);
      setError(null);
      try {
        const res = await fetch(
          `${API_BASE}/api/pricing/${entityType}/${selectedEntityId}${user?.id ? `?userId=${user.id}` : ""}`
        );
        const json = await res.json();
        if (!res.ok) {
          setError(json?.error?.message || "Failed to load dynamic price");
          setPriceData(null);
          return;
        }
        setPriceData(json.data || null);
      } catch {
        setError("Pricing service is unreachable");
        setPriceData(null);
      } finally {
        setPriceLoading(false);
      }
    };

    fetchPrice();
  }, [entityType, selectedEntityId, user?.id]);

  if (loading) return <Loader />;

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-8">
      <div className="max-w-6xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900">Dynamic Pricing</h1>
        <p className="text-gray-600 mt-1 mb-6">
          Dedicated page for pricing backend endpoints.
        </p>

        <div className="bg-white rounded-xl border p-4 grid grid-cols-1 md:grid-cols-2 gap-3 mb-6">
          <select
            value={entityType}
            onChange={(e) => setEntityType(e.target.value as EntityType)}
            className="w-full border rounded-lg px-3 py-2"
          >
            <option value="FLIGHT">Flight Pricing</option>
            <option value="HOTEL">Hotel Pricing</option>
          </select>

          <select
            value={selectedEntityId}
            onChange={(e) => setSelectedEntityId(e.target.value)}
            className="w-full border rounded-lg px-3 py-2"
          >
            {options.map((item) => (
              <option key={item.id} value={item.id}>
                {item.label}
              </option>
            ))}
          </select>
        </div>

        {priceLoading ? (
          <div className="bg-white rounded-xl border p-6 text-center text-gray-600">Loading current price...</div>
        ) : error ? (
          <div className="bg-red-50 border border-red-200 rounded-xl p-4 text-red-600">{error}</div>
        ) : priceData ? (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mb-6">
            <div className="bg-white rounded-xl border p-5">
              <p className="text-sm text-gray-500">Base Price</p>
              <p className="text-2xl font-bold">Rs. {priceData.basePrice}</p>
            </div>
            <div className="bg-white rounded-xl border p-5">
              <p className="text-sm text-gray-500">Multiplier</p>
              <p className="text-2xl font-bold">{priceData.multiplier}x</p>
            </div>
            <div className="bg-white rounded-xl border p-5">
              <p className="text-sm text-gray-500">Current Dynamic Price</p>
              <p className="text-2xl font-bold text-blue-700">Rs. {priceData.finalPrice}</p>
            </div>
          </div>
        ) : null}

        {selectedEntityId && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <div className="bg-white rounded-xl border p-4">
              <h2 className="text-lg font-semibold mb-3">Price Freeze</h2>
              <PriceFreezeButton
                entityId={selectedEntityId}
                entityType={entityType}
                userId={user?.id}
                currentPrice={priceData?.finalPrice || 0}
              />
            </div>
            <PriceHistoryChart entityId={selectedEntityId} entityType={entityType} days={7} />
          </div>
        )}
      </div>
    </div>
  );
}
