import React, { useState, useEffect } from "react";
import {
  User,
  Phone,
  Mail,
  Edit2,
  MapPin,
  Calendar,
  CreditCard,
  X,
  Check,
  LogOut,
  Plane,
  Building2,
  XCircle,
  FileX2,
  ExternalLink,
  ArrowLeft,
} from "lucide-react";
import { useDispatch, useSelector } from "react-redux";
import { useRouter } from "next/router";
import { clearUser, setUser } from "@/store";
import { editprofile, getUserCancellations } from "@/api";
import CancellationDialog from "@/components/Cancellation/CancellationDialog";
import RefundStatusTracker from "@/components/Cancellation/RefundStatusTracker";
const index = () => {
  const dispatch = useDispatch();
  const user = useSelector((state: any) => state.user.user);
  const router = useRouter();

  const logout = () => {
    dispatch(clearUser());
    router.push("/");
  };
  const [isEditing, setIsEditing] = useState(false);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [selectedBooking, setSelectedBooking] = useState<any>(null);
  const [cancellations, setCancellations] = useState<any[]>([]);

  useEffect(() => {
    console.log("[Profile] useEffect triggered. user object:", user);
    console.log("[Profile] user.id:", user?.id, "user._id:", user?._id);
    
    if (!user) {
      console.warn("[Profile] user is null/undefined");
      setCancellations([]);
      return;
    }
    
    const userId = user.id || user._id;
    
    if (!userId) {
      console.error("[Profile] ERROR: Neither user.id nor user._id exists!", "Full user object:", JSON.stringify(user));
      setCancellations([]);
      return;
    }
    
    console.log("[Profile] Fetching cancellations for userId:", userId, "type:", typeof userId);
    getUserCancellations(userId)
      .then((data) => {
        console.log("[Profile] SUCCESS: Got cancellations:", data);
        setCancellations(Array.isArray(data) ? data : []);
      })
      .catch((error) => {
        console.error("[Profile] ERROR fetching cancellations:", {
          message: error.message,
          status: error?.response?.status,
          data: error?.response?.data,
          url: error?.response?.config?.url,
          fullError: error
        });
        setCancellations([]);
      });
  }, [user]);

  const handleCancelBooking = (booking: any) => {
    setSelectedBooking(booking);
    setCancelDialogOpen(true);
  };

  const handleCancellationComplete = (result: any) => {
    // Refresh cancellations list
    const userId = user?.id || user?._id;
    if (userId) {
      console.log("[Profile] handleCancellationComplete - refreshing with userId:", userId);
      getUserCancellations(userId)
        .then((data) => {
          console.log("[Profile] Refreshed cancellations:", data);
          setCancellations(Array.isArray(data) ? data : []);
        })
        .catch((error) => {
          console.error("[Profile] Failed to refresh cancellations:", error);
        });
    } else {
      console.error("[Profile] handleCancellationComplete - no userId available");
    }
  };

  const isBookingCancelled = (bookingId: string) => {
    return cancellations.some(
      (c: any) =>
        c.bookingId === bookingId &&
        c.newBookingStatus === "CANCELLED"
    );
  };

  const getBookingCancellation = (bookingId: string) => {
    return cancellations.find((c: any) => c.bookingId === bookingId);
  };
  const [userData, setUserData] = useState({
    firstName: user?.firstName ? user?.firstName : "",
    lastName: user?.lastName ? user?.lastName : "",
    email: user?.email ? user?.email : "",
    phoneNumber: user?.phoneNumber ? user?.phoneNumber : "",
    bookings: [
      {
        type: "Flight",
        bookingId: "F123456",
        date: "2024-03-25",
        quantity: 2,
        totalPrice: 12499,
        details: {
          from: "Delhi",
          to: "Mumbai",
          airline: "IndiGo",
        },
      },
      {
        type: "Hotel",
        bookingId: "H789012",
        date: "2024-04-15",
        quantity: 1,
        totalPrice: 8999,
        details: {
          name: "Taj Palace",
          location: "Goa",
          nights: 3,
        },
      },
    ],
  });

  const [editForm, setEditForm] = useState({ ...userData });
  const handleSave = async () => {
    try {
      const data = await editprofile(
        user?.id,
        userData.firstName,
        userData.lastName,
        userData.email,
        userData.phoneNumber
      );
      dispatch(setUser(data));
      setIsEditing(false);
    } catch (error) {
      setUserData(editForm);
      setIsEditing(false);
    }
  };

  const formatDate = (dateString: string) => {
    // Parse YYYY-MM-DD as local time to avoid UTC timezone offset
    const parts = dateString.split("-");
    if (parts.length === 3) {
      const d = new Date(parseInt(parts[0]), parseInt(parts[1]) - 1, parseInt(parts[2]));
      return d.toLocaleDateString("en-IN", {
        day: "numeric",
        month: "short",
        year: "numeric",
      });
    }
    return dateString;
  };
  const handleEditFormChange = (field:any, value:any) => {
    setUserData((prevState) => ({
        ...prevState,
        [field]: value, // Update the specific field dynamically
      }));
  };
  return (
    <div className="min-h-screen bg-gray-50 pt-8 px-4">
      <div className="max-w-6xl mx-auto">
        {/* Back to Home */}
        <div className="mb-6">
          <button
            onClick={() => router.push("/")}
            className="flex items-center gap-2 text-gray-600 hover:text-red-600 transition-colors text-sm font-medium"
          >
            <ArrowLeft className="w-4 h-4" />
            Back to Home
          </button>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* Profile Section */}
          <div className="md:col-span-1">
            <div className="bg-white rounded-xl shadow-lg p-6">
              <div className="flex justify-between items-start mb-6">
                <h2 className="text-2xl font-bold">Profile</h2>
                {!isEditing && (
                  <button
                    onClick={() => setIsEditing(true)}
                    className="text-red-600 flex items-center space-x-1 hover:text-red-700"
                  >
                    <Edit2 className="w-4 h-4" />
                    <span>Edit</span>
                  </button>
                )}
              </div>

              {isEditing ? (
                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      First Name
                    </label>
                    <input
                      type="text"
                      value={userData.firstName}
                      onChange={(e) => handleEditFormChange("firstName", e.target.value)}
                      className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Last Name
                    </label>
                    <input
                      type="text"
                      value={userData.lastName}
                      onChange={(e) => handleEditFormChange("lastName", e.target.value)}
                      className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Email
                    </label>
                    <input
                      type="email"
                      value={userData.email}
                      onChange={(e) => handleEditFormChange("email", e.target.value)}
                      
                      className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Phone Number
                    </label>
                    <input
                      type="tel"
                      value={userData.phoneNumber}
                      onChange={(e) => handleEditFormChange("phoneNumber", e.target.value)}
                      className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500"
                    />
                  </div>
                  <div className="flex space-x-3">
                    <button
                      onClick={handleSave}
                      className="flex-1 bg-red-600 text-white py-2 rounded-lg hover:bg-red-700 transition-colors flex items-center justify-center space-x-2"
                    >
                      <Check className="w-4 h-4" />
                      <span>Save</span>
                    </button>
                    <button
                      onClick={() => {
                        setIsEditing(false);
                        setEditForm({ ...user });
                      }}
                      className="flex-1 bg-gray-100 text-gray-700 py-2 rounded-lg hover:bg-gray-200 transition-colors flex items-center justify-center space-x-2"
                    >
                      <X className="w-4 h-4" />
                      <span>Cancel</span>
                    </button>
                  </div>
                </div>
              ) : (
                <div className="space-y-6">
                  <div className="flex items-center space-x-3">
                    <User className="w-5 h-5 text-gray-500" />
                    <div>
                      <p className="font-medium">
                        {user?.firstName} {user?.lastName}
                      </p>
                      {/* <p className="text-sm text-gray-500">{userData.role}</p> */}
                    </div>
                  </div>
                  <div className="flex items-center space-x-3">
                    <Mail className="w-5 h-5 text-gray-500" />
                    <p>{user?.email}</p>
                  </div>
                  <div className="flex items-center space-x-3">
                    <Phone className="w-5 h-5 text-gray-500" />
                    <p>{user?.phoneNumber}</p>
                  </div>
                  <button
                    className="w-full mt-4 flex items-center justify-center space-x-2 text-red-600 hover:text-red-700"
                    onClick={logout}
                  >
                    <LogOut className="w-4 h-4" />
                    <span>Logout</span>
                  </button>
                </div>
              )}
            </div>
          </div>

          {/* Bookings Section */}
          <div className="md:col-span-2">
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h2 className="text-2xl font-bold mb-6">My Bookings</h2>
              <div className="space-y-6">
                {user?.bookings.map((booking: any, index: any) => (
                  <div
                    key={index}
                    className="border rounded-lg p-4 hover:shadow-md transition-shadow"
                  >
                    <div className="flex items-start justify-between mb-4">
                      <div className="flex items-center space-x-3">
                        {booking?.type === "Flight" ? (
                          <div className="bg-blue-100 p-2 rounded-lg">
                            <Plane className="w-6 h-6 text-blue-600" />
                          </div>
                        ) : (
                          <div className="bg-green-100 p-2 rounded-lg">
                            <Building2 className="w-6 h-6 text-green-600" />
                          </div>
                        )}
                        <div>
                          <h3 className="font-semibold">{booking?.type}</h3>
                          <p className="text-sm text-gray-500">
                            Booking ID: {booking?.bookingId}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <p className="font-semibold">
                          ₹ {booking?.totalPrice.toLocaleString("en-IN")}
                        </p>
                        <p className="text-sm text-gray-500">{booking?.type}</p>
                      </div>
                    </div>
                    <div className="flex flex-wrap gap-4 text-sm text-gray-600">
                      <div className="flex items-center space-x-1">
                        <Calendar className="w-4 h-4" />
                        <span>{formatDate(booking?.date)}</span>
                      </div>
                      <div className="flex items-center space-x-1">
                        <MapPin className="w-4 h-4" />
                        <span>{booking?.type}</span>
                      </div>
                      <div className="flex items-center space-x-1">
                        <CreditCard className="w-4 h-4" />
                        <span>Paid</span>
                      </div>
                    </div>

                    {/* Cancel & Refund section */}
                    {isBookingCancelled(booking?.bookingId) ? (
                      <div className="mt-4 pt-3 border-t border-gray-100">
                        <div className="flex items-center gap-2 mb-2">
                          <XCircle className="w-4 h-4 text-red-500" />
                          <span className="text-sm font-medium text-red-600">Booking Cancelled</span>
                        </div>
                        {getBookingCancellation(booking?.bookingId)?.refundTracker && (
                          <RefundStatusTracker
                            tracker={getBookingCancellation(booking?.bookingId).refundTracker}
                            compact
                          />
                        )}
                      </div>
                    ) : (
                      <div className="mt-4 pt-3 border-t border-gray-100 flex justify-end">
                        <button
                          onClick={() => handleCancelBooking(booking)}
                          className="flex items-center gap-1.5 px-4 py-1.5 text-sm text-red-600 border border-red-200 rounded-lg hover:bg-red-50 transition-colors"
                        >
                          <XCircle className="w-4 h-4" />
                          Cancel Booking
                        </button>
                      </div>
                    )}
                  </div>
                ))}
              </div>

              {/* View All Cancellations link */}
              {cancellations.length > 0 && (
                <div className="mt-6 pt-4 border-t border-gray-200">
                  <button
                    onClick={() => router.push("/cancellations")}
                    className="w-full flex items-center justify-center gap-2 px-4 py-3 text-sm font-medium text-blue-600 bg-blue-50 border border-blue-200 rounded-lg hover:bg-blue-100 transition-colors"
                  >
                    <FileX2 className="w-4 h-4" />
                    View All Cancellations & Refund Status
                    <ExternalLink className="w-3.5 h-3.5" />
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Cancellation Dialog */}
      {selectedBooking && (
        <CancellationDialog
          open={cancelDialogOpen}
          onClose={() => {
            setCancelDialogOpen(false);
            setSelectedBooking(null);
          }}
          booking={selectedBooking}
          userId={user?.id || ""}
          onCancellationComplete={handleCancellationComplete}
        />
      )}
    </div>
  );
};

export default index;
