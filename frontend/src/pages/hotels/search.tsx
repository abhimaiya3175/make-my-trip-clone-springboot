import React from "react";
import HotelSearch from "@/components/hotels/HotelSearch";
import Navbar from "@/components/Navbar";

export default function HotelSearchPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-6xl mx-auto p-6">
        <h1 className="text-2xl font-bold mb-6">Search Hotels</h1>
        <HotelSearch />
      </div>
    </div>
  );
}
