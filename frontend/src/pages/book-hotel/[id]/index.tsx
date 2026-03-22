import { useRouter } from "next/router";
import {
  Star,
  MapPin,
  School as Pool,
  UtensilsCrossed,
  Wine,
  Power,
  ChevronRight,
  Camera,
  Image,
  CreditCard,
  Ticket,
  Plane,
  Home,
} from "lucide-react";
import { useEffect, useState } from "react";
import { getHotelById } from "@/api";
import { createBooking, confirmPayment } from "@/services/bookingService";
interface Hotel {
  id: string; // Unique identifier for the hotel
  hotelName: string; // Name of the hotel
  location: string; // Location of the hotel
  pricePerNight: number; // Price per night
  availableRooms: number; // Number of available rooms
  amenities: string; // Amenities provided (comma-separated string or change to string[])
}
interface SelectedRoomInfo {
  id: string;
  roomNumber: string;
  roomType: "STANDARD" | "DELUXE" | "SUITE" | "PENTHOUSE";
}
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useDispatch, useSelector } from "react-redux";
import SignupDialog from "@/components/SignupDialog";
import Loader from "@/components/Loader";
import { setUser } from "@/store";
import RoomGrid from "@/components/Hotel/RoomGrid";
import PriceFreezeButton from "@/components/pricing/PriceFreezeButton";
import PriceHistoryChart from "@/components/pricing/PriceHistoryChart";
import ReviewForm from "@/components/reviews/ReviewForm";
import ReviewList from "@/components/reviews/ReviewList";

