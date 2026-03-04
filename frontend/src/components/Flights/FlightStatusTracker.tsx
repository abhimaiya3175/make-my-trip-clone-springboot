import React, { useState } from "react";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";

interface FlightStatusData {
  flightNumber: string;
  status: string;
  departure: string;
  arrival: string;
  departureTime: string;
  arrivalTime: string;
  gate: string;
}

const FlightStatusTracker: React.FC = () => {
  const [flightNumber, setFlightNumber] = useState("");
  const [status, setStatus] = useState<FlightStatusData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSearch = async () => {
    if (!flightNumber.trim()) return;
    setLoading(true);
    setError("");
    try {
      const res = await fetch(`http://localhost:8080/api/flight-status/${flightNumber}`);
      if (res.ok) {
        const data = await res.json();
        setStatus(data);
      } else {
        setError("Flight not found");
        setStatus(null);
      }
    } catch (err) {
      setError("Unable to fetch flight status");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-4">
      <div className="flex gap-2 mb-6">
        <div className="flex-1">
          <Label htmlFor="flightNumber">Flight Number</Label>
          <Input
            id="flightNumber"
            value={flightNumber}
            onChange={(e) => setFlightNumber(e.target.value)}
            placeholder="Enter flight number (e.g., AI-101)"
          />
        </div>
        <Button onClick={handleSearch} className="mt-6" disabled={loading}>
          {loading ? "Searching..." : "Track"}
        </Button>
      </div>

      {error && <p className="text-red-500 mb-4">{error}</p>}

      {status && (
        <Card>
          <CardHeader>
            <CardTitle>Flight {status.flightNumber}</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            <div className="flex justify-between">
              <span className="text-gray-500">Status</span>
              <span className={`font-semibold ${
                status.status === "On Time" ? "text-green-500" :
                status.status === "Delayed" ? "text-red-500" :
                "text-yellow-500"
              }`}>{status.status}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Departure</span>
              <span>{status.departure} - {status.departureTime}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Arrival</span>
              <span>{status.arrival} - {status.arrivalTime}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Gate</span>
              <span>{status.gate || "TBA"}</span>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
};

export default FlightStatusTracker;
