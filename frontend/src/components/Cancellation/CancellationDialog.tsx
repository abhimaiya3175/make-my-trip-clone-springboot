import React, { useState, useEffect } from "react";
import {
  AlertCircle,
  X,
  ChevronDown,
  Clock,
  IndianRupee,
  Info,
  Loader2,
  CheckCircle2,
  MinusCircle,
  PlusCircle,
} from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { getCancellationPreview, processCancellation, getCancellationReasons } from "@/api";

interface CancellationDialogProps {
  open: boolean;
  onClose: () => void;
  booking: any;
  userId: string;
  onCancellationComplete: (result: any) => void;
}

const CancellationDialog: React.FC<CancellationDialogProps> = ({
  open,
  onClose,
  booking,
  userId,
  onCancellationComplete,
}) => {
  const [step, setStep] = useState<"reason" | "preview" | "confirm" | "success">("reason");
  const [selectedReason, setSelectedReason] = useState("");
  const [additionalNotes, setAdditionalNotes] = useState("");
  const [quantityToCancel, setQuantityToCancel] = useState(booking?.quantity || 1);
  const [preview, setPreview] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [result, setResult] = useState<any>(null);
  const [reasons, setReasons] = useState<Record<string, string>>({});

  const cancellationReasons: Record<string, string> = {
    CHANGE_OF_PLANS: "Change of plans",
    FOUND_BETTER_PRICE: "Found better price",
    MEDICAL_REASON: "Medical reason",
    BOOKING_MISTAKE: "Booking mistake",
    OTHER: "Other",
  };

  useEffect(() => {
    if (open) {
      console.log("[CancellationDialog] Opened with:", { userId, booking });
      if (!userId) {
        console.error("[CancellationDialog] ERROR: userId is empty or undefined!");
        setError("User ID not found. Please refresh and try again.");
      }
      setStep("reason");
      setSelectedReason("");
      setAdditionalNotes("");
      setQuantityToCancel(booking?.quantity || 1);
      setPreview(null);
      setError("");
      setResult(null);

      getCancellationReasons()
        .then((data) => setReasons(data))
        .catch(() => setReasons(cancellationReasons));
    }
  }, [open, booking, userId]);

  const getTravelDateTime = () => {
    console.log("[CancellationDialog] getTravelDateTime - booking.date:", booking?.date);

    if (!booking?.date) {
      // No date available - default to 48 hours from now
      const future = new Date(Date.now() + 48 * 60 * 60 * 1000);
      const result = formatLocalDateTime(future);
      console.warn("[CancellationDialog] No booking date, using fallback:", result);
      return result;
    }

    // Parse YYYY-MM-DD as local date to avoid timezone shift
    const parts = booking.date.split("-");
    if (parts.length === 3) {
      const year = parseInt(parts[0]);
      const month = parseInt(parts[1]) - 1;
      const day = parseInt(parts[2]);
      console.log("[CancellationDialog] Parsed date parts:", { year, month, day });

      const d = new Date(year, month, day, 23, 59, 0);
      if (!isNaN(d.getTime())) {
        const result = formatLocalDateTime(d);
        console.log("[CancellationDialog] Successfully formatted date:", result);
        return result;
      }
    }

    // Fallback for non-standard date strings
    const d = new Date(booking.date + "T23:59:00");
    if (!isNaN(d.getTime())) {
      const result = formatLocalDateTime(d);
      console.log("[CancellationDialog] Fallback datetime format worked:", result);
      return result;
    }

    // Last fallback
    const future = new Date(Date.now() + 48 * 60 * 60 * 1000);
    const result = formatLocalDateTime(future);
    console.error("[CancellationDialog] Could not parse date, using final fallback:", result);
    return result;
  };

  // Format Date as ISO LocalDateTime string without timezone (YYYY-MM-DDTHH:mm:ss)
  const formatLocalDateTime = (d: Date): string => {
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`;
  };

  const handleFetchPreview = async () => {
    if (!userId) {
      console.error("[CancellationDialog] handleFetchPreview - userId is empty!");
      setError("User ID is missing. Please refresh the page and try again.");
      return;
    }

    setLoading(true);
    setError("");
    try {
      const travelDate = getTravelDateTime();
      console.log("[CancellationDialog] Fetching preview:", {
        bookingId: booking.id || booking.bookingId,
        bookingType: booking.type?.toUpperCase() === "HOTEL" ? "HOTEL" : "FLIGHT",
        quantityToCancel,
        totalQuantity: booking.quantity,
        totalPrice: booking.totalPrice,
        travelDateTime: travelDate,
        rawBookingDate: booking.date,
      });

      const data = await getCancellationPreview(
        booking.id || booking.bookingId,
        booking.type?.toUpperCase() === "HOTEL" ? "HOTEL" : "FLIGHT",
        quantityToCancel,
        booking.quantity,
        booking.totalPrice,
        travelDate
      );
      console.log("[CancellationDialog] Preview response:", data);
      setPreview(data);
      setStep("preview");
    } catch (err: any) {
      console.error("[CancellationDialog] Preview error:", err?.response?.data || err);
      setError(err?.response?.data?.error || "Failed to fetch cancellation preview");
    }
    setLoading(false);
  };

  const handleConfirmCancellation = async () => {
    if (!userId) {
      console.error("[CancellationDialog] handleConfirmCancellation - userId is empty!");
      setError("User ID is missing. Please refresh the page and try again.");
      return;
    }

    setLoading(true);
    setError("");
    try {
      const travelDate = getTravelDateTime();
      const cancellationRequest = {
        bookingId: booking.id || booking.bookingId,
        bookingType: booking.type?.toUpperCase() === "HOTEL" ? "HOTEL" : "FLIGHT",
        reason: selectedReason,
        quantityToCancel,
        additionalNotes,
      };

      console.log("[CancellationDialog] Confirming cancellation:", {
        userId,
        cancellationRequest,
        totalQuantity: booking.quantity,
        totalPrice: booking.totalPrice,
        travelDateTime: travelDate,
      });

      const data = await processCancellation(
        userId,
        cancellationRequest,
        booking.quantity,
        booking.totalPrice,
        travelDate
      );

      console.log("[CancellationDialog] Cancellation response:", data);

      if (data.success) {
        setResult(data);
        setStep("success");

        // Close dialog and call completion handler
        // Use setTimeout to show success briefly before redirecting
        setTimeout(() => {
          onCancellationComplete(data);
        }, 1500);
      } else {
        console.error("[CancellationDialog] Cancellation failed:", data.message);
        setError(data.message || "Cancellation failed");
      }
    } catch (err: any) {
      console.error("[CancellationDialog] Cancellation error:", err?.response?.data || err);
      setError(
        err?.response?.data?.error || "Failed to process cancellation. Please try again."
      );
    }
    setLoading(false);
  };

  const reasonEntries = Object.keys(reasons).length > 0 ? reasons : cancellationReasons;

  return (
    <Dialog open={open} onOpenChange={(isOpen) => !isOpen && onClose()}>
      <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2 text-xl">
            {step === "success" ? (
              <>
                <CheckCircle2 className="w-6 h-6 text-green-600" />
                Cancellation Successful
              </>
            ) : (
              <>
                <AlertCircle className="w-6 h-6 text-red-500" />
                Cancel Booking
              </>
            )}
          </DialogTitle>
        </DialogHeader>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg text-sm flex items-start gap-2">
            <AlertCircle className="w-4 h-4 mt-0.5 flex-shrink-0" />
            {error}
          </div>
        )}

        {/* Step 1: Select reason */}
        {step === "reason" && (
          <div className="space-y-5">
            {/* Booking summary */}
            <div className="bg-gray-50 rounded-lg p-4 space-y-2">
              <p className="font-semibold text-gray-800">Booking Details</p>
              <div className="grid grid-cols-2 gap-2 text-sm text-gray-600">
                <p>Type: <span className="font-medium text-gray-800">{booking?.type}</span></p>
                <p>ID: <span className="font-medium text-gray-800">{booking?.id || booking?.bookingId}</span></p>
                <p>Quantity: <span className="font-medium text-gray-800">{booking?.quantity} {booking?.type === "Flight" ? "seat(s)" : "room(s)"}</span></p>
                <p>Price: <span className="font-medium text-gray-800">₹{booking?.totalPrice?.toLocaleString("en-IN")}</span></p>
                <p>Date: <span className="font-medium text-gray-800">{booking?.date}</span></p>
              </div>
            </div>

            {/* Partial cancellation */}
            {booking?.quantity > 1 && (
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">
                  How many {booking?.type === "Flight" ? "seats" : "rooms"} to cancel?
                </label>
                <div className="flex items-center gap-3">
                  <button
                    onClick={() => setQuantityToCancel(Math.max(1, quantityToCancel - 1))}
                    className="p-1 rounded-full hover:bg-gray-100"
                    disabled={quantityToCancel <= 1}
                  >
                    <MinusCircle className={`w-6 h-6 ${quantityToCancel <= 1 ? "text-gray-300" : "text-red-500"}`} />
                  </button>
                  <span className="text-xl font-bold w-10 text-center">{quantityToCancel}</span>
                  <button
                    onClick={() => setQuantityToCancel(Math.min(booking.quantity, quantityToCancel + 1))}
                    className="p-1 rounded-full hover:bg-gray-100"
                    disabled={quantityToCancel >= booking.quantity}
                  >
                    <PlusCircle className={`w-6 h-6 ${quantityToCancel >= booking.quantity ? "text-gray-300" : "text-green-500"}`} />
                  </button>
                  <span className="text-sm text-gray-500">of {booking.quantity}</span>
                </div>
                {quantityToCancel < booking.quantity && (
                  <p className="text-xs text-blue-600 flex items-center gap-1">
                    <Info className="w-3 h-3" />
                    Partial cancellation: {booking.quantity - quantityToCancel} {booking?.type === "Flight" ? "seat(s)" : "room(s)"} will remain active
                  </p>
                )}
              </div>
            )}

            {/* Reason dropdown */}
            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">
                Cancellation Reason <span className="text-red-500">*</span>
              </label>
              <div className="relative">
                <select
                  value={selectedReason}
                  onChange={(e) => setSelectedReason(e.target.value)}
                  className="w-full px-3 py-2.5 border border-gray-300 rounded-lg appearance-none bg-white focus:ring-2 focus:ring-red-500 focus:border-red-500 text-sm"
                >
                  <option value="">-- Select a reason --</option>
                  {Object.entries(reasonEntries).map(([key, value]) => (
                    <option key={key} value={key}>
                      {value}
                    </option>
                  ))}
                </select>
                <ChevronDown className="w-4 h-4 text-gray-400 absolute right-3 top-3 pointer-events-none" />
              </div>
            </div>

            {/* Additional notes */}
            <div className="space-y-2">
              <label className="block text-sm font-medium text-gray-700">
                Additional Notes (optional)
              </label>
              <textarea
                value={additionalNotes}
                onChange={(e) => setAdditionalNotes(e.target.value)}
                placeholder="Any additional information..."
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 text-sm resize-none"
                rows={3}
              />
            </div>

            {/* Actions */}
            <div className="flex gap-3 pt-2">
              <Button variant="outline" onClick={onClose} className="flex-1">
                Keep Booking
              </Button>
              <Button
                onClick={handleFetchPreview}
                disabled={!selectedReason || loading}
                className="flex-1 bg-red-600 hover:bg-red-700 text-white"
              >
                {loading ? (
                  <Loader2 className="w-4 h-4 animate-spin mr-2" />
                ) : null}
                View Refund Details
              </Button>
            </div>
          </div>
        )}

        {/* Step 2: Preview refund */}
        {step === "preview" && preview && (
          <div className="space-y-5">
            {/* Refund policy banner */}
            <div
              className={`rounded-lg p-4 flex items-start gap-3 ${preview.eligibleFor90Percent
                  ? "bg-green-50 border border-green-200"
                  : "bg-yellow-50 border border-yellow-200"
                }`}
            >
              <Clock
                className={`w-5 h-5 mt-0.5 flex-shrink-0 ${preview.eligibleFor90Percent ? "text-green-600" : "text-yellow-600"
                  }`}
              />
              <div>
                <p
                  className={`font-medium text-sm ${preview.eligibleFor90Percent ? "text-green-800" : "text-yellow-800"
                    }`}
                >
                  {preview.refundPolicy}
                </p>
                <p className="text-xs text-gray-600 mt-1">
                  Time until travel: {preview.hoursUntilTravel}
                </p>
              </div>
            </div>

            {/* Refund breakdown */}
            <div className="bg-gray-50 rounded-lg p-4 space-y-3">
              <p className="font-semibold text-gray-800">Refund Breakdown</p>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between text-gray-600">
                  <span>Original Price ({preview.totalQuantity} {booking?.type === "Flight" ? "seat(s)" : "room(s)"})</span>
                  <span>₹{preview.originalPrice?.toLocaleString("en-IN")}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Price per {booking?.type === "Flight" ? "seat" : "room"}</span>
                  <span>₹{preview.pricePerUnit?.toLocaleString("en-IN")}</span>
                </div>
                <div className="flex justify-between text-gray-600">
                  <span>Cancelling {preview.quintityToCancel} of {preview.totalQuantity}</span>
                  <span>₹{preview.cancellationPrice?.toLocaleString("en-IN")}</span>
                </div>
                <hr className="border-gray-300" />
                <div className="flex justify-between text-gray-600">
                  <span>Refund Percentage</span>
                  <span className="font-medium text-gray-800">{preview.refundPercentage}%</span>
                </div>
                <div className="flex justify-between font-bold text-lg text-green-700 pt-1">
                  <span>Your Refund</span>
                  <span className="flex items-center">
                    <IndianRupee className="w-4 h-4" />
                    {preview.refundAmount?.toLocaleString("en-IN")}
                  </span>
                </div>
              </div>
              {preview.remainingQuantity > 0 && (
                <p className="text-xs text-blue-600 flex items-center gap-1 mt-2">
                  <Info className="w-3 h-3" />
                  {preview.remainingQuantity} {booking?.type === "Flight" ? "seat(s)" : "room(s)"} will remain active
                </p>
              )}
            </div>

            {/* Cancellation reason */}
            <div className="text-sm text-gray-600">
              <p>
                <span className="font-medium">Reason:</span>{" "}
                {reasonEntries[selectedReason] || selectedReason}
              </p>
              {additionalNotes && (
                <p className="mt-1">
                  <span className="font-medium">Notes:</span> {additionalNotes}
                </p>
              )}
            </div>

            {/* Actions */}
            <div className="flex gap-3 pt-2">
              <Button variant="outline" onClick={() => setStep("reason")} className="flex-1">
                Go Back
              </Button>
              <Button
                onClick={handleConfirmCancellation}
                disabled={loading}
                className="flex-1 bg-red-600 hover:bg-red-700 text-white"
              >
                {loading ? (
                  <Loader2 className="w-4 h-4 animate-spin mr-2" />
                ) : null}
                Confirm Cancellation
              </Button>
            </div>
          </div>
        )}

        {/* Step 3: Success */}
        {step === "success" && result && (
          <div className="space-y-5">
            <div className="bg-green-50 border border-green-200 rounded-lg p-4 text-center space-y-2">
              <CheckCircle2 className="w-12 h-12 text-green-500 mx-auto" />
              <p className="font-semibold text-green-800 text-lg">{result.message}</p>
              <p className="text-sm text-green-700">Redirecting to cancellations page...</p>
            </div>

            <div className="bg-gray-50 rounded-lg p-4 space-y-2 text-sm">
              <div className="flex justify-between">
                <span className="text-gray-600">Cancellation ID</span>
                <span className="font-mono font-medium">{result.cancellationId}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Status</span>
                <span className="font-medium">{result.newBookingStatus}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Cancelled</span>
                <span className="font-medium">
                  {result.cancelledQuantity} of {result.totalQuantity} {booking?.type === "Flight" ? "seat(s)" : "room(s)"}
                </span>
              </div>
              <div className="flex justify-between text-green-700 font-semibold">
                <span>Refund Amount</span>
                <span>₹{result.refundAmount?.toLocaleString("en-IN")}</span>
              </div>
            </div>

            {/* Refund status preview */}
            {result.refundTracker && (
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
                <p className="font-medium text-blue-800 text-sm mb-3">Refund Status</p>
                <div className="flex justify-between items-center">
                  {["Cancellation Requested", "Refund Initiated", "Processing", "Refunded"].map(
                    (stage, index) => {
                      const isCompleted =
                        index <=
                        ["CANCELLATION_REQUESTED", "REFUND_INITIATED", "PROCESSING", "REFUNDED"].indexOf(
                          result.refundTracker.status
                        );
                      return (
                        <div key={stage} className="flex flex-col items-center flex-1">
                          <div
                            className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${isCompleted
                                ? "bg-blue-600 text-white"
                                : "bg-gray-200 text-gray-400"
                              }`}
                          >
                            {isCompleted ? "✓" : index + 1}
                          </div>
                          <span className="text-[10px] text-center text-gray-600 mt-1 leading-tight">
                            {stage}
                          </span>
                        </div>
                      );
                    }
                  )}
                </div>
              </div>
            )}

            <Button onClick={onClose} className="w-full bg-gray-800 hover:bg-gray-900 text-white">
              Close
            </Button>
          </div>
        )}
      </DialogContent>
    </Dialog>
  );
};

export default CancellationDialog;