const BookHotelPage = () => {
  const [quantity, setQuantity] = useState(1);
  const [numberOfNights, setNumberOfNights] = useState(1);
  const [showReviewForm, setShowReviewForm] = useState(false);
  const [reviewRefreshKey, setReviewRefreshKey] = useState(0);
  const router = useRouter();
  const { id } = router.query; // Access the hotel ID from the URL
  const [hotels, sethotels] = useState<Hotel[]>([]);
  const [loading, setLoading] = useState(true);
  const [bookingInProgress, setBookingInProgress] = useState(false);
  const [bookingError, setBookingError] = useState("");
  const [selectedRoom, setSelectedRoom] = useState<SelectedRoomInfo | null>(null);
  const user = useSelector((state: any) => state.user.user);
  const currentUserId = user?.id || user?._id;
  const [open, setopem] = useState(false);
  const dispatch = useDispatch();
  useEffect(() => {
    const fetchhotels = async () => {
      try {
        const data = await getHotelById(id as string);
        if (data) sethotels([data]);
      } catch (error) {
        console.error("Error fetching hotel:", error);
      } finally {
        setLoading(false);
      }
    };

    if (id) {
      fetchhotels();
    }
  }, [id]);

  if (loading) {
    return <Loader />;
  }

  const hotel = hotels && hotels.length > 0 ? hotels[0] : null;

  if (!hotel) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Hotel Not Found</h1>
          <p className="text-gray-600 mb-4">The hotel you're looking for doesn't exist.</p>
          <button
            onClick={() => router.push("/hotels")}
            className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-600"
          >
            Back to Hotels
          </button>
        </div>
      </div>
    );
  }
  const hotelData = {
    name: "Magnum Resorts- Near Candolim Beach",
    rating: 3,
    maxRating: 5,
    propertyPhotos: 91,
    guestPhotos: 386,
    description:
      "One of the best hotels in North Goa, operating since 2001 catering to international and domestic individual and group travelers.",
    amenities: [
      { icon: <Pool className="w-5 h-5" />, name: "Swimming Pool" },
      { icon: <UtensilsCrossed className="w-5 h-5" />, name: "Restaurant" },
      { icon: <Wine className="w-5 h-5" />, name: "Bar" },
      { icon: <Power className="w-5 h-5" />, name: "Power Backup" },
    ],
    room: {
      type: "Standard Room",
      capacity: "Fits 2 Adults",
      features: [
        "No meals included",
        "10% off on food & beverage services",
        "Complimentary welcome drinks on arrival",
        "Non-Refundable",
      ],
      originalPrice: 8999,
      discountedPrice: 664,
      taxes: 527,
    },
    location: {
      area: "Candolim",
      distance: "7 minutes walk to Candolim Beach",
    },
    reviews: {
      rating: 3.8,
      count: 784,
      text: "Very Good",
    },
  };
  const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    e.preventDefault();
    const value = Number.parseInt(e.target.value);
    if (hotel) {
      setQuantity(
        isNaN(value) ? 1 : Math.max(1, Math.min(value, hotel.availableRooms))
      );
    }
  };

  const totalPrice = (hotel?.pricePerNight || 0) * quantity * numberOfNights;
  const totalTaxes = (hotelData?.room.taxes || 0) * quantity * numberOfNights;
  const totalDiscounts = (hotelData?.room.discountedPrice || 0) * quantity * numberOfNights;
  const grandTotal = totalPrice + totalTaxes - totalDiscounts;
  
  const handlebooking = async () => {
    if (bookingInProgress) {
      return;
    }

    if (!currentUserId) {
      setBookingError("Please log in again to continue.");
      return;
    }

    if (numberOfNights < 1) {
      setBookingError("Number of nights must be at least 1");
      return;
    }

    setBookingError("");
    setBookingInProgress(true);
    try {
      // Calculate check-in and check-out dates
      const futureDate = new Date();
      futureDate.setDate(futureDate.getDate() + 7);
      const checkInDate = futureDate.toISOString().substring(0, 10);
      
      // Use the new createBooking endpoint
      const bookingData = {
        entityType: "HOTEL",
        entityId: hotel?.id,
        quantity: quantity,
        totalPrice: grandTotal,
        travelDate: checkInDate,
        numberOfNights: numberOfNights,
        userName: user?.firstName || "Guest"
      };
      
      const data = await createBooking(bookingData);

      if (!data) {
        throw new Error("Booking request failed. Please try again.");
      }

      // Confirm payment to block rooms and finalize booking
      const bookingId = data?.id || data?._id;
      if (bookingId) {
        await confirmPayment(bookingId);
      }

      const updateuser = {
        ...user,
        bookings: [...(Array.isArray(user?.bookings) ? user.bookings : []), data],
      };
      dispatch(setUser(updateuser));
      setopem(false);
      setQuantity(1);
      setNumberOfNights(1);
      if (bookingId) {
        router.push(`/booking/confirmation?bookingId=${encodeURIComponent(bookingId)}&type=hotel`);
        return;
      }
      router.push("/profile");
    } catch (error) {
      const message = error instanceof Error ? error.message : "Unable to process booking right now.";
      setBookingError(message);
    } finally {
      setBookingInProgress(false);
    }
  };
  const HotelContent = () => (
    <DialogContent className="sm:max-w-[700px] bg-white max-h-[90vh] flex flex-col overflow-hidden">
      <DialogHeader className="shrink-0">
        <DialogTitle className="text-2xl font-bold flex items-center">
          <Home className="w-6 h-6 mr-2" />
          Hotel Booking Details
        </DialogTitle>
      </DialogHeader>
      <div className="grid gap-6 mt-4 flex-1 overflow-y-auto pr-1">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Hotel Name */}
          <div className="space-y-2">
            <Label htmlFor="hotelName" className="flex items-center">
              <MapPin className="w-4 h-4 mr-2" />
              Hotel Name
            </Label>
            <Input id="hotelName" value={hotel.hotelName} readOnly />
          </div>

          {/* Location */}
          <div className="space-y-2">
            <Label htmlFor="location" className="flex items-center">
              <MapPin className="w-4 h-4 mr-2" />
              Location
            </Label>
            <Input id="location" value={hotel.location} readOnly />
          </div>
          {/* Price Per Night */}
          <div className="space-y-2">
            <Label htmlFor="pricePerNight" className="flex items-center">
              <Ticket className="w-4 h-4 mr-2" />
              Price Per Night
            </Label>
            <Input
              id="pricePerNight"
              value={`₹ ${hotel.pricePerNight}`}
              readOnly
            />
          </div>

          {/* Available Rooms */}
          <div className="space-y-2">
            <Label htmlFor="availableRooms" className="flex items-center">
              <Ticket className="w-4 h-4 mr-2" />
              Available Rooms
            </Label>
            <Input id="availableRooms" value={hotel.availableRooms} readOnly />
          </div>

          {/* Number of Rooms to Book */}
          <div className="space-y-2">
            <Label htmlFor="quantity" className="flex items-center">
              <Ticket className="w-4 h-4 mr-2" />
              Number of Rooms to Book
            </Label>
            <Input
              id="quantity"
              type="number"
              min="1"
              max={hotel.availableRooms}
              value={quantity}
              onChange={handleQuantityChange}
            />
          </div>

          {/* Number of Nights */}
          <div className="space-y-2">
            <Label htmlFor="nights" className="flex items-center">
              <Home className="w-4 h-4 mr-2" />
              Number of Nights
            </Label>
            <Input
              id="nights"
              type="number"
              min="1"
              max="365"
              value={numberOfNights}
              onChange={(e) => setNumberOfNights(Math.max(1, Number.parseInt(e.target.value) || 1))}
              className="bg-white"
            />
          </div>
          <div className="md:col-span-2 mt-4 bg-gray-50 p-4 rounded-xl border">
            <h3 className="font-semibold text-gray-800 mb-2">Select Your Room Type</h3>
            <RoomGrid
              hotelId={hotel?.id as string}
              userId={currentUserId as string}
              onRoomConfirm={(room) => setSelectedRoom(room as SelectedRoomInfo)}
            />
            {selectedRoom && (
              <p className="mt-2 text-sm text-green-700">
                Selected Room: {selectedRoom.roomNumber} ({selectedRoom.roomType})
              </p>
            )}
          </div>
        </div>
        <div className="bg-gray-100 rounded-lg p-4">
          <h3 className="text-lg font-bold mb-4 flex items-center">
            <CreditCard className="w-5 h-5 mr-2" />
            Fare Summary
          </h3>
          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">Base Fare</span>
              <span className="font-medium">
                ₹ {totalPrice.toLocaleString()}
              </span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">Taxes and Extracharges</span>
              <span className="font-medium">
                ₹ {totalTaxes.toLocaleString()}
              </span>
            </div>
            <div className="flex justify-between items-center text-green-600">
              <span className="font-medium">Discounts</span>
              <span className="font-medium">
                - ₹ {Math.abs(totalDiscounts).toLocaleString()}
              </span>
            </div>
            <div className="border-t pt-2 mt-2">
              <div className="flex justify-between items-center">
                <span className="font-bold text-lg">Total Amount</span>
                <span className="font-bold text-lg">
                  ₹ {grandTotal.toLocaleString()}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      {bookingError && (
        <p className="text-sm text-red-600 mt-2">{bookingError}</p>
      )}
      <Button className="w-full mt-4 shrink-0" onClick={handlebooking} disabled={bookingInProgress}>
        {bookingInProgress ? "Processing..." : "Proceed to Payment"}
      </Button>
    </DialogContent>
  );
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Breadcrumb */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 py-3">
          <div className="flex items-center space-x-2 text-sm">
            <a href="/" className="text-blue-500">
              Home
            </a>
            <ChevronRight className="w-4 h-4 text-gray-400" />
            <a href="/" className="text-blue-500">
              {hotel?.location}
            </a>
            <ChevronRight className="w-4 h-4 text-gray-400" />
            <span className="text-gray-600">{hotel?.hotelName}</span>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2">
            {/* Hotel Title & Rating */}
            <div className="mb-6">
              <h1 className="text-2xl font-bold mb-2">{hotel.hotelName}</h1>
              <div className="flex items-center space-x-1">
                {[...Array(hotelData.rating)].map((_, i) => (
                  <Star
                    key={i}
                    className="w-5 h-5 text-yellow-400 fill-current"
                  />
                ))}
                {[...Array(hotelData.maxRating - hotelData.rating)].map(
                  (_, i) => (
                    <Star key={i} className="w-5 h-5 text-gray-300" />
                  )
                )}
              </div>
            </div>

            {/* Image Gallery */}
            <div className="grid grid-cols-3 gap-4 mb-8">
              <div className="col-span-2 relative group cursor-pointer">
                <img
                  src="https://images.unsplash.com/photo-1566073771259-6a8506099945?auto=format&fit=crop&w=800"
                  alt="Hotel Main"
                  className="w-full h-80 object-cover rounded-lg"
                />
                <div className="absolute bottom-4 left-4 bg-white/90 px-3 py-1 rounded-full flex items-center space-x-1">
                  <Camera className="w-4 h-4" />
                  <span className="text-sm">
                    +{hotelData.propertyPhotos} Property Photos
                  </span>
                </div>
              </div>
              <div className="space-y-4">
                <div className="relative group cursor-pointer">
                  <img
                    src="https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?auto=format&fit=crop&w=800"
                    alt="Hotel Room"
                    className="w-full h-[152px] object-cover rounded-lg"
                  />
                </div>
                <div className="relative group cursor-pointer">
                  <img
                    src="https://images.unsplash.com/photo-1587474260584-136574528ed5?auto=format&fit=crop&w=800"
                    alt="Hotel Amenity"
                    className="w-full h-[152px] object-cover rounded-lg"
                  />
                  <div className="absolute bottom-4 left-4 bg-white/90 px-3 py-1 rounded-full flex items-center space-x-1">
                    <Image className="w-4 h-4" />
                    <span className="text-sm">
                      +{hotelData.guestPhotos} Guest Photos
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Description */}
            <p className="text-gray-600 mb-6">
              {hotelData.description}
              <button className="text-blue-500 ml-2">Read more</button>
            </p>

            {/* Amenities */}
            <div className="mb-8">
              <h2 className="text-xl font-semibold mb-4">Amenities</h2>
              <div className="flex flex-wrap gap-6">
                {hotelData.amenities.map((amenity, index) => (
                  <div
                    key={index}
                    className="flex items-center space-x-2 text-gray-600"
                  >
                    {amenity.icon}
                    <span>{amenity.name}</span>
                  </div>
                ))}
                <button className="text-blue-500">+ 31 Amenities</button>
              </div>
            </div>

            {/* Price History & Freeze */}
            <div className="bg-white rounded-xl shadow-sm p-6 mb-8 mt-6 border">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-lg font-bold flex items-center">
                  <CreditCard className="w-5 h-5 mr-2 text-blue-500" />
                  Dynamic Pricing & Price Freeze
                </h2>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h3 className="font-semibold mb-2 text-sm text-gray-700">Price Trend</h3>
                  <PriceHistoryChart entityId={hotel?.id as string} entityType="HOTEL" />
                </div>
                <div className="flex flex-col justify-center items-center bg-gray-50 rounded-xl p-4 border border-dashed border-gray-300">
                  <Home className="w-8 h-8 text-blue-300 mb-2" />
                  <p className="text-sm text-gray-600 mb-4 text-center">Lock in the current price and avoid future demand hikes! Your rate will be guaranteed.</p>
                  <PriceFreezeButton entityId={hotel?.id as string} entityType="HOTEL" userId={user?.id} currentPrice={grandTotal} />
                </div>
              </div>
            </div>
          </div>

          {/* Booking Card */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-lg p-6">
              <h3 className="text-xl font-semibold mb-4">
                {hotelData.room.type}
              </h3>
              <p className="text-gray-600 mb-4">{hotelData.room.capacity}</p>

              <ul className="space-y-3 mb-6">
                {hotelData.room.features.map((feature, index) => (
                  <li key={index} className="flex items-start space-x-2">
                    <span className="text-gray-400">•</span>
                    <span className="text-gray-600">{feature}</span>
                  </li>
                ))}
              </ul>
              <div className="mb-6">
                {/* Price Per Night */}
                <div className="flex items-center justify-between mb-2">
                  <span className="text-gray-800 font-semibold">
                    Price Per Night:
                  </span>
                  <span className="text-lg font-medium text-gray-800">
                    ₹ {totalPrice}
                  </span>
                </div>

                {/* Available Rooms */}
                <div className="flex items-center justify-between mb-4">
                  <span className="text-gray-800 font-semibold">
                    Available Rooms:
                  </span>
                  <span className="text-lg font-medium text-gray-800">
                    {hotel.availableRooms}
                  </span>
                </div>

                {/* Amenities */}
                <div>
                  <h4 className="text-gray-800 font-semibold mb-2">
                    Amenities:
                  </h4>
                  <p className="text-gray-600">{hotel.amenities}</p>
                </div>
              </div>
              <div className="space-y-2 mb-6">
                <div className="flex items-center justify-between">
                  <span className="text-gray-500 line-through">
                    ₹ {totalPrice}
                  </span>
                  <span className="text-gray-500">Per Night:</span>
                </div>
                <div className="flex items-center justify-between text-2xl font-bold">
                  <span>₹ {grandTotal}</span>
                  <span className="text-sm text-gray-500 font-normal">
                    + ₹ {totalTaxes} taxes & fees
                  </span>
                </div>
              </div>
              <Dialog open={open} onOpenChange={setopem}>
                <DialogTrigger asChild>
                  <button className="w-full bg-blue-500 text-white py-3 rounded-lg hover:bg-blue-600 transition-colors mb-3">
                    BOOK THIS NOW
                  </button>
                </DialogTrigger>
                {user ? (
                  <HotelContent />
                ) : (
                  <DialogContent className="bg-white">
                    <DialogHeader>
                      <DialogTitle>Login Required</DialogTitle>
                    </DialogHeader>
                    <p>Please log in to continue with your booking.</p>
                    <SignupDialog
                      trigger={
                        <Button className="w-full">Log In / Sign Up</Button>
                      }
                    />
                  </DialogContent>
                )}
              </Dialog>

              <button className="w-full text-blue-500 text-center">
                14 More Options
              </button>
            </div>

            {/* Rating Card */}
            <div className="bg-white rounded-xl shadow-lg p-6 mt-6">
              <div className="flex items-center justify-between mb-4">
                <div className="flex items-center space-x-4">
                  <div className="bg-blue-500 text-white text-2xl font-bold w-16 h-16 rounded-lg flex items-center justify-center">
                    {hotelData.reviews.rating}
                  </div>
                  <div>
                    <div className="font-semibold text-lg">
                      {hotelData.reviews.text}
                    </div>
                    <div className="text-gray-500">
                      ({hotelData.reviews.count} ratings)
                    </div>
                  </div>
                </div>
                <button
                  onClick={() => router.push(`/reviews?entityType=HOTEL&entityId=${hotel?.id}`)}
                  className="text-blue-500 hover:underline"
                >
                  All Reviews
                </button>
              </div>
            </div>

            {/* Location Card */}
            <div className="bg-white rounded-xl shadow-lg p-6 mt-6">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-semibold text-lg mb-1">
                    {hotel.location}
                  </h3>
                </div>
                <button className="text-blue-500">See on Map</button>
              </div>
            </div>

            {/* Reviews Section */}
            <div className="bg-white rounded-xl shadow-lg p-6 mt-6">
              <div className="flex justify-between items-center mb-6">
                <h3 className="font-semibold text-lg">Guest Reviews</h3>
                {currentUserId && (
                  <button
                    onClick={() => setShowReviewForm(!showReviewForm)}
                    className={`px-4 py-2 rounded-lg font-semibold transition-colors ${
                      showReviewForm
                        ? "bg-gray-200 text-gray-800 hover:bg-gray-300"
                        : "bg-blue-600 text-white hover:bg-blue-700"
                    }`}
                  >
                    {showReviewForm ? "Cancel" : "Write a Review"}
                  </button>
                )}
              </div>

              {showReviewForm && currentUserId && (
                <div className="mb-6 pb-6 border-b">
                  <ReviewForm
                    entityType="HOTEL"
                    entityId={hotel?.id as string}
                    userId={currentUserId}
                    userName={user ? `${user.firstName} ${user.lastName}`.trim() : "Anonymous"}
                    onSuccess={() => {
                      setShowReviewForm(false);
                      setReviewRefreshKey((prev) => prev + 1);
                    }}
                  />
                </div>
              )}

              <ReviewList
                entityType="HOTEL"
                entityId={hotel?.id as string}
                currentUserId={currentUserId}
                currentUserName={user ? `${user.firstName} ${user.lastName}`.trim() : "Anonymous"}
                refreshTrigger={reviewRefreshKey}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookHotelPage;
