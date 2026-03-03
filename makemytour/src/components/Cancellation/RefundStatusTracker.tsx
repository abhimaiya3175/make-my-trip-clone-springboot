import React from "react";
import { Clock, CheckCircle2, ArrowRight, IndianRupee } from "lucide-react";

interface RefundTrackerData {
  id: string;
  cancellationId: string;
  status: string;
  statusDisplay: string;
  refundAmount: number;
  createdAt?: string;
  updatedAt?: string;
  notes?: string;
}

interface RefundStatusTrackerProps {
  tracker: RefundTrackerData;
  compact?: boolean;
}

const STAGES = [
  { key: "CANCELLATION_REQUESTED", label: "Requested", color: "text-yellow-600", bg: "bg-yellow-500" },
  { key: "REFUND_INITIATED", label: "Initiated", color: "text-blue-600", bg: "bg-blue-500" },
  { key: "PROCESSING", label: "Processing", color: "text-orange-600", bg: "bg-orange-500" },
  { key: "REFUNDED", label: "Refunded", color: "text-green-600", bg: "bg-green-500" },
];

const RefundStatusTracker: React.FC<RefundStatusTrackerProps> = ({ tracker, compact = false }) => {
  const currentIndex = STAGES.findIndex((s) => s.key === tracker.status);

  if (compact) {
    const currentStage = STAGES[currentIndex] || STAGES[0];
    return (
      <div className="flex items-center gap-2">
        <span
          className={`inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium ${
            tracker.status === "REFUNDED"
              ? "bg-green-100 text-green-700"
              : tracker.status === "PROCESSING"
              ? "bg-orange-100 text-orange-700"
              : tracker.status === "REFUND_INITIATED"
              ? "bg-blue-100 text-blue-700"
              : "bg-yellow-100 text-yellow-700"
          }`}
        >
          {tracker.status === "REFUNDED" ? (
            <CheckCircle2 className="w-3 h-3" />
          ) : (
            <Clock className="w-3 h-3" />
          )}
          {currentStage.label}
        </span>
        <span className="text-xs text-green-700 font-semibold">
          ₹{tracker.refundAmount?.toLocaleString("en-IN")}
        </span>
      </div>
    );
  }

  return (
    <div className="bg-white border border-gray-200 rounded-xl p-5 space-y-4">
      <div className="flex items-center justify-between">
        <h4 className="font-semibold text-gray-800">Refund Status</h4>
        <span className="flex items-center text-green-700 font-bold text-lg">
          <IndianRupee className="w-4 h-4" />
          {tracker.refundAmount?.toLocaleString("en-IN")}
        </span>
      </div>

      {/* Timeline */}
      <div className="relative">
        <div className="flex items-center justify-between">
          {STAGES.map((stage, index) => {
            const isCompleted = index <= currentIndex;
            const isCurrent = index === currentIndex;
            return (
              <React.Fragment key={stage.key}>
                <div className="flex flex-col items-center z-10 relative">
                  <div
                    className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold transition-all ${
                      isCompleted
                        ? `${stage.bg} text-white shadow-md`
                        : "bg-gray-200 text-gray-400"
                    } ${isCurrent ? "ring-2 ring-offset-2 ring-blue-400" : ""}`}
                  >
                    {isCompleted ? "✓" : index + 1}
                  </div>
                  <span
                    className={`text-[11px] text-center mt-1.5 leading-tight font-medium ${
                      isCompleted ? stage.color : "text-gray-400"
                    }`}
                  >
                    {stage.label}
                  </span>
                </div>
                {index < STAGES.length - 1 && (
                  <div className="flex-1 mx-1">
                    <div
                      className={`h-1 rounded-full ${
                        index < currentIndex ? "bg-blue-400" : "bg-gray-200"
                      }`}
                    />
                  </div>
                )}
              </React.Fragment>
            );
          })}
        </div>
      </div>

      {/* Details */}
      <div className="text-xs text-gray-500 space-y-1 pt-2 border-t border-gray-100">
        {tracker.updatedAt && (
          <p>Last updated: {new Date(tracker.updatedAt).toLocaleString("en-IN")}</p>
        )}
        {tracker.notes && (
          <p className="text-gray-600">
            <span className="font-medium">Note:</span> {tracker.notes}
          </p>
        )}
      </div>
    </div>
  );
};

export default RefundStatusTracker;
