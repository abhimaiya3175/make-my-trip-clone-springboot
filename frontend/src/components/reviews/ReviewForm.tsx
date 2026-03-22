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
  const [photos, setPhotos] = useState<File[]>([]);
  const [photoPreviews, setPhotoPreviews] = useState<string[]>([]);
  const API_BASE = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
  const [requestState, setRequestState] = useState(REQUEST_STATE.IDLE);
  const [error, setError] = useState<string | null>(null);

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files) return;
    const selectedFiles = Array.from(e.target.files);
    
    if (photos.length + selectedFiles.length > 5) {
      setError('Maximum 5 photos allowed');
      return;
    }

    for (const file of selectedFiles) {
      if (file.size > 5 * 1024 * 1024) {
        setError(`File ${file.name} exceeds 5MB limit`);
        return;
      }
      if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) {
        setError(`File ${file.name} is not a supported format (JPEG, PNG, WEBP)`);
        return;
      }
    }

    const newPhotos = [...photos, ...selectedFiles];
    setPhotos(newPhotos);
    
    // Generate previews
    const newPreviews = selectedFiles.map(f => URL.createObjectURL(f));
    setPhotoPreviews([...photoPreviews, ...newPreviews]);
    setError(null);
    
    // Reset input
    e.target.value = '';
  };

  const handleRemovePhoto = (index: number) => {
    // Revoke object URL to avoid memory leaks
    URL.revokeObjectURL(photoPreviews[index]);
    
    setPhotos(photos.filter((_, i) => i !== index));
    setPhotoPreviews(photoPreviews.filter((_, i) => i !== index));
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
      const token =
        typeof window !== "undefined" && localStorage
          ? localStorage.getItem("authToken")
          : null;

      if (!token) {
        throw new Error("Authentication required. Please login again.");
      }

      const uploadedUrls: string[] = [];
      for (const file of photos) {
        const formData = new FormData();
        formData.append('photo', file);

        const res = await fetch(`${API_BASE}/api/reviews/upload-photo`, {
          method: 'POST',
          headers: {
            Authorization: `Bearer ${token}`,
          },
          body: formData,
        });
        
        const json = await res.json();
        if (!res.ok) {
          throw new Error(json.error?.message || 'Failed to upload photo');
        }
        uploadedUrls.push(json.data);
      }

      await createReview(userId, userName || 'Anonymous', {
        entityId,
        entityType,
        rating,
        text: text.trim(),
        photos: uploadedUrls.length > 0 ? uploadedUrls : undefined
      });

      setRequestState(REQUEST_STATE.SUCCESS);
      
      // Reset form
      setRating(0);
      setText('');
      setPhotos([]);
      photoPreviews.forEach(p => URL.revokeObjectURL(p));
      setPhotoPreviews([]);
      
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
          
          <div className="flex gap-2 mb-3">
            <input
              type="file"
              accept="image/jpeg, image/png, image/webp"
              multiple
              onChange={handleFileSelect}
              disabled={photos.length >= 5}
              id="photo-upload"
              className="hidden"
            />
            <label
              htmlFor="photo-upload"
              className={`px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 flex items-center gap-2 cursor-pointer ${
                photos.length >= 5 ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              <Image className="w-4 h-4" />
              Select Photos
            </label>
            <span className="text-sm text-gray-400 mt-2">
              Max 5MB per file (JPEG, PNG, WEBP)
            </span>
          </div>

          {/* Photo Preview */}
          {photoPreviews.length > 0 && (
            <div className="flex gap-2 flex-wrap">
              {photoPreviews.map((preview, index) => (
                <div key={index} className="relative">
                  <img
                    src={preview}
                    alt={`Preview ${index + 1}`}
                    className="w-20 h-20 object-cover rounded-md border border-gray-200"
                  />
                  <button
                    type="button"
                    onClick={() => handleRemovePhoto(index)}
                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 hover:bg-red-600 shadow-sm"
                  >
                    <X className="w-3 h-3" />
                  </button>
                  {requestState === REQUEST_STATE.LOADING && (
                    <div className="absolute inset-0 bg-white/60 flex items-center justify-center rounded-md">
                      <div className="w-4 h-4 border-2 border-blue-600 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                  )}
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
