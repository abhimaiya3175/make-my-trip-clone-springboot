import React, { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";

interface FlightStatusData {
  flightNumber: string;
  status: string;
  departure: string;
  arrival: string;
  departureTime: string;
  arrivalTime: string;
  gate: string;
  lastUpdated: string;
}

interface FlightStatusTrackerSSEProps {
  flightNumber: string;
}

const FlightStatusTrackerSSE: React.FC<FlightStatusTrackerSSEProps> = ({ flightNumber }) => {
  const [status, setStatus] = useState<FlightStatusData | null>(null);
  const [connected, setConnected] = useState(false);

  useEffect(() => {
    if (!flightNumber) return;

    const eventSource = new EventSource(
      `http://localhost:8080/api/flight-status/stream/${flightNumber}`
    );

    eventSource.onopen = () => setConnected(true);

    eventSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        setStatus(data);
      } catch (err) {
        console.error("Failed to parse SSE data:", err);
      }
    };

    eventSource.onerror = () => {
      setConnected(false);
      eventSource.close();
    };

    return () => {
      eventSource.close();
    };
  }, [flightNumber]);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          Flight {flightNumber}
          <span className={`w-2 h-2 rounded-full ${connected ? "bg-green-500" : "bg-red-500"}`} />
        </CardTitle>
      </CardHeader>
      <CardContent>
        {status ? (
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-500">Status</span>
              <span className="font-semibold">{status.status}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-500">Last Updated</span>
              <span>{status.lastUpdated || "N/A"}</span>
            </div>
          </div>
        ) : (
          <p className="text-gray-500 text-sm">
            {connected ? "Waiting for updates..." : "Connecting..."}
          </p>
        )}
      </CardContent>
    </Card>
  );
};

export default FlightStatusTrackerSSE;
