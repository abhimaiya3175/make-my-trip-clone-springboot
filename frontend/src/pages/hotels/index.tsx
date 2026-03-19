import React, { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/router";
import { BedDouble, MapPin } from "lucide-react";
import { gethotel } from "@/api";
import Loader from "@/components/Loader";

interface Hotel {
  id: string;
  hotelName: string;
  location: string;
  pricePerNight: number;
  availableRooms: number;
}

export default function HotelsPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(true);
  const [hotels, setHotels] = useState<Hotel[]>([]);
  const [locationFilter, setLocationFilter] = useState("");

  useEffect(() => {
    const loadHotels = async () => {
      try {
        const data = await gethotel();
        setHotels(Array.isArray(data) ? data : []);
      } finally {
        setLoading(false);
      }
    };

    loadHotels();
  }, []);

  const filteredHotels = useMemo(() => {
    if (!locationFilter) return hotels;
    return hotels.filter((hotel) =>
      hotel.location.toLowerCase().includes(locationFilter.toLowerCase())
    );
  }, [hotels, locationFilter]);

  if (loading) return <Loader />;

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-8">
      <div className="max-w-6xl mx-auto">
        <div className="mb-6">
          <h1 className="text-3xl font-bold text-gray-900">Hotels</h1>
          <p className="text-gray-600 mt-1">Separate page for hotel backend endpoints.</p>
        </div>

        <div className="bg-white rounded-xl shadow-sm border p-4 mb-6">
          <input
            type="text"
            placeholder="Filter by location"
            value={locationFilter}
            onChange={(e) => setLocationFilter(e.target.value)}
            className="w-full border rounded-lg px-3 py-2"
          />
        </div>

        {filteredHotels.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm border p-8 text-center text-gray-500">
            No hotels found.
          </div>
        ) : (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            {filteredHotels.map((hotel) => (
              <div key={hotel.id} className="bg-white rounded-xl shadow-sm border p-5">
                <div className="flex items-start justify-between">
                  <div>
                    <h2 className="text-lg font-semibold flex items-center gap-2">
                      <BedDouble className="w-5 h-5 text-red-500" />
                      {hotel.hotelName}
                    </h2>
                    <p className="text-sm text-gray-500">ID: {hotel.id}</p>
                  </div>
                  <p className="text-xl font-bold text-blue-700">Rs. {hotel.pricePerNight}</p>
                </div>

                <div className="mt-4 space-y-2 text-sm text-gray-700">
                  <p className="flex items-center gap-2">
                    <MapPin className="w-4 h-4" />
                    {hotel.location}
                  </p>
                  <p>Available Rooms: {hotel.availableRooms}</p>
                </div>

                <button
                  onClick={() => router.push(`/book-hotel/${hotel.id}`)}
                  className="mt-5 w-full bg-red-600 hover:bg-red-700 text-white font-medium py-2 rounded-lg"
                >
                  Book This Hotel
                </button>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
