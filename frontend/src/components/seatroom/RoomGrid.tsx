import React from "react";

interface RoomGridProps {
  hotelId?: string;
  onRoomSelect?: (roomId: string) => void;
}

const RoomGrid: React.FC<RoomGridProps> = ({ hotelId, onRoomSelect }) => {
  return (
    <div className="p-4">
      <h3 className="font-semibold text-lg mb-2">Room Selection</h3>
      <p className="text-gray-500 text-sm">Select your room for hotel: {hotelId}</p>
      {/* Room grid will be rendered here */}
    </div>
  );
};

export default RoomGrid;
