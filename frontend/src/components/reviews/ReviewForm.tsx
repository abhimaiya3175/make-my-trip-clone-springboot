import React, { useState } from "react";
import StarRating from "./StarRating";
import { createReview } from "@/services/reviewService";
import { REQUEST_STATE, getErrorMessage } from "@/utils/requestState";
import { Image, X } from "lucide-react";

interface ReviewFormProps {
  entityType: 'FLIGHT' | 'HOTEL';
  entityId: string;
  userId?: string;
  userName?: string;
  onSuccess?: () => void;
}

const ReviewForm: React.FC<ReviewFormProps> = ({ 
  entityType, 
  entityId, 
  userId, 
  userName,
  onSuccess 
}) => {
  const [rating, setRating] = useState(0);
  const [text, setText] = useState('');
  const [photos, setPhotos] = useState<string[]>([]);
  const [photoInput, setPhotoInput] = useState('');
  const [requestState, setRequestState] = useState(REQUEST_STATE.IDLE);
  const [error, setError] = useState<string | null>(null);

  const handleAddPhoto = () => {
    if (!photoInput.trim()) return;
    
    if (photos.length >= 5) {
      setError('Maximum 5 photos allowed');
      return;
    }

    // Basic URL validation
    try {
      new URL(photoInput);
      setPhotos([...photos, photoInput.trim()]);
      setPhotoInput('');
      setError(null);
    } catch {
      setError('Please enter a valid URL');
    }
  };

  const handleRemovePhoto = (index: number) => {
    setPhotos(photos.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    // Validation
    if (!userId) {
      setError('Please login to submit a review');
      return;
    }

    if (rating === 0) {
      setError('Please select a rating');
      return;
    }

    if (!text.trim()) {
      setError('Please write a review');
      return;
    }

    if (text.length > 2000) {
      setError('Review must be less than 2000 characters');
      return;
    }

    setRequestState(REQUEST_STATE.LOADING);

    try {
      await createReview(userId, userName || 'Anonymous', {
        entityId,
        entityType,
        rating,
        text: text.trim(),
        photos: photos.length > 0 ? photos : undefined
      });

      setRequestState(REQUEST_STATE.SUCCESS);
      
      // Reset form
      setRating(0);
      setText('');
      setPhotos([]);
      
      // Call success callback
      if (onSuccess) {
        onSuccess();
      }
    } catch (err) {
      setRequestState(REQUEST_STATE.ERROR);
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="bg-white p-6 border border-gray-200 rounded-lg shadow-sm">
      <h3 className="font-semibold text-lg mb-4">Write a Review</h3>
      
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* Rating */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Rating <span className="text-red-500">*</span>
          </label>
          <StarRating 
            rating={rating} 
            onRatingChange={setRating}
            size="lg"
          />
        </div>

        {/* Review Text */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Your Review <span className="text-red-500">*</span>
          </label>
          <textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="Share your experience..."
            className="w-full p-3 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent resize-none"
            rows={5}
            maxLength={2000}
          />
          <div className="text-xs text-gray-500 mt-1 text-right">
            {text.length} / 2000 characters
          </div>
        </div>

        {/* Photos */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Photos (optional, max 5)
          </label>
          
          {/* Photo Input */}
          <div className="flex gap-2 mb-3">
            <input
              type="text"
              value={photoInput}
              onChange={(e) => setPhotoInput(e.target.value)}
              placeholder="Enter image URL"
              className="flex-1 p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  e.preventDefault();
                  handleAddPhoto();
                }
              }}
            />
            <button
              type="button"
              onClick={handleAddPhoto}
              disabled={photos.length >= 5}
              className="px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
            >
              <Image className="w-4 h-4" />
              Add
            </button>
          </div>

          {/* Photo Preview */}
          {photos.length > 0 && (
            <div className="flex gap-2 flex-wrap">
              {photos.map((photo, index) => (
                <div key={index} className="relative">
                  <img
                    src={photo}
                    alt={`Preview ${index + 1}`}
                    className="w-20 h-20 object-cover rounded-md border border-gray-200"
                  />
                  <button
                    type="button"
                    onClick={() => handleRemovePhoto(index)}
                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600"
                  >
                    <X className="w-3 h-3" />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Error Message */}
        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">
            {error}
          </div>
        )}

        {/* Success Message */}
        {requestState === REQUEST_STATE.SUCCESS && (
          <div className="p-3 bg-green-50 border border-green-200 rounded-md text-sm text-green-700">
            Review submitted successfully!
          </div>
        )}

        {/* Submit Button */}
        <button
          type="submit"
          disabled={requestState === REQUEST_STATE.LOADING}
          className="w-full py-3 bg-blue-600 text-white rounded-md font-medium hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
        >
          {requestState === REQUEST_STATE.LOADING ? 'Submitting...' : 'Submit Review'}
        </button>
      </form>
    </div>
  );
};

export default ReviewForm;
