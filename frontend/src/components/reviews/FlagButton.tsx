import React, { useState } from 'react';
import { Flag } from 'lucide-react';

interface FlagButtonProps {
  reviewId: string;
  userId?: string;
  onFlag: (reviewId: string, reason: string) => Promise<void>;
}

const FLAG_REASONS = [
  'Spam or misleading',
  'Inappropriate content',
  'Hate speech or abusive',
  'Off-topic',
  'Other'
];

export const FlagButton: React.FC<FlagButtonProps> = ({
  reviewId,
  userId,
  onFlag
}) => {
  const [showDialog, setShowDialog] = useState(false);
  const [selectedReason, setSelectedReason] = useState('');
  const [loading, setLoading] = useState(false);
  const [flagged, setFlagged] = useState(false);

  const handleSubmit = async () => {
    if (!selectedReason) {
      alert('Please select a reason');
      return;
    }

    if (!userId) {
      alert('Please login to flag reviews');
      return;
    }

    setLoading(true);
    try {
      await onFlag(reviewId, selectedReason);
      setFlagged(true);
      setShowDialog(false);
      alert('Review flagged successfully. Our team will review it.');
    } catch (error) {
      console.error('Flag failed:', error);
      alert('Failed to flag review. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (flagged) {
    return (
      <span className="text-sm text-gray-500 italic">Flagged</span>
    );
  }

  return (
    <>
      <button
        onClick={() => setShowDialog(true)}
        className="text-gray-500 hover:text-red-600 transition-colors"
        title="Flag as inappropriate"
      >
        <Flag className="w-4 h-4" />
      </button>

      {showDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
            <h3 className="text-lg font-semibold mb-4">Flag Review</h3>
            
            <div className="space-y-2 mb-6">
              <p className="text-sm text-gray-600 mb-3">
                Why are you flagging this review?
              </p>
              {FLAG_REASONS.map((reason) => (
                <label key={reason} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="reason"
                    value={reason}
                    checked={selectedReason === reason}
                    onChange={(e) => setSelectedReason(e.target.value)}
                    className="text-blue-600"
                  />
                  <span className="text-sm">{reason}</span>
                </label>
              ))}
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setShowDialog(false)}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-50"
              >
                Cancel
              </button>
              <button
                onClick={handleSubmit}
                disabled={loading}
                className="flex-1 px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50"
              >
                {loading ? 'Flagging...' : 'Flag Review'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};
