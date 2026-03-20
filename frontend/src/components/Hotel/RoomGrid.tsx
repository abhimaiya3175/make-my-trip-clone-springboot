import React, { useState, useEffect, useCallback, useMemo } from "react";
import { createPortal } from "react-dom";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Button } from "../ui/button";
import { X, ChevronLeft, ChevronRight, Eye } from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

const buildAuthHeaders = () => {
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("authToken");
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
  }
  return headers;
};

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
  isPanorama: boolean;
  locked: boolean;
  lockedByMe: boolean;
}

type RoomViewMode = "BUILDING" | "GRID";
type FloorPreference = "ANY" | "LOW" | "HIGH";
type RoomPreference = "ANY" | "STANDARD" | "DELUXE" | "SUITE" | "PENTHOUSE";
type RoomVisualState = "AVAILABLE" | "BOOKED" | "CLEANING" | "SELECTED";

interface RoomPreferences {
  preferredFloor: FloorPreference;
  preferredRoomType: RoomPreference;
  budgetMin: number;
  budgetMax: number;
}

interface RoomGridProps {
  hotelId: string;
  userId?: string;
  onRoomSelect?: (room: RoomResponse) => void;
  onRoomConfirm?: (room: RoomResponse) => void;
}

const ROOM_TYPE_CONFIG: Record<string, { label: string; icon: string }> = {
  STANDARD: { label: "Standard", icon: "🛏️" },
  DELUXE: { label: "Deluxe", icon: "✨" },
  SUITE: { label: "Suite", icon: "🌟" },
  PENTHOUSE: { label: "Penthouse", icon: "👑" },
};

const VISUAL_STATE_STYLE: Record<RoomVisualState, string> = {
  AVAILABLE: "bg-emerald-50 border-emerald-300 text-emerald-900 hover:bg-emerald-100",
  BOOKED: "bg-rose-50 border-rose-200 text-rose-900 opacity-70 cursor-not-allowed",
  CLEANING: "bg-amber-50 border-amber-300 text-amber-900 opacity-80 cursor-not-allowed",
  SELECTED: "bg-blue-50 border-blue-500 text-blue-900 ring-2 ring-blue-300",
};

const DEFAULT_PREFERENCES: RoomPreferences = {
  preferredFloor: "ANY",
  preferredRoomType: "ANY",
  budgetMin: 0,
  budgetMax: 0,
};

