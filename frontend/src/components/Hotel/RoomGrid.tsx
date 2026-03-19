import React, { useState, useEffect, useCallback } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Button } from "../ui/button";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface RoomResponse {
  id: string;
  hotelId: string;
  roomNumber: string;
  roomType: "STANDARD" | "DELUXE" | "SUITE" | "PENTHOUSE";
  available: boolean;
  pricePerNight: number;
  maxOccupancy: number;
  amenities: string[];
  images: string[];
  locked: boolean;
  lockedByMe: boolean;
}

interface RoomGridProps {
  hotelId: string;
  userId?: string;
  onRoomSelect?: (room: RoomResponse) => void;
  onRoomConfirm?: (room: RoomResponse) => void;
}

const ROOM_TYPE_CONFIG: Record<string, { color: string; label: string; icon: string }> = {
  STANDARD: { color: "bg-green-50 border-green-200 hover:bg-green-100", label: "Standard", icon: "🛏️" },
  DELUXE: { color: "bg-purple-50 border-purple-200 hover:bg-purple-100", label: "Deluxe", icon: "✨" },
  SUITE: { color: "bg-amber-50 border-amber-200 hover:bg-amber-100", label: "Suite", icon: "🌟" },
  PENTHOUSE: { color: "bg-rose-50 border-rose-200 hover:bg-rose-100", label: "Penthouse", icon: "👑" },
};

