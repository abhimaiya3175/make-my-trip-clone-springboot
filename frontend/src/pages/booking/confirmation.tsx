import React, { useEffect, useMemo, useState } from "react";
import BookingSummary from "@/components/booking/BookingSummary";
import { useRouter } from "next/router";
import { getBookingById } from "@/api";

export default function BookingConfirmationPage() {
  const router = useRouter();
  const [booking, setBooking] = useState<any | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const bookingId = useMemo(() => {
    const raw = router.query.bookingId;
    if (Array.isArray(raw)) {
      return raw[0];
    }
    return raw;
  }, [router.query.bookingId]);

  const type = useMemo<"flight" | "hotel">(() => {
    const queryType = Array.isArray(router.query.type) ? router.query.type[0] : router.query.type;
    if (queryType === "hotel") {
      return "hotel";
    }
    if (queryType === "flight") {
      return "flight";
    }
    if (booking?.entityType === "HOTEL") {
      return "hotel";
    }
    return "flight";
  }, [router.query.type, booking?.entityType]);

  useEffect(() => {
    if (!router.isReady) {
      return;
    }

    if (!bookingId) {
      setLoading(false);
      setError("Booking ID is missing from the confirmation URL.");
      return;
    }

    const fetchBooking = async () => {
      try {
        setLoading(true);
        setError("");
        const response = await getBookingById(bookingId);
        setBooking(response || null);
      } catch (err) {
        setError("Unable to load booking details. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    fetchBooking();
  }, [router.isReady, bookingId]);

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-2xl mx-auto p-6">
        <h1 className="text-2xl font-bold mb-6">Booking Confirmation</h1>
        {error ? (
          <div className="border rounded-lg p-4 bg-red-50 text-red-700">{error}</div>
        ) : (
          <BookingSummary booking={booking} type={type} isLoading={loading} />
        )}
        <button
          className="mt-4 text-blue-500 underline"
          onClick={() => router.push("/")}
        >
          Back to Home
        </button>
      </div>
    </div>
  );
}
