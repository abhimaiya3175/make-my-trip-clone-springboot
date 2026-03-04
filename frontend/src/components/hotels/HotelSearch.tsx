import React from "react";

interface HotelSearchProps {
  onSearch?: (location: string, date: string) => void;
}

const HotelSearch: React.FC<HotelSearchProps> = ({ onSearch }) => {
  return (
    <div className="p-4">
      <h2 className="text-xl font-bold mb-4">Search Hotels</h2>
      {/* Hotel search functionality is currently embedded in the home page */}
      <p className="text-gray-500">Hotel search component - integrated in home page</p>
    </div>
  );
};

export default HotelSearch;
