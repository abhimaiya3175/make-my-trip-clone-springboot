import React, { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from "recharts";

const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

interface SnapshotPoint {
  time: string;
  price: number;
  multiplier: number;
  rules: string;
}

interface PriceHistoryChartProps {
  entityId: string;
  entityType: "FLIGHT" | "HOTEL";
  days?: number;
}

const PriceHistoryChart: React.FC<PriceHistoryChartProps> = ({
  entityId,
  entityType,
  days = 7,
}) => {
  const [history, setHistory] = useState<SnapshotPoint[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchHistory = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await fetch(
          `${API_BASE}/api/pricing/${entityType}/${entityId}/history?days=${days}`
        );
        if (res.ok) {
          const json = await res.json();
          setHistory(json.data?.history || []);
        } else {
          setError("Failed to load price history.");
        }
      } catch {
        setError("Could not connect to pricing service.");
      } finally {
        setLoading(false);
      }
    };
    if (entityId) fetchHistory();
  }, [entityId, entityType, days]);

  const chartData = history.map((point) => ({
    time: new Date(point.time).toLocaleDateString("en-IN", {
      day: "2-digit",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    }),
    price: point.price,
    multiplier: point.multiplier,
    rules: point.rules,
  }));

  if (loading) return <p className="text-center py-4 text-sm">Loading price history...</p>;
  if (error) return <p className="text-center py-4 text-sm text-red-500">{error}</p>;

  return (
    <Card>
      <CardHeader>
        <CardTitle className="text-base">Price History ({days}d)</CardTitle>
      </CardHeader>
      <CardContent>
        {chartData.length > 1 ? (
          <ResponsiveContainer width="100%" height={250}>
            <LineChart data={chartData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="time" tick={{ fontSize: 10 }} />
              <YAxis
                tick={{ fontSize: 10 }}
                tickFormatter={(v: number) => `₹${v}`}
              />
              <Tooltip
                formatter={(value: number | undefined) => value ? [`₹${value}`, "Price"] : [`₹0`, "Price"]}
                labelStyle={{ fontWeight: "bold" }}
                contentStyle={{ fontSize: 12 }}
              />
              <Line
                type="monotone"
                dataKey="price"
                stroke="#3b82f6"
                strokeWidth={2}
                dot={{ r: 3 }}
                activeDot={{ r: 5 }}
              />
            </LineChart>
          </ResponsiveContainer>
        ) : (
          <p className="text-gray-500 text-sm text-center py-6">
            Not enough data yet. Price snapshots are captured hourly.
          </p>
        )}
      </CardContent>
    </Card>
  );
};

export default PriceHistoryChart;
