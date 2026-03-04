import React from "react";
import BookingSummary from "@/components/booking/BookingSummary";
import Navbar from "@/components/Navbar";
import { useRouter } from "next/router";

export default function BookingConfirmationPage() {
  const router = useRouter();

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <div className="max-w-2xl mx-auto p-6">
        <h1 className="text-2xl font-bold mb-6">Booking Confirmation</h1>
        <BookingSummary booking={{}} type="flight" />
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
