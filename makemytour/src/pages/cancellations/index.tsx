import React, { useEffect, useState } from "react";
import {
  ArrowLeft,
  Plane,
  Building2,
  Calendar,
  IndianRupee,
  AlertCircle,
  RefreshCw,
  FileX2,
} from "lucide-react";
import { useRouter } from "next/router";
import { useSelector } from "react-redux";
import { getUserCancellations } from "@/api";
import RefundStatusTracker from "@/components/Cancellation/RefundStatusTracker";
import Loader from "@/components/Loader";

const CancellationsPage = () => {
  const router = useRouter();
  const user = useSelector((state: any) => state.user.user);
  const [cancellations, setCancellations] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchCancellations = async () => {
    if (!user?.id) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError("");
    try {
      const data = await getUserCancellations(user.id);
      setCancellations(Array.isArray(data) ? data : []);
    } catch (err) {
      setError("Failed to load cancellations. Please try again.");
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchCancellations();
  }, [user]);

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center space-y-4">
          <AlertCircle className="w-12 h-12 text-gray-400 mx-auto" />
          <p className="text-gray-600">Please login to view your cancellations</p>
          <button
            onClick={() => router.push("/")}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
          >
            Go Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 pt-8 px-4 pb-12">
      <div className="max-w-4xl mx-auto">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div className="flex items-center gap-3">
            <button
              onClick={() => router.push("/")}
              className="p-2 hover:bg-gray-200 rounded-full transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div>
              <h1 className="text-2xl font-bold text-gray-800">My Cancellations</h1>
              <p className="text-sm text-gray-500">Track your cancellations and refunds</p>
            </div>
          </div>
          <button
            onClick={fetchCancellations}
            disabled={loading}
            className="flex items-center gap-2 px-3 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-100 transition-colors"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? "animate-spin" : ""}`} />
            Refresh
          </button>
        </div>

        {/* Content */}
        {loading ? (
          <div className="flex justify-center py-20">
            <Loader />
          </div>
        ) : error ? (
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <AlertCircle className="w-10 h-10 text-red-400 mx-auto mb-3" />
            <p className="text-red-700">{error}</p>
            <button
              onClick={fetchCancellations}
              className="mt-3 px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
            >
              Retry
            </button>
          </div>
        ) : cancellations.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-12 text-center">
            <FileX2 className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <p className="text-xl font-semibold text-gray-700">No Cancellations</p>
            <p className="text-gray-500 mt-2">You haven&apos;t cancelled any bookings yet</p>
            <button
              onClick={() => router.push("/profile")}
              className="mt-6 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
            >
              Go to My Bookings
            </button>
          </div>
        ) : (
          <div className="space-y-6">
            {cancellations.map((cancellation: any, idx: number) => (
              <div
                key={cancellation.cancellationId || idx}
                className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden"
              >
                {/* Cancellation header */}
                <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    {cancellation.bookingType === "FLIGHT" ? (
                      <div className="bg-blue-100 p-2 rounded-lg">
                        <Plane className="w-5 h-5 text-blue-600" />
                      </div>
                    ) : (
                      <div className="bg-green-100 p-2 rounded-lg">
                        <Building2 className="w-5 h-5 text-green-600" />
                      </div>
                    )}
                    <div>
                      <p className="font-semibold text-gray-800">
                        {cancellation.bookingType === "FLIGHT" ? "Flight" : "Hotel"} Booking
                      </p>
                      <p className="text-xs text-gray-500">
                        Booking ID: {cancellation.bookingId}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <span
                      className={`inline-block px-3 py-1 rounded-full text-xs font-semibold ${
                        cancellation.newBookingStatus === "CANCELLED"
                          ? "bg-red-100 text-red-700"
                          : "bg-yellow-100 text-yellow-700"
                      }`}
                    >
                      {cancellation.newBookingStatus === "CANCELLED"
                        ? "Fully Cancelled"
                        : "Partially Cancelled"}
                    </span>
                  </div>
                </div>

                {/* Cancellation details */}
                <div className="px-6 py-4 space-y-4">
                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
                    <div>
                      <p className="text-gray-500">Cancelled</p>
                      <p className="font-semibold text-gray-800">
                        {cancellation.cancelledQuantity} of {cancellation.totalQuantity}{" "}
                        {cancellation.bookingType === "FLIGHT" ? "seat(s)" : "room(s)"}
                      </p>
                    </div>
                    {cancellation.partialCancellation && (
                      <div>
                        <p className="text-gray-500">Remaining</p>
                        <p className="font-semibold text-gray-800">
                          {cancellation.remainingQuantity}{" "}
                          {cancellation.bookingType === "FLIGHT" ? "seat(s)" : "room(s)"}
                        </p>
                      </div>
                    )}
                    <div>
                      <p className="text-gray-500">Refund %</p>
                      <p className="font-semibold text-gray-800">
                        {cancellation.refundPercentage}%
                      </p>
                    </div>
                    <div>
                      <p className="text-gray-500">Refund</p>
                      <p className="font-bold text-green-700 flex items-center">
                        <IndianRupee className="w-3.5 h-3.5" />
                        {cancellation.refundAmount?.toLocaleString("en-IN")}
                      </p>
                    </div>
                  </div>

                  {/* Refund tracker */}
                  {cancellation.refundTracker && (
                    <RefundStatusTracker tracker={cancellation.refundTracker} />
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default CancellationsPage;
