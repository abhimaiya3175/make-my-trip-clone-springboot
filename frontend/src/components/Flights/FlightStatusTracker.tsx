import React, { useState, useEffect } from "react";
import { useRouter } from "next/router";
import { getAllLiveFlightStatuses, getFlightStatus, getVapidPublicKey, subscribeToFlightStatus } from "@/services/flightStatusService";
import FlightTimeline from "./FlightTimeline";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Bell, BellOff } from "lucide-react";
import { toast } from "sonner";

interface FlightStatusData {
  flightId: string;
  airline: string;
  origin: string;
  destination: string;
  scheduledDeparture: string;
  estimatedDeparture: string;
  scheduledArrival: string;
  estimatedArrival: string;
  status: "ON_TIME" | "DELAYED" | "BOARDING" | "LANDED" | "CANCELLED";
  statusMessage?: string;
  delayMinutes: number;
  delayReason: string | null;
  arrivalDelayMinutes?: number;
  estimatedArrivalUpdate?: string;
  lastUpdated: string;
}

interface LiveUpdateItem {
  id: string;
  timestamp: string;
  title: string;
  detail: string;
}

const FlightStatusTracker: React.FC = () => {
  const router = useRouter();
  const { flightId: queryFlightId } = router.query;
  
  const [flightNumber, setFlightNumber] = useState("");
  const [status, setStatus] = useState<FlightStatusData | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [lastStatus, setLastStatus] = useState<string | null>(null);
  const [notificationPermission, setNotificationPermission] = useState<NotificationPermission>("default");
  const [notificationsEnabled, setNotificationsEnabled] = useState(false);
  const [pushSupported, setPushSupported] = useState(true);
  const [registration, setRegistration] = useState<ServiceWorkerRegistration | null>(null);
  const [vapidPublicKey, setVapidPublicKey] = useState("");
  const [subscribedFlightId, setSubscribedFlightId] = useState<string | null>(null);
  const [liveUpdates, setLiveUpdates] = useState<LiveUpdateItem[]>([]);
  const [allStatuses, setAllStatuses] = useState<FlightStatusData[]>([]);
  const [allLoading, setAllLoading] = useState(false);
  const [showSplitView, setShowSplitView] = useState(false);
  const detailsSectionRef = React.useRef<HTMLDivElement | null>(null);

  const STATUS_COLORS = {
    "ON_TIME": "bg-green-100 text-green-800 border-green-300",
    "DELAYED": "bg-yellow-100 text-yellow-800 border-yellow-300",
    "BOARDING": "bg-blue-100 text-blue-800 border-blue-300",
    "LANDED": "bg-gray-100 text-gray-800 border-gray-300",
    "CANCELLED": "bg-red-100 text-red-800 border-red-300",
  };

  const fetchStatus = async (fId: string) => {
    if (!fId) return;
    setLoading(true);
    setError("");
    try {
      const data = await getFlightStatus(fId);
      
      // Show status change as non-blocking toast notification.
      if (lastStatus && data.status !== lastStatus) {
        toast(`Flight ${fId} status changed: ${lastStatus} → ${data.status}`, { icon: "✈" });
        setLiveUpdates((prev) => [
          {
            id: `${Date.now()}-${Math.random()}`,
            timestamp: new Date().toISOString(),
            title: `${data.airline} ${data.flightId} ${data.statusMessage || data.status.replace("_", " ")}`,
            detail: data.estimatedArrivalUpdate || "New status update received",
          },
          ...prev,
        ].slice(0, 8));
      }
      
      setStatus(data);
      setLastStatus(data.status);

      if (notificationsEnabled && notificationPermission === "granted") {
        await subscribeForFlight(data.flightId);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || "Flight not found");
      setStatus(null);
    } finally {
      setLoading(false);
    }
  };

  const fetchAllStatuses = async () => {
    setAllLoading(true);
    try {
      const data = await getAllLiveFlightStatuses();
      setAllStatuses(Array.isArray(data) ? data : []);
    } catch (allStatusError) {
      console.error("Failed to fetch live statuses for all flights", allStatusError);
    } finally {
      setAllLoading(false);
    }
  };

  // Auto-fetch if flightId in URL
  useEffect(() => {
    if (queryFlightId && typeof queryFlightId === "string") {
      setFlightNumber(queryFlightId);
      setShowSplitView(true);
      fetchStatus(queryFlightId);
    }
  }, [queryFlightId]);

  useEffect(() => {
    fetchAllStatuses();
    const interval = setInterval(() => {
      fetchAllStatuses();
    }, 30000);
    return () => clearInterval(interval);
  }, []);

  // Poll every 30 seconds
  useEffect(() => {
    if (!status) return;
    
    const interval = setInterval(() => {
      fetchStatus(status.flightId);
    }, 30000);
    
    return () => clearInterval(interval);
  }, [status]);

  useEffect(() => {
    if (typeof window === "undefined" || !("serviceWorker" in navigator)) {
      return;
    }

    const onServiceWorkerMessage = (event: MessageEvent) => {
      const payload = event.data;
      if (!payload || payload.type !== "FLIGHT_STATUS_PUSH" || !payload.payload) {
        return;
      }

      const pushFlightId = payload.payload.flightId;
      if (status?.flightId && pushFlightId && status.flightId !== pushFlightId) {
        return;
      }

      setLiveUpdates((prev) => [
        {
          id: `${Date.now()}-${Math.random()}`,
          timestamp: payload.payload.lastUpdated || new Date().toISOString(),
          title: payload.payload.title || "Flight push notification",
          detail: payload.payload.estimatedArrivalUpdate || payload.payload.body || "Status changed",
        },
        ...prev,
      ].slice(0, 8));

      if (pushFlightId) {
        fetchStatus(pushFlightId);
      }
      fetchAllStatuses();
    };

    navigator.serviceWorker.addEventListener("message", onServiceWorkerMessage);
    return () => {
      navigator.serviceWorker.removeEventListener("message", onServiceWorkerMessage);
    };
  }, [status?.flightId]);

  const formatDelayLabel = (delayMinutes: number) => {
    if (!delayMinutes || delayMinutes <= 0) {
      return "On schedule";
    }
    if (delayMinutes >= 60 && delayMinutes % 60 === 0) {
      return `Delayed by ${delayMinutes / 60}h`;
    }
    return `Delayed by ${delayMinutes}m`;
  };

  const handleSearch = () => {
    const targetFlightId = flightNumber.trim();
    if (!targetFlightId) {
      return;
    }

    setShowSplitView(true);
    fetchStatus(targetFlightId);
  };

  const initializeNotifications = async () => {
    if (typeof window === "undefined") {
      return;
    }

    const supported = "Notification" in window && "serviceWorker" in navigator && "PushManager" in window;
    if (!supported) {
      setPushSupported(false);
      return;
    }

    try {
      const permission = await Notification.requestPermission();
      setNotificationPermission(permission);

      if (permission !== "granted") {
        setNotificationsEnabled(false);
        return;
      }

      const swRegistration = await navigator.serviceWorker.register("/sw.js");
      setRegistration(swRegistration);
      setNotificationsEnabled(true);

      const key = await getVapidPublicKey();
      if (key) {
        setVapidPublicKey(key);
      }
    } catch (notificationError) {
      console.error("Failed to initialize push notifications", notificationError);
      setNotificationsEnabled(false);
    }
  };

  const urlBase64ToUint8Array = (base64String: string) => {
    const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
    const rawData = window.atob(base64);
    const outputArray = new Uint8Array(rawData.length);
    for (let i = 0; i < rawData.length; ++i) {
      outputArray[i] = rawData.charCodeAt(i);
    }
    return outputArray;
  };

  const subscribeForFlight = async (flightId: string) => {
    if (!registration || notificationPermission !== "granted" || !vapidPublicKey) {
      return;
    }

    if (subscribedFlightId === flightId) {
      return;
    }

    const existingSubscription = await registration.pushManager.getSubscription();
    const subscription = existingSubscription || await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: urlBase64ToUint8Array(vapidPublicKey),
    });

    const subscriptionJson = subscription.toJSON();
    await subscribeToFlightStatus({
      flightId,
      endpoint: subscriptionJson.endpoint,
      keys: {
        p256dh: subscriptionJson.keys?.p256dh,
        auth: subscriptionJson.keys?.auth,
      },
    });
    setSubscribedFlightId(flightId);
  };

  const toggleNotifications = async () => {
    if (!pushSupported) {
      return;
    }

    if (!notificationsEnabled) {
      await initializeNotifications();
      if (status) {
        await subscribeForFlight(status.flightId);
      }
      return;
    }

    if (registration) {
      const existingSubscription = await registration.pushManager.getSubscription();
      if (existingSubscription) {
        await existingSubscription.unsubscribe();
      }
    }

    setNotificationsEnabled(false);
    setSubscribedFlightId(null);
  };

  return (
    <div className="h-screen flex flex-col bg-gray-100">
      {/* Sticky Header */}
      <div className="sticky top-0 z-40 bg-gray-50/95 backdrop-blur-sm border-b border-gray-200">
        <div className="p-4">
          <div className="flex gap-2 mb-2 items-end max-w-2xl mx-auto">
            <div className="flex-1">
              <Label htmlFor="flightNumber">Flight Number</Label>
              <Input
                id="flightNumber"
                value={flightNumber}
                onChange={(e) => setFlightNumber(e.target.value)}
                placeholder="Enter flight number (e.g., AI101)"
                onKeyDown={(e) => e.key === "Enter" && handleSearch()}
              />
            </div>
            <Button onClick={handleSearch} className="mt-6" disabled={loading}>
              {loading ? "Searching..." : "Track"}
            </Button>
            <Button
              type="button"
              variant="outline"
              className="mt-6"
              onClick={toggleNotifications}
              disabled={!pushSupported}
              title={notificationsEnabled ? "Disable flight push notifications" : "Enable flight push notifications"}
            >
              {notificationsEnabled ? <Bell className="w-4 h-4" /> : <BellOff className="w-4 h-4" />}
            </Button>
            {showSplitView && (
              <Button
                type="button"
                variant="outline"
                className="mt-6"
                onClick={() => {
                  setShowSplitView(false);
                  setStatus(null);
                }}
              >
                ✕ Close
              </Button>
            )}
          </div>

          <p className="text-xs text-gray-500 max-w-2xl mx-auto">
            Notifications: {pushSupported ? (notificationPermission === "granted" && notificationsEnabled ? "Enabled" : "Disabled") : "Not supported in this browser"}
          </p>
        </div>
      </div>

      {/* Main Content */}
      {error && (
        <div className="px-4 py-2">
          <p className="text-red-500 text-sm">{error}</p>
        </div>
      )}

      {showSplitView ? (
        <div className="flex-1 overflow-hidden flex gap-4 p-4">
          {/* Left Panel: All Flights */}
          <div className="w-1/2 overflow-y-auto">
            <Card className="h-full flex flex-col">
              <CardHeader className="flex-shrink-0">
                <CardTitle>All Flights Live Tracking</CardTitle>
              </CardHeader>
              <CardContent className="flex-1 overflow-y-auto">
                {allLoading && allStatuses.length === 0 ? (
                  <p className="text-sm text-gray-500">Loading live flights...</p>
                ) : allStatuses.length === 0 ? (
                  <p className="text-sm text-gray-500">No flights available for tracking.</p>
                ) : (
                  <div className="space-y-2">
                    {allStatuses.map((flight) => (
                      <button
                        key={flight.flightId}
                        type="button"
                        onClick={() => {
                          setFlightNumber(flight.flightId);
                          fetchStatus(flight.flightId);
                        }}
                        className={`w-full rounded-md border px-3 py-2 text-left transition-colors ${
                          status?.flightId === flight.flightId
                            ? "border-blue-500 bg-blue-50"
                            : "border-gray-200 hover:bg-gray-50"
                        }`}
                      >
                        <div className="flex items-center justify-between gap-3">
                          <p className="font-medium text-sm text-gray-900">
                            {flight.airline} {flight.flightId} • {flight.origin} to {flight.destination}
                          </p>
                          <span className={`text-xs px-2 py-1 rounded-full border ${STATUS_COLORS[flight.status]}`}>
                            {flight.statusMessage || flight.status.replace("_", " ")}
                          </span>
                        </div>
                        <p className="text-xs text-gray-500 mt-1">
                          {flight.estimatedArrivalUpdate || "Live update available"}
                        </p>
                      </button>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Right Panel: Flight Details */}
          {status && (
            <div className="w-1/2 overflow-y-auto">
              <div ref={detailsSectionRef}>
                <Card className="h-full flex flex-col">
                  <CardHeader className="flex-shrink-0">
                    <CardTitle className="flex items-center justify-between">
                      <span>{status.airline} {status.flightId}</span>
                      <span className={`text-sm px-3 py-1 rounded-full border ${STATUS_COLORS[status.status]}`}>
                        {status.statusMessage || status.status.replace("_", " ")}
                      </span>
                    </CardTitle>
                  </CardHeader>
                  <CardContent className="flex-1 overflow-y-auto space-y-3">
                    <div className="flex justify-between items-center">
                      <div>
                        <p className="text-sm text-gray-500">From</p>
                        <p className="font-semibold text-lg">{status.origin}</p>
                      </div>
                      <div className="text-center text-gray-400">→</div>
                      <div className="text-right">
                        <p className="text-sm text-gray-500">To</p>
                        <p className="font-semibold text-lg">{status.destination}</p>
                      </div>
                    </div>
                    
                    <div className="border-t pt-3 space-y-2">
                      <div className="flex justify-between">
                        <span className="text-gray-500">Scheduled Departure</span>
                        <span className="font-semibold">
                          {new Date(status.scheduledDeparture).toLocaleString()}
                        </span>
                      </div>
                      {status.estimatedDeparture && (
                        <div className="flex justify-between">
                          <span className="text-gray-500">Estimated Departure</span>
                          <span className="font-semibold">
                            {new Date(status.estimatedDeparture).toLocaleString()}
                          </span>
                        </div>
                      )}
                      {status.estimatedArrival && (
                        <div className="flex justify-between">
                          <span className="text-gray-500">Estimated Arrival</span>
                          <span className="font-semibold">
                            {new Date(status.estimatedArrival).toLocaleString()}
                          </span>
                        </div>
                      )}
                      <div className="flex justify-between">
                        <span className="text-gray-500">Live Delay</span>
                        <span className="font-semibold text-orange-600">
                          {formatDelayLabel(status.delayMinutes)}
                        </span>
                      </div>
                      {status.delayMinutes > 0 && (
                        <>
                          <div className="flex justify-between">
                            <span className="text-gray-500">Delay</span>
                            <span className="font-semibold text-orange-600">
                              {status.delayMinutes} minutes
                            </span>
                          </div>
                          {status.delayReason && (
                            <div className="flex justify-between">
                              <span className="text-gray-500">Reason</span>
                              <span className="text-sm">{status.delayReason}</span>
                            </div>
                          )}
                        </>
                      )}
                      {status.estimatedArrivalUpdate && (
                        <div className="rounded-md border border-sky-200 bg-sky-50 px-3 py-2 text-sm text-sky-800">
                          {status.estimatedArrivalUpdate}
                        </div>
                      )}
                      <div className="flex justify-between text-xs text-gray-400 pt-2 border-t">
                        <span>Last updated</span>
                        <span>{new Date(status.lastUpdated).toLocaleTimeString()}</span>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </div>
            </div>
          )}
        </div>
      ) : (
        /* Normal view: Single column layout */
        <div className="flex-1 overflow-y-auto">
          <div className="max-w-2xl mx-auto p-4">
            <Card className="mb-6">
              <CardHeader>
                <CardTitle>All Flights Live Tracking</CardTitle>
              </CardHeader>
              <CardContent>
                {allLoading && allStatuses.length === 0 ? (
                  <p className="text-sm text-gray-500">Loading live flights...</p>
                ) : allStatuses.length === 0 ? (
                  <p className="text-sm text-gray-500">No flights available for tracking.</p>
                ) : (
                  <div className="space-y-2">
                    {allStatuses.map((flight) => (
                      <button
                        key={flight.flightId}
                        type="button"
                        onClick={() => {
                          setFlightNumber(flight.flightId);
                          setShowSplitView(true);
                          fetchStatus(flight.flightId);
                        }}
                        className="w-full rounded-md border border-gray-200 px-3 py-2 text-left hover:bg-gray-50"
                      >
                        <div className="flex items-center justify-between gap-3">
                          <p className="font-medium text-sm text-gray-900">
                            {flight.airline} {flight.flightId} • {flight.origin} to {flight.destination}
                          </p>
                          <span className={`text-xs px-2 py-1 rounded-full border ${STATUS_COLORS[flight.status]}`}>
                            {flight.statusMessage || flight.status.replace("_", " ")}
                          </span>
                        </div>
                        <p className="text-xs text-gray-500 mt-1">
                          {flight.estimatedArrivalUpdate || "Live update available"}
                        </p>
                      </button>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>

            {status && (
              <div ref={detailsSectionRef}>
                <Card>
                <CardHeader>
                  <CardTitle className="flex items-center justify-between">
                    <span>{status.airline} {status.flightId}</span>
                    <span className={`text-sm px-3 py-1 rounded-full border ${STATUS_COLORS[status.status]}`}>
                      {status.statusMessage || status.status.replace("_", " ")}
                    </span>
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  <div className="flex justify-between items-center">
                    <div>
                      <p className="text-sm text-gray-500">From</p>
                      <p className="font-semibold text-lg">{status.origin}</p>
                    </div>
                    <div className="text-center text-gray-400">→</div>
                    <div className="text-right">
                      <p className="text-sm text-gray-500">To</p>
                      <p className="font-semibold text-lg">{status.destination}</p>
                    </div>
                  </div>
                  
                  <div className="border-t pt-3 space-y-2">
                    <div className="flex justify-between">
                      <span className="text-gray-500">Scheduled Departure</span>
                      <span className="font-semibold">
                        {new Date(status.scheduledDeparture).toLocaleString()}
                      </span>
                    </div>
                    {status.estimatedDeparture && (
                      <div className="flex justify-between">
                        <span className="text-gray-500">Estimated Departure</span>
                        <span className="font-semibold">
                          {new Date(status.estimatedDeparture).toLocaleString()}
                        </span>
                      </div>
                    )}
                    {status.estimatedArrival && (
                      <div className="flex justify-between">
                        <span className="text-gray-500">Estimated Arrival</span>
                        <span className="font-semibold">
                          {new Date(status.estimatedArrival).toLocaleString()}
                        </span>
                      </div>
                    )}
                    <div className="flex justify-between">
                      <span className="text-gray-500">Live Delay</span>
                      <span className="font-semibold text-orange-600">
                        {formatDelayLabel(status.delayMinutes)}
                      </span>
                    </div>
                    {status.delayMinutes > 0 && (
                      <>
                        <div className="flex justify-between">
                          <span className="text-gray-500">Delay</span>
                          <span className="font-semibold text-orange-600">
                            {status.delayMinutes} minutes
                          </span>
                        </div>
                        {status.delayReason && (
                          <div className="flex justify-between">
                            <span className="text-gray-500">Reason</span>
                            <span className="text-sm">{status.delayReason}</span>
                          </div>
                        )}
                      </>
                    )}
                    {status.estimatedArrivalUpdate && (
                      <div className="rounded-md border border-sky-200 bg-sky-50 px-3 py-2 text-sm text-sky-800">
                        {status.estimatedArrivalUpdate}
                      </div>
                    )}
                    <div className="flex justify-between text-xs text-gray-400 pt-2 border-t">
                      <span>Last updated</span>
                      <span>{new Date(status.lastUpdated).toLocaleTimeString()}</span>
                    </div>
                  </div>
                </CardContent>
                </Card>
              </div>
            )}

            {liveUpdates.length > 0 && (
              <Card className="mt-6">
                <CardHeader>
                  <CardTitle>Live Updates</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    {liveUpdates.map((update) => (
                      <div key={update.id} className="border-l-2 border-blue-300 pl-3">
                        <p className="font-medium text-sm text-gray-900">{update.title}</p>
                        <p className="text-sm text-gray-600">{update.detail}</p>
                        <p className="text-xs text-gray-400 mt-1">{new Date(update.timestamp).toLocaleTimeString()}</p>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}

            {status && <FlightTimeline flightId={status.flightId} />}
          </div>
        </div>
      )}
    </div>
  );
};

export default FlightStatusTracker;
