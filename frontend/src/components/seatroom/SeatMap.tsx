import React from "react";

interface SeatMapProps {
  flightId?: string;
  onSeatSelect?: (seatId: string) => void;
}

const SeatMap: React.FC<SeatMapProps> = ({ flightId, onSeatSelect }) => {
  return (
    <div className="p-4">
      <h3 className="font-semibold text-lg mb-2">Seat Map</h3>
      <p className="text-gray-500 text-sm">Select your seat for flight: {flightId}</p>
      {/* Interactive seat map will be rendered here */}
    </div>
  );
};

export default SeatMap;