const RoomGrid: React.FC<RoomGridProps> = ({ hotelId, userId, onRoomSelect, onRoomConfirm }) => {
  const [rooms, setRooms] = useState<RoomResponse[]>([]);
  const [selectedRoomId, setSelectedRoomId] = useState<string | null>(null);
  const [viewMode, setViewMode] = useState<RoomViewMode>("BUILDING");
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [preferences, setPreferences] = useState<RoomPreferences>(DEFAULT_PREFERENCES);

  const [previewRoom, setPreviewRoom] = useState<RoomResponse | null>(null);
  const [currentImageIndex, setCurrentImageIndex] = useState(0);

  const preferenceStorageKey = `hotel-room-preferences-${hotelId}`;

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

  useEffect(() => {
    if (typeof window === "undefined") return;
    try {
      const raw = localStorage.getItem(preferenceStorageKey);
      if (!raw) {
        setPreferences(DEFAULT_PREFERENCES);
        return;
      }
      const parsed = JSON.parse(raw) as Partial<RoomPreferences>;
      setPreferences((prev) => ({
        ...prev,
        preferredFloor: (parsed.preferredFloor as FloorPreference) || "ANY",
        preferredRoomType: (parsed.preferredRoomType as RoomPreference) || "ANY",
        budgetMin: typeof parsed.budgetMin === "number" ? parsed.budgetMin : 0,
        budgetMax: typeof parsed.budgetMax === "number" ? parsed.budgetMax : 0,
      }));
    } catch {
      setPreferences(DEFAULT_PREFERENCES);
    }
  }, [preferenceStorageKey]);

  const minPrice = useMemo(() => {
    if (!rooms.length) return 0;
    return Math.min(...rooms.map((room) => room.pricePerNight));
  }, [rooms]);

  const maxPrice = useMemo(() => {
    if (!rooms.length) return 0;
    return Math.max(...rooms.map((room) => room.pricePerNight));
  }, [rooms]);

  useEffect(() => {
    if (!rooms.length) return;
    setPreferences((prev) => {
      const nextMin = prev.budgetMin === 0 ? minPrice : Math.max(minPrice, Math.min(prev.budgetMin, maxPrice));
      const nextMax = prev.budgetMax === 0 ? maxPrice : Math.max(minPrice, Math.min(prev.budgetMax, maxPrice));
      return {
        ...prev,
        budgetMin: Math.min(nextMin, nextMax),
        budgetMax: Math.max(nextMin, nextMax),
      };
    });
  }, [rooms, minPrice, maxPrice]);

  useEffect(() => {
    if (typeof window === "undefined" || !hotelId) return;
    localStorage.setItem(preferenceStorageKey, JSON.stringify(preferences));
  }, [hotelId, preferenceStorageKey, preferences]);

  const openPreview = (e: React.MouseEvent, room: RoomResponse) => {
    e.stopPropagation();
    setPreviewRoom(room);
    setCurrentImageIndex(0);
  };

  const nextImage = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (previewRoom) {
      setCurrentImageIndex((prev) => (prev + 1) % previewRoom.images.length);
    }
  };

  const prevImage = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (previewRoom) {
      setCurrentImageIndex((prev) => (prev - 1 + previewRoom.images.length) % previewRoom.images.length);
    }
  };

  useEffect(() => {
    if (previewRoom?.isPanorama && previewRoom.images.length > 0) {
      const script = document.createElement("script");
      script.src = "https://cdn.jsdelivr.net/npm/pannellum/build/pannellum.js";
      script.async = true;
      
      const link = document.createElement("link");
      link.rel = "stylesheet";
      link.href = "https://cdn.jsdelivr.net/npm/pannellum/build/pannellum.css";

      script.onload = () => {
        if ((window as any).pannellum) {
          (window as any).pannellum.viewer("panorama-container", {
            type: "equirectangular",
            panorama: previewRoom.images[0],
            autoLoad: true,
            showFullscreenCtrl: false,
          });
        }
      };

      document.body.appendChild(script);
      document.head.appendChild(link);

      return () => {
        if (document.body.contains(script)) document.body.removeChild(script);
        if (document.head.contains(link)) document.head.removeChild(link);
      };
    }
  }, [previewRoom]);

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
        headers: buildAuthHeaders(),
        body: JSON.stringify({ userId }),
      });
      const json = await res.json();
      if (res.ok) {
        setSelectedRoomId(room.id);
        onRoomSelect?.(json.data);
        await fetchRooms();
      } else {
        if (res.status === 401) {
          setError("Please log in again to lock rooms.");
        } else {
          setError(json.error?.message || "Failed to lock room");
        }
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
        headers: buildAuthHeaders(),
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
        headers: buildAuthHeaders(),
        body: JSON.stringify({ userId }),
      });
      const json = await res.json();
      if (res.ok) {
        onRoomConfirm?.(json.data);
        setSelectedRoomId(null);
        await fetchRooms();
      } else {
        if (res.status === 401) {
          setError("Please log in again to confirm rooms.");
        } else {
          setError(json.error?.message || "Failed to confirm room");
        }
      }
    } catch (err) {
      setError("Failed to confirm room booking");
    } finally {
      setActionLoading(false);
    }
  };

  const parseFloor = (roomNumber: string): number => {
    if (/^PH/i.test(roomNumber)) return 99;
    const onlyDigits = roomNumber.match(/\d+/)?.[0];
    if (!onlyDigits) return 1;
    const numeric = Number.parseInt(onlyDigits, 10);
    if (Number.isNaN(numeric)) return 1;
    if (numeric >= 100) return Math.max(1, Math.floor(numeric / 100));
    return numeric;
  };

  const getVisualState = (room: RoomResponse): RoomVisualState => {
    if (room.id === selectedRoomId || room.lockedByMe) return "SELECTED";
    if (!room.available) return "BOOKED";
    if (room.locked) return "CLEANING";
    return "AVAILABLE";
  };

  const averagePrice = useMemo(() => {
    if (!rooms.length) return 0;
    return rooms.reduce((acc, room) => acc + room.pricePerNight, 0) / rooms.length;
  }, [rooms]);

  const isPremiumRoom = useCallback((room: RoomResponse) => {
    const floor = parseFloor(room.roomNumber);
    const premiumType = ["DELUXE", "SUITE", "PENTHOUSE"].includes(room.roomType);
    return premiumType || floor >= 3 || room.pricePerNight >= averagePrice * 1.25;
  }, [averagePrice]);

  const isRoomSelectable = (room: RoomResponse) => room.available && (!room.locked || room.lockedByMe);

  const scoreRoomByPreferences = useCallback((room: RoomResponse) => {
    if (!isRoomSelectable(room)) return -1;
    let score = 0;
    const floor = parseFloor(room.roomNumber);

    if (preferences.preferredRoomType !== "ANY" && room.roomType === preferences.preferredRoomType) score += 4;
    if (preferences.preferredRoomType === "ANY") score += 1;

    if (preferences.preferredFloor === "LOW") {
      score += floor <= 2 ? 3 : 0;
    } else if (preferences.preferredFloor === "HIGH") {
      score += floor >= 3 ? 3 : 0;
    } else {
      score += 1;
    }

    if (room.pricePerNight >= preferences.budgetMin && room.pricePerNight <= preferences.budgetMax) {
      score += 3;
    }

    if (isPremiumRoom(room)) score += 1;
    return score;
  }, [isPremiumRoom, preferences]);

  const suggestedRoomId = useMemo(() => {
    if (!rooms.length) return null;
    const ranked = rooms
      .map((room) => ({ room, score: scoreRoomByPreferences(room) }))
      .filter((entry) => entry.score >= 0)
      .sort((a, b) => b.score - a.score || a.room.pricePerNight - b.room.pricePerNight);
    return ranked[0]?.room.id || null;
  }, [rooms, scoreRoomByPreferences]);

  const premiumRooms = useMemo(() => rooms.filter((room) => isPremiumRoom(room)), [rooms, isPremiumRoom]);

  const bestValuePremiumId = useMemo(() => {
    const candidate = premiumRooms
      .filter((room) => isRoomSelectable(room))
      .sort((a, b) => a.pricePerNight - b.pricePerNight)[0];
    return candidate?.id || null;
  }, [premiumRooms]);

  const selectedRoom = rooms.find(r => r.id === selectedRoomId);

  const upgradeSuggestion = useMemo(() => {
    if (!selectedRoom || selectedRoom.roomType !== "STANDARD") return null;
    const premiumCandidate = rooms
      .filter((room) => ["DELUXE", "SUITE", "PENTHOUSE"].includes(room.roomType) && isRoomSelectable(room))
      .sort((a, b) => a.pricePerNight - b.pricePerNight)[0];
    if (!premiumCandidate) return null;
    return {
      room: premiumCandidate,
      extra: Math.max(0, premiumCandidate.pricePerNight - selectedRoom.pricePerNight),
    };
  }, [rooms, selectedRoom]);

  const roomsByFloor = useMemo(() => {
    const grouped = rooms.reduce<Record<number, RoomResponse[]>>((acc, room) => {
      const floor = parseFloor(room.roomNumber);
      (acc[floor] = acc[floor] || []).push(room);
      return acc;
    }, {});

    Object.values(grouped).forEach((floorRooms) => {
      floorRooms.sort((a, b) => a.roomNumber.localeCompare(b.roomNumber));
    });

    return Object.entries(grouped)
      .map(([floor, floorRooms]) => ({ floor: Number.parseInt(floor, 10), rooms: floorRooms }))
      .sort((a, b) => b.floor - a.floor);
  }, [rooms]);

  // Group rooms by type
  const roomsByType = useMemo(() => {
    const grouped = rooms.reduce<Record<string, RoomResponse[]>>((acc, room) => {
      (acc[room.roomType] = acc[room.roomType] || []).push(room);
      return acc;
    }, {});
    Object.values(grouped).forEach((typedRooms) => {
      typedRooms.sort((a, b) => a.pricePerNight - b.pricePerNight);
    });
    return grouped;
  }, [rooms]);

  const roomTooltip = (room: RoomResponse) => {
    const visual = getVisualState(room);
    const stateLabel = visual === "CLEANING" ? "Cleaning / Maintenance" : visual;
    return [
      `Room ${room.roomNumber} (${ROOM_TYPE_CONFIG[room.roomType]?.label || room.roomType})`,
      `Price: ₹${room.pricePerNight}/night`,
      `Capacity: ${room.maxOccupancy} guest(s)`,
      `Status: ${stateLabel}`,
      room.amenities.length ? `Amenities: ${room.amenities.join(", ")}` : "Amenities: -",
    ].join("\n");
  };

  const RoomTile = ({ room, compact = false }: { room: RoomResponse; compact?: boolean }) => {
    const visualState = getVisualState(room);
    const isPremium = isPremiumRoom(room);
    const isSuggested = room.id === suggestedRoomId;
    const isBestValue = room.id === bestValuePremiumId;
    const selectable = isRoomSelectable(room) && !actionLoading;

    return (
      <div
        key={room.id}
        role="button"
        title={roomTooltip(room)}
        onClick={() => {
          if (selectable) handleSelect(room);
        }}
        className={[
          "rounded-xl border p-3 transition-all duration-200",
          compact ? "min-h-[96px]" : "min-h-[140px]",
          VISUAL_STATE_STYLE[visualState],
          selectable ? "cursor-pointer hover:-translate-y-0.5 hover:shadow-md" : "",
          isPremium ? "shadow-[0_0_0_1px_rgba(234,179,8,0.35)]" : "",
          isSuggested ? "ring-2 ring-indigo-300" : "",
        ].join(" ")}
      >
        <div className="flex items-start justify-between gap-2 mb-1">
          <div>
            <p className="font-semibold leading-tight">{room.roomNumber}</p>
            <p className="text-xs opacity-80">{ROOM_TYPE_CONFIG[room.roomType]?.label || room.roomType}</p>
          </div>
          <div className="flex flex-col items-end gap-1">
            {isSuggested && (
              <span className="text-[10px] bg-indigo-100 text-indigo-700 px-2 py-0.5 rounded-full">Recommended</span>
            )}
            {isBestValue && (
              <span className="text-[10px] bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full">Best Value</span>
            )}
          </div>
        </div>

        <p className="text-sm font-bold">₹{room.pricePerNight}<span className="text-xs font-normal"> / night</span></p>
        <p className="text-xs opacity-80">Up to {room.maxOccupancy} guests</p>
        {!compact && room.amenities.length > 0 && (
          <div className="mt-2 flex flex-wrap gap-1">
            {room.amenities.slice(0, 3).map((amenity) => (
              <span key={amenity} className="text-[10px] bg-white/80 border px-1.5 py-0.5 rounded">
                {amenity}
              </span>
            ))}
            {room.amenities.length > 3 && (
              <span className="text-[10px] opacity-80">+{room.amenities.length - 3}</span>
            )}
          </div>
        )}

        {room.images && room.images.length > 0 && !compact && (
          <Button
            type="button"
            className="mt-2 w-full text-xs h-8 flex items-center justify-center gap-1 bg-white hover:bg-gray-50 text-gray-700 border border-gray-300"
            onClick={(e) => openPreview(e, room)}
          >
            <Eye className="w-3 h-3" /> Preview
          </Button>
        )}
      </div>
    );
  };

  if (loading) return <p className="text-center py-4">Loading room inventory...</p>;

  return (
    <Card className="flex flex-col h-full">
      <CardHeader className="sticky top-0 bg-white z-20 border-b">
        <CardTitle>Select Your Room</CardTitle>
      </CardHeader>
      <CardContent className="relative flex-1 overflow-y-auto">
        {/* Cover screen overlay when action is in progress */}
        {actionLoading && (
          <div className="fixed inset-0 bg-black/40 backdrop-blur-sm z-50 flex items-center justify-center">
            <div className="flex flex-col items-center gap-3">
              <div className="w-8 h-8 rounded-full border-3 border-white border-t-blue-500 animate-spin"></div>
              <p className="text-white font-semibold text-lg">Processing...</p>
              <p className="text-white/80 text-sm">Please wait while we confirm your room</p>
            </div>
          </div>
        )}

        {error && (
          <div className="bg-red-50 text-red-600 text-sm p-2 rounded mb-3">{error}</div>
        )}

        {rooms.length > 0 ? (
          <>
            <div className="mb-4 rounded-xl border border-gray-200 bg-white p-3 sticky top-0 z-10">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    variant={viewMode === "BUILDING" ? "default" : "outline"}
                    className="h-8"
                    onClick={() => setViewMode("BUILDING")}
                  >
                    Building View
                  </Button>
                  <Button
                    type="button"
                    variant={viewMode === "GRID" ? "default" : "outline"}
                    className="h-8"
                    onClick={() => setViewMode("GRID")}
                  >
                    Room-Type Grid
                  </Button>
                </div>

                <p className="text-xs text-gray-500">Preferences auto-saved for this hotel</p>
              </div>

              <div className="mt-3 grid grid-cols-1 md:grid-cols-4 gap-3">
                <label className="text-xs font-medium text-gray-600">
                  Preferred Floor
                  <select
                    className="mt-1 w-full rounded-md border px-2 py-1.5 text-sm"
                    value={preferences.preferredFloor}
                    onChange={(e) => setPreferences((prev) => ({ ...prev, preferredFloor: e.target.value as FloorPreference }))}
                  >
                    <option value="ANY">Any</option>
                    <option value="LOW">Low Floor</option>
                    <option value="HIGH">High Floor</option>
                  </select>
                </label>

                <label className="text-xs font-medium text-gray-600">
                  Preferred Room Type
                  <select
                    className="mt-1 w-full rounded-md border px-2 py-1.5 text-sm"
                    value={preferences.preferredRoomType}
                    onChange={(e) => setPreferences((prev) => ({ ...prev, preferredRoomType: e.target.value as RoomPreference }))}
                  >
                    <option value="ANY">Any</option>
                    <option value="STANDARD">Standard</option>
                    <option value="DELUXE">Deluxe</option>
                    <option value="SUITE">Suite</option>
                    <option value="PENTHOUSE">Penthouse</option>
                  </select>
                </label>

                <label className="text-xs font-medium text-gray-600">
                  Budget Min
                  <input
                    type="number"
                    min={minPrice}
                    max={preferences.budgetMax || maxPrice}
                    className="mt-1 w-full rounded-md border px-2 py-1.5 text-sm"
                    value={preferences.budgetMin || minPrice}
                    onChange={(e) => {
                      const next = Number.parseInt(e.target.value, 10) || minPrice;
                      setPreferences((prev) => ({
                        ...prev,
                        budgetMin: Math.max(minPrice, Math.min(next, prev.budgetMax || maxPrice)),
                      }));
                    }}
                  />
                </label>

                <label className="text-xs font-medium text-gray-600">
                  Budget Max
                  <input
                    type="number"
                    min={preferences.budgetMin || minPrice}
                    max={maxPrice}
                    className="mt-1 w-full rounded-md border px-2 py-1.5 text-sm"
                    value={preferences.budgetMax || maxPrice}
                    onChange={(e) => {
                      const next = Number.parseInt(e.target.value, 10) || maxPrice;
                      setPreferences((prev) => ({
                        ...prev,
                        budgetMax: Math.min(maxPrice, Math.max(next, prev.budgetMin || minPrice)),
                      }));
                    }}
                  />
                </label>
              </div>
            </div>

            <div className="mb-4 rounded-xl border bg-gray-50 p-3">
              <h4 className="text-sm font-semibold mb-2">Legend</h4>
              <div className="flex flex-wrap gap-2 text-xs">
                <span className="px-2 py-1 rounded bg-emerald-100 text-emerald-800 border border-emerald-300">Available</span>
                <span className="px-2 py-1 rounded bg-rose-100 text-rose-800 border border-rose-300">Booked</span>
                <span className="px-2 py-1 rounded bg-amber-100 text-amber-800 border border-amber-300">Cleaning / Maintenance</span>
                <span className="px-2 py-1 rounded bg-blue-100 text-blue-800 border border-blue-300">Selected</span>
                <span className="px-2 py-1 rounded bg-yellow-100 text-yellow-800 border border-yellow-300">Premium</span>
              </div>
            </div>

            {viewMode === "BUILDING" ? (
              <div className="space-y-3">
                {roomsByFloor.map(({ floor, rooms: floorRooms }) => (
                  <div key={floor} className="rounded-xl border border-slate-200 bg-slate-50 p-3">
                    <div className="flex items-center justify-between mb-2">
                      <h4 className="text-sm font-semibold text-slate-800">
                        {floor === 99 ? "Penthouse" : `Floor ${floor}`}
                      </h4>
                      <span className="text-[11px] text-gray-500">{floorRooms.length} room(s)</span>
                    </div>
                    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-2">
                      {floorRooms.map((room) => (
                        <RoomTile key={room.id} room={room} compact />
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="space-y-5">
                {Object.entries(roomsByType).map(([type, typeRooms]) => {
                  const config = ROOM_TYPE_CONFIG[type] || { label: type, icon: "🏨" };
                  return (
                    <div key={type} className="rounded-xl border border-gray-200 p-3 bg-white">
                      <h3 className="text-sm font-semibold text-gray-700 mb-3">
                        {config.icon} {config.label}
                      </h3>
                      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-3">
                        {typeRooms.map((room) => (
                          <RoomTile key={room.id} room={room} />
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            {/* Selected room summary + confirm */}
            {selectedRoom && (
              <div className="sticky bottom-0 mt-6 p-4 bg-blue-50 rounded-lg border border-blue-200 z-20 bg-gradient-to-b from-blue-50 to-blue-100">
                <div className="flex justify-between items-center">
                  <div>
                    <p className="font-semibold">
                      Room {selectedRoom.roomNumber} — {ROOM_TYPE_CONFIG[selectedRoom.roomType]?.label}
                    </p>
                    <p className="text-sm text-gray-600">
                      Up to {selectedRoom.maxOccupancy} guests · {selectedRoom.amenities.join(", ")}
                    </p>
                    <p className="font-bold text-lg mt-1">₹{selectedRoom.pricePerNight}/night</p>
                    {isPremiumRoom(selectedRoom) && (
                      <p className="text-xs text-amber-700 mt-1">Premium room perks active</p>
                    )}
                  </div>
                  {userId && (
                    <Button onClick={handleConfirm} disabled={actionLoading}>
                      {actionLoading ? "Confirming..." : "Confirm Room"}
                    </Button>
                  )}
                </div>

                {upgradeSuggestion && (
                  <div className="mt-3 rounded-lg border border-amber-300 bg-amber-50 p-3">
                    <p className="text-sm font-medium text-amber-900">
                      Upgrade to {ROOM_TYPE_CONFIG[upgradeSuggestion.room.roomType]?.label} for +₹{upgradeSuggestion.extra}
                    </p>
                    <p className="text-xs text-amber-800 mt-1">
                      Room {upgradeSuggestion.room.roomNumber} includes {upgradeSuggestion.room.amenities.slice(0, 2).join(", ")} and more.
                    </p>
                    <Button
                      type="button"
                      className="mt-2 h-8 text-xs"
                      onClick={() => handleSelect(upgradeSuggestion.room)}
                    >
                      Choose Upgrade
                    </Button>
                  </div>
                )}
              </div>
            )}
          </>
        ) : (
          <p className="text-gray-500 text-center">No rooms available for this hotel</p>
        )}
      </CardContent>

      {/* Preview Modal */}
      {typeof window !== "undefined" && previewRoom && createPortal((
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4" onClick={() => setPreviewRoom(null)}>
          <div className="relative w-full max-w-4xl bg-white rounded-lg shadow-xl overflow-hidden" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center p-4 border-b">
              <h3 className="text-xl font-bold">
                Room {previewRoom.roomNumber} - {ROOM_TYPE_CONFIG[previewRoom.roomType]?.label}
              </h3>
              <button type="button" onClick={() => setPreviewRoom(null)} className="p-1 hover:bg-gray-100 rounded-full">
                <X className="w-6 h-6" />
              </button>
            </div>
            
            <div className="p-4 bg-gray-50">
              {previewRoom.isPanorama ? (
                <div id="panorama-container" className="w-full h-[500px] bg-gray-200 rounded-md overflow-hidden"></div>
              ) : (
                <div className="relative w-full h-[500px] bg-black flex items-center justify-center rounded-md overflow-hidden shadow-inner">
                  {previewRoom.images.length > 0 && (
                    <img 
                      src={previewRoom.images[currentImageIndex]} 
                      alt="Room preview" 
                      className="max-w-full max-h-full object-contain"
                    />
                  )}
                  
                  {previewRoom.images.length > 1 && (
                    <>
                      <button 
                        type="button"
                        onClick={prevImage}
                        className="absolute left-4 top-1/2 -translate-y-1/2 p-2 bg-white/50 hover:bg-white rounded-full shadow-md transition-colors"
                      >
                        <ChevronLeft className="w-6 h-6" />
                      </button>
                      <button 
                        type="button"
                        onClick={nextImage}
                        className="absolute right-4 top-1/2 -translate-y-1/2 p-2 bg-white/50 hover:bg-white rounded-full shadow-md transition-colors"
                      >
                        <ChevronRight className="w-6 h-6" />
                      </button>
                      
                      <div className="absolute bottom-4 left-0 right-0 flex justify-center gap-2 px-4">
                        {previewRoom.images.map((img, i) => (
                          <button
                            type="button"
                            key={i}
                            onClick={(e) => { e.stopPropagation(); setCurrentImageIndex(i); }}
                            className={`w-16 h-12 rounded border-2 overflow-hidden ${i === currentImageIndex ? 'border-primary ring-2 ring-white shadow-lg' : 'border-transparent opacity-60 hover:opacity-100'}`}
                          >
                            <img src={img} alt={`Thumb ${i}`} className="w-full h-full object-cover" />
                          </button>
                        ))}
                      </div>
                    </>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      ), document.body)}
    </Card>
  );
};

export default RoomGrid;
