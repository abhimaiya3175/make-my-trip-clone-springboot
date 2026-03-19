import React, { useState, useEffect } from "react";
import { getFlightTimeline } from "@/services/flightStatusService";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";

interface TimelineEvent {
  timestamp: string;
  event: string;
  detail: string;
}

interface FlightTimelineProps {
  flightId: string;
}

const FlightTimeline: React.FC<FlightTimelineProps> = ({ flightId }) => {
  const [timeline, setTimeline] = useState<TimelineEvent[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const fetchTimeline = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getFlightTimeline(flightId);
      // Backend returns { flightId, timeline: [...] }
      setTimeline(data.timeline || []);
    } catch (err: any) {
      setError(err.response?.data?.message || "Unable to fetch timeline");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTimeline();

    // Poll every 30 seconds
    const interval = setInterval(fetchTimeline, 30000);
    return () => clearInterval(interval);
  }, [flightId]);

  if (loading && timeline.length === 0) {
    return <p className="text-gray-500 text-center py-4">Loading timeline...</p>;
  }

  if (error) {
    return <p className="text-red-500 text-center py-4">{error}</p>;
  }

  if (timeline.length === 0) {
    return <p className="text-gray-500 text-center py-4">No timeline events yet</p>;
  }

  return (
    <Card className="mt-6">
      <CardHeader>
        <CardTitle>Flight Timeline</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="space-y-4">
          {timeline.map((event, index) => (
            <div key={index} className="flex gap-4">
              <div className="flex flex-col items-center">
                <div className={`w-3 h-3 rounded-full ${index === 0 ? "bg-blue-500" : "bg-gray-300"
                  }`} />
                {index < timeline.length - 1 && (
                  <div className="w-0.5 h-full bg-gray-200 my-1" />
                )}
              </div>
              <div className="flex-1 pb-4">
                <div className="flex justify-between items-start mb-1">
                  <p className="font-semibold">{event.event}</p>
                  <span className="text-xs text-gray-500">
                    {new Date(event.timestamp).toLocaleString()}
                  </span>
                </div>
                <p className="text-sm text-gray-600">{event.detail}</p>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
};

export default FlightTimeline;
