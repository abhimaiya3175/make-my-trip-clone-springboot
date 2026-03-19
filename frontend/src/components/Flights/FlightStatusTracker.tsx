import React, { useState, useEffect } from "react";
import { useRouter } from "next/router";
import { getFlightStatus, getVapidPublicKey, subscribeToFlightStatus } from "@/services/flightStatusService";
import FlightTimeline from "./FlightTimeline";
import { Button } from "../ui/button";
import { Input } from "../ui/input";
import { Label } from "../ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "../ui/card";
import { Bell, BellOff } from "lucide-react";

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
  delayMinutes: number;
  delayReason: string | null;
  lastUpdated: string;
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
      
      // Show toast if status changed
      if (lastStatus && data.status !== lastStatus) {
        // Simple alert for now - can be replaced with toast library
        alert(`Flight ${fId} status changed: ${lastStatus} → ${data.status}`);
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

  // Auto-fetch if flightId in URL
  useEffect(() => {
    if (queryFlightId && typeof queryFlightId === "string") {
      setFlightNumber(queryFlightId);
      fetchStatus(queryFlightId);
    }
  }, [queryFlightId]);

  // Poll every 30 seconds
  useEffect(() => {
    if (!status) return;
    
    const interval = setInterval(() => {
      fetchStatus(status.flightId);
    }, 30000);
    
    return () => clearInterval(interval);
  }, [status]);

  const handleSearch = () => {
    if (flightNumber.trim()) {
      fetchStatus(flightNumber.trim());
    }
  };

  useEffect(() => {
    initializeNotifications();
  }, []);

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
    <div className="max-w-2xl mx-auto p-4">
      <div className="flex gap-2 mb-6 items-end">
        <div className="flex-1">
          <Label htmlFor="flightNumber">Flight Number</Label>
          <Input
            id="flightNumber"
            value={flightNumber}
            onChange={(e) => setFlightNumber(e.target.value)}
            placeholder="Enter flight number (e.g., AI101)"
            onKeyPress={(e) => e.key === "Enter" && handleSearch()}
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
      </div>

      <p className="text-xs text-gray-500 mb-4">
        Notifications: {pushSupported ? (notificationPermission === "granted" && notificationsEnabled ? "Enabled" : "Disabled") : "Not supported in this browser"}
      </p>

      {error && <p className="text-red-500 mb-4">{error}</p>}

      {status && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <span>{status.airline} {status.flightId}</span>
              <span className={`text-sm px-3 py-1 rounded-full border ${STATUS_COLORS[status.status]}`}>
                {status.status.replace("_", " ")}
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
              <div className="flex justify-between text-xs text-gray-400 pt-2 border-t">
                <span>Last updated</span>
                <span>{new Date(status.lastUpdated).toLocaleTimeString()}</span>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {status && <FlightTimeline flightId={status.flightId} />}
    </div>
  );
};

export default FlightStatusTracker;
