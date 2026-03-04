import React from "react";
import FlightSearch from "@/components/Flights/FlightSearch";
import Navbar from "@/components/Navbar";

export default function FlightSearchPage() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-6xl mx-auto p-6">
        <h1 className="text-2xl font-bold mb-6">Search Flights</h1>
        <FlightSearch />
      </div>
    </div>
  );
}
