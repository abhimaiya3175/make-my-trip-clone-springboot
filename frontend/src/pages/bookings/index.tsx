import React, { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import Link from "next/link";
import { getUserBookings } from "@/api";
import Loader from "@/components/Loader";
import { Calendar, Receipt, Plane, Building2 } from "lucide-react";

export default function BookingsPage() {
  const user = useSelector((state: any) => state.user.user);
  const [loading, setLoading] = useState(true);
  const [bookings, setBookings] = useState<any[]>([]);

  useEffect(() => {
    const loadBookings = async () => {
      if (!user?.id) {
        setLoading(false);
        return;
      }
      try {
        const data = await getUserBookings(user.id);
        setBookings(Array.isArray(data) ? data : []);
      } finally {
        setLoading(false);
      }
    };

    loadBookings();
  }, [user?.id]);

  if (loading) return <Loader />;

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-50 px-4 py-10">
        <div className="max-w-3xl mx-auto bg-white border rounded-xl p-6 text-center">
          <h1 className="text-2xl font-bold">Bookings</h1>
          <p className="text-gray-600 mt-2">Please login to view booking backend data.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-10">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-gray-900">Bookings</h1>
        <p className="text-gray-600 mt-1 mb-6">Dedicated page for booking backend endpoints.</p>

        {bookings.length === 0 ? (
          <div className="bg-white border rounded-xl p-6 text-center text-gray-500">
            No bookings found.
          </div>
        ) : (
          <div className="space-y-4">
            {bookings.map((booking) => {
              const isFlight = booking.entityType === "FLIGHT";
              return (
                <div key={booking.id} className="bg-white border rounded-xl p-5">
                  <div className="flex justify-between items-start gap-4">
                    <div>
                      <h2 className="font-semibold text-lg flex items-center gap-2">
                        {isFlight ? <Plane className="w-5 h-5 text-red-500" /> : <Building2 className="w-5 h-5 text-red-500" />}
                        {booking.entityType} Booking
                      </h2>
                      <p className="text-sm text-gray-500">Booking ID: {booking.id}</p>
                    </div>
                    <p className="text-lg font-bold text-blue-700">Rs. {booking.totalPrice}</p>
                  </div>

                  <div className="mt-3 text-sm text-gray-700 space-y-1">
                    <p className="flex items-center gap-2"><Receipt className="w-4 h-4" /> Status: {booking.bookingStatus}</p>
                    <p className="flex items-center gap-2"><Calendar className="w-4 h-4" /> Travel Date: {booking.travelDate || "N/A"}</p>
                    <p>Quantity: {booking.quantity}</p>
                  </div>
                </div>
              );
            })}
          </div>
        )}

        <div className="mt-6">
          <Link href="/cancellations" className="text-red-600 font-medium hover:underline">
            View Cancellation and Refund Page
          </Link>
        </div>
      </div>
    </div>
  );
}
