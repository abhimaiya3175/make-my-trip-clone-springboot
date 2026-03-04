import React from "react";

interface FlightSearchProps {
  onSearch?: (from: string, to: string, date: string) => void;
}

const FlightSearch: React.FC<FlightSearchProps> = ({ onSearch }) => {
  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Search Flights</h2>
      {/* Flight search functionality is currently embedded in the home page */}
      <p className="text-gray-500">Flight search component - integrated in home page</p>
    </div>
  );
};

export default FlightSearch;