const RoomGrid: React.FC<RoomGridProps> = ({ hotelId, userId, onRoomSelect, onRoomConfirm }) => {
  const [rooms, setRooms] = useState<RoomResponse[]>([]);
  const [selectedRoomId, setSelectedRoomId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchRooms = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const url = `${API_BASE}/api/seatroom/rooms/hotel/${hotelId}${userId ? `?userId=${userId}` : ""}`;
      const res = await fetch(url);
      if (res.ok) {
        const json = await res.json();
        setRooms(json.data || []);
      }
    } catch (err) {
      console.error("Failed to fetch rooms:", err);
      setError("Failed to load room inventory");
    } finally {
      setLoading(false);
    }
  }, [hotelId, userId]);

  useEffect(() => {
    if (hotelId) fetchRooms();
  }, [hotelId, fetchRooms]);

  const handleSelect = async (room: RoomResponse) => {
    if (!room.available || (room.locked && !room.lockedByMe)) return;
    if (!userId) {
      setSelectedRoomId(room.id);
      onRoomSelect?.(room);
      return;
    }

    // Release any previously locked room
    const currentlyLocked = rooms.find(r => r.lockedByMe && r.id !== room.id);
    if (currentlyLocked) {
      await releaseRoom(currentlyLocked.id);
    }

    setActionLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/api/seatroom/rooms/${room.id}/lock`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId }),
      });
      const json = await res.json();
      if (res.ok) {
        setSelectedRoomId(room.id);
        onRoomSelect?.(json.data);
        await fetchRooms();
      } else {
        setError(json.error?.message || "Failed to lock room");
      }
    } catch (err) {
      setError("Failed to lock room");
    } finally {
      setActionLoading(false);
    }
  };

  const releaseRoom = async (roomId: string) => {
    if (!userId) return;
    try {
      await fetch(`${API_BASE}/api/seatroom/rooms/${roomId}/release`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId }),
      });
    } catch (err) {
      console.error("Failed to release room:", err);
    }
  };

  const handleConfirm = async () => {
    if (!selectedRoomId || !userId) return;
    setActionLoading(true);
    setError(null);
    try {
      const res = await fetch(`${API_BASE}/api/seatroom/rooms/${selectedRoomId}/confirm`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId }),
      });
      const json = await res.json();
      if (res.ok) {
        onRoomConfirm?.(json.data);
        setSelectedRoomId(null);
        await fetchRooms();
      } else {
        setError(json.error?.message || "Failed to confirm room");
      }
    } catch (err) {
      setError("Failed to confirm room booking");
    } finally {
      setActionLoading(false);
    }
  };

  const getRoomCardStyle = (room: RoomResponse) => {
    if (room.lockedByMe) return "border-2 border-blue-500 bg-blue-50 ring-2 ring-blue-200";
    if (!room.available) return "border border-gray-300 bg-gray-100 opacity-60";
    if (room.locked) return "border border-orange-300 bg-orange-50 opacity-75";
    return `border ${ROOM_TYPE_CONFIG[room.roomType]?.color || "bg-white border-gray-200"}`;
  };

  const selectedRoom = rooms.find(r => r.id === selectedRoomId);

  // Group rooms by type
  const roomsByType = rooms.reduce<Record<string, RoomResponse[]>>((acc, room) => {
    (acc[room.roomType] = acc[room.roomType] || []).push(room);
    return acc;
  }, {});

  if (loading) return <p className="text-center py-4">Loading room inventory...</p>;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Select Your Room</CardTitle>
      </CardHeader>
      <CardContent>
        {error && (
          <div className="bg-red-50 text-red-600 text-sm p-2 rounded mb-3">{error}</div>
        )}

        {rooms.length > 0 ? (
          <>
            {Object.entries(roomsByType).map(([type, typeRooms]) => {
              const config = ROOM_TYPE_CONFIG[type] || { label: type, icon: "🏨", color: "" };
              return (
                <div key={type} className="mb-6">
                  <h3 className="text-sm font-semibold text-gray-700 mb-2">
                    {config.icon} {config.label}
                  </h3>
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                    {typeRooms.map((room) => (
                      <button
                        key={room.id}
                        onClick={() => handleSelect(room)}
                        disabled={!room.available || (room.locked && !room.lockedByMe) || actionLoading}
                        className={`p-3 rounded-lg text-left transition-all ${getRoomCardStyle(room)}`}
                      >
                        <div className="flex justify-between items-start">
                          <span className="font-medium">Room {room.roomNumber}</span>
                          {!room.available && <span className="text-xs text-red-500 font-medium">Booked</span>}
                          {room.locked && !room.lockedByMe && <span className="text-xs text-orange-500 font-medium">Held</span>}
                          {room.lockedByMe && <span className="text-xs text-blue-600 font-medium">Selected</span>}
                        </div>
                        <p className="text-lg font-bold mt-1">₹{room.pricePerNight}<span className="text-xs font-normal text-gray-500">/night</span></p>
                        <p className="text-xs text-gray-500">Up to {room.maxOccupancy} guests</p>
                        {room.amenities.length > 0 && (
                          <div className="flex flex-wrap gap-1 mt-2">
                            {room.amenities.slice(0, 3).map((a) => (
                              <span key={a} className="text-[10px] bg-white/80 px-1.5 py-0.5 rounded border">{a}</span>
                            ))}
                            {room.amenities.length > 3 && (
                              <span className="text-[10px] text-gray-400">+{room.amenities.length - 3} more</span>
                            )}
                          </div>
                        )}
                      </button>
                    ))}
                  </div>
                </div>
              );
            })}

            {/* Selected room summary + confirm */}
            {selectedRoom && (
              <div className="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-200">
                <div className="flex justify-between items-center">
                  <div>
                    <p className="font-semibold">
                      Room {selectedRoom.roomNumber} — {ROOM_TYPE_CONFIG[selectedRoom.roomType]?.label}
                    </p>
                    <p className="text-sm text-gray-600">
                      Up to {selectedRoom.maxOccupancy} guests · {selectedRoom.amenities.join(", ")}
                    </p>
                    <p className="font-bold text-lg mt-1">₹{selectedRoom.pricePerNight}/night</p>
                  </div>
                  {userId && (
                    <Button onClick={handleConfirm} disabled={actionLoading}>
                      {actionLoading ? "Confirming..." : "Confirm Room"}
                    </Button>
                  )}
                </div>
              </div>
            )}
          </>
        ) : (
          <p className="text-gray-500 text-center">No rooms available for this hotel</p>
        )}
      </CardContent>
    </Card>
  );
};

export default RoomGrid;
